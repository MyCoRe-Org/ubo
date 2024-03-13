<?xml version="1.0" encoding="UTF-8"?>

<!-- Post-processor for editor forms producing MODS -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  exclude-result-prefixes="xsl xalan xlink">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="MCR.PICA2MODS.DATABASE" select="'gvk'" />

  <!-- Transform URLs containing DOI to identifier field of type DOI -->
  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates />
      <xsl:apply-templates select="mods:location/mods:url[contains(text(),'doi.org/10.')]" mode="url2doi" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:location/mods:url[contains(text(),'doi.org/10.')]" />

  <xsl:template match="mods:location/mods:url[contains(text(),'doi.org/10.')]" mode="url2doi">
    <mods:identifier type="doi">
      <xsl:value-of select="substring-after(text(),'doi.org/')" />
    </mods:identifier>
  </xsl:template>

  <!-- In editor, categories are coded as <mods:classification classID="CLASSIFICATION">CATEGORYID</mods:classification> -->
  <!-- In persistent store, use authorityURI and valueURI attributes instead -->
  <xsl:template match="mods:classification[@classID]">
    <mods:classification authorityURI="{$WebApplicationBaseURL}classifications/{@classID}" valueURI="{$WebApplicationBaseURL}classifications/{@classID}#{text()}" />
  </xsl:template>

  <xsl:template match="mods:roleTerm[@classID]">
    <mods:roleTerm type="text" authorityURI="{$WebApplicationBaseURL}classifications/{@classID}" valueURI="{$WebApplicationBaseURL}classifications/{@classID}#{text()}">
      <xsl:value-of select="text()" />
    </mods:roleTerm>
  </xsl:template>

  <!-- In editor, all variants of page numbers are edited in a single text field -->
  <xsl:template match="mods:part/mods:extent[@unit='pages']" xmlns:pages="xalan://org.mycore.mods.MCRMODSPagesHelper">
    <xsl:copy-of select="pages:buildExtentPagesNodeSet(mods:list/text())" />
  </xsl:template>
  
  <!-- Derive dateIssued from dateOther if not present -->
  <xsl:template match="mods:dateOther[(@type='accepted') and not(../mods:dateIssued)]">
    <mods:dateIssued>
      <xsl:copy-of select="@encoding" />
      <xsl:value-of select="substring(.,1,4)" />
    </mods:dateIssued>
    <xsl:copy-of select="." />
  </xsl:template>
  
  <!-- Move volume number from series up to titleInfo as partNumber if series is not specified -->
  <xsl:template match="mods:titleInfo">
    <xsl:copy>
      <xsl:apply-templates select="@*|*" />
      <xsl:for-each select="../mods:relatedItem[@type='series'][string-length(mods:titleInfo/mods:title)=0]/mods:part/mods:detail[@type='volume']">
        <mods:partNumber>
          <xsl:value-of select="*" />
        </mods:partNumber>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='ppn']">
    <xsl:variable name="database">
      <xsl:choose>
        <xsl:when test="@transliteration and string-length(@transliteration) &gt; 0">
          <xsl:value-of select="@transliteration" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$MCR.PICA2MODS.DATABASE" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <mods:identifier type="uri">
      <xsl:value-of select="concat('https://uri.gbv.de/document/', $database, ':ppn:',text())" />
    </mods:identifier>
  </xsl:template>

</xsl:stylesheet>
