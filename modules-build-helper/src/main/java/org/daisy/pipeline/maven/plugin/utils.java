package org.daisy.pipeline.maven.plugin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

abstract class utils {
	
	static abstract class URLs {
		
		public static URL resolve(URI base, String url) {
			try {
				return new URL(new URL(decode(base.toString())), url); }
			catch (MalformedURLException e) {
				throw new RuntimeException(e); }
		}
		
		@SuppressWarnings(
			"deprecation" // URLDecode.decode is deprecated
		)
		private static String decode(String uri) {
			// URIs treat the + symbol as is, but URLDecoder will decode both + and %20 into a space
			return URLDecoder.decode(uri.replace("+", "%2B"));
		}
	}
}
