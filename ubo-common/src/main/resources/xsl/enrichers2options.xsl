<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">
  <xsl:output method="html"/>

  <xsl:template match="/">
    <result>
      <xsl:apply-templates select="enrichmentDebugger/enrichers/enricher">
        <xsl:sort select="@id"/>
      </xsl:apply-templates>
    </result>
  </xsl:template>

  <xsl:template match="enricher">
    <option value="{@id}" title="{@id}">
      <xsl:value-of select="text()"/>
    </option>
  </xsl:template>
</xsl:stylesheet>
