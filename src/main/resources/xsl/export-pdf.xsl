<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== --> 

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"  
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl i18n xalan mods"
>

<xsl:output method="xml" encoding="UTF-8" media-type="application/pdf" />

<xsl:param name="loaded_navigation_xml" />
<xsl:param name="lastPage" />
<xsl:param name="href" />
<xsl:param name="HttpSession" />
<xsl:param name="RequestURL" />
<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="ServletsBaseURL" />

<xsl:include href="bibmaster.xsl" />
<xsl:include href="mods-display.xsl" />
<xsl:include href="mycoreobject-html.xsl" />
<xsl:include href="coreFunctions.xsl" />

<xsl:template match="/export">
  <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  
    <fo:layout-master-set>
      <fo:simple-page-master master-name="pageLayout"
        page-height="29.7cm" page-width="21cm"
        margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">        
        <fo:region-body background-color="white" margin-top="2cm" margin-bottom="2cm"/>
        <fo:region-before extent="2cm" />
        <fo:region-after extent="1cm" />
      </fo:simple-page-master>
    </fo:layout-master-set>
    
    <fo:page-sequence master-reference="pageLayout" initial-page-number="1">
                      
      <fo:static-content flow-name="xsl-region-before">
        <fo:block font-size="14pt" font-family="serif" font-weight="bold" space-before.optimum="20pt"
          space-after="30pt" text-align="center" text-decoration="underline">
          <xsl:value-of select="i18n:translate('search.collection.pup')"/>
        </fo:block>
      </fo:static-content>
      
      <fo:static-content flow-name="xsl-region-after">
        <fo:block>
          <fo:leader leader-pattern="rule" leader-length="16cm" />
        </fo:block>
        <fo:block font-size="10pt" font-family="serif" text-align="right">
          <xsl:value-of select="i18n:translate('result.dozbib.pdfpage')"/> 
          <fo:page-number/>
        </fo:block>
      </fo:static-content>
      
      <fo:flow flow-name="xsl-region-body">
        <xsl:apply-templates select="mycoreobject" />
      </fo:flow>

    </fo:page-sequence>
      
  </fo:root>
</xsl:template>

<xsl:template match="mycoreobject">
  <fo:table width="180mm" table-layout="fixed">
    <fo:table-column column-width="177mm"/>
    <fo:table-body>
      <fo:table-row keep-together.within-column="always">
        <fo:table-cell>
     
          <fo:block text-align="left" margin-top="20pt">
            <fo:list-block provisional-distance-between-starts="10mm" end-indent="10mm" start-indent="10mm">
              <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                  <fo:block font-size="10pt" font-family="serif">
                    <xsl:number level="any"/>
                  </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                
                  <xsl:variable name="export.html">
                    <xsl:apply-templates select="." mode="html-export" /> 
                  </xsl:variable>
                  <xsl:apply-templates select="xalan:nodeset($export.html)" />
                  
                </fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
          </fo:block>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-body>
  </fo:table>
</xsl:template>

<xsl:template match="div">
  <fo:block font-size="12pt" font-family="serif">
    <xsl:apply-templates select="@class|*|text()" />
  </fo:block>
</xsl:template>

<xsl:template match="@class[.='authors']">
  <xsl:attribute name="font-style">italic</xsl:attribute>
</xsl:template>

<xsl:template match="@class[.='title']">
  <xsl:attribute name="font-weight">bold</xsl:attribute>
</xsl:template>

<xsl:template match="a">
  <fo:basic-link external-destination="url('{@href}')" color="blue"> 
    <xsl:value-of select="." />
  </fo:basic-link>
</xsl:template>

<xsl:template match="text()">
  <xsl:value-of select="." />
</xsl:template>

<xsl:template match="@*" />

</xsl:stylesheet>