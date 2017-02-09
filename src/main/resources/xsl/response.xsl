<?xml version="1.0" encoding="UTF-8"?>

<!-- Displays a navigable result list of a SOLR search for bibliography entries -->

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:str="xalan://java.lang.String"
  exclude-result-prefixes="xsl xalan i18n mods mcr encoder" 
>

<xsl:include href="layout.xsl" />
<xsl:include href="bibmaster.xsl" />
<xsl:include href="mods-display.xsl" />
<xsl:include href="response-facets.xsl" />

<xsl:param name="RequestURL" />

<xsl:variable name="ContextID" select="'dozbib.search'" />

<xsl:variable name="breadcrumb.extensions">
  <item label="{i18n:translate('result.dozbib.results')}" />
</xsl:variable>

<xsl:variable name="head.additional">
  <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
</xsl:variable>

<!-- ==================== Trefferliste Metadaten ==================== -->

<xsl:variable name="numFound" select="/response/result[@name='response']/@numFound" />
<xsl:variable name="numDocs" select="count(/response/result[@name='response']/doc)" />
<xsl:variable name="start">
  <xsl:choose>
    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']">
      <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']" />
    </xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="rows">
  <xsl:choose>
    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']">
      <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
    </xsl:when>
    <xsl:when test="$start + $numDocs &lt; $numFound">
      <xsl:value-of select="$numDocs" /> <!-- rows parameter missing, use count docs returned -->
    </xsl:when>
    <xsl:otherwise>10</xsl:otherwise> <!-- guess, no way to find out -->
  </xsl:choose>
</xsl:variable>

<!-- ==================== Anzeige Seitentitel ==================== -->

<xsl:variable name="page.title">
  <xsl:value-of select="i18n:translate('result.dozbib.results')" /> 
  <xsl:text>: </xsl:text> 
  <xsl:choose>
    <xsl:when test="$numFound > 1">
      <xsl:value-of select="$numFound" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationMany')"/>
    </xsl:when>
    <xsl:when test="$numFound = 1">
      <xsl:value-of select="i18n:translate('result.dozbib.publicationOne')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')"/>
    </xsl:otherwise>
  </xsl:choose> 
</xsl:variable>

<!-- ==================== Export-Buttons ==================== -->

<xsl:variable name="exportParams">
  <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/*[(@name='q') or (@name='fq')]">
    <xsl:variable name="name" select="@name" />
    <xsl:for-each select="descendant-or-self::str"> <!-- may be an array: arr/str or ./str -->
      <xsl:value-of select="$name" />
      <xsl:text>=</xsl:text>
      <xsl:value-of select="encoder:encode(text(),'UTF-8')" />
      <xsl:text>&amp;</xsl:text>
    </xsl:for-each>
  </xsl:for-each>
  <xsl:value-of select="concat('rows=',$numFound)" />
</xsl:variable>

<xsl:variable name="actions">
  <xsl:if test="$numFound &gt; 0">
    <action label="MODS"      target="export?{$exportParams}&amp;XSL.Transformer=mods" />
    <action label="BibTeX"    target="export?{$exportParams}&amp;XSL.Transformer=bibtex" />
    <action label="EndNote"   target="export?{$exportParams}&amp;XSL.Transformer=endnote" />
    <action label="RIS"       target="export?{$exportParams}&amp;XSL.Transformer=ris" />
    <action label="PDF"       target="export?{$exportParams}&amp;XSL.Transformer=pdf" />
    <action label="HTML"      target="export?{$exportParams}&amp;XSL.Transformer=html" />
    <action label="CSV"       target="export?{$exportParams}&amp;fl=id,year,genre,title&amp;wt=csv" />
  </xsl:if>
</xsl:variable>

<!-- ==================== Facetten / Filtern ==================== -->

<xsl:variable name="sidebar">
  <xsl:apply-templates select="/response/lst[@name='facet_counts'][lst[@name='facet_fields']/lst[int]]" />
</xsl:variable>

<!-- ==================== Anzeige Trefferliste ==================== -->

<xsl:template match="response">
  <xsl:apply-templates select="result[@name='response']" />
</xsl:template>

<xsl:template match="result[@name='response']">

  <!-- Seitennavigation oben -->
  <xsl:call-template name="navigation" />

  <!-- Trefferliste -->
  <div class="container_12" style="margin-top:1ex;">
    <xsl:if test="$numFound = 0">
      <p>
        <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')" />
      </p>
    </xsl:if>
    <xsl:if test="$numFound &gt; 0">
      <ol class="results">
        <xsl:apply-templates select="doc">
          <xsl:with-param name="start" select="@start"/>
        </xsl:apply-templates>
      </ol>
    </xsl:if>
  </div>
  <div class="clear"></div>
  
  <!-- Seitennavigation unten -->
  <xsl:call-template name="navigation" />
  
</xsl:template>

<!-- ==================== Anzeige Navigation in Trefferliste ==================== -->

<xsl:variable name="resultsPageURL">
  <xsl:text>select?</xsl:text>
  <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/*[not(@name='start')]">
    <xsl:variable name="name" select="@name" />
    <xsl:for-each select="descendant-or-self::str"> <!-- may be an array: arr/str or ./str -->
      <xsl:value-of select="$name" />
      <xsl:text>=</xsl:text>
      <xsl:value-of select="encoder:encode(text())" />
      <xsl:text>&amp;</xsl:text>
    </xsl:for-each>
  </xsl:for-each>
  <xsl:text>start=</xsl:text>
</xsl:variable>

<xsl:template name="navigation">
  <div class="resultsNavigation section">

    <span class="pageLink" style="float:left;">
      <a href="{$ServletsBaseURL}Results2Basket?solr={encoder:encode($exportParams)}"><xsl:value-of select="i18n:translate('button.basketAdd')" /></a>
    </span>

    <xsl:if test="$numFound &gt; $rows">
      <xsl:call-template name="link2resultsPage">
        <xsl:with-param name="condition" select="$start &gt; 0" />
        <xsl:with-param name="start"     select="0" />
        <xsl:with-param name="icon"      select="'fast-backward'" />
      </xsl:call-template>
      <xsl:call-template name="link2resultsPage">
        <xsl:with-param name="condition" select="$start &gt; 0" />
        <xsl:with-param name="start"     select="number($start)-number($rows)" />
        <xsl:with-param name="icon"      select="'backward'" />
      </xsl:call-template>
      <xsl:call-template name="link2resultsPage">
        <xsl:with-param name="text"      select="floor(number($start) div number($rows))+1" />
      </xsl:call-template>
      <xsl:call-template name="link2resultsPage">
        <xsl:with-param name="condition" select="number($start)+number($rows) &lt; $numFound" />
        <xsl:with-param name="start"     select="number($start)+number($rows)" />
        <xsl:with-param name="icon"      select="'forward'" />
      </xsl:call-template>
      <xsl:call-template name="link2resultsPage">
        <xsl:with-param name="condition" select="number($start)+number($rows) &lt; $numFound" />
        <xsl:with-param name="start"     select="floor(number($numFound) div number($rows))*number($rows)" />
        <xsl:with-param name="icon"      select="'fast-forward'" />
      </xsl:call-template>
    </xsl:if>

    <span class="pageLink" style="float:right;">
      <a href="statistics?{$exportParams}&amp;XSL.Transformer=statistics"><xsl:value-of select="i18n:translate('button.statistics')" /></a>
    </span>

  </div>
</xsl:template>

<xsl:template name="link2resultsPage">
  <xsl:param name="condition" />
  <xsl:param name="start" />
  <xsl:param name="icon" />
  <xsl:param name="text" />
  
  <span class="pageLink">
    <xsl:choose>
      <xsl:when test="string-length($text) &gt; 0">
        <xsl:value-of select="concat('&#160;',$text,'&#160;')" />
      </xsl:when>
      <xsl:when test="$condition">
        <a href="{$resultsPageURL}{$start}">
          <span class="glyphicon glyphicon-{$icon}" aria-hidden="true"></span>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <span class="glyphicon glyphicon-{$icon}" aria-hidden="true"></span>
      </xsl:otherwise>
    </xsl:choose>
  </span>
</xsl:template>

<!-- ==================== Anzeige Treffer ==================== -->

<xsl:template match="doc">
  <xsl:param name="start" />
  <xsl:variable name="hitNo" select="$start + position()" />
  
  <div class="grid_12">
    <div class="hit">
      <xsl:variable name="id" select="str[@name='id']" />
      <xsl:variable name="mycoreobject" select="document(concat('mcrobject:',$id))/mycoreobject" />
      <xsl:for-each select="$mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
        <div class="labels">
          <xsl:call-template name="label-year" />
          <xsl:call-template name="pubtype" />
        </div>
        <div class="content bibentry">  
          <xsl:apply-templates select="." mode="cite"> 
            <xsl:with-param name="mode">divs</xsl:with-param> 
          </xsl:apply-templates>
        </div>
        <div class="footer">
          <xsl:call-template name="bibentry.show.details" />
          <xsl:call-template name="bibentry.add.to.basket" />
          <span class="floatRight"># <xsl:value-of select="$hitNo"/></span>
        </div>
      </xsl:for-each>
    </div>
  </div>
</xsl:template>

<xsl:template name="bibentry.add.to.basket">
  <form action="{$ServletsBaseURL}MCRBasketServlet" method="get">
    <input type="hidden" name="action" value="add"/>
    <input type="hidden" name="type" value="bibentries"/>
    <input type="hidden" name="resolve" value="true"/>
    <input type="hidden" name="id" value="{ancestor::mycoreobject/@ID}"/>
    <input type="hidden" name="uri" value="mcrobject:{ancestor::mycoreobject/@ID}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('button.basketAdd')}" />
  </form>
</xsl:template>

<xsl:template name="bibentry.show.details">
  <form action="{$ServletsBaseURL}DozBibEntryServlet" method="get">
    <input type="hidden" name="mode" value="show"/>
    <input type="hidden" name="id" value="{ancestor::mycoreobject/@ID}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('result.dozbib.info')}" />
  </form>
</xsl:template>

</xsl:stylesheet>
