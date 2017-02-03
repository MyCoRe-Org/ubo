<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:param name="CurrentLang" />

  <xsl:template match="response">
    <ubostatistics total="{result[@name='response']/@numFound}">
      <xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']">
        <xsl:apply-templates select="lst[@name='year']" />
        <xsl:apply-templates select="lst[@name='subject']" />
        <xsl:apply-templates select="lst[@name='genre']" />
        <xsl:apply-templates select="lst[@name='facet_person']" />  
      </xsl:for-each>
    </ubostatistics>
  </xsl:template>
  
  <xsl:template match="lst[@name='facet_fields']/lst[@name='year']">
    <table name="Erscheinungsjahre" charttype="PublicationsByYear" id="{generate-id(.)}">
      <xsl:for-each select="int">
        <xsl:sort select="@name" data-type="number" order="ascending" />
        <row num="{text()}" key="{@name}" label="{@name}" />
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:variable name="subjects" select="document('resource:fachreferate.xml')/fachreferate" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='subject']">
    <table name="Fachgebiete (Mehrfachzuordnungen möglich, nur Top 20)" charttype="ColumnRotatedLabels" id="{generate-id(.)}">
      <xsl:for-each select="int">
        <xsl:sort select="text()" data-type="number" order="descending" />
        <xsl:if test="position() &lt;= 20">
          <row num="{text()}" key="{@name}" label="{$subjects/item[@value=current()/@name]/@label}" />
        </xsl:if>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='genre']">
    <table name="Publikationstypen" charttype="Piechart" id="{generate-id(.)}">
      <xsl:for-each select="int">
        <xsl:sort select="text()" data-type="number" order="descending" />
        <row num="{text()}" key="{@name}" label="{$genres//category[@ID=current()/@name]/label[lang($CurrentLang)]/@text}" />
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='facet_person']">
    <table name="Personennamen (nur Top 20)" charttype="ColumnRotatedLabels" id="{generate-id(.)}">
      <xsl:for-each select="int">
        <xsl:sort select="text()" data-type="number" order="descending" />
        <xsl:if test="position() &lt;= 20">
          <row num="{text()}" key="{@name}" label="{@name}" />
        </xsl:if>
      </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>