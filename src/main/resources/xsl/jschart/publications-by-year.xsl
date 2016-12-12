<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n">
  <xsl:template match="table[@charttype='PublicationsByYear']" mode="chart">
    
    <script type="text/javascript">
    <![CDATA[
jQuery(document).ready(function() {
      
      new Highcharts.Chart({
         chart: {
            renderTo: 'ubo-chart-]]><xsl:value-of select="@id" /><![CDATA[',
            defaultSeriesType: 'column',
            events: {
              click: function(e) {
                jQuery('#chart-dialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 100,
                  height: jQuery(window).height() - 100,
                  draggable: false,
                  resizable: false,
                  modal: false
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chart-dialog';
                dialogOptions.chart.events = null;
                dialogOptions.chart.zoomType = 'x';
                new Highcharts.Chart(dialogOptions);
              }
            }
         },
         title: {
            text: ']]><xsl:value-of select="@name" /><![CDATA['
         },
         legend: {
            enabled: false
         },
         tooltip: {
              formatter: function() {
                return '<b>' + Highcharts.dateFormat('%Y', this.point.x) +'</b>: '+ this.point.y;
              }
         },
         xAxis: {
           type: 'datetime',
           min: Date.UTC(1970, 0, 1),
           max: Date.UTC(]]><xsl:value-of xmlns:datetime="http://exslt.org/dates-and-times" select="datetime:year() + 1" /><![CDATA[, 11, 31),
           maxZoom: 365 * 24 * 3600 * 1000,
           dateTimeLabelFormats: {
              day: '%Y'
           }
           
         },
         yAxis: {
            title: {
                text: ']]><xsl:value-of select="i18n:translate('stats.count')" /><![CDATA['
            }
         },
         plotOptions: {
            column: {
              pointPadding: 0.2,
              borderWidth: 0
            }
         },
         series: [{
              name: ']]><xsl:value-of select="@name" /><![CDATA[',
              data: [
                ]]>
                <xsl:for-each select="row[number(@label) > 1960]">
                  <xsl:sort data-type="number" select="@label"/>
                  [Date.UTC(<xsl:value-of select="@label"/>, 0, 1), <xsl:value-of select="@num"/>],
                </xsl:for-each>
                <![CDATA[
              ]
          }
        ]
    });
 });
            ]]>
    </script>

  </xsl:template>
  
</xsl:stylesheet>