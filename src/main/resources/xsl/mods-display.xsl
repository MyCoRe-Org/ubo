<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:check="xalan://unidue.ubo.AccessControl" 
  xmlns:encoder="xalan://java.net.URLEncoder" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:mcr="http://www.mycore.org/"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan xlink i18n encoder mcr check">
  
  <xsl:include href="shelfmark-normalization.xsl" />
  <xsl:include href="output-category.xsl" />
  
  <xsl:param name="step" />
  <xsl:param name="RequestURL" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="UBO.LSF.Link" />
  <xsl:param name="CurrentLang" />

  <!-- ============ Katalogsuche Basis-URLs ============ -->
  <xsl:variable name="primo.search">
    <xsl:text>https://primo.ub.uni-due.de/primo-explore/search?tab=localude&amp;search_scope=LocalUDE</xsl:text>
    <xsl:text>&amp;sortby=rank&amp;vid=UDE_NUI&amp;lang=de_DE&amp;mode=advanced&amp;offset=0&amp;query=</xsl:text>
  </xsl:variable>
  
  <!-- ============ Fächerliste (subject) laden ============ -->
  <xsl:variable name="subjects" select="document('resource:fachreferate.xml')/fachreferate" />
  
  <!-- ============ Ausgabe Publikationsart ============ -->
  
  <xsl:template name="pubtype">
    <span class="label-info">
      <xsl:apply-templates select="mods:genre[@type='intern']" />
      <xsl:for-each select="mods:relatedItem[@type='host']/mods:genre[@type='intern']">
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
  
  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="label-info">
    <span class="label-info">
      <xsl:call-template name="output.category">
        <xsl:with-param name="classID" select="'ORIGIN'" />
        <xsl:with-param name="categID" select="substring-after(@valueURI,'#')" />
      </xsl:call-template>
    </span>
  </xsl:template>
  
  <!-- ============ Ausgabe Open Access ============ -->
  
  <xsl:template name="label-oa">
    <xsl:choose>
      <xsl:when test="mods:classification[contains(@authorityURI,'oa')]">
        <xsl:apply-templates select="mods:classification[contains(@authorityURI,'oa')]" mode="label-info" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:classification[contains(@authorityURI,'oa')]" mode="label-info" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'oa')]" mode="label-info">
    <xsl:variable name="category" select="$oa//category[@ID=substring-after(current()/@valueURI,'#')]" />
    <span class="label-info" style="background-color:{$category/label[lang('x-color')]/@text}">
      <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
    </span>
  </xsl:template>
  
  <xsl:template match="mods:classification[contains(@authorityURI,'oa')]" mode="details">
    <div class="grid_3 label">Open Access?</div>
    <div class="grid_9">
      <xsl:variable name="category" select="$oa//category[@ID=substring-after(current()/@valueURI,'#')]" />
      <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Ausgabe Jahr ========== -->
  
  <xsl:template name="label-year">
    <xsl:for-each select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[not(@type='host')])][1]">
      <span class="label-info">
        <xsl:value-of select="text()" />
      </span>
    </xsl:for-each>
  </xsl:template>
  
  <!-- ========== ORCID status and publish button ========== -->
  
  <xsl:template name="orcid-status">
    <div class="orcid-status" data-id="{ancestor::mycoreobject/@ID}" />
  </xsl:template>
  
  <xsl:template name="orcid-publish">
    <div class="orcid-publish" data-id="{ancestor::mycoreobject/@ID}" />
  </xsl:template>

  <!-- ========== URI bauen, um Dubletten zu finden ========== -->
  
  <xsl:template name="buildFindDuplicatesURI">
    <xsl:text>solr:fl=id&amp;rows=999&amp;q=(</xsl:text>
    <xsl:for-each select="dedup">
      <xsl:text>dedup:</xsl:text>
      <xsl:value-of select="encoder:encode(@key,'UTF-8')" xmlns:encoder="xalan://java.net.URLEncoder" />
      <xsl:if test="position() != last()">
        <xsl:text>+OR+</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <!-- ========== Zitierform ========== -->
  <xsl:template match="mods:mods|mods:relatedItem" mode="cite">
    <xsl:param name="mode">plain</xsl:param> <!-- plain: Als Fließtext formatieren, sonst mit <div>'s -->

    <xsl:apply-templates select="." mode="cite.title.name">
      <xsl:with-param name="mode" select="$mode" />
    </xsl:apply-templates>    
    <xsl:if test="not(mods:genre='journal')">
      <xsl:call-template name="output.line">
        <xsl:with-param name="selected" select="mods:name[@type='conference']" />
        <xsl:with-param name="before"> - </xsl:with-param>
        <xsl:with-param name="mode" select="$mode" />
        <xsl:with-param name="class" select="'conference'" />
      </xsl:call-template>
      <xsl:if test="not(mods:relatedItem[@type='host'])">
        <xsl:call-template name="output.line">
          <xsl:with-param name="selected" select="mods:originInfo" />
          <xsl:with-param name="before"> - </xsl:with-param>
          <xsl:with-param name="mode" select="$mode" />
          <xsl:with-param name="class" select="'origin'" />
        </xsl:call-template>
      </xsl:if>
      <xsl:call-template name="output.line"> 
        <xsl:with-param name="selected" select="mods:relatedItem[@type='series']" />
        <xsl:with-param name="before"> - </xsl:with-param>
        <xsl:with-param name="mode" select="$mode" />
        <xsl:with-param name="class" select="'in.series'" />
      </xsl:call-template>
      <xsl:call-template name="output.line">
        <xsl:with-param name="selected" select="mods:relatedItem[@type='host']" />
        <xsl:with-param name="before"> - </xsl:with-param>
        <xsl:with-param name="mode" select="$mode" />
        <xsl:with-param name="class" select="'in.host'" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- for typical publications: "Name: Title" -->
  <xsl:template match="mods:mods|mods:relatedItem" mode="cite.title.name">
    <xsl:param name="mode">plain</xsl:param> 
    
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm][contains($creator.roles,mods:role/mods:roleTerm[@type='code'])]" />
      <xsl:with-param name="after" select="': '" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'authors'" />
    </xsl:call-template>
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm='edt']" />
      <xsl:with-param name="after" select="concat(' ',i18n:translate('ubo.editors.abbreviated'),': ')" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'editors'" />
    </xsl:call-template>
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:titleInfo[1]" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'title'" />
    </xsl:call-template>
  </xsl:template>

  <!-- for collections: "Title / Name (Edt.)" -->
  <xsl:template match="mods:relatedItem[mods:name[mods:role/mods:roleTerm='edt']]" mode="cite.title.name">
    <xsl:param name="mode">plain</xsl:param> 
    
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm][contains($creator.roles,mods:role/mods:roleTerm[@type='code'])]" />
      <xsl:with-param name="after" select="': '" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'authors'" />
    </xsl:call-template>
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:titleInfo[1]" />
      <xsl:with-param name="after" select="' / '" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'title'" />
    </xsl:call-template>
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm='edt']" />
      <xsl:with-param name="after" select="concat(' ',i18n:translate('ubo.editors.abbreviated'),'. ')" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'editors'" />
    </xsl:call-template>
  </xsl:template>

  <!-- ========== Ausgabe einer Zeile in Zitierform ========== -->
  <xsl:template name="output.line">
    <xsl:param name="selected" />
    <xsl:param name="before" />
    <xsl:param name="after" />
    <xsl:param name="mode" />
    <xsl:param name="class" />

    <xsl:if test="count($selected) &gt; 0">
      <xsl:choose>
        <xsl:when test="$mode='plain'">
          <xsl:value-of select="$before" />
          <xsl:apply-templates select="$selected" mode="brief" />
          <xsl:value-of select="$after" />
        </xsl:when>
        <xsl:otherwise>
          <div class="{$class}">
            <xsl:apply-templates select="$selected" mode="brief" />
            <xsl:value-of select="$after" />
          </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- ========== Personennamen als Liste ========== -->
  <xsl:template match="mods:name" mode="brief">
    <xsl:apply-templates select="." />
    <xsl:if test="position() != last()"> <!-- et al ? -->
      <xsl:text>; </xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- ========== Serie(n) ========== -->
  <xsl:template match="mods:relatedItem[@type='series']" mode="brief">
    <xsl:apply-templates select="." />
    <xsl:if test="position() != last()">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>
  
  <!-- ========== Überordnung (In:) ========== -->
  <xsl:template match="mods:relatedItem[@type='host']" mode="brief">
    <xsl:text>In: </xsl:text>
    <xsl:apply-templates select="." mode="cite" />

    <!-- journal article without volume: display year directly behind title, otherwise later behind volume number -->
    <xsl:if test="(mods:genre[@type='intern']='journal') and not(mods:part/mods:detail[@type='volume']/mods:number)">
      <xsl:for-each select="ancestor::mods:mods/descendant::mods:dateIssued[1]">
        <xsl:value-of select="concat(' (',.,')')" /> 
      </xsl:for-each>
    </xsl:if>
    
    <xsl:if test="mods:part">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="mods:part" />
  </xsl:template>
  
  <!-- ========== Zitierform: alle anderen ========== -->
  <xsl:template match="mods:*" mode="brief">
    <xsl:apply-templates select="." />
  </xsl:template>

  <!-- ========== Personennamen gruppiert je Rolle ========== -->
  <xsl:template match="mods:name[@type='personal']" mode="details">
    <xsl:variable name="role" select="mods:role/mods:roleTerm[@type='code']" />
    <xsl:variable name="list" select="../mods:name[mods:role/mods:roleTerm[@type='code']=$role]" />
    <xsl:if test="count($list[1]|.)=1">
      <div class="grid_3 label">
        <xsl:apply-templates select="$role" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="grid_9">
        <xsl:for-each select="$list">
          <span class="personalName">
            <xsl:if test="mods:affiliation and check:currentUserIsAdmin()">
              <xsl:attribute name="title">
                <xsl:apply-templates select="mods:affiliation" mode="details" />
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." />
            <xsl:apply-templates select="mods:nameIdentifier[not(@type='orcid')]" />
            <xsl:apply-templates select="mods:nameIdentifier[@type='orcid']" />
            <xsl:if test="position() != last()">
              <xsl:text>; </xsl:text>
            </xsl:if>
          </span>
        </xsl:for-each>
      </div>
      <div class="clear" />
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="mods:affiliation" mode="details" >
    <xsl:value-of select="text()" />
    <xsl:if test="position() != last()"> / </xsl:if>
  </xsl:template>
  
  <xsl:param name="UBO.LSF.Link" />
  
  <xsl:template match="mods:nameIdentifier[@type='lsf']">
    <span class="nameIdentifier" title="LSF ID: {.}">
      <a href="{$UBO.LSF.Link}{.}">LSF</a>
    </span>
  </xsl:template>
  
  <xsl:param name="MCR.ORCID.LinkURL" />
  
  <xsl:template match="mods:nameIdentifier[@type='orcid']">
    <xsl:variable name="url" select="concat($MCR.ORCID.LinkURL,text())" />
    <a href="{$url}">
      <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
      <xsl:value-of select="$url" />
    </a>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='researcherid']">
    <span class="nameIdentifier" title="ResearcherID: {.}">
      <a href="http://www.researcherid.com/rid/{.}">ResearcherID</a>
    </span>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='gnd']">
    <span class="nameIdentifier" title="GND: {.}">
      <a href="http://d-nb.info/gnd/{.}">GND</a>
    </span>
  </xsl:template>

  <xsl:param name="UBO.Scopus.Author.Link" />
  
  <xsl:template match="mods:nameIdentifier[@type='scopus']">
    <span class="nameIdentifier" title="SCOPUS Author ID: {.}">
      <a href="{$UBO.Scopus.Author.Link}{.}">SCOPUS</a>
    </span>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier">
    <span class="nameIdentifier" title="{@type}: {.}">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <!-- ========== Konferenz ========== -->
  <xsl:template match="mods:name[@type='conference']" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.conference')" />
    </div>
    <div class="grid_9">
      <xsl:value-of select="mods:namePart" />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Titel mit Typ und Sprache ========== -->
  <xsl:template match="mods:titleInfo" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.title.type')" />
      <xsl:apply-templates select="@xml:lang" />
      <xsl:apply-templates select="@type" />
      <xsl:text>:</xsl:text>
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>
  
   <!-- ========== Erster Titel der Überordnung/Serie in Detailansicht, Tabelle ========== -->
  <xsl:template match="mods:relatedItem/mods:titleInfo[1]" mode="details" priority="1">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate(concat('ubo.relatedItem.',../@type))" />
    </div>
    <div class="grid_9">
      <xsl:choose>
        <xsl:when test="../@xlink:href">
          <a href="{$ServletsBaseURL}DozBibEntryServlet?id={../@xlink:href}">
            <xsl:apply-templates select="." />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." />
        </xsl:otherwise>
      </xsl:choose>
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Auflage ========== -->
  <xsl:template match="mods:edition" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.edition')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Erscheinungsort ========== -->
  <xsl:template match="mods:place" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.place')" />
      <xsl:text>:</xsl:text>
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Land bei Patent ========== -->
  <xsl:template match="mods:place[../../mods:genre='patent']" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.place.country')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Verlag ========== -->
  <xsl:template match="mods:publisher" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.publisher')" />
      <xsl:text>:</xsl:text>
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Sender bei Audio/Video ========== -->
  <xsl:template match="mods:publisher[(../../mods:genre='audio') or (../../mods:genre='video') or (../../mods:genre='broadcasting')]" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.publisher.station')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Erscheinungsjahr/datum ========== -->
  <xsl:template match="mods:dateIssued" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate(concat('ubo.date.issued.',string-length(.)))" />
      <xsl:text>:</xsl:text>
    </div>
    <div class="grid_9">
      <xsl:value-of select="text()" />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Sendedatum ========== -->
  <xsl:template match="mods:dateIssued[(../../mods:genre='audio') or (../../mods:genre='video') or (../../mods:genre='broadcasting')]" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.date.broadcasted')" />
    </div>
    <div class="grid_9">
      <xsl:value-of select="text()" />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Umfang ========== -->
  <xsl:template match="mods:physicalDescription/mods:extent" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.extent')" />
    </div>
    <div class="grid_9">
      <xsl:value-of select="text()" />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Identifier mit Typ ========== -->
  <xsl:template match="mods:identifier" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate(concat('ubo.identifier.',@type))" />:
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Link mit Typ ========== -->
  <xsl:template match="mods:location/mods:url" mode="details">
    <div class="grid_3 label">
      <xsl:choose>
        <xsl:when test="@access">
          <xsl:value-of select="i18n:translate(concat('ubo.link.',translate(@access,' ','_')))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('ubo.link')" />
        </xsl:otherwise>
      </xsl:choose>
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Genre ========== -->
  <xsl:template name="genres">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.genre')" />:
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="mods:genre" mode="details" />
    </div>
    <div class="clear" />
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern']" mode="details">
    <xsl:apply-templates select="." />
    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />
  <xsl:variable name="oa"     select="document('classification:metadata:-1:children:oa')/mycoreclass/categories" />

  <xsl:template match="mods:genre[@type='intern']">
    <xsl:value-of select="$genres//category[@ID=current()]/label[lang($CurrentLang)]/@text" />
  </xsl:template>

  <!-- ========== Notiz, Kommentar ========== -->
  <xsl:template match="mods:note" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.note')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Sprache der Publikation ========== -->
  <xsl:template match="mods:language" mode="details">
    <div class="grid_3 label">
       <xsl:value-of select="i18n:translate('ubo.language')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="mods:languageTerm" />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== Signatur ========== -->
  <xsl:template match="mods:location/mods:shelfLocator" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.shelfmark')" />
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>
  
  <!-- ========== Verweise/Überordnung ========== -->
  <xsl:template match="mods:relatedItem" mode="details">
    <div class="ubo_related_details">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </xsl:template>

  <!-- ========== part ========== -->

  <xsl:template match="mods:part" mode="details">
    <div class="grid_3 label">in:</div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <!-- ========== details_lines ========== -->
  
  <xsl:template match="mods:mods|mods:relatedItem" mode="details_lines">
    <xsl:apply-templates select="mods:titleInfo" mode="details" />
    <xsl:apply-templates select="mods:name[@type='conference']" mode="details" />
    <xsl:apply-templates select="mods:name[@type='personal']" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:edition" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:place" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:publisher" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:dateIssued" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'oa')]" mode="details" />
    <xsl:apply-templates select="mods:part" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:dateOther" mode="details" />    
    <xsl:apply-templates select="mods:physicalDescription/mods:extent" mode="details" />
    <xsl:apply-templates select="mods:identifier" mode="details" />
    <xsl:apply-templates select="mods:location/mods:shelfLocator" mode="details" />
    <xsl:apply-templates select="mods:location/mods:url" mode="details" />
    <xsl:apply-templates select="mods:note" mode="details" />
    <xsl:apply-templates select="mods:language" mode="details" />
    <xsl:apply-templates select="mods:relatedItem" mode="details" />
    <xsl:call-template name="subject.topic" />
    <xsl:apply-templates select="mods:abstract/@xlink:href" mode="details" />
    <xsl:apply-templates select="mods:abstract[string-length(.) &gt; 0]" mode="details" />
  </xsl:template>
  
  <!-- =========== Schlagworte =========== -->
  <xsl:template name="subject.topic">
    <xsl:if test="mods:subject/mods:topic">
      <div class="grid_3 label">
        <xsl:value-of select="i18n:translate('ubo.subject.topic')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="grid_9">
        <xsl:for-each select="mods:subject[mods:topic]">
          <xsl:for-each select="mods:topic">
            <xsl:value-of select="." />
            <xsl:if test="position() != last()"> &#187; </xsl:if>
          </xsl:for-each>
          <xsl:if test="position() != last()"> ; </xsl:if>
        </xsl:for-each>
        <xsl:apply-templates select="." />
      </div>
      <div class="clear" />
    </xsl:if>
  </xsl:template>
  
  <!-- =========== Link zum Abstract ========== --> 
  <xsl:template match="mods:abstract/@xlink:href" mode="details">
    <div class="grid_3 label">
      <xsl:value-of select="i18n:translate('ubo.abstract')" />
      <xsl:apply-templates select="../@xml:lang" />
      <xsl:text>:</xsl:text>
    </div>
    <div class="grid_9">
      <xsl:apply-templates select="." />
    </div>
    <div class="clear" />
  </xsl:template>

  <xsl:template match="mods:abstract[string-length(.) &gt; 0]" mode="details">
    <div style="padding:1ex;">
      <h3>
        <xsl:value-of select="i18n:translate('ubo.abstract')" />
        <xsl:apply-templates select="@xml:lang" />
        <xsl:text>:</xsl:text>
      </h3>
      <p>
        <xsl:value-of select="text()" />
      </p>
    </div>
  </xsl:template>
  
  <xsl:param name="CurrentLang" />
  <xsl:param name="DefaultLang" />

  <!-- ========== Rollen, die als DC.Creator betrachtet werden ========== -->

  <xsl:variable name="creator.roles">cre aut tch pht prg</xsl:variable>
  
  <!-- ========== Titel ========== -->
  <xsl:template match="mods:titleInfo">
    <xsl:apply-templates select="mods:nonSort" />
    <xsl:apply-templates select="mods:title" />
    <xsl:apply-templates select="mods:subTitle" />
  </xsl:template>

  <!-- ========== Führende Artikel: Der, Die, Das ========== -->
  <xsl:template match="mods:nonSort">
    <xsl:value-of select="text()" />
    <xsl:text> </xsl:text>
  </xsl:template>

  <!-- ========== Haupttitel ========== -->
  <xsl:template match="mods:title">
    <xsl:value-of select="text()" />
  </xsl:template>

  <!-- ========== Untertitel ========== -->
  <xsl:template match="mods:subTitle">
    <xsl:variable name="lastCharOfTitle" select="substring(../mods:title,string-length(../mods:title))" />
    <!-- Falls Titel nicht mit Satzzeichen endet, trenne Untertitel durch : -->
    <xsl:if test="translate($lastCharOfTitle,'?!.:,-;','.......') != '.'">
      <xsl:text> :</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <!-- ========== Typ des Titels: Haupttitel, abgekürzt, übersetzt, ... ========== -->
  <xsl:template match="mods:titleInfo/@type">
    <xsl:text> (</xsl:text>
    <xsl:value-of select="i18n:translate(concat('ubo.title.type.',.))" />
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- ========== Rolle einer Person oder Körperschaft ========== -->
  <xsl:template match="mods:roleTerm[@type='code' and @authority='marcrelator']">
    <xsl:variable name="uri" select="concat('classification:metadata:0:children:marcrelator:',.)" />
    <xsl:apply-templates select="document($uri)/mycoreclass/categories/category[1]" />
  </xsl:template>
  
  <xsl:template match="category">
    <xsl:choose>
      <xsl:when test="label[lang($CurrentLang)]">
        <xsl:value-of select="label[lang($CurrentLang)]/@text" />
      </xsl:when>
      <xsl:when test="label[lang($DefaultLang)]">
        <xsl:value-of select="label[lang($DefaultLang)]/@text" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="label[1]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 
  
  <!-- ========== Personenname ========== -->
  <xsl:template match="mods:name[@type='personal']">
    <xsl:apply-templates select="mods:namePart[@type='family']" />  
    <xsl:apply-templates select="mods:namePart[@type='given']" />  
  </xsl:template>
  
  <!-- ========== Nachname ============ -->
  <xsl:template match="mods:namePart[@type='family']">
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <!-- ========== Vorname ============ -->
  <xsl:template match="mods:namePart[@type='given']">
    <xsl:text>, </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <!-- ========== Konferenz ============ -->
  <xsl:template match="mods:name[@type='conference']">
    <xsl:value-of select="mods:namePart" />
  </xsl:template>

  <!-- ========== ISBN ========== -->
  <xsl:template match="mods:identifier[@type='isbn']">
    <a href="{$primo.search}isbn,contains,{translate(text(),'-','')}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== ISSN ========== -->
  <xsl:template match="mods:identifier[@type='issn']">
    <xsl:variable name="parameters" select="concat('genre=journal&amp;sid=bib:ughe&amp;pid=bibid%3DUGHE&amp;issn=',text())" />
    <a href="https://www.uni-due.de/ub/ghbsys/jop?{$parameters}" title="{i18n:translate('ubo.jop')}">
      <xsl:value-of select="text()" />
      <xsl:text> </xsl:text>
      <img style="float:none" src="https://services.dnb.de/fize-service/gvr/icon?{$parameters}" alt="{i18n:translate('ubo.jop')}" />
    </a>
  </xsl:template>

  <!-- ========== DOI ========== -->
  <xsl:param name="UBO.DOIResolver" />
  
  <xsl:template match="mods:identifier[@type='doi']">
    <a href="{$UBO.DOIResolver}{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== URN ========== -->
  <xsl:template match="mods:identifier[@type='urn']">
    <a href="https://nbn-resolving.org/{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== Handle ========== -->
  <xsl:template match="mods:identifier[@type='hdl']">
    <a href="https://hdl.handle.net/{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>
  
  <!-- ========== PubMed ID ========== -->
  <xsl:param name="UBO.PubMed.Link" />
  
  <xsl:template match="mods:identifier[@type='pubmed']">
    <a href="{$UBO.PubMed.Link}{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== Scopus ID ========== -->
  <xsl:param name="UBO.Scopus.Link" />
  
  <xsl:template match="mods:identifier[@type='scopus']">
    <a href="{$UBO.Scopus.Link}{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== IEEE ID ========== -->
  <xsl:param name="UBO.IEEE.Link" />
  
  <xsl:template match="mods:identifier[@type='ieee']">
    <a href="{$UBO.IEEE.Link}{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== Web of Science ID ========== -->
  <xsl:param name="UBO.WebOfScience.Link" />
  
  <xsl:template match="mods:identifier[@type='isi']">
    <a href="{$UBO.WebOfScience.Link}{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== DuEPublico ID ========== -->
  <xsl:template match="mods:identifier[@type='duepublico']">
    <a href="http://duepublico.uni-duisburg-essen.de/servlets/DocumentServlet?id={text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== Andere Identifier ========== -->
  <xsl:template match="mods:identifier">
    <xsl:value-of select="text()" />
  </xsl:template>

  <!-- ========== URL ========== -->
  <xsl:template match="mods:url">
    <a href="{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>
  
  <!-- ========== Notiz, Kommentar ========== -->
  <xsl:template match="mods:note">
    <xsl:value-of select="." />
  </xsl:template>

  <!-- ========== Auflage, Ort : Verlag, Jahr ========== -->
  <xsl:template match="mods:originInfo">
    <xsl:apply-templates select="mods:edition" />
    <xsl:if test="mods:edition and (mods:place or mods:publisher)">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="mods:place" />
    <xsl:if test="mods:place and mods:publisher">
      <xsl:text>: </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="mods:publisher" />
    <xsl:if test="(mods:edition or mods:place or mods:publisher) and mods:dateIssued">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="mods:dateIssued" />
  </xsl:template>

  <!-- ========== Auflage ========== -->
  <xsl:template match="mods:edition">
    <xsl:value-of select="text()" />
    <!-- Wenn Auflage nicht "Aufl." oder "Ed." und nur Ziffern enthält (Auflagennummer), ergänze "Aufl." -->    
    <xsl:if test="not(contains(translate(text(),'AaUuEeDd','@@@@@@@@'),'@')) and (string-length(translate(text(),'0123456789. ','')) = 0)">
      <xsl:if test="substring(.,string-length(.)) != '.'">
        <xsl:text>.</xsl:text>
      </xsl:if>
      <xsl:text> </xsl:text>
      <xsl:value-of select="i18n:translate('ubo.edition.out')" />
    </xsl:if>
  </xsl:template>
  
  <!-- ========== Erscheinungsort ========== -->
  <xsl:template match="mods:place">
    <xsl:value-of select="mods:placeTerm[@type='text']" />
  </xsl:template>
  
  <!-- ========== Verlag ========== -->
  <xsl:template match="mods:publisher">
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <!-- ========== Erscheinungsjahr/datum ========== -->
  <xsl:template match="mods:dateIssued">
    <xsl:value-of select="text()" />
  </xsl:template>

  <!-- ========== Signatur der UB ========== -->
  <xsl:template match="mods:shelfLocator">
    <xsl:variable name="sig">
      <xsl:apply-templates select="." mode="normalize.shelfmark" />
    </xsl:variable>
    <a href="{$primo.search}lsr11,contains,%22{$sig}%22">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <!-- ========== Band/Jahrgang, Heftnummer, Seitenangaben ========== -->
  <xsl:template match="mods:part">
    <xsl:apply-templates select="mods:detail[@type='volume']" />
    <xsl:apply-templates select="mods:detail[@type='issue']" />
    <xsl:apply-templates select="mods:detail[@type='page']" />
    <xsl:apply-templates select="mods:extent[@unit='pages']" />
  </xsl:template>
  
  <!-- ========== Band/Jahrgang ========== -->
  <xsl:template match="mods:detail[@type='volume']">
    <xsl:choose>
      <xsl:when test="../mods:detail[@type='issue']">
        <xsl:value-of select="i18n:translate('ubo.details.volume.journal')" />  
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="i18n:translate('ubo.details.volume.series')" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:value-of select="mods:number" />

    <xsl:variable name="volume.number" select="mods:number" />
    <xsl:variable name="year.issued" select="ancestor::mods:mods/descendant::mods:dateIssued[1]" />
    
    <xsl:if test="ancestor::mods:relatedItem/mods:genre[@type='intern']='journal'"> <!-- if it is a journal -->
      <xsl:if test="(string-length($year.issued) &gt; 0) and translate($year.issued,'0123456789','jjjjjjjjjj') = 'jjjj'"> <!-- and there is a year -->
        <xsl:if test="not($volume.number = $year.issued)"> <!-- and the year is not same as the volume number -->
          <xsl:text> (</xsl:text> <!-- then output "volume (year)" -->
          <xsl:value-of select="$year.issued" /> 
          <xsl:text>)</xsl:text>
        </xsl:if>  
      </xsl:if>
    </xsl:if>  
  </xsl:template>

  <!-- ========== Heftnummer ========== -->
  <xsl:template match="mods:detail[@type='issue']">
    <xsl:if test="../mods:detail[@type='volume']">, </xsl:if>
    <xsl:value-of select="i18n:translate('ubo.details.issue')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="mods:number" />
  </xsl:template>
  
  <!-- ========== Einzelne Seite ========== -->
  <xsl:template match="mods:detail[@type='page']">
    <xsl:if test="../mods:detail[not(@type='page')]">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="i18n:translate('ubo.pages.abbreviated.single')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="mods:number" />
  </xsl:template>

  <!-- ========== Seiten von-bis ========== -->
  <xsl:template match="mods:part/mods:extent[@unit='pages']">
    <xsl:if test="../mods:detail">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="mods:start|mods:end|mods:list|mods:total" />
  </xsl:template>
  
  <xsl:template match="mods:start[../mods:end]">
    <xsl:value-of select="i18n:translate('ubo.pages.abbreviated.multiple')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:start">
    <xsl:value-of select="i18n:translate('ubo.pages.abbreviated.single')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <!-- Special case of a single page with start=end, only output start page -->
  <xsl:template match="mods:end[text() = ../mods:start/text()]" />
  
  <xsl:template match="mods:end">
    <xsl:text> - </xsl:text>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:list">
    <xsl:if test="preceding-sibling::mods:*">
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:value-of select="text()" />
  </xsl:template>

  <xsl:template match="mods:total[../mods:start]">
    <xsl:text> (</xsl:text>
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="mods:total">
    <xsl:value-of select="text()" />
    <xsl:text> Seiten</xsl:text>
  </xsl:template>

  <!-- ========== Sprache eines Eintrages ========== -->  
  <xsl:template match="@xml:lang">
    <xsl:text> in </xsl:text>
    <xsl:value-of select="document(concat('language:',.))/language/label[@xml:lang=$CurrentLang]" />
  </xsl:template>

  <!-- ========== Sprache der Publikation ========== -->
  <xsl:template match="mods:languageTerm[@type='code']">
    <xsl:value-of select="document(concat('language:',.))/language/label[@xml:lang=$CurrentLang]" />
    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- ========== Link zum Abstract ========== -->
  <xsl:template match="mods:abstract/@xlink:href">
    <a href="{.}">
      <xsl:value-of select="." />
    </a>
  </xsl:template>
  
  <!-- ========== ( Serie ; Bandzählung ) ========== -->
  <xsl:template match="mods:relatedItem[@type='series']">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="mods:titleInfo[1]" />
    <xsl:if test="mods:part/mods:detail[@type='volume']">
      <xsl:text> ; </xsl:text>
      <xsl:value-of select="mods:part/mods:detail[@type='volume']/mods:number" />
    </xsl:if>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- ========== Rest ignorieren ========== -->
  <xsl:template match="*|@*|text()" />

</xsl:stylesheet>
