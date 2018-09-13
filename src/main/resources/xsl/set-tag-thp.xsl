<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xsl mods">
  
  <xsl:include href="copynodes.xsl" />
  
  <xsl:template match="mods:extension">
    <xsl:copy>
      <xsl:apply-templates />
      <xsl:variable name="isFromTHP">
        <xsl:choose>
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='49640'">true</xsl:when>
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='49884'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='1127'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='1126'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='2867'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='50405'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='59791'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='59789'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='57938'">true</xsl:when>
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='54740'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='59793'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='55028'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='55030'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='58881'">true</xsl:when> 
          <xsl:when test="../mods:name[mods:role/mods:roleTerm[@type='code'][contains('aut cre tch pht prg edt',text())]]/mods:nameIdentifier[@type='lsf']='59672'">true</xsl:when> 
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="$isFromTHP='true'">
        <tag>thp</tag>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

