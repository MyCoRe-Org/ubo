<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder" 
  exclude-result-prefixes="xsl xalan i18n encoder">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="yes" xalan:indent-amount="2" />

  <xsl:param name="CurrentLang" />

  <xsl:variable name="jquery.version" select="'3.3.1'" />
  <xsl:variable name="jquery-ui.version" select="'1.12.1'" />
  <xsl:variable name="chosen.version" select="'1.8.7'" />
  <xsl:variable name="bootstrap.version" select="'4.4.1'" />
  <xsl:variable name="font-awesome.version" select="'5.5.0'" />

  <!-- ==================== IMPORTS ==================== -->
  <!-- additional stylesheets -->
  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="html-layout-backend.xsl" />


  <!-- ==================== HTML ==================== -->
  
  <xsl:template match="/html">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>

    </xsl:text>
    <html lang="{$CurrentLang}">
      <xsl:apply-templates select="head" />
      <xsl:call-template name="layout.body" />
    </html>
  </xsl:template>

  <xsl:template match="head">
    <head>
      <meta charset="utf-8" />

      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <meta http-equiv="x-ua-compatible" content="ie=edge" />

      <link href="{$WebApplicationBaseURL}rsc/sass/scss/bootstrap-ubo.css" rel="stylesheet" />
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery/{$jquery.version}/jquery.min.js"></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap/{$bootstrap.version}/js/bootstrap.bundle.min.js"></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/chosen-js/{$chosen.version}/chosen.jquery.min.js"></script>
      <link href="{$WebApplicationBaseURL}webjars/chosen-js/{$chosen.version}/chosen.min.css" rel="stylesheet" />
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery-ui/{$jquery-ui.version}/jquery-ui.js"></script>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/jquery-ui/{$jquery-ui.version}/jquery-ui.css" type="text/css"/>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/font-awesome/{$font-awesome.version}/css/all.css" type="text/css"/>
      <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans+Mono" type="text/css" />
      <link rel="apple-touch-icon-precomposed" sizes="114x114" href="https://www.uni-due.de/imperia/md/images/cms/h/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" sizes="72x72" href="https://www.uni-due.de/imperia/md/images/cms/m/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon-precomposed.png" />
      <link rel="shortcut icon" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon.png" />
      <link rel="shortcut icon" href="{$WebApplicationBaseURL}images/favicon.ico" />
      
      <script type="text/javascript">var webApplicationBaseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';</script>
      <script type="text/javascript">var currentLang = '<xsl:value-of select="$CurrentLang" />';</script>

      <xsl:copy-of select="node()" />
    </head>
  </xsl:template>


  <!-- html body -->

  <xsl:template name="layout.body">
    <body class="d-flex flex-column">
      <xsl:call-template name="layout.header" />

      <div class="container bg-white d-flex flex-column flex-grow-1">
        <div class="row mb-auto">
          <div class="col">
            <div class="row">
              <div class="col-lg-9">
                <xsl:call-template name="layout.pageTitle"/>
                <xsl:call-template name="layout.breadcrumbPath"/>
              </div>
              <div class="col-lg-3 pl-lg-0 d-flex">
                <xsl:call-template name="layout.basket.info"/>
              </div>
            </div>
            <div class="row">
              <div class="col-lg">
                <xsl:call-template name="layout.inhalt" />
              </div>
              <xsl:if test="body/aside[@id='sidebar']">
                <div class="col-lg-3 pl-lg-0">
                  <xsl:copy-of select="body/aside[@id='sidebar']" />
                </div>
              </xsl:if>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col">
            <hr class="mb-0"/>
          </div>
        </div>
        <footer class="row">
          <xsl:call-template name="layout.footer" />
        </footer>
      </div>
    </body>
  </xsl:template>

  <xsl:template name="layout.header">
    <div class="container bg-white">
      <div class="row">
          <div class="col">
            <h3 class="text-muted">
              <a href="https://www.uni-due.de/">Universität Duisburg-Essen</a>
            </h3>
          </div>
          <div class="col">
            <div class="nav nav-pills float-right">
              <xsl:call-template name="layout.login"/>
            </div>
          </div>
        </div>
    </div>

    <div class="container bg-light">
        <xsl:call-template name="layout.mainnavigation"/>
    </div>

    <div class="jumbotron m-0">
      <div class="container">
        <div class="row">
          <div class="col">
            Universitätsbibliographie
          </div>
          <div class="col">
            <span class="float-right">
              <a href="https://www.uni-due.de/" id="ude-logo" class="containsimage"><img alt="Logo" src="https://bibliographie.ub.uni-due.de/images/ude-logo.png"/></a>
            </span>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.basket.info">
    <div id="basket-info" class="card my-3 w-100">
      <div class="card-body">
	<a href="{$ServletsBaseURL}MCRBasketServlet?action=show&amp;type=bibentries">
          <span class="fas fa-bookmark mr-1" aria-hidden="true" />
          <span class="mr-1"><xsl:value-of select="i18n:translate('basket')" />:</span>
          <span class="mr-1" id="basket-info-num">
            <xsl:value-of xmlns:basket="xalan://unidue.ubo.basket.BasketUtils" select="basket:size()" />
          </span>
          <span class="mr-1"><xsl:value-of select="i18n:translate('ubo.publications')" /></span>
	</a>
      </div>
    </div>
  </xsl:template>

  <!-- page content -->

  <xsl:template name="layout.inhalt">
    <section role="main" id="inhalt">
    
      <xsl:choose>
        <xsl:when test="$allowed.to.see.this.page = 'true'">
          <xsl:copy-of select="body/*[not(@id='sidebar')][not(@id='breadcrumb')]" />
        </xsl:when>
        <xsl:otherwise>
          <h3>
            <xsl:value-of select="i18n:translate('navigation.notAllowedToSeeThisPage')" />
          </h3>
        </xsl:otherwise>
      </xsl:choose>
    </section>
  </xsl:template>

  <!-- Brotkrumen-Navigation -->

  <xsl:template name="layout.breadcrumbPath">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          <a href="https://www.uni-due.de/ub/">
            <xsl:value-of select="i18n:translate('navigation.UB')" />
          </a>
        </li>
        <li class="breadcrumb-item">
          <a href="{$WebApplicationBaseURL}">
            <xsl:value-of select="i18n:translate('navigation.Home')" />
          </a>
        </li>
        <xsl:apply-templates mode="breadcrumb"
                             select="$CurrentItem/ancestor-or-self::item[@label|label][ancestor-or-self::*=$navigation.tree[@role='main']]" />
        <xsl:for-each select="body/ul[@id='breadcrumb']/li">
          <li class="breadcrumb-item">
              <xsl:copy-of select="node()" />
          </li>
        </xsl:for-each>
      </ol>
    </nav>
  </xsl:template>

  <xsl:template match="item" mode="breadcrumb">
    <li class="breadcrumb-item">
      <xsl:call-template name="output.item.label" />
    </li>
  </xsl:template>

  <!-- current user and login formular-->
  <xsl:template name="layout.login">

    <div class="nav-item mr-2">
      <xsl:choose>
        <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
          <span class="user btn p-0" style="cursor: default;">
            [<xsl:value-of select="i18n:translate('component.user2.login.guest')" />]
          </span>
        </xsl:when>
        <xsl:otherwise>
          <a aria-expanded="false" aria-haspopup="true" data-toggle="dropdown"
             role="button" id="mcrFunctionsDropdown" href="#"
             class="user nav-link dropdown-toggle p-0" style="cursor: default;">
            <xsl:choose>
              <xsl:when test="contains($CurrentUser,'@')">
                [<xsl:value-of select="substring-before($CurrentUser,'@')" />]
              </xsl:when>
              <xsl:otherwise>
                [<xsl:value-of select="$CurrentUser" />]
              </xsl:otherwise>
            </xsl:choose>
          </a>
          <div aria-labeledby="mcrFunctionsDropdown" class="dropdown-menu">
            <xsl:call-template name="layout.usernav" />
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="orcidUser" />

    </div>
    <div class="nav-item mr-2">
      <xsl:choose>
        <xsl:when test="/webpage/@id='login'" />
        <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
          <form action="{$WebApplicationBaseURL}login.xed" method="get">
            <input type="hidden" name="url" value="{$RequestURL}" />
            <input class="btn btn-link p-0" type="submit" name="{i18n:translate('component.user2.button.login')}" value="{i18n:translate('component.user2.button.login')}" />
          </form>
        </xsl:when>
        <xsl:otherwise>
          <form action="{$ServletsBaseURL}logout" method="get">
            <input type="hidden" name="url" value="{$RequestURL}" />
            <input class="btn btn-link p-0" style="border:0;" type="submit" name="{i18n:translate('login.logOut')}" value="{i18n:translate('login.logOut')}" />
          </form>
        </xsl:otherwise>
      </xsl:choose>
    </div>
    <div class="nav-item">
      <span class="btn p-0">
        <a>
          <xsl:attribute name="href">
            <xsl:choose>
              <xsl:when test="$CurrentLang='de'">
                <xsl:call-template name="UrlSetParam">
                  <xsl:with-param name="url" select="$RequestURL" />
                  <xsl:with-param name="par" select="'lang'" />
                  <xsl:with-param name="value" select="'en'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="$CurrentLang='en'">
                <xsl:call-template name="UrlSetParam">
                  <xsl:with-param name="url" select="$RequestURL" />
                  <xsl:with-param name="par" select="'lang'" />
                  <xsl:with-param name="value" select="'de'" />
                </xsl:call-template>
              </xsl:when>
            </xsl:choose>
          </xsl:attribute>
          <img src="{$WebApplicationBaseURL}images/lang_{$CurrentLang}.gif" alt="{i18n:translate('navigation.Language')}" />
          <xsl:value-of select="i18n:translate('navigation.ende')"/>
        </a>
      </span>
    </div>

  </xsl:template>

  <!-- If current user has ORCID and we are his trusted party, display ORCID icon to indicate that -->  
  <xsl:param name="MCR.ORCID.LinkURL" />

  <xsl:template name="orcidUser">

    <xsl:variable name="orcidUser" select="orcidSession:getCurrentUser()" xmlns:orcidSession="xalan://org.mycore.orcid.user.MCRORCIDSession" />
    <xsl:variable name="userStatus" select="orcidUser:getStatus($orcidUser)" xmlns:orcidUser="xalan://org.mycore.orcid.user.MCRORCIDUser" />
    <xsl:variable name="trustedParty" select="userStatus:weAreTrustedParty($userStatus)" xmlns:userStatus="xalan://org.mycore.orcid.user.MCRUserStatus" />
    
    <xsl:if test="$trustedParty = 'true'">
      <xsl:variable name="orcid" select="orcidUser:getORCID($orcidUser)" xmlns:orcidUser="xalan://org.mycore.orcid.user.MCRORCIDUser" />
      <a href="{$MCR.ORCID.LinkURL}{$orcid}">
        <img alt="ORCID {$orcid}" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
      </a>
    </xsl:if>
  </xsl:template>

  <!-- main navigation -->

  <xsl:template name="layout.mainnavigation">
  
    <nav class="navbar navbar-expand-lg navbar-light bg-light" role="navigation" id="hauptnavigation">
      <button class="navbar-toggler ml-auto" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarSupportedContent">
        <ul class="navbar-nav" id="mainnav">
          <xsl:call-template name="layout.mainnav" />
        </ul>
      </div>
    </nav>
  </xsl:template>

  <xsl:template name="layout.pageTitle">
    <h1 id="seitentitel">
      <xsl:value-of select="head/title" disable-output-escaping="yes" />
    </h1>
  </xsl:template>
  
  <!-- Footer -->

  <xsl:template name="layout.footer">
    <div class="col-lg-2">
      <xsl:call-template name="layout.metanav" />
    </div>
    <div class="col-lg d-flex align-items-center">
      <p class="ml-lg-auto mb-0">
        <xsl:variable name="lastModified">
          <xsl:variable name="lastModified_pre">
            <xsl:apply-templates select="/html/@lastModified" />
          </xsl:variable>
          <xsl:if test="string($lastModified_pre)">
            <xsl:value-of select="concat($lastModified_pre, ' | ')"/>
          </xsl:if>
        </xsl:variable>
        <xsl:value-of select="$lastModified"/>
        <xsl:text>© Universität Duisburg-Essen | </xsl:text>
        <a href="mailto:{$MCR.Mail.Address}">
          <xsl:value-of select="$MCR.Mail.Address" />
        </a>
      </p>
    </div>
  </xsl:template>


</xsl:stylesheet>
