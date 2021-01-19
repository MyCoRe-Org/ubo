<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:template match="/interface">
    <select>
      <xsl:apply-templates select="response[@type='institutions']/institution" />
    </select>
  </xsl:template>

  <xsl:template match="institution">
    <option value="{@id}">
      <xsl:value-of select="fullname" />
    </option>
  </xsl:template>


</xsl:stylesheet>
