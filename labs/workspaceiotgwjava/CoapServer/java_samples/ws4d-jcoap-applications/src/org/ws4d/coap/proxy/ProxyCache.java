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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

/*
 * TODO's:
 * - implement Date option as described in "Connecting the Web with the Web of Things: Lessons Learned From Implementing a CoAP-HTTP Proxy"
 * - caching of HTTP resources not supported as HTTP servers may have enough resources (in terms of RAM/ROM/batery/computation)
 * 
 * */

public class ProxyCache {
	static Logger logger = Logger.getLogger(Proxy.class);
	private static final int MAX_LIFETIME = Integer.MAX_VALUE;
	private static Cache cache;				
	private static CacheManager cacheManager;			
	private boolean enabled = true;
	private static final int defaultMaxAge = org.ws4d.coap.Constants.COAP_DEFAULT_MAX_AGE_S;
	private static final ProxyCacheTimePolicy cacheTimePolicy = ProxyCacheTimePolicy.Halftime;
	
	public ProxyCache() {
		cacheManager = CacheManager.create();
		cache = new Cache(new CacheConfiguration("proxy", 100)
		.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
		.overflowToDisk(true)
		.eternal(false)
		.diskPersistent(false)
		.diskExpiryThreadIntervalSeconds(0));
		cacheManager.addCache(cache);	
	}
	
	public void removeKey(URI uri) {
		cache.remove(uri);
	}
	

//	public void put(ProxyMessageContext context) {
//		if (isEnabled() || context == null){
//			return;
//		}
//		//TODO: check for overwrites
//		
//		
//		insertElement(context.getResource().getKey(), context.getResource());
////		URI uri = context.getUri();
////		if (uri.getScheme().equalsIgnoreCase("coap")){
////			putCoapRes(uri, context.getCoapResponse());
////		} else if (uri.getScheme().equalsIgnoreCase("http")){
////			putHttpRes(uri, context.getHttpResponse());
////		}
//	}
	
//	private void putHttpRes(URI uri, HttpResponse response){
//		if (response == null){
//			return;
//		}
//		logger.info( "Cache HTTP Resource (" + uri.toString() + ")");
//		//first determine what to do
//		int code = response.getStatusLine().getStatusCode();
//		
//		//make some garbage collection to avoid a cache overflow caused by many expired elements (when 80% charged as first idea)
//		if (cache.getSize() > cache.getCacheConfiguration().getMaxElementsInMemory()*0.8) {
//			cache.evictExpiredElements();
//		}
//		
//		//set the max-age of new element
//		//use the http-header-options expires and date
//		//difference is the same value as the corresponding max-age from coap-response, but at this point we only have a http-response
//		int timeToLive = 0;
//		Header[] expireHeaders = response.getHeaders("Expires");
//		if (expireHeaders.length == 1) {
//			String expire = expireHeaders[0].getValue();
//			Date expireDate = StringToDate(expire);
//		
//			Header[] dateHeaders = response.getHeaders("Date");
//			if (dateHeaders.length == 1) {
//				String dvalue = dateHeaders[0].getValue();
//				Date date = StringToDate(dvalue);
//			
//				timeToLive = (int) ((expireDate.getTime() - date.getTime()) / 1000);
//			}
//		}
//		
//		//cache-actions are dependent of response-code, as described in coap-rfc-draft-7
//		switch(code) {
//			case HttpStatus.SC_CREATED: {
//				if (cache.isKeyInCache(uri)) {
//					markExpired(uri);
//				}
//				break;
//			}
//			case HttpStatus.SC_NO_CONTENT: {
//				if (cache.isKeyInCache(uri)) {
//					markExpired(uri);
//				}
//				break;
//			}
//			case HttpStatus.SC_NOT_MODIFIED: {
//				if (cache.isKeyInCache(uri)) {
//					insertElement(uri, response, timeToLive); //should update the response if req is already in cache
//				}
//				break;
//			}
//			default: {
//				insertElement(uri, response, timeToLive);
//				break;
//			}
//		}
//	}
	
//	private void putCoapRes(ProxyResourceKey key, CoapResponse response){
//		if (response == null){
//			return;
//		}
//		logger.debug( "Cache CoAP Resource (" + uri.toString() + ")");
//		
//		long timeToLive = response.getMaxAge();
//		if (timeToLive < 0){
//			timeToLive = defaultTimeToLive;
//		}
//		insertElement(key, response);
//	}
//	
//	public HttpResponse getHttpRes(URI uri) {
//		if (defaultTimeToLive == 0) return null;
//		if (cache.getQuiet(uri) != null) {
//			Object o = cache.get(uri).getObjectValue();
//			logger.debug( "Found in cache (" + uri.toString() + ")");
//			return (HttpResponse) o;
//		} else {
//			logger.debug( "Not in cache (" + uri.toString() + ")");
//			return null;
//		}
//	}
//	
//	public CoapResponse getCoapRes(URI uri) {
//		if (defaultTimeToLive == 0) return null;
//
//		if (cache.getQuiet(uri) != null) {
//			Object o = cache.get(uri).getObjectValue();
//			logger.debug( "Found in cache (" + uri.toString() + ")");
//			return (CoapResponse) o;
//		}else{
//			logger.debug( "Not in cache (" + uri.toString() + ")");
//			return null;
//		}
//	}

	public boolean isInCache(ProxyResourceKey key) {
		if (!isEnabled()){
			return false;
		}
		if (cache.isKeyInCache(key)) {
			return true;
		} else {
			return false;
		}
	}
	
	//for some operations it is necessary to build an http-date from string
	private static Date StringToDate(String string_date) {
		
		Date date = null;
			
		//this pattern is the official http-date format
    	final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());				//CEST, default is GMT
		
		try {
			date = (Date) formatter.parse(string_date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}

//	//mark an element as expired
//	private void markExpired(ProxyResourceKey key) {
//		if (cache.getQuiet(key) != null) {
//			cache.get(key).setTimeToLive(0);
//		}
//	}
	
	private boolean insertElement(ProxyResourceKey key, ProxyResource resource) {
		Element elem = new Element(key, resource);
		if (resource.expires() == -1) {
			/* never expires */
			cache.put(elem);
		} else {
			long ttl = resource.expires() - System.currentTimeMillis();
			if (ttl > 0) {
				/* limit the maximum lifetime */
				if (ttl > MAX_LIFETIME) {
					ttl = MAX_LIFETIME;
				}
				elem.setTimeToLive((int) ttl);
				cache.put(elem);
				logger.debug("cache insert: " + resource.getPath() );

			} else {
				/* resource is already expired */
				return false;
			}
		}
		return true;
	}
	
	private void updateTtl(ProxyResourceKey key, long newExpires) {
		/*getQuiet is used to not update statistics */
		Element elem = cache.getQuiet(key);
		
		if (elem != null) {
			long ttl = newExpires - System.currentTimeMillis();
			if (ttl > 0 || newExpires == -1 ) {
				/* limit the maximum lifetime */
				if (ttl > MAX_LIFETIME) {
					ttl = MAX_LIFETIME;
				}
				elem.setTimeToLive((int) ttl);
			} 
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ProxyResource get(ProxyMessageContext context) {
		if (!isEnabled()){
			return null;
		}
		
		String path = context.getUri().getPath();
		if(path == null){
			/* no caching */
			return null;
		}
		Element elem = cache.get(new ProxyResourceKey(context.getServerAddress(), context.getServerPort(), path));
		logger.debug("cache get: " + context.getServerAddress().toString() + " " + context.getServerPort() + " " + path);
		if (elem != null) {
			/* found cached entry */
			ProxyResource res = (ProxyResource) elem.getObjectValue();
			if (!res.isExpired()) {
				return res;
			}
		}
		return null;
	}

	public void cacheHttpResponse(ProxyMessageContext context) {
		if (!isEnabled()){
			return;
		}
		/* TODO caching of HTTP responses currently not supported (not use to unload HTTP servers) */
		return;
	}

	public void cacheCoapResponse(ProxyMessageContext context) {
		if (!isEnabled()){
			return;
		}
		
		CoapResponse response = context.getInCoapResponse();
		
		String path = context.getUri().getPath();
		if(path == null){
			/* no caching */
			return;
		}
		
		ProxyResourceKey key = new ProxyResourceKey(context.getServerAddress(), context.getServerPort(), path);
		
		/* NOTE:
		 * - currently caching is only implemented for success error codes (2.xx) 
		 * - not fresh resources are removed (could be used for validation model)*/
		
		switch (context.getInCoapResponse().getResponseCode()) {
		case Created_201:
			/* A cache SHOULD mark any stored response for the
			   created resource as not fresh. This response is not cacheable.*/
			cache.remove(key); 
			break;
		case Deleted_202:
			/*    This response is not cacheable.  However, a cache SHOULD mark any
   				stored response for the deleted resource as not fresh.*/
			cache.remove(key); 
			break;
		case Valid_203:
			/* When a cache receives a 2.03 (Valid) response, it needs to update the
   				stored response with the value of the Max-Age Option included in the
   				response (see Section 5.6.2). */
			//TODO
			break;
		case Changed_204:
			/* This response is not cacheable.  However, a cache SHOULD mark any
   				stored response for the changed resource as not fresh. */
			cache.remove(key); 
			break;
		case Content_205:
			/* This response is cacheable: Caches can use the Max-Age Option to
   				determine freshness (see Section 5.6.1) and (if present) the ETag
   				Option for validation (see Section 5.6.2).*/
			/* CACHE RESOURCE */
			ProxyResource resource = new ProxyResource(path, response.getPayload(), response.getContentType());
			resource.setExpires(cacheTimePolicy.calcExpires(context.getRequestTime(), context.getResponseTime(), response.getMaxAge()));
			insertElement(key, resource);
			break;

		default:
			break;
		}
	}
	
	public enum ProxyCacheTimePolicy{
		Request(0), 
		Response(1), 
		Halftime(2);
		
		int state;
		
		private ProxyCacheTimePolicy(int state){
			this.state = state;
		}
		
		public long calcExpires(long requestTime, long responseTime, long maxAge){
			if (maxAge == -1){
				maxAge = defaultMaxAge;
			}
			switch (this) {
			case Request:
				return requestTime + (maxAge * 1000) ;
			case Response:
				return responseTime + (maxAge * 1000);
			case Halftime:
				return requestTime + ((responseTime - requestTime) / 2) + (maxAge * 1000);
			}
			return 0;
		}
	}
}
