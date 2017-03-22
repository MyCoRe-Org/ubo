<?xml version="1.0" encoding="UTF-8"?>

<!-- Converts the Excel version of http://www.oe.uni-due.de/start/forschung/Publikationen/index.asp?l=de to MODS -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
  exclude-result-prefixes="xsl xalan ss" 
>

<xsl:template match="/">
  <mods:modsCollection>
    <xsl:apply-templates select="//ss:Row[position() &gt; 1]" />
  </mods:modsCollection>
</xsl:template>

<xsl:template match="ss:Row">
  <xsl:variable name="genres">
    <xsl:choose>
      <xsl:when test="ss:Cell[29]/ss:Data='1' or ss:Cell[20]/ss:Data[string-length(.) &gt; 0]">article journal</xsl:when>
      <xsl:when test="ss:Cell[21]/ss:Data[string-length(.) &gt; 0] and ss:Cell[31]/ss:Data='1'">chapter proceedings</xsl:when>
      <xsl:when test="ss:Cell[21]/ss:Data[string-length(.) &gt; 0] or ss:Cell[30]/ss:Data='1'">chapter collection</xsl:when>
      <xsl:otherwise>speech proceedings</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="hasHost" select="string-length(concat(ss:Cell[20]/ss:Data,ss:Cell[21]/ss:Data,ss:Cell[24]/ss:Data,ss:Cell[25]/ss:Data,ss:Cell[26]/ss:Data,ss:Cell[37]/ss:Data)) &gt; 0" />
  
  <mods:mods>
    <mods:genre type="intern">
      <xsl:value-of select="substring-before($genres,' ')" />
    </mods:genre>
    <xsl:apply-templates select="ss:Cell[12]" /> <!-- title -->
    <xsl:apply-templates select="ss:Cell[(position() &gt; 1) and (position() &lt; 12)]" />  <!-- author -->
    <xsl:apply-templates select="ss:Cell[28]" /> <!-- doi -->
    <xsl:if test="starts-with($genres,'article') and string-length(ss:Cell[27]/ss:Data) &gt; 0">
      <mods:originInfo>
        <xsl:apply-templates select="ss:Cell[27]" /> <!-- year --> 
      </mods:originInfo>
    </xsl:if>
    <xsl:if test="$hasHost">
      <mods:relatedItem type="host">
        <mods:genre type="intern">
          <xsl:value-of select="substring-after($genres,' ')" />
        </mods:genre>
        <xsl:apply-templates select="ss:Cell[20]" /> <!-- journal -->
        <xsl:apply-templates select="ss:Cell[21]" /> <!-- book -->
        <xsl:if test="string-length(ss:Cell[19]/ss:Data) &gt; 0">
          <xsl:choose>
            <xsl:when test="string-length(concat(ss:Cell[20]/ss:Data,ss:Cell[21]/ss:Data)) = 0">
              <mods:titleInfo>
                <mods:title>
                  <xsl:value-of select="ss:Cell[19]/ss:Data" /> <!-- conference as title -->
                </mods:title>
              </mods:titleInfo>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="ss:Cell[19]" /> <!-- conference -->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
        <xsl:apply-templates select="ss:Cell[37]" /> <!-- issn, isbn -->
        <xsl:if test="contains($genres,'proceedings') or contains($genres,'collection')">
          <xsl:if test="string-length(concat(ss:Cell[22]/ss:Data,ss:Cell[23]/ss:Data,ss:Cell[27]/ss:Data)) &gt; 0">
            <mods:originInfo>
              <xsl:apply-templates select="ss:Cell[23]" /> <!-- place -->
              <xsl:apply-templates select="ss:Cell[22]" /> <!-- publisher -->
              <xsl:apply-templates select="ss:Cell[27]" /> <!-- year -->
            </mods:originInfo>
          </xsl:if>
        </xsl:if>
        <xsl:call-template name="part" />
      </mods:relatedItem>
    </xsl:if>
    <xsl:if test="string-length(concat(ss:Cell[35]/ss:Data,ss:Cell[36]/ss:Data)) &gt; 0">
      <mods:location>
        <xsl:apply-templates select="ss:Cell[35]" /> <!-- url -->
        <xsl:apply-templates select="ss:Cell[36]" /> <!-- pdf -->
      </mods:location>
    </xsl:if>
    <xsl:apply-templates select="ss:Cell[39]" /> <!-- abstract -->
    <mods:classification valueURI="http://duepublico.uni-duisburg-essen.de/classifications/ORIGIN#18.02.07" authorityURI="http://duepublico.uni-duisburg-essen.de/classifications/ORIGIN"/>
    <mods:classification valueURI="http://duepublico.uni-duisburg-essen.de/classifications/fachreferate#tech" authorityURI="http://duepublico.uni-duisburg-essen.de/classifications/fachreferate"/>
  </mods:mods>
</xsl:template>

<xsl:template match="ss:Cell">
  <xsl:apply-templates select="ss:Data" />
</xsl:template>

<xsl:template match="ss:Cell[string-length(normalize-space(ss:Data))=0]" priority="1" />

<xsl:template match="*" />

<xsl:template name="part">
  <xsl:if test="string-length(concat(ss:Cell[24]/ss:Data,ss:Cell[25]/ss:Data,ss:Cell[26]/ss:Data)) &gt; 0">
    <mods:part>
      <xsl:apply-templates select="ss:Cell[24]" /> <!-- volume -->
      <xsl:apply-templates select="ss:Cell[25]" /> <!-- issue -->
      <xsl:apply-templates select="ss:Cell[26]" /> <!-- pages -->
    </mods:part>
  </xsl:if>
</xsl:template>

<xsl:template match="ss:Cell[12]/ss:Data|ss:Cell[20]/ss:Data|ss:Cell[21]/ss:Data">
  <mods:titleInfo>
    <mods:title>
      <xsl:value-of select="." />
    </mods:title>
  </mods:titleInfo>
</xsl:template>

<xsl:template match="ss:Cell[19]/ss:Data">
  <mods:name type="conference">
    <mods:namePart>
      <xsl:value-of select="." />
    </mods:namePart>
  </mods:name>
</xsl:template>

<xsl:template match="ss:Cell[2]/ss:Data|ss:Cell[3]/ss:Data|ss:Cell[4]/ss:Data|ss:Cell[5]/ss:Data|ss:Cell[6]/ss:Data|ss:Cell[7]/ss:Data|ss:Cell[8]/ss:Data|ss:Cell[9]/ss:Data|ss:Cell[10]/ss:Data|ss:Cell[11]/ss:Data">
  <xsl:for-each select="xalan:tokenize(.,',')">
    <mods:name type="personal">
      <mods:namePart type="family">
        <xsl:for-each select="xalan:tokenize(.)">
          <xsl:variable name="first" select="substring(string(.),1,1)" />
          <xsl:variable name="isUpperCase" select="translate($first,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','')=''" />
          <xsl:if test="(position() = last()) or not($isUpperCase)">
            <xsl:value-of select="." />
            <xsl:if test="position() != last()">
              <xsl:text> </xsl:text>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>
      </mods:namePart>
      <mods:namePart type="given">
        <xsl:for-each select="xalan:tokenize(.)">
          <xsl:variable name="first" select="substring(string(.),1,1)" />
          <xsl:variable name="isUpperCase" select="translate($first,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','')=''" />
          <xsl:if test="position() != last() and $isUpperCase">
            <xsl:if test="position() != 1">
              <xsl:text> </xsl:text>
            </xsl:if>
            <xsl:value-of select="." />
          </xsl:if>
        </xsl:for-each>
      </mods:namePart>
      <mods:role>
        <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
      </mods:role>
    </mods:name>
  </xsl:for-each>
</xsl:template>

<xsl:template match="ss:Cell[22]/ss:Data">
  <mods:publisher>
    <xsl:value-of select="." />
  </mods:publisher>
</xsl:template>

<xsl:template match="ss:Cell[23]/ss:Data">
  <mods:place>
    <mods:placeTerm>
      <xsl:value-of select="." />
    </mods:placeTerm>
  </mods:place>
</xsl:template>

<xsl:template match="ss:Cell[27]/ss:Data">
  <mods:dateIssued encoding="w3cdtf">
    <xsl:value-of select="." />
  </mods:dateIssued>
</xsl:template>

<xsl:template match="ss:Cell[28]/ss:Data">
  <mods:identifier type="doi">
    <xsl:value-of select="." />
  </mods:identifier>
</xsl:template>

<xsl:template match="ss:Cell[24]/ss:Data[contains(.,'ol.')]">
  <mods:detail type="volume">
    <mods:number>
      <xsl:value-of select="normalize-space(substring-after(.,'ol.'))" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="ss:Cell[24]/ss:Data[not(contains(.,'ol.'))]">
  <mods:detail type="volume">
    <mods:number>
      <xsl:value-of select="." />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="ss:Cell[25]/ss:Data[starts-with(.,'no.') or starts-with(.,'No.')]">
  <mods:detail type="issue">
    <mods:number>
      <xsl:value-of select="normalize-space(substring-after(.,'o.'))" />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="ss:Cell[25]/ss:Data[not(starts-with(.,'no.') or starts-with(.,'No.'))]">
  <mods:detail type="issue">
    <mods:number>
      <xsl:value-of select="." />
    </mods:number>
  </mods:detail>
</xsl:template>

<xsl:template match="ss:Cell[26]/ss:Data">
  <xsl:copy-of xmlns:pages="xalan://org.mycore.mods.MCRMODSPagesHelper" select="pages:buildExtentPagesNodeSet(text())" />
</xsl:template>

<xsl:template match="ss:Cell[35]/ss:Data">
  <mods:url>
    <xsl:value-of select="." />
  </mods:url>
</xsl:template>

<xsl:template match="ss:Cell[36]/ss:Data">
  <mods:url>
    <xsl:text>http://www.oe.uni-due.de/research/publications/</xsl:text>
    <xsl:value-of select="." />
    <xsl:text>.pdf</xsl:text>
  </mods:url>
</xsl:template>

<xsl:template match="ss:Cell[37]/ss:Data[starts-with(.,'ISSN ')]">
  <mods:identifier type="issn">
    <xsl:value-of select="substring-after(.,'ISSN ')" />
  </mods:identifier>
</xsl:template>

<xsl:template match="ss:Cell[37]/ss:Data[starts-with(.,'ISBN ')]">
  <mods:identifier type="isbn">
    <xsl:value-of select="substring-after(.,'ISBN ')" />
  </mods:identifier>
</xsl:template>

<xsl:template match="ss:Cell[39]/ss:Data">
  <mods:abstract>
    <xsl:value-of select="." />
  </mods:abstract>
</xsl:template>

</xsl:stylesheet>