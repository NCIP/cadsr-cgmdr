<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:q="http://cancergrid.org/schema/query" xmlns:xlink="http://www.w3.org/1999/xlink" version="2.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:value-of select="concat(/q:query/q:serviceUrl, '?query=DescLogicConcept&amp;DescLogicConcept[@code=', replace(/q:query/q:term, ' ', '%20'),']')"/>
    </xsl:template>
</xsl:stylesheet>