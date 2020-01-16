package org.ws4d.coap.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapEmptyMessage;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class BasicCoapClient implements CoapClient {
    private static String SERVER_ADDRESS = "localhost";
    private static int PORT = Constants.COAP_DEFAULT_PORT;
    static int counter = 0;
    CoapChannelManager channelManager = null;
    CoapClientChannel clientChannel = null;

    public static void main(String[] args) {
	if (args != null && args.length >= 1) {
		SERVER_ADDRESS = args[0];
		if (args.length >= 2)
			PORT = Integer.parseInt(args[1]);
	}
        System.out.println("Start CoAP Client: " + SERVER_ADDRESS);
        BasicCoapClient client = new BasicCoapClient();
        client.channelManager = BasicCoapChannelManager.getInstance();
        client.runTestClient();
    }
    
    public void runTestClient(){
    	try {
			clientChannel = channelManager.connect(this, InetAddress.getByName(SERVER_ADDRESS), PORT);
			//CoapRequest coapRequest = clientChannel.createRequest(true, CoapRequestCode.GET);
//			coapRequest.setContentType(CoapMediaType.octet_stream);
//			coapRequest.setToken("ABCD".getBytes());
//			coapRequest.setUriHost("123.123.123.123");
//			coapRequest.setUriPort(1234);
//			coapRequest.setUriPath("/sub1/sub2/sub3/");
//			coapRequest.setUriQuery("a=1&b=2&c=3");
//			coapRequest.setProxyUri("http://proxy.org:1234/proxytest");

			CoapRequest coapRequest = clientChannel.createRequest(true, CoapRequestCode.POST);
			//coapRequest.setContentType(CoapMediaType.octet_stream);
			coapRequest.setToken("ABCD".getBytes());
			//coapRequest.setUriHost("123.123.123.123");
			//coapRequest.setUriPort(1234);
			coapRequest.setUriPath("/IoTSunRPi001/GPIO21/tempSensor/");
			coapRequest.setUriQuery("temperatureCelsius=27&c=3");
			
			clientChannel.sendMessage(coapRequest);
			System.out.println("Sent Request");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		System.out.println("Received response");
	}
}
