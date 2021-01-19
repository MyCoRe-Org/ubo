<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:strutils="xalan://org.apache.commons.lang.StringEscapeUtils"
  exclude-result-prefixes="strutils"
>

<!-- Serialize <mycoreobject> so that MODS in <modsContainer> becomes plain text, used in edit-mods.xed -->

<xsl:include href="copynodes.xsl" />

<xsl:template match="mods:mods">
  <xsl:apply-templates select="." mode="serialize" />
</xsl:template>

<xsl:template match="*[not(*)][string-length(text())=0]" mode="serialize">
  <xsl:text>&lt;</xsl:text><xsl:value-of select="name(.)" />
  <xsl:apply-templates select="@*" mode="serialize" />
  <xsl:text> /&gt;</xsl:text>
</xsl:template>

<xsl:template match="*" mode="serialize">
  <xsl:text>&lt;</xsl:text><xsl:value-of select="name(.)" />
  <xsl:apply-templates select="@*" mode="serialize" />
  <xsl:text>&gt;</xsl:text>
  <xsl:apply-templates select="node()" mode="serialize" />
  <xsl:text>&lt;/</xsl:text><xsl:value-of select="name(.)" /><xsl:text>&gt;</xsl:text>
</xsl:template>

<xsl:template match="text()" mode="serialize">
  <xsl:value-of select="strutils:escapeXml(.)" disable-output-escaping="no" />
</xsl:template>

<xsl:template match="@*" mode="serialize">
  <xsl:text> </xsl:text>
  <xsl:value-of select="name()"/>
  <xsl:text>="</xsl:text>
  <xsl:value-of select="." />
  <xsl:text>"</xsl:text>
</xsl:template>

<xsl:template match="mods:extension[not(tag)]" mode="serialize" />
<xsl:template match="mods:extension/dedup" mode="serialize" />

</xsl:stylesheet>
