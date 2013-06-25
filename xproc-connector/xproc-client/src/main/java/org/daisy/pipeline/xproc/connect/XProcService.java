package org.daisy.pipeline.xproc.connect;

import java.util.Map;

public interface XProcService {
	public void run(String pipeline,
	                Map<String,String> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters);
}
