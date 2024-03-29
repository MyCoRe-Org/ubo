<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xsl xalan i18n">

<xsl:param name="ServletsBaseURL" />

<xsl:template match="/">
  <html id="dozbib.duplicates">
    <head>
      <title>Dublettenbericht</title>
    </head>
    <body>
      <xsl:apply-templates select="duplicates" />
    </body>
  </html>
</xsl:template>

<xsl:template match="duplicates">
  <article class="card">
    <div class="card-body">
      <h2>
	<xsl:text>Es wurden </xsl:text>
	<xsl:value-of select="count(group)" /> 
	<xsl:text> Gruppen mit </xsl:text>
	<xsl:value-of select="count(group/id)" />
	<xsl:text> Publikationen gefunden:</xsl:text>
      </h2>
      <table class="table">
	<tr>
          <th>Anzeigen</th>
          <th>#</th>
          <th>IDs</th>
          <th>Kriterium</th>
	</tr>
	<xsl:apply-templates select="group">
          <xsl:sort select="count(id)" data-type="number" />
          <xsl:sort select="count(key)" data-type="number" order="descending"/>
          <xsl:sort select="id" data-type="text" order="descending" />
	</xsl:apply-templates>
      </table>
    </div>
  </article>
</xsl:template>

<xsl:template match="group">
  <tr>
    <td>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$ServletsBaseURL" />
          <xsl:text>DozBibEntryServlet?id=</xsl:text>
          <xsl:value-of select="id[1]" />
          <xsl:text>&amp;XSL.Style=structure</xsl:text>
        </xsl:attribute>
        <xsl:text>Anzeigen</xsl:text>
      </a>
    </td>
    <td>
      <xsl:value-of select="count(id)" />
    </td>
    <td>
      <xsl:apply-templates select="id">
        <xsl:sort order="ascending" data-type="text" />
      </xsl:apply-templates>
    </td>
    <td>
      <xsl:apply-templates select="dedup">
        <xsl:sort select="@type" />
      </xsl:apply-templates>
    </td>
  </tr>
</xsl:template>

<xsl:template match="id">
  <a class="ubo-duplicateid-link" href="{$ServletsBaseURL}DozBibEntryServlet?id={text()}">
    <xsl:value-of select="number(substring-after(text(),'mods_'))" />
  </a>
  <xsl:if test="position() != last()">
    <xsl:text> </xsl:text>
  </xsl:if> 
</xsl:template>

<xsl:template match="dedup">
  <strong>
    <xsl:value-of select="@type" />
  </strong>
  <xsl:text>: </xsl:text>
  <xsl:value-of select="text()" />
  <xsl:if test="position() != last()">
    <br />
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
