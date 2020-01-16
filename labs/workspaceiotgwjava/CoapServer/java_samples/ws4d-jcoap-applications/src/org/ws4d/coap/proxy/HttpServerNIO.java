/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */
package org.ws4d.coap.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpRequestHandlerRegistry;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.log4j.Logger;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 * 
 * TODO: IMPROVE Async Server Implementation, avoid ModifiedAsyncNHttpServiceHandler and deprecated function calls
 */

public class HttpServerNIO extends Thread{
	static Logger logger = Logger.getLogger(Proxy.class);
	static private int PORT = 8080;
	
	ProxyMapper mapper = ProxyMapper.getInstance();
	
	//interface-function for other classes/modules
	public void sendResponse(ProxyMessageContext context) {
    	HttpResponse httpResponse = context.getOutHttpResponse();
    	NHttpResponseTrigger trigger = context.getTrigger();
   		trigger.submitResponse(httpResponse);
	}
	
	public void run() {
		
		this.setName("HTTP_NIO_Server");				
		
		//parameters for connection
        HttpParams params = new SyncBasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
                
        //needed by framework, don't need any processors except the connection-control
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                new ResponseConnControl()
        });
        
        //create own service-handler with bytecontentlistener-class
        ModifiedAsyncNHttpServiceHandler handler = new ModifiedAsyncNHttpServiceHandler(
                httpproc, new DefaultHttpResponseFactory(),
                new DefaultConnectionReuseStrategy(), params);
        
        // Set up request handlers, use the same request-handler for all uris
        NHttpRequestHandlerRegistry reqistry = new NHttpRequestHandlerRegistry();
        reqistry.register("*", new ProxyHttpRequestHandler());
        handler.setHandlerResolver(reqistry);
        
        
		try {
			//create and start responder-thread
			//ioreactor is used by nio-framework to listen and react to http connections
			//2 dispatcher-threads are used to do the work
			ListeningIOReactor ioReactor = new DefaultListeningIOReactor(2, params);

			IOEventDispatch ioeventdispatch = new DefaultServerIOEventDispatch(handler, params);

			ioReactor.listen(new InetSocketAddress(PORT));
			ioReactor.execute(ioeventdispatch);
			
		} catch (IOReactorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	static class ProxyHttpRequestHandler implements NHttpRequestHandler  {


        public ProxyHttpRequestHandler() {
            super();
        }

		@Override
		public ConsumingNHttpEntity entityRequest(
				HttpEntityEnclosingRequest arg0, HttpContext arg1)
				throws HttpException, IOException {
			return null;
		}

		//handle() is called when a request is received
		//response is automatically generated by HttpProcessor, but use response from mapper
		//trigger is used for asynchronous response, see java-documentation
		@Override
		public void handle(final HttpRequest request, final HttpResponse response,
				final NHttpResponseTrigger trigger, HttpContext con)
				throws HttpException, IOException {
			logger.info("incomming HTTP request");
			URI uri = ProxyMapper.resolveHttpRequestUri(request);
			if (uri != null){
				InetAddress serverAddress = InetAddress.getByName(uri.getHost()); //FIXME: blocking operation??? 
				int serverPort = uri.getPort();
				if (serverPort == -1) {
					serverPort = org.ws4d.coap.Constants.COAP_DEFAULT_PORT;
				}
				/* translate always */
				ProxyMessageContext context = new ProxyMessageContext(request, true, uri, trigger);
				context.setServerAddress(serverAddress, serverPort);
				ProxyMapper.getInstance().handleHttpServerRequest(context); 
			} else {
				trigger.submitResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "Bad Header: Host"));
			}
		}
    }
}
