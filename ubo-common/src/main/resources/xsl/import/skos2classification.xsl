<?xml version="1.0" encoding="UTF-8"?>

<!-- 
  Can be used to import SKOS vocabulary as MyCoRe classification.
  Example: 
  
  get uri xslStyle:import/simplify-json-xml,import/skos2classification:xslTransform:json2xml: ...
    ... https://skohub.io/KDSF-FFK/kdsf-ffk/heads/main/w3id.org/kdsf-ffk/index.json 
    to file kdsf-ffk.xml
  (or even: update classification from uri ...)
 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/entry">
    <mycoreclass xsi:noNamespaceSchemaLocation="MCRClassification.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <xsl:attribute name="ID">
        <xsl:value-of select="substring-before(substring-after(id,'://w3id.org/'),'/')" />
      </xsl:attribute>

      <xsl:apply-templates select="title/de" />
      <xsl:apply-templates select="title/en" />

      <xsl:apply-templates select="hasTopConcept" />
    </mycoreclass>
  </xsl:template>

  <xsl:template match="title/*|prefLabel/*">
    <label xml:lang="{name()}" text="{text()}">
      <xsl:apply-templates select="../../description/*[name()=name(current())]" />
      <xsl:apply-templates select="../../example/*[name()=name(current())]" />
    </label>
  </xsl:template>

  <xsl:template match="description/*|example/*">
    <xsl:attribute name="description">
      <xsl:value-of select="text()" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="hasTopConcept">
    <categories>
      <xsl:apply-templates select="entry" />
    </categories>
  </xsl:template>
  
  <xsl:template match="narrower">
    <xsl:apply-templates select="entry" />
  </xsl:template>

  <xsl:template match="entry">
    <category>
      <xsl:apply-templates select="id" />
      <xsl:apply-templates select="prefLabel/de" />
      <xsl:apply-templates select="prefLabel/en" />
      <xsl:apply-templates select="narrower" />
    </category>
  </xsl:template>

  <xsl:variable name="idPrefix" select="/entry/id" />

  <xsl:template match="id">
    <xsl:attribute name="ID">
      <xsl:value-of select="substring-after(text(),$idPrefix)" />
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
