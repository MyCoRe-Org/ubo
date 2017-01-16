<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3"
>
  
<xsl:include href="mycoreobject2record.xsl" />
<xsl:include href="bibentry-mods.xsl" />

<xsl:template match="mycoreobject" mode="metadata">
  <mods:mods ID="{@ID}" version="3.6" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="copy-mods" />
  </mods:mods>
</xsl:template>

</xsl:stylesheet>
