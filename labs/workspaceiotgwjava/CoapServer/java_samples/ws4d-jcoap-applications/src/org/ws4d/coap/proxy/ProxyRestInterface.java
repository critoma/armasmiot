package org.ws4d.coap.proxy;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResourceServer;

public class ProxyRestInterface {
	static Logger logger = Logger.getLogger(Proxy.class);
	private CoapResourceServer resourceServer;
	
	public void start(){
	    if (resourceServer != null)
		    resourceServer.stop();
		resourceServer = new CoapResourceServer();
		resourceServer.createResource(new ProxyStatisticResource());
		try {
			resourceServer.start(5684);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class ProxyStatisticResource extends BasicCoapResource{
		
	    private ProxyStatisticResource(String path, byte[] value, CoapMediaType mediaType) {
			super(path, value, mediaType);
		}
	    
	    public ProxyStatisticResource(){
	    	this("/statistic", null, CoapMediaType.text_plain);
	    }

		@Override
		public byte[] getValue(Vector<String> query) {
			StringBuilder val = new StringBuilder();
			ProxyMapper.getInstance().getCoapRequestCount();
			val.append("Number of HTTP Requests: " + ProxyMapper.getInstance().getHttpRequestCount() + "\n");
			val.append("Number of CoAP Requests: " + ProxyMapper.getInstance().getCoapRequestCount()  + "\n");
			val.append("Number of Reqeusts served from cache: " + ProxyMapper.getInstance().getServedFromCacheCount() + "\n");
			return val.toString().getBytes();
		}
	}
}
