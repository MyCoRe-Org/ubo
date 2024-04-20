<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl mods">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:genre">
    <xsl:copy>
      <xsl:attribute name="authorityURI">
        <xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre')"/>
      </xsl:attribute>
      <xsl:attribute name="valueURI">
        <xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#', .)"/>
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>