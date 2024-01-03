<?xml version="1.0" encoding="UTF-8"?>

<!-- https://export.arxiv.org/api/query?id_list=1704.01408 -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:atom="http://www.w3.org/2005/Atom"
  xmlns:arxiv="http://arxiv.org/schemas/atom"
  exclude-result-prefixes="xsl xalan atom arxiv">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="atom:feed">
    <xsl:apply-templates select="atom:entry[1]" />
  </xsl:template>
  
  <xsl:template match="atom:entry">
    <mods:mods>
      <xsl:apply-templates select="atom:title" />
      <xsl:apply-templates select="atom:author" />
      <xsl:apply-templates select="atom:id" />
      <xsl:apply-templates select="arxiv:doi" />
      <xsl:apply-templates select="atom:link[@title='pdf']" />
      <xsl:apply-templates select="atom:published" />
      <xsl:apply-templates select="atom:summary" />
      <xsl:apply-templates select="arxiv:journal_ref" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="atom:title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="atom:author">
    <mods:name type="personal">
      <xsl:apply-templates select="atom:name" />
      <xsl:apply-templates select="arxiv:affiliation" />
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:template>
  
  <xsl:template match="atom:name[contains(text(),' ')]">
    <mods:namePart type="family">
      <xsl:value-of select="substring-after(text(),' ')" />
    </mods:namePart>
    <mods:namePart type="given">
      <xsl:value-of select="substring-before(text(),' ')" />
    </mods:namePart>
  </xsl:template>
  
  <xsl:template match="arxiv:affiliation">
    <mods:affiliation>
      <xsl:value-of select="text()" />
    </mods:affiliation>
  </xsl:template>
  
  <xsl:template match="atom:published">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="substring(text(),1,4)" />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="atom:id">
    <mods:identifier type="arxiv">
      <xsl:value-of select="substring-after(text(),'abs/')" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="arxiv:doi">
    <mods:identifier type="doi">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="atom:link[@title='pdf']">
    <mods:location>
      <mods:url>
        <xsl:value-of select="@href" />
      </mods:url>
    </mods:location>
  </xsl:template>
  
  <xsl:template match="atom:summary">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
  </xsl:template>
  
  <xsl:template match="arxiv:journal_ref">
    <mods:relatedItem type="host">
      <mods:genre authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}" valueURI="{concat($WebApplicationBaseURL,'classifications/ubogenre#journal')}" />
      <mods:titleInfo>
        <mods:title>
          <xsl:value-of select="text()" />
        </mods:title>
      </mods:titleInfo>
    </mods:relatedItem>
  </xsl:template>
  
  <xsl:template match="@*|*" />
  
</xsl:stylesheet>
