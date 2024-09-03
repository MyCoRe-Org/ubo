<?xml version="1.0" encoding="UTF-8"?>

<!-- Transforms the MODS so that it better fits into BibUtils and will result in the right BibTeX publication types -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" >

  <xsl:include href="copynodes.xsl" />

  <xsl:template match="mods:genre[not(@type='intern')]" priority="1" />
  <xsl:template match="mods:genre/@type" />

  <!--  may be this helps: https://github.com/jayvdb/bibutils-archive/blob/master/lib/bltypes.c -->
  
  <xsl:template match="mods:genre[@type='intern'][substring-after(@valueURI, '#') = 'proceedings']">
    <mods:genre>conference publication</mods:genre>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern'][substring-after(@valueURI, '#') = 'festschrift']">
    <mods:genre>collection</mods:genre>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern'][substring-after(@valueURI, '#') = 'journal']">
    <mods:genre>academic journal</mods:genre>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern'][substring-after(@valueURI, '#') = 'dissertation']">
    <mods:genre>thesis</mods:genre>
  </xsl:template>

  <xsl:template match="mods:name[@type='conference']" />

  <xsl:template match="mods:genre[@type='intern'][contains('poster speech', substring-after(@valueURI, '#'))][not(../mods:relatedItem[@type='host'])]">
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

</xsl:stylesheet>
