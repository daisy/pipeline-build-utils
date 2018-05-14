package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.calabash.Calabash;

import org.daisy.pipeline.maven.plugin.utils.URLs;
import static org.daisy.pipeline.maven.plugin.utils.URIs.asURI;

@Mojo(
	name = "xsl-doc"
)
public class XslDocMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.basedir}/src/main/resources"
	)
	private File sourceDirectory;
	
	@Parameter(
		defaultValue = "${project.basedir}/src/main/resources/META-INF/catalog.xml"
	)
	private File catalogXmlFile;
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-resources/doc/"
	)
	private File outputDirectory;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.artifactId}"
	)
	private String projectArtifactId;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.groupId}"
	)
	private String projectGroupId;
	
	public void execute() throws MojoFailureException {
		if (!catalogXmlFile.exists()) {
			getLog().info("File " + catalogXmlFile + " does not exist. Skipping xsl-doc goal.");
			return; }
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
				public URLStreamHandler createURLStreamHandler(String protocol) {
					if ("cp".equals(protocol))
						return new top.marchand.xml.protocols.cp.Handler();
					return null;
				}});
		Calabash engine = new Calabash();
		outputDirectory.mkdirs();
		String absoluteRootFolder = asURI(sourceDirectory).toASCIIString();
		if (absoluteRootFolder.endsWith("/"))
			absoluteRootFolder = absoluteRootFolder.substring(0, absoluteRootFolder.length() - 1);
		String outputFolder = asURI(outputDirectory).toASCIIString();
		if (outputFolder.endsWith("/"))
			outputFolder = outputFolder.substring(0, outputFolder.length() - 1);
		try {
			engine.setDefaultConfiguration(
				new InputStreamReader(URLs.resolve(asURI(XProcDocMojo.class.getResource("/catalog-to-xsl-doc.xpl")),
				                                   "config-calabash.xml")
				                          .openStream()));
			engine.run(asURI(XProcDocMojo.class.getResource("/catalog-to-xsl-doc.xpl")).toASCIIString(),
			           ImmutableMap.of("source", Collections.singletonList(asURI(catalogXmlFile).toASCIIString())),
			           null,
			           ImmutableMap.of("projectName", projectGroupId + ":" + projectArtifactId,
			                           "absoluteRootFolder", absoluteRootFolder,
			                           "outputFolder", outputFolder),
			           null); }
		catch (XProcExecutionException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage()); }
		catch (IOException e) {
			throw new MojoFailureException(e.getMessage()); }
	}
}
