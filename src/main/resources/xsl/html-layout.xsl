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
  <xsl:variable name="bootstrap.version" select="'4.1.3'" />
  <xsl:variable name="font-awesome.version" select="'5.5.0'" />


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
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/bootstrap-glyphicons/bdd2cbfba0/css/bootstrap-glyphicons.css" type="text/css"/>
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

  <!-- id of the current page -->
  <xsl:variable name="PageID" select="/html/@id" />

  <!-- Parameters of MyCoRe LayoutService -->

  <xsl:param name="CurrentUser" />
  <xsl:param name="DefaultLang" />
  <xsl:param name="WritePermission" />
  <xsl:param name="ReadPermission" />
  <xsl:param name="UBO.System.ReadOnly" />
  <xsl:param name="UBO.Build.TimeStamp" select="''" />

  <xsl:param name="MCR.Users.Guestuser.UserName" />
  <xsl:param name="MCR.Mail.Address" />

  <!-- additional stylesheets -->
  <xsl:include href="coreFunctions.xsl" />

  <!-- last navigation id calculated from navigation.xml -->
  <xsl:variable name="NavigationID" xmlns:lastPageID="xalan://unidue.ubo.LastPageID">
    <xsl:choose>
      <xsl:when test="(string-length($PageID) &gt; 0) and ($navigation.tree/descendant-or-self::item[@id = $PageID])">
        <xsl:value-of select="lastPageID:setLastPageID($PageID)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="LastID" select="lastPageID:getLastPageID()" />
        <xsl:choose>
          <xsl:when test="(string-length($LastID) &gt; 0) and ($navigation.tree/descendant-or-self::item[@id = $LastID])">
            <xsl:value-of select="$LastID" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="lastPageID:setLastPageID('home')" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- load navigation from navigation.xml -->
  <xsl:variable name="navigation.tree" select="document('webapp:navigation.xml')/navigation" />

  <xsl:variable name="CurrentItem" select="$navigation.tree/descendant-or-self::item[@id = $NavigationID]" />
  
  <!-- true if current user is the guest user -->
  <xsl:variable name="isGuest" select="$CurrentUser = $MCR.Users.Guestuser.UserName"/>

  <!-- html body -->

  <xsl:template name="layout.body">
    <body class="d-flex flex-column" style="height: 100%;">
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
          <span class="glyphicon glyphicon-bookmark mr-1" aria-hidden="true" />
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

  <!-- ========== Prüfe auf Berechtigung des Benutzers ======== -->
  
  <xsl:variable name="allowed.to.see.this.page">
    <xsl:for-each select="$CurrentItem">
      <xsl:choose>
        <xsl:when test="ancestor-or-self::item[@role]">
          <xsl:for-each select="(ancestor-or-self::item[@role])[last()]">
            <xsl:call-template name="check.allowed" />
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>true</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:variable>
  
  <xsl:template name="check.allowed">
    <xsl:choose> 
      <xsl:when test="not(@role)">true</xsl:when>
      <xsl:when xmlns:check="xalan://org.mycore.common.xml.MCRXMLFunctions" test="check:isCurrentUserInRole(@role)">true</xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Colorbox -->

  <xsl:template name="layout.colorbox">
    <div style="display: none;" id="cboxOverlay" />
    <div id="colorbox" style="padding-bottom: 57px; padding-right: 28px; display: none;">
      <div id="cboxWrapper">
        <div style="">
          <div style="float: left;" id="cboxTopLeft" />
          <div style="float: left;" id="cboxTopCenter" />
          <div style="float: left;" id="cboxTopRight" />
        </div>
        <div style="clear: left;">
          <div style="float: left;" id="cboxMiddleLeft" />
          <div style="float: left;" id="cboxContent">
            <div style="width: 0pt; height: 0pt; overflow: hidden;" id="cboxLoadedContent" />
            <div id="cboxLoadingOverlay" />
            <div id="cboxLoadingGraphic" />
            <div id="cboxTitle" />
            <div id="cboxCurrent" />
            <div id="cboxNext" />
            <div id="cboxPrevious" />
            <div id="cboxSlideshow" />
            <div id="cboxClose" />
          </div>
          <div style="float: left;" id="cboxMiddleRight" />
        </div>
        <div style="clear: left;">
          <div style="float: left;" id="cboxBottomLeft" />
          <div style="float: left;" id="cboxBottomCenter" />
          <div style="float: left;" id="cboxBottomRight" />
        </div>
      </div>
      <div style="position: absolute; width: 9999px; visibility: hidden; display: none;" />
    </div>
  </xsl:template>

  <!-- Skip-Navigation -->

  <xsl:template name="layout.skip">
    <ul id="skip">
      <li><a href="#inhalt">Inhalt</a></li>
      <li><a href="#zielgruppen">Zielgruppeneinstieg</a></li>
      <li><a href="#hauptnavigation">Hauptnavigation</a></li>
    </ul>
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

  <!-- Ausgabe Link und Bezeichnung des Menüpunktes -->

  <xsl:template name="output.item.label">
    <xsl:choose>

      <!-- ==== link to external url ==== -->
      <xsl:when test="@href">
        <a class="nav-link" href="{@href}">
          <xsl:copy-of select="@target" />
          <xsl:call-template name="output.label.for.lang" />
        </a>
      </xsl:when>

      <!-- ==== link to internal url ==== -->
      <xsl:when test="@ref">
        <a class="nav-link" href="{$WebApplicationBaseURL}{@ref}">
          <xsl:copy-of select="@target" />
          <xsl:call-template name="output.label.for.lang" />
        </a>
      </xsl:when>

      <!-- no link -->
      <xsl:otherwise>
        <xsl:call-template name="output.label.for.lang" />
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <!-- print label in current language -->

  <xsl:template name="output.label.for.lang">
    <xsl:choose>
      <xsl:when test="label[lang($CurrentLang)]">
        <xsl:value-of select="label[lang($CurrentLang)]" />
      </xsl:when>
      <xsl:when test="label[lang($DefaultLang)]">
        <xsl:value-of select="label[lang($DefaultLang)]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@label" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- current user and login formular-->
  <xsl:template name="layout.login">

    <div class="nav-item mr-2">
      <span class="user btn p-0" style="cursor: default;">
        <xsl:text>[ </xsl:text>
        <xsl:choose>
          <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
            <xsl:value-of select="i18n:translate('component.user2.login.guest')" />
          </xsl:when>
          <xsl:when test="contains($CurrentUser,'@')">
            <a href="{$ServletsBaseURL}MCRUserServlet?action=show" style="text-decoration: none;">
              <xsl:value-of select="substring-before($CurrentUser,'@')" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$CurrentUser" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text> ]</xsl:text>

        <xsl:call-template name="orcidUser" />        
      </span>
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
          <!-- Find the item that is the root of the navigation tree to display -->
          <xsl:for-each select="$navigation.tree">
            <xsl:apply-templates select="item[@label|label]" mode="navigation" />
          </xsl:for-each>
        </ul>
      </div>
    </nav>
  </xsl:template>



  <!-- Menüpunkt im Navigationsmenü -->
  <xsl:template match="item" mode="navigation">

    <!-- ==== Prüfe ob aktueller Benutzer berechtigt ist, diesen Menüpunkt zu sehen ==== -->
    <xsl:variable name="allowed">
      <xsl:call-template name="check.allowed" />
    </xsl:variable>

    <!-- add class="active for selected items" -->
    <xsl:variable name="class_active">
      <xsl:if test="@id and (@id = $PageID)">
        <xsl:value-of select="'active'"/>
      </xsl:if>
      <xsl:if test="./item[@id = $PageID]">
        <xsl:value-of select="'active'"/>
      </xsl:if>
    </xsl:variable>

    <xsl:if test="$allowed = 'true'">

      <xsl:choose>
        <xsl:when test="count(./item/@ref) &gt; 0">
          <!-- print dropdown menu option -->
          <li class="nav-item dropdown {$class_active}">
            <xsl:if test="@class != ''">
              <xsl:attribute name="class">
                <xsl:value-of select="concat('nav-item dropdown', $class_active, ' ', @class)"/>
              </xsl:attribute>
            </xsl:if>
            <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown"
               aria-haspopup="true" aria-expanded="false">
              <xsl:call-template name="output.label.for.lang" />
            </a>
            <div class="dropdown-menu" aria-labelledby="navbarDropdown">
              <xsl:for-each select="./item">
                <a class="dropdown-item" href="{@ref}">
                  <xsl:copy-of select="@target" />
                  <xsl:call-template name="output.label.for.lang" />
                </a>
              </xsl:for-each>
            </div>
          </li>
        </xsl:when>
        <xsl:otherwise>
          <!-- print single menu option -->
          <li class="nav-item {$class_active}">
            <xsl:if test="@class != ''">
              <xsl:attribute name="class">
                <xsl:value-of select="concat('nav-item ', $class_active, ' ', @class)"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:call-template name="output.item.label"/>
          </li>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

  </xsl:template>

  <!-- Master Head -->

  <xsl:template name="layout.mastHead">
    <div class="col">
      <h2>
        <a href="{$WebApplicationBaseURL}">Universitätsbibliographie</a>
      </h2>
    </div>
    <div class="col text-right p-0">
      <a class="containsimage" id="ude-logo" href="https://www.uni-due.de/">
        <img src="{$WebApplicationBaseURL}images/ude-logo.png" alt="Logo" />
      </a>
    </div>
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
        <xsl:apply-templates select="/html/@lastModified" />
        <xsl:text>© Universität Duisburg-Essen | </xsl:text>
        <a href="mailto:{$MCR.Mail.Address}">
          <xsl:value-of select="$MCR.Mail.Address" />
        </a>
      </p>
    </div>
  </xsl:template>

  <!-- Optional: Datum der letzten Änderung dieser Seite im Format YYYY-MM-TT -->

  <xsl:template match="/html/@lastModified">
    <xsl:value-of select="i18n:translate('webpage.modified')" />
    <xsl:text>: </xsl:text>
    <xsl:choose>
      <xsl:when test="$CurrentLang = 'de'">
        <xsl:value-of select="substring(.,9,2)" />
        <xsl:text>.</xsl:text>
        <xsl:value-of select="substring(.,6,2)" />
        <xsl:text>.</xsl:text>
        <xsl:value-of select="substring(.,1,4)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="." />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> | </xsl:text>
  </xsl:template>

  <!-- Meta-Navigation -->

  <xsl:template name="layout.metanav">
    <xsl:variable name="metanavigation" select="$navigation.tree/item[@role='meta']/item"/>
    <nav class="navbar navbar navbar-expand">
      <ul class="navbar-nav">
        <!-- Find the item that is the root of the navigation tree to display -->
        <xsl:for-each select="$metanavigation" >
          <xsl:choose>
            <!-- There is an item that should be displayed as root of the tree -->
            <xsl:when test="name()='item'">
              <xsl:apply-templates select="." mode="navigation" />
            </xsl:when>
            <!-- Display the complete navigation tree down from top -->
            <xsl:otherwise>
              <xsl:apply-templates select="item[@label|label]" mode="navigation" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </ul>
    </nav>
  </xsl:template>

  <!-- print out message that the system is in read only mode -->

  <xsl:param name="UBO.System.ReadOnly.Message" />
  <xsl:template name="local.readonly.message">
    <xsl:if test="($WritePermission = 'true') and ($UBO.System.ReadOnly = 'true')">
      <div class="section">
        <span style="color:red">
          <xsl:value-of select="$UBO.System.ReadOnly.Message" />
        </span>
      </div>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
