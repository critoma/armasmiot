package org.ws4d.coap.server;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResourceServer;
import org.ws4d.coap.rest.ResourceHandler;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * 
 */
public class CoapSampleResourceServer {

	private static CoapSampleResourceServer sampleServer;
	private CoapResourceServer resourceServer;
	private static Logger logger = Logger
			.getLogger(CoapSampleResourceServer.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		logger.setLevel(Level.INFO);
		logger.info("Start Sample Resource Server");
		sampleServer = new CoapSampleResourceServer();
		sampleServer.run();
	}

	private void run() {
		if (resourceServer != null)
			resourceServer.stop();
		resourceServer = new CoapResourceServer();
		
		/* Show detailed logging of Resource Server*/
		Logger resourceLogger = Logger.getLogger(CoapResourceServer.class.getName());
		resourceLogger.setLevel(Level.ALL);
		
		/* add resources */
		BasicCoapResource light = new BasicCoapResource("/test/light", "Content".getBytes(), CoapMediaType.text_plain);
		light.registerResourceHandler(new ResourceHandler() {
			@Override
			public void onPost(byte[] data) {
				System.out.println("Post to /test/light");
			}
		});
		light.setResourceType("light");
		light.setObservable(true);
		
		resourceServer.createResource(light);
		
		try {
			resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
			light.setValue(((String)"Message #" + counter).getBytes());
			light.changed();
		}
	}
}
