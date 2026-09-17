<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="encoder i18n mcrxml mods xsl">

  <xsl:import href="xslImport:badges:badges/badges-orcid.xsl"/>
  <xsl:param name="CurrentLang"/>
  <xsl:param name="MCR.ORCID2.OAuth.ClientSecret" select="''"/>
  <xsl:param name="MCR.ORCID2.OAuth.Scope" select="''"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:if test="string-length($MCR.ORCID2.OAuth.ClientSecret) &gt; 0 and contains($MCR.ORCID2.OAuth.Scope,'update') and not(mcrxml:isCurrentUserGuestUser())">
      <xsl:variable name="publication-connection-ids">
        <xsl:for-each select=".//mods:nameIdentifier[@type='connection']">
          <xsl:value-of select="concat(., ' ')"/>
        </xsl:for-each>
      </xsl:variable>

      <xsl:if test="contains($publication-connection-ids, $current-user-connection-id)">
        <span class="orcid-status" data-id="{ancestor::mycoreobject/@ID}"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>
 </xsl:stylesheet>
