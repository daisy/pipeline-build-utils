<?xml version="1.0" encoding="UTF-8"?>
<xsl:package xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
             xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
             xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
             name="http://www.daisy.org/pipeline/modules/foo-utils/library.xsl"
             package-version="0-SNAPSHOT">
	
	<xd:doc scope="stylesheet">
		<xd:desc>
			<xd:p><xd:b>Author:</xd:b> bert</xd:p>
		</xd:desc>
	</xd:doc>
	
	<xsl:include href="foo.xsl"/>
	
	<xsl:expose component="function"
	            names="pf:foo
	                   pf:bar"
	            visibility="final"/>
	
</xsl:package>
