<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:output method="html" />

  <xsl:template match="/response">
    <html>
      <head>
        <style>
          table { border-collapse:collapse; }
          th, td { border:1px solid black; vertical-align:top; padding:1ex; }
        </style>
      </head>
      <body>
        <xsl:apply-templates select="lst/lst[@name='facet_pivot']" />
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="arr">
    <table>
      <xsl:apply-templates select="lst" />
    </table>
  </xsl:template>

  <xsl:template match="arr/lst">
    <tr>
      <xsl:for-each select="*">
        <td>
          <xsl:apply-templates select="." />
        </td>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <xsl:template match="str|int">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="str[@name='value'][../str[@name='field']='name_id_lsf']">
    <a href="https://www.uni-due.de/zim/services/suchdienste/mitarbeiter.php?id={.}">
      <xsl:value-of select="." />
    </a>
  </xsl:template>

  <xsl:template match="str[@name='value'][../str[@name='field']='name_id_orcid']">
    <xsl:variable name="orcid" select="concat(substring(.,1,4),'-',substring(.,5,4),'-',substring(.,9,4),'-',substring(.,13,4))" />
    <a href="https://orcid.org/{$orcid}">
      <xsl:value-of select="$orcid" />
    </a>
  </xsl:template>

</xsl:stylesheet>
