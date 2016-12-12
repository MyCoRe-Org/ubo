<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:output method="xml" />

  <!-- Select only those items that have an x-hosts attribute and flatten the list -->
  <xsl:template match="items">
    <items>
      <xsl:for-each select="//item[label[@xml:lang='x-hosts']]"> 
        <item>
          <xsl:copy-of select="@*" />
          <xsl:copy-of select="label" />
        </item>
      </xsl:for-each>
    </items>
  </xsl:template>

</xsl:stylesheet>
