<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
  Converts bibentry schema to MODS schema for export.
  See http://www.loc.gov/standards/mods/ 
-->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  exclude-result-prefixes="xsl xalan"  
>

<xsl:include href="bibentry-mods.xsl" />

<xsl:template match="/bibentries">
  <mods:modsCollection xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
    <xsl:apply-templates select="bibentry" />
  </mods:modsCollection>
</xsl:template>

<xsl:template match="bibentry" priority="1">
  <mods:mods ID="ubo:{@id}">
    <xsl:apply-templates select="." mode="mods" />
  </mods:mods>
</xsl:template>

</xsl:stylesheet>
