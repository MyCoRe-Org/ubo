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
  xmlns:basket="xalan://unidue.ubo.basket.BasketUtils"
  exclude-result-prefixes="xsl xalan i18n mods mcr encoder str basket" 
>

<xsl:include href="layout.xsl" />
<xsl:include href="mods-display.xsl" />

<xsl:param name="RequestURL" />

<xsl:variable name="ContextID" select="'dozbib.new.publication'" />

<xsl:variable name="breadcrumb.extensions">
  <item label="{i18n:translate('ubo.newPublicationWizard')}" href="{$WebApplicationBaseURL}newPublication.xed" />
  <item label="{i18n:translate('ubo.newPublicationWizard.notFound')}" />
</xsl:variable>

<xsl:variable name="head.additional">
  <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
</xsl:variable>

<!-- ==================== Anzeige Seitentitel ==================== -->

<xsl:variable name="page.title">
  <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound')" />
</xsl:variable>

<xsl:template match="mods:mods">
  <article class="highlight2">
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
      <a class="roundedButton" href="{$WebApplicationBaseURL}newPublication.xed">
        <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound.continue')"/>
      </a>
    </p>
  </article>
</xsl:template>

</xsl:stylesheet>
