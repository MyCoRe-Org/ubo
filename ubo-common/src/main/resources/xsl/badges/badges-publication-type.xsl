<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="encoder i18n mods xsl">

  <xsl:import href="xslImport:badges:badges/badges-publication-type.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:variable name="genre" select="substring-after(mods:genre[@type='intern']/@valueURI, '#')"/>

    <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer" title="{i18n:translate('ubo.genre')}"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+genre:&quot;', $genre, '&quot;'))}')">

      <xsl:apply-templates select="mods:genre[@type='intern']"/>

      <xsl:for-each select="mods:relatedItem[@type='host']/mods:genre[@type='intern']">
        <xsl:text> in </xsl:text>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </span>
  </xsl:template>
</xsl:stylesheet>
