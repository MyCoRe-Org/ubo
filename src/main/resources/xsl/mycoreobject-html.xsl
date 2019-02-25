<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 34267 $ $Date: 2016-01-05 13:56:19 +0100 (Di, 05 Jan 2016) $ -->
<!-- ============================================== --> 

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"  
  exclude-result-prefixes="xsl xalan i18n mods"  
>

<xsl:param name="ServletsBaseURL" />

<!-- ============ Einzeltreffer HTML Export ============ -->

  <xsl:template match="mycoreobject" mode="html-export">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="html-export" />
  </xsl:template>

  <xsl:template match="mods:mods" mode="html-export">
    <xsl:apply-templates select="." mode="cite"> 
      <xsl:with-param name="mode">divs</xsl:with-param> 
    </xsl:apply-templates>
    <xsl:call-template name="ibn-export">
      <xsl:with-param name="type">isbn</xsl:with-param>
    </xsl:call-template> 
    <xsl:apply-templates select="mods:identifier[@type='doi']" mode="html-export" />
    <xsl:apply-templates select="mods:identifier[@type='urn']" mode="html-export" /> 
    <xsl:apply-templates select="mods:identifier[@type='duepublico']" mode="html-export" /> 
    <xsl:apply-templates select="mods:identifier[@type='duepublico2']" mode="html-export" /> 
  </xsl:template>

  <!-- ========== Liste von ISBNs oder ISSNs =========== -->

  <xsl:template name="ibn-export">
    <xsl:param name="type" />
    <xsl:variable name="identifiers" select="mods:identifier[@type=$type]|mods:relatedItem[@type='host']/mods:identifier[@type=$type]" />
    <xsl:if test="count($identifiers) &gt; 0">
      <div class="isn">
        <xsl:value-of select="translate($type,'isbn','ISBN')" />
        <xsl:text>: </xsl:text>
        <xsl:for-each select="$identifiers">
          <xsl:value-of select="text()" />
          <xsl:if test="position() != last()">, </xsl:if>
        </xsl:for-each>
      </div>
    </xsl:if>
  </xsl:template>
  
  <!-- ========== DuEPublico ID ========== -->
  
  <xsl:template match="mods:identifier[@type='duepublico']" mode="html-export">
    <div class="link">
      <xsl:value-of select="i18n:translate('result.dozbib.fulltext')"/>:
      <a href="https://duepublico.uni-due.de/servlets/DocumentServlet?id={text()}" target="_blank">
        <xsl:value-of select="i18n:translate('document')"/>
        <xsl:value-of select="text()"/>
      </a>
    </div>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico2']" mode="html-export">
    <div class="link">
      <xsl:value-of select="i18n:translate('result.dozbib.fulltext')"/>:
      <a href="https://duepublico2.uni-due.de/receive/{text()}" target="_blank">
        <xsl:value-of select="i18n:translate('document')"/>
        <xsl:value-of select="text()"/>
      </a>
    </div>
  </xsl:template>

  <!-- ========== DOI ========== -->
  
  <xsl:param name="UBO.DOIResolver" />
  
  <xsl:template match="mods:identifier[@type='doi']" mode="html-export">
    <div class="link">
      <xsl:text>DOI: </xsl:text>
      <a href="{$UBO.DOIResolver}{text()}" target="_blank">
        <xsl:value-of select="text()"/>
      </a>
    </div>
  </xsl:template>

  <!-- ========== URN ========== -->
  
  <xsl:template match="mods:identifier[@type='urn']" mode="html-export">
    <div class="link">
      <xsl:text>URN: </xsl:text>
      <a href="https://nbn-resolving.org/{text()}" target="_blank">
        <xsl:value-of select="text()"/>
      </a>
    </div>
  </xsl:template>

</xsl:stylesheet>
