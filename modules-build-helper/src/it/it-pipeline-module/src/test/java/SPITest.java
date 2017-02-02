import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.XProcScriptService;

import org.daisy.pipeline.junit.OSGiLessRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.junit.Test;

@RunWith(OSGiLessRunner.class)
public class SPITest {
	
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
}
