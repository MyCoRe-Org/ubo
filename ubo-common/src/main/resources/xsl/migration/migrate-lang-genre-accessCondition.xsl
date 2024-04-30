<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mods xlink xsl">

  <xsl:param name="MCR.baseurl"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

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

  <xsl:template match="mods:accessCondition[@type = 'use and reproduction'][@classID = 'licenses']">
    <mods:accessCondition type="use and reproduction"
                          xlink:href="{$MCR.baseurl}classifications/licenses#{.}"/>
  </xsl:template>

  <xsl:template match="mods:languageTerm[@authority = 'rfc4646'][@type = 'code']">
    <mods:languageTerm authority="rfc5646" type="code">
      <xsl:choose>
        <xsl:when test="text() = 'hr'">
          <xsl:value-of select="'sh-hr'"/>
        </xsl:when>
        <xsl:when test="text() = 'id'">
          <xsl:value-of select="'ms-id'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </mods:languageTerm>
  </xsl:template>

</xsl:stylesheet>
