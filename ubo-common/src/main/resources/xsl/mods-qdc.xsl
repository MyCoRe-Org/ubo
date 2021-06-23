<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  exclude-result-prefixes="xsl mods xlink xalan encoder"
>

  <xsl:include href="mods-dc.xsl" />

  <xsl:template match="mods:mods" mode="qdc">
    <xsl:apply-templates select="mods:titleInfo[1]" mode="dc" />
    <xsl:apply-templates select="mods:titleInfo[position() &gt; 1]" mode="qdc" />
    <xsl:apply-templates select="mods:name[@type='personal']" mode="dc" />
    <xsl:apply-templates select="mods:genre[@type='intern']" mode="dc" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="dc" />
    <xsl:apply-templates select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[not(@type='host')])][1]" mode="qdc" />
    <xsl:apply-templates select="mods:originInfo/mods:dateOther" mode="qdc" />
    <xsl:apply-templates select="mods:originInfo[mods:edition|mods:place|mods:publisher]" mode="dc" />
    <xsl:apply-templates select="mods:relatedItem[(@type='host') or (@type='series')]" mode="qdc" />
    <xsl:apply-templates select="mods:identifier|mods:location/mods:url|mods:location/mods:shelfLocator" mode="qdc" />
    <xsl:apply-templates select="mods:note" mode="dc" />
    <xsl:apply-templates select="mods:abstract|mods:abstract/@xlink:href" mode="qdc" />
    <xsl:apply-templates select="mods:physicalDescription/mods:extent" mode="qdc" />
    <xsl:apply-templates select="mods:language/mods:languageTerm" mode="qdc" />
  </xsl:template>

  <!-- title -->

  <xsl:template match="mods:titleInfo[position() &gt; 1]" mode="qdc">
    <dcterms:alternative>
      <xsl:copy-of select="@xml:lang" />
      <xsl:apply-templates select="." />
    </dcterms:alternative>
  </xsl:template>

  <!-- date issued -->

  <xsl:template match="mods:dateIssued[@encoding='w3cdtf']" mode="qdc">
    <dcterms:issued scheme="dcterms:W3CDTF">
      <xsl:value-of select="text()" />
    </dcterms:issued>
  </xsl:template>

  <!-- date other -->

  <xsl:template match="mods:dateOther[@encoding='w3cdtf'][@type='accepted']" mode="qdc">
    <dcterms:dateAccepted scheme="dcterms:W3CDTF">
      <xsl:value-of select="text()" />
    </dcterms:dateAccepted>
  </xsl:template>

  <xsl:template match="mods:dateOther[@encoding='w3cdtf'][@type='submitted']" mode="qdc">
    <dcterms:dateSubmitted scheme="dcterms:W3CDTF">
      <xsl:value-of select="text()" />
    </dcterms:dateSubmitted>
  </xsl:template>

  <xsl:template match="mods:dateOther" mode="qdc" />

  <!-- relation -->

  <xsl:template match="mods:relatedItem[@type='host']" mode="qdc">
    <dcterms:isPartOf>
      <xsl:apply-templates select="." mode="brief" />
      <xsl:apply-templates select="mods:identifier[@type='isbn']|mods:identifier[@type='issn']" mode="dc.host" />
      <xsl:apply-templates select="mods:location/mods:shelfLocator" mode="dc.host" />
    </dcterms:isPartOf>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='series']" mode="qdc">
    <dcterms:isPartOf>
      <xsl:apply-templates select="." />
    </dcterms:isPartOf>
  </xsl:template>

  <!-- abstract -->

  <xsl:template match="mods:abstract[string-length(text()) &gt; 0]" mode="qdc">
    <dcterms:abstract>
      <xsl:copy-of select="@xml:lang" />
      <xsl:value-of select="text()" />
    </dcterms:abstract>
  </xsl:template>

  <xsl:template match="mods:abstract/@xlink:href" mode="qdc">
    <dcterms:abstract scheme="dcterms:URI">
      <xsl:copy-of select="@xml:lang" />
      <xsl:value-of select="." />
    </dcterms:abstract>
  </xsl:template>

  <!-- extent -->

  <xsl:template match="mods:physicalDescription/mods:extent" mode="qdc">
    <dcterms:extent>
      <xsl:value-of select="text()" />
    </dcterms:extent>
  </xsl:template>

  <!-- language -->

  <xsl:template match="mods:languageTerm[@authority='rfc4646'][@type='code']" mode="qdc">
    <dcterms:language scheme="dcterms:RFC4646">
      <xsl:value-of select="text()" />
    </dcterms:language>
  </xsl:template>

  <!-- identifier -->

  <xsl:template match="mods:identifier[@type='urn']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='issn']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:text>urn:ISSN:</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='isbn']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:text>urn:ISBN:</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.DOIResolver" />

  <xsl:template match="mods:identifier[@type='doi']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:value-of select="$UBO.DOIResolver" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='hdl']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:text>https://hdl.handle.net/</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.PubMed.Link" />

  <xsl:template match="mods:identifier[@type='pubmed']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:value-of select="$UBO.PubMed.Link" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.Scopus.Link" />

  <xsl:template match="mods:identifier[@type='scopus']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:value-of select="$UBO.Scopus.Link" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:text>https://duepublico.uni-due.de/servlets/DocumentServlet?id=</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico2']" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:text>https://duepublico2.uni-due.de/receive/</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier" mode="qdc">
    <dc:identifier>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:location/mods:shelfLocator" mode="qdc">
    <xsl:if test="$UBO.Primo.Search.Link and string-length($UBO.Primo.Search.Link) &gt; 0">
      <dc:identifier scheme="dcterms:URI">
        <xsl:variable name="sig">
          <xsl:apply-templates select="." mode="normalize.shelfmark" />
        </xsl:variable>
        <xsl:value-of select="concat($UBO.Primo.Search.Link,'holding_call_number,exact,%22',encoder:encode(.),'%22')" />
      </dc:identifier>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:location/mods:url" mode="qdc">
    <dc:identifier scheme="dcterms:URI">
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

</xsl:stylesheet>
