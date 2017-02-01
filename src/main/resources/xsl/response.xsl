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

<xsl:variable name="ContextID" select="'dozbib.search'" />

<xsl:variable name="breadcrumb.extensions">
  <item label="{i18n:translate('result.dozbib.results')}" />
</xsl:variable>

<xsl:variable name="head.additional">
  <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
</xsl:variable>

<!-- ==================== Trefferliste Metadaten ==================== -->

<xsl:variable name="numFound" select="/response/result[@name='response']/@numFound" />
<xsl:variable name="rows" select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
<xsl:variable name="start">
  <xsl:choose>
    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']">
      <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']" />
    </xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
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

<xsl:variable name="exportURL">
  <xsl:value-of select="$ServletsBaseURL" />
  <xsl:text>solr/select?</xsl:text>
  <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/str">
    <xsl:value-of select="@name" />
    <xsl:text>=</xsl:text>
    <xsl:choose>
      <xsl:when test="@name='rows'">
        <xsl:value-of select="$numFound" />
      </xsl:when>
      <xsl:when test="@name='start'">
        <xsl:text>0</xsl:text>
      </xsl:when>
      <xsl:when test="@name='fl'">
        <xsl:text>id</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="encoder:encode(text())" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&amp;</xsl:text>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="actions">
  <xsl:if test="$numFound &gt; 0">
    <action label="{i18n:translate('button.basketAdd')}" target="{$ServletsBaseURL}Results2Basket?solr={encoder:encode($exportURL)}" />
    <action label="MODS"    target="{$exportURL}XSL.Transformer=mods" />
    <action label="BibTeX"  target="{$exportURL}XSL.Transformer=bibtex" />
    <action label="EndNote" target="{$exportURL}XSL.Transformer=endnote" />
    <action label="RIS"     target="{$exportURL}XSL.Transformer=ris" />
    <action label="PDF"     target="{$exportURL}XSL.Transformer=pdf" />
    <action label="HTML"    target="{$exportURL}XSL.Transformer=html" />
    <action label="CSV"     target="{str:replaceAll(str:new($exportURL),'fl=id','fl=id,year,genre,title')}wt=csv" />
  </xsl:if>
</xsl:variable>

<!-- ==================== Anzeige Trefferliste ==================== -->

<xsl:template match="response">
  <xsl:apply-templates select="result[@name='response']" />
</xsl:template>

<xsl:template match="result[@name='response']">

  <!-- Seitennavigation oben -->
  <xsl:if test="$numFound &gt; $rows">
    <div class="resultsNavigation section">
      <xsl:call-template name="navigation" />
    </div>
  </xsl:if>

  <!-- Trefferliste -->
  <div class="container_12">
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
  <xsl:if test="$numFound &gt; $rows">
    <div class="resultsNavigation section" id="sectionlast">
      <xsl:call-template name="navigation" />
    </div>
  </xsl:if>
  
</xsl:template>

<!-- ==================== Anzeige Navigation in Trefferliste ==================== -->

<xsl:template name="navigation">
  <xsl:call-template name="link2resultsPage">
    <xsl:with-param name="condition" select="$start &gt; 0" />
    <xsl:with-param name="pageNr"    select="0" />
    <xsl:with-param name="icon"      select="'fast-backward'" />
  </xsl:call-template>
  <xsl:call-template name="link2resultsPage">
    <xsl:with-param name="condition" select="$start &gt; 0" />
    <xsl:with-param name="pageNr"    select="number($start)-number($rows)" />
    <xsl:with-param name="icon"      select="'backward'" />
  </xsl:call-template>
  <xsl:call-template name="link2resultsPage">
    <xsl:with-param name="text"      select="floor(number($start) div number($rows))+1" />
  </xsl:call-template>
  <xsl:call-template name="link2resultsPage">
    <xsl:with-param name="condition" select="number($start)+number($rows) &lt; $numFound" />
    <xsl:with-param name="pageNr"    select="number($start)+number($rows)" />
    <xsl:with-param name="icon"      select="'forward'" />
  </xsl:call-template>
  <xsl:call-template name="link2resultsPage">
    <xsl:with-param name="condition" select="number($start)+number($rows) &lt; $numFound" />
    <xsl:with-param name="pageNr"    select="floor(number($numFound) div number($rows))*number($rows)" />
    <xsl:with-param name="icon"      select="'fast-forward'" />
  </xsl:call-template>
</xsl:template>

<xsl:variable name="resultsPageURL">
  <xsl:text>select?</xsl:text>
  <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/str[not(@name='start')]">
    <xsl:value-of select="@name" />
    <xsl:text>=</xsl:text>
    <xsl:value-of select="encoder:encode(text())" />
    <xsl:text>&amp;</xsl:text>
  </xsl:for-each>
  <xsl:text>start=</xsl:text>
</xsl:variable>

<xsl:template name="link2resultsPage">
  <xsl:param name="condition" />
  <xsl:param name="pageNr" />
  <xsl:param name="icon" />
  <xsl:param name="text" />
  
  <span class="pageLink">
    <xsl:choose>
      <xsl:when test="string-length($text) &gt; 0">
        <xsl:value-of select="concat('&#160;',$text,'&#160;')" />
      </xsl:when>
      <xsl:when test="$condition">
        <a href="{$resultsPageURL}{$pageNr}">
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
          <xsl:call-template name="pubtype" />
          <xsl:call-template name="label-year" />
        </div>
        <div class="content bibentry">  
          <xsl:apply-templates select="." mode="cite"> 
            <xsl:with-param name="mode">divs</xsl:with-param> 
          </xsl:apply-templates>
        </div>
        <div class="footer">
          <xsl:call-template name="bibentry.show.details" />
          <xsl:call-template name="bibentry.add.to.basket" />
          <xsl:apply-templates select="mods:identifier[@type='duepublico']" mode="bibentry.button" />
          <xsl:apply-templates select="mods:identifier[@type='doi']" mode="bibentry.button" />
          <xsl:apply-templates select="mods:location/mods:url" mode="bibentry.button" />
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

<xsl:template match="mods:mods/mods:identifier[@type='duepublico']" mode="bibentry.button">
  <form action="http://duepublico.uni-duisburg-essen.de/servlets/DocumentServlet" method="get">
    <input type="hidden" name="id" value="{text()}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('result.dozbib.fulltext')}" />
  </form>
</xsl:template>

<xsl:template match="mods:mods/mods:identifier[@type='doi']" mode="bibentry.button">
  <form action="https://doi.org/{text()}" method="get">
    <input type="submit" class="roundedButton" value="DOI" />
  </form>
</xsl:template>

<xsl:template match="mods:mods/mods:location/mods:url" mode="bibentry.button">
  <form action="{text()}" method="get">
    <input type="submit" class="roundedButton">
      <xsl:variable name="tmp">
        <xsl:choose>
          <xsl:when test="contains(text(),'://')">
            <xsl:value-of select="substring-after(text(),'://')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="text()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="value">
        <xsl:value-of select="tmp"/>
        <xsl:choose>
          <xsl:when test="contains($tmp,'/')">
            <xsl:value-of select="substring-before($tmp,'/')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$tmp"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </input>
  </form>
</xsl:template>

</xsl:stylesheet>
