<?xml version="1.0"?>

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">

  <xsl:param name="UBO.Export.Fields" />
  
  <xsl:variable name="fields"
                select="tokenize($UBO.Export.Fields, ',')" />

  <xsl:variable name="col_seperator" select="';'"/>
  <xsl:variable name="line_seperator" select="'&#xa;'"/>
  <xsl:variable name="str_wrap" select="'&quot;'"/>
  <xsl:variable name="regex" select="concat('([',$str_wrap,'])')"/>

  <xsl:output method="text" omit-xml-declaration="yes" indent="no" media-type="text/csv"/>

  <xsl:template match="/response">
    <xsl:apply-templates select="result"/>
  </xsl:template>

  <xsl:template match="/add|result">
    <xsl:message>Match add or response</xsl:message>
    <xsl:for-each select="$fields">
      <xsl:value-of select="$str_wrap"/>
      <xsl:value-of select="."/>
      <xsl:value-of select="$str_wrap"/>
      <xsl:value-of select="$col_seperator"/>
    </xsl:for-each>
    <xsl:value-of select="$line_seperator"/>
    <xsl:for-each select="doc">
      <xsl:variable name="doc" select="."/>
      <xsl:for-each select="$fields">
        <xsl:variable name="fn" select="."/>
        <xsl:value-of select="$str_wrap"/>
        <xsl:apply-templates select="$doc/*[@name=$fn]"/>
        <xsl:value-of select="$str_wrap"/>
        <xsl:value-of select="$col_seperator"/>
      </xsl:for-each>
      <xsl:value-of select="$line_seperator"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="arr">
    <xsl:apply-templates select="str"/>
  </xsl:template>

  <xsl:template match="field|str">
    <xsl:value-of select="replace(string(text()), $regex, '$1$1')"/>
    <xsl:if test="position()!=last()">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
