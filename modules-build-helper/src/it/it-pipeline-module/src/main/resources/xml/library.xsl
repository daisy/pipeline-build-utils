<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <xsl:function name="pf:xslt-function" visibility="public">
        <xsl:param name="param"/>
        <xsl:sequence select="upper-case($param)"/>
    </xsl:function>

</xsl:stylesheet>
