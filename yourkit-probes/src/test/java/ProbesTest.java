import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.stream.StreamSource;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XPipeline;
import com.xmlcalabash.util.Input;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.braille.common.CSSStyledText;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;

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
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;

import org.osgi.framework.BundleContext;

import org.xml.sax.InputSource;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ProbesTest {
	
	@Test
	public void runTests() throws InterruptedException, XPathExpressionException, SaxonApiException {
		// Wait a few seconds to give me some time to attach the profiler to the process.
		// The process is called "RemoteFrameworkImpl"
		Thread.sleep(10000);
		// run tests in a specific order
		testLiblouisTranslation();
		testCustomXPathFunction();
		testXsltTransformation();
		testXProc();
		// Give some time to refresh the view before the process ends
		Thread.sleep(10000);
	}
	
	@Inject
	LiblouisTranslator.Provider liblouis;
	
	public void testLiblouisTranslation() {
		assertEquals(braille("foobar"),
		             liblouis.get(mutableQuery().add("table","file:" + PathUtils.getBaseDir() + "/target/test-classes/foobar.cti")
		                                        .add("output", "ascii"))
		                     .iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(text("foobar")));
	}
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}
	
	private Iterable<String> braille(String... text) {
		return Arrays.asList(text);
	}
	
	@Inject
	BundleContext context;
	
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
	
	@Inject
	Processor processor;
	
	public void testXsltTransformation() throws SaxonApiException {
		XsltTransformer transformer = processor.newXsltCompiler().compile(
				new StreamSource(new File(PathUtils.getBaseDir(), "target/test-classes/identity.xsl"))).load();
		transformer.setSource(new StreamSource(new File(PathUtils.getBaseDir(), "target/test-classes/hello.xml")));
		XdmDestination dest = new XdmDestination();
		transformer.setDestination(dest);
		transformer.transform();
	}
	
	public void testXProc() throws SaxonApiException {
		System.setProperty("com.xmlcalabash.config.user", "false");
		System.setProperty("com.xmlcalabash.config.local", "false");
		XProcRuntime runtime = new XProcRuntime(new XProcConfiguration("he", false));
		XPipeline pipeline = runtime.load(new Input("file:" + new File(PathUtils.getBaseDir(), "target/test-classes/identity.xpl")));
		pipeline.writeTo("source", runtime.parse(new InputSource("file:" + new File(PathUtils.getBaseDir(), "target/test-classes/hello.xml"))));
		pipeline.run();
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
			         + ",probe=org.daisy.pipeline.yourkit.probes.XSLT"
			         + ",probe=org.daisy.pipeline.yourkit.probes.XProc"
			),
			vmOption("-Xbootclasspath/a:" + probeClasspath),
			bootDelegationPackage("org.daisy.pipeline.yourkit.probes"),
			logbackConfigFile(),
			felixDeclarativeServices(),
			domTraversalPackage(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("common-utils"),
				brailleModule("liblouis-core"),
				brailleModule("liblouis-native").forThisPlatform(),
				mavenBundle("org.daisy.pipeline:saxon-adapter:?"),
				mavenBundle("org.daisy.libs:com.xmlcalabash:?"),
				logbackClassic())
		);
	}
}
