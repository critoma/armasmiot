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

import org.apache.log4j.Logger;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class CoapClientProxy implements CoapClient{
	static Logger logger = Logger.getLogger(Proxy.class);
	ProxyMapper mapper = ProxyMapper.getInstance();
	static final boolean RELIABLE = true; //use CON as client (NON has no timeout!!!)
	

	/* creates a client channel and stores it in the context*/
	public void createChannel(ProxyMessageContext context){
		// create channel
		CoapClientChannel channel;
		channel = BasicCoapChannelManager.getInstance().connect(this, context.getServerAddress(), context.getServerPort());
		if (channel != null) {
			channel.setTrigger(context);
			context.setOutCoapClientChannel(channel);
		} else {
			throw new IllegalStateException("CoAP client connect() failed");
		}
	}
	
	public void closeChannel(ProxyMessageContext context){
		context.getOutCoapClientChannel().close();
	}
	
	
	public void sendRequest(ProxyMessageContext context) {
		context.getOutCoapRequest().getChannel().sendMessage(context.getOutCoapRequest());
	}
		
	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		ProxyMessageContext context = (ProxyMessageContext) channel.getTrigger();
		channel.close();
		if (context != null) {
			context.setInCoapResponse(response);
			mapper.handleCoapClientResponse(context);
		}
	}

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		ProxyMessageContext context = (ProxyMessageContext) channel.getTrigger();
		channel.close();
		if (context != null) {
			logger.warn("Coap client connection failed (e.g., timeout)!");
			context.setInCoapResponse(null); // null indicates no response
			mapper.handleCoapClientResponse(context);
		}
	}

}
