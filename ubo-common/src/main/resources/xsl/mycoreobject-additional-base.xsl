<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="mods xsl">

  <xsl:import href="xslImport:additional:mycoreobject-additional-base.xsl"/>

  <!-- Overwrite to provide additional metadata within the standard ubo card -->
  <xsl:template match="mods:mods" mode="additional-metadata"/>

  <!-- Overwrite to provide an additional box with metadata beneath the standard ubo card -->
  <xsl:template match="mycoreobject" mode="additional-metadata-card"/>

  <!-- ignored -->
  <xsl:template match="*" mode="additional-metadata"/>
  <xsl:template match="*" mode="additional-metadata-card"/>

</xsl:stylesheet>
