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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.CoapResponseCode;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

public class CoapServerProxy implements CoapServer{
	static Logger logger = Logger.getLogger(Proxy.class);
	
    private static final int LOCAL_PORT = 5683;					//port on which the server is listening
    ProxyMapper mapper = ProxyMapper.getInstance();
    
    //coapOUTq_ receives a coap-response from mapper in case of coap-http
    CoapChannelManager channelManager;
    

    
    //constructor of coapserver-class, initiates the jcoap-components and starts CoapSender
    public CoapServerProxy() {

        channelManager = BasicCoapChannelManager.getInstance();
        channelManager.createServerListener(this, LOCAL_PORT);
    }
    
    //interface-function for the message-queue
    public void sendResponse(ProxyMessageContext context) {
		CoapServerChannel channel = (CoapServerChannel) context.getInCoapRequest().getChannel();
		channel.sendMessage(context.getOutCoapResponse());
		channel.close(); //TODO: implement strategy when to close a channel
    }

    @Override
    public CoapServer onAccept(CoapRequest request) {
        logger.info("new incomming CoAP connection");
        /* accept every incoming connection */
        return this;
    }

    @Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
    	/* draft-08:
    	 *  CoAP distinguishes between requests to an origin server and a request
   			made through a proxy.  A proxy is a CoAP end-point that can be tasked
   			by CoAP clients to perform requests on their behalf.  This may be
   			useful, for example, when the request could otherwise not be made, or
   			to service the response from a cache in order to reduce response time
   			and network bandwidth or energy consumption.
   			
   			CoAP requests to a proxy are made as normal confirmable or non-
			confirmable requests to the proxy end-point, but specify the request
   			URI in a different way: The request URI in a proxy request is
   			specified as a string in the Proxy-Uri Option (see Section 5.10.3),
   			while the request URI in a request to an origin server is split into
   			the Uri-Host, Uri-Port, Uri-Path and Uri-Query Options (see
   			Section 5.10.2).
    	*/
    	URI proxyUri = null;
    	
    	
		/* we need to cast to allow an efficient header copy */
    	//create a prototype response, will be changed during the translation process
		try {
			BasicCoapResponse response = (BasicCoapResponse) channel.createResponse(request, CoapResponseCode.Internal_Server_Error_500);
			try {
				proxyUri = new URI(request.getProxyUri());
			} catch (Exception e) {
				proxyUri = null;
			}

			if (proxyUri == null) {
				/* PROXY URI MUST BE AVAILABLE */
				logger.warn("received CoAP request without Proxy-Uri option");
				channel.sendMessage(channel.createResponse(request, CoapResponseCode.Bad_Request_400));
				channel.close();
				return;
			}

			/* check scheme if we should translate */
			boolean translate;
			if (proxyUri.getScheme().compareToIgnoreCase("http") == 0) {
				translate = true;
			} else if (proxyUri.getScheme().compareToIgnoreCase("coap") == 0) {
				translate = false;
			} else {
				/* unknown scheme */
				logger.warn("invalid proxy uri scheme");
				channel.sendMessage(channel.createResponse(request, CoapResponseCode.Bad_Request_400));
				channel.close();
				return;
			}

			/* parse URL */
			InetAddress serverAddress = InetAddress.getByName(proxyUri.getHost());
			int serverPort = proxyUri.getPort();
			if (serverPort == -1) {
				if (translate) {
					/* HTTP Server */
					serverPort = 80; // FIXME: use constant for HTTP well known
										// port
				} else {
					/* CoAP Server */
					serverPort = org.ws4d.coap.Constants.COAP_DEFAULT_PORT;
				}
			}
			/* generate context and forward message */
			ProxyMessageContext context = new ProxyMessageContext(request, translate, proxyUri);
			context.setServerAddress(serverAddress, serverPort);
			context.setOutCoapResponse(response);
			mapper.handleCoapServerRequest(context);
		} catch (Exception e) {
			logger.warn("invalid message");
			channel.sendMessage(channel.createResponse(request, CoapResponseCode.Bad_Request_400));
			channel.close();
		}

	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		// TODO Auto-generated method stub
	}
   
}

