<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xsl xalan i18n encoder">

  <!-- last navigation id calculated from navigation.xml -->
  <xsl:variable name="NavigationID" xmlns:lastPageID="xalan://org.mycore.ubo.LastPageID">
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


  <!-- load navigation from navigation.xml -->
  <xsl:variable name="navigation.tree" select="document('webapp:navigation.xml')/navigation" />

  <xsl:variable name="CurrentItem" select="$navigation.tree/descendant-or-self::item[@id = $NavigationID]" />

  <!-- true if current user is the guest user -->
  <xsl:variable name="isGuest" select="$CurrentUser = $MCR.Users.Guestuser.UserName"/>


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

  <!-- Main-Navigation -->

  <xsl:template name="layout.mainnav">
    <!-- Find the item that is the root of the navigation tree to display -->
    <xsl:for-each select="$navigation.tree/item[@menu='main']">
      <xsl:apply-templates select="item[@label|label]" mode="navigation" />
    </xsl:for-each>
  </xsl:template>

  <!-- User-Dropdown-Navigation -->

  <xsl:template name="layout.usernav">
    <!-- Find the item that is the root of the navigation tree to display -->

    <xsl:for-each select="$navigation.tree/item[@menu='user']/item">
      <!-- ==== Prüfe ob aktueller Benutzer berechtigt ist, diesen Menüpunkt zu sehen ==== -->
      <xsl:variable name="allowed">
        <xsl:call-template name="check.allowed" />
      </xsl:variable>

      <xsl:if test="$allowed = 'true'">
        <a href="{$WebApplicationBaseURL}{@ref}" class="dropdown-item">
          <xsl:call-template name="output.label.for.lang" />
        </a>
      </xsl:if>
    </xsl:for-each>
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

  <!-- Menüpunkt im Navigationsmenü -->
  <xsl:template match="item" mode="navigation">

    <!-- ==== Prüfe ob aktueller Benutzer berechtigt ist, diesen Menüpunkt zu sehen ==== -->
    <xsl:variable name="allowed">
      <xsl:call-template name="check.allowed" />
    </xsl:variable>

    <xsl:variable name="alwaysVisible">
      <xsl:choose>
        <xsl:when test="string-length(@alwaysVisible)&gt;0">
          <xsl:value-of select="@alwaysVisible" />
        </xsl:when>
        <xsl:otherwise><xsl:text>false</xsl:text></xsl:otherwise>
      </xsl:choose>
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

    <xsl:if test="$allowed = 'true' or $alwaysVisible = 'true'">

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
                <a class="dropdown-item" href="{$WebApplicationBaseURL}{@ref}">
                  <xsl:copy-of select="@target" />
                  <xsl:call-template name="output.label.for.lang" />
                </a>
              </xsl:for-each>
            </div>
          </li>
        </xsl:when>
        <xsl:otherwise>
          <!-- print single menu option -->
          <li class="nav-item {$class_active} {@class}">
            <xsl:call-template name="output.item.label"/>
          </li>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
