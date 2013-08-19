package org.daisy.pipeline.xproc.connect;

import java.util.List;
import java.util.Map;

public interface XProcService {
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters);
}
