<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
    xmlns:px="http://www.daisy.org/ns/pipeline" xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc" xmlns:xd="http://www.daisy.org/ns/pipeline/doc"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:param name="outputDir" required="no" select="''" as="xs:string"/>
    
    <xsl:template match="/">
        

        <xsl:result-document href="{$outputDir}/bnd.bnd" method="text" xml:space="preserve">
<xsl:if test="//cat:nextCatalog">Require-Bundle: <xsl:value-of select="string-join(//cat:nextCatalog/translate(@catalog,':','.'),',')"/></xsl:if>
<xsl:if test="//cat:uri[@px:script]">Service-Component: <xsl:value-of select="string-join(//cat:uri[@px:script]/concat('OSGI-INF/',substring-after(document(@uri,..)/*/@type,':'),'.xml'),',')"/>
Import-Package: org.daisy.pipeline.script</xsl:if>
        </xsl:result-document>

        <xsl:apply-templates mode="ds"/>
    </xsl:template>
    <xsl:template match="cat:uri[@px:script]" mode="ds">
        <xsl:variable name="id" select="substring-after(document(@uri,.)/*/@type,':')"/>
        <xsl:variable name="name" select="(document(@uri,.)//*[@pxd:role='name'])[1]"/>
        <xsl:variable name="descr" select="(document(@uri,.)//*[@pxd:role='desc'])[1]"/>
        <xsl:result-document href="{$outputDir}/OSGI-INF/{$id}.xml" method="xml">
            <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" name="{$id}">
                <scr:implementation class="org.daisy.pipeline.script.XProcScriptService"/>
                <scr:service>
                    <scr:provide interface="org.daisy.pipeline.script.XProcScriptService"/>
                </scr:service>
                <scr:property name="script.id" type="String" value="{$id}"/>
                <scr:property name="script.name" type="String" value="{$name}"/>
                <scr:property name="script.description" type="String" value="{$descr}"/>
                <scr:property name="script.url" type="String" value="{@name}"/>
            </scr:component>
        </xsl:result-document>
    </xsl:template>
    
</xsl:stylesheet>
