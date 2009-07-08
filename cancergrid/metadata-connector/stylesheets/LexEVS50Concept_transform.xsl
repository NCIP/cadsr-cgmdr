<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all">
    <xsl:output indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:strip-space elements="*"/>
    
    <xsl:template match="/">
        <xsl:variable name="buffer">
            <xsl:apply-templates select="*|text()|comment()"/>
        </xsl:variable>
        <xsl:copy-of select="$buffer" copy-namespaces="no"/>
    </xsl:template>
    
    <xsl:template match="*|@*|comment()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="Concept">
        <!--<concept xmlns="http://cancergrid.org/schema/result-set">-->
        <concept>
            <names>
                <id>US-NCICB-CACORE-EVS-DESCLOGICCONCEPT-<xsl:value-of select="_entityCode"/></id>
                <preferred><xsl:value-of select="_presentationList/Presentation[_isPreferred='true']/_value/Text/_content"/></preferred>
                
                <xsl:apply-templates select="_presentationList"/>            
            </names>
            <definition><xsl:value-of select="_definitionList/Definition[_isPreferred='true']/_value/Text/_content"/></definition>
            <xsl:apply-templates select="_definitionList"/>
            <xsl:apply-templates select="_propertyList"/>
        </concept>
    </xsl:template>
    
    <xsl:template match="_presentationList">
        <all-names>
            <xsl:for-each select="Presentation[_isPreferred='false']">
                <xsl:sort select="_propertyName"/>
                <xsl:apply-templates select="."/>
            </xsl:for-each>    
        </all-names>
    </xsl:template>
    
    <xsl:template match="_propertyList">
        <!--<properties  xmlns="http://cancergrid.org/schema/result-set">-->
        <properties>
            <xsl:for-each select="Property">
                <xsl:sort select="_propertyName"/>
               <!-- <xsl:if test="not(contains(Name, 'DEFINITION')) and not(contains(Name, 'Preferred_Name'))">-->
                    <xsl:apply-templates select="."/>                    
                <!--</xsl:if> -->
            </xsl:for-each>
        </properties>
    </xsl:template>
    
    <xsl:template match="_definitionList">
        <all-definitions>
            <xsl:for-each select="Definition[_isPreferred='false']">
                <xsl:sort select="_propertyName"/>
                <xsl:apply-templates select="."/>
            </xsl:for-each>
        </all-definitions>
    </xsl:template>
    
    
    <xsl:template match="Property">
        <!--<property  xmlns="http://cancergrid.org/schema/result-set">-->
        <property>
            <name><xsl:value-of select="_propertyName"/></name>
            <value><xsl:value-of select="_value/Text/_content"/></value>
        </property>
    </xsl:template>
    
    <xsl:template match="Presentation">
        <name>
            <type><xsl:value-of select="_propertyName"/></type>
            <value><xsl:value-of select="_value/Text/_content"/></value>
            <source><xsl:value-of select="_sourceList/Source/_content"/></source>
        </name>
    </xsl:template>
    
    <xsl:template match="Definition">
        <definition>
            <type><xsl:value-of select="_propertyName"/></type>
            <value><xsl:value-of select="_value/Text/_content"/></value>
            <source><xsl:value-of select="_sourceList/Source/_content"/></source>
        </definition>
    </xsl:template>
    
    <!-- Filter out extra nodes 
        <xsl:template match=""/>-->
</xsl:stylesheet>
