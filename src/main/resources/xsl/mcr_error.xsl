<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink">

  <xsl:include href="layout.xsl" />

  <xsl:variable name="page.title" select="i18n:translate('error.title',concat(' ',/mcr_error/@HttpError))" />

  <!-- MCRServlet.generateErrorMessage() was called -->
  <xsl:template match="mcr_error">
    <div class="section" id="sectionlast">
      <p>
        <xsl:value-of select="concat(i18n:translate('error.intro'),' :')" />
      </p>
      <pre style="padding-left:3ex;">
        <xsl:value-of select="text()" />
      </pre>
      <xsl:choose>
        <xsl:when test="exception">
          <xsl:apply-templates select="exception" />
        </xsl:when>
        <xsl:otherwise>
          <p>
            <xsl:value-of select="i18n:translate('error.noInfo')" />
          </p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <!-- MCRErrorServlet generated this page -->
  <xsl:template match="mcr_error[@errorServlet]">
    <div class="section" id="sectionlast">
      <p>
        <xsl:call-template name="lf2br">
          <xsl:with-param name="string" select="text()" />
        </xsl:call-template>
      </p>
      <p>
        <xsl:value-of select="i18n:translate('error.requestURI',@requestURI)" />
      </p>
      <xsl:apply-templates select="exception" />
    </div>
  </xsl:template>

  <xsl:template match="exception">
    <p>
      <xsl:value-of select="concat(i18n:translate('error.stackTrace'),' :')" />
    </p>
    <xsl:apply-templates select="trace" />
  </xsl:template>

  <xsl:template match="trace">
    <pre style="padding-left:3ex;">
      <xsl:value-of select="." />
    </pre>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'&#xA;')">
        <xsl:value-of select="substring-before($string,'&#xA;')" />
        <!-- replace line break character by xhtml tag -->
        <br />
        <xsl:call-template name="lf2br">
          <xsl:with-param name="string" select="substring-after($string,'&#xA;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
