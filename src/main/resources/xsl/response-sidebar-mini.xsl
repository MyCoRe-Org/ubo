<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl mods xalan i18n">

  <xsl:include href="mods-display.xsl" />

  <xsl:param name="ServletsBaseURL" />

  <xsl:template match="/response">
    <include>
      <article class="highlight2">
        <hgroup>
          <h2>
            <xsl:value-of select="i18n:translate('ubo.numPublicationsTotal',result[@name='response']/@numFound)" />
          </h2>
        </hgroup>
        <p>
          <xsl:for-each select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='year']/int">
            <xsl:sort select="@name" data-type="number" order="descending" />
            <xsl:if test="position() &lt; 4">
              <xsl:value-of select="@name"/>
              <xsl:text> : </xsl:text>
              <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+year:{@name}">
                <xsl:value-of select="text()"/>
                <xsl:text> </xsl:text> 
                <xsl:value-of select="i18n:translate('ubo.publications')" />
              </a>
              <br/>
            </xsl:if>
          </xsl:for-each>
        </p>
      </article>
    </include>
  </xsl:template>

</xsl:stylesheet>