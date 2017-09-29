<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  exclude-result-prefixes="xsl"  
>

<xsl:template name="copy-and-apply"> <!-- copy supported elements and attributes calling this template -->
  <xsl:copy> 
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="*|@*" /> <!-- ignore all other unsupported elements and attributes -->

<xsl:template match="mods:mods">
  <xsl:copy>
    <xsl:apply-templates select="mods:genre|mods:titleInfo|mods:name|mods:classification|mods:originInfo|mods:dateIssued|mods:physicalDescription|mods:identifier|mods:relatedItem|mods:note|mods:extension|mods:location|mods:subject|mods:abstract|mods:language" />
  </xsl:copy>
</xsl:template>

<xsl:template match="mods:titleInfo|mods:titleInfo/@type|mods:titleInfo/@xml:lang|mods:title|mods:subTitle">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:genre[@type='intern'][1]|mods:genre/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:classification[contains(@valueURI,'/classifications/') and contains(@authorityURI,'/classifications/')]|mods:classification/@valueURI|mods:classification/@authorityURI">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:classification/text()" /> 

<xsl:template match="mods:name[@type='personal']|mods:name[@type='conference'][1]|mods:name/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:name[@type='personal']/mods:namePart[@type='family'][1]|mods:name[@type='personal']/mods:namePart[@type='given'][1]|mods:name[@type='conference']/mods:namePart[not(@type)][1]|mods:namePart/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:nameIdentifier[contains('|lsf|orcid|researcherid|gnd|scopus|',concat('|',@type,'|'))]|mods:nameIdentifier/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:role[1]|mods:roleTerm[@type='code'][@authority='marcrelator'][1]|mods:roleTerm/@type|mods:roleTerm/@authority">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:affiliation">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:originInfo[1]|mods:publisher[1]|mods:edition[1]|mods:place[1]|mods:placeTerm[@type='text'][1]|mods:placeTerm/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:dateIssued[@encoding='w3cdtf'][1]|mods:dateIssued/@encoding">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:part[1]|mods:detail[@type='volume'][1]|mods:detail[@type='issue'][1]|mods:detail/@type|mods:number[1]">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:part/mods:extent[@unit='pages'][1]|mods:part/mods:extent/@unit">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:physicalDescription[1]|mods:physicalDescription/mods:extent[1]|mods:start[1]|mods:end[1]|mods:list[1]|mods:total[1]">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:identifier[contains('|isbn|issn|doi|urn|pubmed|ieee|hdl|zdb|isi|evaluna|ppn|hbz|scopus|duepublico|',concat('|',@type,'|'))]|mods:identifier/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:relatedItem[@type='host'][1]|mods:relatedItem[@type='series']">
  <xsl:copy>
    <xsl:copy-of select="@type" />
    <xsl:apply-templates select="mods:genre|mods:titleInfo|mods:name|mods:originInfo|mods:dateIssued|mods:identifier|mods:relatedItem|mods:location|mods:part" />
  </xsl:copy>
</xsl:template>

<xsl:template match="mods:note[1]" />

<xsl:template match="mods:extension[tag|dedup][1]|mods:extension/tag|mods:extension/dedup">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:location|mods:location/mods:url|mods:location/mods:shelfLocator">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:subject[mods:topic]|mods:subject/mods:topic[1]">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:language|mods:languageTerm[@type='code'][@authority='rfc4646']|mods:languageTerm/@type|mods:languageTerm/@authority">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:abstract|mods:abstract/@xml:lang|mods:abstract/@xlink:href">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

</xsl:stylesheet>