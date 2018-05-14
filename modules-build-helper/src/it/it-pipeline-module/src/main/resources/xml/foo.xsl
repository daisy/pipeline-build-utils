<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Author:</xd:b> bert</xd:p>
        </xd:desc>
    </xd:doc>
    
    <xsl:template match="@*|node()">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xd:doc>
        <xd:desc>
            <xd:p>Do something.</xd:p>
        </xd:desc>
        <xd:param name="string">
            <xd:p>A string</xd:p>
        </xd:param>
        <xd:return>
            <xd:p>A boolean</xd:p>
        </xd:return>
    </xd:doc>
    <xsl:function name="pf:foo" as="xs:boolean">
        <xsl:param name="string" as="xs:string"/>
        <xsl:sequence select="true()"/>
    </xsl:function>
    
    <xd:doc>
        <xd:desc>
            <xd:p>Do something else. See also <xd:ref name="pf:foo"
            type="function">pf:foo</xd:ref></xd:p>
        </xd:desc>
        <xd:return>
            <xd:p>A boolean</xd:p>
        </xd:return>
    </xd:doc>
    <xsl:function name="pf:bar" as="xs:boolean">
        <xsl:sequence select="false()"/>
    </xsl:function>
    
</xsl:stylesheet>
