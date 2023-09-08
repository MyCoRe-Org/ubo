<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="UBO.Editable.Attributes" />

  <xsl:template match="item[string-length($UBO.Editable.Attributes) &gt; 0]" priority="1">
    <xsl:if test="contains(concat(',',$UBO.Editable.Attributes,','),concat(',',@value,','))">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>