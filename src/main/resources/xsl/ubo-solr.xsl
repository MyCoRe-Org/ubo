<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  exclude-result-prefixes="mods xlink">
  
  <xsl:import href="xslImport:solr-document:ubo-solr.xsl" />

  <xsl:template match="mycoreobject">
    <xsl:apply-imports />
    <xsl:apply-templates select="service/servflags/servflag[@type='status']" mode="solrField" />
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="solrField" />
  </xsl:template>
  
  <xsl:template match="mods:mods" mode="solrField">
    <xsl:apply-templates select="mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="mods:name[@type='personal']" mode="solrField" />
    <xsl:apply-templates select="mods:name/mods:nameIdentifier" mode="solrField" />
    <xsl:apply-templates select="mods:genre[@type='intern']" mode="solrField" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="solrField" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="solrField" />
    <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:titleInfo" mode="solrField.host" />
    <xsl:apply-templates select="descendant::mods:relatedItem[@type='host'][mods:genre='journal']/mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:relatedItem[@type='series']/mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:name[@type='conference']" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:dateIssued[1][translate(text(),'1234567890','YYYYYYYYYY')='YYYY']" mode="solrField" />
    <xsl:apply-templates select="mods:identifier[@type]" mode="solrField" />
    <xsl:apply-templates select="mods:language/mods:languageTerm[@type='code']" mode="solrField" />
    <xsl:apply-templates select="mods:extension/tag" mode="solrField" />
    <xsl:apply-templates select="mods:extension/dedup" mode="solrField" />
  </xsl:template>
  
  <xsl:template match="servflag[@type='status']" mode="solrField">
    <field name="status">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:mods/mods:titleInfo" mode="solrField">
    <xsl:call-template name="buildTitleField">
      <xsl:with-param name="name">title</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="mods:relatedItem[@type='host']/mods:titleInfo" mode="solrField.host">
    <xsl:call-template name="buildTitleField">
      <xsl:with-param name="name">host_title</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='host'][mods:genre='journal']/mods:titleInfo" mode="solrField">
    <xsl:call-template name="buildTitleField">
      <xsl:with-param name="name">journal</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='series']/mods:titleInfo" mode="solrField">
    <xsl:call-template name="buildTitleField">
      <xsl:with-param name="name">journal</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="buildTitleField">
    <xsl:param name="name" />
    
    <field name="{$name}">
      <xsl:apply-templates select="mods:nonSort"  mode="solrField" />
      <xsl:apply-templates select="mods:title"    mode="solrField" />
      <xsl:apply-templates select="mods:subTitle" mode="solrField" />
    </field>
  </xsl:template>

  <xsl:template match="mods:nonSort" mode="solrField">
    <xsl:value-of select="text()" />
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="mods:title" mode="solrField">
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:subTitle" mode="solrField">
    <xsl:variable name="lastCharOfTitle" select="substring(../mods:title,string-length(../mods:title))" />
    <xsl:if test="translate($lastCharOfTitle,'?!.:,-;','.......') != '.'">
      <xsl:text> :</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <xsl:template match="mods:name[@type='personal']" mode="solrField">
    <field name="person">
      <xsl:value-of select="mods:namePart[@type='family']" />
      <xsl:for-each select="mods:namePart[@type='given'][1]">
        <xsl:value-of select="concat(', ',text())" />
      </xsl:for-each>
    </field>
  </xsl:template>
  
  <xsl:template match="mods:name[@type='conference']" mode="solrField">
    <field name="conference">
      <xsl:value-of select="mods:namePart" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:name/mods:nameIdentifier" mode="solrField">
    <field name="nid_{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:genre" mode="solrField">
    <field name="genre">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="solrField">
    <xsl:variable name="category" select="substring-after(@valueURI,'#')" /> 
    <xsl:for-each select="document(concat('classification:editor:0:parents:ORIGIN:',$category))/descendant::item">
      <field name="origin">
        <xsl:value-of select="@value" />
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'fachreferate')]" mode="solrField">
    <field name="subject">
      <xsl:value-of select="substring-after(@valueURI,'#')" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:dateIssued" mode="solrField">
    <field name="year">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:identifier[@type]" mode="solrField">
    <field name="id_{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:language/mods:languageTerm[@type='code']" mode="solrField">
    <field name="lang">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="mods:extension/tag" mode="solrField">
    <field name="tag">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="mods:extension/dedup" mode="solrField">
    <field name="dedup">
      <xsl:value-of select="@key" />
    </field>
  </xsl:template>

</xsl:stylesheet>