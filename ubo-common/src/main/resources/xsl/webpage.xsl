<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 35435 $ $Date: 2016-05-24 10:54:17 +0200 (Di, 24 Mai 2016) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  exclude-result-prefixes="xsl i18n">

  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />
  
  <xsl:include href="copynodes.xsl" />
  <xsl:include href="xslInclude:webpage" />

  <xsl:template match="/webpage">
    <xsl:variable name="title">
      <xsl:choose>
        <xsl:when test="title[lang($CurrentLang)]">
          <xsl:value-of select="title[lang($CurrentLang)]" />
        </xsl:when>
        <xsl:when test="title[lang($DefaultLang)]">
          <xsl:value-of select="title[lang($DefaultLang)]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="title[1]" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <html>
      <xsl:copy-of select="@id" />
      <xsl:copy-of select="@lastModified" />
      <head>
        <title>
          <xsl:value-of select="$title" />
        </title>
      </head>
      <body>
        <ul id="breadcrumb">
          <li>
            <xsl:value-of select="$title" />
          </li>
        </ul>
        <xsl:apply-templates select="*" />
      </body>
    </html>
  </xsl:template>
  
  <!-- Required to launch WebCLI and classification editor -->
  <xsl:template match="/site">
    <html>
      <head>
        <title>
          <xsl:value-of select="@title" />
        </title>
      </head>
      <body>
        <ul id="breadcrumb">
          <li>
            <xsl:value-of select="@title" />
          </li>
        </ul>
        <xsl:apply-templates select="*" />
      </body>
    </html>
  </xsl:template>

  <!-- article -->

  <xsl:template match="article[lang($CurrentLang) or (string-length(@xml:lang) = 0)]" priority="1">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="node()" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="article" />
  
  <!-- Dynamic includes -->
  
  <xsl:template match="xinclude">
    <xsl:apply-templates select="document(@uri)/*" />
  </xsl:template>
  
</xsl:stylesheet>
