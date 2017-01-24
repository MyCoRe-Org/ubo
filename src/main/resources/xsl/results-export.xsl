<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  exclude-result-prefixes="xsl mcr">

  <xsl:include href="copynodes.xsl" /> <!-- required to "pass through" exported publications from basket, not results -->

  <xsl:template match="mcr:results">
    <export>
      <xsl:for-each select="mcr:hit">
        <xsl:copy-of select="document(concat('mcrobject:',@id))/mycoreobject" />
      </xsl:for-each>
    </export>
  </xsl:template>

</xsl:stylesheet>