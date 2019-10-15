<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:orcid="xalan://org.mycore.orcid.user.MCRORCIDSession"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan i18n encoder orcid mods">

<xsl:param name="error" />

<xsl:param name="CurrentLang" />
<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="UBO.Build.TimeStamp" select="''" />
<xsl:param name="MCR.ORCID.LinkURL" />

<xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="yes" xalan:indent-amount="2" />

<xsl:template match="/">
  <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>

  </xsl:text>
  <html lang="{$CurrentLang}">
    <head>
      <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans+Mono" type="text/css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}external/jquery-ui-theme/jquery-ui-1.8.21.custom.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/style.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/legacy.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/duepublico.css?v={$UBO.Build.TimeStamp}" />
      <title>
        <xsl:value-of select="i18n:translate('orcid.integration.popup.title')" />
      </title>
    </head>
    <body>
      <xsl:apply-templates select="user" />
    </body>
  </html>
</xsl:template>

<xsl:template match="user">

  <script type="text/javascript">
    function closeWindowAndReloadProfilePage() {
      window.opener.location.reload(); 
      window.close();
    }
  </script>

  <article class="highlight1">
    <xsl:choose>
      <xsl:when test="string-length($error) &gt; 0">
        <xsl:call-template name="orcidIntegrationRejected" />
      </xsl:when>
      <xsl:when test="attributes/attribute[@name='token_orcid']">
        <xsl:call-template name="orcidIntegrationConfirmed" />
      </xsl:when>
    </xsl:choose>
    <p>
      <button id="orcid-oauth-button" onclick="closeWindowAndReloadProfilePage();">
        <xsl:value-of select="i18n:translate('orcid.integration.popup.close')" />
      </button>
    </p>
  </article>
  
</xsl:template>

<xsl:template name="orcidIntegrationRejected">
  <h3 style="margin-bottom: 0.5em;">
    <xsl:value-of select="i18n:translate('orcid.integration.rejected.headline')" />
  </h3>
  <p style="background-color:red; color:yellow; padding:0 1ex 0 1ex;">
    <xsl:choose>
      <xsl:when test="$error='access_denied'">
        <xsl:value-of select="i18n:translate('orcid.integration.rejected.denied')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="i18n:translate('orcid.integration.rejected.error')" />
        <xsl:text> :</xsl:text>
        <xsl:value-of select="$error" />
      </xsl:otherwise>
    </xsl:choose>
  </p>
</xsl:template>

<xsl:template name="orcidIntegrationConfirmed">
  <h3 style="margin-bottom: 0.5em;">
    <span class="fas fa-check" aria-hidden="true" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('orcid.integration.confirmed.thanks')" />
    <xsl:text>, </xsl:text>
    <xsl:value-of select="i18n:translate('orcid.integration.confirmed.headline')" />
  </h3>
  <p>
    <xsl:value-of select="i18n:translate('orcid.integration.confirmed.text')" />
  </p>
  <ul style="margin-top:1ex;">
    <li>
      <xsl:value-of select="i18n:translate('orcid.integration.import')" />
    </li>
    <li>
      <xsl:value-of select="i18n:translate('orcid.integration.publish')" />
    </li>
  </ul>
</xsl:template>

</xsl:stylesheet>
