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
      <xsl:apply-templates select="work:title" />
      <xsl:apply-templates select="work:title/common:translated-title" />
      <xsl:apply-templates select="work:contributors/work:contributor" />
      <xsl:apply-templates select="work:journal-title" />
      <xsl:apply-templates select="common:publication-date/common:year" />
      <xsl:apply-templates select="work:short-description" />
      <xsl:apply-templates select="work:url" />
      <xsl:apply-templates select="common:external-ids/common:external-id" />
      <xsl:apply-templates select="common:language-code" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="work:type">
    <mods:genre>
      <xsl:value-of select="text()" />
    </mods:genre>
  </xsl:template>
  
  <xsl:template match="work:title">
    <mods:titleInfo>
      <xsl:apply-templates select="common:title" />
      <xsl:apply-templates select="common:subtitle" />
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="common:title">
    <mods:title>
      <xsl:value-of select="text()" />
    </mods:title>
  </xsl:template>
  
  <xsl:template match="common:subtitle">
    <mods:subTitle>
      <xsl:value-of select="text()" />
    </mods:subTitle>
  </xsl:template>

  <xsl:template match="work:title/common:translated-title">
    <mods:titleInfo type="translated">
      <xsl:for-each select="@language-code">
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="." />
        </xsl:attribute>
      </xsl:for-each>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="work:journal-title">
    <mods:relatedItem type="host">
      <mods:titleInfo>
        <mods:title>
          <xsl:value-of select="text()" />
        </mods:title>
      </mods:titleInfo>
    </mods:relatedItem>  
  </xsl:template>

  <xsl:template match="work:contributors/work:contributor">
    <mods:name type="personal">
      <xsl:apply-templates select="work:credit-name" />
      <xsl:call-template name="contributor-role" /> 
      <xsl:apply-templates select="common:contributor-orcid" />
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
  
  <xsl:template name="contributor-role">
    <mods:role>
      <mods:roleTerm type="code" authority="marcrelator">
        <xsl:choose>
          <xsl:when test="work:contributor-attributes/work:contributor-role='author'">aut</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='assignee'">asg</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='editor'">edt</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='chair-or-translator'">trl</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='co-investigator'">ctb</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='co-inventor'">ctb</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='graduate-student'">ctb</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='other-inventor'">ctb</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='principal-investigator'">res</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='postdoctoral-researcher'">res</xsl:when>
          <xsl:when test="work:contributor-attributes/work:contributor-role='support-staff'">ctb</xsl:when>
          <xsl:otherwise>aut</xsl:otherwise>
        </xsl:choose>
      </mods:roleTerm>
    </mods:role>
  </xsl:template>
  
  <xsl:template match="common:contributor-orcid">
    <mods:nameIdentifier type="orcid">
      <xsl:value-of select="common:path" />
    </mods:nameIdentifier>
  </xsl:template>

  <xsl:template match="common:publication-date/common:year">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="text()" />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="work:short-description">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
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
  
  <xsl:template match="common:language-code">
    <mods:language>
      <mods:languageTerm authority="rfc4646" type="code">
        <xsl:value-of select="document(concat('language:',text()))/language/@xmlCode" />
      </mods:languageTerm>
    </mods:language>
  </xsl:template>
  
  <xsl:template match="*|text()" />

</xsl:stylesheet>
