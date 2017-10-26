<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:work="http://www.orcid.org/ns/work"
  xmlns:common="http://www.orcid.org/ns/common"
  xmlns:activities="http://www.orcid.org/ns/activities"
  exclude-result-prefixes="xsl work common activities">

  <xsl:template match="/activities:works">
    <mods:modsCollection>
      <xsl:apply-templates select="activities:group/work:work-summary" />      
    </mods:modsCollection>
  </xsl:template>

  <xsl:template match="/work:work|work:work-summary">
    <mods:mods>
      <xsl:apply-templates select="work:title/common:title" />
      <xsl:apply-templates select="common:publication-date/common:year" />
      <xsl:apply-templates select="common:external-ids/common:external-id" />
      <xsl:apply-templates select="@path" />
    </mods:mods>
  </xsl:template>

  <xsl:template match="work:title/common:title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="common:publication-date/common:year">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="text()" />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="common:external-id[common:external-id-type='doi']">
    <mods:identifier type="doi">
      <xsl:value-of select="common:external-id-value" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="common:external-id[common:external-id-type='eid'][common:external-id-value[starts-with(.,'2-s2.0-')]]">
    <mods:identifier type="scopus">
      <xsl:value-of select="substring-after(common:external-id-value,'2-s2.0-')" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="work:work-summary/@path|work:work/@path">
    <mods:identifier type="orcid">
      <xsl:value-of select="substring-after(.,'/')" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="*|text()" />

</xsl:stylesheet>
