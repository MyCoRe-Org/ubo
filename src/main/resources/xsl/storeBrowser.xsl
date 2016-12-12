<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  exclude-result-prefixes="xlink i18n xalan">

<xsl:include href="layout.xsl" />

<xsl:variable name="page.title" select="i18n:translate('storeBrowser.title')" />
<xsl:variable name="PageID"     select="robots" />

<xsl:template match="/storeBrowser">
  <div class="section">
    <p>
      <xsl:value-of select="i18n:translate('storeBrowser.text')" />
    </p>
    <p>
      <a href="{$WebApplicationBaseURL}">
        <xsl:value-of select="i18n:translate('storeBrowser.back')" />
      </a>
    </p>
  </div>
  <div class="section" id="sectionlast">
    <ul>
      <xsl:variable name="tmp">
        <xsl:for-each select="slot|object">
          <xsl:sort select="@from" data-type="number" />
          <xsl:sort select="@id" />
          <xsl:copy-of select="." />
        </xsl:for-each>
      </xsl:variable>
      <xsl:apply-templates select="xalan:nodeset($tmp)/*" />
    </ul>
  </div>
</xsl:template>

<xsl:template match="slot">
  <li>
    <a class="metavalue" href="{@path}/index.html">
      <xsl:choose>
        <xsl:when test="following::slot">
          <xsl:value-of select="i18n:translate('storeBrowser.from')" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="@from" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="i18n:translate('storeBrowser.to')" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="following::slot[1]/@from - 1" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('storeBrowser.beginning')" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="@from" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </li>
</xsl:template>

<xsl:template match="object">
  <xsl:variable name="bibentryID" select="number(substring-after(@id,'ubo_'))" />
  <xsl:call-template name="object.link">
    <xsl:with-param name="id" select="$bibentryID" />
    <xsl:with-param name="url" select="concat($ServletsBaseURL,'DozBibEntryServlet?mode=show&amp;id=',$bibentryID)" />
  </xsl:call-template>
</xsl:template>

<xsl:template name="object.link">
  <xsl:param name="id" />
  <xsl:param name="url" />

  <li>
    <a href="{$url}">
      <xsl:value-of select="i18n:translate('storeBrowser.object')" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="$id" />
      <xsl:text>, </xsl:text>
      <xsl:value-of select="i18n:translate('storeBrowser.lastModified')" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="@lastModified" />
    </a>
  </li>
</xsl:template>

</xsl:stylesheet>
