<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xed="http://www.mycore.de/xeditor"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:transformer="xalan://org.mycore.frontend.xeditor.MCRXEditorTransformer"
  exclude-result-prefixes="xsl xed xalan i18n transformer">

  <xsl:param name="WebApplicationBaseURL" />

  <!-- ========== Repeater buttons: <xed:repeat><xed:controls> ========== -->

  <xsl:template match="text()" mode="xed.control">
    <!-- append insert remove up down -->
    <xsl:param name="name" /> <!-- name to submit as request parameter when button/image is clicked -->

    <!-- Choose a label for the button -->
    <xsl:variable name="symbol">
      <xsl:choose>
        <xsl:when test=".='append'">
          <xsl:value-of select="'plus'" />
        </xsl:when>
        <xsl:when test=".='insert'">
          <xsl:value-of select="'plus'" />
        </xsl:when>
        <xsl:when test=".='remove'">
          <xsl:value-of select="'minus'" />
        </xsl:when>
        <xsl:when test=".='up'">
          <xsl:value-of select="'up'" />
        </xsl:when>
        <xsl:when test=".='down'">
          <xsl:value-of select="'down'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="fa-class">
      <xsl:choose>
        <xsl:when test=".='append'">
          <xsl:value-of select="'fa-plus'" />
        </xsl:when>
        <xsl:when test=".='insert'">
          <xsl:value-of select="'fa-plus'" />
        </xsl:when>
        <xsl:when test=".='remove'">
          <xsl:value-of select="'fa-minus'" />
        </xsl:when>
        <xsl:when test=".='up'">
          <xsl:value-of select="'fa-arrow-up'" />
        </xsl:when>
        <xsl:when test=".='down'">
          <xsl:value-of select="'fa-arrow-down'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <div class="form-control ubo-repeater-button-box">
      <button type="submit" name="{$name}" class="xeditor-pmud fas" title="{$symbol}" tabindex="999">
        <i class="fas {$fa-class}"></i>
      </button>
    </div>
  </xsl:template>

  <!-- ========== Validation error messages: <xed:validate /> ========== -->

  <xsl:template match="xed:validate[@i18n]" mode="message">
    <li>
      <xsl:value-of select="i18n:translate(@i18n)" />
    </li>
  </xsl:template>

  <xsl:template match="xed:validate" mode="message">
    <li>
      <xsl:apply-templates select="node()" mode="xeditor" />
    </li>
  </xsl:template>

</xsl:stylesheet>
