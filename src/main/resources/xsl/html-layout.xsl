<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder" 
  exclude-result-prefixes="xsl xalan i18n encoder">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="yes" xalan:indent-amount="2" />

  <xsl:param name="CurrentLang" />

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
      
      <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
      <meta http-equiv="X-UA-Compatible" content="IE=9,chrome=1" />
      <meta http-equiv="cleartype" content="on" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="cache-control" content="no-cache" />
      <meta http-equiv="pragma" content="no-cache" />
      
      <meta name="HandheldFriendly" content="True" />
      <meta name="MobileOptimized" content="320" />
      <meta name="viewport" content="width=device-width, target-densitydpi=160dpi, initial-scale=1" />
      
      <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans+Mono" type="text/css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}external/jquery-ui-theme/jquery-ui-1.8.21.custom.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/style.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/legacy.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/duepublico.css?v={$UBO.Build.TimeStamp}" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}external/chosen/chosen.css" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/bootstrap-glyphicons/bdd2cbfba0/css/bootstrap-glyphicons.css" />
      
      <xsl:text disable-output-escaping="yes">&lt;!--[if gte IE 9]&gt;</xsl:text>
        <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/ie9fixes.css" />
      <xsl:text disable-output-escaping="yes">&lt;![endif]--&gt;</xsl:text>

      <link rel="apple-touch-icon-precomposed" sizes="114x114" href="https://www.uni-due.de/imperia/md/images/cms/h/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" sizes="72x72" href="https://www.uni-due.de/imperia/md/images/cms/m/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon-precomposed.png" />
      <link rel="shortcut icon" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon.png" />
      <link rel="shortcut icon" href="{$WebApplicationBaseURL}images/favicon.ico" />
      
      <script type="text/javascript">var webApplicationBaseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';</script>
      <script type="text/javascript">var currentLang = '<xsl:value-of select="$CurrentLang" />';</script>

      <script type="text/javascript" src="{$WebApplicationBaseURL}external/jquery-1.7.min.js"></script>
      <script type="text/javascript"> jQuery.noConflict(); </script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}external/html5shiv-3.5/html5shiv.js"></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}external/modernizr-2.5.3/modernizr.js"></script>
      
      <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 9]&gt;</xsl:text>
        <script src="{$WebApplicationBaseURL}external/html5shiv-3.5/html5shiv.js"></script>
      <xsl:text disable-output-escaping="yes">&lt;![endif]--&gt;</xsl:text>
      
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
    <xsl:element name="body">
      <xsl:attribute name="class">
        <xsl:text>clearfix layout</xsl:text>
        <xsl:choose>
          <xsl:when test="body/aside[@id='sidebar']">2</xsl:when>
          <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <xsl:call-template name="layout.colorbox" />
      <div id="container" class="clearfix">
        <xsl:call-template name="layout.skip" />
        <div>
          <xsl:call-template name="layout.h1" />
          <xsl:call-template name="layout.mastHead" />
        </div>
        <section id="breadcrumb" style="padding-right:0; width:74%;">
          <xsl:call-template name="layout.breadcrumbPath" />
          <xsl:call-template name="layout.basket.info" />
        </section>
        <div id="leftbar">
          <xsl:call-template name="layout.login" />
          <xsl:call-template name="layout.mainnavigation" />
        </div>
        <xsl:call-template name="layout.inhalt" />
        <xsl:copy-of select="body/aside[@id='sidebar']" />
        <xsl:call-template name="layout.footer" />
      </div>
    </xsl:element>
  </xsl:template>

  <xsl:template name="layout.basket.info">
    <div id="basket-info" style="float:right; margin:0; padding:0; display:inline-block;">
      <a href="{$ServletsBaseURL}MCRBasketServlet?action=show&amp;type=bibentries">
        <span class="glyphicon glyphicon-bookmark" aria-hidden="true" />
        <span><xsl:value-of select="i18n:translate('basket')" />:</span>
        <span id="basket-info-num">
          <xsl:value-of xmlns:basket="xalan://unidue.ubo.basket.BasketUtils" select="basket:size()" />
        </span>
        <span><xsl:value-of select="i18n:translate('ubo.publications')" /></span>
      </a>
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

  <!-- H 1 -->

  <xsl:template name="layout.h1">
    <h1>
      <a href="https://www.uni-due.de/">Universität Duisburg-Essen</a>
    </h1>
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
    <span>
      <a href="https://www.uni-due.de/ub/">
        <xsl:value-of select="i18n:translate('navigation.UB')" />
      </a>
    </span>
    <span>
      <a href="{$WebApplicationBaseURL}">
        <xsl:value-of select="i18n:translate('navigation.Home')" />
      </a>
    </span>

    <xsl:apply-templates mode="breadcrumb"
      select="$CurrentItem/ancestor-or-self::item[@label|label][ancestor-or-self::*=$navigation.tree[@role='main']]" />

    <xsl:for-each select="body/ul[@id='breadcrumb']/li">
      <span>
        <xsl:copy-of select="node()" />
      </span>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="item" mode="breadcrumb">
    <span>
      <xsl:call-template name="output.item.label" />
    </span>
  </xsl:template>

  <!-- Ausgabe Link und Bezeichnung des Menüpunktes -->

  <xsl:template name="output.item.label">
    
    <!-- add class="current for selected items" -->
    <xsl:variable name="class">
      <xsl:if test="@id and (@id = $PageID)">
        <xsl:value-of select="'current'"/>
      </xsl:if>
    </xsl:variable>
    
    <xsl:choose>

      <!-- ==== link to external url ==== -->
      <xsl:when test="@href">
        <a href="{@href}" class="{$class}">
          <xsl:copy-of select="@target" />
          <xsl:call-template name="output.label.for.lang" />
        </a>
      </xsl:when>

      <!-- ==== link to internal url ==== -->
      <xsl:when test="@ref">
        <a href="{$WebApplicationBaseURL}{@ref}" class="{$class}">
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
    <div id="login">
      <span class="user">
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

      <xsl:choose>
        <xsl:when test="/webpage/@id='login'" />
        <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
          <form action="{$WebApplicationBaseURL}login.xed" method="get">
            <input type="hidden" name="url" value="{$RequestURL}" />
            <input class="roundedButton" style="border:0;" type="submit" name="{i18n:translate('component.user2.button.login')}" value="{i18n:translate('component.user2.button.login')}" />
          </form>
        </xsl:when>
        <xsl:otherwise>
          <form action="{$ServletsBaseURL}logout" method="get">
            <input type="hidden" name="url" value="{$WebApplicationBaseURL}" />
            <input class="roundedButton" style="border:0;" type="submit" name="{i18n:translate('login.logOut')}" value="{i18n:translate('login.logOut')}" />
          </form>
        </xsl:otherwise>
      </xsl:choose>
      
      <div class="clearfix"/>
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
  
    <nav role="navigation" id="hauptnavigation">
      <ul id="mainnav">
        <!-- Find the item that is the root of the navigation tree to display -->
        <xsl:for-each select="$navigation.tree">
          <xsl:apply-templates select="item[@label|label]" mode="navigation" />
        </xsl:for-each>
      </ul>
      <ul id="languagenav">
        <li>
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
        </li>
      </ul>
    </nav>
  </xsl:template>

  <!-- Menüpunkt im Navigationsmenü -->

  <xsl:template match="item" mode="navigation">

    <!-- ==== Prüfe ob aktueller Benutzer berechtigt ist, diesen Menüpunkt zu sehen ==== -->
    <xsl:variable name="allowed">
      <xsl:call-template name="check.allowed" />
    </xsl:variable>

    <!-- print single menu option -->
    <xsl:if test="$allowed = 'true'">
      <xsl:element name="li">
        <xsl:if test="@class != ''">
          <xsl:attribute name="class">
            <xsl:value-of select="@class"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:call-template name="output.item.label" />
        <xsl:if test="descendant::item[@id = $NavigationID]">
          <ul>
            <xsl:apply-templates select="item[@label|label]" mode="navigation" />
          </ul>
        </xsl:if>
      </xsl:element>
    </xsl:if>

  </xsl:template>

  <!-- Master Head -->

  <xsl:template name="layout.mastHead">
    <div id="masthead">
      <header role="banner" class="clearfix">
        <h2>
          <a href="{$WebApplicationBaseURL}">Universitätsbibliographie</a>
        </h2>
        <a class="containsimage" id="ude-logo" href="https://www.uni-due.de/">
          <img src="{$WebApplicationBaseURL}images/ude-logo.png" alt="Logo" />
        </a>
      </header>
      <h1 id="seitentitel">
        <xsl:value-of select="head/title" disable-output-escaping="yes" />
      </h1>
    </div>
  </xsl:template>
  
  <!-- Footer -->

  <xsl:template name="layout.footer">
    <footer role="contentinfo" class="clearfix">
      <xsl:call-template name="layout.metanav" />
      <p>
        <xsl:apply-templates select="/html/@lastModified" />
        <xsl:text>© Universität Duisburg-Essen | </xsl:text>
        <a href="mailto:{$MCR.Mail.Address}">
          <xsl:value-of select="$MCR.Mail.Address" />
        </a>
      </p>
    </footer>
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
    <nav id="metanav">
      <ul>
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