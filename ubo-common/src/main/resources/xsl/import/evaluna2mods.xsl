<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xsl xalan"
>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/interface">
    <xsl:apply-templates select="response[@type='publications']" />
  </xsl:template>

  <xsl:variable name="institutions" select="/interface/response[@type='institutions']" />

  <xsl:template match="response[@type='publications']">
    <mods:modsCollection>
      <xsl:apply-templates select="publication" />
    </mods:modsCollection>
  </xsl:template>
  
  <xsl:template match="publication[@version='2.0']">
   <mods:mods>
     <xsl:apply-templates select="citation" />
     <xsl:apply-templates select="." mode="recordInfo" />
   </mods:mods>
  </xsl:template>

  <xsl:template match="publication" mode="recordInfo">
    <mods:recordInfo>
      <mods:recordContentSource>http://e-med.biblio.evaluna.net</mods:recordContentSource>
      <mods:recordChangeDate encoding="w3cdtf">
        <xsl:value-of select="translate(@last_update,' ','T')" />
      </mods:recordChangeDate>
    </mods:recordInfo>
  </xsl:template>
  
  <xsl:template match="citation[@type='journal article']">
    <xsl:apply-templates select="@type" />
    <xsl:apply-templates select="title" />
    <xsl:apply-templates select="../authors" />
    <xsl:apply-templates select="journal" />
    <xsl:apply-templates select="abstract" />
    <xsl:apply-templates select="../keywords/keyword" />
    <xsl:apply-templates select="language" />
    <xsl:apply-templates select="../@id" />
    <xsl:apply-templates select="pmid" />
    <xsl:apply-templates select="isi_loc" />
  </xsl:template> 
  
  <xsl:template match="citation">
    <xsl:message>
      <xsl:text>No mapping for </xsl:text>
      <xsl:value-of select="@type" />
    </xsl:message>
  </xsl:template>

  <xsl:template match="citation/@type">
    <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#article" />
  </xsl:template>

  <xsl:template match="title">
    <xsl:variable name="title" select="concat(substring(text(),1,string-length(text())-1),translate(substring(text(),string-length(text())),'.:',''))" />
    
    <mods:titleInfo>
      <mods:title>
        <xsl:choose>
          <xsl:when test="contains($title,': ')">
            <xsl:value-of select="substring-before($title,': ')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$title" />
          </xsl:otherwise>
        </xsl:choose>
      </mods:title>
      <xsl:if test="contains($title,': ')">
        <mods:subTitle>
          <xsl:value-of select="substring-after($title,': ')" />
        </mods:subTitle>
      </xsl:if>
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="authors">
    <xsl:apply-templates select="author">
      <xsl:sort select="@pos" data-type="number" />
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="author[(@collective='yes') or (@investigator='no') or not(@institution='0')]">
    <mods:name type="personal">
      <xsl:apply-templates select="lastname" />
      <xsl:call-template name="firstname" />
      <xsl:apply-templates select="@institution" />
      <xsl:apply-templates select="@investigator" />
    </mods:name>
  </xsl:template>
  
  <xsl:template match="author/@investigator">
    <mods:role>
      <mods:roleTerm type="code" authority="marcrelator">
        <xsl:choose>
          <xsl:when test=".='no'">aut</xsl:when>
          <xsl:when test=".='yes'">ctb</xsl:when>
        </xsl:choose>
      </mods:roleTerm>
    </mods:role>
</xsl:template>
  
  <xsl:template match="lastname">
    <mods:namePart type="family">
      <xsl:value-of select="text()" />
    </mods:namePart>
  </xsl:template>

  <xsl:template name="firstname">
    <mods:namePart type="given">
      <xsl:choose>
        <xsl:when test="firstname[string-length() &gt; 0]">
          <xsl:value-of select="firstname" />
        </xsl:when>
        <xsl:when test="initials[string-length() &gt; 2]">
          <xsl:value-of select="initials" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="str:tokenize(initials,'')" xmlns:str="http://exslt.org/strings">
            <xsl:value-of select="." />
            <xsl:text>.</xsl:text>
            <xsl:if test="position() != last()">
              <xsl:text> </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </mods:namePart>
  </xsl:template>

  <xsl:template match="@institution[number(.) != 0]">
    <mods:affiliation>
      <xsl:value-of select="$institutions/institution[@id=current()]/fullname" />
    </mods:affiliation>
  </xsl:template>
  
  <xsl:template match="@institution" />

  <xsl:template match="journal">
    <xsl:apply-templates select="year" />
    <mods:relatedItem type="host">
      <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#journal" />
      <xsl:apply-templates select="abbreviation" />
      <xsl:apply-templates select="issn|essn" />
      <mods:part>
        <xsl:apply-templates select="volume" />
        <xsl:apply-templates select="issue" />
        <xsl:apply-templates select="../pages" />
      </mods:part>
    </mods:relatedItem>
  </xsl:template>
  
  <xsl:template match="year">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="." />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>
  
  <xsl:template match="abbreviation">
    <mods:titleInfo type="abbreviated">
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="issn|essn">
    <mods:identifier type="issn">
      <xsl:value-of select="." />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="volume">
    <mods:detail type="volume">
      <mods:number>
        <xsl:value-of select="." />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="issue">
    <mods:detail type="issue">
      <mods:number>
        <xsl:value-of select="." />
      </mods:number>
    </mods:detail>
  </xsl:template>
  
  <xsl:template match="pages">
    <xsl:copy-of xmlns:pages="xalan://org.mycore.mods.MCRMODSPagesHelper" select="pages:buildExtentPagesNodeSet(text())" />
  </xsl:template>

  <xsl:template match="abstract">
    <mods:abstract>
      <xsl:value-of select="text()" />
    </mods:abstract>
  </xsl:template>

  <xsl:template match="keyword">
    <mods:subject>
      <mods:topic authority="{@source}">
        <xsl:value-of select="." />
      </mods:topic>
    </mods:subject>
  </xsl:template>

  <xsl:template match="language">
    <!-- Find language with matching label in any language, or with matching ID in any supported code schema -->
    <xsl:variable name="given" select="translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
    <xsl:for-each select="document('classification:metadata:-1:children:rfc4646')/mycoreclass/categories/category[@ID=$given or label[translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')=$given]][1]">
      <mods:language>
        <mods:languageTerm authority="rfc4646" type="code">
          <xsl:value-of select="@ID" />
        </mods:languageTerm>
      </mods:language>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="publication/@id">
    <mods:identifier type="evaluna">
      <xsl:value-of select="." />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="pmid">
    <mods:identifier type="pubmed">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="isi_loc">
    <mods:identifier type="isi">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="*[string-length(text()) = 0]" priority="1" /> <!-- ignore empty elements -->

  <xsl:template match="*|@*|text()" />

</xsl:stylesheet>
