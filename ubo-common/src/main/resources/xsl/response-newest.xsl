<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl mods xalan i18n">

  <xsl:include href="coreFunctions.xsl" />

  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="CurrentLang" />

  <xsl:template match="/response">
    <xsl:variable name="originalQuery" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='q']"/>

    <article class="card">
      <div class="card-body">
        <h3>
          <xsl:value-of select="i18n:translate('ubo.newest')" />
        </h3>
        <xsl:for-each select="result[@name='response']">
          <ul class="list-group">
            <xsl:for-each select="doc">
              <xsl:variable name="cite">
                <xsl:choose>
                  <xsl:when test="str[@name=concat('cite.', $CurrentLang)]">
                    <xsl:value-of select="str[@name=concat('cite.', $CurrentLang)]"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="str[@name='cite']"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

              <li class="list-group-item">
                <xsl:variable name="id" select="str[@name='id']" />
                <div class="content bibentry ubo-hover-pointer" title="{i18n:translate('button.show')}" onclick="location.assign('{$WebApplicationBaseURL}servlets/DozBibEntryServlet?mode=show&amp;id={$id}');">
                  <xsl:value-of select="$cite" disable-output-escaping="yes"/>
                </div>
              </li>
            </xsl:for-each>
          </ul>
          <xsl:variable name="year" select="doc[1]/int[@name='year']" />
          <p>
            <a href="{$ServletsBaseURL}solr/select?q={$originalQuery}+AND+year:%5B{$year - 1}+TO+*%5D&amp;sort=year+desc,created+desc">
              <xsl:value-of select="i18n:translate('ubo.more')"/>...
            </a>
          </p>
        </xsl:for-each>
      </div>
    </article>
  </xsl:template>

</xsl:stylesheet>
