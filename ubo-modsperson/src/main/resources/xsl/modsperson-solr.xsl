<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:str="http://exslt.org/strings"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="mods xlink str xalan">

  <xsl:import href="xslImport:solr-document:modsperson-solr.xsl" />
  
  <xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]">
    <xsl:apply-templates select="." mode="baseFields" />
    
    <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
      <xsl:for-each select="mods:name[@type='personal']">
        <xsl:apply-templates select="." mode="solrField" />
        <xsl:apply-templates select="mods:nameIdentifier" mode="solrField" />
        <xsl:apply-templates select="mods:alternativeName" mode="solrField" />
      </xsl:for-each>
    </xsl:for-each>
    
  </xsl:template>

  <xsl:template match="mods:name[@type='personal']" mode="solrField">
    <field name="name">
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:if test="mods:namePart[@type='given']">
        <xsl:text>,</xsl:text> 
        <xsl:for-each select="mods:namePart[@type='given']">
          <xsl:value-of select="concat(' ',text())" />
        </xsl:for-each>
      </xsl:if>
    </field>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier" mode="solrField">
    <field name="nid_{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="mods:alternativeName" mode="solrField">
    <field name="alternative_name">
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:if test="mods:namePart[@type='given']">
        <xsl:text>,</xsl:text>
        <xsl:for-each select="mods:namePart[@type='given']">
          <xsl:value-of select="concat(' ',text())" />
        </xsl:for-each>
      </xsl:if>
    </field>
  </xsl:template>

</xsl:stylesheet>
