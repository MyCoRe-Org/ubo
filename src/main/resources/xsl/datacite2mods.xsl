<?xml version="1.0" encoding="UTF-8"?>

<!-- https://data.datacite.org/application/vnd.datacite.datacite+xml/10.5524/100005 -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:datacite="http://datacite.org/schema/kernel-2.2"
  exclude-result-prefixes="xsl xsi xalan datacite">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />
		
  <xsl:template match="datacite:resource">
    <mods:mods>
      <xsl:apply-templates select="datacite:titles/datacite:title" />
      <xsl:apply-templates select="datacite:creators/datacite:creator" />
      <mods:originInfo>
        <xsl:apply-templates select="datacite:publisher" />
        <xsl:apply-templates select="datacite:publicationYear" />
      </mods:originInfo>
      <xsl:apply-templates select="datacite:language" />
      <xsl:apply-templates select="datacite:subjects/datacite:subject" />
      <xsl:apply-templates select="datacite:descriptions/datacite:description[@descriptionType='Abstract']" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="datacite:title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="datacite:creator[contains(datacite:creatorName,',')]">
    <mods:name type="personal">
      <xsl:apply-templates select="datacite:creatorName" />
      <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
      </mods:role>
      <xsl:apply-templates select="datacite:nameIdentifier[@nameIdentifierScheme='ORCID']" />
      <xsl:apply-templates select="datacite:affiliation" />
    </mods:name>
  </xsl:template>
  
  <xsl:template match="datacite:creatorName">
    <mods:namePart type="family">
      <xsl:value-of select="normalize-space(substring-before(.,','))" />
    </mods:namePart>
    <mods:namePart type="given">
      <xsl:value-of select="normalize-space(substring-after(.,','))" />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="datacite:nameIdentifier[@nameIdentifierScheme='ORCID']">
    <mods:nameIdentifier type="orcid">
      <xsl:value-of select="text()" />
    </mods:nameIdentifier>
  </xsl:template>
  
  <xsl:template match="datacite:affiliation">
    <mods:affiliation>
      <xsl:value-of select="text()" />
    </mods:affiliation>
  </xsl:template>
  
  <xsl:template match="datacite:publisher">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="datacite:publicationYear">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="text()" />
    </mods:dateIssued>
  </xsl:template>
  
  <xsl:template match="datacite:language">
    <mods:language>
      <mods:languageTerm authority="rfc4646" type="code">
        <xsl:value-of select="document(concat('language:',.))/language/@xmlCode" />
      </mods:languageTerm>
    </mods:language>
  </xsl:template>

  <xsl:template match="datacite:subject">
    <mods:subject>
      <mods:topic>
        <xsl:value-of select="text()" />
      </mods:topic>
    </mods:subject>
  </xsl:template>

  <xsl:template match="datacite:description[@descriptionType='Abstract']">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
  </xsl:template>
  
  <xsl:template match="@*|*" />
  
</xsl:stylesheet>