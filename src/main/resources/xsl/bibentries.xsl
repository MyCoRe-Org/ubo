<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Displays a navigable result list of a search for bibliography entries -->

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan i18n mods" 
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

<!-- ==================== Anzeige Seitentitel ==================== -->

<xsl:variable name="page.title">
  <xsl:value-of select="i18n:translate('result.dozbib.results')" /> 
  <xsl:text>: </xsl:text> 
  <xsl:choose>
    <xsl:when test="/bibentries/@numHits > 1">
      <xsl:value-of select="/bibentries/@numHits" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationMany')"/>
    </xsl:when>
    <xsl:when test="/bibentries/@numHits = 1">
      <xsl:value-of select="i18n:translate('result.dozbib.publicationOne')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')"/>
    </xsl:otherwise>
  </xsl:choose> 
</xsl:variable>

<!-- ==================== Export-Buttons ==================== -->

<xsl:variable name="actions">
  <xsl:if test="/bibentries/@numHits &gt; 0">
    <action label="{i18n:translate('button.basketAdd')}" target="{$ServletsBaseURL}DozBibServlet">
      <param name="mode"    value="allToBasket" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="MODS" target="{$ServletsBaseURL}DozBibServlet/mods.xml">
      <param name="mode"    value="export" />
      <param name="format"  value="mods" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="BibTeX" target="{$ServletsBaseURL}DozBibServlet/results.bib">
      <param name="mode"    value="export" />
      <param name="format"  value="bibtex" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="EndNote" target="{$ServletsBaseURL}DozBibServlet/results.enl">
      <param name="mode"    value="export" />
      <param name="format"  value="endnote" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="RIS" target="{$ServletsBaseURL}DozBibServlet/results.ris">
      <param name="mode"    value="export" />
      <param name="format"  value="ris" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="PDF" target="{$ServletsBaseURL}DozBibServlet/results.pdf">
      <param name="mode"    value="export" />
      <param name="format"  value="pdf" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
    <action label="HTML" target="{$ServletsBaseURL}DozBibServlet/results.html">
      <param name="mode"    value="export" />
      <param name="format"  value="html" />
      <param name="listKey" value="{/bibentries/@listKey}" />
    </action>
  </xsl:if>
</xsl:variable>

<!-- ==================== Anzeige Trefferliste ==================== -->

<xsl:template match="bibentries">

  <xsl:variable name="hitStartIndex" select="(number(@page) - 1) * number(@numPerPage) + 1"/>

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
        <xsl:apply-templates select="mycoreobject">
          <xsl:with-param name="hitStartIndex" select="$hitStartIndex"/>
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

  <xsl:variable name="resultpage" select="concat('DozBibServlet?mode=list&amp;listKey=',@listKey,'&amp;numPerPage=',@numPerPage,'&amp;page=')" />
  <xsl:variable name="first" select="(number(@page) - 1) * number(@numPerPage) + 1" />
  <xsl:variable name="last" select="$first - 1 + count(mycoreobject)" />
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

<xsl:template match="mycoreobject">
  <xsl:param name="hitStartIndex"/>
  
  <xsl:variable name="index" select="position() + number($hitStartIndex) - 1"/>
    
  <div class="grid_1">
    <div class="number">#<xsl:value-of select="$index"/></div>
  </div>
  <div class="grid_11">
    <div class="hit">
      <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
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
    <input type="hidden" name="id" value="{number(substring-after(ancestor::mycoreobject/@ID,'mods_'))}"/>
    <input type="hidden" name="uri" value="ubo:{number(substring-after(ancestor::mycoreobject/@ID,'mods_'))}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('button.basketAdd')}" />
  </form>
</xsl:template>

<xsl:template name="bibentry.show.details">
  <form action="{$ServletsBaseURL}DozBibEntryServlet" method="get">
    <input type="hidden" name="mode" value="show"/>
    <input type="hidden" name="id" value="{number(substring-after(ancestor::mycoreobject/@ID,'mods_'))}"/>
    <input type="hidden" name="XSL.ListKey" value="{/bibentries/@listKey}"/>
    <input type="hidden" name="XSL.PageNr" value="{/bibentries/@pageNr}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('result.dozbib.info')}" />
  </form>
</xsl:template>

<xsl:template match="mods:mods/mods:identifier[@type='duepublico']" mode="bibentry.button">
  <form action="{$ServletsBaseURL}DocumentServlet" method="get">
    <input type="hidden" name="id" value="{text()}"/>
    <input type="submit" class="roundedButton" value="{i18n:translate('result.dozbib.fulltext')}" />
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
