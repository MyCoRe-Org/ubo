<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">

  <!-- ========== ORCID status and publish button ========== -->
  <xsl:template name="orcid-status">
    <xsl:param name="mcrid" select="ancestor::mycoreobject/@ID"/>
    <div class="orcid-status" data-id="{$mcrid}"/>
  </xsl:template>

  <xsl:template name="orcid-publish">
    <xsl:param name="mcrid" select="ancestor::mycoreobject/@ID"/>
    <div class="orcid-publish d-inline" data-id="{$mcrid}"/>
  </xsl:template>
</xsl:stylesheet>
