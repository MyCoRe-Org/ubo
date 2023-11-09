<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:template match="/">
    <xsl:apply-templates select="/entry/Data/Records/records/REC/entry[1]" />
  </xsl:template>

  <xsl:template match="REC/entry">
    <mods:mods>
      <xsl:apply-templates select="static_data/summary/doctypes/doctype" />
      <xsl:apply-templates select="static_data/summary/titles/title/entry[type='item']" />
      <xsl:apply-templates select="static_data/summary/names/name/entry" />
      <xsl:apply-templates select="static_data/summary/pub_info/pubyear" />
      <xsl:apply-templates select="static_data/fullrecord_metadata/normalized_languages/language" />
      <xsl:apply-templates select="dynamic_data/cluster_related/identifiers/identifier/entry[type='doi']" />
      <xsl:apply-templates select="dynamic_data/cluster_related/identifiers/identifier/entry[type='pmid']" />
      <xsl:apply-templates select="UID" />
      <mods:relatedItem type="host">
        <xsl:apply-templates select="static_data/summary/pub_info/pubtype" />
        <xsl:apply-templates select="static_data/summary/titles/title/entry[type='source']" />
        <xsl:apply-templates select="static_data/summary/publishers/publisher" />
        <xsl:apply-templates
          select="dynamic_data/cluster_related/identifiers/identifier/entry[(type='issn') or (type='eissn')]" />
        <xsl:apply-templates select="static_data/summary/pub_info" mode="part" />
      </mods:relatedItem>
    </mods:mods>
  </xsl:template>

  <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

  <xsl:template match="language">
    <!-- Find language with matching label in any language, or with matching ID in any supported code schema -->
    <xsl:variable name="given"
      select="translate(content,$upper,$lower)" />
    <xsl:for-each select="document('classification:metadata:-1:children:rfc4646')/mycoreclass/categories">
      <xsl:for-each select="category[@ID=$given or label[translate(@text,$upper,$lower)=$given]][1]">
        <mods:language>
          <mods:languageTerm authority="rfc4646" type="code">
            <xsl:value-of select="@ID" />
          </mods:languageTerm>
        </mods:language>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="doctype|pubtype">
    <mods:genre>
      <xsl:value-of select="." />
    </mods:genre>
  </xsl:template>

  <xsl:template match="title/entry">
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="content" />
      </mods:title>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="name/entry">
    <mods:name type="personal">
      <xsl:apply-templates select="last_name" />
      <xsl:apply-templates select="first_name" />
      <xsl:apply-templates select="role" />
      <xsl:apply-templates select="daisng_id" mode="affiliation" />
      <xsl:apply-templates select="orcid_id" />
      <xsl:apply-templates select="r_id" />
    </mods:name>
  </xsl:template>

  <xsl:template match="last_name">
    <mods:namePart type="family">
      <xsl:value-of select="." />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="first_name">
    <mods:namePart type="given">
      <xsl:value-of select="." />
    </mods:namePart>
  </xsl:template>

  <xsl:template match="role">
    <mods:role>
      <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
    </mods:role>
  </xsl:template>

  <xsl:template match="daisng_id" mode="affiliation">
    <xsl:variable name="id" select="text()" />
    <xsl:for-each
      select="ancestor::static_data/fullrecord_metadata/addresses/address_name/entry[names/name[daisng_id=$id]]/address_spec">
      <mods:affiliation>
        <xsl:for-each select="organizations/organization/entry[pref='Y']">
          <xsl:value-of select="content" />
        </xsl:for-each>
        <xsl:for-each select="suborganizations/suborganization">
          <xsl:text>, </xsl:text>
          <xsl:value-of select="text()" />
        </xsl:for-each>
      </mods:affiliation>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="orcid_id">
    <mods:nameIdentifier type="orcid">
      <xsl:value-of select="." />
    </mods:nameIdentifier>
  </xsl:template>

  <xsl:template match="r_id">
    <mods:nameIdentifier type="researcherid">
      <xsl:value-of select="." />
    </mods:nameIdentifier>
  </xsl:template>

  <xsl:template match="pubyear">
    <mods:originInfo>
      <mods:dateIssued encoding="w3cdtf">
        <xsl:value-of select="." />
      </mods:dateIssued>
    </mods:originInfo>
  </xsl:template>

  <xsl:template match="publisher">
    <mods:originInfo>
      <xsl:apply-templates select="address_spec" />
      <xsl:apply-templates select="names/name[1]" />
    </mods:originInfo>
  </xsl:template>

  <xsl:template match="publisher/names/name">
    <mods:publisher>
      <xsl:value-of select="unified_name" />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="publisher/address_spec">
    <mods:place>
      <mods:placeTerm type="text">
        <xsl:value-of select="city" />
      </mods:placeTerm>
    </mods:place>
  </xsl:template>

  <xsl:template match="pub_info" mode="part">
    <mods:part>
      <xsl:apply-templates select="vol" />
      <xsl:apply-templates select="issue" />
      <xsl:apply-templates select="page" />
    </mods:part>
  </xsl:template>

  <xsl:template match="vol">
    <mods:detail type="volume">
      <mods:number>
        <xsl:value-of select="." />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="issue">
    <mods:detail type="issue">
      <mods:number>
        <xsl:value-of select="." />
      </mods:number>
    </mods:detail>
  </xsl:template>

  <xsl:template match="page">
    <mods:extent unit="pages">
      <xsl:apply-templates select="begin" />
      <xsl:apply-templates select="end" />
    </mods:extent>
  </xsl:template>

  <xsl:template match="page/begin">
    <mods:start>
      <xsl:value-of select="." />
    </mods:start>
  </xsl:template>

  <xsl:template match="page/end">
    <mods:end>
      <xsl:value-of select="." />
    </mods:end>
  </xsl:template>

  <xsl:variable name="wosPrefix">
    WOS:
  </xsl:variable>

  <xsl:template match="UID[starts-with(text(),$wosPrefix)]">
    <mods:identifier type="isi">
      <xsl:value-of select="substring-after(text(),$wosPrefix)" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="identifier/entry[type='doi']">
    <mods:identifier type="doi">
      <xsl:value-of select="value" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="identifier/entry[type='pmid']">
    <mods:identifier type="pubmed">
      <xsl:value-of select="substring-after(text(),':')" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="identifier/entry[(type='issn') or (type='eissn')]">
    <mods:identifier type="issn">
      <xsl:value-of select="value" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="*" />

</xsl:stylesheet>
