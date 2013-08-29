package org.daisy.pipeline.xproc.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;

import org.daisy.common.base.Provider;
import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XProcServer {
	
	private static final String QUEUE_NAME = "pipeline_xproc_connect";
	
	private Connection connection;
	private JsonRpcServer server;
	private XProcEngine engine;
	
	protected void setEngine(XProcEngine engine) {
		this.engine = engine;
	}
	
	protected void launch() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		server = new JsonRpcServer(channel, QUEUE_NAME, XProcService.class, new XProcService() {
			public void run(String pipeline,
			                Map<String,List<String>> inputs,
			                Map<String,String> outputs,
			                Map<String,String> options,
			                Map<String,Map<String,String>> parameters) {
				try {
					XProcPipeline xprocPipeline = engine.load(new URI(pipeline));
					XProcInput.Builder inputBuilder = new XProcInput.Builder();
					if (inputs != null)
						for (String port : inputs.keySet())
							for (String document : inputs.get(port))
								inputBuilder.withInput(port, new LazySaxSourceProvider(document));
					if (options != null)
						for (String name : options.keySet())
							inputBuilder.withOption(new QName("", name), options.get(name));
					if (parameters != null)
						for (String port : parameters.keySet())
							for (String name : parameters.get(port).keySet())
								inputBuilder.withParameter(port, new QName("", name), parameters.get(port).get(name));
					XProcResult results = xprocPipeline.run(inputBuilder.build());
					XProcOutput.Builder outputBuilder = new XProcOutput.Builder();
					for (XProcPortInfo info : xprocPipeline.getInfo().getOutputPorts()) {
						String port = info.getName();
						outputBuilder.withOutput(port, (outputs != null) && outputs.containsKey(port) ?
								new LazySaxResultProvider(outputs.get(port)) :
								new DevNullStreamResultProvider()); }
					results.writeTo(outputBuilder.build()); }
				catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("XProc job failed!"); }}});
		new ServerMainloop(server).start();
		logger.debug("XProc server is up, waiting for XProc requests...");
	}
	
	protected void kill() throws Exception {
		server.terminateMainloop();
		connection.close();
		logger.debug("Shutting down XProc server...");
	}
	
	private static class ServerMainloop extends Thread {
		private final JsonRpcServer server;
		public ServerMainloop(JsonRpcServer server) {
			this.server = server;
		}
		@Override
		public void run() {
			try { server.mainloop(); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
	}
	
	private static class DevNullStreamResultProvider implements Provider<Result> {
		private static final Result result = new StreamResult(
			new OutputStream() {
				@Override public void write(byte[] b, int off, int len) throws IOException {}
				@Override public void write(byte[] b) throws IOException {}
				@Override public void write(int b) throws IOException {}});
		public Result provide() {
			return result;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(XProcServer.class);
}
