<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl mods xalan i18n">

  <xsl:param name="ServletsBaseURL" />

  <xsl:template match="/response">
    <include>
      <xsl:if xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()"> 
        <article class="highlight2">
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='status']" />
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='modified']/lst[@name='counts']" />
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='created']/lst[@name='counts']" />
        </article>
      </xsl:if>
    </include>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='status']">
    <hgroup>
      <h2>Publikationen nach Status:</h2>
    </hgroup>
    <ul style="list-style:none;">
      <xsl:for-each select="int">
        <xsl:call-template name="output.value">
          <xsl:with-param name="label" select="i18n:translate(concat('search.dozbib.status.',@name))"/>
          <xsl:with-param name="value" select="text()" />
          <xsl:with-param name="query" select="concat('status:',@name)" />
        </xsl:call-template>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <xsl:template match="lst[@name='facet_ranges']/lst/lst[@name='counts']">
    <xsl:variable name="numDays" select="count(int)" />
    <xsl:variable name="dateField" select="../@name" /> <!-- created|modified -->
    <hgroup>
      <h2><xsl:value-of select="i18n:translate(concat('facets.facet.',$dateField))" />:</h2>
    </hgroup>
    <ul style="list-style:none;">
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">in den letzten 14 Tagen</xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 14)])" />
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-13DAY TO NOW]')" />
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">in den letzten 7 Tagen</xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 7)])" />
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-6DAY TO NOW]')" />
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">gestern oder heute</xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 2)])" />
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-1DAY TO NOW]')" />
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">heute</xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 1)])" />
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY TO NOW]')" />
      </xsl:call-template>
    </ul>
  </xsl:template>
  
  <xsl:template name="output.value">
    <xsl:param name="label" />
    <xsl:param name="value" />
    <xsl:param name="query" />
    
    <li>
      <a href="{$ServletsBaseURL}solr/select?q={$query}">
        <span style="width:140px; display:inline-block; text-align:right; padding-right:1ex;">
          <xsl:value-of select="$label"/>:
        </span>
        <xsl:value-of select="$value"/>
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>