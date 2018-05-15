<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl xalan i18n mods java"
>

<xsl:include href="layout.xsl" />
<xsl:include href="mods-display.xsl" />

<xsl:variable name="page.title" select="'Merkliste'" />
<xsl:variable name="PageID" select="'basket'" />

<xsl:variable name="actions">
  <xsl:for-each select="/basket[entry]">
    <div id="buttons">
      <a class="action" href="{$ServletsBaseURL}MCRBasketServlet?type=bibentries&amp;action=clear">
        <xsl:value-of select="i18n:translate('button.clear')" />
      </a>
      <xsl:choose>
        <xsl:when test="$UBO.System.ReadOnly = 'true'" />
        <xsl:when xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()">
          <a class="action" href="{$WebApplicationBaseURL}edit-contributors.xed">Personen zuordnen</a>
          <xsl:if test="count(/basket/entry) &gt; 1">
            <a class="action" href="BasketPubMerger?commit=false&amp;target=publications">Zusammenführen</a> 
            <a class="action" href="BasketPubMerger?commit=false&amp;target=hosts">Zusammenhosten</a>
          </xsl:if>
        </xsl:when>
      </xsl:choose>
      <a class="action" href="MCRExportServlet/mods.xml?basket=bibentries&amp;root=export&amp;transformer=mods">MODS</a>
      <a class="action" href="MCRExportServlet/export.bibtex?basket=bibentries&amp;root=export&amp;transformer=bibtex">BibTeX</a>
      <xsl:if xmlns:check="xalan://unidue.ubo.AccessControl" test="not(check:currentUserIsAdmin())">
        <a class="action" href="MCRExportServlet/export.endnote?basket=bibentries&amp;root=export&amp;transformer=endnote">EndNote</a>
        <a class="action" href="MCRExportServlet/export.ris?basket=bibentries&amp;root=export&amp;transformer=ris">RIS</a>
        <a class="action" href="MCRExportServlet/export.pdf?basket=bibentries&amp;root=export&amp;transformer=pdf">PDF</a>
        <a class="action" href="MCRExportServlet/export.html?basket=bibentries&amp;root=export&amp;transformer=html">HTML</a>
      </xsl:if>
    </div>
  </xsl:for-each>
</xsl:variable>

<xsl:template name="basketNumEntries">
  <div class="section">
    <xsl:if test="not(entry)">
      <xsl:attribute name="id">sectionlast</xsl:attribute>
    </xsl:if>
    <p>
      <xsl:choose>
        <xsl:when test="count(entry) = 0">
          <xsl:value-of select="i18n:translate('basket.containsNone')" />
        </xsl:when>
        <xsl:when test="count(entry) = 1">
          <xsl:value-of select="i18n:translate('basket.containsOne')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('basket.containsMany',count(*))" />
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </div>
</xsl:template>

<xsl:template name="basketEntries">
  <xsl:if test="entry">
    <div class="section" id="sectionlast">
      <xsl:apply-templates select="entry" />
    </div>
  </xsl:if>
</xsl:template>

<xsl:template match="entry">
  <div class="basketEntry">
    <xsl:attribute name="class">
      <xsl:text>basketEntry basketEntry</xsl:text>
      <xsl:value-of select="2 - (position() mod 2)" />
    </xsl:attribute>
    <div class="basketButtons">
      <xsl:call-template name="buttons" />
    </div>
    <div class="basketContent">
      <xsl:choose>
        <xsl:when test="*[not(name()='comment')]">
          <xsl:apply-templates select="*[not(name()='comment')]" mode="basketContent" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="document(@uri)/*" mode="basketContent" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="comment" mode="basketContent" />
    </div>
  </div>  
</xsl:template>

<xsl:template name="button">
  <xsl:param name="image" />
  <xsl:param name="alt" />
  <xsl:param name="action" />
  <xsl:param name="condition" select="true()" />

  <xsl:choose>
    <xsl:when test="$condition">
      <a href="MCRBasketServlet?action={$action}&amp;type={/basket/@type}&amp;id={@id}" class="roundedButton basketButton">  
        <img alt="{$alt}" src="{$WebApplicationBaseURL}images/{$image}" />
      </a>
    </xsl:when>
    <xsl:otherwise>
      <span />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="basket">

  <article class="highlight1">
    <p>
      <xsl:value-of select="i18n:translate('basket.introduction')" />
    </p>
    <xsl:call-template name="basketNumEntries" />
  </article>

  <xsl:call-template name="basketEntries" />

</xsl:template>

<xsl:template match="mycoreobject" mode="basketContent">
  <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
    <div class="labels">
      <xsl:call-template name="label-year" />
      <xsl:call-template name="pubtype" />
    </div>
    <div class="content bibentry">  
      <xsl:apply-templates select="." mode="cite"> 
        <xsl:with-param name="mode">divs</xsl:with-param> 
      </xsl:apply-templates>
      <div class="footer">
        <a class="roundedButton" href="{$ServletsBaseURL}DozBibEntryServlet?id={ancestor::mycoreobject/@ID}">
          <xsl:value-of select="i18n:translate('result.dozbib.info')" />
        </a>
      </div>
    </div>
  </xsl:for-each>
</xsl:template>

<xsl:template name="buttons">
  <xsl:call-template name="button">
    <xsl:with-param name="image">pmud-up.png</xsl:with-param>
    <xsl:with-param name="action">up</xsl:with-param>
    <xsl:with-param name="alt">Nach oben</xsl:with-param>
    <xsl:with-param name="condition" select="position() &gt; 1" />
  </xsl:call-template>
  <xsl:call-template name="button">
    <xsl:with-param name="image">pmud-down.png</xsl:with-param>
    <xsl:with-param name="action">down</xsl:with-param>
    <xsl:with-param name="alt">Nach unten</xsl:with-param>
    <xsl:with-param name="condition" select="position() != last()" />
  </xsl:call-template>
  <xsl:call-template name="button">
    <xsl:with-param name="image">remove.png</xsl:with-param>
    <xsl:with-param name="action">remove</xsl:with-param>
    <xsl:with-param name="alt">Entfernen</xsl:with-param>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
