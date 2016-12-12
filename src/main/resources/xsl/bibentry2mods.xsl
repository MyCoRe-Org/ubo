<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
>
  
<xsl:include href="bibentry2record.xsl" />
<xsl:include href="bibentry-mods.xsl" />

<xsl:template match="bibentry" mode="metadata">
  <mods:mods>
    <xsl:apply-templates select="." mode="mods" />
  </mods:mods>
</xsl:template>

</xsl:stylesheet>
