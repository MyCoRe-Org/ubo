<?xml version="1.0"?>

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl">



  <xsl:template match="/exportCollection">
    <add>
      <xsl:for-each select="entry">
        <xsl:copy-of select="mycoreobject" />
      </xsl:for-each>
    </add>
  </xsl:template>


</xsl:stylesheet>
