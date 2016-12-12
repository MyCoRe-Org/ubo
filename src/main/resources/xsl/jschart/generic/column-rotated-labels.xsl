<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n">
  <xsl:template match="table[@charttype='ColumnRotatedLabels']" mode="chart">
		
    <script type="text/javascript">
    <![CDATA[
jQuery(document).ready(function() {
      
      new Highcharts.Chart({
         chart: {
            renderTo: 'ubo-chart-]]><xsl:value-of select="@id" /><![CDATA[',
            zoomType: 'x',
            defaultSeriesType: 'column',
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
            categories: [
                ]]>
                 <xsl:for-each select="row">
                    '<xsl:value-of select="@label"/>',
                 </xsl:for-each>
                 <![CDATA[
            ],
            labels: {
              rotation: -45,
              align: 'right',
              style: {
                font: 'normal 13px Verdana, sans-serif'
              }
            }
         },
         yAxis: {
            title: {
               text: ']]><xsl:value-of select="i18n:translate('stats.count')" /><![CDATA['
            }
         },
         tooltip: {
           formatter: function() {
              return '<b>'+ this.x +'</b>: '+ this.y;
           }
           
         },
         legend: {
            enabled: false
         },         
         series: [{
              name: ']]><xsl:value-of select="@name" /><![CDATA[',
              data: [
                ]]>
                 <xsl:for-each select="row">
                    <xsl:value-of select="@num"/>,
                 </xsl:for-each>
                <![CDATA[
              ],
              dataLabels: {
                enabled: true,
                rotation: -90,
                color: Highcharts.theme.dataLabelsColor || '#FFFFFF',
                align: 'right',
                x: -3,
                y: 10,
                formatter: function() {
                   return this.y;
                },
                style: {
                   font: 'normal 13px Verdana, sans-serif'
                }
              }
          }
        ]
    });
 });
            ]]>
    </script>

	</xsl:template>
  
</xsl:stylesheet>