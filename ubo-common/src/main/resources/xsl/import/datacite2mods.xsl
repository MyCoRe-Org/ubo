<?xml version="1.0" encoding="UTF-8"?>

<!-- https://data.datacite.org/application/vnd.datacite.datacite+xml/10.5524/100005 -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xsl xsi xalan">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />
		
  <xsl:template match="resource">
    <mods:mods>
      <xsl:apply-templates select="titles/title" />
      <xsl:apply-templates select="creators/creator" />
      <mods:originInfo>
        <xsl:apply-templates select="publisher" />
        <xsl:apply-templates select="publicationYear" />
      </mods:originInfo>
      <xsl:apply-templates select="language" />
      <xsl:apply-templates select="subjects/subject" />
      <xsl:apply-templates select="descriptions/description[@descriptionType='Abstract']" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="creator[contains(creatorName,',')]">
    <mods:name type="personal">
      <xsl:apply-templates select="creatorName" />
      <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
      </mods:role>
      <xsl:apply-templates select="nameIdentifier[@nameIdentifierScheme='ORCID']" />
      <xsl:apply-templates select="affiliation" />
    </mods:name>
  </xsl:template>
  
  <xsl:template match="creatorName">
    <mods:namePart type="family">
      <xsl:value-of select="normalize-space(substring-before(.,','))" />
    </mods:namePart>
    <mods:namePart type="given">
      <xsl:value-of select="normalize-space(substring-after(.,','))" />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="nameIdentifier[@nameIdentifierScheme='ORCID']">
    <mods:nameIdentifier type="orcid">
      <xsl:choose>
        <xsl:when test="contains(text(),'orcid.org/')"> 
          <!-- value seems to be URL, strip server name part -->
          <xsl:value-of select="substring-after(text(),'orcid.org/')" />
        </xsl:when>
        <xsl:otherwise>
          <!-- plain ORCID iD -->
          <xsl:value-of select="text()" />
        </xsl:otherwise>
      </xsl:choose>
      
    </mods:nameIdentifier>
  </xsl:template>
  
  <xsl:template match="affiliation">
    <mods:affiliation>
      <xsl:value-of select="text()" />
    </mods:affiliation>
  </xsl:template>
  
  <xsl:template match="publisher">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="publicationYear">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="text()" />
    </mods:dateIssued>
  </xsl:template>
  
  <xsl:template match="language">
    <xsl:for-each select="document(concat('notnull:language:',.))/language/@xmlCode">
      <mods:language>
        <mods:languageTerm authority="rfc5646" type="code">
          <xsl:value-of select="." />
        </mods:languageTerm>
      </mods:language>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="subject">
    <mods:subject>
      <mods:topic>
        <xsl:value-of select="text()" />
      </mods:topic>
    </mods:subject>
  </xsl:template>

  <xsl:template match="description[@descriptionType='Abstract']">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
  </xsl:template>
  
  <xsl:template match="@*|*" />
  
</xsl:stylesheet>
