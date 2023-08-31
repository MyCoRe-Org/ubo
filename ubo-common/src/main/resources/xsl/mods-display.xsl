<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:cerif="https://www.openaire.eu/cerif-profile/1.1/"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:check="xalan://org.mycore.ubo.AccessControl"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="xsl xalan xlink i18n encoder mcr mcrxsl check cerif">

  <xsl:include href="shelfmark-normalization.xsl" />
  <xsl:include href="output-category.xsl" />

  <xsl:param name="step" />
  <xsl:param name="RequestURL" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="UBO.LSF.Link" />
  <xsl:param name="UBO.JOP.Parameters" />
  <xsl:param name="UBO.JOP.URL" />
  <xsl:param name="UBO.URI.gbv.de.ppn.redirect" />
  <xsl:param name="UBO.CreatorRoles" select="'cre aut tch pht prg'" />   <!-- Rollen, die als DC.Creator betrachtet werden -->

  <!-- Expect one more author to be displayed as the last author is always getting displayed -->
  <xsl:param name="UBO.Initially.Visible.Authors" select="14" />

<!-- ============ Katalogsuche Basis-URLs ============ -->
  <xsl:param name="UBO.Primo.Search.Link" />
  <xsl:param name="UBO.ISBN.Search.Link" />
  <xsl:param name="MCR.ORCID2.LinkURL"/>

  <xsl:variable name="genres"                select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />
  <xsl:variable name="origin"                select="document('classification:metadata:-1:children:ORIGIN')/mycoreclass/categories" />
  <xsl:variable name="oa"                    select="document('classification:metadata:-1:children:oa')/mycoreclass/categories" />
  <xsl:variable name="accessrights"          select="document('notnull:classification:metadata:-1:children:accessrights')/mycoreclass/categories" />
  <xsl:variable name="peerreviewed"          select="document('notnull:classification:metadata:-1:children:peerreviewed')/mycoreclass/categories" />
  <xsl:variable name="partner"               select="document('notnull:classification:metadata:-1:children:partner')/mycoreclass/categories" />
  <xsl:variable name="publication_category"  select="document('notnull:classification:metadata:-1:children:category')/mycoreclass/categories" />
  <xsl:variable name="partOf"                select="document('notnull:classification:metadata:-1:children:partOf')/mycoreclass/categories" />

  <xsl:variable name="fq">
    <xsl:if test="not(check:currentUserIsAdmin())">
      <xsl:value-of select="'+status:&quot;confirmed&quot; '"/>
    </xsl:if>
  </xsl:variable>

  <!-- ============ Ausgabe Publikationsart ============ -->

  <xsl:template name="pubtype">
    <span class="label-info badge badge-secondary mr-1">
      <xsl:apply-templates select="mods:genre[@type='intern']" />
      <xsl:for-each select="mods:relatedItem[@type='host']/mods:genre[@type='intern']">
        <xsl:text> in </xsl:text>
        <xsl:apply-templates select="." />
      </xsl:for-each>
    </span>
  </xsl:template>

  <!-- ============ Ausgabe Fach ============ -->

  <xsl:template match="mods:mods/mods:classification[contains(@authorityURI,'fachreferate')]" mode="label-info">
    <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer badge-class-fachreferate" title="{i18n:translate('facets.facet.subject')}"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+subject:', substring-after(@valueURI,'#')))}')">
      <xsl:call-template name="output.category">
        <xsl:with-param name="classID" select="'fachreferate'" />
        <xsl:with-param name="categID" select="substring-after(@valueURI,'#')" />
      </xsl:call-template>
    </span>
  </xsl:template>

  <!-- ========== Ausgabe Fakultät ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="label-info">
    <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer" title="{i18n:translate('ubo.department')}"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+origin:', substring-after(@valueURI,'#')))}')">
      <xsl:call-template name="output.category">
        <xsl:with-param name="classID" select="'ORIGIN'" />
        <xsl:with-param name="categID" select="substring-after(@valueURI,'#')" />
      </xsl:call-template>
    </span>
  </xsl:template>

  <!-- Derive destatis from origin if fachreferate is not set -->
  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN') and not (../mods:classification[contains(@authorityURI,'fachreferate')])]" mode="label-info-destatis">
    <xsl:variable name="origin-value" select="substring-after(@valueURI, '#')"/>
    <xsl:variable name="destatis-attr" select="$origin//category[@ID = $origin-value]/label[@xml:lang = 'x-destatis']/@text"/>

    <xsl:if test="string-length($destatis-attr) &gt; 0">
        <xsl:variable name="destatis-categories">
          <xsl:call-template name="Tokenizer">
            <xsl:with-param name="string" select="$destatis-attr" />
          </xsl:call-template>
        </xsl:variable>

        <xsl:for-each select="xalan:nodeset($destatis-categories)/token">
          <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer badge-class-fachreferate" title="{i18n:translate('facets.facet.subject')}">
            <xsl:call-template name="output.category">
              <xsl:with-param name="classID" select="'fachreferate'" />
              <xsl:with-param name="categID" select="." />
            </xsl:call-template>
          </span>
        </xsl:for-each>
    </xsl:if>
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
    <span class="badge oa-badge oa-badge-{$category/@ID} ubo-hover-pointer mr-1"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+oa_exact:', $category/@ID))}')">
      <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
    </span>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'oa')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.oa')" /><xsl:text>:</xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$oa//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Access Rights ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'accessrights')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.accessrights')" /><xsl:text>:
      </xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$accessrights//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe PeerReviewed ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'peerreviewed')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.peerreviewed')" /><xsl:text>:
      </xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$peerreviewed//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Praxispartner ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'partner')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.partner')" /><xsl:text>:</xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$partner//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Kategorie ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'category')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.category')" /><xsl:text>:
      </xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$publication_category//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Teil der Statistik ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'partOf')]" mode="details">
    <div class="row">
      <div class="col-3"><xsl:value-of select="i18n:translate('ubo.partOf')" /><xsl:text>:</xsl:text></div>
      <div class="col-9">
        <xsl:variable name="category" select="$partOf//category[@ID=substring-after(current()/@valueURI,'#')]" />
        <xsl:value-of select="$category/label[lang($CurrentLang)]/@text"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN') and not (../mods:classification[contains(@authorityURI,'fachreferate')])]" mode="details">
    <xsl:variable name="origin-value" select="substring-after(@valueURI, '#')"/>
    <xsl:variable name="destatis-attr" select="$origin//category[@ID = $origin-value]/label[@xml:lang = 'x-destatis']/@text"/>

    <xsl:if test="string-length($destatis-attr) &gt; 0">
      <xsl:variable name="destatis-categories">
        <xsl:call-template name="Tokenizer">
          <xsl:with-param name="string" select="$destatis-attr" />
        </xsl:call-template>
      </xsl:variable>

      <xsl:for-each select="xalan:nodeset($destatis-categories)/token">
        <div class="row ubo-metadata-destatis-by-origin">
          <div class="col-3">
            <xsl:value-of select="concat(i18n:translate('ubo.destatis'), ':')" />
          </div>

          <div class="col-9">
            <xsl:call-template name="output.category">
              <xsl:with-param name="classID" select="'fachreferate'" />
              <xsl:with-param name="categID" select="." />
            </xsl:call-template>
          </div>
        </div>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- ========== Ausgabe Datenträgertyp ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'mediaType')]" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(i18n:translate('ubo.mediaType'), ':')" />
      </div>
      <div class="col-9">
        <xsl:value-of select="mcrxsl:getDisplayName('mediaType', substring-after(current()/@valueURI,'#'))"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Zweitveröffentlichung ========== -->

  <xsl:template match="mods:classification[contains(@authorityURI,'republication')]" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(i18n:translate('ubo.republication'), ':')" />
      </div>
      <div class="col-9">
        <xsl:value-of select="mcrxsl:getDisplayName('republication', substring-after(current()/@valueURI,'#'))"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Lizenz ========== -->

  <xsl:template match="mods:accessCondition[@classID='licenses']" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(i18n:translate('ubo.licenses'), ':')" />
      </div>
      <div class="col-9">
        <xsl:value-of select="mcrxsl:getDisplayName('licenses', current()/text())"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Ressourcentyp ========== -->
  <xsl:template match="mods:typeOfResource" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(i18n:translate('ubo.typeOfResource'), ':')" />
      </div>
      <div class="col-9">
        <xsl:value-of select="mcrxsl:getDisplayName('typeOfResource', current()/text())"/>
      </div>
    </div>
  </xsl:template>

  <!-- ========== Ausgabe Jahr ========== -->

  <xsl:template name="label-year">
    <xsl:choose>
      <xsl:when test="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[@type='host'])][1]">
        <xsl:for-each select="descendant-or-self::mods:dateIssued[not(ancestor::mods:relatedItem[@type='host'])][1]">
          <xsl:apply-templates select="." mode="label-year-badge"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="descendant-or-self::mods:dateIssued[(ancestor::mods:relatedItem[(@type='host')])][1]">
          <xsl:apply-templates select="." mode="label-year-badge"/>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:dateIssued" mode="label-year-badge">
    <span class="label-info badge badge-secondary mr-1 ubo-hover-pointer" title="{i18n:translate('ubo.search.year')}"
          onclick="location.assign('{$WebApplicationBaseURL}servlets/solr/select?sort=modified+desc&amp;q={encoder:encode(concat($fq, '+year:', text()))}')">
      <xsl:value-of select="text()" />
    </span>
  </xsl:template>

  <!-- ========== ORCID status and publish button ========== -->

  <xsl:template name="orcid-status">
    <div class="orcid-status" data-id="{ancestor::mycoreobject/@ID}" />
  </xsl:template>

  <xsl:template name="orcid-publish">
    <div class="orcid-publish d-inline" data-id="{ancestor::mycoreobject/@ID}" />
  </xsl:template>

  <!-- ========== URI bauen, um Dubletten zu finden ========== -->

  <xsl:template name="buildFindDuplicatesURI">
    <xsl:text>solr:fl=id&amp;rows=999&amp;q=(</xsl:text>
    <xsl:for-each select="dedup">
      <xsl:text>dedup%3A</xsl:text>
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
          <xsl:with-param name="selected" select="mods:originInfo[not(@eventType) or @eventType='publication']" />
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
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm][contains($UBO.CreatorRoles,mods:role/mods:roleTerm[@type='code'])]" />
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
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm='ivr']" />
      <xsl:with-param name="after" select="concat(' ',i18n:translate('ubo.interviewer.abbreviated'),': ')" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'interviewer'" />
    </xsl:call-template>
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:titleInfo[1]" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'title font-weight-bold'" />
    </xsl:call-template>
  </xsl:template>

  <!-- for collections: "Title / Name (Edt.)" -->
  <xsl:template match="mods:relatedItem[mods:name[mods:role/mods:roleTerm='edt']]" mode="cite.title.name">
    <xsl:param name="mode">plain</xsl:param>

    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm][contains($UBO.CreatorRoles,mods:role/mods:roleTerm[@type='code'])]" />
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
    <xsl:call-template name="output.line">
      <xsl:with-param name="selected" select="mods:name[mods:role/mods:roleTerm='ivr']" />
      <xsl:with-param name="after" select="concat(' ',i18n:translate('ubo.interviewer.abbreviated'),'. ')" />
      <xsl:with-param name="mode" select="$mode" />
      <xsl:with-param name="class" select="'interviewer'" />
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
    <xsl:choose>
      <xsl:when test="position() &lt;= $UBO.Initially.Visible.Authors">
        <xsl:apply-templates select="." />

        <xsl:if test="position() &lt;= $UBO.Initially.Visible.Authors and not(position() = last())">
          <xsl:text>; </xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:when test="position() = last() and position() &gt; $UBO.Initially.Visible.Authors">
        <xsl:apply-templates select="." />
        <xsl:text> et al</xsl:text>
      </xsl:when>
    </xsl:choose>
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
  <xsl:template match="mods:name[@type='personal' or @type='corporate']" mode="details">
    <xsl:variable name="role" select="mods:role/mods:roleTerm[@type='code']" />
    <xsl:variable name="list" select="../mods:name[mods:role/mods:roleTerm[@type='code']=$role]" />
    <xsl:if test="count($list[1]|.)=1">

      <div class="row">
        <div class="col-3">
          <xsl:apply-templates select="$role" />
          <xsl:text>:</xsl:text>
        </div>
        <div class="col-9">
          <xsl:for-each select="$list">
            <span>
              <xsl:attribute name="class">
                <xsl:choose>
                  <xsl:when test="position() &lt;= $UBO.Initially.Visible.Authors or (position() = last())">
                    <xsl:value-of select="'personalName'" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="'personalName d-none'" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>

              <xsl:if test="mods:affiliation and check:currentUserIsAdmin()">
                <xsl:attribute name="title">
                  <xsl:apply-templates select="mods:affiliation" mode="details" />
                </xsl:attribute>
              </xsl:if>

              <xsl:if test="position() &gt; 1">
                <xsl:text>; </xsl:text>
              </xsl:if>

              <xsl:apply-templates select="." />

              <xsl:choose>
                <xsl:when test="count(mods:nameIdentifier[@type='connection']) &gt;0">
                  <xsl:apply-templates select="mods:nameIdentifier[@type='connection']" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:apply-templates select="mods:nameIdentifier[@type='orcid']" />
                  <xsl:apply-templates select="mods:nameIdentifier[not(@type='orcid')]" />
                  <xsl:apply-templates select="mods:role/mods:roleTerm" mode="corresponding-author"/>
                </xsl:otherwise>
              </xsl:choose>
            </span>
          </xsl:for-each>

          <xsl:variable name="hideable-count" select="count($list) - $UBO.Initially.Visible.Authors - 1"/>
          <xsl:if test="count($list) &gt; $UBO.Initially.Visible.Authors and $hideable-count &gt; 0">
            <div class="row">
              <div class="col">
                <a href="javascript:void(0)" onclick="ModsDisplayUtils.expand(this)" data-hideable-count="{$hideable-count}">
                  <xsl:value-of select="i18n:translate('button.view.all.authors', $hideable-count)" />
                </a>
              </div>
            </div>
          </xsl:if>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:affiliation" mode="details" >
    <xsl:value-of select="text()" />
    <xsl:if test="position() != last()"> / </xsl:if>
  </xsl:template>

  <xsl:param name="UBO.LSF.Link"/>

  <xsl:template match="mods:nameIdentifier[@type='connection']">
    <xsl:variable name="userXML" select="document(concat('userconnection:', text()))"/>
    <xsl:variable name="userAttributeClassification" select="document('classification:metadata:-1:children:user_attributes')"/>
    <xsl:variable name="popId" select="generate-id()"/>
    <xsl:variable name="is-corresponding-author" select="contains(../mods:role/mods:roleTerm/@valueURI, 'author_roles#corresponding_author')"/>

    <span id="{$popId}" title="{i18n:translate('person.search.information')}">
      <xsl:attribute name="class">
        <xsl:text>ubo-person-popover ml-1 fas fa-user</xsl:text>
        <xsl:if test="$is-corresponding-author = true()">
          <xsl:text>-edit</xsl:text>
        </xsl:if>
      </xsl:attribute>
    </span>

    <div id="{$popId}-content" class="d-none">
      <dl>
        <xsl:if test="count($userXML/user/attributes/attribute) &gt; 0">
          <xsl:for-each select="$userXML/user/attributes/attribute">
            <xsl:variable name="attrName" select="@name"/>
            <xsl:variable name="classNode" select="$userAttributeClassification/.//category[@ID=$attrName]"/>
            <xsl:if test="count($classNode)&gt;0 and count($classNode/label[@xml:lang='x-display' and @text='true'])&gt;0">
              <dt>
                <xsl:value-of select="$classNode/label[lang($CurrentLang)]/@text"/>
              </dt>
              <dd>
                <xsl:choose>
                  <xsl:when test="$attrName='id_orcid'">
                    <!-- special display code for orcid -->
                    <xsl:variable name="url" select="concat($MCR.ORCID2.LinkURL,@value)" />
                    <a href="{$url}" title="ORCID iD: {@value}">
                      <xsl:value-of select="@value" />
                      <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
                    </a>
                  </xsl:when>
                  <xsl:when test="count($classNode/label[@xml:lang='x-uri'])  &gt;0">
                    <!-- display as link -->
                    <a href="{$classNode/label[@xml:lang='x-uri']/@text}{@value}" title="{$classNode/label[lang($CurrentLang)]/@text}: {@value}">
                      <xsl:value-of select="@value" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- display as text -->
                    <xsl:value-of select="@value" />
                  </xsl:otherwise>
                </xsl:choose>
              </dd>
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
      </dl>
    </div>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='lsf']">
    <span class="nameIdentifier lsf" title="LSF ID: {.}">
      <a href="{$UBO.LSF.Link}{.}">LSF</a>
    </span>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='orcid']">
    <xsl:variable name="url" select="concat($MCR.ORCID2.LinkURL,text())" />
    <a href="{$url}" title="ORCID iD: {text()}">
      <img alt="ORCID iD" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
    </a>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='researcherid']">
    <span class="nameIdentifier researcherid" title="ResearcherID: {.}">
      <a href="http://www.researcherid.com/rid/{.}">ResearcherID</a>
    </span>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='gnd']">
    <span class="nameIdentifier gnd" title="GND: {.}">
      <a href="http://d-nb.info/gnd/{.}">GND</a>
    </span>
  </xsl:template>

  <xsl:param name="UBO.Scopus.Author.Link" />

  <xsl:template match="mods:nameIdentifier[@type='scopus']">
    <span class="nameIdentifier scopus" title="SCOPUS Author ID: {.}">
      <a href="{$UBO.Scopus.Author.Link}{.}">SCOPUS</a>
    </span>
  </xsl:template>

  <xsl:param name="UBO.Local.Author.Link" />

  <xsl:template match="mods:nameIdentifier[@type='local']">
    <span class="nameIdentifier local" title="{i18n:translate('ubo.authorlink.local.title')}: {.}">
      <xsl:choose>
        <xsl:when test="string-length($UBO.Local.Author.Link) &gt; 0">
          <a href="{$UBO.Local.Author.Link}{.}"><xsl:value-of select="i18n:translate('ubo.authorlink.local.text')" /></a>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="i18n:translate('ubo.authorlink.local.text')" /></xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier">
    <xsl:variable name="badge.label">
      <xsl:choose>
        <xsl:when test="i18n:exists(concat('badge.nameIdentifier.', @type))">
          <xsl:value-of select="i18n:translate(concat('badge.nameIdentifier.', @type))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@type"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <span class="nameIdentifier genericid" title="{@type}: '{.}'">
      <a href="javascript:void(0)">
        <xsl:value-of select="$badge.label" />
      </a>
    </span>
  </xsl:template>

  <!-- ========== Konferenz ========== -->
  <xsl:template match="mods:name[@type='conference']" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.conference')" />
      </div>
      <div class="col-9">
        <xsl:value-of select="mods:namePart" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Titel mit Typ und Sprache ========== -->
  <xsl:template match="mods:titleInfo" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.title.type')" />
        <xsl:apply-templates select="@xml:lang" />
        <xsl:apply-templates select="@type" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

   <!-- ========== Erster Titel der Überordnung/Serie in Detailansicht, Tabelle ========== -->
  <xsl:template match="mods:relatedItem/mods:titleInfo[1]" mode="details" priority="1">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate(concat('ubo.relatedItem.',../@type))" />
      </div>
      <div class="col-9">
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
    </div>
  </xsl:template>

  <!-- ========== Auflage ========== -->
  <xsl:template match="mods:edition" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(i18n:translate('ubo.edition'), ':')" />
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Erscheinungsort ========== -->
  <xsl:template match="mods:place" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.place')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Land bei Patent ========== -->
  <xsl:template match="mods:place[../../mods:genre='patent']" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.place.country')" />
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Verlag ========== -->
  <xsl:template match="mods:publisher" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.publisher')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Sender bei Audio/Video ========== -->
  <xsl:template match="mods:publisher[(../../mods:genre='audio') or (../../mods:genre='video') or (../../mods:genre='broadcasting')]" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.publisher.station')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Erscheinungsjahr/datum ========== -->
  <xsl:template match="mods:dateIssued" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate(concat('ubo.date.issued.',string-length(.)))" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:value-of select="text()" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Jahr der Erstanmeldung ========== -->
  <xsl:template match="mods:originInfo[@eventType='application']/mods:dateIssued" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.patent.application')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:value-of select="text()" />
      </div>
    </div>
  </xsl:template>


  <!-- ========== Sendedatum ========== -->
  <xsl:template match="mods:dateIssued[(../../mods:genre='audio') or (../../mods:genre='video') or (../../mods:genre='broadcasting')]" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.date.broadcasted')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:value-of select="text()" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Umfang ========== -->
  <xsl:template match="mods:physicalDescription/mods:extent" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.extent')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:value-of select="text()" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Identifier mit Typ ========== -->
  <xsl:template match="mods:identifier" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:choose>
          <xsl:when test="contains(text(), 'uri.gbv.de/document')">
            <xsl:value-of select="concat(i18n:translate('ubo.identifier.ppn'), ':')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="i18n:exists(concat('ubo.identifier.', @type))">
                <xsl:value-of select="i18n:translate(concat('ubo.identifier.', @type), ':')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="title">
                  <xsl:value-of select="concat(i18n:translate('ubo.identifier'), ' (', @type, ')')"/>
                </xsl:attribute>
                <xsl:value-of select="i18n:translate('ubo.identifier')"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Link mit Typ ========== -->
  <xsl:template match="mods:location/mods:url" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:choose>
          <xsl:when test="@access">
            <xsl:value-of select="i18n:translate(concat('ubo.link.',translate(@access,' ','_')))" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="i18n:translate('ubo.link')" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Genre ========== -->
  <xsl:template name="genres">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.genre')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="mods:genre" mode="details" />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern']" mode="details">
    <xsl:apply-templates select="." />
    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>


  <xsl:template match="mods:genre[@type='intern']">
    <xsl:value-of select="$genres//category[@ID=current()]/label[lang($CurrentLang)]/@text" />
  </xsl:template>

  <!-- ========== Notiz, Kommentar ========== -->
  <xsl:template match="mods:note" mode="details">
    <xsl:if test="not(@type) or (@type and check:currentUserIsAdmin())">
      <div class="row">
        <div class="col-3">
          <xsl:value-of select="i18n:translate('ubo.note')" />
          <xsl:text>:</xsl:text>
        </div>
        <div class="col-9">
          <xsl:apply-templates select="." />
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:subject/mods:cartographics" mode="details">
    <xsl:if test="mods:scale">
      <div class="row">
        <div class="col-3">
          <xsl:value-of select="i18n:translate('ubo.scale')"/>
          <xsl:text>:</xsl:text>
        </div>
        <div class="col-9">
          <xsl:value-of select="mods:scale"/>
        </div>
      </div>
    </xsl:if>

    <xsl:if test="mods:coordinates">
      <div class="row">
        <div class="col-3">
          <xsl:value-of select="i18n:translate('ubo.coordinates')"/>
          <xsl:text>:</xsl:text>
        </div>
        <div class="col-9">
          <span title="{i18n:translate('ubo.coordinates.latitude')}">
            <xsl:value-of select="concat(mods:coordinates[1], '°')"/>
          </span>
          <xsl:text>, </xsl:text>
          <span title="{i18n:translate('ubo.coordinates.longitude')}">
            <xsl:value-of select="concat(mods:coordinates[2], '°')"/>
          </span>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- ========== Sprache der Publikation ========== -->
  <xsl:template match="mods:language" mode="details">
    <div class="row">
      <div class="col-3">
         <xsl:value-of select="i18n:translate('ubo.language')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="mods:languageTerm" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Signatur ========== -->
  <xsl:template match="mods:location/mods:shelfLocator" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.shelfmark')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="mods:extension[@displayLabel='project']/cerif:Project" mode="details" >
    <xsl:variable name="title" select="cerif:Title"/>
    <xsl:variable name="acronym" select="cerif:Acronym"/>
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.project.label')" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:value-of select="concat($title, ' (', $acronym, ')')" />
      </div>
    </div>
  </xsl:template>

  <!-- ========== Verweise/Überordnung ========== -->
  <xsl:template match="mods:relatedItem[(@type='host') or (@type='series')]" mode="details">
    <div class="ubo_related_details">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </xsl:template>

  <!-- mit @xlink:href -->
  <xsl:template match="mods:relatedItem[not(@type='host') and not(@type='series')][@xlink:href]" mode="details">
    <div class="ubo_related_details">
      <div class="row">
        <div class="col-3 label">
          <xsl:choose>
            <xsl:when test="i18n:exists(concat('ubo.relatedItem.', @type))">
              <xsl:value-of select="concat(i18n:translate(concat('ubo.relatedItem.', @type)), ':')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="title">
                <xsl:value-of select="@type"/>
              </xsl:attribute>
              <xsl:value-of select="concat(i18n:translate('ubo.relatedItem.unknown.type'), ':')"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
        <div class="col-9">
          <a href="{$ServletsBaseURL}DozBibEntryServlet?id={@xlink:href}">
            <xsl:apply-templates select="document(concat('notnull:mcrobject:',@xlink:href))//mods:mods" mode="cite" />
          </a>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- ohne @xlink:href -->
  <xsl:template match="mods:relatedItem[not(@type='host') and not(@type='series')][not(@xlink:href)]" mode="details">
    <div class="ubo_related_details">
      <div class="row">
        <div class="col-3 label">
          <xsl:choose>
            <xsl:when test="i18n:exists(concat('ubo.relatedItem.', @type))">
              <xsl:value-of select="concat(i18n:translate(concat('ubo.relatedItem.', @type)), ':')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="title">
                <xsl:value-of select="@type"/>
              </xsl:attribute>
              <xsl:value-of select="concat(i18n:translate('ubo.relatedItem.unknown.type'), ':')"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
        <div class="col-9">
          <xsl:apply-templates select="." mode="cite" />
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- ========== part ========== -->

  <xsl:template match="mods:part" mode="details">
    <div class="row">
      <div class="col-3">in:</div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <!-- ========== details_lines ========== -->

  <xsl:template match="mods:mods|mods:relatedItem" mode="details_lines">
    <xsl:apply-templates select="mods:titleInfo" mode="details" />
    <xsl:apply-templates select="mods:name[@type='conference']" mode="details" />
    <xsl:apply-templates select="mods:name[@type='personal']" mode="details" />
    <xsl:apply-templates select="mods:name[@type='corporate']" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:edition" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:place" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:publisher" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:dateIssued" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'oa')]" mode="details" />
    <xsl:apply-templates select="mods:part" mode="details" />
    <xsl:apply-templates select="mods:originInfo/mods:dateOther" mode="details" />
    <xsl:apply-templates select="mods:physicalDescription/mods:extent" mode="details" />
    <xsl:apply-templates select="mods:identifier" mode="details">
      <xsl:sort select="@type"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="mods:location/mods:shelfLocator" mode="details" />
    <xsl:apply-templates select="mods:extension[@displayLabel='project']/cerif:Project" mode="details" />
    <xsl:apply-templates select="mods:location/mods:url" mode="details" />
    <xsl:apply-templates select="mods:note" mode="details" />
    <xsl:apply-templates select="mods:subject/mods:cartographics" mode="details"/>
    <xsl:apply-templates select="mods:language" mode="details" />
    <xsl:apply-templates select="mods:relatedItem" mode="details" />
    <xsl:call-template name="subject.topic" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'mediaType')]" mode="details" />
    <xsl:apply-templates select="mods:typeOfResource" mode="details" />
    <xsl:apply-templates select="mods:accessCondition[@classID]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'republication')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'accessrights')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'peerreviewed')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'partner')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'category')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'partOf')]" mode="details" />
    <xsl:apply-templates select="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="details" />
    <xsl:apply-templates select="mods:abstract/@xlink:href" mode="details" />
    <xsl:apply-templates select="mods:abstract[string-length(.) &gt; 0]" mode="details" />
  </xsl:template>

  <!-- =========== Schlagworte =========== -->
  <xsl:template name="subject.topic">
    <xsl:if test="mods:subject/mods:topic">
      <div class="row">
        <div class="col-3">
          <xsl:value-of select="i18n:translate('ubo.subject.topic')" />
          <xsl:text>:</xsl:text>
        </div>
        <div class="col-9">
          <xsl:for-each select="mods:subject[mods:topic]">
            <xsl:for-each select="mods:topic">
              <xsl:value-of select="." />
              <xsl:if test="position() != last()"> &#187; </xsl:if>
            </xsl:for-each>
            <xsl:if test="position() != last()"> ; </xsl:if>
          </xsl:for-each>
          <xsl:apply-templates select="." />
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- =========== Link zum Abstract ========== -->
  <xsl:template match="mods:abstract/@xlink:href" mode="details">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="i18n:translate('ubo.abstract')" />
        <xsl:apply-templates select="../@xml:lang" />
        <xsl:text>:</xsl:text>
      </div>
      <div class="col-9">
        <xsl:apply-templates select="." />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="mods:abstract[string-length(.) &gt; 0]" mode="details">
    <div class="ubo-content-block ubo-abstract">
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
    <xsl:variable name="uri">
      <xsl:choose>
        <xsl:when test="../.././@type[. = 'corporate'] and mcrxsl:isCategoryID('marcrelator_corporation', .)">
          <xsl:value-of select="concat('classification:metadata:0:children:marcrelator_corporation:', .)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('classification:metadata:0:children:marcrelator:', .)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:apply-templates select="document($uri)/mycoreclass/categories/category[1]" />
  </xsl:template>

  <xsl:template match="mods:roleTerm" mode="corresponding-author">
    <xsl:if test="contains(@valueURI, 'author_roles#corresponding_author')" >
      <i class="fas fa-user-edit" title="{i18n:translate('ubo.corresponding_author')}"/>
    </xsl:if>
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

  <!-- ========== Körperschaft ============ -->
  <xsl:template match="mods:name[@type='corporate']">
    <xsl:value-of select="mods:namePart" />
  </xsl:template>

  <!-- ========== Konferenz ============ -->
  <xsl:template match="mods:name[@type='conference']">
    <xsl:value-of select="mods:namePart" />
  </xsl:template>

  <!-- ========== ISBN ========== -->
  <xsl:template match="mods:identifier[@type='isbn']">
    <xsl:choose>
      <xsl:when test="$UBO.Primo.Search.Link and string-length($UBO.Primo.Search.Link) &gt;0">
        <a href="{$UBO.Primo.Search.Link}isbn,contains,{translate(text(),'-','')}">
          <xsl:value-of select="text()"/>
        </a>
      </xsl:when>
      <xsl:when test="$UBO.ISBN.Search.Link and string-length($UBO.ISBN.Search.Link) &gt;0">
        <a href="{$UBO.ISBN.Search.Link}{text()}">
          <xsl:value-of select="text()"/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="text()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ========== ISSN ========== -->
  <xsl:template match="mods:identifier[@type='issn']">
    <xsl:variable name="parameters" select="concat($UBO.JOP.Parameters,text())" />

    <a href="{$UBO.JOP.URL}?{$parameters}" title="{i18n:translate('ubo.jop')}">
      <xsl:value-of select="text()" />
      <xsl:text> </xsl:text>
      <img style="float:none" loading="lazy" data-src="https://services.dnb.de/fize-service/gvr/icon?{$parameters}" alt="{i18n:translate('ubo.jop')}" />
    </a>
  </xsl:template>

  <!-- ========== ZDB ID ========== -->
  <xsl:template match="mods:identifier[@type='zdb']">
    <a href="https://ld.zdb-services.de/resource/{.}" title="{i18n:translate('ubo.identifier.zdb')}">
      <xsl:value-of select="." />
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

  <!-- ========== arXiv.org ID ========== -->
  <xsl:param name="UBO.arXiv.Link" />

  <xsl:template match="mods:identifier[@type='arxiv']">
    <a href="{$UBO.arXiv.Link}{text()}">
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
    <a href="https://duepublico.uni-due.de/servlets/DocumentServlet?id={text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico2']">
    <a href="https://duepublico2.uni-due.de/receive/{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='oclc']">
    <a href="https://www.worldcat.org/oclc/{text()}">
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <xsl:template match="mods:identifier[@type = 'dbt']">
    <a href="{concat('https://www.db-thueringen.de/receive/', .)}" target="_blank">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- ========== URI / PPN ========== -->

  <xsl:template match="mods:identifier[@type='uri']">
    <xsl:variable name="ppn" select="substring-after(text(), ':ppn:')"/>

    <xsl:choose>
      <xsl:when test="$UBO.URI.gbv.de.ppn.redirect">
        <a href="{$UBO.URI.gbv.de.ppn.redirect}{$ppn}">
          <xsl:value-of select="$ppn" />
        </a>
      </xsl:when>
      <xsl:when test="contains(text(), 'uri.gbv.de/document')">
        <a href="{text()}?format=redirect">
          <xsl:value-of select="$ppn" />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{text()}">
          <xsl:value-of select="text()" />
        </a>
      </xsl:otherwise>
    </xsl:choose>
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
    <xsl:choose>
      <xsl:when test="not(@type)">
        <xsl:value-of select="." />
      </xsl:when>
      <xsl:when test="check:currentUserIsAdmin()">
        <xsl:value-of select="." />
        <xsl:value-of select="concat(' [', @type, ']')" />
      </xsl:when>
    </xsl:choose>
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
    <xsl:choose>
      <xsl:when test="$UBO.Primo.Search.Link and string-length($UBO.Primo.Search.Link) &gt; 0">
        <a href="{$UBO.Primo.Search.Link}holding_call_number,exact,%22{$sig}%22">
          <xsl:value-of select="text()" />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="text()" />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- ========== Band/Jahrgang, Heftnummer, Seitenangaben ========== -->
  <xsl:template match="mods:part">
    <xsl:apply-templates select="mods:detail[@type='volume']" />
    <xsl:apply-templates select="mods:detail[@type='issue']" />
    <xsl:apply-templates select="mods:detail[@type='page']" />
    <xsl:apply-templates select="mods:extent[@unit='pages']" />
    <xsl:apply-templates select="mods:detail[@type='article_number']" />
  </xsl:template>

  <!-- ========== Band/Jahrgang ========== -->
  <xsl:template match="mods:detail[@type='volume']">
    <span class="ubo-mods-detail-volume">
      <xsl:choose>
        <xsl:when test="../mods:detail[@type='issue']">
          <xsl:value-of select="i18n:translate('ubo.details.volume.journal')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('ubo.details.volume.series')" />
        </xsl:otherwise>
      </xsl:choose>
    </span>
    <xsl:text> </xsl:text>
    

    <xsl:value-of select="mods:number" />

    <xsl:variable name="volume.number" select="mods:number" />
    <xsl:variable name="year.issued" select="ancestor::mods:mods/descendant::mods:dateIssued[not(ancestor::mods:relatedItem[@type='host'])][1]" />

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
    <xsl:if test="../mods:detail[not(@type='page')] or ../mods:detail[not(@type='article_number')]">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="i18n:translate('ubo.pages.abbreviated.single')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="mods:number" />
  </xsl:template>

  <!-- ========== Seiten von-bis ========== -->
  <xsl:template match="mods:part/mods:extent[@unit='pages']">
    <xsl:if test="../mods:detail[not(@type='article_number')]">
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

  <!-- ========== Artikelnummer ========== -->
  <xsl:template match="mods:detail[@type='article_number']">
    <xsl:if test="../mods:detail[not(@type='article_number')] or ../mods:extent[@unit='pages']">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:value-of select="i18n:translate('ubo.articlenumber')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="mods:number" />
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
