<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xsl mods">
  
  <xsl:include href="copynodes.xsl" />

  <xsl:template match="mods:name/mods:nameIdentifier[@type='lsf'][text()='59000']">
    <xsl:copy-of select="." />
    <xsl:if test="not(../mods:nameIdentifier[@type='orcid'])">
      <mods:nameIdentifier type="orcid">0000-0003-4817-0829</mods:nameIdentifier>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>

