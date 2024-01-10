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
  <mods:mods>
    <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#article" />
    <xsl:apply-templates select="Titel" />
    <xsl:apply-templates select="Autoren" />
    <mods:relatedItem type="host">
      <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#journal" />
      <xsl:apply-templates select="Quelle" />
      <xsl:call-template name="originInfo" />
      <xsl:apply-templates select="ISBN" />
    </mods:relatedItem>
    <xsl:apply-templates select="DOI" />
    <xsl:apply-templates select="Bemerkung" />
    <xsl:call-template name="fach" />
    <xsl:call-template name="origin" />
  </mods:mods>
</xsl:template>

<xsl:template match="Titel|Quelle">
  <mods:titleInfo>
    <mods:title>
      <xsl:value-of select="text()" />
    </mods:title>
  </mods:titleInfo>
</xsl:template>

<xsl:template match="Autoren">
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
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:for-each>
</xsl:template>

<xsl:template name="originInfo">
  <mods:originInfo>
    <xsl:apply-templates select="Verlag" />
    <xsl:apply-templates select="Jahr" />
  </mods:originInfo>
</xsl:template>

<xsl:template match="Verlag">
  <mods:publisher>
    <xsl:value-of select="text()" />
  </mods:publisher>
</xsl:template>

<xsl:template match="Jahr">
  <mods:dateIssued encoding="w3cdtf">
    <xsl:value-of select="text()" />
  </mods:dateIssued>
</xsl:template>

<xsl:template match="DOI">
  <mods:identifier>
    <xsl:attribute name="type">
      <xsl:choose>
        <xsl:when test="not(starts-with(text(),'10.')) and (string-length(text()) = 9) and (substring(text(),5,1) = '-')">issn</xsl:when>
        <xsl:otherwise>doi</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <xsl:value-of select="text()" />
  </mods:identifier>
</xsl:template>

<xsl:template match="ISBN">
  <mods:identifier>
    <xsl:attribute name="type">
      <xsl:choose>
        <xsl:when test="(string-length(text()) = 9) and (substring(text(),5,1) = '-')">issn</xsl:when>
        <xsl:otherwise>isbn</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <xsl:value-of select="text()" />
  </mods:identifier>
</xsl:template>

<xsl:template match="Bemerkung">
  <mods:note>
    <xsl:value-of select="text()" />
  </mods:note>
</xsl:template>

<xsl:template name="fach">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/fachreferate</xsl:variable>
  <mods:classification valueURI="{$uri}#ele" authorityURI="{$uri}"/>
</xsl:template>

<xsl:template name="origin">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/ORIGIN</xsl:variable>
  <mods:classification valueURI="{$uri}#18.02.06" authorityURI="{$uri}"/>
</xsl:template>

</xsl:stylesheet>
