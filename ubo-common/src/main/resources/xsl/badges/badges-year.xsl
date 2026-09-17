<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="encoder i18n mods xsl">

  <xsl:import href="xslImport:badges:badges/badges-year.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:choose>
      <xsl:when test="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[@type='host'])][1]">
        <xsl:for-each select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[@type='host'])][1]">
          <xsl:apply-templates select="." mode="label-year-badge"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="descendant-or-self::mods:dateIssued[(ancestor::mods:relatedItem[(@type='host')])][1]">
          <xsl:apply-templates select="." mode="label-year-badge"/>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:dateIssued" mode="label-year-badge">
    <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer" title="{i18n:translate('ubo.search.year')}"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+year:', text()))}')">
      <xsl:value-of select="text()"/>
    </span>
  </xsl:template>
</xsl:stylesheet>
