<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl mods">
  <xsl:include href="copynodes.xsl"/>

  <xsl:param name="MCR.baseurl"/>

  <xsl:template match="mods:genre">
    <xsl:copy>
      <xsl:attribute name="authorityURI">
        <xsl:value-of select="concat($MCR.baseurl, 'classifications/ubogenre')"/>
      </xsl:attribute>
      <xsl:attribute name="valueURI">
        <xsl:value-of select="concat($MCR.baseurl, 'classifications/ubogenre#', .)"/>
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
