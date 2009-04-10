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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<xsl:apply-templates select="valueDomain"/>
		</data-element>
	</xsl:template>
	<xsl:template match="valueDomain">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="NonenumeratedValueDomain">
		<!--<values xmlns="http://cancergrid.org/schema/result-set">-->
		<values>
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
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<enumerated>
				<xsl:apply-templates select="valueDomainPermissibleValueCollection/ValueDomainPermissibleValue/permissibleValue/PermissibleValue"/>
			</enumerated>
		</values>
	</xsl:template>
	
	<xsl:template match="PermissibleValue">
		<!--<valid-value xmlns="http://cancergrid.org/schema/result-set">-->
		<valid-value>
			<code>
				<xsl:value-of select="value"/>
			</code>
			<meaning>
				<xsl:value-of select="valueMeaning/ValueMeaning/shortMeaning"/>
			</meaning>
			<xsl:apply-templates select="valueMeaning/ValueMeaning/conceptDerivationRule/ConceptDerivationRule/componentConceptCollection"/>
		</valid-value>
	</xsl:template>
    	
	<xsl:template match="ObjectClass">
		<!--<data-element xmlns="http://cancergrid.org/schema/result-set">-->
		<object-class>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<xsl:apply-templates select="conceptDerivationRule/ConceptDerivationRule/componentConceptCollection"/>
		</object-class>
	</xsl:template>
	
	<xsl:template match="Property">
		<!--<data-element xmlns="http://cancergrid.org/schema/result-set">-->
		<property-expanded>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<xsl:apply-templates select="conceptDerivationRule/ConceptDerivationRule/componentConceptCollection"/>
		</property-expanded>
	</xsl:template>
	
	<xsl:template match="ConceptualDomain">
		<!--<data-element xmlns="http://cancergrid.org/schema/result-set">-->
		<conceptual-domain>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<context>
				<xsl:value-of select="context/Context/name"/>
			</context>
			<xsl:apply-templates select="conceptDerivationRule/ConceptDerivationRule/componentConceptCollection"/>
		</conceptual-domain>
	</xsl:template>
	
	<xsl:template match="Representation">
		<!--<data-element xmlns="http://cancergrid.org/schema/result-set">-->
		<representation-term>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<xsl:apply-templates select="conceptDerivationRule/ConceptDerivationRule/componentConceptCollection"/>
		</representation-term>
	</xsl:template>
	
<xsl:template match="componentConceptCollection">
	<conceptCollection>
		<xsl:apply-templates select="ComponentConcept"/>
	</conceptCollection>
</xsl:template>	
	
	<xsl:template match="ComponentConcept">
		<evsconcept>
			<displayOrder>
				<xsl:value-of select="displayOrder"/>
			</displayOrder>
			<name>
				<xsl:value-of select="concept/Concept/preferredName"/>
			</name>
		</evsconcept>
	</xsl:template>
	
	
	<xsl:template match="DataElementConcept">
		<data-element-concept>
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
			<registration-status>
				<xsl:value-of select="registrationStatus"/>
			</registration-status>
			<xsl:apply-templates select="property/Property"/>

			<xsl:apply-templates select="objectClass/ObjectClass"/>

		</data-element-concept>
	</xsl:template>
	
	<!-- Filter out extra nodes -->
	<xsl:template match="questionCollection|workflowStatusDescription|unresolvedIssue|registrationStatus|publicID|concept|origin|modifiedBy|latestVersionIndicator|endDate|deletedIndicator|dateModified|dateCreated|createdBy|changeNote|beginDate|version|prequestionCollection|dataElementDerivationCollection|parentDataElementRelationshipsCollection|dataElementConcept|derivedDataElement|childDataElementRelationshipsCollection|context|administeredComponentClassSchemeItemCollection|designationCollection|referenceDocumentCollection|administeredComponentContactCollection|definitionCollection|validValueCollection|parentValueDomainRelationshipCollection|dataElementCollection|childValueDomainRelationshipCollection|parentDataElementConceptRelationshipCollection|childDataElementConceptRelationshipCollection|registrations|conceptualDomain"/>
</xsl:stylesheet>
