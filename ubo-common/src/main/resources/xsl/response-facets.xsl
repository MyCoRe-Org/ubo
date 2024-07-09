<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:str="xalan://java.lang.String"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:dozbib="xalan://org.mycore.ubo.DozBibCommands"
  xmlns:solrUtil="xalan://org.mycore.solr.MCRSolrUtils"
  exclude-result-prefixes="dozbib xsl xalan i18n encoder mcrxml str solrUtil">

<xsl:param name="RequestURL" />
<xsl:param name="ServletsBaseURL" />

<xsl:variable name="maxFacetValuesDisplayed">5</xsl:variable>
<xsl:variable name="quotes">"</xsl:variable>
<xsl:variable name="fq_not">-</xsl:variable>

<xsl:template match="lst[@name='facet_counts']">
  <xsl:apply-templates select="../lst[@name='responseHeader']/lst[@name='params']" mode="fq" />
  <xsl:apply-templates select="lst[@name='facet_fields']/lst[int]" />
</xsl:template>

<!-- List currently active filter queries -->
<xsl:template match="lst[@name='responseHeader']/lst[@name='params']" mode="fq">
  <xsl:variable name="hasActiveFilters" select="boolean(*[@name='fq'])" />
  <article class="card mb-3">
    <div class="card-body">
      <hgroup>
        <h3>
          <xsl:value-of select="i18n:translate(concat('facets.filters.',$hasActiveFilters))" />
        </h3>
      </hgroup>
      <xsl:if test="$hasActiveFilters">
        <ul class="list-group">
          <xsl:for-each select="*[@name='fq']/descendant-or-self::str">

            <xsl:variable name="fq" select="text()" />

            <xsl:variable name="removeURL">
              <xsl:value-of select="$solrRequestHandler"/>

              <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/*">
                <xsl:variable name="param_name" select="@name" />
                <xsl:for-each select="descendant-or-self::str"> <!-- may be an array: arr/str or ./str -->
                  <xsl:choose>
                    <xsl:when test="$param_name='start'" />
                    <xsl:when test="($param_name='fq') and (text()=$fq)" /> <!--  remove this -->
                    <xsl:otherwise>
                      <xsl:value-of select="$param_name" />
                      <xsl:text>=</xsl:text>
                      <xsl:value-of select="encoder:encode(text(),'UTF-8')" />
                      <xsl:text>&amp;</xsl:text>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:for-each>
              <xsl:text>start=0</xsl:text>
            </xsl:variable>

            <xsl:variable name="fq_without_quotes" select="str:replaceAll(str:new($fq),$quotes,'')" />

            <li class="mycore-list-item">
              <xsl:variable name="facet-remove-display-text">
              <xsl:choose>
                <xsl:when test="starts-with($fq_without_quotes,$fq_not)">
                  <xsl:variable name="fq_without_not" select="substring-after($fq_without_quotes,'-')" />
                  <xsl:call-template name="output.facet.value">
                    <xsl:with-param name="prefix" select="concat(i18n:translate('facets.filters.not'),' ')" />
                    <xsl:with-param name="type"  select="substring-before($fq_without_not,':')" />
                    <xsl:with-param name="value" select="substring-after($fq_without_not,':')" />
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="output.facet.value">
                    <xsl:with-param name="type"  select="substring-before($fq_without_quotes,':')" />
                    <xsl:with-param name="value" select="substring-after($fq_without_quotes,':')" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
              <a class="mycore-facet-remove" href="{$removeURL}" title="{translate($facet-remove-display-text, '\', '')} {i18n:translate('edit.remove')}">
                <span class="far fa-times-circle" aria-hidden="true" />
              </a>
              <span class="mycore-facet-filter">
                <xsl:value-of select="translate($facet-remove-display-text, '\', '')"/>
              </span>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </div>
  </article>
</xsl:template>

<xsl:variable name="permission.admin" xmlns:check="xalan://org.mycore.ubo.AccessControl" select="check:currentUserIsAdmin()" />

<!-- List a facet -->
<xsl:template match="lst[@name='facet_fields']/lst[int]">
  <xsl:choose>
    <xsl:when test="(@name='status') and not($permission.admin)" /> <!-- do not show status facet if not admin -->
    <xsl:when test="(@name='importID') and not($permission.admin)" /> <!-- do not show importID facet if not admin -->
    <xsl:otherwise>
      <xsl:call-template name="display.facet" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="display.facet">
  <xsl:variable name="max">
    <xsl:for-each select="int">
      <xsl:sort select="text()" data-type="number" order="descending"/>
      <xsl:if test="position() = 1"><xsl:value-of select="string-length(text())"/></xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <article class="card mb-3">
    <div class="card-body">
      <hgroup>
        <h3>
          <xsl:call-template name="get-facet-name">
            <xsl:with-param name="facetName" select="str:replaceAll(str:new(@name),'facet_', '')"/>
          </xsl:call-template>
        </h3>
      </hgroup>
      <ul id="{generate-id(.)}" class="list-group counter-length-{$max}">
        <xsl:choose>
          <xsl:when test="@name='year'"> <!-- sort year facets by year, descending -->
            <xsl:apply-templates select="int" mode="facets">
              <xsl:sort select="@name" data-type="number" order="descending" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise> <!-- sort other facets by facet count, descending -->
            <xsl:apply-templates select="int" mode="facets">
              <xsl:sort select="text()" data-type="number" order="descending" />
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </ul>
      <xsl:variable name="numMore" select="count(int) - number($maxFacetValuesDisplayed)" />
      <xsl:if test="$numMore &gt; 0">
        <xsl:variable name="facet-human-readable">
          <xsl:call-template name="get-facet-name">
            <xsl:with-param name="facetName" select="str:replaceAll(str:new(@name), 'facet_', '')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="facets-toggle-more-link-text" select="concat($numMore,' ',i18n:translate('facets.toggle.more'))"/>

        <div class="float-right mycore-slidetoggle">
          <a class="mycore-facets-toggle" id="tg{generate-id(.)}" title="{$facet-human-readable}: {$facets-toggle-more-link-text}" role="button"
             onclick="$('ul#{generate-id(.)} li:gt({$maxFacetValuesDisplayed - 1})').slideToggle(); $('a#tg{generate-id(.)} span').toggle();">
            <span>
              <xsl:value-of select="$facets-toggle-more-link-text"/>
            </span>
            <span style="display:none;" title="{i18n:translate('facets.toggle.less')}">
              <xsl:value-of select="i18n:translate('facets.toggle.less')"/>
            </span>
            <xsl:text>...</xsl:text>
          </a>
        </div>
      </xsl:if>
    </div>
  </article>
</xsl:template>

<!-- URL to build links to add a facet filter query -->
<xsl:variable name="baseURL">
  <xsl:value-of select="$solrRequestHandler"/>

  <xsl:for-each select="/response/lst[@name='responseHeader']/lst[@name='params']/*">
    <xsl:variable name="name" select="@name" />
    <xsl:for-each select="descendant-or-self::str"> <!-- may be an array: arr/str or ./str -->
      <xsl:choose>
        <xsl:when test="$name='start'" />
        <xsl:otherwise>
          <xsl:value-of select="$name" />
          <xsl:text>=</xsl:text>
          <xsl:value-of select="encoder:encode(text(),'UTF-8')" />
          <xsl:text>&amp;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:for-each>
  <xsl:text>start=0&amp;fq=</xsl:text>
</xsl:variable>

<!-- Output single facet value -->
<xsl:template match="lst[@name='facet_fields']/lst/int" mode="facets">
  <li class="mycore-list-item">
    <xsl:if test="position() &gt; number($maxFacetValuesDisplayed)">
      <xsl:attribute name="style">display:none;</xsl:attribute>
    </xsl:if>
    <span class="mycore-facet-count">
      <xsl:value-of select="text()" />
    </span>
    <xsl:variable name="fq" select="encoder:encode(concat(../@name,':', $quotes, solrUtil:escapeSearchValue(@name), $quotes), 'UTF-8')" />
    <xsl:variable name="facet-human-readable">
      <xsl:call-template name="get-facet-name">
        <xsl:with-param name="facetName" select="str:replaceAll(str:new(../@name),'facet_', '')"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="facet-value">
      <xsl:call-template name="output.facet.value">
        <xsl:with-param name="type" select="../@name"/>
        <xsl:with-param name="value" select="@name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="number(text()) &lt; number($numFound)"> <!-- When count = 100%, filtering makes no sense -->
        <xsl:variable name="title-exclude-facet-value" select="dozbib:translate('facets.results.exclude', concat($facet-human-readable, ',' , $facet-value))"/>
        <xsl:variable name="title-include-facet-value" select="dozbib:translate('facets.results.include', concat($facet-human-readable, ',' , $facet-value))"/>

        <a class="mycore-facet-exclude" href="{$baseURL}{encoder:encode($fq_not)}{$fq}" title="{$title-exclude-facet-value}"> <!-- Link to exclude this facet value -->
          <span class="far fa-times-circle" aria-hidden="true" />
        </a>
        <a class="mycore-facet-add" href="{$baseURL}{$fq}" title="{$title-include-facet-value}">
          <span class="mycore-facet-value">
            <span>
              <xsl:if test="string-length($facet-value) &gt; 20">
                <xsl:attribute name="class">scroll-on-hover</xsl:attribute>
              </xsl:if>
              <xsl:value-of select="$facet-value"/>
            </span>
          </span>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <span class="mycore-facet-exclude" />
        <span class="mycore-facet-value">
          <span>
            <xsl:if test="string-length($facet-value) &gt; 20">
              <xsl:attribute name="class">scroll-on-hover</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$facet-value"/>
          </span>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </li>
</xsl:template>

<!-- Output facet value: some must be translated to a label, e.g. subject, genre -->
<xsl:template name="output.facet.value">
  <xsl:param name="prefix"/>
  <xsl:param name="type"/>
  <xsl:param name="value"/>

  <xsl:variable name="label">
    <xsl:value-of select="$prefix"/>
    <xsl:choose>
      <xsl:when test="$type='subject'">
        <xsl:variable name="url">classification:metadata:0:children:fachreferate:<xsl:value-of
            select="encoder:encode($value,'UTF-8')"/>
        </xsl:variable>
        <xsl:value-of select="document($url)/mycoreclass/categories/category[1]/label[@xml:lang=$CurrentLang]/@text"/>
      </xsl:when>
      <xsl:when test="$type='oa'">
        <xsl:value-of select="$oa//category[@ID=$value]/label[lang($CurrentLang)]/@text"/>
      </xsl:when>
      <xsl:when test="$type='genre'">
        <xsl:value-of select="$genres//category[@ID=$value]/label[lang($CurrentLang)]/@text"/>
      </xsl:when>
      <xsl:when test="$type='origin_exact'">
        <xsl:value-of select="$origin//category[@ID=$value]/label[lang($CurrentLang)]/@text"/>
      </xsl:when>
      <xsl:when test="$type='status'">
        <xsl:value-of select="i18n:translate(concat('search.dozbib.status.',$value))"/>
      </xsl:when>
      <xsl:when test="$type='partOf'">
        <xsl:value-of select="i18n:translate(concat('search.dozbib.partOf.', $value))"/>
      </xsl:when>
      <xsl:when test="$type='project'">
        <xsl:value-of select="mcrxml:getDisplayName('project', $value)"/>
      </xsl:when>
      <xsl:when test="$type='fundingType'">
        <xsl:value-of select="mcrxml:getDisplayName('fundingType', $value)"/>
      </xsl:when>
      <xsl:when test="$type='peerreviewed'">
          <xsl:value-of select="$peerreviewed//category[@ID=$value]/label[lang($DefaultLang)]/@text"/>
      </xsl:when>
      <xsl:when test="$type='accessrights'">
          <xsl:value-of select="$accessrights//category[@ID=$value]/label[lang($CurrentLang)]/@text"/>
      </xsl:when>
      <xsl:when test="$type='mediaType'">
          <xsl:value-of select="$mediaType//category[@ID=$value]/label[lang($CurrentLang)]/@text"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="fallback-label">
    <xsl:if test="string-length($label) = 0">
      <xsl:value-of select="$prefix"/>
      <xsl:choose>
        <xsl:when test="$type='subject'">
          <xsl:variable name="url">classification:metadata:0:children:fachreferate:<xsl:value-of
              select="encoder:encode($value,'UTF-8')"/>
          </xsl:variable>
          <xsl:value-of select="document($url)/mycoreclass/categories/category[1]/label[@xml:lang=$DefaultLang]/@text"/>
        </xsl:when>
        <xsl:when test="$type='oa'">
          <xsl:value-of select="$oa//category[@ID=$value]/label[lang($DefaultLang)]/@text"/>
        </xsl:when>
        <xsl:when test="$type='genre'">
          <xsl:value-of select="$genres//category[@ID=$value]/label[lang($DefaultLang)]/@text"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$value"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>

  <span>
    <xsl:choose>
      <xsl:when test="string-length($label) ">
        <xsl:if test="string-length($label) &gt; 20">
          <xsl:attribute name="class">scroll-on-hover</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="$label"/>
      </xsl:when>
      <xsl:when test="$label">
        <xsl:if test="string-length($fallback-label) &gt; 20">
          <xsl:attribute name="class">scroll-on-hover</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="$fallback-label"/>
      </xsl:when>
    </xsl:choose>
  </span>
</xsl:template>

  <xsl:template name="get-facet-name">
    <xsl:param name="facetName"/>

    <xsl:choose>
      <xsl:when test="i18n:exists(concat('facets.facet.', $facetName))">
        <xsl:value-of select="i18n:translate(concat('facets.facet.', $facetName))"/>
      </xsl:when>
      <xsl:when test="not(document(concat('notnull:classification:metadata:all:children:', $facetName))/null)">
        <xsl:value-of
          select="document(concat('notnull:classification:metadata:all:children:', $facetName))/mycoreclass/label[@xml:lang=$CurrentLang]/@text"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="i18n:translate(concat('facets.facet.', $facetName))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
