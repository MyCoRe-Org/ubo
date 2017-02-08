<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:param name="ServletsBaseURL" />
  
  <xsl:variable name="thisYear" xmlns:datetime="http://exslt.org/dates-and-times" select="number(datetime:year())" />
  <xsl:variable name="nextYear" select="$thisYear + 1" />
  <xsl:variable name="lastYear" select="$thisYear - 1" />

  <xsl:template match="/sidebar">
    <sidebar>
      <article class="highlight2">
        <xsl:call-template name="numPublications" />
        <xsl:call-template name="latestYearsFacets" />
      </article>
      <article class="highlight1">
        <xsl:call-template name="newestPublications" />
      </article>
    </sidebar>
  </xsl:template>
  
  <xsl:template name="numPublications">
    <xsl:for-each select="document('solr:q=status:confirmed&amp;rows=0')/response">
      <p>Derzeit sind <strong><xsl:value-of select="result[@name='response']/@numFound" /> Publikationen verzeichnet.</strong></p>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="latestYearsFacets">
    <xsl:for-each select="document(concat('solr:q=status:confirmed+AND+year:[',$lastYear - 1,'+TO+',$nextYear,']&amp;rows=0&amp;facet=true&amp;facet.field=year&amp;facet.mincount=1'))/response">
      <p>
        <xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='year']/int">
          <xsl:sort select="@name" data-type="number" order="descending" />
          <xsl:value-of select="@name"/>
          <xsl:text> : </xsl:text>
          <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+year:{@name}">
            <xsl:value-of select="text()"/> Publikationen
          </a>
          <br/>
        </xsl:for-each>
      </p>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="newestPublications">
    <hgroup>
      <h2>Neueste Publikationen:</h2>
    </hgroup>
    <xsl:for-each select="document(concat('solr:q=status:confirmed+AND+year:[',$lastYear,'+TO+',$nextYear,']&amp;fl=id,title,year,person&amp;rows=4&amp;sort:created+desc'))/response">
      <ul>
        <xsl:for-each select="result[@name='response']/doc">
          <li style="margin-bottom:1ex;">
            <xsl:value-of select="arr[@name='person']/str[1]" />
            <xsl:if test="count(arr[@name='person']/str) &gt; 1">
              <xsl:text> u. a.</xsl:text>
            </xsl:if>
            <xsl:text>:</xsl:text>
            <br/>
            <a href="{$ServletsBaseURL}DozBibEntryServlet?id={str[@name='id']}">
              <xsl:value-of select="arr[@name='title']/str" />
            </a>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="int[@name='year']" />
            <xsl:text>)</xsl:text>
          </li>
        </xsl:for-each>
      </ul>
    </xsl:for-each>
    <p class="mehrzu_info">
      <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+year:[{$lastYear}+TO+{$nextYear}]&amp;sort:created+desc">mehr...</a>
    </p>
  </xsl:template>
  
</xsl:stylesheet>