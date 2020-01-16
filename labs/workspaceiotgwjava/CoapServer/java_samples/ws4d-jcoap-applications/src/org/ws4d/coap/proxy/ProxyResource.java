package org.ws4d.coap.proxy;

import org.apache.log4j.Logger;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;

public class ProxyResource extends BasicCoapResource {
	static Logger logger = Logger.getLogger(Proxy.class);
	
	private ProxyResourceKey key = null;
	

	public ProxyResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		
	}
	
	public ProxyResourceKey getKey() {
		return key;
	}

	public void setKey(ProxyResourceKey key) {
		this.key = key;
	}
}
