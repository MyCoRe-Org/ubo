<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mods xlink xsl">
  <xsl:include href="copynodes.xsl"/>

  <xsl:param name="MCR.baseurl"/>

  <xsl:template match="mods:accessCondition[@type = 'use and reproduction'][@classID = 'licenses']">
    <mods:accessCondition type="use and reproduction"
                          xlink:href="{$MCR.baseurl}classifications/licenses#{.}"/>
  </xsl:template>
</xsl:stylesheet>
