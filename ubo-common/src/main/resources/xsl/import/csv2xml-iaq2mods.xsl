<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  exclude-result-prefixes="xsl xalan"
>

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:param name="WebApplicationBaseURL"/>

<xsl:template match="/csv2xml">
  <mods:modsCollection>
    <xsl:apply-templates select="row" />
  </mods:modsCollection>
</xsl:template>

<xsl:template match="row">
  <xsl:variable name="pubType">
    <xsl:apply-templates select="typ" mode="pubType" />
  </xsl:variable>
  
  <mods:mods>
    <xsl:apply-templates select="typ" />
    <xsl:apply-templates select="titel" />
    <xsl:apply-templates select="autor" />
    <xsl:apply-templates select="editor" />
    <xsl:apply-templates select="mitarbeiter" />

    <xsl:choose>
      <xsl:when test="$pubType='chapter'">

        <mods:relatedItem type="host">
          <xsl:apply-templates select="typ" mode="host" />
          <xsl:apply-templates select="buchtitel" />
          <xsl:apply-templates select="beditor" />
          <xsl:call-template name="originInfo" />
          <mods:part>
            <xsl:apply-templates select="seiten" />
          </mods:part>
        </mods:relatedItem>

      </xsl:when>
      <xsl:when test="$pubType='book'">
      
        <xsl:call-template name="originInfo" />
        <mods:relatedItem type="series">
          <xsl:apply-templates select="reihentitel" />
          <mods:part>
            <xsl:apply-templates select="heft" />
          </mods:part>
        </mods:relatedItem>
      
      </xsl:when>
      <xsl:when test="$pubType='article'">

        <mods:relatedItem type="host">
          <xsl:apply-templates select="typ" mode="host" />
          <xsl:apply-templates select="zeitschriftentitel" />
          <xsl:call-template name="originInfo" />
          <xsl:apply-templates select="issnprint|issnonline" />
          <mods:part>
            <xsl:apply-templates select="volume" />
            <xsl:apply-templates select="heft" />
            <xsl:apply-templates select="seiten" />
          </mods:part>
        </mods:relatedItem>

      </xsl:when>
    </xsl:choose>

    <xsl:apply-templates select="sprache" />
    <xsl:apply-templates select="isbnprint|isbneBook" />
    <xsl:apply-templates select="doi" />
    <xsl:apply-templates select="datei" />
    <xsl:apply-templates select="volltext" />
    <xsl:call-template name="schwerpunkt" />
    <xsl:call-template name="fach" />
    <xsl:call-template name="projekt" />
  </mods:mods>
</xsl:template>

<xsl:template match="typ" mode="pubType">
  <xsl:choose>
    <xsl:when test="contains(.,'Rezension')">article</xsl:when>
    <xsl:when test="contains(.,'Zeitschriftenaufsatz')">article</xsl:when>
    <xsl:when test="contains(.,'Internet-Dokument')">article</xsl:when>
    <xsl:when test="contains(.,'Dissertation')">book</xsl:when>
    <xsl:when test="contains(.,'IAQ-Reihe')">book</xsl:when>
    <xsl:when test="contains(.,'Monografie')">book</xsl:when>
    <xsl:when test="contains(.,'monografie')">book</xsl:when>
    <xsl:when test="contains(.,'Sammelband')">book</xsl:when>
    <xsl:when test="contains(.,'Sammelwerk')">book</xsl:when>
    <xsl:when test="contains(.,'Tagungsdokumentation')">book</xsl:when>
    <xsl:when test="contains(.,'Buchaufsatz')">chapter</xsl:when>
    <xsl:when test="contains(.,'Buchaufsatz-Tagung')">chapter</xsl:when>
    <xsl:when test="contains(.,'Lexikoneintrag')">chapter</xsl:when>
    <xsl:otherwise>article</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="typ">

  <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre">
    <xsl:attribute name="valueURI">
      <xsl:choose>
        <xsl:when test="contains(.,'Rezension')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/></xsl:when>
        <xsl:when test="contains(.,'Zeitschriftenaufsatz')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/></xsl:when>
        <xsl:when test="contains(.,'Internet-Dokument')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/></xsl:when>
        <xsl:when test="contains(.,'Dissertation')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#dissertation')"/></xsl:when>
        <xsl:when test="contains(.,'IAQ-Reihe')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#series')"/></xsl:when>
        <xsl:when test="contains(.,'Monografie')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#book')"/></xsl:when>
        <xsl:when test="contains(.,'monografie')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#book')"/></xsl:when>
        <xsl:when test="contains(.,'Sammelband')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#collection')"/></xsl:when>
        <xsl:when test="contains(.,'Sammelwerk')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#collection')"/></xsl:when>
        <xsl:when test="contains(.,'Tagungsdokumentation')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#proceedings')"/></xsl:when>
        <xsl:when test="contains(.,'Buchaufsatz-Tagung')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#chapter')"/></xsl:when>
        <xsl:when test="contains(.,'Buchaufsatz')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#chapter')"/></xsl:when>
        <xsl:when test="contains(.,'Lexikoneintrag')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#entry')"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/></xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </mods:genre>
</xsl:template>

<xsl:template match="typ" mode="host">
  <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre">
    <xsl:attribute name="valueURI">
      <xsl:choose>
        <xsl:when test="contains(.,'Rezension')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#journal')"/></xsl:when>
        <xsl:when test="contains(.,'Zeitschriftenaufsatz')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#journal')"/></xsl:when>
        <xsl:when test="contains(.,'Buchaufsatz-Tagung')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#proceedings')"/></xsl:when>
        <xsl:when test="contains(.,'Buchaufsatz')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#collection')"/></xsl:when>
        <xsl:when test="contains(.,'Lexikoneintrag')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#lexicon')"/></xsl:when>
        <xsl:when test="contains(.,'Internet-Dokument')"><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#collection')"/></xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </mods:genre>
</xsl:template>

<xsl:template match="titel|buchtitel|reihentitel|zeitschriftentitel">
  <mods:titleInfo>
    <mods:title>
      <xsl:value-of select="text()" />
      <xsl:if test="(name()='titel') and (../typ='Rezension')"> [Rezension]</xsl:if>
    </mods:title>
  </mods:titleInfo>
</xsl:template>

<xsl:template match="autor|editor|beditor|mitarbeiter">
  <xsl:variable name="role">
    <xsl:choose>
      <xsl:when test="name()='autor'">aut</xsl:when>
      <xsl:when test="name()='mitarbeiter'">ctb</xsl:when>
      <xsl:when test="name()='beditor'">edt</xsl:when>
      <xsl:when test="name()='editor'">edt</xsl:when>
    </xsl:choose>
  </xsl:variable>
  <xsl:for-each select="xalan:tokenize(text(),';')">
    <mods:name type="personal">
      <xsl:for-each select="xalan:tokenize(.,',')">
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="position() = 1">family</xsl:when>
            <xsl:when test="position() = 2">given</xsl:when>
          </xsl:choose>
        </xsl:variable>
        <mods:namePart type="{$type}">
          <xsl:value-of select="normalize-space(.)" />
        </mods:namePart>
      </xsl:for-each> 
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">
          <xsl:value-of select="$role" />
        </mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:for-each>
</xsl:template>

<xsl:template name="originInfo">
  <mods:originInfo>
    <xsl:apply-templates select="ort" />
    <xsl:apply-templates select="verlag" />
    <xsl:apply-templates select="jahr" />
  </mods:originInfo>
</xsl:template>

<xsl:template match="ort">
  <mods:place>
    <mods:placeTerm type="text">
      <xsl:value-of select="text()" />
    </mods:placeTerm>
  </mods:place>
</xsl:template>

<xsl:template match="verlag">
  <mods:publisher>
    <xsl:value-of select="text()" />
  </mods:publisher>
</xsl:template>

<xsl:template match="jahr">
  <mods:dateIssued encoding="w3cdtf">
    <xsl:value-of select="text()" />
  </mods:dateIssued>
</xsl:template>

<xsl:template match="doi">
  <mods:identifier type="doi">
    <xsl:value-of select="text()" />
  </mods:identifier>
</xsl:template>

<xsl:template match="isbnprint|isbneBook">
  <mods:identifier type="isbn">
    <xsl:value-of select="text()" />
  </mods:identifier>
</xsl:template>

<xsl:template match="issnprint|issnonline">
  <mods:identifier type="issn">
    <xsl:value-of select="text()" />
  </mods:identifier>
</xsl:template>

<xsl:template match="volltext|datei">
  <mods:location>
    <mods:url>
    <xsl:value-of select="text()" />
    </mods:url>
  </mods:location>
</xsl:template>

<xsl:template match="sprache">
  <mods:language>
    <mods:languageTerm type="code" authority="rfc4646">
      <xsl:value-of select="text()" />
    </mods:languageTerm>
  </mods:language>
</xsl:template>

<xsl:template match="volume|heft[../reihentitel]">
  <mods:detail type="volume">
    <mods:number>
      <xsl:value-of select="text()" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="heft">
  <mods:detail type="issue">
    <mods:number>
      <xsl:value-of select="text()" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="seiten" xmlns:pages="xalan://org.mycore.mods.MCRMODSPagesHelper">
  <xsl:copy-of select="pages:buildExtentPagesNodeSet(text())" />
</xsl:template>

<xsl:template name="schwerpunkt">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/ORIGIN</xsl:variable>
  <xsl:choose>
    <xsl:when test="schwerpunkt">
      <xsl:for-each select="xalan:tokenize(schwerpunkt,',')">
        <mods:classification valueURI="{$uri}#{normalize-space(.)}" authorityURI="{$uri}" />
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <mods:classification valueURI="{$uri}#16.07" authorityURI="{$uri}" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="fach">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/fachreferate</xsl:variable>
  <mods:classification valueURI="{$uri}#sowi" authorityURI="{$uri}"/>
</xsl:template>

<xsl:template name="projekt">
 <xsl:if test="projekt">
   <mods:extension>
      <xsl:for-each select="xalan:tokenize(projekt,';')">
        <tag>
          <xsl:value-of select="normalize-space(.)" />
        </tag>  
      </xsl:for-each>
   </mods:extension>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
