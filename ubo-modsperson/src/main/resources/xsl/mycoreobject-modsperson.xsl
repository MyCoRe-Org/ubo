<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl mods"
>

<xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]" mode="pageTitle">
  <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
    <xsl:for-each select="mods:name[@type='personal'][1]">
      <xsl:apply-templates select="mods:namePart[@type='family']"/>
      <xsl:apply-templates select="mods:namePart[@type='given']"/>
    </xsl:for-each>
  </xsl:for-each>
</xsl:template>

<xsl:template match="mycoreobject/@ID[contains(.,'_modsperson_')]" mode="breadcrumb">
  <xsl:text>Person </xsl:text>
  <xsl:value-of select="number(substring-after(.,'_modsperson_'))" />
</xsl:template>

<xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]">
  <script type="text/javascript" src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js"/>

  <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
    <div class="section">
      <div class="ubo_details card">
        <div class="card-body">
          <xsl:for-each select="mods:name[@type='personal']" >
            <xsl:apply-templates select="." mode="modsperson" />
            <xsl:apply-templates select="mods:nameIdentifier" mode="modsperson" />
          </xsl:for-each>
        </div>
      </div>
    </div>
  </xsl:for-each>
</xsl:template>

<xsl:template match="mods:name" mode="modsperson">
  <div class="row">
    <div class="col-3">
      <xsl:text>Name:</xsl:text>
    </div>
    <div class="col-9">
      <xsl:apply-templates select="." />
    </div>
  </div>
</xsl:template>

<xsl:template match="mods:nameIdentifier" mode="modsperson">
  <xsl:variable name="type" select="@type" />
  <xsl:variable name="classNode" select="$nameIdentifierClassification//category[@ID=$type]" />

  <div class="row">
    <div class="col-3">
      <xsl:value-of select="$classNode/label[lang($CurrentLang)]/@text"/>
      <xsl:text>:</xsl:text>
    </div>
    <div class="col-9">
      <xsl:apply-templates select="." mode="value">
        <xsl:with-param name="classNode" select="$classNode" />
      </xsl:apply-templates>
    </div>
  </div>
</xsl:template>

</xsl:stylesheet>
