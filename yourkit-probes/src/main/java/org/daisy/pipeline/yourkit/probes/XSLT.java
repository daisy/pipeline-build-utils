package org.daisy.pipeline.yourkit.probes;

import java.util.HashMap;
import java.util.Map;

import com.yourkit.probes.MethodPattern;
import com.yourkit.probes.ObjectRowIndexMap;
import com.yourkit.probes.OnEnterResult;
import com.yourkit.probes.Param;
import com.yourkit.probes.ReturnValue;
import com.yourkit.probes.StringColumn;
import com.yourkit.probes.Table;
import com.yourkit.probes.This;

public final class XSLT {
	
	@MethodPattern("net.sf.saxon.s9api.XsltCompiler:compile(*)")
	public static final class XsltExecutableProbe {
		public static void onReturn(@Param(1) Object source,
		                            @ReturnValue Object executable) {
			EXECUTABLES.createRow(executable, getSystemId(source));
		}
	}
	
	@MethodPattern("net.sf.saxon.s9api.XsltExecutable:load()")
	public static final class XsltTransformerProbe {
		public static void onReturn(@This Object executable,
		                            @ReturnValue Object transformer) {
			EXECUTABLES.assocTransformer(executable, transformer);
		}
	}
	
	@MethodPattern("net.sf.saxon.s9api.XsltTransformer:setSource(*)")
	public static final class XsltTransformerSourceProbe {
		public static void onEnter(@This Object transformer,
		                           @Param(1) Object source) {
			TRANSFORMATIONS.setSource(transformer, getSystemId(source));
		}
	}
	
	@MethodPattern("net.sf.saxon.s9api.XsltTransformer:transform()")
	public static final class XsltTransformProbe {
		public static int onEnter(@This Object transformer) {
			return TRANSFORMATIONS.createRow(transformer);
		}
		public static void onReturn(@OnEnterResult int row) {
			TRANSFORMATIONS.closeRow(row);
		}
	}
	
	private static final ExecutableTable EXECUTABLES = new ExecutableTable();
	private static class ExecutableTable extends Table {
		public ExecutableTable() {
			super("XSLT Transformators", Table.MASK_FOR_GENERIC_LASTING_EVENTS);
		}
		public final StringColumn SOURCE = new StringColumn("Source");
		private final ObjectRowIndexMap rows = new ObjectRowIndexMap();
		public void createRow(Object executable, String source) {
			int row = createRow();
			rows.put(executable, row);
			SOURCE.setValue(row, source);
		}
		private final Map transformerToExecutable = new HashMap();
		public void assocTransformer(Object executable, Object transformer) {
			transformerToExecutable.put(transformer, executable);
		}
		public int getRowForTransformer(Object transformer) {
			return rows.get(transformerToExecutable.get(transformer));
		}
	}
	
	private static final TransformationTable TRANSFORMATIONS = new TransformationTable();
	private static class TransformationTable extends Table {
		public TransformationTable() {
			super(EXECUTABLES, "Transformations", Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
		public final StringColumn SOURCE = new StringColumn("Source");
		private final Map<Object,String> transformerToSource = new HashMap<Object,String>();
		public void setSource(Object transformer, String source) {
			transformerToSource.put(transformer, source);
		}
		public int createRow(Object transformer) {
			int row = createRow(EXECUTABLES.getRowForTransformer(transformer));
			SOURCE.setValue(row, transformerToSource.get(transformer));
			return row;
		}
	}
	
	private static String getSystemId(Object source) {
		try {
			return (String)source.getClass().getMethod("getSystemId").invoke(source); }
		catch (Exception e) {
			throw new RuntimeException(e); }
	}
}
