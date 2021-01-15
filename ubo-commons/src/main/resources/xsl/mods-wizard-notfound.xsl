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

<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="RequestURL" />

<xsl:template match="/">
  <html id="dozbib.new.publication">
    <head>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
      <title>
        <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound')" />
      </title>
    </head>
    <body>
      <xsl:call-template name="breadcrumb" />
      <xsl:apply-templates select="mods:mods" />
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
      <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound')" />
    </li>
  </ul>
</xsl:template>

<xsl:template match="mods:mods">
  <article class="card">
    <div class="card-body">
      <hgroup>
	<h2><xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound')"/></h2>
      </hgroup>
      <p>
	<xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound.info')"/>
      </p>
      <p>
	<xsl:apply-templates select="mods:identifier" />
      </p>
      <p>
	<a class="btn btn-sm btn-primary" href="{$WebApplicationBaseURL}newPublication.xed">
          <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound.continue')"/>
	</a>
      </p>
    </div>
  </article>
</xsl:template>

</xsl:stylesheet>
