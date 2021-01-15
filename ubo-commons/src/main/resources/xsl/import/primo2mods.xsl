<?xml version="1.0" encoding="UTF-8"?>

<!-- http://primo.ub.uni-due.de/PrimoWebServices/xservice/search/brief?institution=UDE&query=any,contains,cancer+connection&loc=adaptor,primo_central_multiple_fe -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:search="http://www.exlibrisgroup.com/xsd/jaguar/search" 
  xmlns:pnx="http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  exclude-result-prefixes="xsl xalan search pnx xsi">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />
  
  <xsl:template match="/search:SEGMENTS">
    <xsl:apply-templates select="search:JAGROOT/search:RESULT/search:DOCSET/search:DOC[1]/pnx:PrimoNMBib/pnx:record" />
  </xsl:template>
  
  <xsl:template match="pnx:PrimoNMBib/pnx:record">
    <mods:mods>
      <xsl:apply-templates select="pnx:display/pnx:title" />
      <xsl:apply-templates select="pnx:addata/pnx:au" />
      <mods:relatedItem type="host">
        <xsl:apply-templates select="pnx:addata/pnx:jtitle|pnx:addata/pnx:btitle" />
        <xsl:apply-templates select="pnx:addata/pnx:isbn" />
        <xsl:apply-templates select="pnx:addata/pnx:issn" />
        <xsl:apply-templates select="pnx:addata/pnx:eissn" />
        <mods:part>
          <xsl:apply-templates select="pnx:addata/pnx:volume" />
          <xsl:apply-templates select="pnx:addata/pnx:issue" />
          <mods:extent unit="pages">
            <xsl:apply-templates select="pnx:addata/pnx:spage" />
            <xsl:apply-templates select="pnx:addata/pnx:epage" />
          </mods:extent>
        </mods:part>
      </mods:relatedItem>
      <mods:originInfo>
        <xsl:apply-templates select="pnx:facets/pnx:creationdate" />
      </mods:originInfo>
      <xsl:apply-templates select="pnx:addata/pnx:doi" />
      <mods:location>
        <xsl:apply-templates select="pnx:addata/pnx:url" />
      </mods:location>
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="pnx:display/pnx:title|pnx:addata/pnx:jtitle|pnx:addata/pnx:btitle">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="pnx:addata/pnx:au">
    <mods:name type="personal">
      <xsl:choose>
        <xsl:when test="contains(text(),',')">
          <mods:namePart type="family">
            <xsl:value-of select="normalize-space(substring-before(text(),','))" />
          </mods:namePart>
          <mods:namePart type="given">
            <xsl:value-of select="normalize-space(substring-after(text(),','))" />
          </mods:namePart>
        </xsl:when>
        <xsl:otherwise>
          <mods:namePart type="family">
            <xsl:value-of select="normalize-space(substring-after(text(),' '))" />
          </mods:namePart>
          <mods:namePart type="given">
            <xsl:value-of select="normalize-space(substring-before(text(),' '))" />
          </mods:namePart>
        </xsl:otherwise>
      </xsl:choose>
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
  </mods:name>
  </xsl:template>
  
  <xsl:template match="pnx:addata/pnx:issn|pnx:addata/pnx:eissn">
    <mods:identifier type="issn">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="pnx:addata/pnx:isbn">
    <mods:identifier type="isbn">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="pnx:addata/pnx:volume">
    <mods:detail type="volume">
      <mods:number>
        <xsl:value-of select="text()" />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="pnx:addata/pnx:issue">
    <mods:detail type="issue">
      <mods:number>
        <xsl:value-of select="text()" />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="pnx:addata/pnx:spage">
    <mods:start>
      <xsl:value-of select="text()" />
    </mods:start>
  </xsl:template>

  <xsl:template match="pnx:addata/pnx:epage">
    <mods:end>
      <xsl:value-of select="text()" />
    </mods:end>
  </xsl:template>

  <xsl:template match="pnx:addata/pnx:doi">
    <mods:identifier type="doi">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="pnx:facets/pnx:creationdate">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="text()" />
    </mods:dateIssued>
  </xsl:template>
  
  <xsl:template match="pnx:addata/pnx:url">
    <mods:url>
      <xsl:value-of select="text()" />
    </mods:url>
  </xsl:template>

</xsl:stylesheet>