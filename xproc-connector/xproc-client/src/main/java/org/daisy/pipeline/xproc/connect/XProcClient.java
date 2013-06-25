package org.daisy.pipeline.xproc.connect;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.tools.jsonrpc.JsonRpcClient;

public class XProcClient {
	
	private static final String QUEUE_NAME = "pipeline_xproc_connect";
	private static final int RPC_TIMEOUT = 10000;
	
	private Connection connection;
	
	public XProcService openConnection() {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			JsonRpcClient client = new JsonRpcClient(channel, "", QUEUE_NAME, RPC_TIMEOUT);
			return (XProcService)client.createProxy(XProcService.class); }
		catch (Exception e) { throw new RuntimeException(e); }
	}
	
	public void closeConnection() throws IOException {
		connection.close();
	}
}
