<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n"
>

<xsl:include href="layout.xsl" />

<xsl:template name="basketClearButton">
  <xsl:for-each select="/basket[entry]">
    <action target="{$ServletsBaseURL}MCRBasketServlet" label="{i18n:translate('button.clear')}">
      <param name="type"   value="{@type}" />
      <param name="action" value="clear" />
    </action>
  </xsl:for-each>
</xsl:template>

<xsl:template name="basketNumEntries">
  <div class="section">
    <xsl:if test="not(entry)">
      <xsl:attribute name="id">sectionlast</xsl:attribute>
    </xsl:if>
    <p>
      <xsl:choose>
        <xsl:when test="count(entry) = 0">
          <xsl:value-of select="i18n:translate('basket.containsNone')" />
        </xsl:when>
        <xsl:when test="count(entry) = 1">
          <xsl:value-of select="i18n:translate('basket.containsOne')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('basket.containsMany',count(*))" />
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </div>
</xsl:template>

<xsl:template name="basketEntries">
  <xsl:if test="entry">
    <div class="section" id="sectionlast">
      <xsl:apply-templates select="entry" />
    </div>
  </xsl:if>
</xsl:template>

<xsl:template match="entry">
  <div class="basketEntry">
    <xsl:attribute name="class">
      <xsl:text>basketEntry basketEntry</xsl:text>
      <xsl:value-of select="2 - (position() mod 2)" />
    </xsl:attribute>
    <div class="basketButtons">
      <xsl:call-template name="buttons" />
    </div>
    <div class="basketContent">
      <xsl:choose>
        <xsl:when test="*[not(name()='comment')]">
          <xsl:apply-templates select="*[not(name()='comment')]" mode="basketContent" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="document(@uri)/*" mode="basketContent" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="comment" mode="basketContent" />
    </div>
  </div>  
</xsl:template>

<xsl:template name="basketButtonsUpDownDelete">
  <xsl:call-template name="button">
    <xsl:with-param name="image">pmud-up.png</xsl:with-param>
    <xsl:with-param name="action">up</xsl:with-param>
    <xsl:with-param name="alt">Nach oben</xsl:with-param>
    <xsl:with-param name="condition" select="position() &gt; 1" />
  </xsl:call-template>
  <xsl:call-template name="button">
    <xsl:with-param name="image">pmud-down.png</xsl:with-param>
    <xsl:with-param name="action">down</xsl:with-param>
    <xsl:with-param name="alt">Nach unten</xsl:with-param>
    <xsl:with-param name="condition" select="position() != last()" />
  </xsl:call-template>
  <xsl:call-template name="button">
    <xsl:with-param name="image">remove.png</xsl:with-param>
    <xsl:with-param name="action">remove</xsl:with-param>
    <xsl:with-param name="alt">Entfernen</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="button">
  <xsl:param name="image" />
  <xsl:param name="alt" />
  <xsl:param name="action" />
  <xsl:param name="condition" select="true()" />

  <xsl:choose>
    <xsl:when test="$condition">
      <a href="MCRBasketServlet?action={$action}&amp;type={/basket/@type}&amp;id={@id}" class="roundedButton basketButton">  
        <img alt="{$alt}" src="{$WebApplicationBaseURL}images/{$image}" />
      </a>
    </xsl:when>
    <xsl:otherwise>
      <span />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>