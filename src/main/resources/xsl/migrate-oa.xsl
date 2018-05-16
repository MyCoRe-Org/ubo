<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xsl mods">
  
  <xsl:include href="copynodes.xsl" />
  
  <xsl:variable name="authorityURI">https://bibliographie.ub.uni-due.de/classifications/oa</xsl:variable>
  
  <xsl:template match="mods:note">
    <xsl:copy-of select="." />
    <xsl:if test="starts-with(.,'OA')">
      <xsl:variable name="category">
        <xsl:choose>
          <xsl:when test="contains(.,'OA grün')">green</xsl:when>
          <xsl:when test="contains(.,'OA gold')">gold</xsl:when>
          <xsl:when test="contains(.,'OA hybrid')">hybrid</xsl:when>
          <xsl:when test="contains(.,'OA embargo')">embargo</xsl:when>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="string-length($category) &gt; 0">
        <mods:classification valueURI="{$authorityURI}#{$category}" authorityURI="{$authorityURI}" />
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
