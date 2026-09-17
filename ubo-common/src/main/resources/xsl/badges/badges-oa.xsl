<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="encoder i18n mods xsl">

  <xsl:import href="xslImport:badges:badges/badges-oa.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:choose>
      <xsl:when test="mods:classification[contains(@authorityURI,'oa')]">
        <xsl:apply-templates select="mods:classification[contains(@authorityURI,'oa')]" mode="label-info" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:classification[contains(@authorityURI,'oa')]" mode="label-info" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'oa')]" mode="label-info">
    <xsl:variable name="category" select="$oa//category[@ID=substring-after(current()/@valueURI,'#')]" />
    <span class="badge oa-badge oa-badge-{$category/@ID} ubo-hover-pointer mr-1" onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+oa_exact:', $category/@ID))}')">
      <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
    </span>
  </xsl:template>
</xsl:stylesheet>
