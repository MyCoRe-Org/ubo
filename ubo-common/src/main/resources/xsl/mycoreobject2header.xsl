<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns="http://www.openarchives.org/OAI/2.0/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  
<xsl:output method="xml" encoding="UTF-8" indent="yes" />
  
<xsl:param name="MCR.OAIDataProvider.OAI.RepositoryIdentifier" />

<xsl:template match="/">
  <header>
    <xsl:apply-templates select="mycoreobject" mode="header" />
  </header>
</xsl:template>

<xsl:template match="mycoreobject" mode="header">

  <identifier>
    <xsl:text>oai:</xsl:text>
    <xsl:value-of select="$MCR.OAIDataProvider.OAI.RepositoryIdentifier" />
    <xsl:text>:</xsl:text>
    <xsl:value-of select="@ID" />
  </identifier>

  <datestamp>
    <xsl:value-of select="substring-before(service/servdates/servdate[@type='modifydate'],'T')" />
  </datestamp>

</xsl:template>

</xsl:stylesheet>
