<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:str="xalan://java.lang.String"
  exclude-result-prefixes="xsl xalan i18n encoder"
>

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
              <xsl:text>select?</xsl:text>
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
              <a class="mycore-facet-remove" href="{$removeURL}">
                <span class="far fa-times-circle" aria-hidden="true" />
              </a>
              <span class="mycore-facet-filter">
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
  <article class="card mb-3">
    <div class="card-body">
      <hgroup>
	<h3><xsl:value-of select="i18n:translate(concat('facets.facet.',str:replaceAll(str:new(@name),'facet_','')))" /></h3>
      </hgroup>
      <ul id="{generate-id(.)}" class="list-group">
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
	<div class="float-right mycore-slidetoggle">
          <a class="mycore-facets-toggle" id="tg{generate-id(.)}" onclick="$('ul#{generate-id(.)} li:gt({$maxFacetValuesDisplayed - 1})').slideToggle(); $('a#tg{generate-id(.)} span').toggle();">
            <span><xsl:value-of select="concat($numMore,' ',i18n:translate('facets.toggle.more'))" /></span>
            <span style="display:none;"><xsl:value-of select="i18n:translate('facets.toggle.less')" /></span>
            <xsl:text>...</xsl:text>
          </a>
	</div>
      </xsl:if>
    </div>
  </article>
</xsl:template>

<!-- URL to build links to add a facet filter query -->
<xsl:variable name="baseURL">
  <xsl:text>select?</xsl:text>
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
    <xsl:variable name="fq" select="encoder:encode(concat(../@name,':',$quotes,@name,$quotes),'UTF-8')" />
    <xsl:choose>
      <xsl:when test="number(text()) &lt; number($numFound)"> <!-- When count = 100%, filtering makes no sense -->
        <a class="mycore-facet-exclude" href="{$baseURL}{encoder:encode($fq_not)}{$fq}"> <!-- Link to exclude this facet value -->
          <span class="far fa-times-circle" aria-hidden="true" />
        </a>
        <a class="mycore-facet-add" href="{$baseURL}{$fq}">
          <span class="mycore-facet-value">
            <xsl:call-template name="output.facet.value">
              <xsl:with-param name="type"  select="../@name" />
              <xsl:with-param name="value" select="@name"/>
            </xsl:call-template>
          </span>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <span class="mycore-facet-exclude" />
        <span class="mycore-facet-value">
          <xsl:call-template name="output.facet.value">
            <xsl:with-param name="type"  select="../@name" />
            <xsl:with-param name="value" select="@name"/>
          </xsl:call-template>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </li>
</xsl:template>

<!-- Output facet value: some must be translated to a label, e.g. subject, genre -->
<xsl:template name="output.facet.value">
  <xsl:param name="prefix" />
  <xsl:param name="type"  />
  <xsl:param name="value" />

  <xsl:variable name="label">
    <xsl:value-of select="$prefix" />
    <xsl:choose>
      <xsl:when test="$type='subject'">
        <xsl:variable name="url">classification:metadata:0:children:fachreferate:<xsl:value-of select="encoder:encode($value,'UTF-8')" /></xsl:variable>
        <xsl:value-of select="document($url)/mycoreclass/categories/category[1]/label[@xml:lang=$CurrentLang]/@text" />
      </xsl:when>
      <xsl:when test="$type='oa'">
        <xsl:value-of select="$oa//category[@ID=$value]/label[lang($CurrentLang)]/@text" />
      </xsl:when>
      <xsl:when test="$type='genre'">
        <xsl:value-of select="$genres//category[@ID=$value]/label[lang($CurrentLang)]/@text" />
      </xsl:when>
      <xsl:when test="$type='status'">
        <xsl:value-of select="i18n:translate(concat('search.dozbib.status.',$value))" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <span>
    <xsl:if test="string-length($label) &gt; 20">
       <xsl:attribute name="class">scroll-on-hover</xsl:attribute>
    </xsl:if>
    <xsl:value-of select="$label" />
  </span>
</xsl:template>

</xsl:stylesheet>
