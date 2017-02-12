<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n">

  <xsl:include href="layout.xsl"/>
  <xsl:include href="statistics.xsl" />
  
  <xsl:variable name="page.title">
    <xsl:value-of select="i18n:translate('stats.page.title')" />
    <xsl:text>: </xsl:text>
    <xsl:value-of select="/response/result[@name='response']/@numFound" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('ubo.publications')" />
  </xsl:variable>

  <xsl:template match="/response" priority="1">
    <script src="{$WebApplicationBaseURL}external/jquery-ui-1.8.12.custom.min.js" type="text/javascript"></script>
    <script src="{$WebApplicationBaseURL}webjars/github-com-highcharts-highcharts/4.2.5/lib/highcharts.src.js" type="text/javascript"></script>
    <script src="{$WebApplicationBaseURL}webjars/github-com-highcharts-highcharts/4.2.5/lib/themes/grid.js" type="text/javascript"></script>
    
    <div id="chartDialog" />
    
    <xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']">
      <article class="highlight1">
        <xsl:apply-templates select="lst[@name='year']" />
      </article>
      <article class="highlight1">
        <xsl:apply-templates select="lst[@name='subject']" />
      </article>
      <article class="highlight1">
        <xsl:apply-templates select="lst[@name='genre']" />
      </article>
      <article class="highlight1">
        <xsl:apply-templates select="lst[@name='facet_person']" />
      </article>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>