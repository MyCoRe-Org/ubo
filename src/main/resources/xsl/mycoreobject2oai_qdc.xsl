<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:oai_qdc="http://epubs.cclrc.ac.uk/xmlns/qdc/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:mods="http://www.loc.gov/mods/v3"
>
  
<xsl:include href="mods-display.xsl" />
<xsl:include href="mods-qdc.xsl" />
<xsl:include href="mycoreobject2record.xsl" />

<xsl:template match="mycoreobject" mode="metadata">
  <oai_qdc:qualifieddc>
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="qdc" />
  </oai_qdc:qualifieddc> 
</xsl:template>

</xsl:stylesheet>
