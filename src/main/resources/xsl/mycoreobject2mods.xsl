<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3"
>
  
<xsl:include href="mycoreobject2record.xsl" />
<xsl:include href="mycoreobject-mods.xsl" />

<xsl:template match="mycoreobject" mode="metadata">
  <xsl:apply-templates select="." />
</xsl:template>

</xsl:stylesheet>
