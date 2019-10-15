<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
>

<!-- Deserialize <mycoreobject> so that plain MODS text in <modsContainer> becomes <mods:mods> again, used in edit-mods.xed -->

<xsl:include href="copynodes.xsl" />

<xsl:template match="modsContainer">
  <xsl:copy>
    <xsl:copy-of select="@*" />
    <!-- This is ugly, but the only working method I found to get rid of a trailing CR/LF -->
    <mods:mods>
      <xsl:value-of select="substring-after(substring-before(text(),'&lt;/mods:mods&gt;'),'&gt;')" disable-output-escaping="yes" />
    </mods:mods>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>