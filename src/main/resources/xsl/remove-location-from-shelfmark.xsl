<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3">

  <!-- Entfernt den Standort aus Zeitschriftensignaturen -->
  <!-- E30/64 Z 123 => 64 Z 123 -->

  <xsl:include href="copynodes.xsl" />

  <xsl:template match="mods:shelfLocator">
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','AAAAAAAAAAAAAAAAAAAAAAAAAA'),'AAA')">
          <xsl:value-of select="text()" /> <!-- book shelfmark -->
        </xsl:when>
        <xsl:when test="contains(text(),'/')"> <!--  remove location part -->
          <xsl:value-of select="normalize-space(substring-after(text(),'/'))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text()" /> <!-- was already fixed -->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
