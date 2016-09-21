package org.daisy.pipeline.build.annotations;

import java.util.ArrayList;
import java.util.List;

public class ComponentModel {
	
	String packageName;
	String className;
	String qualifiedClassName;
	String spiClassName;
	ActivateModel activate;
	boolean immediate;
	final List<PropertyModel> properties = new ArrayList<PropertyModel>();
	final List<String> services = new ArrayList<String>();
	final List<ReferenceModel> references = new ArrayList<ReferenceModel>();
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}
	
	public String getSpiClassName() {
		return spiClassName;
	}
	
	public List<PropertyModel> getProperties() {
		return properties;
	}
	
	public ActivateModel getActivate() {
		return activate;
	}
	
	public boolean getImmediate() {
		return immediate || services.isEmpty();
	}
	
	public List<ReferenceModel> getReferences() {
		return references;
	}
	
	public static class ActivateModel {
		
		String methodName;
		Class<?> propertiesArgumentType;
		
		public String getMethodName() {
			return methodName;
		}
		
		public Class<?> getPropertiesArgumentType() {
			return propertiesArgumentType;
		}
	}
	
	public static class ReferenceModel {
		
		String methodName;
		String service;
		String cardinality;
		
		public String getMethodName() {
			return methodName;
		}
		
		public String getService() {
			return service;
		}
		
		public String getCardinality() {
			return cardinality;
		}
	}
	
	public static class PropertyModel {
		
		Class<?> type;
		String key;
		Object value;
		
		public String getKeyLiteral() {
			return "\"" + key.replaceAll("\"", "\\\"") + "\"";
		}
		
		public String getValueLiteral() {
			if (type == String.class)
				return "\"" + value.toString().replaceAll("\"", "\\\"") + "\"";
			else
				return value.toString();
		}
	}
}
