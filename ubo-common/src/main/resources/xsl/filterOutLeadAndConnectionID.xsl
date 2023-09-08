<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="MCR.user2.matching.lead_id" />

  <xsl:template match="item">
    <xsl:choose>
      <xsl:when test="@value='connection'" />
      <xsl:when test="@value=$MCR.user2.matching.lead_id" />
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" />
        </xsl:copy>      
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>