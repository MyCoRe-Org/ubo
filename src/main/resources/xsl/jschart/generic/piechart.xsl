<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n">
	<xsl:template match="table[@charttype='Piechart']" mode="chart">
		
    <script type="text/javascript">
    <![CDATA[
jQuery(document).ready(function() {
      
      new Highcharts.Chart({
         chart: {
            renderTo: 'ubo-chart-]]><xsl:value-of select="@id" /><![CDATA[',         
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            defaultSeriesType: 'pie',
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
                new Highcharts.Chart(dialogOptions);
              }
            }
         },
         title: {
            text: ']]><xsl:value-of select="@name" /><![CDATA['
         },
         tooltip: {
            formatter: function() {
              return '<b>'+ this.point.name +'</b>: '+ this.y;
            }
       
         },
         plotOptions: {
            pie: {
              allowPointSelect: true,
              cursor: 'pointer',
              dataLabels: {
                 enabled: true,
                 color: Highcharts.theme.textColor || '#000000',
                 connectorColor: Highcharts.theme.textColor || '#000000',
                 formatter: function() {
                    return '<b>'+ this.point.name +'</b>: '+ this.y;
                 }
              }
            }
         },

         
         series: [{
              name: ']]><xsl:value-of select="@name" /><![CDATA[',
              data: [
                ]]>
                <xsl:for-each select="row">
                  ['<xsl:value-of select="@label"/>' , <xsl:value-of select="@num"/>]
                  <xsl:if test="position()!=last()">,</xsl:if>
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