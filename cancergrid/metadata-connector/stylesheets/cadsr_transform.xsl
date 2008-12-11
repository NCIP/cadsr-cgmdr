<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	<xsl:output indent="yes" omit-xml-declaration="yes"/>
	<xsl:strip-space elements="*"/>
	<xsl:template match="/">
		<xsl:variable name="buffer">
			<xsl:apply-templates select="*|text()|comment()"/>
		</xsl:variable>
		<xsl:copy-of select="$buffer/*" copy-namespaces="no"/>
	</xsl:template>
	<xsl:template match="*|@*|comment()">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|comment()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="DataElement">
		<!--<data-element xmlns="http://cancergrid.org/schema/result-set">-->
		<data-element>
			<names>
				<id>US-NCICB-CACORE-CADSR-<xsl:value-of select="publicID"/>-<xsl:value-of select="version"/>
				</id>
				<preferred>
					<xsl:value-of select="longName"/>
				</preferred>
				<all-names>
					<name>
						<xsl:value-of select="preferredName"/>
					</name>
					<name>
						<xsl:value-of select="longName"/>
					</name>
				</all-names>
			</names>
			<definition>
				<xsl:value-of select="preferredDefinition"/>
			</definition>
			<workflow-status>
				<xsl:value-of select="workflowStatusName"/>
			</workflow-status>
			<xsl:apply-templates select="valueDomain"/>
		</data-element>
	</xsl:template>
	<xsl:template match="valueDomain">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="NonenumeratedValueDomain">
		<!--<values xmlns="http://cancergrid.org/schema/result-set">-->
		<values>
			<non-enumerated>
				<data-type>
					<xsl:value-of select="datatypeName"/>
				</data-type>
				<units/>
			</non-enumerated>
		</values>
	</xsl:template>
	<xsl:template match="EnumeratedValueDomain">
		<!--<values xmlns="http://cancergrid.org/schema/result-set">-->
		<values>
			<enumerated>
				<xsl:apply-templates select="valueDomainPermissibleValueCollection/ValueDomainPermissibleValue"/>
			</enumerated>
		</values>
	</xsl:template>
	<xsl:template match="ValueDomainPermissibleValue">
		<!--<valid-value xmlns="http://cancergrid.org/schema/result-set">-->
		<valid-value>
			<code>
				<xsl:value-of select="permissibleValue/PermissibleValue/value"/>
			</code>
			<meaning>
				<xsl:value-of select="permissibleValue/PermissibleValue/valueMeaning/ValueMeaning/shortMeaning"/>
			</meaning>
			<evsconcept>
				<xsl:value-of select="concept/Concept/preferredName"/>
			</evsconcept>
		</valid-value>
	</xsl:template>
	<!-- Filter out extra nodes -->
	<xsl:template match="questionCollection|workflowStatusDescription|unresolvedIssue|registrationStatus|publicID|origin|modifiedBy|latestVersionIndicator|endDate|deletedIndicator|dateModified|dateCreated|createdBy|changeNote|beginDate|version|prequestionCollection|dataElementDerivationCollection|parentDataElementRelationshipsCollection|dataElementConcept|derivedDataElement|childDataElementRelationshipsCollection|context|administeredComponentClassSchemeItemCollection|designationCollection|referenceDocumentCollection|administeredComponentContactCollection|definitionCollection|validValueCollection|parentValueDomainRelationshipCollection|dataElementCollection|childValueDomainRelationshipCollection|conceptDerivationRule|represention|conceptualDomain"/>
</xsl:stylesheet>
