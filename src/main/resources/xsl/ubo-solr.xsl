<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  exclude-result-prefixes="mods xlink">
  
  <xsl:import href="xslImport:solr-document:ubo-solr.xsl" />

  <xsl:template match="mycoreobject">
    <xsl:apply-templates select="." mode="baseFields" />
    <xsl:apply-templates select="service/servflags/servflag[@type='status']" mode="solrField" />
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="solrField" />
  </xsl:template>
  
  <xsl:template match="mods:mods" mode="solrField">
    <xsl:apply-templates select="mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code']" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:name/mods:nameIdentifier" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:name[mods:nameIdentifier[@type='lsf']]" mode="solrField.lsf" />
    <xsl:apply-templates select="descendant::mods:name[@type='personal']" mode="child" />
    <xsl:apply-templates select="mods:genre[@type='intern']" mode="solrField" />
    <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:genre[@type='intern']" mode="solrField" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="solrField" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="solrField" />
    <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:titleInfo" mode="solrField.host" />
    <xsl:apply-templates select="mods:relatedItem[@type='host'][mods:genre='journal']/mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:relatedItem[@type='series']/mods:titleInfo" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:name[@type='conference']" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:dateIssued[1][translate(text(),'1234567890','YYYYYYYYYY')='YYYY']" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:identifier[@type]" mode="solrField" />
    <xsl:apply-templates select="descendant::mods:shelfLocator" mode="solrField" />
    <xsl:apply-templates select="mods:note" mode="solrField" />
    <xsl:apply-templates select="mods:abstract" mode="solrField" />
    <xsl:apply-templates select="mods:language/mods:languageTerm[@type='code']" mode="solrField" />
    <xsl:apply-templates select="mods:extension/tag" mode="solrField" />
    <xsl:apply-templates select="mods:extension/dedup" mode="solrField" />
    <xsl:call-template name="sortby_person" />
  </xsl:template>
  
  <xsl:template name="sortby_person">
    <xsl:if test="mods:name[@type='personal']">
      <field name="sortby_person">
        <xsl:for-each select="mods:name[@type='personal']">
          <xsl:value-of select="concat(mods:namePart[@type='family'],' ',mods:namePart[@type='given'],' ')" />
        </xsl:for-each>
      </field>
    </xsl:if>
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
    <xsl:if test="position() = 1"> <!-- sort by first title only, not multi-valued -->
      <field name="sortby_title">
        <xsl:apply-templates select="mods:title"    mode="solrField" />
        <xsl:apply-templates select="mods:subTitle" mode="solrField" />
      </field>
    </xsl:if>
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
  
  <xsl:template match="mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code']" mode="solrField">
    <field name="person_{text()}">
      <xsl:apply-templates select="../.." mode="solrField" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:name[@type='personal']" mode="solrField">
    <xsl:value-of select="mods:namePart[@type='family']" />
    <xsl:for-each select="mods:namePart[@type='given'][1]">
      <xsl:value-of select="concat(', ',text())" />
    </xsl:for-each>
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
  
  <xsl:template match="mods:name[mods:nameIdentifier[@type='lsf']]" mode="solrField.lsf">
    <xsl:for-each select="mods:role/mods:roleTerm[@type='code']">
      <field name="role_lsf">
        <xsl:value-of select="." />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="../../mods:nameIdentifier[@type='lsf']" />
      </field>
      <field name="role_lsf"> <!-- support for legacy role codes -->
        <xsl:choose>
          <xsl:when test=".='aut'">author</xsl:when>
          <xsl:when test=".='edt'">publisher</xsl:when>
          <xsl:when test=".='ths'">advisor</xsl:when>
          <xsl:when test=".='rev'">referee</xsl:when>
          <xsl:when test=".='trl'">translator</xsl:when>
          <xsl:when test=".='ctb'">contributor</xsl:when>
          <xsl:otherwise>contributor</xsl:otherwise>
        </xsl:choose>
        <xsl:text>_</xsl:text>
        <xsl:value-of select="../../mods:nameIdentifier[@type='lsf']" />
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="mods:mods/mods:genre[@type='intern']" mode="solrField">
    <field name="genre">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <xsl:template match="mods:relatedItem[@type='host']/mods:genre[@type='intern']" mode="solrField">
    <field name="host_genre">
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
  
  <xsl:template match="mods:shelfLocator" mode="solrField">
    <field name="shelfmark">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="mods:language/mods:languageTerm[@type='code']" mode="solrField">
    <field name="lang">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template match="mods:note" mode="solrField">
    <field name="note">
      <xsl:value-of select="text()" />
    </field>
    <xsl:if test="contains(.,'Univ') and contains(.,'Diss') and ( contains(.,'Essen') or contains(.,'Duisburg') ) and contains( translate(.,'0123456789','JJJJJJJJJJ'),'JJJJ')">
      <xsl:variable name="jjjj" select="translate(.,'0123456789','JJJJJJJJJJ')" />
      <xsl:variable name="before" select="substring-before($jjjj,'JJJJ')" />
      <field name="year_diss">
        <xsl:value-of select="substring(substring-after(.,$before),1,4)" />
      </field>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:abstract" mode="solrField">
    <field name="abstract">
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

  <xsl:template match="mods:name[@type='personal']" mode="child">
    <doc>
      <field name="objectKind">name</field>
      <field name="id">
        <xsl:value-of select="concat(ancestor::mycoreobject/@ID,'_',generate-id(.))" />
      </field>
      <xsl:apply-templates select="mods:nameIdentifier" mode="child" />
    </doc>
  </xsl:template>
  
  <xsl:template match="mods:nameIdentifier" mode="child">
    <field name="nid_type">
      <xsl:value-of select="@type" />
    </field>
  </xsl:template>

</xsl:stylesheet>