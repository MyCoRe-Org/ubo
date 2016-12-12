<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 32347 $ $Date: 2015-04-28 10:44:24 +0200 (Di, 28 Apr 2015) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3" 
  exclude-result-prefixes="xsl xalan i18n mods">

  <xsl:template match="/bibentries">
    <xsl:copy>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="bibentry">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="mods:mods" />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="mods:mods">
    <xsl:apply-templates select="mods:genre" />
    <xsl:apply-templates select="mods:classification" />
    <xsl:apply-templates select="mods:titleInfo/mods:title|mods:titleInfo/mods:subTitle|mods:name[@type='conference']" />
    <xsl:apply-templates select="descendant::mods:name[@type='personal']" />
    <xsl:apply-templates select="descendant::mods:relatedItem[@type='host']/mods:titleInfo" />
    <xsl:apply-templates select="descendant::mods:place/mods:placeTerm" />
    <xsl:apply-templates select="descendant::mods:publisher" />
    <xsl:apply-templates select="descendant::mods:edition" />
    <xsl:apply-templates select="descendant::mods:dateIssued" />
    <xsl:apply-templates select="descendant::mods:detail[@type='volume']|descendant::mods:titleInfo/mods:partNumber" />
    <xsl:apply-templates select="descendant::mods:detail[@type='issue']" />
    <xsl:apply-templates select="descendant::mods:extent[@unit='pages']" />
    <xsl:apply-templates select="descendant::mods:relatedItem[@type='series']/mods:titleInfo/mods:title" />
    <xsl:apply-templates select="descendant::mods:physicalDescription/mods:extent" />
    <xsl:apply-templates select="descendant::mods:identifier" />
    <xsl:apply-templates select="descendant::mods:url" />
    <xsl:apply-templates select="descendant::mods:shelfLocator" />
    <xsl:apply-templates select="descendant::mods:note" />
    <xsl:apply-templates select="descendant::mods:abstract" />
  </xsl:template>
  
  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />
  
  <xsl:template match="mods:genre[@type='intern']">
    <xsl:attribute name="type">
      <xsl:variable name="legacy" select="$genres//category[@ID=current()]/label[lang('x-legacy')]/@text" />
      <xsl:choose>
        <xsl:when test="string-length($legacy) &gt; 0">
          <xsl:value-of select="$legacy" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'fachreferate')]">
    <subject>
      <xsl:value-of select="substring-after(@valueURI,'#')" />
    </subject>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]">
    <origin>
      <xsl:value-of select="substring-after(@valueURI,'#')" />
    </origin>
  </xsl:template>

  <xsl:template match="mods:title|mods:subTitle">
    <title>
      <xsl:value-of select="text()" />
    </title>
  </xsl:template>
  
  <xsl:template match="mods:name[@type='conference']">
    <title>
      <xsl:value-of select="mods:namePart" />
    </title>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='host']/mods:titleInfo">
    <xsl:if test="../mods:genre='collection'">
      <xsl:apply-templates select="mods:title|mods:subTitle" mode="book" />
      <xsl:apply-templates select="../mods:name[@type='conference']" mode="book" />
    </xsl:if>
    <xsl:if test="../mods:genre='journal'">
      <xsl:apply-templates select="mods:title|mods:subTitle" mode="journal" />
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="mods:title|mods:subTitle" mode="book">
    <book>
      <xsl:value-of select="text()" />
    </book>
  </xsl:template>

  <xsl:template match="mods:name[@type='conference']" mode="book">
    <book>
      <xsl:value-of select="mods:namePart" />
    </book>
  </xsl:template>

  <xsl:template match="mods:title|mods:subTitle" mode="journal">
    <journal>
      <xsl:value-of select="text()" />
    </journal>
  </xsl:template>

  <xsl:template match="mods:name[@type='personal']">
    <contributor>
      <xsl:apply-templates select="mods:role/mods:roleTerm" />
      <xsl:apply-templates select="mods:namePart" />
      <xsl:apply-templates select="mods:nameIdentifier[@type='lsf']" />
    </contributor>
  </xsl:template>
  
  <xsl:template match="mods:roleTerm[@authority='marcrelator'][@type='code']">
    <xsl:attribute name="role">
      <xsl:choose>
        <xsl:when test=".='aut'">author</xsl:when>
        <xsl:when test=".='edt'">publisher</xsl:when>
        <xsl:when test=".='ths'">advisor</xsl:when>
        <xsl:when test=".='rev'">referee</xsl:when>
        <xsl:when test=".='trl'">translator</xsl:when>
        <xsl:when test=".='ctb'">contributor</xsl:when>
        <xsl:otherwise>contributor</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="mods:namePart[@type='family']">
    <lastName>
      <xsl:value-of select="text()" />
    </lastName>
  </xsl:template>

  <xsl:template match="mods:namePart[@type='given']">
    <firstName>
      <xsl:value-of select="text()" />
    </firstName>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='lsf']">
    <pid>
      <xsl:value-of select="text()" />
    </pid>
  </xsl:template>
  
  <xsl:template match="mods:relatedItem[@type='series']/mods:titleInfo/mods:title">
    <series>
      <xsl:value-of select="text()" />
    </series>
  </xsl:template>
  
  <xsl:template match="mods:place/mods:placeTerm">
    <place>
      <xsl:value-of select="text()" />
    </place>
  </xsl:template>

  <xsl:template match="mods:publisher">
    <publisher>
      <xsl:value-of select="text()" />
    </publisher>
  </xsl:template>

  <xsl:template match="mods:edition">
    <edition>
      <xsl:value-of select="text()" />
    </edition>
  </xsl:template>

  <xsl:template match="mods:dateIssued">
    <year>
      <xsl:value-of select="text()" />
    </year>
  </xsl:template>

  <xsl:template match="mods:part/mods:detail[@type='volume']">
    <volume>
      <xsl:value-of select="mods:number" />
    </volume>
  </xsl:template>
  
  <xsl:template match="mods:titleInfo/mods:partNumber">
    <volume>
      <xsl:value-of select="text()" />
    </volume>
  </xsl:template>

  <xsl:template match="mods:part/mods:detail[@type='issue']">
    <issue>
      <xsl:value-of select="mods:number" />
    </issue>
  </xsl:template>

  <!-- In editor, all variants of page numbers are edited in a single text field -->
  <xsl:template match="mods:extent[@unit='pages']">
    <pages>
      <xsl:apply-templates select="mods:*" />
    </pages>
  </xsl:template>
  
  <xsl:template match="mods:start">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:end">
    <xsl:text> - </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:total[../mods:start]">
    <xsl:text> (</xsl:text>
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="mods:total">
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
  </xsl:template>

  <xsl:template match="mods:list">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:physicalDescription/mods:extent">
    <size>
      <xsl:value-of select="text()" />
    </size>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='isbn']">
    <isbn>
      <xsl:value-of select="text()" />
    </isbn>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='issn']">
    <issn>
      <xsl:value-of select="text()" />
    </issn>
  </xsl:template>
  
  <xsl:template match="mods:identifier[@type='doi']">
    <url>
      <xsl:value-of select="concat('http://dx.doi.org/',text())" />
    </url>
  </xsl:template>

  <xsl:param name="MIL.PubMed.Link" />

  <xsl:template match="mods:identifier[@type='pubmed']">
    <url>
      <xsl:value-of select="concat($MIL.PubMed.Link,text())" />
    </url>
  </xsl:template>

  <xsl:param name="MIL.Scopus.Link" />

  <xsl:template match="mods:identifier[@type='scopus']">
    <url>
      <xsl:value-of select="concat($MIL.Scopus.Link,text())" />
    </url>
  </xsl:template>
  
  <xsl:template match="mods:identifier[@type='urn']">
    <url>
      <xsl:value-of select="concat('http://nbn-resolving.org/',text())" />
    </url>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico']">
    <documentID>
      <xsl:value-of select="text()" />
    </documentID>
  </xsl:template>

  <xsl:template match="mods:url">
    <url>
      <xsl:value-of select="text()" />
    </url>
  </xsl:template>
  
  <xsl:template match="mods:shelfLocator">
    <signature>
      <xsl:value-of select="text()" />
    </signature>
  </xsl:template>
  
  <xsl:template match="mods:note">
    <comment>
      <xsl:value-of select="text()" />
    </comment>
  </xsl:template>

  <xsl:template match="mods:abstract">
    <abstract>
      <xsl:value-of select="text()" />
    </abstract>
  </xsl:template>

  <xsl:template match="@*|*|text()" />

</xsl:stylesheet>
