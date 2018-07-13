import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.script.XProcScriptService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class OSGiTest extends AbstractTest {
	
	@Inject
	public DatatypeRegistry datatypes;
	
	@Test
	public void testDatatype() {
		Set<String> ids = new HashSet<>();
		for (DatatypeService datatype : datatypes.getDatatypes())
			ids.add(datatype.getId());
		assertTrue(ids.remove("foo:choice"));
		assertTrue(ids.remove("px:script-option-1"));
		assertTrue(ids.remove("transform-query")); // because o.d.p.modules.braille:common-utils on class path
		assertTrue(ids.isEmpty());
	}
	
	@Inject
	public XProcScriptService script;
	
	@Test
	public void testScript() {
		assertEquals("my-script", script.getId());
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline.modules.braille:liblouis-core:?",
		};
	}
}
