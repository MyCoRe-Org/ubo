<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xsl xalan">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" 
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    doctype-system="http://www.w3.org/TR/html401/loose.dtd" 
    indent="yes" xalan:indent-amount="2" />

  <xsl:param name="loaded_navigation_xml" />
  <xsl:param name="lastPage" />
  <xsl:param name="href" />
  <xsl:param name="HttpSession" />
  <xsl:param name="RequestURL" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:include href="bibmaster.xsl" />
  <xsl:include href="mods-display.xsl" />
  <xsl:include href="bibentry-html.xsl" />
  <xsl:include href="coreFunctions.xsl" />

  <!-- ============ Link zum default CSS adaptiv via http/https ============ -->
  <xsl:variable name="defaultCssHref">
    <xsl:variable name="delimiter">://</xsl:variable>
    <xsl:value-of select="substring-before($RequestURL,$delimiter)" />
    <xsl:value-of select="$delimiter" />
    <xsl:value-of select="substring-after($WebApplicationBaseURL,$delimiter)" />
    <xsl:text>export-bibentries-html.css</xsl:text> 
  </xsl:variable>

  <xsl:param name="css" select="$defaultCssHref" />

  <xsl:template match="/bibentries">
    <html> 
      <head>
        <META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" type="text/css" href="{$css}" />
      </head>
      <body>
        <ul class="bibentries">
          <xsl:apply-templates select="bibentry" />
        </ul>
      </body>
    </html>
  </xsl:template>

<!-- ============ Einzeltreffer Detail-Anzeige ============ -->

  <xsl:template match="bibentry">
    <li>
      <div class="bibentry">
        <xsl:apply-templates select="." mode="html-export" />
      </div>
    </li>
  </xsl:template>

</xsl:stylesheet>
