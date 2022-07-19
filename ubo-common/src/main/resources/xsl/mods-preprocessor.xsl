<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Pre-processor for editor forms reading MODS -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="MCR.PICA2MODS.DATABASE" select="'gvk'" />

  <!-- In editor, categories are coded as <mods:classification classID="CLASSIFICATION">CATEGORYID</mods:classification> -->
  <!-- In persistent store, authorityURI and valueURI attributes instead are used-->
  <xsl:template match="mods:classification[@valueURI]">
    <mods:classification classID="{substring-after(@authorityURI,'classifications/')}">
      <xsl:value-of select="substring-after(@valueURI,'#')" />
    </mods:classification>
  </xsl:template>
  
  <xsl:template match="mods:roleTerm[@valueURI]">
    <mods:roleTerm type="text" classID="{substring-after(@authorityURI,'classifications/')}">
      <xsl:value-of select="substring-after(@valueURI,'#')" />
    </mods:roleTerm>
  </xsl:template>

  <!-- In editor, volume number is always edited as if belonging to a series, not as partNumber currently -->
  <xsl:template match="mods:titleInfo[mods:partNumber][not(../mods:relatedItem[@type='series'])]">
    <xsl:copy>
      <xsl:apply-templates select="@*|mods:nonSort|mods:title|mods:subTitle|mods:partName" />
    </xsl:copy>
    <mods:relatedItem type="series">
      <mods:part>
        <mods:detail type="volume">
          <mods:number>
            <xsl:value-of select="mods:partNumber" />
          </mods:number>
        </mods:detail>
      </mods:part>
    </mods:relatedItem>
  </xsl:template>

  <!-- In editor, all variants of page numbers are edited in a single text field -->
  <xsl:template match="mods:extent[@unit='pages']">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <mods:list>
        <xsl:apply-templates select="mods:*" />
      </mods:list>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="mods:start">
    <xsl:value-of select="i18n:translate('ubo.pages.abbreviated.multiple')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:end">
    <xsl:text> - </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:total[../mods:start]">
    <xsl:text> (</xsl:text>
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="mods:total">
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
  </xsl:template>

  <xsl:template match="mods:list">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:identifier[@type='uri' and (contains(text(),'PPN') or contains(text(),'ppn'))]">
    <xsl:if test="not(../mods:identifier[@type='ppn'])">
      <mods:identifier type="ppn">
        <xsl:attribute name="transliteration">
          <xsl:choose>
            <xsl:when test="contains(., ':ppn:')">
              <xsl:value-of select="substring-after(substring-before(., ':ppn:'),'http://uri.gbv.de/document/')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$MCR.PICA2MODS.DATABASE"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="contains(., 'PPN=')"><xsl:value-of select="substring-after(., 'PPN=')" /></xsl:when>
          <xsl:otherwise><xsl:value-of select="substring-after(., ':ppn:')"/></xsl:otherwise>
        </xsl:choose>
      </mods:identifier>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
