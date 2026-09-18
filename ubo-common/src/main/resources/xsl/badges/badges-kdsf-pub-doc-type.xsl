<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:check="xalan://org.mycore.ubo.AccessControl"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="check i18n mcrxsl mods xsl">

  <xsl:import href="xslImport:badges:badges/badges-kdsf-pub-doc-type.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:if test="check:currentUserIsAdmin()">
      <xsl:variable name="kdsf-pubtype" select="mods:classification[@generator='xpathmapping2kdsfPublicationType-mycore']"/>
      <xsl:for-each select="$kdsf-pubtype">
        <span class="label-info badge bg-warning text-white me-1" title="{i18n:translate('ubo.publication.type.kdsf')}">
          <xsl:variable name="categid" select="substring-after(@valueURI, '#')"/>
          <xsl:value-of select="mcrxsl:getDisplayName('kdsfPublicationType', $categid)"/>
        </span>
      </xsl:for-each>

      <xsl:variable name="kdsf-doctype" select="mods:classification[@generator='xpathmapping2kdsfDocumentType-mycore']"/>
      <xsl:for-each select="$kdsf-doctype">
        <span class="label-info badge bg-info text-white me-1" title="{i18n:translate('ubo.document.type.kdsf')}">
          <xsl:variable name="categid" select="substring-after(@valueURI, '#')"/>
          <xsl:value-of select="mcrxsl:getDisplayName('kdsfDocumentType', $categid)"/>
        </span>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
