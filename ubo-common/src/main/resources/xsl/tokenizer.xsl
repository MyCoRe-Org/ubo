<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl xlink">

  <xsl:template name="tokenizer">
    <xsl:param name="string"/>
    <xsl:param name="delimiter" select="' '"/>
    <xsl:choose>
      <xsl:when test="$delimiter and contains($string, $delimiter)">
        <token>
          <xsl:value-of select="substring-before($string, $delimiter)"/>
        </token>
        <xsl:call-template name="tokenizer">
          <xsl:with-param name="string" select="substring-after($string, $delimiter)"/>
          <xsl:with-param name="delimiter" select="$delimiter"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <token>
          <xsl:value-of select="$string"/>
        </token>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
