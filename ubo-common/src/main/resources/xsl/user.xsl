<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:orcid="xalan://org.mycore.orcid.user.MCRORCIDSession"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:const="xalan://org.mycore.user2.MCRUser2Constants"
  exclude-result-prefixes="xsl xalan i18n encoder orcid mods acl const">

<xsl:param name="error" />
<xsl:param name="url" />
<xsl:param name="step" />

<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="ServletsBaseURL" />
<xsl:param name="CurrentUser" />
<xsl:param name="UBO.LSF.Link" />
<xsl:param name="UBO.Scopus.Author.Link" />
<xsl:param name="UBO.ORCID2.InfoURL" />
<xsl:param name="MCR.ORCID2.LinkURL" />
<xsl:param name="MCR.ORCID2.OAuth.ClientSecret" select="''"/>
<xsl:param name="MCR.ORCID2.OAuth.Scope" select="''"/>

<xsl:variable name="uid">
  <xsl:value-of select="/user/@name" />
  <xsl:if test="not ( /user/@realm = 'local' )">
    <xsl:text>@</xsl:text>
    <xsl:value-of select="/user/@realm" />
  </xsl:if>
</xsl:variable>
<xsl:variable name="owns" select="document(concat('user:getOwnedUsers:',$uid))/owns" />
<xsl:variable name="isCurrentUser" select="$CurrentUser = $uid" />

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

  <article class="card mb-3" xml:lang="de">
    <div class="card-body">
      <div class="text-right mb-3">
        <div id="buttons" class="btn-group">
          <xsl:apply-templates select="." mode="actions" />
        </div>
      </div>
      <section id="steps">
        <xsl:call-template name="steps" />
      </section>
      <table class="table">
        <xsl:apply-templates select="realName" />
        <xsl:apply-templates select="eMail" />
        <xsl:apply-templates select="@name" />
        <xsl:apply-templates select="attributes/attribute[starts-with(@name, 'id_')]" />
      </table>
    </div>
  </article>

  <xsl:if test="string-length($MCR.ORCID2.OAuth.ClientSecret) &gt; 0 and $isCurrentUser">
    <xsl:call-template name="orcid" />
  </xsl:if>
  <xsl:call-template name="publications" />

</xsl:template>

  <xsl:template name="steps">
    <xsl:if test="$step = 'confirmDelete'">
      <div class="section alert alert-danger">
        <p>
          <strong>
            <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteRequest')" />
          </strong>
          <br />
          <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteExplain')" />
          <br />
          <xsl:if test="$owns/user">
            <strong>
              <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteExplainRead1')" />
              <xsl:value-of select="count($owns/user)" />
              <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteExplainRead2')" />
            </strong>
          </xsl:if>
        </p>
        <form class="pull-left" method="post" action="MCRUserServlet">
          <input name="action" value="delete" type="hidden" />
          <input name="id" value="{$uid}" type="hidden" />
          <input name="XSL.step" value="deleted" type="hidden" />
          <input value="{i18n:translate('component.user2.button.deleteYes')}" class="btn btn-danger" type="submit" />
        </form>
        <form method="get" action="MCRUserServlet">
          <input name="action" value="show" type="hidden" />
          <input name="id" value="{$uid}" type="hidden" />
          <input value="{i18n:translate('component.user2.button.cancelNo')}" class="btn btn-primary" type="submit" />
        </form>
      </div>
    </xsl:if>
    <xsl:if test="$step = 'deleted'">
      <div class="section alert alert-success alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
          <xsl:text disable-output-escaping="yes">&amp;times;</xsl:text>
        </button>
        <p>
          <strong>
            <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteConfirm')" />
          </strong>
        </p>
      </div>
    </xsl:if>
    <xsl:if test="$step = 'changedPassword'">
      <div class="section alert alert-success alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
          <xsl:text disable-output-escaping="yes">&amp;times;</xsl:text>
        </button>
        <p>
          <strong>
            <xsl:value-of select="i18n:translate('component.user2.admin.passwordChangeConfirm')" />
          </strong>
        </p>
      </div>
    </xsl:if>
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

<xsl:template name="id_orcid">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.orcid')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <xsl:variable name="url" select="concat($MCR.ORCID2.LinkURL,@value)" />
      <a href="{$url}">
        <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
        <xsl:value-of select="$url" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template name="id_lsf">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.lsf')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <a href="{$UBO.LSF.Link}{@value}">
        <xsl:value-of select="@value" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template name="id_scopus">
  <tr>
    <th scope="row">
      <xsl:value-of select="i18n:translate('user.profile.id.scopus')" />
      <xsl:text>:</xsl:text>
    </th>
    <td>
      <a href="{$UBO.Scopus.Author.Link}{@value}">
        <xsl:value-of select="@value" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[starts-with(@name, 'id_')]">
  <xsl:choose>
    <xsl:when test="@name='id_scopus'">
      <xsl:call-template name="id_scopus" />
    </xsl:when>
    <xsl:when test="@name='id_orcid'">
      <xsl:call-template name="id_orcid" />
    </xsl:when>
    <xsl:when test="@name='id_lsf'">
      <xsl:call-template name="id_lsf" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="typeOfId" select="substring-after(@name, 'id_')" />
      <tr>
        <th scope="row">
          <xsl:value-of select="i18n:translate(concat('user.profile.id.', $typeOfId))" />
          <xsl:text>:</xsl:text>
        </th>
        <td>
          <xsl:value-of select="@value" />
        </td>
      </tr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="orcid">
  <article class="card mb-3" xml:lang="de">
    <div class="card-body">
      <xsl:choose>
        <xsl:when test="attributes/attribute[starts-with(@name, 'orcid_credential')]">
          <xsl:call-template name="orcidIntegrationConfirmed" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="orcidIntegrationPending" />
        </xsl:otherwise>
      </xsl:choose>
      <p>
        <a href="{$UBO.ORCID2.InfoURL}">
          <xsl:value-of select="i18n:translate('orcid.integration.more')" />
          <xsl:text>...</xsl:text>
        </a>
      </p>
    </div>
  </article>
</xsl:template>

<xsl:template name="orcidIntegrationConfirmed">
  <h3>
    <span class="fas fa-check" aria-hidden="true" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('orcid.integration.confirmed.headline')" />
  </h3>
  <p>
    <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.confirmed.text')" />
  </p>
  <xsl:if test="string-length(normalize-space(i18n:translate('orcid.integration.import'))) &gt; 0 and
                string-length(normalize-space(i18n:translate('orcid.integration.publish'))) &gt; 0">
    <ul class="mt-1">
      <li>
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.import')" />
      </li>
      <li>
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.publish')" />
      </li>
    </ul>
  </xsl:if>
  <p>
    <a class="btn btn-danger" href="{$WebApplicationBaseURL}servlets/DozBibUserServlet">
      <xsl:value-of select="i18n:translate('orcid.integration.unlink')"/>
    </a>
  </p>
</xsl:template>

<xsl:template name="orcidIntegrationPending">
  <script src="{$WebApplicationBaseURL}js/ORCIDTools.js"/>

  <h3>
    <span class="far fa-hand-point-right mr-1" aria-hidden="true" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('orcid.integration.pending.headline')" />
  </h3>
  <p>
    <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.pending.intro')" />
  </p>

  <p>
    <button class="btn btn-primary" id="orcid-oauth-button" title="({i18n:translate('orcid.integration.popup.tooltip')})"
            onclick="ORCIDTools.orcidOAuth('{$MCR.ORCID2.LinkURL}','{$WebApplicationBaseURL}rsc/orcid/auth?scope={$MCR.ORCID2.OAuth.Scope}');">
      <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
      <xsl:value-of select="i18n:translate('orcid.oauth.link')" />
    </button>
  </p>

  <p>
    <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.pending.authorize')" />
  </p>
  <xsl:if test="string-length(normalize-space(i18n:translate('orcid.integration.import'))) &gt; 0 and
                string-length(normalize-space(i18n:translate('orcid.integration.publish'))) &gt; 0">
    <ul class="mt-1">
      <li>
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.import')" />
      </li>
      <li>
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate('orcid.integration.publish')" />
      </li>
    </ul>
  </xsl:if>
</xsl:template>

<xsl:template name="publications">
  <article class="card mb-3" xml:lang="de">
    <div class="card-body">
      <h3>
        <xsl:value-of select="i18n:translate('user.profile.publications')" />
      </h3>
      <ul>
        <xsl:call-template name="numPublicationsUBO" />
        <xsl:if test="$isCurrentUser">
          <xsl:apply-templates select="attributes[attribute[@name='token_orcid']]/attribute[@name='id_orcid']" mode="publications" />
        </xsl:if>
      </ul>
    </div>
  </article>
</xsl:template>

<xsl:template name="numPublicationsUBO">
  <xsl:variable name="connection_id" select="attributes/attribute[@name='id_connection']/@value" />
  <xsl:variable name="solr_query_confirmed" select="concat('q=status%3Aconfirmed+nid_connection%3A',$connection_id)" />
  <xsl:variable name="solr_query_all" select="concat('q=nid_connection%3A',$connection_id)" />
  <xsl:variable name="numFoundConfirmed" select="document(concat('solr:rows=0&amp;',$solr_query_confirmed))/response/result/@numFound"/>
  <xsl:variable name="numFoundAll" select="document(concat('solr:rows=0&amp;',$solr_query_all))/response/result/@numFound"/>
  <xsl:variable name="numPubsConfirmedText">
    <xsl:call-template name="numPublications">
      <xsl:with-param name="num" select="$numFoundConfirmed" />
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="numPubsAllText">
    <xsl:call-template name="numPublications">
      <xsl:with-param name="num" select="$numFoundAll" />
    </xsl:call-template>
  </xsl:variable>

  <li>
    <xsl:value-of select="concat(i18n:translate('user.profile.publications.ubo.intro'), ' ')" />
    <a href="{$ServletsBaseURL}solr/select?{$solr_query_all}&amp;sort=year+desc">
      <xsl:value-of select="$numPubsAllText"/>
      <xsl:value-of select="concat(i18n:translate('user.profile.publications.ubo.outro'), '.')" />
    </a>

    <xsl:if test="$numFoundAll &gt; 1 and $numFoundConfirmed != $numFoundAll">
      <xsl:variable name="isMulti" select="($numFoundConfirmed = 0 or $numFoundConfirmed &gt; 1)"/>

      <xsl:value-of select="concat(' ', i18n:translate(concat('user.profile.publications.ubo.published.intro.plural.', $isMulti)), ' ')"/>
      <a href="{$ServletsBaseURL}solr/select?{$solr_query_confirmed}&amp;sort=year+desc">
        <xsl:value-of select="$numPubsConfirmedText"/>
     </a>
      <xsl:value-of select="concat(' ', i18n:translate(concat('user.profile.publications.ubo.published.extro.plural.', $isMulti)))" disable-output-escaping="yes" />
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="attribute[@name='id_orcid']" mode="publications">
  <li>
    <xsl:value-of disable-output-escaping="yes" select="concat(i18n:translate('user.profile.publications.orcid.intro'), ' ')" />
    <a href="{$MCR.ORCID2.LinkURL}{@value}">
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

  <xsl:template match="user" mode="actions">
    <xsl:if test="(string-length($step) = 0) or ($step = 'changedPassword')">
      <xsl:variable name="isUserAdmin" select="acl:checkPermission(const:getUserAdminPermission())" />
      <xsl:choose>
        <xsl:when test="$isUserAdmin">
          <a class="btn btn-secondary" href="{$WebApplicationBaseURL}authorization/change-user.xed?action=save&amp;id={$uid}">
            <xsl:value-of select="i18n:translate('component.user2.admin.changedata')" />
          </a>
        </xsl:when>
        <xsl:when test="$isCurrentUser and not(/user/@locked = 'true')">
          <a class="btn btn-secondary" href="{$WebApplicationBaseURL}authorization/change-current-user.xed?action=saveCurrentUser">
            <xsl:value-of select="i18n:translate('component.user2.admin.changedata')" />
          </a>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="/user/@realm = 'local' and $isCurrentUser and not(/user/@locked = 'true')">
        <a class="btn btn-primary" href="{$WebApplicationBaseURL}authorization/change-password.xed?action=password">
          <xsl:value-of select="i18n:translate('component.user2.admin.changepw')" />
        </a>
      </xsl:if>
      <xsl:if test="/user/@realm = 'local' and not($isCurrentUser)">
        <a class="btn btn-primary" href="{$WebApplicationBaseURL}authorization/change-password.xed?action=password&amp;id={$uid}">
          <xsl:value-of select="i18n:translate('component.user2.admin.changepw')" />
        </a>
      </xsl:if>
      <xsl:if test="$isUserAdmin and not($isCurrentUser)">
        <a class="btn btn-danger" href="{$ServletsBaseURL}MCRUserServlet?action=show&amp;id={$uid}&amp;XSL.step=confirmDelete">
          <xsl:value-of select="i18n:translate('component.user2.admin.userDeleteYes')" />
        </a>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
