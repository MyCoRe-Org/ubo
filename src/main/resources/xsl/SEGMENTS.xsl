<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
   XSL to transform Primo search result list of dissertations to A-Z list as HTML page.

1. Search in Primo:
   http://primo.ub.uni-due.de/PrimoWebServices/xservice/search/brief?institution=UDE&view=UDE&onCampus=false&query=any,exact,%22duisburg+essen+univ+diss+2012%22&indx=1&bulkSize=1000&dym=true&highlight=true&lang=eng&loc=local,scope:%28UDEALEPH%29
2. Transform results using this XSL stylesheet 
 -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:jaguar="http://www.exlibrisgroup.com/xsd/jaguar/search" 
  xmlns:primo="http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib"
  exclude-result-prefixes="xsl xalan jaguar primo">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" 
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    doctype-system="http://www.w3.org/TR/html401/loose.dtd" 
    indent="yes" xalan:indent-amount="2" />

  <xsl:template match="/">
    <html>
      <head>
        <META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <style type="text/css">
          th { padding: 0 1ex 0 1ex; text-align: left; font-weight: bold; }
          td { padding: 0 1ex 0 1ex; vertical-align: top; }
        </style>
      </head>
      <body>
       <xsl:apply-templates select="jaguar:SEGMENTS/jaguar:JAGROOT/jaguar:RESULT/jaguar:DOCSET" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="jaguar:DOCSET">
    <h3>Primo: <xsl:value-of select="@TOTALHITS" /> Dissertationen</h3>
    <table>
      <tr>
        <th>ID:</th>
        <th>Prom.</th>
        <th>Jahr:</th>
        <th>DoktorandIn:</th>
        <th>Titel:</th>
      </tr>
      <xsl:apply-templates select="jaguar:DOC/primo:PrimoNMBib/primo:record">
        <xsl:sort select="primo:search/primo:creatorcontrib" />
      </xsl:apply-templates>
    </table>
  </xsl:template>

<xsl:variable name="primoLink">http://primo.ub.uni-due.de/UDE:UDEALEPH</xsl:variable>

<xsl:template match="primo:record">
  <tr>
    <td>
      <a href="{$primoLink}{primo:control/primo:recordid}">
        <xsl:value-of select="primo:control/primo:recordid" />
      </a>
    </td>
    <td>
      <xsl:apply-templates select="primo:display/primo:description" />
    </td>
    <td>
      <xsl:value-of select="translate(primo:display/primo:creationdate,'[] ','')" />
    </td>
    <td>
      <xsl:value-of select="primo:search/primo:creatorcontrib" />
    </td>
    <td>
      <xsl:value-of select="primo:display/primo:title" />
    </td>
  </tr>
</xsl:template>

<xsl:template match="primo:description">
  <xsl:if test="contains(.,'Univ') and contains(.,'Diss') and ( contains(.,'Essen') or contains(.,'Duisburg') ) and contains( translate(.,'0123456789','JJJJJJJJJJ'),'JJJJ')">
    <xsl:variable name="jjjj" select="translate(.,'0123456789','JJJJJJJJJJ')" />
    <xsl:variable name="before" select="substring-before($jjjj,'JJJJ')" />
    <xsl:value-of select="substring(substring-after(.,$before),1,4)" />
  </xsl:if>
</xsl:template>

</xsl:stylesheet>

