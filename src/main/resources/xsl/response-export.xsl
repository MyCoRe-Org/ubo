<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  exclude-result-prefixes="xsl mcr">

  <xsl:include href="copynodes.xsl" /> <!-- required to "pass through" exported publications from basket, not results -->

  <xsl:template match="response">
    <export>
      <xsl:for-each select="result[@name='response']/doc">
        <xsl:copy-of select="document(concat('mcrobject:',str[@name='id']))/mycoreobject" />
      </xsl:for-each>
    </export>
  </xsl:template>

</xsl:stylesheet>