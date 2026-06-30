<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="i18n mods xsl">

  <xsl:import href="xslImport:uboActionButtons:ubo-actions-default.xsl"/>

  <xsl:template match="*" mode="ubo-actions">
    <xsl:apply-imports/>

    <xsl:if test="$permission.admin and not($UBO.System.ReadOnly = 'true')">
      <xsl:if test="(string-length($step) = 0) or contains($step,'merged')">
        <a class="action btn btn-sm btn-outline-primary mb-1"
           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-publication.xed&amp;id={/mycoreobject/@ID}">
          <xsl:value-of select="i18n:translate('button.edit')"/>
        </a>
        <a class="action btn btn-sm btn-outline-primary mb-1"
           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-admin.xed&amp;id={/mycoreobject/@ID}">Admin
        </a>
        <xsl:if test="$permission.isSuperuser">
          <a class="action btn btn-sm btn-outline-primary mb-1"
             href="{$WebApplicationBaseURL}modules/webtools/texteditor/objects/{/mycoreobject/@ID}">XML
          </a>
        </xsl:if>
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
  </xsl:template>
</xsl:stylesheet>
