import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.XProcScriptService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SPITest {
	
	@Test
	public void testDatatype() {
		Set<String> ids = new HashSet<>();
		Iterator<DatatypeService> datatypes = ServiceLoader.load(DatatypeService.class).iterator();
		while (datatypes.hasNext())
			ids.add(datatypes.next().getId());
		assertTrue(ids.remove("foo:choice"));
		assertTrue(ids.remove("px:script-option-1"));
		assertTrue(ids.isEmpty());
	}
	
	@Test
	public void testScript() {
		Iterator<XProcScriptService> scripts = ServiceLoader.load(XProcScriptService.class).iterator();
		assertTrue(scripts.hasNext());
		XProcScriptService s = scripts.next();
		assertEquals("script", s.getId());
		assertFalse(scripts.hasNext());
	}
}
