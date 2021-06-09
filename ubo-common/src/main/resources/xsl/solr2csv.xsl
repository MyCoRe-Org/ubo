<?xml version="1.0"?>

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">

  <xsl:variable name="fields"
                select="('id','subject','oa','genre','host_genre','person_aut','person_edt','title','id_doi','id_scopus','id_pubmed','id_urn','id_duepublico','id_duepublico2','host_title','series','id_issn','id_isbn','shelfmark','year','volume','issue','pages','place','publisher')"/>

  <xsl:variable name="col_seperator" select="';'"/>
  <xsl:variable name="line_seperator" select="'&#xa;'"/>
  <xsl:variable name="str_wrap" select="'&quot;'"/>
  <xsl:variable name="regex" select="concat('([',$str_wrap,'])')"/>

  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

  <xsl:template match="/add">
    <xsl:message>
      <xsl:value-of select="replace('Dies &quot;ist&quot; ein ; test ; ',$regex, '\\$1')"/>
    </xsl:message>
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

  <xsl:template match="field">
    <xsl:value-of select="replace(string(text()), $regex, '$1$1')"/>
    <xsl:if test="position()!=last()">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
