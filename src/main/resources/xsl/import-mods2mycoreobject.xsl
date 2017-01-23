<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl">

  <!-- default templates: just copy -->
  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()|comment()' />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="mods:modsCollection">
    <import>
      <xsl:apply-templates select="mods:mods|comment()" />
    </import>
  </xsl:template>

  <xsl:template match="mods:mods">
    <mycoreobject ID="ubo_mods_00000000" label="ubo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="datamodel-mods.xsd">
      <structure />
      <metadata>
        <def.modsContainer class="MCRMetaXML">
          <modsContainer>
            <mods:mods>
              <xsl:apply-templates select="*" />
              <xsl:apply-templates select="comment()" />
            </mods:mods>
           </modsContainer>
         </def.modsContainer>
       </metadata>
       <service>
         <servflags class="MCRMetaLangText">
           <servflag type="status" inherited="0" form="plain">imported</servflag>
         </servflags>
       </service>
     </mycoreobject>
  </xsl:template>

  <!-- the following templates actually maps BibTeX publication types to currently used internal publication types -->

  <xsl:template match="mods:mods/mods:genre[1]">
    <mods:genre type="intern">
      <xsl:choose>
        <xsl:when test=". = 'conference'">chapter</xsl:when>
        <xsl:when test=". = 'incollection'">chapter</xsl:when>
        <xsl:when test=". = 'inproceedings'">chapter</xsl:when>
        <xsl:when test=". = 'inbook'">chapter</xsl:when>
        <xsl:when test=". = 'article'">article</xsl:when>
        <xsl:when test=". = 'phdthesis'">dissertation</xsl:when>
        <xsl:when test=". = 'book'">book</xsl:when>
        <xsl:when test=". = 'booklet'">book</xsl:when>
        <xsl:when test=". = 'proceedings'">proceedings</xsl:when>
        <xsl:when test=". = 'manual'">book</xsl:when>
        <xsl:when test=". = 'techreport'">book</xsl:when>
        <xsl:when test=". = 'misc'">book</xsl:when>
        <xsl:when test=". = 'unpublished'">book</xsl:when>
        <xsl:when test=". = 'other'">book</xsl:when> 
        <xsl:otherwise>article</xsl:otherwise>
      </xsl:choose>
    </mods:genre>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='host']/mods:genre[1]">
    <xsl:for-each select="../../mods:genre[1]"> 
      <mods:genre type="intern">
        <xsl:choose>
        <xsl:when test=". = 'conference'">proceedings</xsl:when>
        <xsl:when test=". = 'incollection'">collection</xsl:when>
        <xsl:when test=". = 'inproceedings'">proceedings</xsl:when>
        <xsl:when test=". = 'inbook'">collection</xsl:when>
        <xsl:when test=". = 'article'">journal</xsl:when>
        <xsl:when test=". = 'techreport'">collection</xsl:when>
        <xsl:when test=". = 'misc'">collection</xsl:when>
        <xsl:when test=". = 'unpublished'">collection</xsl:when>
        <xsl:when test=". = 'other'">collection</xsl:when> 
          <xsl:otherwise>journal</xsl:otherwise>
        </xsl:choose>
      </mods:genre>
    </xsl:for-each>
  </xsl:template>

  <!-- add genre to series, if missing -->
  <xsl:template match="mods:relatedItem[@type='series'][not(mods:genre)]">
    <xsl:copy>
      <xsl:apply-templates select="@*|*" />
      <mods:genre type="intern">series</mods:genre>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:genre" /> <!-- Ignore more than one genre -->

  <xsl:template match="mods:name[mods:etAl]"> <!-- Ignore "et al." names, currently not supported -->
    <xsl:comment>"et al." wird zur Zeit vom Datenmodell nicht unterstützt und ignoriert.</xsl:comment>
  </xsl:template>

</xsl:stylesheet>