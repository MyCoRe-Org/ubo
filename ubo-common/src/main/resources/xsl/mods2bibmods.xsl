<?xml version="1.0" encoding="UTF-8"?>

<!-- Transforms the MODS so that it better fits into BibUtils and will result in the right BibTeX publication types -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" >

  <xsl:include href="copynodes.xsl" />

  <!--  may be this helps: https://github.com/jayvdb/bibutils-archive/blob/master/lib/bltypes.c -->
  <xsl:template match="mods:genre[@type='intern']">
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="substring-after(@valueURI, '#') = 'proceedings'">
          <xsl:text>conference publication</xsl:text>
        </xsl:when>
        <xsl:when test="substring-after(@valueURI, '#') = 'festschrift'">
          <xsl:text>collection</xsl:text>
        </xsl:when>
        <xsl:when test="substring-after(@valueURI, '#') = 'journal'">
          <xsl:text>academic journal</xsl:text>
        </xsl:when>
        <xsl:when test="substring-after(@valueURI, '#') = 'dissertation'">
          <xsl:text>thesis</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring-after(@valueURI, '#')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern'][contains('poster speech', substring-after(@valueURI, '#'))][not(../mods:relatedItem[@type='host'])]" priority="1">
    <mods:genre>conference</mods:genre>
    <mods:relatedItem type="host">
      <mods:genre>conference publication</mods:genre>
      <mods:titleInfo>
        <mods:title>
          <xsl:copy-of select="../mods:name[@type='conference']/mods:namePart/text()" />
        </mods:title>
      </mods:titleInfo>
    </mods:relatedItem>
  </xsl:template>

  <xsl:template match="mods:genre" />
  
  <xsl:template match="mods:name[@type='conference']" />

</xsl:stylesheet>
