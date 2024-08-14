<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xed="http://www.mycore.de/xeditor"
  exclude-result-prefixes="xsl">

  <xsl:include href="copynodes.xsl" />
  
  <xsl:param name="classID" select="'fachreferate'" />
  <xsl:param name="required" select="'true'" />
  <xsl:param name="max" select="'5'" />
  
  <xsl:template match="xed:validate">
    <xsl:if test="$required='true'">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:variable name="classIDMarker" select="'__classID__'" />
  
  <xsl:template match="@*[contains(.,$classIDMarker)]" priority="1">
    <xsl:attribute name="{name()}">
      <xsl:value-of select="substring-before(.,$classIDMarker)" />
      <xsl:value-of select="$classID" />
      <xsl:value-of select="substring-after(.,$classIDMarker)" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="xed:repeat/@max">
    <xsl:attribute name="max">
      <xsl:value-of select="$max" />
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="label/@class">
    <xsl:attribute name="class">
      <xsl:value-of select="." />
      <xsl:if test="$required='true'">
        <xsl:text> ubo-mandatory</xsl:text>
      </xsl:if>
    </xsl:attribute>
  </xsl:template>
  
</xsl:stylesheet>
