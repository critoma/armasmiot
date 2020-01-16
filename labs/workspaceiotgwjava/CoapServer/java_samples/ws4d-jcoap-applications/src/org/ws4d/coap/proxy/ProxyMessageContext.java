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

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class ProxyMessageContext {
	/*unique for reqMessageID, remoteHost, remotePort*/

	/*
	*                  Server                 Client
	*     inRequest  +--------+  TRANSFORM  +--------+ outRequest
	*  ------------->|        |-----|||---->|        |------------->
	*                |        |             |        |
	*    outResponse |        |  TRANSFORM  |        | inResponse
	*  <-------------|        |<----|||-----|        |<-------------
	*                +--------+             +--------+
	*
	*/	

	
	/* incomming messages */
	private CoapRequest inCoapRequest;  //the coapRequest of the origin client (maybe translated)
	private HttpRequest inHttpRequest;	//the httpRequest of the origin client (maybe translated)
	private CoapResponse inCoapResponse; //the coap response of the final server
	private HttpResponse inHttpResponse; //the http response of the final server

	/* generated outgoing messages */
	private CoapResponse outCoapResponse; //the coap response send to the client
	private CoapRequest outCoapRequest; 
	private HttpResponse outHttpResponse; 
	private HttpUriRequest outHttpRequest; 

	/* trigger and channels*/
	private CoapClientChannel outCoapClientChannel; 
	NHttpResponseTrigger trigger; //needed by http

	/* corresponding cached resource*/
	private ProxyResource resource; 

	private URI uri;
	private InetAddress clientAddress;
	private int clientPort;
	private InetAddress serverAddress;
	private int serverPort;

	/* is true if a translation was done (always true for incoming http requests)*/
	private boolean translate;  //translate from coap to http

	/* indicates that the response comes from the cache*/
	private boolean cached = false;
	/* in case of a HTTP Head this is true, GET and HEAD are both mapped to CoAP GET */
	private boolean httpHeadMethod = false;
	
	/* times */
	long requestTime;
	long responseTime;
	
	
	public ProxyMessageContext(CoapRequest request, boolean translate, URI uri) {
		this.inCoapRequest = request;
		this.translate = translate;
		this.uri = uri;
	}
	
	public ProxyMessageContext(HttpRequest request, boolean translate, URI uri, NHttpResponseTrigger trigger) {
		this.inHttpRequest = request;
		this.translate = translate;
		this.uri = uri;
		this.trigger = trigger;
	}
	
	public boolean isCoapRequest(){
		return inCoapRequest != null;
	}

	public boolean isHttpRequest(){
		return inHttpRequest != null;
	}

	
	
	public CoapRequest getInCoapRequest() {
		return inCoapRequest;
	}

	public void setInCoapRequest(CoapRequest inCoapRequest) {
		this.inCoapRequest = inCoapRequest;
	}

	public HttpRequest getInHttpRequest() {
		return inHttpRequest;
	}

	public void setInHttpRequest(HttpRequest inHttpRequest) {
		this.inHttpRequest = inHttpRequest;
	}

	public CoapResponse getInCoapResponse() {
		return inCoapResponse;
	}

	public void setInCoapResponse(CoapResponse inCoapResponse) {
		this.inCoapResponse = inCoapResponse;
	}

	public HttpResponse getInHttpResponse() {
		return inHttpResponse;
	}

	public void setInHttpResponse(HttpResponse inHttpResponse) {
		this.inHttpResponse = inHttpResponse;
	}

	public CoapResponse getOutCoapResponse() {
		return outCoapResponse;
	}

	public void setOutCoapResponse(CoapResponse outCoapResponse) {
		this.outCoapResponse = outCoapResponse;
	}

	public CoapRequest getOutCoapRequest() {
		return outCoapRequest;
	}

	public void setOutCoapRequest(CoapRequest outCoapRequest) {
		this.outCoapRequest = outCoapRequest;
	}

	public HttpResponse getOutHttpResponse() {
		return outHttpResponse;
	}

	public void setOutHttpResponse(HttpResponse outHttpResponse) {
		this.outHttpResponse = outHttpResponse;
	}

	public HttpUriRequest getOutHttpRequest() {
		return outHttpRequest;
	}

	public void setOutHttpRequest(HttpUriRequest outHttpRequest) {
		this.outHttpRequest = outHttpRequest;
	}
	
	public CoapClientChannel getOutCoapClientChannel() {
		return outCoapClientChannel;
	}

	public void setOutCoapClientChannel(CoapClientChannel outClientChannel) {
		this.outCoapClientChannel = outClientChannel;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetAddress clientAddress, int clientPort) {
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
	}

	public InetAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(InetAddress serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

	public int getClientPort() {
		return clientPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public boolean isTranslate() {
		return translate;
	}

	public void setTranslatedCoapRequest(CoapRequest request) {
		this.inCoapRequest = request;
	}

	public void setTranslatedHttpRequest(HttpRequest request) {
		this.inHttpRequest = request;
	}

	public URI getUri() {
		return uri;
	}

	public NHttpResponseTrigger getTrigger() {
		return trigger;
	}

	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}

	public ProxyResource getResource() {
		return resource;
	}

	public void setResource(ProxyResource resource) {
		this.resource = resource;
	}

	public void setHttpHeadMethod(boolean httpHeadMethod) {
		this.httpHeadMethod = httpHeadMethod;
	}

	public boolean isHttpHeadMethod() {
		return httpHeadMethod;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

}
