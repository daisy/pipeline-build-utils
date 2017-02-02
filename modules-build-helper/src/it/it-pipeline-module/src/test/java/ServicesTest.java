import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.XProcScriptService;

import org.daisy.pipeline.junit.AbstractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class ServicesTest extends AbstractTest {
	
	@Inject
	// public DatatypeRegistry datatypes;
	public DatatypeService datatype;
	
	@Test
	public void testDatatype() {
		String id = datatype.getId();
		assertTrue(id.equals("foo:choice") ||
		           id.equals("px:script-option-1"));
		// FIXME: DefaultDatatypeRegistry (framework-core) must support SPI
		// Set<String> ids = new HashSet<>();
		// for (DatatypeService datatype : datatypes.getDatatypes())
		// 	ids.add(datatype.getId());
		// assertTrue(ids.remove("foo:choice"));
		// assertTrue(ids.remove("px:script-option-1"));
		// assertTrue(ids.isEmpty());
	}
	
	@Inject
	public XProcScriptService script;
	
	@Test
	public void testScript() {
		assertEquals("script", script.getId());
	}
	
	/* ------------- */
	/* For OSGi only */
	/* ------------- */
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/resolver-mock.xml");
		return probe;
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
