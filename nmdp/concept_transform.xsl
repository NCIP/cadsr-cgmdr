<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:ns1="urn:nci:concept-entry"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:mesh="http://www.nlm.nih.gov/mesh/2006#" >
    
    <xsl:output indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="/"> 
        <xsl:variable name="buffer">
            <!--<result-set xmlns="http://cancergrid.org/schema/result-set">-->
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:mesh="http://www.nlm.nih.gov/mesh/2006#" xml:lang="en">

                <xsl:apply-templates select="//ns1:conceptEntry"/>
            </rdf:RDF>
        </xsl:variable>
        <xsl:copy-of select="$buffer"/>
    </xsl:template>
    
    <xsl:template match="ns1:conceptEntry">
        <!--<concept xmlns="http://cancergrid.org/schema/result-set">-->
		<rdf:Description>

			<xsl:attribute name="rdf:about">US-NMDP-LEXBIG-<xsl:value-of select="normalize-space(ns1:CUI)"/></xsl:attribute>

			<xsl:attribute name="skos:prefLabel"><xsl:value-of select='normalize-space(ns1:Concept)'/></xsl:attribute> 
			<xsl:apply-templates select="ns1:ConcantenationFormula"/>
        	<xsl:apply-templates select="ns1:Definition"/>
       </rdf:Description>
	</xsl:template>    
    
    <xsl:template match="ns1:ConcantenationFormula">
        <skos:altLabel>
        	<xsl:value-of select="../ns1:ConcantenationFormula"/>
        </skos:altLabel>
    </xsl:template>
    <xsl:template match="ns1:Definition">
        <skos:scopeNote>
        	<xsl:value-of select="../ns1:Definition"/>
        </skos:scopeNote>
    </xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="conceptTransform.xml" htmlbaseurl="" outputurl="terminologyImport.rdf" processortype="saxon8" useresolver="yes" profilemode="0" profiledepth=""
		          profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal"
		          customvalidator="">
			<advancedProp name="sInitialMode" value=""/>
			<advancedProp name="bXsltOneIsOkay" value="true"/>
			<advancedProp name="bSchemaAware" value="true"/>
			<advancedProp name="bXml11" value="false"/>
			<advancedProp name="iValidation" value="0"/>
			<advancedProp name="bExtensions" value="true"/>
			<advancedProp name="iWhitespace" value="0"/>
			<advancedProp name="sInitialTemplate" value=""/>
			<advancedProp name="bTinyTree" value="true"/>
			<advancedProp name="bWarnings" value="true"/>
			<advancedProp name="bUseDTD" value="false"/>
			<advancedProp name="iErrorHandling" value="fatal"/>
		</scenario>
	</scenarios>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no">
			<SourceSchema srcSchemaPath="conceptTransform.xml" srcSchemaRoot="ns1:conceptEntryList" AssociatedInstance="" loaderFunction="document" loaderFunctionUsesURI="no"/>
		</MapperInfo>
		<MapperBlockPosition>
			<template match="/"></template>
			<template match="ns1:ConcantenationFormula"></template>
			<template match="ns1:conceptEntry">
				<block path="rdf:Description/xsl:attribute/xsl:value-of" x="293" y="54"/>
				<block path="rdf:Description/xsl:attribute[1]/xsl:value-of" x="253" y="72"/>
				<block path="rdf:Description/xsl:apply-templates" x="253" y="36"/>
			</template>
		</MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
-->