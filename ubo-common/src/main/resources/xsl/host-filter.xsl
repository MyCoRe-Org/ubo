<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:output method="xml" />

  <!-- Select only those items that may be host and flatten the list -->
  <xsl:template match="items">
    <items>
      <xsl:for-each select="//item">
        <xsl:variable name="id" select="@value" />
        <xsl:if test="//item[contains(label[lang('x-hosts')],$id)]"> 
          <item>
            <xsl:copy-of select="@*" />
            <xsl:copy-of select="label" />
          </item>
        </xsl:if>
      </xsl:for-each>
    </items>
  </xsl:template>

</xsl:stylesheet>
