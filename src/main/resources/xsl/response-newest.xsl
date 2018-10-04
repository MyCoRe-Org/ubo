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
    <article class="highlight1">
      <h3>
        <xsl:value-of select="i18n:translate('ubo.newest')" />:
      </h3>
      <xsl:for-each select="result[@name='response']">
        <ul style="margin-top:1ex;">
          <xsl:for-each select="doc">
            <li style="margin-bottom:1ex;">
              <xsl:variable name="id" select="str[@name='id']" />
              <xsl:variable name="mycoreobject" select="document(concat('mcrobject:',$id))/mycoreobject" />
              <div class="content bibentry">
                <xsl:apply-templates select="$mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" mode="cite">
                  <xsl:with-param name="mode">divs</xsl:with-param>
                </xsl:apply-templates>
              </div>
            </li>
          </xsl:for-each>
        </ul>
        <xsl:variable name="year" select="doc[1]/int[@name='year']" />
        <p>
          <a href="{$ServletsBaseURL}solr/select?q=status:confirmed+AND+year:[{$year - 1}+TO+{$year}]&amp;sort=year+desc,created+desc">
            <xsl:value-of select="i18n:translate('ubo.more')" />...
          </a>
        </p>
      </xsl:for-each>
    </article>
  </xsl:template>

</xsl:stylesheet>