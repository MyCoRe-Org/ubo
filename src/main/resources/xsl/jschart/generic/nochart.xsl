<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="table[@charttype='Nochart']" mode="chart">
  <div class="outer-stats-container">
    <div class="inner-stats-container">
      <h3><xsl:value-of select="@name" /></h3>
      <table class="statistics">
        <xsl:apply-templates select="row">
          <xsl:sort select="@num" data-type="number" order="descending" />
        </xsl:apply-templates>
      </table>
    </div>
  </div>
</xsl:template>

<xsl:template match="row">
  <tr>
    <td class="number">
      <xsl:value-of select="@num" />
    </td>
    <td class="label">
      <xsl:value-of select="@label" />
    </td>
  </tr>
</xsl:template>
</xsl:stylesheet>