<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xsl mods">
  
  <xsl:include href="copynodes.xsl" />
  
  <xsl:variable name="tag">[Rezension]</xsl:variable>
  <xsl:variable name="tag2">Rezension:</xsl:variable>
  <xsl:variable name="tag3">Rezension zu:</xsl:variable>
  
  <xsl:template match="mods:genre[@type='intern']">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:choose>
        <xsl:when test="../mods:titleInfo/*[contains(text(),$tag) or starts-with(text(),$tag2) or starts-with(text(),$tag3)]">
          <xsl:text>review</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="text()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="mods:subTitle[text()=$tag]" />
  
  <xsl:template match="mods:title[contains(text(),$tag)]">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:value-of select="normalize-space( concat(substring-before(text(),$tag), ' ', substring-after(text(),$tag) ))" />
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
