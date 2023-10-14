package org.daisy.pipeline.yourkit.probes;

import java.lang.reflect.Method;
import java.util.Arrays;
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

public final class XPath {
	
	@MethodPattern("net.sf.saxon.lib.ExtensionFunctionDefinition:<init>()")
	public static final class ExtensionFunctionProbe {
		public static void onReturn(@This Object definition) {
			String qName = invoke(definition, "net.sf.saxon.lib.ExtensionFunctionDefinition", "getFunctionQName").toString();
			EXTENSION_FUNCTIONS.createRow(definition, qName);
		}
	}
	
	@MethodPattern("*:makeCallExpression()")
	public static final class ExtensionFunctionCallExpressionProbe {
		public static void onReturn(@This Object definition,
		                            @ReturnValue Object call) {
			if (isInstanceOf(definition, "net.sf.saxon.lib.ExtensionFunctionDefinition"))
				EXTENSION_FUNCTIONS.assocCall(definition, call);
		}
	}
	
	@MethodPattern("*:call(*)")
	public static final class ExtensionFunctionCallProbe {
		public static int onEnter(@This Object call,
		                          @Param(1) Object context,
		                          @Param(2) Object arguments) {
			if (isInstanceOf(call, "net.sf.saxon.lib.ExtensionFunctionCall"))
				return EXTENSION_FUNCTION_CALLS.createRow(call, Arrays.toString((Object[])arguments));
			else
				return Table.NO_ROW;
		}
		public static void onReturn(@OnEnterResult int row) {
			if (row != Table.NO_ROW)
				EXTENSION_FUNCTION_CALLS.closeRow(row);
		}
	}
	
	private static final ExtensionFunctionTable EXTENSION_FUNCTIONS = new ExtensionFunctionTable();
	private static class ExtensionFunctionTable extends Table {
		public ExtensionFunctionTable() {
			super("XPath Extension Functions", Table.MASK_FOR_GENERIC_LASTING_EVENTS);
		}
		public final StringColumn QNAME = new StringColumn("QName");
		private final ObjectRowIndexMap rows = new ObjectRowIndexMap();
		public void createRow(Object definition, String qName) {
			int row = createRow();
			rows.put(definition, row);
			QNAME.setValue(row, qName);
		}
		private final Map callToDefinition = new HashMap();
		public void assocCall(Object definition, Object call) {
			callToDefinition.put(call, definition);
		}
		public int getRowForCall(Object call) {
			return rows.get(callToDefinition.get(call));
		}
	}
	
	private static final ExtensionFunctionCallTable EXTENSION_FUNCTION_CALLS = new ExtensionFunctionCallTable();
	private static class ExtensionFunctionCallTable extends Table {
		public ExtensionFunctionCallTable() {
			super(EXTENSION_FUNCTIONS, "Calls", Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
		private final StringColumn INPUT = new StringColumn("Input");
		public int createRow(Object call, String input) {
			int row = createRow(EXTENSION_FUNCTIONS.getRowForCall(call));
			INPUT.setValue(row, input);
			return row;
		}
	}
	
	private static Object invoke(Object obj, String className, String methodName, Object... params) {
		Class c = obj.getClass();
		while (!c.getName().equals(className))
			c = c.getSuperclass();
		Method method;
		Class[] paramTypes = new Class[params.length];
		int i = 0;
		for (Object o : params)
			paramTypes[i++] = o.getClass();
		try {
			method = c.getMethod(methodName, paramTypes);
			return method.invoke(obj, params); }
		catch (Exception e) {
			throw new RuntimeException(e); }
	}
	
	private static boolean isInstanceOf(Object obj, String className) {
		Class c = obj.getClass();
		while (c != null) {
			if (c.getName().equals(className))
				return true;
			c = c.getSuperclass(); }
		return false;
	}
}
