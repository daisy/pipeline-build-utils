package org.daisy.pipeline.yourkit.probes;

import com.yourkit.probes.MethodPattern;
import com.yourkit.probes.ObjectRowIndexMap;
import com.yourkit.probes.OnEnterResult;
import com.yourkit.probes.Param;
import com.yourkit.probes.ReturnValue;
import com.yourkit.probes.StringColumn;
import com.yourkit.probes.Table;
import com.yourkit.probes.This;

public final class XProc {
	
	@MethodPattern("com.xmlcalabash.core.XProcRuntime:load(*)")
	public static final class XPipelineProbe {
		public static void onReturn(@Param(1) Object input,
		                            @ReturnValue Object pipeline) {
			PIPELINES.createRow(pipeline, getUri(input));
		}
	}
	
	@MethodPattern("com.xmlcalabash.runtime.XPipeline:run()")
	public static final class XPipelineRunProbe {
		public static int onEnter(@This Object pipeline) {
			return RUNS.createRow(pipeline);
		}
		public static void onReturn(@OnEnterResult int row) {
			RUNS.closeRow(row);
		}
	}
	
	private static final PipelineTable PIPELINES = new PipelineTable();
	private static class PipelineTable extends Table {
		public PipelineTable() {
			super("XProc Pipelines", Table.MASK_FOR_GENERIC_LASTING_EVENTS);
		}
		public final StringColumn SOURCE = new StringColumn("Source");
		private final ObjectRowIndexMap rows = new ObjectRowIndexMap();
		public void createRow(Object pipeline, String source) {
			int row = createRow();
			rows.put(pipeline, row);
			SOURCE.setValue(row, source);
		}
		public int getRow(Object pipeline) {
			return rows.get(pipeline);
		}
	}
	
	private static final PipelineRunTable RUNS = new PipelineRunTable();
	private static class PipelineRunTable extends Table {
		public PipelineRunTable() {
			super(PIPELINES, "Runs", Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
		public int createRow(Object pipeline) {
			return createRow(PIPELINES.getRow(pipeline));
		}
	}
	
	private static String getUri(Object input) {
		try {
			return (String)input.getClass().getMethod("getUri").invoke(input); }
		catch (Exception e) {
			throw new RuntimeException(e); }
	}
}
