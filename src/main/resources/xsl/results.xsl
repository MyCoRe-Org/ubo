<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Displays a navigable result list of a search for bibliography entries -->

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcr="http://www.mycore.org/"
  exclude-result-prefixes="xsl xalan i18n mods mcr" 
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

<xsl:variable name="resultsID" select="/mcr:results/@id" />
<xsl:variable name="resultsPage" select="/mcr:results/@page" />
<xsl:variable name="resultsNumPerPage" select="/mcr:results/@numPerPage" />

<!-- ==================== Anzeige Seitentitel ==================== -->

<xsl:variable name="page.title">
  <xsl:value-of select="i18n:translate('result.dozbib.results')" /> 
  <xsl:text>: </xsl:text> 
  <xsl:choose>
    <xsl:when test="/mcr:results/@numHits > 1">
      <xsl:value-of select="/mcr:results/@numHits" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationMany')"/>
    </xsl:when>
    <xsl:when test="/mcr:results/@numHits = 1">
      <xsl:value-of select="i18n:translate('result.dozbib.publicationOne')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')"/>
    </xsl:otherwise>
  </xsl:choose> 
</xsl:variable>

<!-- ==================== Export-Buttons ==================== -->

<xsl:variable name="actions">
  <xsl:if test="/mcr:results/@numHits &gt; 0">
    <action label="{i18n:translate('button.basketAdd')}" target="{$ServletsBaseURL}Results2Basket">
      <param name="basket" value="bibentries" />
      <param name="id"     value="{$resultsID}" />
    </action>
    <action label="MODS" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="mods" />
    </action>
    <action label="BibTeX" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="bibtex" />
    </action>
    <action label="EndNote" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="endnote" />
    </action>
    <action label="RIS" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="ris" />
    </action>
    <action label="PDF" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="pdf" />
    </action>
    <action label="HTML" target="{$ServletsBaseURL}MCRSearchServlet">
      <param name="mode"             value="results" />
      <param name="id"               value="{$resultsID}" />
      <param name="numPerPage"       value="{/mcr:results/@numHits}" />
      <param name="XSL.Transformer"  value="html" />
    </action>
  </xsl:if>
</xsl:variable>

<!-- ==================== Anzeige Trefferliste ==================== -->

<xsl:template match="mcr:results">

  <xsl:variable name="offset" select="(number(@page) - 1) * number(@numPerPage)"/>

  <!-- Seitennavigation oben -->
  <xsl:if test="@numPages &gt; 1">
    <div class="resultsNavigation section">
      <xsl:call-template name="navigation" />
    </div>
  </xsl:if>

  <!-- Trefferliste -->
  <div class="container_12">
    <xsl:if test="@numHits = 0">
      <p>
        <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')" />
      </p>
    </xsl:if>
    <xsl:if test="@numHits &gt; 0">
      <ol class="results">
        <xsl:apply-templates select="mcr:hit">
          <xsl:with-param name="offset" select="$offset"/>
        </xsl:apply-templates>
      </ol>
    </xsl:if>
  </div>
  <div class="clear"></div>
  
  <!-- Seitennavigation unten -->
  <xsl:if test="@numPages &gt; 1">
    <div class="resultsNavigation section" id="sectionlast">
      <xsl:call-template name="navigation" />
    </div>
  </xsl:if>
  
</xsl:template>

<!-- ==================== Anzeige Navigation in Trefferliste ==================== -->

<xsl:template name="navigation">

  <xsl:variable name="resultpage" select="concat('MCRSearchServlet?mode=results&amp;id=',@id,'&amp;numPerPage=',@numPerPage,'&amp;page=')" />
  <xsl:variable name="first" select="(number(@page) - 1) * number(@numPerPage) + 1" />
  <xsl:variable name="last" select="$first - 1 + count(mcr:hit)" />
  <xsl:variable name="pageaddition" select="'2'"/>
  
  <xsl:variable name="firstPageIndex">
    <xsl:choose>
      <xsl:when test="number(@page) - $pageaddition &lt; 1">1</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(@page) - $pageaddition"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="lastPageIndex">
    <xsl:choose>
      <xsl:when test="number(@page) + $pageaddition &gt; number(@numPages)">
        <xsl:value-of select="@numPages"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(@page) + $pageaddition"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="loop1"> <!-- Sufficient for up to 200 result pages -->
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
    <x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/><x/>
  </xsl:variable>
  <xsl:variable name="loop2" select="xalan:nodeset($loop1)" />
  
  <xsl:variable name="currentPage" select="number(@page)"/>

  <!-- link to first page -->
  <span>
    <a href="{$resultpage}1" class="pageLink"><xsl:value-of select="i18n:translate('search.first')"/></a>
  </span>
  
  <xsl:if test="$currentPage &gt; 1">
    <span>
      <xsl:element name="a">
        <xsl:attribute name="class">pageLink</xsl:attribute>
        <xsl:attribute name="href">
          <xsl:value-of select="$resultpage"/>
          <xsl:value-of select="$currentPage - 1"/>
        </xsl:attribute>
        <xsl:value-of select="i18n:translate('search.go.back')"/>
      </xsl:element>
    </span>
  </xsl:if>
  
  <xsl:for-each select="$loop2/x[position() &lt;= number($lastPageIndex) - number($firstPageIndex) + 1]">
    <span>
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:value-of select="$resultpage"/>
          <xsl:value-of select="position() + number($firstPageIndex) - 1"/>
        </xsl:attribute>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="position() + number($firstPageIndex) - 1 = $currentPage">
              <xsl:value-of select="'current'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'normal'"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text> pageLink</xsl:text>
        </xsl:attribute>
      
        <xsl:value-of select="position() + number($firstPageIndex) - 1"/>
      </xsl:element>
    </span>
  </xsl:for-each>
  
  <xsl:if test="$currentPage != number(@numPages)">
    <span>
      <xsl:element name="a">
        <xsl:attribute name="class">pageLink</xsl:attribute>
        <xsl:attribute name="href">
          <xsl:value-of select="$resultpage"/>
          <xsl:value-of select="$currentPage + 1"/>
        </xsl:attribute>
        <xsl:value-of select="i18n:translate('search.next')"/>
      </xsl:element>
    </span>
  </xsl:if>

  <!-- link to last page -->
  <span>
    <a href="{$resultpage}{number(@numPages)}" class="pageLink"><xsl:value-of select="i18n:translate('search.last')"/></a>
  </span>
  
</xsl:template>

<!-- ==================== Anzeige Treffer ==================== -->

<xsl:template match="mcr:hit">
  <xsl:param name="offset" />
  
  <div class="grid_1">
    <div class="number">#<xsl:value-of select="$offset + position()"/></div>
  </div>
  <div class="grid_11">
    <div class="hit">
      <xsl:variable name="mycoreobject" select="document(concat('mcrobject:',@id))/mycoreobject" />
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
          <span class="floatRight">[ ID <xsl:value-of select="number(substring-after(ancestor::mycoreobject/@ID,'mods_'))"/> ]</span>
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
    <input type="hidden" name="XSL.resultsID" value="{$resultsID}"/>
    <input type="hidden" name="XSL.resultsPage" value="{$resultsPage}"/>
    <input type="hidden" name="XSL.resultsNumPerPage" value="{$resultsNumPerPage}"/>
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
