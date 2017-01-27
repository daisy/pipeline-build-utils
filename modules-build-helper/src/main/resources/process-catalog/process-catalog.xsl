<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xd="http://www.daisy.org/ns/pipeline/doc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                exclude-result-prefixes="#all" version="2.0">
    
    <xsl:param name="generatedSourcesDirectory" required="yes" as="xs:string"/>
    <xsl:param name="generatedResourcesDirectory" required="yes" as="xs:string"/>
    <xsl:param name="moduleVersion" required="yes" as="xs:string"/>
    
    <xsl:include href="../lib/extend-script.xsl"/>
    
    <xsl:template match="/*">
        <!-- extract data types -->
        <xsl:for-each select="cat:uri">
            <xsl:variable name="name" select="@name"/>
            <xsl:if test="doc-available(resolve-uri(@uri,base-uri(.)))">
                <xsl:variable name="data-types" as="element()*">
                    <xsl:apply-templates select="document(@uri)/p:*/p:option/p:pipeinfo/pxd:data-type/*" mode="data-type-xml"/>
                </xsl:variable>
                <xsl:for-each select="$data-types">
                    <xsl:variable name="url" as="xs:string" select="concat($name,'/data-types/',replace(@id,'^.*:',''),'.xml')"/>
                    <xsl:result-document href="{concat($generatedResourcesDirectory,'/data-types/',replace(@id,'^.*:',''),'.xml')}" method="xml">
                        <xsl:sequence select="."/>
                    </xsl:result-document>
                    <xsl:call-template name="data-type-class">
                        <xsl:with-param name="id" select="@id"/>
                        <xsl:with-param name="url" select="$url"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        <xsl:result-document href="{$generatedResourcesDirectory}/bnd.bnd" method="text"><c:data>
            <xsl:if test="cat:nextCatalog">
                <xsl:text>Require-Bundle: </xsl:text>
                <xsl:value-of select="string-join(cat:nextCatalog/translate(@catalog,':','.'),',')"/>
                <xsl:text>&#xa;</xsl:text>
            </xsl:if>
        </c:data></xsl:result-document>
        <xsl:result-document href="{$generatedResourcesDirectory}/META-INF/catalog.xml" method="xml">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
                <xsl:for-each select="cat:uri">
                    <xsl:variable name="name" select="@name"/>
                    <xsl:if test="doc-available(resolve-uri(@uri,base-uri(.)))">
                        <xsl:variable name="data-types" as="xs:string*">
                            <xsl:apply-templates select="document(@uri)/p:*/p:option/p:pipeinfo/pxd:data-type" mode="data-type-id"/>
                        </xsl:variable>
                        <xsl:for-each select="$data-types">
                            <cat:uri name="{concat($name,'/data-types/',replace(.,'^.*:',''),'.xml')}"
                                     uri="{concat('../data-types/',replace(.,'^.*:',''),'.xml')}"/>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each>
            </xsl:copy>
        </xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:script]" priority="1">
        <xsl:variable name="type" select="string(document(@uri,.)/*/@type)"/>
        <xsl:variable name="id" select="if (namespace-uri-for-prefix(substring-before($type,':'),document(@uri,.)/*)='http://www.daisy.org/ns/pipeline/xproc') then substring-after($type,':') else $type"/>
        <xsl:variable name="desc" as="element()?" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='desc'])[1]"/>
        <xsl:variable name="desc" select="if ($desc/@xml:space='preserve')
                                          then tokenize(string($desc),'&#xa;')[1]
                                          else normalize-space(string($desc))"/>
        <xsl:call-template name="script-class">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="desc" select="$desc"/>
            <xsl:with-param name="url" select="@name"/>
            <xsl:with-param name="version" select="$moduleVersion"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="script-class">
        <xsl:param name="id" as="xs:string" required="yes"/>
        <xsl:param name="desc" as="xs:string" required="yes"/>
        <xsl:param name="url" as="xs:string" required="yes"/>
        <xsl:param name="version" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('XProcScript_',replace($id,'[:-]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/script/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    name = "<xsl:value-of select="$id"/>",
    immediate = true,
    service = { XProcScriptService.class },
    property = {
        "script.id:String=<xsl:value-of select="$id"/>",
        "script.description:String=<xsl:value-of select="replace(replace($desc,'&quot;','\\&quot;'),'\\','\\\\')"/>",
        "script.url:String=<xsl:value-of select="$url"/>",
        "script.version:String=<xsl:value-of select="$version"/>"
    }
)
public class <xsl:value-of select="$className"/> extends XProcScriptService {
    
    @Activate
    @Override
    public void activate(Map&lt;?,?&gt; properties) {
        super.activate(properties);
    }
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:extends]">
        <xsl:variable name="generated-href" select="f:generated-href(@uri)"/>
        <xsl:result-document href="{resolve-uri($generated-href,concat($generatedResourcesDirectory,'/META-INF/catalog.xml'))}" method="xml">
            <xsl:variable name="doc">
                <xsl:call-template name="extend-script">
                    <xsl:with-param name="script-uri" select="resolve-uri(@uri,base-uri(.))"/>
                    <xsl:with-param name="extends-uri" select="resolve-uri(@px:extends,base-uri(.))"/>
                    <xsl:with-param name="catalog-xml" select="/*"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:data-type">
                    <xsl:apply-templates select="$doc" mode="finalize-script"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$doc"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:result-document>
        <xsl:copy>
            <xsl:apply-templates select="@* except @uri" mode="#current"/>
            <xsl:attribute name="uri" select="$generated-href"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="cat:uri[not(@px:extends)]">
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:choose>
            <xsl:when test="doc-available($uri)">
                <xsl:variable name="doc" select="document($uri)"/>
                <xsl:choose>
                    <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:data-type">
                        <xsl:variable name="generated-href" select="f:generated-href(@uri)"/>
                        <xsl:result-document href="{resolve-uri($generated-href,concat($generatedResourcesDirectory,'/META-INF/catalog.xml'))}" method="xml">
                            <xsl:apply-templates select="$doc" mode="finalize-script"/>
                        </xsl:result-document>
                        <xsl:copy>
                            <xsl:apply-templates select="@* except @uri" mode="#current"/>
                            <xsl:attribute name="uri" select="$generated-href"/>
                            <xsl:apply-templates mode="#current"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="f:generated-href">
        <xsl:param name="uri" as="xs:string"/>
        <xsl:value-of select="replace($uri,'^(.*/)?([^/]+)$','$1__processed__$2')"/>
    </xsl:function>
    
    <xsl:template match="cat:uri[@px:data-type]" priority="1">
        <xsl:variable name="id" select="string(document(@uri,.)/*/@id)"/>
        <xsl:call-template name="data-type-class">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="url" select="@name"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="data-type-class">
        <xsl:param name="id" as="xs:string" required="yes"/>
        <xsl:param name="url" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('Datatype_',replace($id,'[:-]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/datatypes/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.datatypes.impl;

import java.util.Map;
import javax.xml.transform.URIResolver;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.UrlBasedDatatypeService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "<xsl:value-of select="$id"/>",
    immediate = true,
    service = { DatatypeService.class },
    property = {
        "data-type.id:String=<xsl:value-of select="$id"/>",
        "data-type.url:String=<xsl:value-of select="$url"/>"
    }
)
public class <xsl:value-of select="$className"/> extends UrlBasedDatatypeService {
    
    @Activate
    @Override
    public void activate(Map&lt;?,?&gt; properties) {
        super.activate(properties);
    }
    
    @Reference(
        name = "URIResolver",
        unbind = "-",
        service = URIResolver.class,
        cardinality = ReferenceCardinality.MANDATORY,
        policy = ReferencePolicy.STATIC
    )
    @Override
    public void setUriResolver(URIResolver resolver) {
        super.setUriResolver(resolver);
    }
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri/@px:script|
                         cat:uri/@px:extends|
                         cat:uri/@px:data-type"/>
    
    <xsl:template match="/*/p:option[p:pipeinfo/pxd:data-type]" mode="finalize-script">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:apply-templates select="p:pipeinfo/pxd:data-type" mode="data-type-attribute"/>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo" mode="finalize-script">
        <xsl:if test="* except pxd:data-type">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="finalize-script"/>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="data-type-attribute">
        <xsl:attribute name="pxd:data-type">
            <xsl:apply-templates select="." mode="data-type-id"/>
        </xsl:attribute>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type/*" mode="data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id">
                    <xsl:apply-templates select="parent::*" mode="data-type-id"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="data-type-id" as="xs:string">
        <xsl:sequence select="(@id,child::*/@id,concat(/*/@type,'-',parent::*/parent::*/@name))[1]"/>
    </xsl:template>
    
    <xsl:template match="@*|node()" mode="#default data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
