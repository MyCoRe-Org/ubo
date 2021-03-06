<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl mods">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="UBO.projectid.default" />

  <xsl:template match="/">
    <mycoreobject ID="{$UBO.projectid.default}_mods_00000000" label="{$UBO.projectid.default}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="datamodel-mods.xsd">
      <structure />
      <metadata>
        <def.modsContainer class="MCRMetaXML">
          <modsContainer>
            <xsl:apply-templates select="mods:mods" />
          </modsContainer>
        </def.modsContainer>
      </metadata>
    </mycoreobject>
  </xsl:template>

</xsl:stylesheet>
