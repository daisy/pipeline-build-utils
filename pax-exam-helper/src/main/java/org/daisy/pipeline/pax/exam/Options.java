package org.daisy.pipeline.pax.exam;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;

public abstract class Options {
	
	public static SystemPropertyOption logbackConfigFile() {
		return systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml");
	}
	
	public static SystemPropertyOption calabashConfigFile() {
		return systemProperty("org.daisy.pipeline.xproc.configuration").value(PathUtils.getBaseDir() + "/src/test/resources/config-calabash.xml");
	}
	
	public static SystemPackageOption domTraversalPackage() {
		return systemPackage("org.w3c.dom.traversal;uses:=\"org.w3c.dom\";version=\"0.0.0.1\"");
	}
	
	public static Option loggingBundles() {
		return composite(
			mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").version("1.7.2"),
			mavenBundle().groupId("ch.qos.logback").artifactId("logback-core").version("1.0.11"),
			mavenBundle().groupId("ch.qos.logback").artifactId("logback-classic").version("1.0.11")
		);
	}
	
	public static MavenArtifactProvisionOption felixDeclarativeServices() {
		return mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("1.6.2");
	}
	
	public static Option spiflyBundles() {
		return composite(
			mavenBundle().groupId("org.ow2.asm").artifactId("asm-all").version("4.0"),
			mavenBundle().groupId("org.apache.aries").artifactId("org.apache.aries.util").version("1.0.0"),
			mavenBundle().groupId("org.apache.aries.spifly").artifactId("org.apache.aries.spifly.dynamic.bundle").version("1.0.0")
		);
	}
	
	public static Option pipelineFrameworkBundles() {
		return composite(
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("jing").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("saxon-he").versionAsInProject(),
			mavenBundle().groupId("org.slf4j").artifactId("jcl-over-slf4j").versionAsInProject(),
			mavenBundle().groupId("commons-codec").artifactId("commons-codec").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("commons-httpclient").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("com.xmlcalabash").versionAsInProject(),
			mavenBundle().groupId("org.eclipse.persistence").artifactId("javax.persistence").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("common-utils").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("xpath-registry").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("xproc-api").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("common-stax").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("framework-core").versionAsInProject(),
			mavenBundle().groupId("org.codehaus.woodstox").artifactId("woodstox-core-lgpl").versionAsInProject(),
			mavenBundle().groupId("org.codehaus.woodstox").artifactId("stax2-api").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("woodstox-osgi-adapter").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("xmlcatalog").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("modules-api").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("modules-registry").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("calabash-adapter").versionAsInProject()
		);
	}
	
	public static MavenArtifactProvisionOption pipelineModule(String artifactId) {
		return mavenBundle().groupId("org.daisy.pipeline.modules").artifactId(artifactId).versionAsInProject();
	}
	
	public static UrlProvisionOption thisBundle() {
		return bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/");
	}
	
	private static WrappedUrlProvisionOption xprocspec() {
		String versionAsInProject = MavenUtils.asInProject().getVersion("org.daisy", "xprocspec");
		MavenArtifactProvisionOption xprocspec = mavenBundle().groupId("org.daisy").artifactId("xprocspec").version(versionAsInProject);
		return wrappedBundle(xprocspec).bundleSymbolicName("org.daisy.xprocspec").bundleVersion(versionAsInProject.replaceAll("-","."));
	}
	
	public static Option xprocspecBundles() {
		return composite(
			mavenBundle().groupId("org.daisy.maven").artifactId("xproc-engine-api").versionAsInProject(),
			mavenBundle().groupId("org.daisy.maven").artifactId("xproc-engine-daisy-pipeline").versionAsInProject(),
			xprocspec(),
			mavenBundle().groupId("org.daisy.maven").artifactId("xprocspec-runner").versionAsInProject()
		);
	}
	
	public static Option xspecBundles() {
		return composite(
			mavenBundle().groupId("org.daisy.pipeline").artifactId("saxon-adapter").versionAsInProject(),
			mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.xmlresolver").versionAsInProject(),
			mavenBundle().groupId("org.daisy.maven").artifactId("xspec-runner").versionAsInProject()
		);
	}
}
