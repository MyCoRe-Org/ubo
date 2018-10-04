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

<xsl:include href="mods-dc-meta.xsl" />
<xsl:include href="mods-highwire.xsl" />
<xsl:include href="mods-display.xsl" />
<xsl:include href="mods-sherpa-romeo.xsl" />

<xsl:param name="Referer" select="concat($ServletsBaseURL,'DozBibEntryServlet?id=',/mycoreobject/@ID)" />
<xsl:param name="CurrentUserPID" />
<xsl:param name="step" />
<xsl:param name="UBO.System.ReadOnly" />

<!-- ============ Bearbeitungsrechte ========== -->

<xsl:variable name="permission.admin" xmlns:check="xalan://unidue.ubo.AccessControl" select="check:currentUserIsAdmin()" />

<!-- ============ Seitentitel ============ -->

<xsl:template name="page.title"> 
 <title>
  <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
    <xsl:for-each select="mods:name[@type='personal'][1]">
      <xsl:apply-templates select="mods:namePart[@type='family']"/>
      <xsl:apply-templates select="mods:namePart[@type='given']"/>
      <xsl:text>: </xsl:text>
    </xsl:for-each>
    <xsl:apply-templates select="mods:titleInfo[1]" />
  </xsl:for-each>
 </title>
</xsl:template>

<xsl:template name="pageLastModified">
 <xsl:attribute name="lastModified">
  <xsl:value-of select="substring-before(/mycoreobject/service/servdates/servdate[@type='modifydate'],'T')" />
 </xsl:attribute>
</xsl:template>

<!-- ========== Dublin Core and Highwire Press meta tags ========== -->

<xsl:template name="head.additional">
  <xsl:apply-templates select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" mode="dc-meta" />
  <xsl:apply-templates select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" mode="highwire" />
  <link rel="stylesheet" href="{$WebApplicationBaseURL}i/clouds/grid12.css" />
  <script src="{$WebApplicationBaseURL}js/mycore2orcid.js" />
</xsl:template>

<!-- ========== Navigation ========== -->

<xsl:variable name="breadcrumb.extensions">
  <item label="{i18n:translate('result.dozbib.results')}" />
  <item label="{i18n:translate('result.dozbib.entry')} {number(substring-after(/mycoreobject/@ID,'ubo_mods_'))}" />
</xsl:variable>

<!-- ============ Aktionen ============ -->

<xsl:template name="actions">
  <div id="buttons">
    <xsl:if test="$permission.admin and (string-length($step) = 0) and not ($UBO.System.ReadOnly = 'true')">
      <a class="action" href="{$WebApplicationBaseURL}edit-publication.xed?id={/mycoreobject/@ID}">
        <xsl:value-of select="i18n:translate('button.edit')" />
      </a>
      <a class="action" href="{$WebApplicationBaseURL}edit-admin.xed?id={/mycoreobject/@ID}">Admin</a>
      <xsl:if test="not(/mycoreobject/structure/children/child)">
        <a class="action" href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.step=ask.delete">
          <xsl:value-of select="i18n:translate('button.delete')" />
        </a>
      </xsl:if>
      <xsl:if test="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host'][string-length(@xlink:href)=0]">
        <!-- Button to extract mods:relatedItem[@type='host'] to a new separate entry -->
        <a class="action" href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;mode=xhost">
          <xsl:value-of select="i18n:translate('ubo.relatedItem.host.separate')" />
        </a>
      </xsl:if>
    </xsl:if>
    <xsl:if xmlns:basket="xalan://unidue.ubo.basket.BasketUtils" test="basket:hasSpace() and not(basket:contains(string(/mycoreobject/@ID)))">
      <a class="action" href="{$ServletsBaseURL}MCRBasketServlet?type=bibentries&amp;action=add&amp;resolve=true&amp;id={/mycoreobject/@ID}&amp;uri=mcrobject:{/mycoreobject/@ID}">
        <xsl:value-of select="i18n:translate('button.basketAdd')" />
      </a>
    </xsl:if>
    <xsl:if test="string-length($step) = 0">
      <a class="action" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.xml?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=mods">MODS</a>
      <xsl:if test="not($permission.admin)">
        <a class="action" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.bib?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=bibtex">BibTeX</a>
        <a class="action" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.enl?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=endnote">EndNote</a>
        <a class="action" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.ris?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=ris">RIS</a>
      </xsl:if>    
    </xsl:if>
  </div>
</xsl:template>

<!-- ============ Seite ============ -->

<xsl:template match="/">
  <html>
    <xsl:call-template name="pageLastModified" />
    <head>
      <xsl:call-template name="head.additional" />
      <xsl:call-template name="page.title" />
    </head>
    <body>
      <xsl:call-template name="actions" />
      <xsl:apply-templates select="mycoreobject" />
    </body>
  </html>
</xsl:template>

<!-- ============ Rechte Seite: Inhalte ============ -->

<xsl:template match="mycoreobject">
 <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
  <xsl:call-template name="steps.and.actions" /> 
  <div class="section bibentry highlight2" style="padding:1ex;">
    <xsl:apply-templates select="." mode="cite">
      <xsl:with-param name="mode">divs</xsl:with-param>
    </xsl:apply-templates>
  </div>
  <div class="section">
    <xsl:call-template name="altmetrics" />
    <div class="labels">
      <xsl:call-template name="label-year" />
      <xsl:call-template name="pubtype" />
      <xsl:call-template name="label-oa" />
      <xsl:call-template name="orcid-status" />
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
    <xsl:apply-templates select="/mycoreobject/structure/children[child]" />
  </div>
  <div class="section highlight2" style="padding-top:2ex; padding-bottom:2ex;">
    <div class="container_12 ubo_details">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </div>
  <xsl:apply-templates select="." mode="romeo" />
  <xsl:if test="$permission.admin and mods:extension[dedup]">
    <xsl:call-template name="listDuplicates" />
  </xsl:if>
 </xsl:for-each>
</xsl:template>

<xsl:template match="/mycoreobject/structure/children[child]">
  <div class="labels">
    <span class="label-info">
      <xsl:value-of select="i18n:translate('ubo.relatedItem.host.contains')"/>
      <xsl:text>: </xsl:text>
      <a href="solr/select?q=link:{/mycoreobject/@ID}&amp;sort=year+desc">
        <xsl:value-of select="count(child)" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="i18n:translate('ubo.relatedItem.host.contains.publications')"/>
      </a>
    </span>
  </div>
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
      
      <a style="float:right" class="roundedButton">
        <xsl:attribute name="href">
          <xsl:text>MCRBasketServlet?type=bibentries&amp;action=add&amp;resolve=true</xsl:text>
          <xsl:text>&amp;id=</xsl:text><xsl:value-of select="$myID" />
          <xsl:text>&amp;uri=mcrobject:</xsl:text><xsl:value-of select="$myID" />
          <xsl:for-each select="$duplicates3">
            <xsl:text>&amp;id=</xsl:text><xsl:value-of select="." />
            <xsl:text>&amp;uri=mcrobject:</xsl:text><xsl:value-of select="." />
          </xsl:for-each>
        </xsl:attribute>
        <xsl:value-of select="i18n:translate('button.basketAdd')" />
      </a>
      
      <ul>
        <xsl:for-each select="$duplicates3">
          <li>
            <a href="DozBibEntryServlet?id={.}">
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
    <xsl:when test="$step='ask.publications'">
      <xsl:call-template name="ask.publications" />
    </xsl:when>
    <xsl:when test="$step='merged.publications'">
      <xsl:call-template name="merged.publications" />
    </xsl:when>
    <xsl:when test="$step='ask.hosts'">
      <xsl:call-template name="ask.hosts" />
    </xsl:when>
    <xsl:when test="$step='merged.hosts'">
      <xsl:call-template name="merged.hosts" />
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

<xsl:template name="ask.publications">
  <div style="border:1px solid yellow; padding:1ex; margin-bottom:1ex; color:yellow; background-color:red;">
    <p>
      Wenn Sie die Publikationen im Korb zusammenführen, 
      werden die bibliographischen Daten zu einem neuen Eintrag zusammengefasst 
      und die ursprünglichen, alten Einträge gelöscht. 
      Ergebnis wäre dieser Eintrag hier.
    </p>
    <p>
      <strong>      
        Wollen Sie die Publikationen im Korb wirklich zusammenführen?
      </strong>
    </p>
    <input type="button" class="editorButton" name="merge" value="Zusammenführen" 
      onclick="self.location.href='BasketPubMerger?commit=true&amp;target=publications'" />
    <input type="button" class="editorButton" name="cancel" value="{i18n:translate('button.cancelNo')}" 
      onclick="self.location.href='MCRBasketServlet?type=bibentries&amp;action=show'" />
  </div>
</xsl:template>

<xsl:template name="ask.hosts">
  <div style="border:1px solid yellow; padding:1ex; margin-bottom:1ex; color:yellow; background-color:red;">
    <p>
      Wenn Sie die Publikationen im Korb zusammenhosten, 
      werden die Überordnungen jeder dieser Publikationen extrahiert,
      deren bibliographische Daten zu einem neuen Eintrag zusammengefasst 
      und die Publikationen im Korb mit diesem neuen Eintrag verlinkt.  
      Ergebnis wäre dieser Eintrag hier als gemeinsame Überordnung aller Publikationen im Korb.
    </p>
    <p>
      <strong>      
        Wollen Sie die Publikationen im Korb wirklich zusammenhosten?
      </strong>
    </p>
    <input type="button" class="editorButton" name="merge" value="Zusammenhosten" 
      onclick="self.location.href='BasketPubMerger?commit=true&amp;target=hosts'" />
    <input type="button" class="editorButton" name="cancel" value="{i18n:translate('button.cancelNo')}" 
      onclick="self.location.href='MCRBasketServlet?type=bibentries&amp;action=show'" />
  </div>
</xsl:template>

<xsl:template name="merged.publications">
  <div style="border:1px solid yellow; padding:1ex; margin-bottom:1ex; color:yellow; background-color:red;">
    <p>
      Alle Publikationen im Korb wurden zu diesem Eintrag zusammengeführt. 
      Die anderen Einträge wurden gelöscht. Der Korb wurde geleert.
    </p>
  </div>
</xsl:template>

<xsl:template name="merged.hosts">
  <div style="border:1px solid yellow; padding:1ex; margin-bottom:1ex; color:yellow; background-color:red;">
    <p>
      Alle Publikationen im Korb wurden mit dieser Überordnung verknüpft. 
      Andere evtl. bereits vorhandene Überordnungen wurden gelöscht.
    </p>
  </div>
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

<xsl:template name="altmetrics">
  <xsl:if test="mods:identifier[contains('doi urn isbn pubmed',@type)]">
    <script type='text/javascript' src='https://d1bxh8uas1mnw7.cloudfront.net/assets/embed.js' />
    <div class="altmetric-embed" data-badge-type="donut" data-badge-popover="left" data-hide-no-mentions="true" style="float:right; display:inline-block; margin:0 2ex 2ex 2ex;">
      <xsl:choose>
        <xsl:when test="mods:identifier[@type='doi']">
          <xsl:attribute name="data-doi">
            <xsl:value-of select="mods:identifier[@type='doi']" />
          </xsl:attribute> 
        </xsl:when>
        <xsl:when test="mods:identifier[@type='urn']">
          <xsl:attribute name="data-uri">
            <xsl:value-of select="mods:identifier[@type='urn']" />
          </xsl:attribute> 
        </xsl:when>
        <xsl:when test="mods:identifier[@type='isbn']">
          <xsl:attribute name="data-isbn">
            <xsl:value-of select="mods:identifier[@type='isbn']" />
          </xsl:attribute> 
        </xsl:when>
        <xsl:when test="mods:identifier[@type='pubmed']">
          <xsl:attribute name="data-pmid">
            <xsl:value-of select="mods:identifier[@type='pubmed']" />
          </xsl:attribute> 
        </xsl:when>
      </xsl:choose>
    </div>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
