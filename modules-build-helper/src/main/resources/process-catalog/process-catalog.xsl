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
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all" version="2.0">
    
    <xsl:param name="outputDir" required="no" select="''" as="xs:string"/>
    <xsl:param name="version" required="yes"  as="xs:string"/>
    
    <xsl:include href="../lib/uri-functions.xsl"/>
    <xsl:include href="../lib/extend-script.xsl"/>
    
    <xsl:template match="/*">
        <!-- extract data types -->
        <xsl:for-each select="cat:uri">
            <xsl:if test="doc-available(resolve-uri(@uri,base-uri(.)))">
                <xsl:variable name="data-types" as="element()*">
                    <xsl:apply-templates select="document(@uri)/p:*/p:option/p:pipeinfo/pxd:type/*" mode="data-type-xml"/>
                </xsl:variable>
                <xsl:for-each select="$data-types">
                    <xsl:variable name="path" select="concat('/data-types/',replace(@id,'^.*:',''),'.xml')"/>
                    <xsl:result-document href="{concat($outputDir,$path)}" method="xml">
                        <xsl:sequence select="."/>
                    </xsl:result-document>
                    <xsl:result-document href="{concat($outputDir,'/OSGI-INF/data-types/',replace(@id,'^.*:',''),'.xml')}" method="xml">
                        <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{@id}">
                            <scr:implementation class="org.daisy.pipeline.datatypes.UrlBasedDatatypeService"/>
                            <scr:service>
                                <scr:provide interface="org.daisy.pipeline.datatypes.DatatypeService"/>
                            </scr:service>
                            <scr:property name="data-type.id" type="String" value="{@id}"/>
                            <scr:property name="data-type.url" type="String" value="{$path}"/>
                        </scr:component>
                    </xsl:result-document>
                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        <xsl:variable name="data-types" as="xs:string*">
            <xsl:for-each select="cat:uri">
                <xsl:if test="doc-available(resolve-uri(@uri,base-uri(.)))">
                    <xsl:apply-templates select="document(@uri)/p:*/p:option/p:pipeinfo/pxd:type" mode="data-type-id"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:result-document href="{$outputDir}/bnd.bnd" method="text" xml:space="preserve"><c:data>
<xsl:if test="//cat:nextCatalog">Require-Bundle: <xsl:value-of select="string-join(//cat:nextCatalog/translate(@catalog,':','.'),',')"/></xsl:if>
<xsl:variable name="service-components" as="xs:string*"
              select="(//cat:uri[@px:script]/concat('OSGI-INF/',replace(document(@uri,..)/*/@type,'^.*:',''),'.xml'),
                       //cat:uri[@px:data-type]/concat('OSGI-INF/',replace(document(@uri,..)/*/@id,'^.*:',''),'.xml'),
                       for $id in $data-types return concat('OSGI-INF/data-types/',replace($id,'^.*:',''),'.xml'))"/>
<xsl:if test="exists($service-components)">
        Service-Component: <xsl:value-of select="string-join($service-components,',')"/></xsl:if>
<!-- my xslt skills are long forgotten, this sucks-->
<xsl:if test="(//cat:uri[@px:data-type] or exists($data-types)) and not(//cat:uri[@px:script])">
        Import-Package: org.daisy.pipeline.datatypes,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and not(//cat:uri[@px:data-type] or exists($data-types))">
        Import-Package: org.daisy.pipeline.script,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and (//cat:uri[@px:data-type] or exists($data-types))">
        Import-Package: org.daisy.pipeline.script,org.daisy.pipeline.datatypes,*</xsl:if>
        </c:data></xsl:result-document>
        <xsl:variable name="catalog" as="node()*">
            <xsl:apply-templates mode="ds"/>
        </xsl:variable>
        <xsl:if test="$catalog/self::*">
            <xsl:result-document href="{$outputDir}/META-INF/catalog.xml" method="xml">
                <xsl:copy>
                    <xsl:apply-templates select="@*" mode="ds"/>
                    <xsl:sequence select="$catalog"/>
                </xsl:copy>
            </xsl:result-document>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:script]" mode="ds" priority="1">
        <xsl:variable name="type" select="string(document(@uri,.)/*/@type)"/>
        <xsl:variable name="id" select="if (namespace-uri-for-prefix(substring-before($type,':'),document(@uri,.)/*)='http://www.daisy.org/ns/pipeline/xproc') then substring-after($type,':') else $type"/>
        <xsl:variable name="name" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='name'])[1]"/>
        <xsl:variable name="descr" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='desc'])[1]"/>
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:result-document href="{$outputDir}/OSGI-INF/{replace($id,'^.*:','')}.xml" method="xml">
            <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{$id}">
                <scr:implementation class="org.daisy.pipeline.script.XProcScriptService"/>
                <scr:service>
                    <scr:provide interface="org.daisy.pipeline.script.XProcScriptService"/>
                </scr:service>
                <scr:property name="script.id" type="String" value="{$id}"/>
                <scr:property name="script.name" type="String" value="{$name}"/>
                <scr:property name="script.description" type="String" value="{$descr}"/>
                <scr:property name="script.url" type="String" value="{$path}"/>
                <scr:property name="script.version" type="String" value="{$version}"/>
            </scr:component>
        </xsl:result-document>
        <xsl:if test="@name">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:extends]" mode="ds">
        <xsl:variable name="generated-href" select="f:generated-href(@uri)"/>
        <xsl:result-document href="{resolve-uri($generated-href,concat($outputDir,'/META-INF/catalog.xml'))}" method="xml">
            <xsl:variable name="doc">
                <xsl:call-template name="extend-script">
                    <xsl:with-param name="script-uri" select="resolve-uri(@uri,base-uri(.))"/>
                    <xsl:with-param name="extends-uri" select="resolve-uri(@px:extends,base-uri(.))"/>
                    <xsl:with-param name="catalog-xml" select="/*"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:type">
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
    
    <xsl:template match="cat:uri[not(@px:extends)]" mode="ds">
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:choose>
            <xsl:when test="doc-available($uri)">
                <xsl:variable name="doc" select="document($uri)"/>
                <xsl:choose>
                    <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:type">
                        <xsl:variable name="generated-href" select="f:generated-href(@uri)"/>
                        <xsl:result-document href="{resolve-uri($generated-href,concat($outputDir,'/META-INF/catalog.xml'))}" method="xml">
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
    
    <xsl:template match="cat:uri[@px:data-type]" mode="ds" priority="1">
        <xsl:variable name="id" select="string(document(@uri,.)/*/@id)"/>
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:result-document href="{$outputDir}/OSGI-INF/{replace($id,'^.*:','')}.xml" method="xml">
            <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{$id}">
                <scr:implementation class="org.daisy.pipeline.datatypes.UrlBasedDatatypeService"/>
                <scr:service>
                    <scr:provide interface="org.daisy.pipeline.datatypes.DatatypeService"/>
                </scr:service>
                <scr:property name="data-type.id" type="String" value="{$id}"/>
                <scr:property name="data-type.url" type="String" value="{$path}"/>
            </scr:component>
        </xsl:result-document>
        <xsl:if test="@name">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="cat:uri/@px:script|
                         cat:uri/@px:extends|
                         cat:uri/@px:data-type"
                  mode="ds"/>
    
    <xsl:template match="/*/p:option[p:pipeinfo/pxd:type]" mode="finalize-script">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:apply-templates select="p:pipeinfo/pxd:type" mode="data-type-attribute"/>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo" mode="finalize-script">
        <xsl:if test="* except pxd:type">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="finalize-script"/>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="data-type-attribute">
        <xsl:attribute name="pxd:type">
            <xsl:apply-templates select="." mode="data-type-id"/>
        </xsl:attribute>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type/*" mode="data-type-xml">
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
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="data-type-id" as="xs:string">
        <xsl:sequence select="(@id,child::*/@id,concat(/*/@type,'-',parent::*/parent::*/@name))[1]"/>
    </xsl:template>
    
    <xsl:template match="@*|node()" mode="ds data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
