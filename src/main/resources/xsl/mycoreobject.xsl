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
  exclude-result-prefixes="xsl xalan ubo mods xlink i18n dc mcr"  
>

<xsl:include href="layout.xsl" />
<xsl:include href="bibmaster.xsl" />
<xsl:include href="mods-dc-meta.xsl" />
<xsl:include href="mods-highwire.xsl" />
<xsl:include href="mods-display.xsl" />

<xsl:variable name="entryID" select="number(substring-after(/mycoreobject/@ID,'ubo_mods_'))" />
  
<xsl:param name="Referer" select="concat($ServletsBaseURL,'DozBibEntryServlet?mode=show&amp;id=',$entryID)" />
<xsl:param name="PageNr"  />
<xsl:param name="ListKey" />
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
  <item label="{i18n:translate('result.dozbib.results')}">
    <xsl:if test="string-length($ListKey) > 0">
      <xsl:attribute name="ref">
        <xsl:text>servlets/DozBibServlet?mode=list&amp;pageNr=</xsl:text>
        <xsl:value-of select="$PageNr" />
        <xsl:text>&amp;numPerPage=10&amp;listKey=</xsl:text>
        <xsl:value-of select="$ListKey" />
      </xsl:attribute>
    </xsl:if>
  </item>  
  <item label="{i18n:translate('result.dozbib.entry')} {$entryID}" />
</xsl:variable>

<!-- ============ Aktionen ============ -->

<xsl:variable name="actions">
  <xsl:if test="$permission.admin and (string-length($step) = 0) and not ($UBO.System.ReadOnly = 'true')">
    <action label="{i18n:translate('button.edit')}" target="{$WebApplicationBaseURL}edit-publication.xed">
      <param name="id"     value="{$entryID}" />
    </action>
    <action label="Admin" target="{$WebApplicationBaseURL}edit-admin.xed">
      <param name="id"     value="{$entryID}" />
    </action>
    <action label="{i18n:translate('button.delete')}" target="{$ServletsBaseURL}DozBibEntryServlet">
      <param name="mode"       value="show" />
      <param name="XSL.step"   value="ask.delete" />
      <param name="id"         value="{$entryID}" />
    </action>
  </xsl:if>
  <action label="{i18n:translate('button.basketAdd')}" target="{$ServletsBaseURL}MCRBasketServlet">
    <param name="type"    value="bibentries" />
    <param name="action"  value="add" />
    <param name="resolve" value="true" />
    <param name="id"      value="{$entryID}" />
    <param name="uri"     value="ubo:{$entryID}" />
  </action>
  <xsl:if test="string-length($step) = 0">
    <action label="MODS" target="{$ServletsBaseURL}MCRExportServlet/mods-{$entryID}.xml">
      <param name="uri"          value="ubo:{$entryID}" />
      <param name="root"         value="bibentries" />
      <param name="transformer"  value="mods" />
    </action>
    <action label="BibTeX" target="{$ServletsBaseURL}MCRExportServlet/bibentry-{$entryID}.bib">
      <param name="uri"          value="ubo:{$entryID}" />
      <param name="root"         value="bibentries" />
      <param name="transformer"  value="bibtex" />
    </action>
    <action label="EndNote" target="{$ServletsBaseURL}MCRExportServlet/bibentry-{$entryID}.enl">
      <param name="uri"          value="ubo:{$entryID}" />
      <param name="root"         value="bibentries" />
      <param name="transformer"  value="endnote" />
    </action>
    <action label="RIS" target="{$ServletsBaseURL}MCRExportServlet/bibentry-{$entryID}.ris">
      <param name="uri"          value="ubo:{$entryID}" />
      <param name="root"         value="bibentries" />
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
      <xsl:call-template name="pubtype" />
      <xsl:call-template name="label-year" />
      <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="label-info" />
      <xsl:apply-templates select="mods:extension/tag" />
    </div>
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" />
    <xsl:apply-templates select="/mycoreobject/service/servflags/servflag[@type='status']" />
    <xsl:call-template name="steps.and.actions" /> 
  </div>
  <div class="section highlight2" style="padding-top:2ex; padding-bottom:2ex;">
    <div class="container_12 ubo_details">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </div>
  <xsl:if test="$permission.admin and dedup">
    <xsl:call-template name="listDuplicates" />
  </xsl:if>
 </xsl:for-each>
</xsl:template>

<xsl:template match="mods:extension/tag">
  <span class="ubo-tag">
    <xsl:value-of select="text()" />
  </span>
</xsl:template>

<!-- Do internal query for duplicates and show them -->
<xsl:template name="listDuplicates">
  <xsl:variable name="duplicatesURI">
    <xsl:for-each select="mods:extension[dedup]">
      <xsl:call-template name="buildFindDuplicatesURI" />
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="duplicates" select="document($duplicatesURI)/mcr:results/mcr:hit" />
  <xsl:variable name="numDuplicates" select="count($duplicates) - 1" />
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
      <xsl:variable name="myOwnHitID" select="concat('ubo:',$entryID)" />
      <ul>
        <xsl:for-each select="$duplicates">
          <xsl:sort select="substring-after(@id,'ubo:')" data-type="number" order="descending" />
          <xsl:if test="not(@id = $myOwnHitID)">
            <li>
              <xsl:variable name="id" select="substring-after(@id,'ubo:')" />
              <a href="DozBibEntryServlet?mode=show&amp;id={$id}">
                <xsl:text>Eintrag </xsl:text>
                <xsl:value-of select="$id" />
              </a>
              <xsl:for-each select="document(@id)/bibentry/mods:mods">
                <div class="bibentry">
                  <xsl:apply-templates select="." mode="cite">
                    <xsl:with-param name="mode">divs</xsl:with-param>
                  </xsl:apply-templates>
                </div>
                <xsl:call-template name="pubtype" />
                <xsl:call-template name="label-year" />
              </xsl:for-each>
            </li>
          </xsl:if>
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
    onclick="self.location.href='{$ServletsBaseURL}DozBibEntryServlet?mode=delete&amp;id={$entryID}'" />
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
