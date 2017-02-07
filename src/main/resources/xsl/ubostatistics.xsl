<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  exclude-result-prefixes="xsl xalan i18n">

<xsl:include href="layout.xsl"/>
<xsl:include href="highcharts-header.xsl"/>

<xsl:include href="jschart/generic/piechart.xsl" />
<xsl:include href="jschart/generic/column-rotated-labels.xsl"/>
<xsl:include href="jschart/generic/nochart.xsl"/>
<xsl:include href="jschart/generic/single-time-series.xsl"/>
<xsl:include href="jschart/publications-by-year.xsl"/>

<xsl:variable name="PageID" select="'dozbib.stats'"/>

<xsl:variable name="page.title">
  <xsl:value-of select="i18n:translate('stats.page.title')" />
  <xsl:text>: </xsl:text>
  <xsl:value-of select="/ubostatistics/@total" />
  <xsl:text> </xsl:text>
  <xsl:value-of select="i18n:translate('ubo.publications')" />
</xsl:variable>

<xsl:variable name="head.additional">
  <xsl:call-template name="highcharts.header"/>
</xsl:variable>

<xsl:template match="ubostatistics">
  <h2><xsl:value-of select="i18n:translate('stats.hint.title')" />: <xsl:value-of select="i18n:translate('stats.hint.show.dialog')" /></h2>
  <div id="chart-dialog" />
  <xsl:apply-templates select="table" />
</xsl:template>

<xsl:template match="table[@charttype='Matrix']">
  <xsl:variable name="typeHelper">
    <xsl:for-each select="row">
      <xsl:variable name="prefix" select="substring-before(@key,'2')" />
      <xsl:if test="not(preceding::row[starts-with(@key,$prefix)])">
        <xsl:value-of select="$prefix" /><xsl:text> </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="types" select="xalan:tokenize($typeHelper)" />
  <xsl:variable name="table" select="." />
  <div class="ubo-chart">
    <h3>
      <xsl:value-of select="@name" />:
    </h3>
    <table class="editor" style="width:auto;">
      <tr>
        <th style="width:50px; text-align:center;">\</th>
        <xsl:for-each select="$types">
          <th style="width:50px; text-align:center;">
            <xsl:value-of select="." />:
          </th>
        </xsl:for-each>
      </tr>
      <xsl:for-each select="$types">
        <tr>
          <th class="identifier">
            <xsl:value-of select="." />:
          </th>
          <xsl:variable name="prefix" select="." />
          <xsl:for-each select="$types">
            <xsl:variable name="key" select="concat($prefix,'2',.)" />
            <td class="identifier">
              <xsl:value-of select="$table/row[@key=$key]/@num" />
            </td>
          </xsl:for-each>
        </tr>
      </xsl:for-each>
    </table>
  </div>
</xsl:template>

<xsl:param name="UBO.LSF.Link" />

<xsl:template match="table[@charttype='TopList']">
  <div class="ubo-chart">
    <h3>
      <xsl:value-of select="@name" />:
    </h3>
    <table class="editor" style="width:auto;">
      <tr>
        <th><xsl:value-of select="i18n:translate('ubo.search.name')" /></th>
        <th><xsl:value-of select="i18n:translate('stats.count')" />:</th>
      </tr>
      <xsl:for-each select="row">
        <xsl:if test="position() &lt; 200">
          <tr>
            <td>
              <a href="{$UBO.LSF.Link}{@key}">
                <xsl:value-of select="@label" />
              </a>
            </td>
            <td>
              <a href="servlets/DozBibServlet?nid_lsf={@key}&amp;year={/ubostatistics/@minYear}&amp;year.operator=%3E%3D">
                <xsl:value-of select="@num" />
              </a>
            </td>
          </tr>
        </xsl:if>
      </xsl:for-each>
    </table>
  </div>
</xsl:template>

<xsl:template match="table">
  <div id="ubo-chart-{@id}" class="chart">
    <xsl:apply-templates select="." mode="chart" />
  </div>
</xsl:template>

</xsl:stylesheet>