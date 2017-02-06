<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:java="http://xml.apache.org/xalan/java"
  exclude-result-prefixes="xsl xalan i18n mods java"
>

<xsl:include href="basket-common.xsl" />
<xsl:include href="bibmaster.xsl" />
<xsl:include href="mods-display.xsl" />

<xsl:variable name="page.title" select="'Korb'" />
<xsl:variable name="PageID" select="'bibbasket'" />

<xsl:variable name="actions">
  <xsl:call-template name="basketClearButton" />
  <xsl:for-each select="/basket[entry]">
    <xsl:choose>
      <xsl:when test="$UBO.System.ReadOnly = 'true'" />
      <xsl:when xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()">
        <action label="Personen zuordnen" target="{$WebApplicationBaseURL}edit-contributors.xed" />
        <xsl:if test="entry/mycoreobject[@changed='true']">
          <action label="Änderungen speichern" target="{$ServletsBaseURL}BasketName2PIDEditor">
            <param name="action" value="save" />
          </action>
        </xsl:if>
      </xsl:when>
    </xsl:choose>
    <action label="MODS" target="MCRExportServlet/mods.xml">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="mods" />
    </action>
    <action label="BibTeX" target="MCRExportServlet/export.bibtex">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="bibtex" />
    </action>
    <action label="EndNote" target="MCRExportServlet/export.endnote">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="endnote" />
    </action>
    <action label="RIS" target="MCRExportServlet/export.ris">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="ris" />
    </action>
    <action label="PDF" target="MCRExportServlet/export.pdf">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="pdf" />
    </action>
    <action label="HTML" target="MCRExportServlet/export.html">
      <param name="basket" value="bibentries" />
      <param name="root" value="export" />
      <param name="transformer" value="html" />
    </action>
  </xsl:for-each>
</xsl:variable>

<xsl:template match="basket">

  <div class="section">
    <p>
      In diesem Korb können Sie ausgewählte Publikationen sammeln, um sie dann in verschiedenen Formaten
      zu exportieren. Bitte beachten Sie, dass der Korb an Ihre Benutzersitzung gebunden ist, so dass
      er nach maximal 30 Minuten Inaktivität geleert wird.
    </p>  
  </div>

  <xsl:call-template name="basketNumEntries" />

  <xsl:if test="entry/mycoreobject[@changed='true']">
    <div class="section">
      <p>
        In Ihrem Korb befinden sich geänderte Einträge. Die Änderungen wirken momentan nur auf den
        Korb selbst. Klicken Sie "Änderungen speichern", um die Einträge dauerhaft zu ändern. 
      </p>
    </div>
  </xsl:if>

  <xsl:call-template name="basketEntries" />

</xsl:template>

<xsl:template match="mycoreobject" mode="basketContent">
  <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
    <div class="labels">
      <xsl:call-template name="label-year" />
      <xsl:call-template name="pubtype" />
    </div>
    <div class="content bibentry">  
      <xsl:apply-templates select="." mode="cite"> 
        <xsl:with-param name="mode">divs</xsl:with-param> 
      </xsl:apply-templates>
      <div class="footer">
        <form action="{$ServletsBaseURL}DozBibEntryServlet" method="get">
          <input type="hidden" name="mode" value="show"/>
          <input type="hidden" name="id" value="{ancestor::mycoreobject/@ID}"/>
          <input type="submit" class="roundedButton" value="{i18n:translate('result.dozbib.info')}" />
        </form>
      </div>
    </div>
  </xsl:for-each>
</xsl:template>

<xsl:template name="buttons">
  <xsl:call-template name="basketButtonsUpDownDelete" />
</xsl:template>

</xsl:stylesheet>

