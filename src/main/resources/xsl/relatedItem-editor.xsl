<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 36983 $ $Date: 2017-02-15 16:01:53 +0100 (Mi, 15 Feb 2017) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xed="http://www.mycore.de/xeditor" 
  exclude-result-prefixes="xsl xalan mods xed">

  <xsl:param name="loaded_navigation_xml" />
  <xsl:param name="lastPage" />
  <xsl:param name="href" />
  <xsl:param name="HttpSession" />
  <xsl:param name="RequestURL" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:include href="mods-display.xsl" />
  <xsl:include href="mycoreobject-html.xsl" />
  <xsl:include href="coreFunctions.xsl" />

  <xsl:template match="mycoreobject">
    <xed:template>
      <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
        <xsl:apply-templates select="." mode="cite"> 
          <xsl:with-param name="mode">divs</xsl:with-param> 
        </xsl:apply-templates>
        <xsl:call-template name="ibn-export">
          <xsl:with-param name="type">isbn</xsl:with-param>
        </xsl:call-template> 
        <xsl:call-template name="ibn-export">
          <xsl:with-param name="type">issn</xsl:with-param>
        </xsl:call-template> 
        <xsl:apply-templates select="mods:identifier[@type='doi']" mode="html-export" />
        <xsl:apply-templates select="mods:identifier[@type='urn']" mode="html-export" /> 
        <xsl:apply-templates select="mods:identifier[@type='duepublico']" mode="html-export" /> 
      </xsl:for-each>
    </xed:template>
  </xsl:template>

</xsl:stylesheet>
