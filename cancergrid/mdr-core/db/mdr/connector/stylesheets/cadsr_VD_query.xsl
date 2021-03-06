<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:q="http://cancergrid.org/schema/query">
    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:value-of select="concat(/q:query/q:serviceUrl, '?query=ValueDomain&amp;ValueDomain[longName=', replace(/q:query/q:term, ' ', '%20'),']&amp;startIndex=',/q:query/q:startIndex,'&amp;pageSize=',/q:query/q:numResults)"/>
    </xsl:template>

</xsl:stylesheet>
