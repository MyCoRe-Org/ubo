<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:str="xalan://java.lang.String"
  xmlns:basket="xalan://org.mycore.ubo.basket.BasketUtils"
  exclude-result-prefixes="xsl xalan i18n mods mcr encoder str basket" 
>

<xsl:include href="mods-display.xsl" />

<xsl:param name="RequestURL" />
<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="ServletsBaseURL" />
  
<xsl:template match="/">
  <html id="dozbib.new.publication">
    <head>
      <title>
        <xsl:call-template name="page.title" />
      </title>
    </head>
    <body>
      <xsl:call-template name="breadcrumb" />
      <xsl:apply-templates select="response" />
    </body>
  </html>
</xsl:template>

<xsl:template name="breadcrumb">
  <ul id="breadcrumb">
    <li>
      <a href="{$WebApplicationBaseURL}newPublication.xed">
        <xsl:value-of select="i18n:translate('ubo.newPublicationWizard')" />
      </a>
    </li>
    <li>
      <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.duplicates')" />
    </li>
  </ul>
</xsl:template>

<!-- ==================== Trefferliste Metadaten ==================== -->

<xsl:variable name="numFound" select="/response/result[@name='response']/@numFound" />
<xsl:variable name="numDocs" select="count(/response/result[@name='response']/doc)" />
<xsl:variable name="start">
  <xsl:choose>
    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']">
      <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']" />
    </xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="rows">
  <xsl:choose>
    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']">
      <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
    </xsl:when>
    <xsl:when test="$start + $numDocs &lt; $numFound">
      <xsl:value-of select="$numDocs" /> <!-- rows parameter missing, use count docs returned -->
    </xsl:when>
    <xsl:otherwise>10</xsl:otherwise> <!-- guess, no way to find out -->
  </xsl:choose>
</xsl:variable>

<!-- ==================== Anzeige Seitentitel ==================== -->

<xsl:template name="page.title">
  <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.duplicates')" /> 
  <xsl:text>: </xsl:text> 
  <xsl:choose>
    <xsl:when test="$numFound > 1">
      <xsl:value-of select="$numFound" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationMany')"/>
    </xsl:when>
    <xsl:when test="$numFound = 1">
      <xsl:value-of select="i18n:translate('result.dozbib.publicationOne')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')"/>
    </xsl:otherwise>
  </xsl:choose> 
</xsl:template>

<!-- ==================== Anzeige Dublettenliste ==================== -->

<xsl:template match="response">
  <article class="card mb-2">
    <div class="card-body">
      <hgroup>
        <h2><xsl:value-of select="i18n:translate('ubo.newPublicationWizard.duplicates.headline')"/>:</h2>
      </hgroup>
      <xsl:copy-of select="document('xslStyle:mods-wizard-display:session:ubo.submission')/*" />
      <p>
        <strong>
          <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.duplicates.pleaseCheck')"/>
        </strong>
        <form method="get" action="{$ServletsBaseURL}NewPublicationWizard">
          <input type="hidden" name="step" value="genres" />
          <input type="submit" class="btn btn-sm btn-primary btn-block" value="{i18n:translate('ubo.newPublicationWizard.duplicates.noContinue')}" />
        </form>
      </p>
    </div>
  </article>
  <xsl:apply-templates select="result[@name='response']" />
</xsl:template>

<xsl:template match="result[@name='response']">
  <xsl:if test="$numFound = 0">
    <div class="card">
      <div class="card-body">
        <p>
          <xsl:value-of select="i18n:translate('result.dozbib.publicationNo')" />
        </p>
      </div>
    </div>
  </xsl:if>

  <xsl:if test="$numFound &gt; 0">
    <xsl:apply-templates select="doc">
      <xsl:with-param name="start" select="@start"/>
    </xsl:apply-templates>
  </xsl:if>

</xsl:template>

<!-- ==================== Anzeige Dublette ==================== -->

<xsl:template match="doc">
  <xsl:param name="start" />
  <xsl:variable name="hitNo" select="$start + position()" />
  
  <div class="result mt-2 mb-2">
    <div class="hit card">
      <xsl:variable name="id" select="str[@name='id']" />
      <xsl:variable name="mycoreobject" select="document(concat('mcrobject:',$id))/mycoreobject" />
      <xsl:for-each select="$mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
        <div class="labels card-header">
          <xsl:call-template name="label-year" />
          <xsl:call-template name="pubtype" />
        </div>
        <div class="content bibentry card-body">
          <xsl:apply-templates select="." mode="cite"> 
            <xsl:with-param name="mode">divs</xsl:with-param> 
          </xsl:apply-templates>
        </div>
        <div class="footer card-footer">
          <xsl:call-template name="bibentry.show.details" />
          <span class="float-right"># <xsl:value-of select="$hitNo"/></span>
        </div>
      </xsl:for-each>
    </div>
  </div>
</xsl:template>

<xsl:template name="bibentry.show.details">
  <form class="d-inline" action="{$ServletsBaseURL}DozBibEntryServlet" method="get">
    <input type="hidden" name="mode" value="show"/>
    <input type="hidden" name="id" value="{ancestor::mycoreobject/@ID}"/>
    <input type="submit" class="btn btn-sm btn-primary" value="{i18n:translate('result.dozbib.info')}" />
  </form>
</xsl:template>

</xsl:stylesheet>
