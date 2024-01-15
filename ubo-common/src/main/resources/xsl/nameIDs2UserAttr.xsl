<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />
  
  <xsl:template match="category/@ID|item/@value" priority="1">
    <xsl:attribute name="{name()}">
      <xsl:value-of select="concat('id_',.)" />
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>