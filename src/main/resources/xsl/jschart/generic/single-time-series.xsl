<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n">
  <xsl:template match="table[@charttype='SingleTimeSeries']" mode="chart">
    
    <script type="text/javascript">
    <![CDATA[
jQuery(document).ready(function() {
      
      new Highcharts.Chart({
         chart: {
            renderTo: 'ubo-chart-]]><xsl:value-of select="@id" /><![CDATA[',    
            zoomType: 'x',
            events: {
              click: function(e) {
                jQuery('#chart-dialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 100,
                  height: jQuery(window).height() - 100,
                  draggable: false,
                  resizable: false,
                  modal: true
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chart-dialog';
                new Highcharts.Chart(dialogOptions);
              }
            }
         },
         title: {
            text: ']]><xsl:value-of select="@name" /><![CDATA['
         },
         xAxis: {
            type: 'datetime',
            maxZoom: 365 * 24 * 3600 * 1000,
            title: {
              text: null
            }
         },
         tooltip: {
              formatter: function() {
                return '<b>' + Highcharts.dateFormat('%Y', this.point.x) +'</b>: '+ this.point.y;
              }
         },
         legend: {
            enabled: false
         },
         plotOptions: {
           area: {
              fillColor: {
                 linearGradient: [0, 0, 0, 350],
                 stops: [
                    [0, Highcharts.theme.colors[0]],
                    [1, 'rgba(2,0,0,0)']
                 ]
              },
              lineWidth: 2,
              marker: {
                 enabled: false,
                 states: {
                    hover: {
                       enabled: true,
                       radius: 5
                    }
                 }
              },
              shadow: false,
              states: {
                 hover: {
                    lineWidth: 1                  
                 }
              }
           }
        },

         
         series: [{
              name: ']]><xsl:value-of select="@name" /><![CDATA[',
              type: 'area',
              data: [
                ]]>
                <xsl:apply-templates select="row" mode="timeseries-row">
                  <xsl:sort select="@label" order="ascending"/>
                </xsl:apply-templates>
                <![CDATA[
              ]
          }
        ]
    });
 });
            ]]>
    </script>

  </xsl:template>
  
  <xsl:template match="row" mode="timeseries-row">
    [Date.UTC(<xsl:value-of select="@label"/>, 12, 31) , <xsl:value-of select="@num"/>],
  </xsl:template>
</xsl:stylesheet>