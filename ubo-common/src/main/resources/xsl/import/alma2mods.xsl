<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Retrieve bibliographic data of dissertations from Alma -->
<!-- https://developers.exlibrisgroup.com/alma/apis/docs/bibs/R0VUIC9hbG1hd3MvdjEvYmlicy97bW1zX2lkfQ==/ -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl xalan java">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/bib">
    <mods:mods>
      <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#dissertation" />
      <xsl:apply-templates select="record/datafield[@tag='245']" />
      <xsl:apply-templates select="record/datafield[@tag='100']" />
      <xsl:apply-templates select="record/datafield[@tag='264']" />
      <xsl:apply-templates select="record/datafield[@tag='490']" />
      <xsl:apply-templates select="record/datafield[@tag='020']" />
      <xsl:apply-templates select="mms_id" />
      <xsl:apply-templates select="record/datafield[@tag='035']/subfield[@code='a'][starts-with(.,'(DE-605)')]" />
      <xsl:apply-templates select="record/datafield[@tag='024'][subfield[@code='2']='doi']" />
      <xsl:apply-templates select="record/datafield[@tag='AVA'][subfield[@code='d']]" />
      <xsl:apply-templates select="record/datafield[@tag='300']/subfield[@code='a']" />
      <xsl:apply-templates select="record/datafield[@tag='041']/subfield[@code='a']" />
      <xsl:apply-templates select="record/datafield[@tag='502']" />
    </mods:mods>
  </xsl:template>

  <!-- Title -->
  <xsl:template match="datafield[@tag='245']">
    <mods:titleInfo>
      <xsl:for-each select="subfield[@code='a']">
        <mods:title>
          <xsl:choose>
            <xsl:when test="starts-with(.,'&lt;&lt;')"> <!-- No support for mods:nonSort in UBO? -->
              <xsl:value-of select="normalize-space(substring-after(substring-before(text(),'&gt;&gt;'),'&lt;&lt;'))" />
              <xsl:text>  </xsl:text>
              <xsl:value-of select="normalize-space(substring-after(text(),'&gt;&gt;'))" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="text()" />
            </xsl:otherwise>
          </xsl:choose>
        </mods:title>
      </xsl:for-each>
      <xsl:for-each select="subfield[@code='b']">
        <mods:subTitle>
          <xsl:value-of select="translate(substring(text(),1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
          <xsl:value-of select="substring(text(),2)" />
        </mods:subTitle>
      </xsl:for-each>
    </mods:titleInfo>
  </xsl:template>

  <!-- Main personal name -->
  <xsl:template match="datafield[@tag='100']">
    <mods:name type="personal">
      <xsl:for-each select="subfield[@code='a']">
        <mods:namePart type="family">
          <xsl:value-of select="substring-before(.,', ')" />
        </mods:namePart>
        <mods:namePart type="given">
          <xsl:value-of select="substring-after(.,', ')" />
        </mods:namePart>
      </xsl:for-each>
      <mods:role> <!-- or: check for subfield code 4 = aut -->
        <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
      </mods:role>
      <xsl:apply-templates select="subfield[@code='0'][starts-with(text(),'(DE-588)')]" />
    </mods:name>
  </xsl:template>

  <!-- GND -->
  <xsl:template match="subfield[@code='0'][starts-with(text(),'(DE-588)')]">
    <mods:nameIdentifier type="gnd">
      <xsl:value-of select="substring-after(text(),'(DE-588)')" />
    </mods:nameIdentifier>
  </xsl:template>

  <!-- Physical extent -->
  <xsl:template match="datafield[@tag='300']/subfield[@code='a']">
    <mods:physicalDescription>
      <mods:extent>
        <xsl:value-of select="text()" />
      </mods:extent>
    </mods:physicalDescription>
  </xsl:template>

  <!-- ISBN with hyphens -->
  <xsl:template match="datafield[@tag='020'][subfield[@code='9']]" priority="1">
    <mods:identifier type="isbn">
      <xsl:value-of select="subfield[@code='9']" />
    </mods:identifier>
  </xsl:template>

  <!-- ISBN without hyphens -->
  <xsl:template match="datafield[@tag='020'][subfield[@code='0']]">
    <mods:identifier type="isbn">
      <xsl:value-of select="subfield[@code='0']" />
    </mods:identifier>
  </xsl:template>

  <!-- DOI -->
  <xsl:template match="datafield[@tag='024'][subfield[@code='2']='doi']">
    <mods:identifier type="doi">
      <xsl:value-of select="subfield[@code='a']" />
    </mods:identifier>
  </xsl:template>

  <!-- MMS ID -->
  <xsl:template match="/bib/mms_id">
    <mods:identifier type="mms">
      <xsl:value-of select="text()" />
    </mods:identifier>
  </xsl:template>

  <!-- HBZ HT Number -->
  <xsl:template match="datafield[@tag='035']/subfield[@code='a'][starts-with(.,'(DE-605)')]">
    <mods:identifier type="hbz">
      <xsl:value-of select="substring-after(text(),'(DE-605)')" />
    </mods:identifier>
  </xsl:template>

  <!-- Shelfmark -->
  <xsl:template match="datafield[@tag='AVA'][subfield[@code='d']][1]">
    <mods:location>
      <mods:shelfLocator>
        <xsl:value-of select="subfield[@code='d']" />
      </mods:shelfLocator>
    </mods:location>
  </xsl:template>

  <!-- Language -->
  <xsl:template match="datafield[@tag='041']/subfield[@code='a']">
    <mods:language>
      <mods:languageTerm authority="rfc4646" type="code">
        <xsl:value-of select="document(concat('language:',.))/language/@xmlCode" />
      </mods:languageTerm>
    </mods:language>
  </xsl:template>

  <!-- Dissertation note, unstructured -->
  <xsl:template match="datafield[@tag='502'][subfield[@code='a']]">
    <mods:note>
      <xsl:for-each select="subfield[@code='a']">
        <xsl:value-of select="text()" />
      </xsl:for-each>
    </mods:note>
  </xsl:template>

  <!-- Dissertation note, structured -->
  <xsl:template match="datafield[@tag='502'][subfield[contains('bcd',@code)]]">
    <mods:note>
      <xsl:for-each select="subfield[contains('bcd',@code)]">
        <xsl:value-of select="text()" />
        <xsl:if test="position() != last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </mods:note>
  </xsl:template>

  <!-- Publication info -->
  <xsl:template match="datafield[@tag='264'][@ind2='1']">
    <mods:originInfo>
      <xsl:apply-templates select="subfield[@code='a']" />
      <xsl:apply-templates select="subfield[@code='b']" />
      <xsl:apply-templates select="subfield[@code='c']" />
    </mods:originInfo>
  </xsl:template>

  <!-- Place -->
  <xsl:template match="datafield[@tag='264'][@ind2='1']/subfield[@code='a']">
    <mods:place>
      <mods:placeTerm type="text">
        <xsl:value-of select="text()" />
      </mods:placeTerm>
    </mods:place>
  </xsl:template>

  <!-- Publisher -->
  <xsl:template match="datafield[@tag='264'][@ind2='1']/subfield[@code='b']">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>

  <!-- Date issued -->
  <xsl:template match="datafield[@tag='264'][@ind2='1']/subfield[@code='c']">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="translate(text(),'0123456789[]()- ','0123456789')" />
    </mods:dateIssued>
  </xsl:template>

  <!-- Series information -->
  <xsl:template match="datafield[@tag='490']">
    <xsl:for-each select="subfield[@code='a']">
      <xsl:choose>
        <xsl:when test="contains(.,';')"> <!-- RAK -->
          <xsl:call-template name="series.volume">
            <xsl:with-param name="series" select="substring-before(.,';')" />
            <xsl:with-param name="volume" select="normalize-space(substring-after(.,';'))" />
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="../subfield[@code='v']"> <!-- RDA -->
          <xsl:variable name="volume" select="java:java.lang.String.new(../subfield[@code='v']/text())" />
          <xsl:call-template name="series.volume">
            <xsl:with-param name="series" select="text()" />
            <xsl:with-param name="volume" select="normalize-space(java:replaceAll($volume,'Band ',''))" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="series.volume">
            <xsl:with-param name="series" select="text()" />
            <xsl:with-param name="volume" select="''" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="series.volume">
    <xsl:param name="series" />
    <xsl:param name="volume" />

    <mods:relatedItem type="series">
      <mods:titleInfo>
        <mods:title>
          <xsl:value-of select="normalize-space($series)" />
        </mods:title>
      </mods:titleInfo>
      <xsl:if test="string-length($volume) &gt; 0">
        <mods:part>
          <mods:detail type="volume">
            <mods:number>
              <xsl:value-of select="$volume" />
            </mods:number>
          </mods:detail>
        </mods:part>
      </xsl:if>
      <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#series" />
    </mods:relatedItem>
  </xsl:template>

  <xsl:template match="*" />

</xsl:stylesheet>
