import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

// does not do anything
// needed by org.daisy.pipeline.datatypes.impl.Datatype_px_script_option_1
public class URIResolverMock implements URIResolver {
	public Source resolve(String href, String base) throws TransformerException {
		throw new UnsupportedOperationException();
	}
}
