<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 20877 $ $Date: 2011-05-12 17:05:23 +0200 (Do, 12 Mai 2011) $ -->
<!-- ============================================== --> 

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n"
>

<xsl:template name="highcharts.header">

  <script src="{$WebApplicationBaseURL}external/jquery-ui-1.8.12.custom.min.js" type="text/javascript"></script>
  <script src="{$WebApplicationBaseURL}external/jquery.corners.min.js" type="text/javascript"></script>
  <script src="{$WebApplicationBaseURL}webjars/github-com-highcharts-highcharts/4.2.5/lib/highcharts.src.js" type="text/javascript"></script>
  <script src="{$WebApplicationBaseURL}webjars/github-com-highcharts-highcharts/4.2.5/lib/themes/grid.js" type="text/javascript"></script>
  
  <script type="text/javascript">
<![CDATA[
jQuery(document).ready(function() {

  jQuery('.inner-stats-container').each(function() {
    jQuery(this).corner("round 6px").parent().css('padding', '2px').corner("round 8px");
  });

  highchartsOptions.colors = [
  
          '#4572A7', 
          '#AA4643', 
          '#89A54E', 
          '#80699B', 
          '#3D96AE', 
          '#DB843D', 
          '#92A8CD', 
          '#A47D7C', 
          '#B5CA92'
  ];
  Highcharts.setOptions({
        lang: {
          months: [']]><xsl:value-of select="i18n:translate('month.january')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.february')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.march')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.april')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.may')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.june')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.juli')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.august')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.september')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.october')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.november')" /><![CDATA[', 
                   ']]><xsl:value-of select="i18n:translate('month.december')" /><![CDATA['],
          weekdays: [']]><xsl:value-of select="i18n:translate('day.sunday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.monday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.tuesday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.wednesday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.thursday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.friday')" /><![CDATA[', 
                     ']]><xsl:value-of select="i18n:translate('day.saturday')" /><![CDATA['],
          resetZoom: ']]><xsl:value-of select="i18n:translate('stats.revert.zoom')" /><![CDATA[',
          printButtonTitle: ']]><xsl:value-of select="i18n:translate('stats.print')" /><![CDATA[',
          exportButtonTitle: ']]><xsl:value-of select="i18n:translate('stats.export')" /><![CDATA[',
          downloadPNG: ']]><xsl:value-of select="i18n:translate('stats.export.png')" /><![CDATA[',
          downloadJPEG: ']]><xsl:value-of select="i18n:translate('stats.export.jpg')" /><![CDATA[',
          downloadPDF: ']]><xsl:value-of select="i18n:translate('stats.export.pdf')" /><![CDATA[',
          downloadSVG: ']]><xsl:value-of select="i18n:translate('stats.export.svg')" /><![CDATA['
        },
        exporting: {
          width: jQuery(window).width() - 100
        }
      });
});
]]>
  </script>

</xsl:template>


</xsl:stylesheet>