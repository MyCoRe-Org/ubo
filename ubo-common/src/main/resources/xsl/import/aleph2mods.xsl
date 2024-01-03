<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ==================================================
 Verwendet den Aleph X-Server, um eine Dissertation via Signatur aus Aleph zu importieren.
 
 Input: 
   <request>
     <shelfmark>BFH3309</shelfmark>
   </request> 
   
 Output:
   <mods:mods ... />  
 ================================================== -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:java="http://xml.apache.org/xalan/java"
                exclude-result-prefixes="xsl xalan java">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template name="call-aleph">
    <xsl:param name="request" />
    <xsl:variable name="url" select="concat('https://alephprod.ub.uni-due.de/X?op=',$request)" />
    <xsl:message>
      <xsl:text>Calling </xsl:text>
      <xsl:value-of select="$url" />
    </xsl:message>
    <xsl:apply-templates select="document($url)/*" />
  </xsl:template>

  <xsl:template match="/find">
    <mods:mods>
      <xsl:apply-templates select="set_number|error" />
    </mods:mods>
  </xsl:template>
  
  <xsl:template match="set_number">
    <xsl:message>
      <!-- It seems we sometimes have to wait a bit for X-Server to respond -->
      <xsl:value-of select="java:java.lang.Thread.sleep(500)" />
    </xsl:message>
    <xsl:call-template name="call-aleph">
      <xsl:with-param name="request" select="concat('present&amp;set_entry=000000001&amp;base=edu01&amp;set_number=',.)" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="error" />
  
  <xsl:template match="/present">
    <xsl:for-each select="record/metadata/oai_marc">
      <mods:genre type="intern" authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}" valueURI="{concat($WebApplicationBaseURL,'classifications/ubogenre#dissertation')}" />
      <mods:titleInfo>
        <xsl:apply-templates select="varfield[@id='331']" />
        <xsl:apply-templates select="varfield[@id='335']" />
      </mods:titleInfo>
      <xsl:apply-templates select="varfield[@id='100']" />
      <xsl:apply-templates select="varfield[@id='104']" />
      <xsl:apply-templates select="varfield[@id='108']" />
      <mods:originInfo>
        <xsl:apply-templates select="varfield[@id='410']/subfield[@label='a']|varfield[@id='419']/subfield[@label='a']" />
        <xsl:apply-templates select="varfield[@id='412']/subfield[@label='a']|varfield[@id='419']/subfield[@label='b']" />
        <xsl:apply-templates select="varfield[@id='425']" />
      </mods:originInfo>
      <xsl:apply-templates select="varfield[@id='433']" />
      <xsl:apply-templates select="varfield[@id='451']" />
      <xsl:apply-templates select="varfield[@id='540']" />
      <xsl:apply-templates select="varfield[@id='519']|varfield[@id='520']" />
      <xsl:apply-templates select="varfield[@id='037']/subfield[@label='a']" />
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="varfield[@id='331']">
    <mods:title>
      <xsl:value-of select="subfield[@label='a']" />
    </mods:title>
  </xsl:template>
  
  <xsl:template match="varfield[@id='335']">
    <mods:subTitle>
      <xsl:value-of select="subfield[@label='a']" />
    </mods:subTitle>
  </xsl:template>

  <xsl:template match="varfield[@id='100']">
    <mods:name type="personal">
      <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">aut</mods:roleTerm>
      </mods:role>
      <xsl:for-each select="subfield[@label='p'][1]|subfield[@label='a'][1]">
        <mods:namePart type="family">
          <xsl:value-of select="substring-before(.,', ')" />
        </mods:namePart>
        <mods:namePart type="given">
          <xsl:value-of select="substring-after(.,', ')" />
        </mods:namePart>
      </xsl:for-each>
      <xsl:for-each select="subfield[@label='9'][starts-with(text(),'(DE-588)')]">
        <mods:nameIdentifier type="gnd">
          <xsl:value-of select="substring-after(text(),'(DE-588)')" />
        </mods:nameIdentifier>
      </xsl:for-each>
    </mods:name>
  </xsl:template>
  
  <xsl:template match="varfield[@id='410']/subfield[@label='a']|varfield[@id='419']/subfield[@label='a']">
    <mods:place>
      <mods:placeTerm type="text">
        <xsl:value-of select="text()" />
      </mods:placeTerm>
    </mods:place>
  </xsl:template>
  
  <xsl:template match="varfield[@id='412']/subfield[@label='a']|varfield[@id='419']/subfield[@label='b']">
    <mods:publisher>
      <xsl:value-of select="text()" />
    </mods:publisher>
  </xsl:template>

  <xsl:template match="varfield[@id='425']">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="subfield[@label='a']" />
    </mods:dateIssued>
  </xsl:template>
  
  <!-- RAK: Duisburg, Essen, Univ., Diss., 2015 -->
  <xsl:template match="varfield[@id='519']">
    <mods:note>
      <xsl:if test="subfield[@label='p']">
        <xsl:value-of select="subfield[@label='p']" />
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="subfield[@label='a']" />
    </mods:note>
  </xsl:template>

  <!-- RDA: Dissertation, Universität Duisburg-Essen, 2015 -->
  <xsl:template match="varfield[@id='520']">
    <mods:note>
      <xsl:for-each select="subfield[contains('bcd',@label)]">
        <xsl:value-of select="text()" />
        <xsl:if test="position() != last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </mods:note>
  </xsl:template>
  
  <xsl:template match="varfield[@id='540'][1]">
    <mods:identifier type="isbn">
      <xsl:value-of select="subfield[@label='a']" />
    </mods:identifier>
  </xsl:template>

  <xsl:template match="varfield[@id='433'][1]">
    <mods:physicalDescription>
      <mods:extent>
        <xsl:value-of select="subfield[@label='a']" />
      </mods:extent>
    </mods:physicalDescription>
  </xsl:template>

  <xsl:template match="varfield[@id='451'][1]">
    <xsl:for-each select="subfield[@label='a']">
      <xsl:choose>
        <xsl:when test="contains(.,';')"> <!-- RAK -->
          <xsl:call-template name="series.volume">
            <xsl:with-param name="series" select="substring-before(.,';')" />
            <xsl:with-param name="volume" select="normalize-space(substring-after(.,';'))" />
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="../subfield[@label='v']"> <!-- RDA -->
          <xsl:variable name="volume" select="java:java.lang.String.new(../subfield[@label='v']/text())" />
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
      <mods:genre type="intern" authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}" valueURI="{concat($WebApplicationBaseURL,'classifications/ubogenre#series')}" />
    </mods:relatedItem>
  </xsl:template>
  
  <xsl:template match="varfield[@id='037']/subfield[@label='a']">
    <mods:language>
      <mods:languageTerm authority="rfc4646" type="code">
        <xsl:value-of select="document(concat('language:',.))/language/@xmlCode" />
      </mods:languageTerm>
    </mods:language>
  </xsl:template>

  <xsl:template match="*" />

</xsl:stylesheet>
