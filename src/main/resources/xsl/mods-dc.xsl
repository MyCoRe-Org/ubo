<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl mods xlink xalan encoder i18n"
>

  <xsl:param name="ServletsBaseURL" />

  <xsl:template match="mods:mods" mode="dc">
    <xsl:apply-templates select="mods:titleInfo" mode="dc" />
    <xsl:apply-templates select="mods:name[@type='personal']" mode="dc" />
    <xsl:apply-templates select="mods:genre[@type='intern']" mode="dc" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="dc" />
    <xsl:apply-templates select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[not(@type='host')])][1]" mode="dc" />
    <xsl:apply-templates select="mods:originInfo[mods:edition|mods:place|mods:publisher]" mode="dc" />
    <xsl:apply-templates select="mods:relatedItem[(@type='host') or (@type='series')]" mode="dc" />
    <xsl:apply-templates select="mods:identifier|mods:location/mods:url|mods:location/mods:shelfLocator" mode="dc" />
    <xsl:apply-templates select="mods:note|mods:abstract" mode="dc" />
    <xsl:apply-templates select="mods:physicalDescription/mods:extent" mode="dc" />
    <xsl:apply-templates select="mods:language/mods:languageTerm" mode="dc" />
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'fachreferate')]" mode="dc">
    <dc:subject>
      <xsl:variable name="categoryID" select="substring-after(current()/@valueURI,'#')" />
      <xsl:variable name="uri" select="concat('classification:editor:0:parents:fachreferate:',encoder:encode($categoryID,'UTF-8'))" />
      <xsl:value-of select="document($uri)/items/item/label[lang($CurrentLang)]" />
    </dc:subject>
  </xsl:template>


  <!-- DC.Title -->

  <xsl:template match="mods:titleInfo" mode="dc">
    <dc:title>
      <xsl:copy-of select="@xml:lang" />
      <xsl:apply-templates select="." />
    </dc:title>
  </xsl:template>

  <!-- DC.Creator -->

  <xsl:template match="mods:name[mods:role/mods:roleTerm][contains($creator.roles,mods:role/mods:roleTerm[@type='code'])]" mode="dc" priority="1">
    <dc:creator>
      <xsl:apply-templates select="." />
    </dc:creator>
  </xsl:template>

  <!-- DC.Contributor -->

  <xsl:template match="mods:name[@type='personal']" mode="dc">
    <dc:contributor>
      <xsl:apply-templates select="." />
      <xsl:text> (</xsl:text>
      <xsl:apply-templates select="mods:role/mods:roleTerm" />
      <xsl:text>)</xsl:text>
    </dc:contributor>
  </xsl:template>

  <!-- DC.Type -->

  <xsl:template match="mods:genre[@type='intern']" mode="dc">
    <dc:type>
      <xsl:value-of select="." />
    </dc:type>
    <dc:type>
      <xsl:apply-templates select="." />
    </dc:type>
  </xsl:template>

  <!-- DC.Date -->

  <xsl:template match="mods:dateIssued[@encoding='w3cdtf']" mode="dc">
    <dc:date>
      <xsl:value-of select="text()" />
    </dc:date>
  </xsl:template>

  <!-- DC.Publisher -->

  <xsl:template match="mods:name[mods:role/mods:roleTerm='pbl']" mode="dc">
    <dc:publisher>
      <xsl:apply-templates select="." />
    </dc:publisher>
  </xsl:template>

  <xsl:template match="mods:originInfo" mode="dc">
    <dc:publisher>
      <xsl:apply-templates select="." />
    </dc:publisher>
  </xsl:template>

  <!-- DC.Relation -->

  <xsl:template match="mods:relatedItem[@type='host']" mode="dc">
    <dc:relation>
      <xsl:apply-templates select="." mode="brief" />
      <xsl:apply-templates select="mods:identifier[@type='isbn']|mods:identifier[@type='issn']" mode="dc.host" />
      <xsl:apply-templates select="mods:location/mods:shelfLocator" mode="dc.host" />
    </dc:relation>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='isbn']|mods:identifier[@type='issn']" mode="dc.host">
    <xsl:text>, </xsl:text>
    <xsl:value-of select="translate(@type,'isbn','ISBN')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:location/mods:shelfLocator" mode="dc.host">
    <xsl:text>, Signatur: </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='series']" mode="dc">
    <dc:relation>
      <xsl:apply-templates select="." />
    </dc:relation>
  </xsl:template>

  <!-- DC.Description -->

  <xsl:template match="mods:note" mode="dc">
    <dc:description>
      <xsl:value-of select="text()" />
    </dc:description>
  </xsl:template>

  <xsl:template match="mods:abstract" mode="dc">
    <xsl:if test="string-length(text()) &gt; 0">
      <dc:description>
        <xsl:copy-of select="@xml:lang" />
        <xsl:value-of select="text()" />
      </dc:description>
    </xsl:if>
    <xsl:apply-templates select="@xlink:href" mode="dc" />
  </xsl:template>

  <xsl:template match="mods:abstract/@xlink:href" mode="dc">
    <dc:description>
      <xsl:copy-of select="@xml:lang" />
      <xsl:value-of select="." />
    </dc:description>
  </xsl:template>

  <!-- DC.Format -->

  <xsl:template match="mods:physicalDescription/mods:extent" mode="dc">
    <dc:format>
      <xsl:value-of select="." />
    </dc:format>
  </xsl:template>

  <!-- DC.Language -->

  <xsl:template match="mods:languageTerm[@authority='rfc4646'][@type='code']" mode="dc">
    <dc:language>
      <xsl:value-of select="document(concat('language:',.))/language/@termCode" />
    </dc:language>
  </xsl:template>

  <!-- DC.Identifier -->

  <xsl:template match="mods:identifier[@type='urn']" mode="dc">
    <dc:identifier>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='issn']" mode="dc">
    <dc:identifier>
      <xsl:text>ISSN </xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='isbn']" mode="dc">
    <dc:identifier>
      <xsl:text>ISBN </xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.DOIResolver" />

  <xsl:template match="mods:identifier[@type='doi']" mode="dc">
    <dc:identifier>
      <xsl:value-of select="$UBO.DOIResolver" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='hdl']" mode="dc">
    <dc:identifier>
      <xsl:text>https://hdl.handle.net/</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.PubMed.Link" />

  <xsl:template match="mods:identifier[@type='pubmed']" mode="dc">
    <dc:identifier>
      <xsl:value-of select="$UBO.PubMed.Link" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:param name="UBO.Scopus.Link" />

  <xsl:template match="mods:identifier[@type='scopus']" mode="dc">
    <dc:identifier>
      <xsl:value-of select="$UBO.Scopus.Link" />
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico']" mode="dc">
    <dc:publisher>Universitätsbibliothek Duisburg-Essen, Dokumenten- und Publikationsserver DuEPublico</dc:publisher>
    <dc:identifier>
      <xsl:text>https://duepublico.uni-due.de/servlets/DocumentServlet?id=</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico2']" mode="dc">
    <dc:publisher>Universitätsbibliothek Duisburg-Essen, Dokumenten- und Publikationsserver DuEPublico</dc:publisher>
    <dc:identifier>
      <xsl:text>https://duepublico2.uni-due.de/receive/</xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:identifier" mode="dc">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:location/mods:shelfLocator" mode="dc">
    <dc:identifier>
      <xsl:text>Signatur: </xsl:text>
      <xsl:value-of select="text()" />
    </dc:identifier>
    <dc:identifier>
      <xsl:variable name="sig">
        <xsl:apply-templates select="." mode="normalize.shelfmark" />
      </xsl:variable>
      <xsl:value-of select="concat($primo.search,'holding_call_number,exact,%22',encoder:encode(.),'%22')" />
    </dc:identifier>
  </xsl:template>

  <xsl:template match="mods:location/mods:url" mode="dc">
    <dc:identifier>
      <xsl:value-of select="text()" />
    </dc:identifier>
  </xsl:template>

</xsl:stylesheet>
