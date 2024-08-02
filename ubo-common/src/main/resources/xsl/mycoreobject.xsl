<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:ubo="xalan://org.mycore.ubo.DozBibEntryServlet"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xsl xalan ubo mods xlink i18n dc mcr mcrxml encoder"
>

<xsl:include href="mods-dc-meta.xsl" />
<xsl:include href="mods-highwire.xsl" />
<xsl:include href="mods-display.xsl" />
<xsl:include href="coreFunctions.xsl" />

<xsl:param name="Referer" select="concat($ServletsBaseURL,'DozBibEntryServlet?id=',/mycoreobject/@ID)" />
<xsl:param name="CurrentUserPID" />
<xsl:param name="step" />
<xsl:param name="UBO.System.ReadOnly" />
<xsl:param name="UBO.Mail.Feedback" />

<!-- ============ Bearbeitungsrechte ========== -->

<xsl:variable name="permission.admin" xmlns:check="xalan://org.mycore.ubo.AccessControl" select="check:currentUserIsAdmin()" />

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

  <xsl:if test="not(mcrxml:isCurrentUserGuestUser())">
    <script src="{$WebApplicationBaseURL}modules/orcid2/js/orcid-auth.js"/>
    <script src="{$WebApplicationBaseURL}js/mycore2orcid.js" />
  </xsl:if>
</xsl:template>

<!-- ========== Navigation ========== -->

<xsl:template name="breadcrumb">
  <ul id="breadcrumb">
    <xsl:if test="contains($Referer, concat($WebApplicationBaseURL, 'servlets/solr'))">
      <li data-href="{$Referer}">
        <xsl:value-of select="i18n:translate('result.dozbib.results')" />
      </li>
    </xsl:if>

    <li>
      <xsl:value-of select="i18n:translate('result.dozbib.entry')" />
      <xsl:text> </xsl:text>
      <xsl:value-of select="number(substring-after(/mycoreobject/@ID,'_mods_'))" />
    </li>
  </ul>
</xsl:template>

<!-- ============ Aktionen ============ -->

<xsl:template name="actions">
  <div id="buttons" class="btn-group mb-3 flex-wrap">
    <xsl:if test="$permission.admin and not($UBO.System.ReadOnly = 'true')">
      <xsl:if test="(string-length($step) = 0) or contains($step,'merged')">
        <a class="action btn btn-sm btn-outline-primary mb-1"
           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-publication.xed&amp;id={/mycoreobject/@ID}">
          <xsl:value-of select="i18n:translate('button.edit')"/>
        </a>
        <a class="action btn btn-sm btn-outline-primary mb-1"
           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-admin.xed&amp;id={/mycoreobject/@ID}">Admin
        </a>
        <a class="action btn btn-sm btn-outline-primary mb-1"
           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-mods.xed&amp;id={/mycoreobject/@ID}">MODS
        </a>
      </xsl:if>
      <xsl:if test="string-length($step) = 0">
        <xsl:if test="not(/mycoreobject/structure/children/child)">
          <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.step=ask.delete">
            <xsl:value-of select="i18n:translate('button.delete')" />
          </a>
        </xsl:if>
        <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.Style=structure">
          <xsl:value-of select="i18n:translate('button.structure')"/>
        </a>
      </xsl:if>
    </xsl:if>
    <xsl:if xmlns:basket="xalan://org.mycore.ubo.basket.BasketUtils" test="basket:hasSpace() and not(basket:contains(string(/mycoreobject/@ID)))">
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}MCRBasketServlet?type=objects&amp;action=add&amp;resolve=true&amp;id={/mycoreobject/@ID}&amp;uri=mcrobject:{/mycoreobject/@ID}">
        <xsl:value-of select="i18n:translate('button.basketAdd')" />
      </a>
    </xsl:if>
    <xsl:if test="$step='confirm.submitted'">
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$WebApplicationBaseURL}servlets/DozBibEntryServlet?mode=show&amp;id={/mycoreobject/@ID}">
        <xsl:value-of select="i18n:translate('button.preview')"/>
      </a>
    </xsl:if>
    <xsl:if test="(string-length($step) = 0) and not($permission.admin)">
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.xml?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=mods">MODS</a>
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.bib?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=bibtex">BibTeX</a>
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.enl?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=endnote">EndNote</a>
      <a class="action btn btn-sm btn-outline-primary mb-1" href="{$ServletsBaseURL}MCRExportServlet/{/mycoreobject/@ID}.ris?root=export&amp;uri=mcrobject:{/mycoreobject/@ID}&amp;transformer=ris">RIS</a>
    </xsl:if>
    <!-- Feedback Button -->
    <xsl:if test="$UBO.Mail.Feedback">
      <a class="action btn btn-sm btn-outline-primary mb-1 ubo-btn-feedback">
        <xsl:call-template name="feedback.href" />
        Feedback
      </a>
    </xsl:if>
  </div>
</xsl:template>

  <xsl:template name="feedback.href">
    <xsl:attribute name="href">
      <xsl:variable name="title">
        <xsl:call-template name="page.title" />
      </xsl:variable>
      <xsl:variable name="title.abbrev">
        <xsl:choose>
          <xsl:when test="string-length($title) &gt; 70">
            <xsl:value-of select="concat(substring($title, 0, 65), '[...]')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$title" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="concat('mailto:', $UBO.Mail.Feedback, '?subject=[Feedback]: ', $title.abbrev, '&amp;body=', $title, '%0D%0AURL: ', encoder:encode($RequestURL))" />
    </xsl:attribute>
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
      <xsl:call-template name="breadcrumb" />
      <xsl:call-template name="actions" />
      <xsl:apply-templates select="mycoreobject" />
    </body>
  </html>
</xsl:template>

<!-- ============ Rechte Seite: Inhalte ============ -->

<xsl:template match="mycoreobject">
  <script type="text/javascript" src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js"/>

  <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
    <xsl:call-template name="steps.and.actions" />
    <div class="section bibentry card">
      <div class="card-body">
        <xsl:apply-templates select="." mode="cite">
          <xsl:with-param name="mode">divs</xsl:with-param>
        </xsl:apply-templates>
      </div>
    </div>
    <div class="section row m-1">
      <div class="col pl-0">
        <div class="row">
          <div class="col">
            <xsl:call-template name="label-year" />
            <xsl:call-template name="pubtype" />
            <xsl:call-template name="label-oa" />
            <xsl:call-template name="orcid-status" />
          </div>
        </div>
        <div class="row">
          <div class="col">
            <xsl:apply-templates select="mods:classification[contains(@authorityURI,'fachreferate')]" mode="label-info" />
            <xsl:call-template name="label-info-destatis"/>
            <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="label-info" />
          </div>
        </div>
        <xsl:if test="mods:extension/tag">
          <div class="row">
            <div class="col">
              <xsl:apply-templates select="mods:extension/tag" />
            </div>
          </div>
        </xsl:if>
        <div class="row">
          <div class="col">
            <xsl:apply-templates select="/mycoreobject/service/servflags/servflag[@type='status']" />
            <xsl:call-template name="printRelatedItem" />
            <xsl:if test="$permission.admin and mods:extension[dedup]">
              <xsl:call-template name="linkToDuplicates" />
            </xsl:if>
          </div>
        </div>
      </div>
      <div class="col align-self-center pr-0">
        <xsl:call-template name="altmetrics" />
      </div>
    </div>

    <div class="section">
      <div class="ubo_details card">
        <div class="card-body">
          <xsl:apply-templates select="." mode="details_lines" />
        </div>
      </div>
    </div>

  </xsl:for-each>
</xsl:template>

<xsl:template name="printRelatedItem">

  <xsl:variable name="linkURI">
    <xsl:text>solr:fl=id&amp;rows=999&amp;</xsl:text>
    <xsl:value-of select="concat('q=link%3A',/mycoreobject/@ID)" />
  </xsl:variable>
  <xsl:variable name="numlinks" select="count(document($linkURI)/response/result[@name='response']/doc)" />

  <xsl:if test="$numlinks &gt; 0">
  <span class="badge badge-alternative mr-1">
    <a href="solr/select?q=link:{/mycoreobject/@ID}&amp;sort=year+desc">
      <xsl:value-of select="concat(i18n:translate('ubo.relatedItem.link'),': ', $numlinks, ' ', i18n:translate('ubo.relatedItem.host.contains.publications'))"/>
    </a>
  </span>
  </xsl:if>

</xsl:template>

<xsl:variable name="quotes">"</xsl:variable>

<xsl:template match="mods:extension/tag">
  <span class="mycore-tag">
    <a class="badge badge-primary" href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+tag:{encoder:encode(concat($quotes,text(),$quotes),'UTF-8')}">
      <xsl:value-of select="text()" />
    </a>
  </span>
</xsl:template>

<!-- ============ Dubletten suchen ============ -->

<xsl:template name="linkToDuplicates">

  <xsl:variable name="duplicatesURI">
    <xsl:text>notnull:</xsl:text>
    <xsl:for-each select="mods:extension[dedup][1]">
      <xsl:call-template name="buildFindDuplicatesURI" />
    </xsl:for-each>
    <xsl:value-of select="concat('+AND+-id%3A',/mycoreobject/@ID)" />
  </xsl:variable>

  <xsl:variable name="numDuplicates" select="count(document($duplicatesURI)/response/result[@name='response']/doc)" />
  
  <xsl:if test="$numDuplicates &gt; 0">
    <span class="badge badge-alternative ml-1 mr-1">
      <a href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.Style=structure">
        <xsl:variable name="extro">
          <xsl:choose>
            <xsl:when test="$numDuplicates = 1">
              <xsl:value-of select="i18n:translate('ubo.badge.duplicate.singular')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="i18n:translate('ubo.badge.duplicate.plural', $numDuplicates)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="concat(i18n:translate('ubo.badge.duplicate.start'), ' ', $extro, '.')"/>
      </a>
    </span>
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
  <div class="section card">
    <div class="card-header">
      <xsl:value-of select="i18n:translate('result.dozbib.now')"/>
    </div>
    <div class="card-body">
      <ul>
        <li>
          <xsl:value-of select="i18n:translate('result.dozbib.nowSaved')"/>
          <xsl:text> </xsl:text>
          <strong><xsl:value-of select="i18n:translate('result.dozbib.nowSavedStrong')"/></strong>
        </li>
        <li>
          <xsl:value-of select="i18n:translate('result.dozbib.nowCheck')"/>
        </li>
        <li>
          <xsl:value-of select="i18n:translate('result.dozbib.nowAccess')"/>
        </li>
      </ul>

    <!-- if additional text exist -->
    <xsl:if test="i18n:translate('result.dozbib.nowAdditional') != 'false'">
      <p>
        <xsl:value-of select="i18n:translate('result.dozbib.nowAdditional')" disable-output-escaping="yes" />
      </p>
    </xsl:if>

      <p>
        <a href="{$WebApplicationBaseURL}newPublication.xed">
          <xsl:value-of select="i18n:translate('result.dozbib.registerMore')"/>
        </a>
      </p>
    </div>
  </div>
</xsl:template>

<xsl:template name="ask.delete">
  <p>
    <strong>
      <xsl:value-of select="i18n:translate('result.dozbib.delete')"/>
    </strong>
  </p>

  <div class="btn-group mb-3">
    <input type="button" class="action btn btn-sm btn-outline-danger" name="delete" value="{i18n:translate('button.deleteYes')}"
      onclick="self.location.href='{$ServletsBaseURL}DozBibEntryServlet?mode=delete&amp;id={/mycoreobject/@ID}'" />
    <input type="button" class="action btn btn-sm btn-outline-primary" name="cancel" value="{i18n:translate('button.cancelNo')}"
      onclick="self.location.href='{$Referer}'" />
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
    <script type='text/javascript' src='{$WebApplicationBaseURL}js/altmetrics.js' />
    <div class="altmetric-embed float-right" data-badge-type="donut" data-badge-popover="left" data-hide-no-mentions="true">
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
