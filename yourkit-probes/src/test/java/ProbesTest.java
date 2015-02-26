import javax.inject.Inject;

import java.io.File;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.braille.liblouis.Liblouis;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.forThisPlatform;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;

import org.osgi.framework.BundleContext;

import org.xml.sax.InputSource;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ProbesTest {
	
	@Inject
	Liblouis liblouis;
	
	@Test
	public void testLiblouisTranslation() {
		assertEquals("foobar",
		             liblouis.translate("file:" + PathUtils.getBaseDir() + "/target/test-classes/foobar.cti",
		                                "foobar", false, null));
	}
	
	@Inject
	BundleContext context;
	
	@Test
	public void testCustomXPathFunction() throws XPathExpressionException {
		context.registerService(
			ExtensionFunctionDefinition.class.getName(),
			new ExtensionFunctionDefinition() {
				public StructuredQName getFunctionQName() {
					return new StructuredQName("ex", "http://example.net/ns", "foo");
				}
				public SequenceType[] getArgumentTypes() {
					return new SequenceType[] {};
				}
				public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
					return SequenceType.SINGLE_STRING;
				}
				public ExtensionFunctionCall makeCallExpression() {
					return new ExtensionFunctionCall() {
						public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
							return new StringValue("foobar");
						}
					};
				}
			},
			new Hashtable<String,Object>());
		
		// Don't use @Inject here because the ExtensionFunctionDefinition binding on
		// org.daisy.common.xpath.saxon.XPathFactoryImpl is static, which means that the injected xpathFactory
		// would not have the custom function registered.
		XPathFactory xpathFactory = (XPathFactory)context.getService(context.getServiceReference(XPathFactory.class.getName()));
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(
			new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					return "ex".equals(prefix) ? "http://example.net/ns" : null; }
				public String getPrefix(String namespaceURI) {
					return null; }
				public Iterator<String> getPrefixes(String namespaceURI) {
					return Collections.emptyIterator(); }
			}
		);
		assertEquals("foobar", xpath.compile("ex:foo()").evaluate(null, XPathConstants.STRING));
	}
	
	@Configuration
	public Option[] config() {
		File probeClasspath = new File(PathUtils.getBaseDir() + "/target/classes");
		return options(
			vmOption("-agentpath:${yourkit.home}/bin/mac/libyjpagent.jnilib="
			         + "dir=" + PathUtils.getBaseDir() + "/target/yourkit"
			         + ",onexit=memory"
			         + ",probeclasspath=" + probeClasspath
			         + ",probe=org.daisy.pipeline.yourkit.probes.Liblouis"
			         + ",probe=org.daisy.pipeline.yourkit.probes.XPath"
			),
			vmOption("-Xbootclasspath/a:" + probeClasspath),
			bootDelegationPackage("org.daisy.pipeline.yourkit.probes"),
			logbackConfigFile(),
			logbackBundles(),
			felixDeclarativeServices(),
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("net.java.dev.jna").artifactId("jna").versionAsInProject(),
			mavenBundle().groupId("org.liblouis").artifactId("liblouis-java").versionAsInProject(),
			brailleModule("common-java"),
			brailleModule("liblouis-core"),
			forThisPlatform(brailleModule("liblouis-native")),
			mavenBundle().groupId("org.daisy.libs").artifactId("saxon-he").versionAsInProject(),
			mavenBundle().groupId("org.daisy.pipeline").artifactId("saxon-adapter").versionAsInProject(),
			junitBundles()
		);
	}
}
