<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ==================================================
 Übernahme einer E-Dissertation von DuEPublico zur Universitätsbibliographie
 ================================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl mods">

  <xsl:template match="/mycoreobject">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" />
  </xsl:template>
  
  <xsl:template match="mods:mods">
    <xsl:copy>
      <mods:genre type="intern">dissertation</mods:genre>
      <xsl:copy-of select="mods:classification[contains(@valueURI,'classifications/ORIGIN#')]" />
      <xsl:apply-templates select="mods:titleInfo" />
      <xsl:apply-templates select="mods:name[@type='personal'][contains('aut ths',mods:role/mods:roleTerm)]" />
      <xsl:apply-templates select="mods:originInfo" />
      <xsl:apply-templates select="mods:identifier[@type='doi']" />
      <xsl:copy-of select="mods:identifier[@type='urn']" />
      <xsl:apply-templates select="/mycoreobject/@ID" /> 
      <xsl:copy-of select="mods:language" />
      <xsl:copy-of select="mods:subject[mods:topic]" />
      <xsl:copy-of select="mods:abstract" />
      <xsl:call-template name="oa" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:titleInfo">
    <mods:titleInfo>
      <mods:title> <!-- dozbib has no field mods:nonSort -->
        <xsl:apply-templates select="mods:nonSort" />
        <xsl:value-of select="mods:title" />
      </mods:title>
      <xsl:copy-of select="mods:subTitle" />
    </mods:titleInfo>
  </xsl:template>
  
  <xsl:template match="mods:nonSort">
    <xsl:value-of select="text()" />
    <xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template match="mods:name">
    <mods:name>
      <xsl:copy-of select="@type" />
      <xsl:copy-of select="mods:role" />
      <xsl:copy-of select="mods:namePart" />
      <xsl:apply-templates select="@valueURI" />
    </mods:name>
  </xsl:template>
  
  <!-- get LSF PID via legalEntityID -->
  <xsl:template match="mods:name/@valueURI">
    <xsl:variable name="legalEntityID" select="substring-after(.,'#')" />
    <xsl:for-each select="document(concat('notnull:legalEntity:',$legalEntityID))/legalEntity/@pid">
      <mods:nameIdentifier type="lsf">
        <xsl:value-of select="." />
      </mods:nameIdentifier>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="mods:originInfo">
    <mods:originInfo>
      <mods:place>
        <mods:placeTerm type="text">Duisburg, Essen</mods:placeTerm>
      </mods:place>
      <xsl:choose>
        <xsl:when test="mods:dateCreated">
          <xsl:apply-templates select="mods:dateCreated"  />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mods:dateModified"  />
        </xsl:otherwise>
      </xsl:choose>    
    </mods:originInfo>
    <xsl:apply-templates select="mods:dateOther[@type='accepted']" />
  </xsl:template>
  
  <!-- source has no dateIssued, guess from other dates -->
  <xsl:template match="mods:dateCreated|mods:dateModified">
    <mods:dateIssued encoding="w3cdtf">
      <xsl:value-of select="substring-before(.,'-')" />
    </mods:dateIssued>
  </xsl:template>
  
  <!-- Hochschulschriftenvermerk -->
  <xsl:template match="mods:dateOther">
    <mods:note>
      <xsl:text>Dissertation, Universität Duisburg-Essen, </xsl:text>
      <xsl:value-of select="substring-before(.,'-')" />
    </mods:note>
  </xsl:template>
  
  <xsl:template match="@ID">
    <mods:identifier type="duepublico">
      <xsl:value-of select="number(substring-after(.,'miless_mods_'))" />
    </mods:identifier>
  </xsl:template>
  
  <xsl:template match="mods:identifier[@type='doi']">
    <mods:identifier type="doi">
      <xsl:value-of select="substring-after(.,'doi:')" />
    </mods:identifier>
  </xsl:template>

  <xsl:variable name="authorityOA">https://bibliographie.ub.uni-due.de/classifications/oa</xsl:variable>

  <xsl:template name="oa">
    <mods:classification authorityURI="{$authorityOA}" valueURI="{$authorityOA}#oa" />
  </xsl:template>

</xsl:stylesheet>
