<?xml version="1.0" encoding="UTF-8"?>

<!-- Outputs a category and its parents -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n"
>

<xsl:param name="CurrentLang" />
<xsl:param name="DefaultLang" />

<xsl:template name="output.category">
  <xsl:param name="classID" />
  <xsl:param name="categID" />
  
  <xsl:if test="string-length($categID) &gt; 0">
    <xsl:apply-templates select="document(concat('classification:editor:0:parents:',$classID,':',encoder:encode($categID,'UTF-8')))/items/item" mode="output.category" />
  </xsl:if>  
</xsl:template>

<xsl:template match="item" mode="output.category">
  <xsl:choose>
    <xsl:when test="label[lang($CurrentLang)]">
      <xsl:value-of select="label[lang($CurrentLang)]"/>
    </xsl:when>
    <xsl:when test="label[lang($DefaultLang)]">
      <xsl:value-of select="label[lang($DefaultLang)]"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="label[1]"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="item">
    <xsl:text> &#187; </xsl:text>
    <xsl:apply-templates select="item" mode="output.category" />
  </xsl:if>
</xsl:template>

</xsl:stylesheet> 
