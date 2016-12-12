<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== --> 

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"  
  exclude-result-prefixes="xsl xalan i18n mods"  
>

<xsl:include href="output-category.xsl" />

<xsl:param name="ServletsBaseURL" />
<xsl:param name="UBO.LSF.Link" />
<xsl:param name="CurrentLang" />

<!-- ============ Katalogsuche Basis-URLs ============ -->
<xsl:variable name="primo.search">
  <xsl:text>http://primo.ub.uni-due.de/primo_library/libweb/action/dlSearch.do</xsl:text>
  <xsl:text>?vid=UDE&amp;institution=UDE&amp;bulkSize=10&amp;indx=1&amp;onCampus=false&amp;query=</xsl:text>
</xsl:variable>

<!-- ============ Fächerliste (subject) laden ============ -->
<xsl:variable name="subjects" select="document('resource:fachreferate.xml')/fachreferate" />

<!-- ============ Ausgabe Publikationsart ============ -->

<xsl:template name="pubtype">
  <span class="label-info">
    <xsl:apply-templates select="mods:mods/mods:genre[@type='intern']" />
    <xsl:for-each select="mods:mods/mods:relatedItem[@type='host']/mods:genre[@type='intern']">
      <xsl:text> in </xsl:text>
      <xsl:apply-templates select="." />
    </xsl:for-each> 
  </span>
</xsl:template>

<!-- ============ Ausgabe Fach ============ -->

<xsl:template match="mods:mods/mods:classification[contains(@authorityURI,'fachreferate')]" mode="label-info">
  <span class="label-info">
    <xsl:value-of select="$subjects/item[@value=substring-after(current()/@valueURI,'#')]/@label"/>
  </span>
</xsl:template>

<!-- ========== Ausgabe Fakultät ========== -->

<xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]">
  <div class="labels">
    <span class="label-info">
      <xsl:call-template name="output.category">
        <xsl:with-param name="classID" select="'ORIGIN'" />
        <xsl:with-param name="categID" select="substring-after(@valueURI,'#')" />
      </xsl:call-template>
    </span>
  </div>
</xsl:template>

<!-- ========== Ausgabe Jahr ========== -->

<xsl:template name="label-year">
  <span class="label-info">
    <xsl:value-of select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[not(@type='host')])][1]" />
  </span>
</xsl:template>

<!-- ========== URI bauen, um Dubletten zu finden ========== -->

<xsl:template name="buildFindDuplicatesURI">
  <xsl:variable name="queryTerm">
    <xsl:for-each select="dedup">
      <xsl:text>( ubo_dedup = </xsl:text>
      <xsl:value-of select="@key" />
      <xsl:text> )</xsl:text>
      <xsl:if test="position() != last()">
        <xsl:text> OR </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <xsl:value-of xmlns:encoder="xalan://java.net.URLEncoder" select="concat('notnull:mcrql:term=',encoder:encode($queryTerm,'UTF-8'))" />
</xsl:template>

</xsl:stylesheet>
