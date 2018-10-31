<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan i18n mods encoder">

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
        <xsl:text>Mein Profil: </xsl:text>
        <xsl:value-of select="/user/@name" />
      </title>
    </head>
    <body>
      <xsl:apply-templates select="user" />
    </body>
  </html>
</xsl:template>

<xsl:template match="user">

  <xsl:if test="string-length($url) &gt; 0">
    <article class="highlight2">
      <h3>Willkommen, <xsl:value-of select="@name" /> !</h3>
      <p>
        <a href="{$url}">Weiter...</a>
      </p>
    </article>
  </xsl:if>

  <xsl:if test="string-length($error) &gt; 0">
    <article style="background-color:red; color:yellow">
      <p>Fehler bei der ORCID Autorisierung: <xsl:value-of select="$error" /></p>
      <xsl:if test="$error='access_denied'">
        <p>
          Schade, Sie haben die Universitätsbibliothek nicht als Trusted Party autorisiert.
          Wir können daher zukünftig nicht mehr Ihre Publikationsdaten mit der Universitätsbibliographie synchronisieren.
        </p>
      </xsl:if>
    </article>
  </xsl:if>

  <article class="highlight1">
    <p>
      Sie können hier Ihre persönlichen Daten einsehen.
    </p>
    <p>
      Helfen Sie uns bei der korrekten Zuordnung Ihrer Publikationen!<br/>
      Verknüpfen Sie Ihre Benutzerkennung mit Ihrer ORCID® iD, damit wir 
      Ihre Publikationslisten synchronisieren können:<br/>
      <a href="https://www.uni-due.de/ub/publikationsdienste/orcid.php">Mehr zu den Vorteilen von ORCID und Autoren-Identifikatoren...</a>
    </p>
  </article>
  <article class="highlight2">
    <table style="width:auto">
      <xsl:apply-templates select="realName" />
      <xsl:apply-templates select="eMail" />
      <xsl:apply-templates select="@name" />
      <xsl:apply-templates select="attributes/attribute[@name='id_lsf']" />
      <xsl:call-template name="orcid" />
      <xsl:apply-templates select="attributes/attribute[@name='id_scopus']" />
    </table>
  </article>
</xsl:template>

<xsl:template match="realName">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihr Name:</th>
    <td><xsl:value-of select="text()" /></td>
  </tr>
</xsl:template>

<xsl:template match="eMail">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihr E-Mail-Adresse:</th>
    <td><xsl:value-of select="text()" /></td>
  </tr>
</xsl:template>

<xsl:template match="@name">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihre Benutzerkennung:</th>
    <td>
      <xsl:value-of select="." />
      <xsl:text> [</xsl:text>
      <xsl:value-of select="translate(../@realm,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
      <xsl:text>]</xsl:text>
    </td>
  </tr>
</xsl:template>

<xsl:template name="orcid">
  <xsl:choose>
    <xsl:when test="attributes/attribute[@name='id_orcid']">
      <xsl:apply-templates select="attributes/attribute[@name='id_orcid']" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="no_orcid">
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="no_orcid">
  <tr>
    <th style="width:30%; text-align:right" scope="row">ORCID iD verknüpfen?</th>
    <td>
      <xsl:call-template name="oauth_link" />
    </td>
  </tr>
</xsl:template>

<xsl:template name="oauth_link">
  <span class="glyphicon glyphicon-hand-right" aria-hidden="true" style="margin-right:1ex;" />
  <strong>ORCID® iD verknüpfen und viele Vorteile nutzen:</strong><br/>
  <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid" />
  <a href="{$WebApplicationBaseURL}orcid">Registrieren oder verknüpfen Sie Ihre ORCID iD</a> 
  und autorisieren Sie die UB Duisburg-Essen, auf Ihr ORCID Profil zuzugreifen. Wir können dann
  <ul style="margin-top:1ex;">
    <li>
      Publikationsdaten aus Ihrem ORCID Profil importieren und regelmäßig mit der Universitätsbibliographie synchronisieren. 
    </li>
    <li>
      Publikationsdaten aus der Universitätsbibliographie in Ihrem ORCID Profil nachtragen, sofern dort nicht schon vorhanden.
      Sie erhöhen so die Sichtbarkeit Ihrer Publikationen und erhalten eine Ihnen dauerhaft eindeutig zugeordnete Publikationsliste!
    </li>
  </ul>
  <a href="https://www.uni-due.de/ub/publikationsdienste/orcid.php">Mehr zu den Vorteilen von ORCID und Autoren-Identifikatoren...</a>
</xsl:template>

<xsl:template match="attribute[@name='id_orcid']">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihre ORCID iD:</th>
    <td>
      <p>
        <xsl:variable name="url" select="concat($MCR.ORCID.LinkURL,@value)" />
        <a href="{$url}">
          <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid" />
          <xsl:value-of select="$url" />
        </a>
      </p>
      <p>
        Ihr ORCID Profil enthält 
        <strong>
          <xsl:value-of xmlns:orcid="xalan://org.mycore.orcid.user.MCRORCIDSession" select="orcid:getNumWorks()" /> Publikationen.
        </strong>
      </p>
      <p style="margin-top:2ex;">
        <xsl:choose>
          <xsl:when test="../attribute[@name='token_orcid']">
            <span class="glyphicon glyphicon-check" aria-hidden="true" />
            Sie haben die <strong>Universitätsbibliothek Duisburg-Essen als "Trusted Party" autorisiert</strong>,
            auf Ihr ORCID Profil zuzugreifen und Ihre Publikationsdaten dort mit der Universitätsbibliographie zu synchronisieren.
            Sie können diese Berechtigung jederzeit in Ihrem ORCID Profil unter [Account Settings] &gt; [Trusted organizations] widerrufen.
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="oauth_link" />
          </xsl:otherwise>
        </xsl:choose>
      </p> 
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[@name='id_lsf']">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihre LSF ID:</th>
    <td>
      <p>
        <a href="{$UBO.LSF.Link}{@value}" target="_blank">
          <xsl:value-of select="@value" />
        </a>
      </p>
      <p>
        In der Universitätsbibliographie sind Ihnen
        <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+nid_lsf:{@value}&amp;sort=year+desc">
          <xsl:value-of select="document(concat('solr:rows=0&amp;q=status:confirmed+nid_lsf:',@value))/response/result/@numFound" /> Publikationen zugeordnet.
        </a>
      </p>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute[@name='id_scopus']">
  <tr>
    <th style="width:30%; text-align:right" scope="row">Ihre Scopus Author ID:</th>
    <td>
      <a href="{$UBO.Scopus.Author.Link}{@value}" target="_blank">
        <xsl:value-of select="@value" />
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template match="attribute">
  <tr>
    <th style="width:30%; text-align:right" scope="row">
      <xsl:value-of select="@name" />:
    </th>
    <td>
      <xsl:value-of select="@value" />
    </td>
  </tr>
</xsl:template>

</xsl:stylesheet>
