<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n">

  <xsl:import href="resource:xsl/charts/bar-chart.xsl"/>
  <xsl:import href="resource:xsl/charts/oa-chart.xsl" />
  <xsl:import href="resource:xsl/charts/pie-chart.xsl"/>

  <xsl:include href="statistics.xsl" />

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/">
    <html id="dozbib.search">
      <head>
        <title>
          <xsl:call-template name="page.title" />
        </title>

        <script src="{$WebApplicationBaseURL}assets/echarts/dist/echarts.js"/>
      </head>
      <body>
        <xsl:apply-templates select="response" />
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="page.title">
    <xsl:value-of select="i18n:translate('stats.page.title')" />
    <xsl:text>: </xsl:text>
    <xsl:value-of select="/response/result[@name='response']/@numFound" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('ubo.publications')" />
  </xsl:template>

  <xsl:template match="response" priority="1">

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.year')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'year'"/>
      <xsl:with-param name="horizontal-bars" select="'false'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.subject')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'subject'"/>
      <xsl:with-param name="height" select="800"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.genre')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ubogenre'"/>
      <xsl:with-param name="facet-name" select="'genre'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.oa')/i18n/text()"/>
      <xsl:with-param name="classId" select="'oa'"/>
      <xsl:with-param name="facet-name" select="'oa'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.nid_connection')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'nid_connection'"/>
      <xsl:with-param name="generate-labels-from-pivot" select="'true'"/>
      <xsl:with-param name="height" select="1500"/>
    </xsl:apply-templates>
  </xsl:template>
</xsl:stylesheet>
