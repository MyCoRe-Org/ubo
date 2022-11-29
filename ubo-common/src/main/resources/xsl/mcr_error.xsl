<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="i18n mcrxsl xalan">

  <xsl:param name="MCR.Users.Superuser.GroupName"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="i18n:translate('error.title',concat(' ',/mcr_error/@HttpError))"/>
        </title>
      </head>
      <body>
        <xsl:apply-templates select="mcr_error"/>
      </body>
    </html>
  </xsl:template>

  <!-- MCRServlet.generateErrorMessage() was called -->
  <xsl:template match="mcr_error">
    <div class="section card" id="sectionlast">
      <div class="card-body">
        <p>
          <xsl:value-of select="concat(i18n:translate('error.intro'),' :')"/>
        </p>
        <pre class="pl-3">
          <xsl:value-of select="text()"/>
        </pre>
        <xsl:choose>
          <xsl:when test="exception">
            <xsl:apply-templates select="exception"/>
          </xsl:when>
          <xsl:otherwise>
            <p>
              <xsl:value-of select="i18n:translate('error.noInfo')"/>
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <!-- MCRErrorServlet generated this page -->
  <xsl:template match="mcr_error[@errorServlet]">
    <div class="section card" id="sectionlast">
      <div class="card-body">
        <p>
          <xsl:call-template name="lf2br">
            <xsl:with-param name="string" select="text()"/>
          </xsl:call-template>
        </p>
        <p>
          <xsl:value-of select="i18n:translate('error.requestURI',@requestURI)"/>
        </p>
        <xsl:if test="mcrxsl:isCurrentUserInRole($MCR.Users.Superuser.GroupName)">
          <xsl:apply-templates select="exception"/>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="exception">
    <p>
      <xsl:value-of select="concat(i18n:translate('error.stackTrace'),' :')"/>
    </p>
    <xsl:apply-templates select="trace"/>
  </xsl:template>

  <xsl:template match="trace">
    <pre style="padding-left:3ex;">
      <xsl:value-of select="."/>
    </pre>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string,'&#xA;')">
        <xsl:value-of select="substring-before($string,'&#xA;')"/>
        <!-- replace line break character by xhtml tag -->
        <br/>
        <xsl:call-template name="lf2br">
          <xsl:with-param name="string" select="substring-after($string,'&#xA;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
