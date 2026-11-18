<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:orcidUtils="xalan://org.mycore.ubo.orcid.DozBibORCIDUtils"
                exclude-result-prefixes="orcidUtils xsl">

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="MCR.ORCID2.LinkURL"/>
  <xsl:param name="current-user" select="document('notnull:user:current')/user"/>

  <!-- ========== ORCID status and publish button ========== -->
  <xsl:variable name="current-user-connection-id" select="$current-user/attributes/attribute[@name='id_connection']/@value"/>

  <xsl:template name="orcid-status">
    <xsl:param name="mycore-id" select="str[@name='id']"/>
    <xsl:param name="publication-connection-ids" select="arr[@name='nid_connection']/str/text()" />

    <xsl:if test="contains($publication-connection-ids, $current-user-connection-id)">
      <div class="orcid-status" data-id="{$mycore-id}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="orcid-publish">
    <xsl:param name="mycore-id" select="str[@name='id']"/>
    <xsl:param name="publication-connection-ids" select="arr[@name='nid_connection']/str/text()" />

    <xsl:if test="contains($publication-connection-ids, $current-user-connection-id)">
      <div class="orcid-publish d-inline" data-id="{$mycore-id}"/>
    </xsl:if>
  </xsl:template>

  <!-- If current user has ORCID, and we are his trusted party, display ORCID icon to indicate that -->
  <xsl:template name="orcidUser">
    <xsl:if test="orcidUtils:weAreTrustedParty() = 'true'">
      <a href="{$MCR.ORCID2.LinkURL}{orcidUtils:getFirstOrcidByCurrentUser()}">
        <img alt="ORCID" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon"/>
      </a>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
