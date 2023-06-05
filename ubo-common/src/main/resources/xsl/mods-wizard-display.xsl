<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl mods" 
>

<xsl:include href="mods-display.xsl" />
<xsl:include href="coreFunctions.xsl" />

<xsl:template match="mods:mods">
  <div>
    <xsl:apply-templates select="." mode="cite"> 
      <xsl:with-param name="mode">divs</xsl:with-param> 
    </xsl:apply-templates>
  </div>
</xsl:template>

</xsl:stylesheet>
