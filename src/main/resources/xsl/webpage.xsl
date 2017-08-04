<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 35435 $ $Date: 2016-05-24 10:54:17 +0200 (Di, 24 Mai 2016) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  exclude-result-prefixes="xsl i18n">

  <xsl:include href="layout.xsl" />

  <xsl:variable name="PageID" select="/webpage/@id" />
  <xsl:variable name="page.title">
    <xsl:for-each select="/webpage">
      <xsl:call-template name="build.title" />
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="pageLastModified" select="substring-before(substring-after(/udepage/@lastModified,' '),' ')" />

  <xsl:variable name="breadcrumb.extensions">
    <item label="{$page.title}" />
  </xsl:variable>

  <xsl:template name="build.title">
    <xsl:choose>
      <xsl:when test="title[lang($CurrentLang)]">
        <xsl:value-of select="title[lang($CurrentLang)]" />
      </xsl:when>
      <xsl:when test="title[lang($DefaultLang)]">
        <xsl:value-of select="title[lang($DefaultLang)]" />
      </xsl:when>
      <xsl:when test="title[1]">
        <xsl:value-of select="title[1]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@title" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- copy -->

  <xsl:template match="*">
    <xsl:copy>
      <xsl:for-each select="@*">
        <xsl:copy-of select="." />
      </xsl:for-each>
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>

  <!-- webpage -->

  <xsl:template match="webpage">
    <xsl:apply-templates select="section|upload|*[not(name()='sidebar')]" />
  </xsl:template>
  
  <!-- Section -->

  <xsl:template match="section">
    <xsl:choose>
      <xsl:when test="lang($CurrentLang)">
        <xsl:call-template name="output.section" />
      </xsl:when>
      <xsl:when test="string-length(@xml:lang) = 0">
        <xsl:call-template name="output.section" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="output.section">
    <xsl:variable name="class">
      <xsl:choose>
        <xsl:when test="@class">
          <xsl:value-of select="@class"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'highlight0'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <article class="{$class}">
      <xsl:copy-of select="@style" />
      <xsl:if test="@title|title">
        <hgroup>
          <h2>
            <xsl:call-template name="build.title" />
          </h2>
        </hgroup>
      </xsl:if>
      <xsl:apply-templates select="* | text()" />
    </article>
  </xsl:template>
  
  <!-- Dynamic includes -->
  
  <xsl:template match="xinclude">
    <xsl:copy-of select="document(@uri)/*/node()" />
  </xsl:template>
  
</xsl:stylesheet>
