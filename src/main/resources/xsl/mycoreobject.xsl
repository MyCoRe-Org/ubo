<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== --> 

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:ubo="xalan://unidue.ubo.DozBibEntryServlet"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xsl xalan ubo mods xlink i18n dc mcr encoder"  
>

<xsl:include href="layout.xsl" />
<xsl:include href="mods-dc-meta.xsl" />
<xsl:include href="mods-highwire.xsl" />
<xsl:include href="mods-display.xsl" />

<xsl:param name="Referer" select="concat($ServletsBaseURL,'DozBibEntryServlet?mode=show&amp;id=',/mycoreobject/@ID)" />
<xsl:param name="CurrentUserPID" />
<xsl:param name="step" />

<!-- ============ Bearbeitungsrechte ========== -->

<xsl:variable name="permission.admin" xmlns:check="xalan://unidue.ubo.AccessControl" select="check:currentUserIsAdmin()" />

<!-- ============ Seitentitel ============ -->

<xsl:variable name="page.title"> 
  <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
    <xsl:for-each select="mods:name[@type='personal'][1]">
      <xsl:apply-templates select="mods:namePart[@type='family']"/>
      <xsl:apply-templates select="mods:namePart[@type='given']"/>
      <xsl:text>: </xsl:text>
    </xsl:for-each>
    <xsl:apply-templates select="mods:titleInfo[1]" />
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="pageLastModified">
  <xsl:value-of select="substring-before(/mycoreobject/service/servdates/servdate[@type='modifydate'],'T')" />
</xsl:variable>

<!-- ========== Dublin Core and Highwire Press meta tags ========== -->

<xsl:variable name="head.additional">
  <xsl:apply-templates select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" mode="dc-meta" />
  <xsl:apply-templates select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" mode="highwire" />
  <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
</xsl:variable>

<!-- ========== Navigation ========== -->

<xsl:variable name="ContextID" select="'dozbib'" />

<xsl:variable name="breadcrumb.extensions">
  <item label="{i18n:translate('result.dozbib.results')}" />
  <item label="{i18n:translate('result.dozbib.entry')} {number(substring-after(/mycoreobject/@ID,'ubo_mods_'))}" />
</xsl:variable>

<!-- ============ Aktionen ============ -->

<xsl:variable name="actions">
  <xsl:if test="$permission.admin and (string-length($step) = 0) and not ($UBO.System.ReadOnly = 'true')">
    <action label="{i18n:translate('button.edit')}" target="{$WebApplicationBaseURL}edit-publication.xed">
      <param name="id"     value="{/mycoreobject/@ID}" />
    </action>
    <action label="Admin" target="{$WebApplicationBaseURL}edit-admin.xed">
      <param name="id"     value="{/mycoreobject/@ID}" />
    </action>
    <action label="{i18n:translate('button.delete')}" target="{$ServletsBaseURL}DozBibEntryServlet">
      <param name="mode"       value="show" />
      <param name="XSL.step"   value="ask.delete" />
      <param name="id"         value="{/mycoreobject/@ID}" />
    </action>
  </xsl:if>
  <xsl:if xmlns:basket="xalan://unidue.ubo.basket.BasketUtils" test="basket:hasSpace() and not(basket:contains(string(/mycoreobject/@ID)))">
    <action label="{i18n:translate('button.basketAdd')}" target="{$ServletsBaseURL}MCRBasketServlet">
      <param name="type"    value="bibentries" />
      <param name="action"  value="add" />
      <param name="resolve" value="true" />
      <param name="id"      value="{/mycoreobject/@ID}" />
      <param name="uri"     value="mcrobject:{/mycoreobject/@ID}" />
    </action>
  </xsl:if>
  <xsl:if test="string-length($step) = 0">
    <action label="MODS" target="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.xml">
      <param name="uri"          value="mcrobject:{/mycoreobject/@ID}" />
      <param name="root"         value="export" />
      <param name="transformer"  value="mods" />
    </action>
    <action label="BibTeX" target="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.bib">
      <param name="uri"          value="mcrobject:{/mycoreobject/@ID}" />
      <param name="root"         value="export" />
      <param name="transformer"  value="bibtex" />
    </action>
    <action label="EndNote" target="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.enl">
      <param name="uri"          value="mcrobject:{/mycoreobject/@ID}" />
      <param name="root"         value="export" />
      <param name="transformer"  value="endnote" />
    </action>
    <action label="RIS" target="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.ris">
      <param name="uri"          value="mcrobject:{/mycoreobject/@ID}" />
      <param name="root"         value="export" />
      <param name="transformer"  value="ris" />
    </action>
  </xsl:if>
</xsl:variable>

<!-- ============ Rechte Seite: Inhalte ============ -->

<xsl:template match="mycoreobject">
 <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
  <div class="section bibentry highlight2" style="padding:1ex;">
    <xsl:apply-templates select="." mode="cite">
      <xsl:with-param name="mode">divs</xsl:with-param>
    </xsl:apply-templates>
  </div>
  <div class="section">
    <div class="labels">
      <xsl:call-template name="label-year" />
      <xsl:call-template name="pubtype" />
    </div>
    <div class="labels">
      <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="label-info" />
      <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="label-info" />
    </div>
    <xsl:if test="mods:extension/tag">
      <div class="labels">
        <xsl:apply-templates select="mods:extension/tag" />
      </div>
    </xsl:if>
    <xsl:apply-templates select="/mycoreobject/service/servflags/servflag[@type='status']" />
    <xsl:call-template name="steps.and.actions" /> 
  </div>
  <div class="section highlight2" style="padding-top:2ex; padding-bottom:2ex;">
    <div class="container_12 ubo_details">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </div>
  <xsl:if test="$permission.admin and mods:extension[dedup]">
    <xsl:call-template name="listDuplicates" />
  </xsl:if>
 </xsl:for-each>
</xsl:template>

<xsl:variable name="quotes">"</xsl:variable>

<xsl:template match="mods:extension/tag">
  <span class="ubo-tag">
    <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+tag:{encoder:encode(concat($quotes,text(),$quotes),'UTF-8')}">
      <xsl:value-of select="text()" />
    </a>
  </span>
</xsl:template>

<!-- ============ Dubletten suchen und anzeigen ============ -->

<xsl:template name="listDuplicates">

  <xsl:variable name="duplicatesURI">
    <xsl:for-each select="mods:extension[dedup]">
      <xsl:call-template name="buildFindDuplicatesURI" />
    </xsl:for-each>
  </xsl:variable>
  
  <xsl:variable name="myID" select="/mycoreobject/@ID" />
  
  <xsl:variable name="duplicates1" select="document($duplicatesURI)/response/result[@name='response']/doc" />
  <xsl:variable name="duplicates2">
    <xsl:for-each select="$duplicates1">
      <xsl:sort select="str[@name='id']" data-type="number" order="descending" />
      <xsl:variable name="duplicateID" select="str[@name='id']" />
      <xsl:if test="not($duplicateID = $myID)">
        <xsl:value-of select="str[@name='id']" />
        <xsl:text> </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="duplicates3" select="xalan:tokenize($duplicates2)" />

  <xsl:variable name="numDuplicates" select="count($duplicates3)" />
  <xsl:if test="$numDuplicates &gt; 0">
    <div class="highlight1 duplicates">
      <h3>
        <xsl:text>Es gibt eventuell </xsl:text>
        <xsl:choose>
          <xsl:when test="$numDuplicates = 1">eine Dublette</xsl:when> 
          <xsl:otherwise>
            <xsl:value-of select="$numDuplicates" />
            <xsl:text> Dubletten</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>:</xsl:text> 
      </h3>
      <ul>
        <xsl:for-each select="$duplicates3">
          <li>
            <a href="DozBibEntryServlet?mode=show&amp;id={.}">
              <xsl:text>Eintrag </xsl:text>
              <xsl:value-of select="number(substring-after(.,'_mods_'))" />
            </a>
            <xsl:for-each select="document(concat('mcrobject:',.))/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
              <div class="bibentry">
                <xsl:apply-templates select="." mode="cite">
                  <xsl:with-param name="mode">divs</xsl:with-param>
                </xsl:apply-templates>
              </div>
              <xsl:call-template name="pubtype" />
              <xsl:call-template name="label-year" />
            </xsl:for-each>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template name="steps.and.actions">
  <xsl:choose>
    <xsl:when test="$step='ask.delete'">
      <xsl:call-template name="ask.delete" />
    </xsl:when>
    <xsl:when test="$step='confirm.submitted'">
      <xsl:call-template name="confirm.submitted" />
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="confirm.submitted">
  <p>
    <xsl:value-of select="i18n:translate('result.dozbib.now')"/>
  </p>
  
  <ul>
    <li>
      <xsl:value-of select="i18n:translate('result.dozbib.nowSaved')"/>
      <strong><xsl:value-of select="i18n:translate('result.dozbib.nowSavedStrong')"/></strong>
    </li>              
    <li>
      <xsl:value-of select="i18n:translate('result.dozbib.nowCheck')"/>
    </li>
    <li>
      <xsl:value-of select="i18n:translate('result.dozbib.nowAccess')"/>
    </li>
  </ul>

  <p>
    <a href="{$WebApplicationBaseURL}newPublication.xed">
      <xsl:value-of select="i18n:translate('result.dozbib.registerMore')"/>
    </a>
  </p>
</xsl:template>

<xsl:template name="ask.delete">
  <p>
    <strong>
      <xsl:value-of select="i18n:translate('result.dozbib.delete')"/>
    </strong>
  </p>  

  <input type="button" class="editorButton" name="delete" value="{i18n:translate('button.deleteYes')}" 
    onclick="self.location.href='{$ServletsBaseURL}DozBibEntryServlet?mode=delete&amp;id={/mycoreobject/@ID}'" />
  <input type="button" class="editorButton" name="cancel" value="{i18n:translate('button.cancelNo')}" 
    onclick="self.location.href='{$Referer}'" />
</xsl:template>

<xsl:template match="servflag[@type='status']">
 <xsl:if test="$permission.admin">
  <p>  
    <strong>    
      <xsl:value-of select="i18n:translate(concat('result.dozbib.status.detailed.',.))" />      
    </strong>    
  </p>
 </xsl:if> 
</xsl:template>

</xsl:stylesheet>
