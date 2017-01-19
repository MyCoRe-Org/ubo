<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
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

<xsl:include href="bibmaster.xsl" />
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

<xsl:template match="/mycoreobject">
  <email>
    <from><xsl:value-of select="$MCR.Mail.Address" /></from>
    <to><xsl:value-of select="$MCR.Mail.Address" /></to>
    <xsl:for-each select="metadata/def.modsCollection/modsCollection/mods:mods">
      <xsl:call-template name="build.to" />
      <xsl:call-template name="build.subject" />
      <xsl:call-template name="build.body" />
    </xsl:for-each>
  </email>
</xsl:template>

<xsl:template name="build.to">
  <xsl:for-each select="mods:classification[contains(@authorityURI,'fachreferate')]">
    <xsl:variable name="subject" select="substring-after(@valueURI,'#')" />
    <xsl:for-each select="$subjects/item[@value=$subject]/email">
      <to><xsl:copy-of select="text()" /></to>
    </xsl:for-each>
  </xsl:for-each>
</xsl:template>

<xsl:template name="build.subject">
  <subject>
    <xsl:text>Universitätsbibliographie: </xsl:text>
    <xsl:value-of select="number(substring-after(/mycoreobject/@ID,'_mods_'))" />
    <xsl:text> / </xsl:text>
    <xsl:for-each select="mods:name[mods:role/mods:roleTerm='aut'][1]">
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:if test="position() != last()">; </xsl:if>
    </xsl:for-each>
    <xsl:text> / </xsl:text>
    <xsl:apply-templates select="mods:titleInfo[1]" />
  </subject>
</xsl:template>

<xsl:template name="build.body">
  <body>
    <xsl:text>
Liebe Kollegin, lieber Kollege,

der folgende Eintrag ist per Selbsteingabe an die Universitätsbibliographie gemeldet worden:

</xsl:text>

  <xsl:for-each select="mods:classification[contains(@authorityURI,'fachreferate')]">
    <xsl:text>Fach: </xsl:text>
    <xsl:value-of select="$subjects/item[@value=substring-after(current()/@valueURI,'#')]/@label" />
    <xsl:text>&#xa;</xsl:text>
  </xsl:for-each>
  <xsl:for-each select="mods:classification[contains(@authorityURI,'ORIGIN')]">
    <xsl:text>Fakultät: </xsl:text>
    <xsl:call-template name="output.category">
      <xsl:with-param name="classID" select="'ORIGIN'" />
      <xsl:with-param name="categID" select="substring-after(@valueURI,'#')" />
    </xsl:call-template>
    <xsl:text>&#xa;</xsl:text>
  </xsl:for-each>
  <xsl:text>&#xa;</xsl:text>
    
  <xsl:variable name="bibentry.html">
    <xsl:apply-templates select="." mode="html-export" /> 
  </xsl:variable>
  <xsl:apply-templates select="xalan:nodeset($bibentry.html)" />
    
<xsl:text>
Bitte folgen Sie diesem Link:

</xsl:text>
<xsl:value-of select="$WebApplicationBaseURL" />
<xsl:text>servlets/DozBibEntryServlet?mode=show&amp;id=</xsl:text>
<xsl:value-of select="/mycoreobject/@ID" />
<xsl:text>

Mit freundlichen Grüßen

Ihre Universitätsbibliographie
</xsl:text>
  </body>
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
