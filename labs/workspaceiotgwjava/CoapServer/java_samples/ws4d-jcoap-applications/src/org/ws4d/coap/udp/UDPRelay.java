package org.ws4d.coap.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class UDPRelay {
	public static final int SERVER_PORT = 6000;
	public static final int CLIENT_PORT = 8000;
	public static final int UDP_BUFFER_SIZE = 66000; // max UDP size = 65535 
	

	public static void main(String[] args) {
		if (args.length < 2){
			System.out.println("expected parameter: server host and port, e.g. 192.168.1.1 1234");
			System.exit(-1);
		}

		UDPRelay relay = new UDPRelay();
		relay.run(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
	}
	
	private DatagramChannel serverChannel = null;
	private DatagramChannel clientChannel = null;
	ByteBuffer serverBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	ByteBuffer clientBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	Selector selector = null;
	InetSocketAddress clientAddr = null;

	public void run(InetSocketAddress serverAddr) {
		try {
			serverChannel = DatagramChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(SERVER_PORT));  
			serverChannel.configureBlocking(false);
			serverChannel.connect(serverAddr);
			
			clientChannel = DatagramChannel.open();
			clientChannel.socket().bind(new InetSocketAddress(CLIENT_PORT));  
			clientChannel.configureBlocking(false);

		    try {
				selector = Selector.open();
				serverChannel.register(selector, SelectionKey.OP_READ);
				clientChannel.register(selector, SelectionKey.OP_READ);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Initialization failed, Shut down");
			System.exit(-1);
		}
		System.out.println("Start UDP Realy on Server Port " + SERVER_PORT + " and Client Port " + CLIENT_PORT);

		int serverLen = 0;
		while (true) {
			/* Receive Packets */
			InetSocketAddress tempClientAddr = null;
			try {
				clientBuffer.clear();
				tempClientAddr = (InetSocketAddress) clientChannel.receive(clientBuffer);
				clientBuffer.flip();
				
				serverBuffer.clear();
				serverLen = serverChannel.read(serverBuffer);
				serverBuffer.flip();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Read failed");
			}
			
			/* forward/send packets client -> server*/
			if (tempClientAddr != null) {
				/* the client address is obtained automatically by the first request of the client 
				 * clientAddr is the last known valid address of the client */
				clientAddr = tempClientAddr;
				try {
					serverChannel.write(clientBuffer);
					System.out.println("Forwarded Message client ("+clientAddr.getHostName()+" "+clientAddr.getPort()
							+ ") -> server (" + serverAddr.getHostName()+" " + serverAddr.getPort() + "): " 
							+ clientBuffer.limit() + " bytes");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Send failed");
				}
			}

			/* forward/send packets server -> client*/
			if (serverLen > 0) {
				try {
					clientChannel.send(serverBuffer, clientAddr);
					System.out.println("Forwarded Message server ("+serverAddr.getHostName()+" "+serverAddr.getPort()
							+ ") -> client (" + clientAddr.getHostName()+" " + clientAddr.getPort() + "): " 
							+ serverBuffer.limit() + " bytes");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Send failed");
				}
			}
			
			/* Select */
			try {
				selector.select(2000);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("select failed");
			}
		}
	}
}
