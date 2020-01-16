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
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.ehcache.Element;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntityTemplate;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.AbstractCoapMessage.CoapHeaderOptionType;
import org.ws4d.coap.messages.BasicCoapRequest;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class ProxyMapper {
	static Logger logger = Logger.getLogger(Proxy.class);
	static final int DEFAULT_MAX_AGE_MS = 60000; //Max Age Default in ms 

	//introduce other needed classes for communication
	private CoapClientProxy coapClient;
	private CoapServerProxy coapServer;
	private HttpServerNIO httpServer;
	private HttpClientNIO httpClient;
	private static ProxyCache cache;	
	
	private static ProxyMapper instance;
	
	/*for statistics*/
	private int httpRequestCount = 0;
	private int coapRequestCount = 0;
	private int servedFromCacheCount = 0;
	

    public synchronized static ProxyMapper getInstance() {
        if (instance == null) {
            instance = new ProxyMapper();
        }
        return instance;
    }

    private ProxyMapper() {
		cache = new ProxyCache();
    }
    
    /*
    *               Server                              Client
    *              +------+                            +------+ RequestTime
    *   InRequ --->|      |----+-->Requ.Trans.-------->|      |--->OutReq
    *              |      |    |                       |      |
    *              |      |    |                       |      |
    *              |      |  ERROR                     |      |
    *              |      |    |                       |      |
    *              |      |    +<---ERROR -----+       |      |
    *              |      |    |               |       |      | ResponseTime
    *   OutResp<---|      +<---+<--Resp.Trans.-+-------+      |<---InResp
    *              +------+                            +------+
    *
    *                HTTP -----------------------------> CoAP
    *
    *                CoAP -----------------------------> HTTP
    *
    *                CoAP -----------------------------> CoAP
    */

    
    
    
	public void handleHttpServerRequest(ProxyMessageContext context) {
		httpRequestCount++;
		// do not translate methods: OPTIONS,TRACE,CONNECT -> error
		// "Not Implemented"
		if (isHttpRequestMethodSupported(context.getInHttpRequest())) {
			// perform request-transformation
			
			/* try to get from cache */
			ProxyResource resource = null;
			if (context.getInHttpRequest().getRequestLine().getMethod().toLowerCase().equals("get")){
				resource = cache.get(context);
			}
			
			if (resource != null) {
				/* answer from cache */
				resourceToHttp(context, resource);
				context.setCached(true); // avoid "recaching"
				httpServer.sendResponse(context);
				logger.info("served HTTP request from cache");
				servedFromCacheCount++;
			} else {
				/* not cached -> forward request */
				try {
					coapClient.createChannel(context); //channel must be created first 
					transRequestHttpToCoap(context);
					context.setRequestTime(System.currentTimeMillis());
					coapClient.sendRequest(context);
				} catch (Exception e) {
					logger.warn("HTTP to CoAP Request failed: " + e.getMessage());
					/* close if a channel was connected */
					if (context.getOutCoapClientChannel() != null){
						context.getOutCoapClientChannel().close();
					}
					sendDirectHttpError(context, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
				}
			}
		} else {
			/* method not supported */
			sendDirectHttpError(context, HttpStatus.SC_NOT_IMPLEMENTED, "Not Implemented");
		}
	}

	public void handleCoapServerRequest(ProxyMessageContext context) {
		coapRequestCount++;
		ProxyResource resource = null;
		if (context.getInCoapRequest().getRequestCode() == CoapRequestCode.GET){
			resource = cache.get(context);
		}
		if (context.isTranslate()) {
			/* coap to http */
			if (resource != null) {
				/* answer from cache */
				resourceToHttp(context, resource);
				context.setCached(true); // avoid "recaching"
				httpServer.sendResponse(context);
				logger.info("served CoAP request from cache");
				servedFromCacheCount++;
			} else {
				/* translate CoAP Request -> HTTP Request */
				try {
					transRequestCoapToHttp(context);
					context.setRequestTime(System.currentTimeMillis());
					httpClient.sendRequest(context); 
				} catch (Exception e) {
					logger.warn("CoAP to HTTP Request translation failed: " + e.getMessage());
					sendDirectCoapError(context, CoapResponseCode.Not_Found_404);
				}
			}
		} else {
			/* coap to coap */
			if (resource != null) {
				/* answer from cache */
				resourceToCoap(context, resource);
				context.setCached(true); // avoid "recaching"
				coapServer.sendResponse(context);
				logger.info("served from cache");
				servedFromCacheCount++;
			} else {
				/* translate CoAP Request -> CoAP Request */
				try {
					coapClient.createChannel(context); //channel must be created first 
					transRequestCoapToCoap(context);
					context.setRequestTime(System.currentTimeMillis());
					coapClient.sendRequest(context);
				} catch (Exception e) {
					logger.warn("CoAP to CoAP Request forwarding failed: " + e.getMessage());
					sendDirectCoapError(context, CoapResponseCode.Not_Found_404);
				}
			}
		}
	}

	public void handleCoapClientResponse(ProxyMessageContext context) {
		context.setResponseTime(System.currentTimeMillis());
		
		if (!context.isCached() && context.getInCoapResponse() !=null ) { // avoid recaching
			cache.cacheCoapResponse(context);
		}

		if (context.isTranslate()) {
			/* coap to HTTP */
			try {
				transResponseCoapToHttp(context);
			} catch (Exception e) {
				logger.warn("CoAP to HTTP Response translation failed: " + e.getMessage());
				context.setOutHttpResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"));
			}
			httpServer.sendResponse(context);
		} else {
			/* coap to coap */
			try {
				transResponseCoapToCoap(context);
			} catch (Exception e) {
				logger.warn("CoAP to CoAP Response forwarding failed: " + e.getMessage());
				context.getOutCoapResponse().setResponseCode(CoapResponseCode.Internal_Server_Error_500);
			}
			coapServer.sendResponse(context);
		}
	}

        
	public void handleHttpClientResponse(ProxyMessageContext context) {
		context.setResponseTime(System.currentTimeMillis());
		
		if (!context.isCached()) {
			cache.cacheHttpResponse(context);
		}
		try {
			transResponseHttpToCoap(context);
		} catch (Exception e) {
			logger.warn("HTTP to CoAP Response translation failed: " + e.getMessage());
			context.getOutCoapResponse().setResponseCode(CoapResponseCode.Internal_Server_Error_500);
		}
		coapServer.sendResponse(context);
	}
	

	/* ------------------------------------ Translate Functions -----------------------------------*/
	
	public static void transRequestCoapToCoap(ProxyMessageContext context){
		CoapRequest in = context.getInCoapRequest();
		CoapClientChannel channel = context.getOutCoapClientChannel();
		context.setOutCoapRequest(channel.createRequest(CoapClientProxy.RELIABLE, in.getRequestCode()));
		CoapRequest out = context.getOutCoapRequest();
		
		/*TODO: translate not using copy header options */
		((BasicCoapRequest) out).copyHeaderOptions((BasicCoapRequest)in);
		
		/* TODO: check if the next hop is a proxy or the final serer 
		 * implement coapUseProxy option for proxy */
		out.removeOption(CoapHeaderOptionType.Proxy_Uri);
		
		out.setUriPath(context.getUri().getPath());
		if (context.getUri().getQuery() != null){
			out.setUriQuery(context.getUri().getQuery());
		}

		out.removeOption(CoapHeaderOptionType.Token);
		out.setPayload(in.getPayload());
	}
	
	public static void transRequestHttpToCoap(ProxyMessageContext context) throws IOException {
		HttpRequest httpRequest = context.getInHttpRequest();
		boolean hasContent = false;
		CoapRequestCode requestCode;
		String method;
		method = httpRequest.getRequestLine().getMethod().toLowerCase();

		if (method.contentEquals("get")) {
			requestCode = CoapRequestCode.GET;
		} else if (method.contentEquals("put")) {
			requestCode = CoapRequestCode.PUT;
			hasContent = true;
		} else if (method.contentEquals("post")) {
			requestCode = CoapRequestCode.POST;
			hasContent = true;
		} else if (method.contentEquals("delete")) {
			requestCode = CoapRequestCode.DELETE;
		} else if (method.contentEquals("head")) {
			// if we have a head request, coap should handle it as a get,
			// but without any message-body
			requestCode = CoapRequestCode.GET;
			context.setHttpHeadMethod(true);
		} else {
			throw new IllegalStateException("unknown message code");
		}

		CoapClientChannel channel = context.getOutCoapClientChannel();
		context.setOutCoapRequest(channel.createRequest(CoapClientProxy.RELIABLE, requestCode));

		// Translate Headers
		CoapRequest coapRequest = context.getOutCoapRequest();

		URI uri = null;

		// construct uri for later use
		uri = resolveHttpRequestUri(httpRequest);

		// Content-Type is in response only

		// Max-Age is in response only

		// Proxy-Uri doesn't matter for this purpose

		// ETag:
		if (httpRequest.containsHeader("Etag")) {
			Header[] headers = httpRequest.getHeaders("Etag");
			if (headers.length > 0) {
				for (int i = 0; i < headers.length; i++) {
					String etag = headers[i].getValue();
					coapRequest.addETag(etag.getBytes());
				}
			}
		}

		// Uri-Host:
		// don't needs to be there

		// Location-Path is in response-only
		// Location-Query is in response-only

		// Uri-Path:
		// this is the implementation according to coap-rfc section 6.4
		// first check if uri is absolute and that it has no fragment
		if (uri.isAbsolute() && uri.getFragment() == null) {
			coapRequest.setUriPath(uri.getPath());
		} else {
			throw new IllegalStateException("uri has wrong format");
		}

		// Token is the same number as msgID, not needed now
		// in future development it should be generated here

		// Accept: possible values are numeric media-types
		if (httpRequest.containsHeader("Accept")) {
			Header[] headers = httpRequest.getHeaders("Accept");
			if (headers.length > 0) {
				for (int i = 0; i < headers.length; i++) {
					httpMediaType2coapMediaType(headers[i].getValue(), coapRequest);
				}
			}
		}

		// TODO: if-match:
		// if (request.containsHeader("If-Match")) {
		// Header[] headers = request.getHeaders("If-Match");
		// if (headers.length > 0) {
		// for (int i=0; i < headers.length; i++) {
		// String header_value = headers[i].getValue();
		// CoapHeaderOption option_ifmatch = new
		// CoapHeaderOption(CoapHeaderOptionType.If_Match,
		// header_value.getBytes());
		// header.addOption(option_ifmatch );
		// }
		// }
		// }

		// Uri-Query:
		// this is the implementation according to coap-rfc section 6.4
		// first check if uri is absolute and that it has no fragment
		if (uri.isAbsolute() && uri.getFragment() == null) {
			if (uri.getQuery() != null) { // only add options if there are
				// some
				coapRequest.setUriQuery(uri.getQuery());
			}
		} else {
			throw new IllegalStateException("uri has wrong format");
		}

		// TODO: If-None-Match:
		// if (request.containsHeader("If-None-Match")) {
		// Header[] headers = request.getHeaders("If-None-Match");
		// if (headers.length > 0) {
		// if (headers.length > 1) {
		// System.out.println("multiple headers in request, ignoring all except the first");
		// }
		// String header_value = headers[0].getValue();
		// CoapHeaderOption option_ifnonematch = new
		// CoapHeaderOption(CoapHeaderOptionType.If_None_Match,
		// header_value.getBytes());
		// header.addOption(option_ifnonematch);
		// }
		// }

		// pass-through the payload
		if (hasContent){
			BasicHttpEntityEnclosingRequest entirequest = (BasicHttpEntityEnclosingRequest) httpRequest;
			ConsumingNHttpEntityTemplate entity = (ConsumingNHttpEntityTemplate) entirequest.getEntity();
			ByteContentListener listener = (ByteContentListener) entity.getContentListener();
			byte[] data = listener.getContent();
			context.getOutCoapRequest().setPayload(data);
		}
	}

	public static void transRequestCoapToHttp(ProxyMessageContext context) throws UnsupportedEncodingException{
		HttpUriRequest httpRequest;
		CoapRequest request = context.getInCoapRequest();
		CoapRequestCode code = request.getRequestCode();
		//TODO:translate header options from coap-request to http-request
		NStringEntity entity;
		entity = new NStringEntity(new String(request.getPayload()));
		switch (code) {
			case GET:
				httpRequest = new HttpGet(context.getUri().toString());
				break;
			case PUT:
				httpRequest = new HttpPut(context.getUri().toString());	
				((HttpPut)httpRequest).setEntity(entity);
				break;
			case POST:
				httpRequest = new HttpPost(context.getUri().toString());
				((HttpPost)httpRequest).setEntity(entity);
				break;
			case DELETE:
				httpRequest = new HttpDelete(context.getUri().toString());		
			default:
				throw new IllegalStateException("unknown request code");
		}
		
		context.setOutHttpRequest(httpRequest);
	}

	public static void transResponseCoapToCoap(ProxyMessageContext context){
		CoapResponse in = context.getInCoapResponse();
		CoapResponse out = context.getOutCoapResponse();
		
		/*TODO: translate not using copy header options */
		((BasicCoapResponse) out).copyHeaderOptions((BasicCoapResponse)in);
		
		out.setResponseCode(in.getResponseCode());
		out.removeOption(CoapHeaderOptionType.Token);
		out.setPayload(in.getPayload());
	}

	public static void transResponseCoapToHttp(ProxyMessageContext context) throws UnsupportedEncodingException{
		CoapResponse coapResponse = context.getInCoapResponse();
		//create a response-object, set http version and assume a default state of ok
		HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		
		// differ between methods is necessary to set the right status-code
		String requestMethod = context.getInHttpRequest().getRequestLine().getMethod();

		if (requestMethod.toLowerCase().contains("get")){

			// set status code and reason phrase
			setHttpMsgCode(coapResponse, "get", httpResponse);

			// pass-through the payload, if we do not answer a head-request
			if (!context.isHttpHeadMethod()) {
				NStringEntity entity;
				entity = new NStringEntity(new String(coapResponse.getPayload()), "UTF-8");
				entity.setContentType("text/plain");
				httpResponse.setEntity(entity);
			}
		} else if (requestMethod.toLowerCase().contains("put")){
			setHttpMsgCode(coapResponse, "put", httpResponse);
		} else if (requestMethod.toLowerCase().contains("post")){
			setHttpMsgCode(coapResponse, "post", httpResponse);
		} else if (requestMethod.toLowerCase().contains("delete")){
			setHttpMsgCode(coapResponse, "delete", httpResponse);
		} else {
			throw new IllegalStateException("unknown request method");
		}

		// set Headers
		headerTranslateCoapToHttp(coapResponse, httpResponse);
		context.setOutHttpResponse(httpResponse);
	}
	
	public static void transResponseHttpToCoap(ProxyMessageContext context) throws ParseException, IOException{
		//set the response-code according to response-code-mapping-table
		CoapResponse coapResponse = context.getOutCoapResponse();
		coapResponse.setResponseCode(getCoapResponseCode(context)); //throws an exception if mapping failed
		//TODO: translate header-options
		
		//assume in this case a string-entity
		//TODO: add more entity-types
		coapResponse.setContentType(CoapMediaType.text_plain);
		
		String entity = "";
		entity = EntityUtils.toString(context.getInHttpResponse().getEntity());
		coapResponse.setPayload(entity);
	}

	/* these functions are called if the request translation fails and no message was forwarded */
	public void sendDirectHttpError(ProxyMessageContext context,int code, String reason){
		HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, code, reason);
		context.setOutHttpResponse(httpResponse);
		httpServer.sendResponse(context);
	}

	/* these functions are called if the request translation fails and no message was forwarded */
	public void sendDirectCoapError(ProxyMessageContext context, CoapResponseCode code){
		CoapServerChannel channel = (CoapServerChannel) context.getInCoapRequest().getChannel();
		CoapResponse response = channel.createResponse(context.getInCoapRequest(), code); 
		context.setInCoapResponse(response);
		coapServer.sendResponse(context);
	}
	
	public static void resourceToHttp(ProxyMessageContext context, ProxyResource resource){
		/* TODO: very rudimentary implementation */
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"); 
		try {
			NStringEntity entity;
			entity = new NStringEntity(new String(resource.getValue()), "UTF-8");
			entity.setContentType("text/plain");
			response.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			logger.error("HTTP entity creation failed");
		}
		context.setOutHttpResponse(response);
	}
	
	public static void resourceToCoap(ProxyMessageContext context, ProxyResource resource){
		CoapResponse response = context.getOutCoapResponse(); //already generated
		/* response code */
		response.setResponseCode(CoapResponseCode.Content_205);
		/* payload */
		response.setPayload(resource.getValue());
		/* mediatype */
		if (resource.getCoapMediaType() != null) {
			response.setContentType(resource.getCoapMediaType());
		}
		/* Max-Age */
		int maxAge = (int)(resource.expires() -  System.currentTimeMillis()) / 1000;
		if (maxAge < 0){
			/* should never happen because the function is only called if the resource is valid. 
			 * However, processing time can be an issue */
			logger.warn("return expired resource (Max-Age = 0)");
			maxAge = 0;
		}
		response.setMaxAge(maxAge);
	}
	
	
	public static void setHttpMsgCode(CoapResponse coapResponse, String requestMethod, HttpResponse httpResponse) {
		
		CoapResponseCode responseCode = coapResponse.getResponseCode();
		
		switch(responseCode) {
//			case OK_200: { //removed from CoAP draft
//				httpResponse.setStatusCode(HttpStatus.SC_OK);
//				httpResponse.setReasonPhrase("Ok");
//				break;
//			}
			case Created_201: {
				if (requestMethod.contains("post") || requestMethod.contains("put")) {				
					httpResponse.setStatusCode(HttpStatus.SC_CREATED);
					httpResponse.setReasonPhrase("Created");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpResponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Deleted_202: {
				if (requestMethod.contains("delete")) {				
					httpResponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
					httpResponse.setReasonPhrase("No Content");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpResponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Valid_203: {
				httpResponse.setStatusCode(HttpStatus.SC_NOT_MODIFIED);
				httpResponse.setReasonPhrase("Not Modified");
				break;
			}
			case Changed_204: {
				if (requestMethod.contains("post") || requestMethod.contains("put")) {				
					httpResponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
					httpResponse.setReasonPhrase("No Content");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpResponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Content_205: {
				if (requestMethod.contains("get")) {
					httpResponse.setStatusCode(HttpStatus.SC_OK);
					httpResponse.setReasonPhrase("OK");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpResponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Bad_Request_400: {
				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpResponse.setReasonPhrase("Bad Request");
				break;
			}
			case Unauthorized_401: {
				httpResponse.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
				httpResponse.setReasonPhrase("Unauthorized");
				break;
			}
			case Bad_Option_402: {
				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpResponse.setReasonPhrase("Bad Option");
				break;
			}
			case Forbidden_403: {
				httpResponse.setStatusCode(HttpStatus.SC_FORBIDDEN);
				httpResponse.setReasonPhrase("Forbidden");
				break;
			}
			case Not_Found_404: {
				httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
				httpResponse.setReasonPhrase("Not Found");
				break;
			}
			case Method_Not_Allowed_405: {
				httpResponse.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				httpResponse.setReasonPhrase("Method Not Allowed");
				break;
			}
			case Precondition_Failed_412: {
				httpResponse.setStatusCode(HttpStatus.SC_PRECONDITION_FAILED);
				httpResponse.setReasonPhrase("Precondition Failed");
				break;
			}
			case Request_Entity_To_Large_413: {
				httpResponse.setStatusCode(HttpStatus.SC_REQUEST_TOO_LONG);
				httpResponse.setReasonPhrase("Request Too Long : Request entity too large");
				break;
			}
			case Unsupported_Media_Type_415: {
				httpResponse.setStatusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
				httpResponse.setReasonPhrase("Unsupported Media Type");
				break;
			}
			case Internal_Server_Error_500: {
				httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				httpResponse.setReasonPhrase("Internal Server Error");
				break;
			}
			case Not_Implemented_501: {
				httpResponse.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
				httpResponse.setReasonPhrase("Not Implemented");
				break;
			}
			case Bad_Gateway_502: {
				httpResponse.setStatusCode(HttpStatus.SC_BAD_GATEWAY);
				httpResponse.setReasonPhrase("Bad Gateway");
				break;
			}
			case Service_Unavailable_503: {
				httpResponse.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
				httpResponse.setReasonPhrase("Service Unavailable");
				break;
			}
			case Gateway_Timeout_504: {
				httpResponse.setStatusCode(HttpStatus.SC_GATEWAY_TIMEOUT);
				httpResponse.setReasonPhrase("Gateway Timeout");
				break;
			}
			case Proxying_Not_Supported_505: {
				httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				httpResponse.setReasonPhrase("Internal Server Error : Proxying not supported");
				break;
			}
			case UNKNOWN: {
				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpResponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
				break;
			}
			default: {
				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpResponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
				break;
			}
		}		
	}

	//sets the coap-response code under use of http-status code; used in case coap-http
	public static CoapResponseCode getCoapResponseCode(ProxyMessageContext context) {
		HttpResponse httpResponse = context.getInHttpResponse();
		//TODO: add cases in which http-code is the same, but coap-code is different, look at response-code-mapping-table
		switch(httpResponse.getStatusLine().getStatusCode()) {
			case HttpStatus.SC_CREATED:	return CoapResponseCode.Created_201;
			case HttpStatus.SC_NO_CONTENT:				
				if (context.getInCoapRequest().getRequestCode() == CoapRequestCode.DELETE) {
					return CoapResponseCode.Deleted_202;
				} else {
					return CoapResponseCode.Changed_204;
				}				
			case HttpStatus.SC_NOT_MODIFIED: return CoapResponseCode.Valid_203;
			case HttpStatus.SC_OK: return CoapResponseCode.Content_205;
			case HttpStatus.SC_UNAUTHORIZED: return CoapResponseCode.Unauthorized_401;
			case HttpStatus.SC_FORBIDDEN:return CoapResponseCode.Forbidden_403;
			case HttpStatus.SC_NOT_FOUND:return CoapResponseCode.Not_Found_404;
			case HttpStatus.SC_METHOD_NOT_ALLOWED:return CoapResponseCode.Method_Not_Allowed_405;
			case HttpStatus.SC_PRECONDITION_FAILED: return CoapResponseCode.Precondition_Failed_412;
			case HttpStatus.SC_REQUEST_TOO_LONG: return CoapResponseCode.Request_Entity_To_Large_413;
			case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:return CoapResponseCode.Unsupported_Media_Type_415;
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:return CoapResponseCode.Internal_Server_Error_500;
			case HttpStatus.SC_NOT_IMPLEMENTED:return CoapResponseCode.Not_Implemented_501;
			case HttpStatus.SC_BAD_GATEWAY:return CoapResponseCode.Bad_Gateway_502;
			case HttpStatus.SC_SERVICE_UNAVAILABLE:return CoapResponseCode.Service_Unavailable_503;
			case HttpStatus.SC_GATEWAY_TIMEOUT:return CoapResponseCode.Gateway_Timeout_504;
			default:
				throw new IllegalStateException("unknown HTTP response code");
		}
	}
		
	//mediatype-mapping:
	public static void httpMediaType2coapMediaType(String mediatype, CoapRequest request) {
		
		String[] type_subtype = mediatype.split(",");
		for (String value : type_subtype) {
			if (value.toLowerCase().contains("text")
					&& value.toLowerCase().contains("plain")) {
				request.addAccept(CoapMediaType.text_plain);
			} else if (value.toLowerCase().contains("application")) { // value is for example "application/xml;q=0.9"
				String[] subtypes = value.toLowerCase().split("/");
				String subtype = "";

				if (subtypes.length == 2) {
					subtype = subtypes[1]; // subtype is for example now "xml;q=0.9"
				} else {
					System.out.println("Error in reading Mediatypes!");
				}

				// extract the subtype-name and remove the quality identifiers:
				String[] subname = subtype.split(";");
				String name = "";

				if (subname.length > 0) {
					name = subname[0]; // name is for example "xml"
				} else {
					System.out.println("Error in reading Mediatypes!");
				}

				if (name.contentEquals("link-format")) {
					request.addAccept(CoapMediaType.link_format);
				}
				if (name.contentEquals("xml")) {
					request.addAccept(CoapMediaType.xml);
				}
				if (name.contentEquals("octet-stream")) {
					request.addAccept(CoapMediaType.octet_stream);
				}
				if (name.contentEquals("exi")) {
					request.addAccept(CoapMediaType.exi);
				}
				if (name.contentEquals("json")) {
					request.addAccept(CoapMediaType.json);
				}
			}
		}
	}
		

	// translate response-header-options in case of http-coap
	public static void headerTranslateCoapToHttp(CoapResponse coapResponse, HttpResponse httpResponse) {

		// investigate all coap-headers and set corresponding http-headers
		CoapMediaType contentType = coapResponse.getContentType();
		if (contentType != null) {
			switch (contentType) {
			case text_plain:
				httpResponse.addHeader("Content-Type", "text/plain");
				break;
			case link_format:
				httpResponse.addHeader("Content-Type",
						"application/link-format");
				break;
			case json:
				httpResponse.addHeader("Content-Type", "application/json");
				break;
			case exi:
				httpResponse.addHeader("Content-Type", "application/exi");
				break;
			case octet_stream:
				httpResponse.addHeader("Content-Type",
						"application/octet-stream");
				break;
			case xml:
				httpResponse.addHeader("Content-Type", "application/xml");
				break;
			default:
				httpResponse.addHeader("Content-Type", "text/plain");
				break;
			}
		} else {
			httpResponse.addHeader("Content-Type", "text/plain");
		}
		
		long maxAge = coapResponse.getMaxAge();
		if (maxAge < 0){
			maxAge = DEFAULT_MAX_AGE_MS;
		}
		long maxAgeMs = maxAge * 1000; 
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE){
			httpResponse.addHeader("Retry-After", String.valueOf(maxAge));
		}
		
		byte[] etag = coapResponse.getETag();
		if (etag != null){
			httpResponse.addHeader("Etag", new String(etag));
		}
				
		//generate content-length-header
		if (httpResponse.getEntity() != null)
			httpResponse.addHeader("Content-length", "" + httpResponse.getEntity().getContentLength());
		
		//set creation-date for Caching:
		httpResponse.addHeader("Date", "" + formatDate(new GregorianCalendar().getTime()));
		
		//expires-option is option-value (default is 60 secs) + current_date
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(calendar.getTimeInMillis() + maxAgeMs);
		String date = formatDate(calendar.getTime());
		httpResponse.addHeader("Expires", date);
	}
	

	public static HttpResponse handleCoapDELETEresponse(CoapResponse response, HttpResponse httpResponse) {		
		//set status code and reason phrase

		headerTranslateCoapToHttp(response, httpResponse);
		return httpResponse;		
	}
	
//	//the mode is used to indicate for which case the proxy is listening to
//	//mode is unneccessary when proxy is listening to all cases, then there are more threads neccessary
//	public void setMode(Integer modenumber) {
//		mode = modenumber;
//	}
	
	//setter-functions to introduce other threads
	public void setHttpServer(HttpServerNIO server) {
		httpServer = server;
	}
	public void setHttpClient(HttpClientNIO client) {
		httpClient = client;
	}
	public void setCoapServer(CoapServerProxy server) {
		coapServer = server;
	}
	public void setCoapClient(CoapClientProxy client) {
		coapClient = client;
	}
	

	//exclude methods from processing:OPTIONS/TRACE/CONNECT
	public static boolean isHttpRequestMethodSupported(HttpRequest request) {
		
		String method = request.getRequestLine().getMethod().toLowerCase();
		
		if (method.contentEquals("options") || method.contentEquals("trace") || method.contentEquals("connect"))		
			return false;

		return true;
	}
		
	//makes a date to a string; http-header-values (expires, date...) must be a string in most cases
    public static String formatDate(Date date) {
    	
    	final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    	
    	if (date == null) {
            throw new IllegalArgumentException("date is null");
        }

        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());				//CEST
        String ret = formatter.format(date);
        return ret;
        
    }
    
    public static URI resolveHttpRequestUri(HttpRequest request){
			URI uri = null;
			
			String uriString = request.getRequestLine().getUri();
			/* make sure to have the scheme */
			if (uriString.startsWith("coap://")){
				/* do nothing */
			} else if (uriString.startsWith("http://")){
				uriString = "coap://" + uriString.substring(7);
			} else {
				/* not an absolute uri */
				Header[] host = request.getHeaders("Host");
				/* only one is accepted */
				if (host.length <= 0){
					/* error, unknown host*/
					return null;
				}
				uriString = "coap://"  + host[0].getValue() + uriString;
			}
			
			try {
				uri = new URI(uriString);
			} catch (URISyntaxException e) {
				return null; //indicates that resolve failed
			}				
				
			return uri;
	}
    
	public static boolean isIPv4Address(InetAddress addr) {
		try {
			@SuppressWarnings("unused") //just to check if casting fails
			Inet4Address addr4 = (Inet4Address) addr;
			return true;
		} catch (ClassCastException ex) {
			return false;
		}
	}

	public static boolean isIPv6Address(InetAddress addr) {
		try {
			@SuppressWarnings("unused") //just to check if casting fails
			Inet6Address addr6 = (Inet6Address) addr;
			return true;
		} catch (ClassCastException ex) {
			return false;
		}
	}

	public int getHttpRequestCount() {
		return httpRequestCount;
	}

	public int getCoapRequestCount() {
		return coapRequestCount;
	}

	public int getServedFromCacheCount() {
		return servedFromCacheCount;
	}
	
	public void resetCounter(){
		httpRequestCount = 0;
		coapRequestCount = 0;
		servedFromCacheCount = 0;
	}

	public void setCacheEnabled(boolean enabled) {
			cache.setEnabled(enabled);
	}

	public CoapClientProxy getCoapClient() {
		return coapClient;
	}

	public CoapServerProxy getCoapServer() {
		return coapServer;
	}

	public HttpServerNIO getHttpServer() {
		return httpServer;
	}

	public HttpClientNIO getHttpClient() {
		return httpClient;
	}
	
}
