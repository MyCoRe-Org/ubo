<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:solr="xalan://org.mycore.solr.MCRSolrUtils"
  exclude-result-prefixes="xsl mods xalan i18n">

  <xsl:param name="ServletsBaseURL" />

  <xsl:template match="/response">
    <xsl:if xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()"> 
      <article class="card">
	<div class="card-body">
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='status']" />
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='modified']/lst[@name='counts']" />
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='created']/lst[@name='counts']" />
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='importID']" />
	</div>
      </article>
    </xsl:if>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='status']">
    <hgroup>
      <h3>Publikationen nach Status:</h3>
    </hgroup>
    <ul class="list-group list-group-flush">
      <xsl:for-each select="int">
        <xsl:call-template name="output.value">
          <xsl:with-param name="label" select="i18n:translate(concat('search.dozbib.status.',@name))"/>
          <xsl:with-param name="value" select="text()" />
          <xsl:with-param name="query" select="concat('status:',@name)" />
        </xsl:call-template>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <xsl:variable name="quote">"</xsl:variable>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='importID']">
    <hgroup>
      <h3>Zuletzt importierte Listen:</h3>
    </hgroup>
    <ul class="list-group list-group-flush">
      <xsl:for-each select="int">
        <xsl:sort select="@name"  order="descending" /> 
        <xsl:if test="position() &lt;= 20"> <!-- show only latest 20 imports -->
          <xsl:call-template name="output.value">
	    <xsl:with-param name="label" select="@name"/>
	    <xsl:with-param name="value" select="text()" />
	    <xsl:with-param name="query" select="concat('importID:',$quote,solr:escapeSearchValue(@name),$quote)" />
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="lst[@name='facet_ranges']/lst/lst[@name='counts']">
    <xsl:variable name="numDays" select="count(int)" />
    <xsl:variable name="dateField" select="../@name" /> <!-- created|modified -->
    <hgroup>
      <h3><xsl:value-of select="i18n:translate(concat('facets.facet.',$dateField))" />:</h3>
    </hgroup>
    <ul class="list-group list-group-flush"> 
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
    
    <li class="list-group-item py-0 px-0 border-0">
      <a href="{$ServletsBaseURL}solr/select?q={$query}">
	<table class="table table-borderless w-100 mb-0">
	  <tbody>
	    <tr>
	      <td class="w-75 py-0 text-right"> 
		<xsl:value-of select="$label"/>:
	      </td>
	      <td class="py-0" align="left">
		<xsl:value-of select="$value"/>
	      </td>
	    </tr>
	  </tbody>
	</table>
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>
