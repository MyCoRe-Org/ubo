<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xsl xalan i18n encoder">

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

</xsl:stylesheet>