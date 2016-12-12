<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:output method="xml" />

  <xsl:template match="fachreferate">
    <options>
      <xsl:apply-templates select="item" />
    </options>
  </xsl:template>
  
  <xsl:template match="item">
    <option>
      <xsl:copy-of select="@value" />
      <xsl:value-of select="@label" />
    </option>
  </xsl:template>

</xsl:stylesheet>
