<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3">

  <!-- Entferne alle Links auf KWI (www.kulturwissenschaften.de) -->

  <xsl:include href="copynodes.xsl" />

  <!-- Remove mods:location completely if KWI link is the only element below -->
  <xsl:template match="mods:location">
    <xsl:choose>
      <xsl:when test="(count(*) = 1) and mods:location/mods:url[contains(text(),'www.kulturwissenschaften.de')]" />
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|*" />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:location/mods:url[contains(text(),'www.kulturwissenschaften.de')]" />

</xsl:stylesheet>
