<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl xalan i18n">

  <xsl:template name="confirm-dialog">
    <xsl:param name="id" select="generate-id()"/>
    <xsl:param name="title"/>
    <xsl:param name="message"/>
    <xsl:param name="action"/>
    <xsl:param name="button.confirm.text" select="i18n:translate('button.action')"/>
    <xsl:param name="button.cancel.text" select="i18n:translate('button.cancel')"/>

    <div id="{$id}" class="modal fade" data-backdrop="static" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <xsl:value-of select="$title"/>
            </h5>
          </div>
          <div class="modal-body">
            <p>
              <xsl:value-of select="$message"/>
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">
              <xsl:value-of select="$button.cancel.text"/>
            </button>
            <button type="button" class="btn btn-primary" onclick="{$action}">
              <xsl:value-of select="$button.confirm.text"/>
            </button>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
