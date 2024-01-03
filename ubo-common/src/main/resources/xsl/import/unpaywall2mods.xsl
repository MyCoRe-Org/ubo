<?xml version="1.0" encoding="UTF-8"?>

<!-- See https://unpaywall.org/data-format -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xalan">

  <xsl:output method="xml" />

  <xsl:template match="/entry[error]">
    <error>
      <xsl:value-of select="message" />
    </error>
  </xsl:template>

  <xsl:template match="/entry">
    <mods:mods>
      <xsl:apply-templates select="title" />
      <xsl:apply-templates select="genre" />
      <xsl:apply-templates select="z_authors/entry[family][given]" />
      <xsl:apply-templates select="doi" />
      <xsl:apply-templates select="best_oa_location[host_type='publisher']" />
      <xsl:call-template name="oa_publication" />
      <mods:relatedItem type="host">
        <xsl:apply-templates select="journal_name" />
        <xsl:apply-templates select="genre" mode="host" />
        <xsl:apply-templates select="journal_issns" />
        <xsl:call-template name="oa_journal" />
        <mods:originInfo>
          <xsl:apply-templates select="year" />
          <xsl:apply-templates select="publisher" />
        </mods:originInfo>
      </mods:relatedItem>
    </mods:mods>
  </xsl:template>

  <xsl:template match="genre[.='book-chapter']">
    <mods:genre>chapter</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='book-section']">
    <mods:genre>chapter</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='journal-article']">
    <mods:genre>article</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='dissertation']">
    <mods:genre>dissertation</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='book']">
    <mods:genre>book</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='journal-article']" mode="host">
    <mods:genre>journal</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='proceedings-article']" mode="host">
    <mods:genre>proceedings</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='book-chapter']" mode="host">
    <mods:genre>collection</mods:genre>
  </xsl:template>

  <xsl:template match="genre[.='book-section']" mode="host">
    <mods:genre>collection</mods:genre>
  </xsl:template>

  <xsl:template match="best_oa_location">
    <xsl:apply-templates select="url_for_pdf" />
  </xsl:template>

  <xsl:variable name="authorityOA">https://bibliographie.ub.uni-due.de/classifications/oa</xsl:variable>

  <xsl:template name="oa_publication">
    <xsl:choose>
      <xsl:when test="(is_oa='true')">
        <mods:classification authorityURI="{$authorityOA}" valueURI="{$authorityOA}#{oa_status}" />
      </xsl:when>
      <xsl:otherwise>
        <mods:classification authorityURI="{$authorityOA}" valueURI="{$authorityOA}#{oa_status}" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="oa_journal">
    <xsl:choose>
      <xsl:when test="(is_oa='true') and ((journal_is_in_doaj='true') or (journal_is_oa='true'))">
        <mods:classification authorityURI="{$authorityOA}" valueURI="{$authorityOA}#gold" />
      </xsl:when>
      <xsl:otherwise>
        <mods:classification authorityURI="{$authorityOA}" valueURI="{$authorityOA}#{oa_status}" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="url_for_pdf">
    <mods:location>
      <mods:url access="raw object">
        <xsl:value-of select="text()" />
      </mods:url>
    </mods:location>
  </xsl:template>

  <xsl:template match="doi">
    <mods:identifier type="doi">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="title|journal_name">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="text()" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="z_authors/entry[family][given]">
    <mods:name type="personal">
      <xsl:apply-templates select="family" />
      <xsl:apply-templates select="given" />
      <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:template>

  <xsl:template match="family|given">
    <mods:namePart type="{name()}">
      <xsl:value-of select="text()" />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="year">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="text()" />
    </mods:dateIssued>
  </xsl:template>

  <xsl:template match="publisher">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="journal_issns">
    <xsl:for-each select="xalan:tokenize(text(),',')">
      <mods:identifier type="issn">
        <xsl:value-of select="." />
      </mods:identifier>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*|text()" />

</xsl:stylesheet>
