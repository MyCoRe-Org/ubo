<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:datacite="http://datacite.org/schema/kernel-2.2"
  exclude-result-prefixes="xsl xalan datacite">

<!-- http://ieeexplore.ieee.org/gateway/ipsSearch.jsp?an=4731412 -->

  <xsl:template match="/root">
    <xsl:apply-templates select="document[1]" />
  </xsl:template>
  
  <xsl:template match="document">
    <mods:mods>
      <xsl:apply-templates select="title" />
      <xsl:apply-templates select="authors" />
      <xsl:apply-templates select="self::node()[pubtitle|issn|isbn|volume|issue|spage|epage]" mode="host" />
      <xsl:apply-templates select="doi" />
      <xsl:apply-templates select="arnumber" />
      <xsl:apply-templates select="abstract" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="title|pubtitle">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="authors">
    <xsl:for-each select="xalan:tokenize(text(),';')">
      <mods:name type="personal">
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
        <mods:namePart type="family">
          <xsl:for-each select="xalan:tokenize(.,' ')">
            <xsl:if test="position() = last()">
              <xsl:value-of select="." />
            </xsl:if>
          </xsl:for-each>
        </mods:namePart>
      </mods:name>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="document" mode="host">
    <mods:relatedItem type="host">
      <xsl:apply-templates select="pubtitle" />
      <xsl:apply-templates select="issn|isbn" />
      <xsl:apply-templates select="self::node()[publisher|py]" mode="origin" />
      <xsl:apply-templates select="self::node()[volume|issue|spage|epage]" mode="part" />
    </mods:relatedItem>
  </xsl:template>
  
  <xsl:template match="document" mode="origin">
    <mods:originInfo>
      <xsl:apply-templates select="publisher" />
      <xsl:apply-templates select="py" />
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="publisher">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>
  
  <xsl:template match="py">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="text()" />
    </mods:dateIssued>
  </xsl:template>
  
  <xsl:template match="document" mode="part">
    <mods:part>
      <xsl:apply-templates select="volume" />
      <xsl:apply-templates select="issue" />
      <xsl:apply-templates select="self::node()[spage|epage]" mode="pages" />
    </mods:part>
  </xsl:template>
  
  <xsl:template match="volume">
    <mods:detail type="volume">
      <mods:number>
        <xsl:value-of select="text()" />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="issue">
    <mods:detail type="issue">
      <mods:number>
        <xsl:value-of select="text()" />
      </mods:number>
    </mods:detail>
  </xsl:template>
  
  <xsl:template match="document" mode="pages">
    <mods:extent unit="pages">
      <xsl:apply-templates select="spage" />
      <xsl:apply-templates select="epage" />
    </mods:extent>
  </xsl:template>
  
  <xsl:template match="spage">
    <mods:start>
      <xsl:value-of select="text()" />
    </mods:start>
  </xsl:template>

  <xsl:template match="epage">
    <mods:end>
      <xsl:value-of select="text()" />
    </mods:end>
  </xsl:template>
  
  <xsl:template match="arnumber">
    <mods:identifier type="ieee">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="doi">
    <mods:identifier type="doi">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="issn|isbn">
    <mods:identifier type="{name()}">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="abstract">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
  </xsl:template>

</xsl:stylesheet>