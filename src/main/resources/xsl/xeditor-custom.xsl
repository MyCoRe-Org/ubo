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
    <xsl:variable name="fa-unicode">
      <xsl:choose>
        <xsl:when test=".='append'">
          <xsl:value-of select="'&#xf067;'" />
        </xsl:when>
        <xsl:when test=".='insert'">
          <xsl:value-of select="'&#xf067;'" />
        </xsl:when>
        <xsl:when test=".='remove'">
          <xsl:value-of select="'&#xf068;'" />
        </xsl:when>
        <xsl:when test=".='up'">
          <xsl:value-of select="'&#xf062;'" />
        </xsl:when>
        <xsl:when test=".='down'">
          <xsl:value-of select="'&#xf063;'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <div class="form-control border-0" style="max-width:20%;">
      <input type="image" class="xeditor-pmud fas" tabindex="999" name="{$name}" title="{$symbol}" value="{$fa-unicode}" style="color:Tomato; font-size:25px;"/>
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
