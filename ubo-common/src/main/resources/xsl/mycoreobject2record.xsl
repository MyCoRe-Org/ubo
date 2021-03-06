<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  
<xsl:include href="mycoreobject2header.xsl" />

<xsl:template match="/">
  <record>
    <header>
      <xsl:apply-templates select="mycoreobject" mode="header" />
    </header>
    <metadata>
      <xsl:apply-templates select="mycoreobject" mode="metadata" />
    </metadata>
  </record>
</xsl:template>

</xsl:stylesheet>
