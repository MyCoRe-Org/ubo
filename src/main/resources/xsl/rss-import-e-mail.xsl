<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 34514 $ $Date: 2016-02-04 19:27:24 +0100 (Do, 04 Feb 2016) $ -->
<!-- ============================================== --> 

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:ubo="xalan://unidue.ubo.DozBibEntryServlet"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan ubo mods"
>

<xsl:output method="xml" />

<xsl:include href="mods-display.xsl" />
<xsl:include href="mycoreobject-html.xsl" />
<xsl:include href="coreFunctions.xsl" />

<xsl:param name="loaded_navigation_xml" />
<xsl:param name="lastPage" />
<xsl:param name="href" />
<xsl:param name="HttpSession" />
<xsl:param name="RequestURL" />
<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="ServletsBaseURL" />
<xsl:param name="MCR.Mail.Address" />
<xsl:param name="RSS.SourceSystem" />

<xsl:variable name="br"><xsl:text>
</xsl:text></xsl:variable>

<xsl:template match="/bibentries">
  <email>
    <from><xsl:value-of select="$MCR.Mail.Address" /></from>
    <to><xsl:value-of select="$MCR.Mail.Address" /></to>
    <subject>Universitätsbibliographie: <xsl:value-of select="$RSS.SourceSystem" /> RSS Feed Import</subject>
    <body>
    <xsl:text>
Liebe Kollegin, lieber Kollege,

die folgenden Publikationen wurden aus </xsl:text><xsl:value-of select="$RSS.SourceSystem" /><xsl:text> importiert:

</xsl:text>
      <xsl:for-each select="mycoreobject">
        <xsl:text>&#xa;</xsl:text>
        <xsl:value-of select="$WebApplicationBaseURL" />
        <xsl:text>servlets/DozBibEntryServlet?mode=show&amp;id=</xsl:text>
        <xsl:value-of select="@ID" />
        <xsl:text>&#xa;</xsl:text>
        <xsl:variable name="bibentry.html">
          <xsl:apply-templates select="." mode="html-export" /> 
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($bibentry.html)" />
        <xsl:text>&#xa;</xsl:text>
      </xsl:for-each> 
<xsl:text>

Mit freundlichen Grüßen

Ihre Universitätsbibliographie
</xsl:text>
    </body>
  </email>
</xsl:template>

<xsl:template match="div">
  <xsl:apply-templates select="*|text()" />
  <xsl:text>&#xa;</xsl:text>
</xsl:template>

<xsl:template match="a">
  <xsl:value-of select="text()" />
  <xsl:text>: </xsl:text>
  <xsl:value-of select="@href" />
</xsl:template>

<xsl:template match="text()">
  <xsl:value-of select="." />
</xsl:template>

<xsl:template match="@*" />

</xsl:stylesheet>
