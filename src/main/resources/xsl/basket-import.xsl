<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcr="http://www.mycore.org/"
  exclude-result-prefixes="xsl xalan i18n mods mcr">

  <xsl:include href="basket-common.xsl" />
  <xsl:include href="bibmaster.xsl" />
  <xsl:include href="mods-display.xsl" />

  <xsl:variable name="page.title" select="'Literaturliste importieren'" />
  <xsl:variable name="PageID" select="'dozbib.import.basket'" />

  <xsl:template match="basket[entry]">
    <article class="highlight1">
      <h3>Die folgenden <xsl:value-of select="count(entry)" /> Einträge sind für den Import vorbereitet.</h3>
      <p>
        <xsl:if test="//mods:extension[@type='fields'][field]">
          Bitte beachten Sie gegebenenfalls die Hinweise zu nicht verarbeiteten oder ignorierten Feldern. <br />
        </xsl:if>
        Sie können hier noch einzelne Publikationen entfernen und damit vom Import ausschließen. <br />
        Anschließend klicken Sie auf:
      </p>
      <a class="roundedButton" href="{$ServletsBaseURL}DozBibImportServlet?action=save">Diese Publikationen importieren</a>
      <a class="roundedButton" style="margin-left:1ex;" href="{$ServletsBaseURL}MCRBasketServlet?type=import&amp;action=clear">Import-Korb leeren</a>
    </article>
    <xsl:call-template name="basketEntries" />
  </xsl:template>
  
  <xsl:template match="basket[not(entry)]">
    <article class="highlight0">
      <h3>Es sind keine für den Import vorbereiteten Einträge vorhanden.</h3>
    </article>
  </xsl:template>

  <xsl:template name="buttons">
    <xsl:for-each select="bibentry">
      <xsl:if test="dedup">
        <xsl:variable name="duplicatesURI">
          <xsl:call-template name="buildFindDuplicatesURI" />
        </xsl:variable>
        <xsl:variable name="duplicates" select="document($duplicatesURI)/mcr:results/mcr:hit" />
        <xsl:if test="count($duplicates) &gt; 0">
          <a class="roundedButton basketButton">
            <xsl:attribute name="href">
              <xsl:value-of select="$ServletsBaseURL" />
              <xsl:text>DozBibServlet?</xsl:text>
              <xsl:for-each select="dedup">
                <xsl:text>ubo_dedup=</xsl:text>
                <xsl:value-of select="@key" />
                <xsl:if test="position() != last()">&amp;</xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:value-of select="count($duplicates)" />
            <xsl:text> Dublette</xsl:text>
            <xsl:if test="count($duplicates) &gt; 1">n</xsl:if>
            <xsl:text>?</xsl:text>
          </a>
        </xsl:if>
      </xsl:if>
    </xsl:for-each>
    <xsl:call-template name="button">
      <xsl:with-param name="image">remove.png</xsl:with-param>
      <xsl:with-param name="action">remove</xsl:with-param>
      <xsl:with-param name="alt">Entfernen</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bibentry" mode="basketContent">
    <div class="labels">
      <xsl:call-template name="pubtype" />
      <xsl:call-template name="label-year" />
    </div>
    <div class="content">
      <xsl:for-each select="mods:mods">
        <div class="bibentry">
          <xsl:apply-templates select="." mode="cite">
            <xsl:with-param name="mode">divs</xsl:with-param>
          </xsl:apply-templates>
        </div>
        <xsl:apply-templates select="mods:extension[@type='source']" />
        <xsl:apply-templates select="mods:extension[@type='fields'][field]" />
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="mods:extension[@type='source']">
    <div class="ubo-extension-source">
      <h4>Quellformat:</h4>
      <pre><xsl:apply-templates select="*|text()" mode="extension-source" /></pre>
    </div>
  </xsl:template>
  
  <xsl:template match="*" mode="extension-source">
    <xsl:value-of select="concat('&lt;',name())" />
    <xsl:apply-templates select="@*" mode="extension-source" />
    <xsl:text>&gt;</xsl:text>
    <xsl:apply-templates select="*|text()" mode="extension-source" />
    <xsl:value-of select="concat('&lt;/',name(),'&gt;')" />
  </xsl:template>
  
  <xsl:template match="text()" mode="extension-source">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template match="@*" mode="extension-source">
    <xsl:value-of select="concat(' ',name(),'=&#34;',.,'&#34;')" />
  </xsl:template>

  <xsl:template match="mods:extension[@type='fields']">
    <ul>
      <xsl:apply-templates select="field" />
      <xsl:for-each select="ancestor::bibentry//comment()">
        <li>
          <xsl:value-of select="." />
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <xsl:template match="mods:extension[@type='fields']/field">
    <li>
      <xsl:text>Feld </xsl:text>
      <strong>
        <xsl:value-of select="@name" />
      </strong>
      <xsl:text> ist kein unterstütztes Feld und wird ignoriert.</xsl:text>
    </li>
  </xsl:template>

</xsl:stylesheet>
