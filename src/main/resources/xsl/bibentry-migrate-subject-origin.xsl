<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  exclude-result-prefixes="xsl xalan"  
>

<xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

<xsl:include href="copynodes.xsl" />

<xsl:param name="WebApplicationBaseURL" />

<xsl:template match="bibentry">
  <xsl:copy>
    <xsl:copy-of select="@*" />
    <xsl:apply-templates select="mods:mods" />
  </xsl:copy>
</xsl:template>

<xsl:template match="mods:mods">
  <xsl:copy>
    <xsl:copy-of select="@*" />
    <xsl:copy-of select="mods:*" />
    <xsl:apply-templates select="../subject" />
    <xsl:apply-templates select="../origin" />
  </xsl:copy>
</xsl:template>

<xsl:template match="subject">
  <mods:classification authorityURI="{$WebApplicationBaseURL}classifications/fachreferate" valueURI="{$WebApplicationBaseURL}classifications/fachreferate#{text()}" />
</xsl:template>

<xsl:template match="origin">
  <mods:classification authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN" valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#{text()}" />
</xsl:template>

</xsl:stylesheet>
