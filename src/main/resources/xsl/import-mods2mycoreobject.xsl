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

  <xsl:template match="mods:name[mods:etAl]"> <!-- Ignore "et al." names, currently not supported -->
    <xsl:comment>"et al." wird zur Zeit vom Datenmodell nicht unterstützt und ignoriert.</xsl:comment>
  </xsl:template>

</xsl:stylesheet>