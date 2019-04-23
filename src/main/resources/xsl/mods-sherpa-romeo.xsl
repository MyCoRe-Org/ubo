<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 37789 $ $Date: 2018-07-19 15:21:07 +0200 (Do, 19 Jul 2018) $ -->
<!-- ============================================== --> 

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan mods i18n"  
>

<xsl:param name="MCR.MODS.SherpaRomeo.LinkISSN" />
<xsl:param name="MCR.MODS.SherpaRomeo.API.URL" />
<xsl:param name="MCR.MODS.SherpaRomeo.API.Key" />

<xsl:template match="mods:mods" mode="romeo">
  <xsl:if test="descendant-or-self::mods:identifier[@type='issn']">
    <xsl:variable name="issn">
      <xsl:for-each select="descendant-or-self::mods:identifier[@type='issn']">
        <xsl:value-of select="text()" />
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="url" select="concat($MCR.MODS.SherpaRomeo.API.URL,'?issn=',$issn,'&amp;ak=',$MCR.MODS.SherpaRomeo.API.Key)" />
    <xsl:apply-templates select="document($url)/romeoapi[publishers/publisher]" />
  </xsl:if>
</xsl:template>

<xsl:template match="romeoapi[publishers/publisher]">
  <div class="card mt-3 bg-alternative">
    <div class="card-body">
      <div class="row">
        <xsl:apply-templates select="publishers/publisher" />
      </div>
    </div>
  </div>
</xsl:template>

<xsl:template match="romeoapi/publishers/publisher">
  <xsl:variable name="issn" select="ancestor::romeoapi/header/parameters/parameter[parametername='issn']/parametervalue" />
  <div class="col-2 align-self-center">
    <a class="float-right" href="{$MCR.MODS.SherpaRomeo.LinkISSN}{$issn}">
      <img src="{$WebApplicationBaseURL}images/romeosmall.gif" width="100" height="54" alt="SHERPA/RoMEO Database" border="0" />
    </a>
  </div>
  <div class="col-10">
    <xsl:apply-templates select="preprints|postprints|pdfversion" />
    <xsl:apply-templates select="ancestor::romeoapi" mode="backlink" />
    <xsl:apply-templates select="romeocolour" />
    <xsl:apply-templates select="paidaccess[string-length(paidaccessurl) &gt; 0]" />
  </div>
</xsl:template>

<xsl:template match="romeoapi" mode="backlink">
  <xsl:variable name="issn" select="header/parameters/parameter[parametername='issn']/parametervalue" />
  <a href="{$MCR.MODS.SherpaRomeo.LinkISSN}{$issn}">Details...</a>
</xsl:template>

<xsl:template match="publisher/preprints[*]|publisher/postprints[*]|publisher/pdfversion[*]">
  <div style="display:inline-block; padding:2px; margin-right:5px; border:1px solid blue;">
    <xsl:value-of select="i18n:translate(concat('ubo.romeo.',name()))" />
    <xsl:text>: </xsl:text>
    <xsl:for-each select="prearchiving|postarchiving|pdfarchiving">
      <xsl:variable name="glyphicon" select="i18n:translate(concat('ubo.romeo.icon.',.))" />
      <xsl:variable name="colour"    select="i18n:translate(concat('ubo.romeo.icon.colour.',.))" />
      <span aria-hidden="true" class="glyphicon glyphicon-{$glyphicon}" style="margin: 0 0.5ex 0 0.5ex; color:{$colour}" />
      <xsl:value-of select="i18n:translate(concat('ubo.romeo.text.',text()))" />  
    </xsl:for-each>
  </div>
</xsl:template>

<xsl:template match="publisher/paidaccess[string-length(paidaccessurl) &gt; 0]">
  <br />
  <a href="{paidaccessurl}">
    <xsl:value-of select="i18n:translate('ubo.romeo.paidaccess')" />
  </a>
</xsl:template>

<xsl:template match="romeocolour">
  <br />
  <a href="{../homeurl}">
    <xsl:value-of select="../name" />
  </a>
  <xsl:text> </xsl:text>
  <xsl:value-of select="i18n:translate('ubo.romeo.colour.isa')" />
  <xsl:text> </xsl:text>
  <span style="background-color:{i18n:translate(concat('ubo.romeo.colour.colour.',.))}; padding:0.3ex; display:inline-block; margin-top:1ex;">
    <xsl:text> SHERPA/RoMEO </xsl:text>
    <xsl:value-of select="." />
  </span>
  <xsl:text> </xsl:text>
  <xsl:value-of select="i18n:translate('ubo.romeo.colour.publisher')" />
  <xsl:text> </xsl:text>
  <br />
  <xsl:value-of select="i18n:translate(concat('ubo.romeo.colour.text.',text()))" disable-output-escaping="yes" />
</xsl:template>

</xsl:stylesheet>
