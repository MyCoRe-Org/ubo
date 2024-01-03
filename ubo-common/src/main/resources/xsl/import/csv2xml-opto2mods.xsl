<?xml version="1.0"?>

<!-- Converts the CSV version of http://www.oe.uni-due.de/start/forschung/Publikationen/index.asp?l=de to MODS -->

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
  <xsl:variable name="genres">
    <xsl:choose>
      <xsl:when test="Journal='WAHR' or Zeitschrift">article journal</xsl:when>
      <xsl:when test="Conference='WAHR' and Buch">chapter proceedings</xsl:when>
      <xsl:when test="Book='WAHR' or Buch">chapter collection</xsl:when>
      <xsl:otherwise>speech proceedings</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="hasHost" select="Zeitschrift or Buch or Ausgabe or Nummer or Seiten or Issn" />
  
  <mods:mods>
    <mods:genre type="intern" authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}" valueURI="{concat($WebApplicationBaseURL,'classifications/ubogenre#', substring-before($genres,' '))}" />

    <xsl:apply-templates select="Titel1" />
    <xsl:apply-templates select="Autor1|Autor2|Autor3|Autor4|Autor5|Autor6|Autor7|Autor8|Autor9|Autor10" />
    <xsl:if test="starts-with($genres,'article') and Jahr">
      <mods:originInfo>
        <xsl:apply-templates select="Jahr" /> 
      </mods:originInfo>
    </xsl:if>
    <xsl:if test="$hasHost">
      <mods:relatedItem type="host">
        <mods:genre type="intern" authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}" valueURI="{concat($WebApplicationBaseURL,'classifications/ubogenre#', substring-after($genres,' '))}" />
        <xsl:apply-templates select="Zeitschrift|Buch" />
        <xsl:apply-templates select="Konferenz" />
        <xsl:apply-templates select="Issn" />
        <xsl:if test="contains($genres,'proceedings') or contains($genres,'collection')">
          <xsl:if test="Ort|Verlag|Jahr">
            <mods:originInfo>
              <xsl:apply-templates select="Ort" />
              <xsl:apply-templates select="Verlag" />
              <xsl:apply-templates select="Jahr" />
            </mods:originInfo>
          </xsl:if>
        </xsl:if>
        <xsl:call-template name="part" />
      </mods:relatedItem>    
    </xsl:if>
    <xsl:apply-templates select="doi" />
    <xsl:if test="www|PDF">
      <mods:location>
        <xsl:apply-templates select="www" />
        <xsl:apply-templates select="PDF" />
      </mods:location>
    </xsl:if>
    <xsl:apply-templates select="Abstract" />
    <xsl:call-template name="fach" />
    <xsl:call-template name="origin" />
  </mods:mods>
</xsl:template>

<xsl:template match="Titel1|Zeitschrift|Buch|Konferenz[not(../Zeitschrift or ../Buch)]">
  <mods:titleInfo>
    <mods:title>
      <xsl:value-of select="." />
    </mods:title>
  </mods:titleInfo>
</xsl:template>

<xsl:template match="Autor1|Autor2|Autor3|Autor4|Autor5|Autor6|Autor7|Autor8|Autor9|Autor10">
  <xsl:for-each select="xalan:tokenize(.,',')">
    <mods:name type="personal">
      <mods:namePart type="family">
        <xsl:for-each select="xalan:tokenize(.)">
          <xsl:variable name="first" select="substring(string(.),1,1)" />
          <xsl:variable name="isUpperCase" select="translate($first,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','')=''" />
          <xsl:if test="(position() = last()) or not($isUpperCase)">
            <xsl:value-of select="." />
            <xsl:if test="position() != last()">
              <xsl:text> </xsl:text>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>
      </mods:namePart>
      <mods:namePart type="given">
        <xsl:for-each select="xalan:tokenize(.)">
          <xsl:variable name="first" select="substring(string(.),1,1)" />
          <xsl:variable name="isUpperCase" select="translate($first,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','')=''" />
          <xsl:if test="position() != last() and $isUpperCase">
            <xsl:if test="position() != 1">
              <xsl:text> </xsl:text>
            </xsl:if>
            <xsl:value-of select="." />
          </xsl:if>
        </xsl:for-each>
      </mods:namePart>
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:for-each>
</xsl:template>

<xsl:template match="Konferenz[../Zeitschrift or ../Buch]">
  <mods:name type="conference">
    <mods:namePart>
      <xsl:value-of select="." />
    </mods:namePart>
  </mods:name>
</xsl:template>

<xsl:template match="Verlag">
  <mods:publisher>
    <xsl:value-of select="." />
  </mods:publisher>
</xsl:template>

<xsl:template match="Ort">
  <mods:place>
    <mods:placeTerm>
      <xsl:value-of select="." />
    </mods:placeTerm>
  </mods:place>
</xsl:template>

<xsl:template match="Jahr">
  <mods:dateIssued encoding="w3cdtf">
    <xsl:value-of select="." />
  </mods:dateIssued>
</xsl:template>

<xsl:template name="part">
  <xsl:if test="Ausgabe|Nummer|Seiten">
    <mods:part>
      <xsl:apply-templates select="Ausgabe" />
      <xsl:apply-templates select="Nummer" />
      <xsl:apply-templates select="Seiten" />
    </mods:part>
  </xsl:if>
</xsl:template>

<xsl:template match="Ausgabe[contains(.,'ol.')]">
  <mods:detail type="volume">
    <mods:number>
      <xsl:value-of select="normalize-space(substring-after(.,'ol.'))" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="Ausgabe[not(contains(.,'ol.'))]">
  <mods:detail type="volume">
    <mods:number>
      <xsl:value-of select="." />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="Nummer[starts-with(.,'no.') or starts-with(.,'No.')]">
  <mods:detail type="issue">
    <mods:number>
      <xsl:value-of select="normalize-space(substring-after(.,'o.'))" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="Nummer[not(starts-with(.,'no.') or starts-with(.,'No.'))]">
  <mods:detail type="issue">
    <mods:number>
      <xsl:value-of select="." />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="Seiten" >
  <xsl:copy-of xmlns:pages="xalan://org.mycore.mods.MCRMODSPagesHelper" select="pages:buildExtentPagesNodeSet(text())" />
</xsl:template>

<xsl:template match="Issn[starts-with(.,'ISSN ')]">
  <mods:identifier type="issn">
    <xsl:value-of select="substring-after(.,'ISSN ')" />
  </mods:identifier>
</xsl:template>

<xsl:template match="Issn[starts-with(.,'ISBN ')]">
  <mods:identifier type="isbn">
    <xsl:value-of select="substring-after(.,'ISBN ')" />
  </mods:identifier>
</xsl:template>

<xsl:template match="doi">
  <mods:identifier type="doi">
    <xsl:value-of select="." />
  </mods:identifier>
</xsl:template>

<xsl:template match="www">
  <mods:url>
    <xsl:value-of select="." />
  </mods:url>
</xsl:template>

<xsl:template match="PDF">
  <mods:url>
    <xsl:text>http://www.oe.uni-due.de/research/publications/</xsl:text>
    <xsl:value-of select="." />
    <xsl:text>.pdf</xsl:text>
  </mods:url>
</xsl:template>

<xsl:template match="Abstract">
  <mods:abstract>
    <xsl:value-of select="." />
  </mods:abstract>
</xsl:template>

<xsl:template name="fach">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/fachreferate</xsl:variable>
  <mods:classification valueURI="{$uri}#ele" authorityURI="{$uri}"/>
</xsl:template>

<xsl:template name="origin">
  <xsl:variable name="uri">https://bibliographie.ub.uni-due.de/classifications/ORIGIN</xsl:variable>
  <mods:classification valueURI="{$uri}#18.02.07" authorityURI="{$uri}"/>
</xsl:template>

</xsl:stylesheet>
