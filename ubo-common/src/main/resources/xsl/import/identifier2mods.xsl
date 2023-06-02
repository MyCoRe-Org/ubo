<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/list[ppn]">
    <mods:modsCollection>
      <xsl:apply-templates select="ppn"/>
    </mods:modsCollection>
  </xsl:template>

  <xsl:template match="/list[doi]">
    <mods:modsCollection>
      <xsl:apply-templates select="doi"/>
    </mods:modsCollection>
  </xsl:template>

  <xsl:template match="ppn">
    <mods:mods>
      <mods:identifier type="ppn">
        <xsl:value-of select="."/>
      </mods:identifier>
    </mods:mods>
  </xsl:template>

  <xsl:template match="doi">
    <mods:mods>
      <mods:identifier type="doi">
        <xsl:value-of select="."/>
      </mods:identifier>
    </mods:mods>
  </xsl:template>

</xsl:stylesheet>
