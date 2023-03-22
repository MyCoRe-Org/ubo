<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/list">
    <mods:modsCollection>
      <xsl:apply-templates select="ppn"/>
    </mods:modsCollection>
  </xsl:template>

  <xsl:template match="ppn">
    <mods:mods>
      <mods:identifier type="ppn">
        <xsl:value-of select="."/>
      </mods:identifier>
    </mods:mods>
  </xsl:template>

</xsl:stylesheet>
