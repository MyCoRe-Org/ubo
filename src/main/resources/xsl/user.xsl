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
<xsl:param name="url" />

<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="ServletsBaseURL" />
<xsl:param name="UBO.LSF.Link" />
<xsl:param name="UBO.Scopus.Author.Link" />
<xsl:param name="MCR.ORCID.LinkURL" />

<xsl:template match="/">
  <html id="profile">
    <head>
      <title>
        <xsl:value-of select="i18n:translate('user.profile')" />
        <xsl:text>: </xsl:text>
        <xsl:value-of select="/user/@name" />
      </title>
    </head>
    <body>
      <xsl:apply-templates select="user" />
    </body>
  </html>
</xsl:template>

<xsl:template match="user">

  <article class="highlight2">
    <table class="userProfile">
      <xsl:apply-templates select="realName" />
      <xsl:apply-templates select="eMail" />
      <xsl:apply-templates select="@name" />
      <xsl:apply-templates select="attributes/attribute[@name='id_lsf']" />
      <xsl:apply-templates select="attributes/attribute[@name='id_orcid']" />
      <xsl:apply-templates select="attributes/attribute[@name='id_scopus']" />
    </table>
  </article>

  <xsl:call-template name="orcid" />
  <xsl:call-template name="publications" />

</xsl:template>

<xsl:template match="realName">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.realName')" />
      <xsl:text>:</xsl:text>
    </th>
    <td><xsl:value-of select="text()" /></td>
  </tr>
</xsl:template>

<xsl:template match="eMail">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.eMail')" />
      <xsl:text>:</xsl:text>
    </th>
    <td><xsl:value-of select="text()" /></td>
  </tr>
</xsl:template>

<xsl:template match="@name">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.account')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <xsl:value-of select="." />
      <xsl:text> [</xsl:text>
      <xsl:value-of select="translate(../@realm,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
      <xsl:text>]</xsl:text>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[@name='id_orcid']">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.orcid')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <xsl:variable name="url" select="concat($MCR.ORCID.LinkURL,@value)" />
      <a href="{$url}">
        <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
        <xsl:value-of select="$url" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[@name='id_lsf']">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.lsf')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <a href="{$UBO.LSF.Link}{@value}" target="_blank">
        <xsl:value-of select="@value" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[@name='id_scopus']">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.scopus')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <a href="{$UBO.Scopus.Author.Link}{@value}" target="_blank">
        <xsl:value-of select="@value" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template name="orcid">
  <article class="highlight1">
    <xsl:choose>
      <xsl:when test="string-length($error) &gt; 0">
        <xsl:call-template name="orcidIntegrationRejected" />
      </xsl:when>
      <xsl:when test="attributes/attribute[@name='token_orcid']">
        <xsl:call-template name="orcidIntegrationConfirmed" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="orcidIntegrationPending" />
      </xsl:otherwise>
    </xsl:choose>
    <p>
      <a href="https://www.uni-due.de/ub/publikationsdienste/orcid.php">
        <xsl:value-of select="i18n:translate('orcid.integration.more')" />
        <xsl:text>...</xsl:text>
      </a>
    </p>
  </article>
</xsl:template>

<xsl:template name="orcidIntegrationRejected">
  <xsl:attribute name="style">background-color:red; color:yellow</xsl:attribute>
  <h3 style="margin-bottom: 0.5em;">
    <xsl:value-of select="i18n:translate('orcid.integration.rejected.headline')" />
  </h3>
  <p>
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
  <p>
    <xsl:value-of select="i18n:translate('orcid.integration.rejected.retry')" />
  </p>
  <xsl:call-template name="orcidOAuthLink" />
</xsl:template>

<xsl:template name="orcidIntegrationConfirmed">
  <h3 style="margin-bottom: 0.5em;">
    <span class="glyphicon glyphicon-check" aria-hidden="true" />
    <xsl:text> </xsl:text>
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

<xsl:template name="orcidIntegrationPending">
  <h3 style="margin-bottom: 0.5em;">
    <span class="glyphicon glyphicon-hand-right" aria-hidden="true" style="margin-right:1ex;" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('orcid.integration.pending.headline')" />
  </h3>
  <p>
    <xsl:value-of select="i18n:translate('orcid.integration.pending.intro')" />
  </p>
  <xsl:call-template name="orcidOAuthLink" />
  <p>
    <xsl:value-of select="i18n:translate('orcid.integration.pending.authorize')" />
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

<xsl:template name="orcidOAuthLink">
  <a href="{$WebApplicationBaseURL}orcid" class="orcidAuthLink">
    <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
    <xsl:value-of select="i18n:translate('orcid.oauth.link')" />
  </a>
</xsl:template>

<xsl:template name="publications">
  <article class="highlight2">
    <h3 style="margin-bottom: 0.5em;">
      <xsl:value-of select="i18n:translate('user.profile.publications')" />
    </h3>
    <ul>
      <xsl:call-template name="numPublicationsUBO" />
      <xsl:apply-templates select="attributes[attribute[@name='token_orcid']]/attribute[@name='id_orcid']" mode="publications" />
    </ul>
  </article>
</xsl:template>

<xsl:template name="numPublicationsUBO">
  <xsl:variable name="lsf_id" select="attributes/attribute[@name='id_lsf']/@value" />
  <xsl:variable name="solr_query" select="concat('q=status:confirmed+nid_lsf:',$lsf_id)" />
  
  <li>
    <xsl:value-of select="i18n:translate('user.profile.publications.ubo.intro')" />
    <xsl:text> </xsl:text>
    <a href="{$ServletsBaseURL}solr/select?{$solr_query}&amp;sort=year+desc">
      <xsl:call-template name="numPublications">
        <xsl:with-param name="num" select="document(concat('solr:rows=0&amp;',$solr_query))/response/result/@numFound" /> 
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('user.profile.publications.ubo.outro')" />
      <xsl:text>.</xsl:text>
    </a>
  </li>
</xsl:template>

<xsl:template match="attribute[@name='id_orcid']" mode="publications">
  <li>
    <xsl:value-of select="i18n:translate('user.profile.publications.orcid.intro')" />
    <xsl:text> </xsl:text>
    <a href="{$MCR.ORCID.LinkURL}{@value}">
      <xsl:call-template name="numPublications">
        <xsl:with-param name="num" select="orcid:getNumWorks()" /> 
      </xsl:call-template>
      <xsl:text>.</xsl:text>
    </a>
  </li>
</xsl:template>

<xsl:template name="numPublications">
  <xsl:param name="num" />
  <xsl:choose>
    <xsl:when test="$num = 0">
      <xsl:value-of select="i18n:translate('user.profile.publications.num.none')" />
    </xsl:when>
    <xsl:when test="$num = 1">
      <xsl:value-of select="i18n:translate('user.profile.publications.num.one')" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="i18n:translate('user.profile.publications.num.multiple',$num)" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
