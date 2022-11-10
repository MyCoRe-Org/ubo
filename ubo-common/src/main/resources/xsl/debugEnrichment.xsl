<?xml version="1.0" encoding="UTF-8"?>

<!-- Renders enrichment resolver debugging output from EnrichmentDebuggerServlet -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3">

  <xsl:include href="mods-display.xsl" />

  <xsl:template match="/">
    <html id="debugEnrichment">
      <head>
        <title>Enrichment Resolver Debugger</title>
        <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/highlightjs/11.5.0/styles/default.min.css" />
        <script src="{$WebApplicationBaseURL}webjars/highlightjs/11.5.0/highlight.min.js" />
        <script src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js" />
      </head>
      <body>
        <!-- <xsl:apply-templates select="debugEnrichment" mode="source" /> -->
        <xsl:apply-templates select="debugEnrichment" />
        <script>hljs.highlightAll();</script>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="debugEnrichment">
    <xsl:apply-templates select="*" />
  </xsl:template>
  
  <xsl:template match="before|resolved|afterMerge|result">
  <div class="card mt-3">
    <div class="card-body">
      <h5 class="card-title">
        <xsl:call-template name="pos" />
        <xsl:text> </xsl:text> 
        <xsl:choose>
          <xsl:when test="name()='before'">
            <xsl:text>Enrichment-Durchlauf für </xsl:text>
            <xsl:apply-templates select="*" mode="headline"/>
          </xsl:when>
          <xsl:when test="name()='resolved'">Publikationsdaten empfangen aus <xsl:value-of select="@from" /></xsl:when>
          <xsl:when test="name()='afterMerge'">Publikationsdaten nach dem Merge dieser Datenquelle <xsl:value-of select="@from" /></xsl:when>
          <xsl:when test="name()='result'">Endergebnis</xsl:when>  
        </xsl:choose>
      </h5>
      <xsl:apply-templates select="mods:mods|mods:relatedItem" mode="source" />
      <xsl:apply-templates select="mods:mods|mods:relatedItem" mode="details_lines" />
      <xsl:if test="(name()='before') and (count(following-sibling::enrichmentIteration) = 0)">
        <p class="mt-2"><strong>Es wurden keine Identifier gefunden - daher kein Enrichment möglich!</strong></p>
      </xsl:if>
    </div>
  </div>
  </xsl:template>
  
  <xsl:template match="enrichmentIteration">
    <div class="card mt-3">
      <div class="card-body">
        <h5 class="card-title">
          <xsl:call-template name="pos" /> 
          <xsl:text> Beginn einer neuen Enrichment Resolver Iteration</xsl:text>
        </h5>
        <p>Neue Identifier gefunden:</p>
        <xsl:for-each select="newIdentifiersFound">
          <xsl:apply-templates select="mods:identifier|mods:location/mods:shelfLocator" mode="details" />
        </xsl:for-each>
      </div>
    </div>

    <xsl:apply-templates select="before|resolved|afterMerge" />
    
    <div class="card mt-3">
      <div class="card-body">
        <h5 class="card-title">
          <xsl:call-template name="pos" /> 
          <xsl:text> Ende der Enrichment Resolver Iteration</xsl:text>
        </h5>
        <p>Keine weiteren neuen Daten/Datenquellen für diese(n) neuen Identifier!</p>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template match="mods:mods|mods:relatedItem" mode="headline">
    <xsl:value-of select="name()" />
    <xsl:for-each select="@type">
      <xsl:text>[@type='</xsl:text>
      <xsl:value-of select="." />
      <xsl:text>']</xsl:text>
    </xsl:for-each>
  </xsl:template>
    
  <xsl:template name="pos">
    <xsl:for-each select="parent::node()[(name() = 'before') or (name() = 'enrichmentIteration')]">
      <xsl:call-template name="pos" />
    </xsl:for-each>
    <xsl:value-of select="count(preceding-sibling::*)" />
    <xsl:text>.</xsl:text>
  </xsl:template>

  <xsl:template match="*" mode="source">
    <pre style="margin-bottom:0;">
      <code class="language-xml" style="max-height:20em; overflow:scroll;">
        <xsl:value-of select="substring('                    ',1,string-length(text()[1]) - 3)" />
        <xsl:apply-templates select="." mode="sourceCode" />
      </code>
    </pre>
  </xsl:template>

  <xsl:template match="*" mode="sourceCode">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()" />
    <xsl:apply-templates select="@*" mode="sourceCode" />
    <xsl:choose>
      <xsl:when test="(count(*) = 0) and (string-length(text()) = 0)">
        <xsl:text> /&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates select="*|text()" mode="sourceCode" />
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()" />
        <xsl:text>&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="@*" mode="sourceCode">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()" />
    <xsl:text>="</xsl:text>
    <xsl:value-of select="." />
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="sourceCode">
    <xsl:value-of select="." />
  </xsl:template>
  
</xsl:stylesheet>
