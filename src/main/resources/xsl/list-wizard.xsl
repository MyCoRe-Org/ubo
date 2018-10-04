<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan i18n mods"
>

<xsl:param name="ServletsBaseURL" />

<xsl:template match="/">
  <html id="dozbib.list-wizard">
    <head>
      <title>
        <xsl:value-of select="i18n:translate('listWizard.title')" />
      </title>
    </head>
    <body>
      <xsl:apply-templates select="list-wizard" />
    </body>
  </html>
</xsl:template>

<xsl:param name="UBO.LSF.Link" />

<xsl:template match="list-wizard">

  <xsl:variable name="url">
    <xsl:value-of select="$ServletsBaseURL" />
    <xsl:text>DozBibServlet</xsl:text>
    <xsl:apply-templates select="format" />

    <xsl:text>query=(</xsl:text>
    <xsl:if test="mods:name">
      <xsl:text>(</xsl:text>
      <xsl:apply-templates select="mods:name" mode="query" />
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:if test="tag and mods:name">
      <xsl:text>+and+</xsl:text>
    </xsl:if>
    <xsl:if test="tag">
      <xsl:if test="count(tag) &gt; 1">(</xsl:if>
      <xsl:apply-templates select="tag" mode="query" />
      <xsl:if test="count(tag) &gt; 1">)</xsl:if>
    </xsl:if>
    <xsl:apply-templates select="min-year" />
    <xsl:text>)</xsl:text>
    
    <xsl:apply-templates select="sort-by/field" />
  </xsl:variable>

  <article class="highlight2">
    <hgroup>
      <h2>
        <xsl:value-of select="i18n:translate('listWizard.title')" />
      </h2>
    </hgroup>
    <p>
      <xsl:apply-templates select="mods:name" mode="display" />
      <xsl:apply-templates select="tag" mode="display" />
    </p>
    <p>
      <xsl:value-of select="i18n:translate('listWizard.link')" />
      <xsl:text>:</xsl:text>
      <br/>
      <a href="{$url}">
        <xsl:value-of select="$url" />
      </a>
    </p>
    <xsl:if test="format='html'">
      <p>
        Über einen iframe können Sie diese Publikationsliste auch direkt in Ihre eigene Webseite einbetten, statt nur darauf zu verlinken. 
        Fügen Sie dazu in Ihre Webseite z. B. das folgende Element ein:
      </p>
      <code>
        &lt;iframe scrolling="yes" width="90%" height="300" <br/>
        src="<xsl:value-of select="$url" />" /&gt;
      </code>
      <iframe style="display:block; margin-top:2ex;" scrolling="yes" width="90%" height="300" src="{$url}" />
    </xsl:if>
  </article>
  
</xsl:template>

<xsl:template match="format[text()='ubo']">
  <xsl:text>?</xsl:text>
</xsl:template>

<xsl:template match="format">
  <xsl:value-of select="concat('/results.',.,'?format=',.,'&amp;')" />
</xsl:template>

<xsl:template match="mods:name" mode="query">
  <xsl:value-of select="concat('(ae_lsf+=+',mods:nameIdentifier[@type='lsf'],')')" />
  <xsl:if test="following::mods:name">+or+</xsl:if>
</xsl:template>

<xsl:template match="tag" mode="query">
  <xsl:value-of select="concat('(tag+=+',.,')')" />
  <xsl:if test="following::tag">+or+</xsl:if>
</xsl:template>

<xsl:template match="min-year">
  <xsl:value-of select="concat('+and+(year+&gt;=+',.,')')" />
</xsl:template>

<xsl:template match="sort-by/field">
  <xsl:value-of select="concat('&amp;',@name,'.sortField.',position(),'=',@order)" />
</xsl:template>

<xsl:template match="mods:name" mode="display">
  <xsl:if test="position() = 1">
    <xsl:value-of select="i18n:translate('listWizard.for')" />
    <xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="preceding::mods:name">
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('listWizard.and')" />
    <xsl:text> </xsl:text>
  </xsl:if>
  <a href="{$UBO.LSF.Link}{mods:nameIdentifier[@type='lsf']}">
    <xsl:value-of select="mods:namePart[@type='family']" />
    <xsl:text>, </xsl:text>
    <xsl:value-of select="mods:namePart[@type='given']" />
  </a>
</xsl:template>

<xsl:template match="tag" mode="display">
  <xsl:if test="preceding::mods:name or preceding::tag">
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('listWizard.and')" />
    <xsl:text> </xsl:text>
  </xsl:if>
  <xsl:value-of select="i18n:translate('listWizard.tag')" />
  <xsl:text> "</xsl:text>
  <xsl:value-of select="text()" />
  <xsl:text>"</xsl:text>
</xsl:template>

</xsl:stylesheet>
