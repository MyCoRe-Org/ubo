<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:work="http://www.orcid.org/ns/work"
  xmlns:common="http://www.orcid.org/ns/common"
  xmlns:activities="http://www.orcid.org/ns/activities"
  exclude-result-prefixes="xsl xalan work common activities">

  <xsl:template match="/work:work">
    <mods:mods>
      <xsl:apply-templates select="work:type" />
      <xsl:apply-templates select="work:title/common:title" />
      <xsl:apply-templates select="work:contributors/work:contributor[work:credit-name]" />
      <xsl:apply-templates select="work:journal-title" mode="host" />
      <xsl:apply-templates select="common:publication-date/common:year" />
      <xsl:apply-templates select="work:url" />
      <xsl:apply-templates select="common:external-ids/common:external-id" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="work:type">
    <mods:genre>
      <xsl:value-of select="text()" />
    </mods:genre>
  </xsl:template>
  
  <xsl:template match="work:title/common:title|work:journal-title">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="work:journal-title" mode="host">
    <mods:relatedItem type="host">
      <xsl:apply-templates select="." />
    </mods:relatedItem>  
  </xsl:template>

  <xsl:template match="work:contributors/work:contributor">
    <mods:name type="personal">
      <xsl:apply-templates select="work:credit-name" />
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:template>
  
  <xsl:template match="work:credit-name[contains(.,',')]">
    <mods:namePart type="family">
      <xsl:value-of select="normalize-space(substring-before(.,','))" />
    </mods:namePart>
    <mods:namePart type="given">
      <xsl:value-of select="normalize-space(substring-after(.,','))" />
    </mods:namePart>
  </xsl:template>
  
  <xsl:template match="work:credit-name[not(contains(.,','))]">
    <mods:namePart type="family">
      <xsl:for-each select="xalan:tokenize(.,' ')">
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </mods:namePart>
    <mods:namePart type="given">
      <xsl:for-each select="xalan:tokenize(.,' ')">
        <xsl:if test="position() != last()">
          <xsl:if test="position() &gt; 1">
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </mods:namePart>
  </xsl:template>

  <xsl:template match="common:publication-date/common:year">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="text()" />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="work:url">
    <mods:location>
      <mods:url>
        <xsl:value-of select="text()" />
      </mods:url>
    </mods:location>
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
  
  <xsl:template match="*|text()" />

</xsl:stylesheet>
