<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3"
>
  
<xsl:include href="mods-display.xsl" />
<xsl:include href="mods-dc.xsl" />
<xsl:include href="mycoreobject2record.xsl" />

<xsl:template match="mycoreobject" mode="metadata">
  <oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="dc" />
  </oai_dc:dc> 
</xsl:template>

</xsl:stylesheet>
