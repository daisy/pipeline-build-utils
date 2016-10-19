package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

abstract class utils {
	
	static abstract class URIs {
		
		static URI asURI(Object o) {
			if (o == null)
				return null;
			try {
				if (o instanceof String)
					return new URI((String)o);
				if (o instanceof File)
					return ((File)o).toURI();
				if (o instanceof URL) {
					URL url = (URL)o;
					if (url.getProtocol().equals("jar"))
						return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
					String authority = (url.getPort() != -1) ?
						url.getHost() + ":" + url.getPort() :
						url.getHost();
					return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }
				if (o instanceof URI)
					return (URI)o; }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to URI: " + o);
		}
	}
	
	static abstract class URLs {
		
		static URL resolve(URI base, String url) {
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
