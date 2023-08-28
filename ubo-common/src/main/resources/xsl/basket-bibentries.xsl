<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl xalan i18n mods java"
>

<xsl:include href="mods-display.xsl" />
<xsl:include href="coreFunctions.xsl" />
<xsl:include href="csl-export-gui.xsl" />


<xsl:param name="UBO.System.ReadOnly" />

<xsl:template match="/">
  <html id="basket">
    <head>
      <title>Merkliste</title>
      <script src="{$WebApplicationBaseURL}modules/orcid2/js/orcid-auth.js"/>
      <script src="{$WebApplicationBaseURL}js/mycore2orcid.js" />
    </head>
    <body>
      <xsl:call-template name="actions" />
      <xsl:apply-templates select="basket" />
    </body>
  </html>
</xsl:template>

<xsl:template name="actions">
  <xsl:for-each select="/basket[entry]">
    <div id="buttons" class="btn-group mb-3 flex-wrap">
      <a class="action btn btn-sm btn-primary mb-1" href="{$ServletsBaseURL}MCRBasketServlet?type=objects&amp;action=clear">
        <xsl:value-of select="i18n:translate('button.clear')" />
      </a>
      <xsl:choose>
        <xsl:when test="$UBO.System.ReadOnly = 'true'" />
        <xsl:when xmlns:check="xalan://org.mycore.ubo.AccessControl" test="check:currentUserIsAdmin()">
          <a class="action btn btn-sm btn-primary mb-1" href="{$WebApplicationBaseURL}edit-contributors.xed">Personen zuordnen</a>
        </xsl:when>
      </xsl:choose>
    </div>
    <xsl:call-template name="exportGUI">
      <xsl:with-param name="type" select="'basket'" />
    </xsl:call-template>
  </xsl:for-each>
</xsl:template>

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
    <xsl:apply-templates select="entry" />
  </xsl:if>
</xsl:template>

<xsl:template match="entry">
  <div>
    <xsl:attribute name="class">
      <xsl:text>card border-top-0 rounded-0 bg-card-body</xsl:text>
      <xsl:value-of select="2 - (position() mod 2)" />
    </xsl:attribute>
    <div class="card-body p-3">
      <xsl:choose>
        <xsl:when test="*[not(name()='comment')]">
          <xsl:apply-templates select="*[not(name()='comment')]" mode="basketContent">
            <xsl:with-param name="position" select="position()"/>
            <xsl:with-param name="last" select="last()" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="document(@uri)/*" mode="basketContent">
            <xsl:with-param name="position" select="position()"/>
            <xsl:with-param name="last" select="last()" />
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="comment" mode="basketContent">
        <xsl:with-param name="position" select="position()"/>
        <xsl:with-param name="last" select="last()" />
      </xsl:apply-templates>
    </div>
  </div>
</xsl:template>

<xsl:template name="button">
  <xsl:param name="image" />
  <xsl:param name="alt" />
  <xsl:param name="action" />
  <xsl:param name="condition" select="true()" />
  <xsl:param name="ancestor_id"/>

  <xsl:choose>
    <xsl:when test="$condition">
      <a href="MCRBasketServlet?action={$action}&amp;type={/basket/@type}&amp;id={$ancestor_id}" class="btn btn-sm btn-primary ml-1">
        <img alt="{$alt}" src="{$WebApplicationBaseURL}images/{$image}" />
      </a>
    </xsl:when>
    <xsl:otherwise>
      <span />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="basket">

  <article class="card bg-alternative rounded-0">
    <div class="card-body">
      <p>
        <xsl:value-of select="i18n:translate('basket.introduction')" />
      </p>
      <xsl:call-template name="basketNumEntries" />
    </div>
  </article>

  <xsl:call-template name="basketEntries" />

</xsl:template>

<xsl:template match="mycoreobject" mode="basketContent">
  <xsl:param name="position"/>
  <xsl:param name="last"/>
  <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">

    <div class="row">
      <div class="col">
        <xsl:call-template name="label-year" />
        <xsl:call-template name="pubtype" />
        <xsl:call-template name="orcid-status" />
      </div>
      <div class="col">
        <xsl:call-template name="buttons">
          <xsl:with-param name="position" select="$position"/>
          <xsl:with-param name="last" select="$last"/>
          <xsl:with-param name="ancestor_id" select="ancestor::mycoreobject/@ID"/>
        </xsl:call-template>
      </div>
    </div>

    <div class="content bibentry">
      <xsl:apply-templates select="." mode="cite"> 
        <xsl:with-param name="mode">divs</xsl:with-param> 
      </xsl:apply-templates>
      <div class="footer">
        <a class="btn btn-sm btn-primary" href="{$ServletsBaseURL}DozBibEntryServlet?id={ancestor::mycoreobject/@ID}">
          <xsl:value-of select="i18n:translate('result.dozbib.info')" />
        </a>
      </div>
    </div>
  </xsl:for-each>
</xsl:template>

<xsl:template name="buttons">
  <xsl:param name="position"/>
  <xsl:param name="last"/>
  <xsl:param name="ancestor_id"/>

  <div class="buttons float-right">
    <xsl:call-template name="button">
      <xsl:with-param name="image">pmud-up.png</xsl:with-param>
      <xsl:with-param name="action">up</xsl:with-param>
      <xsl:with-param name="alt">Nach oben</xsl:with-param>
      <xsl:with-param name="condition" select="$position &gt; 1" />
      <xsl:with-param name="ancestor_id" select="$ancestor_id" />
    </xsl:call-template>
    <xsl:call-template name="button">
      <xsl:with-param name="image">pmud-down.png</xsl:with-param>
      <xsl:with-param name="action">down</xsl:with-param>
      <xsl:with-param name="alt">Nach unten</xsl:with-param>
      <xsl:with-param name="condition" select="$position != $last" />
      <xsl:with-param name="ancestor_id" select="$ancestor_id" />
    </xsl:call-template>
    <xsl:call-template name="button">
      <xsl:with-param name="image">remove.png</xsl:with-param>
      <xsl:with-param name="action">remove</xsl:with-param>
      <xsl:with-param name="alt">Entfernen</xsl:with-param>
      <xsl:with-param name="ancestor_id" select="$ancestor_id" />
    </xsl:call-template>
  </div>
</xsl:template>

</xsl:stylesheet>
