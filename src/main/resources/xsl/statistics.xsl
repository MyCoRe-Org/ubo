<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  exclude-result-prefixes="xsl xalan i18n">

  <xsl:param name="CurrentLang" />

  <xsl:variable name="count" select="concat(i18n:translate('stats.count'),' ',i18n:translate('ubo.publications'))" />

  <xsl:template match="/response">
    <xinclude>
      <xsl:for-each select="lst[@name='facet_counts']">
        <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='year'][int]" />
        <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='subject'][int]" />
        <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='genre'][int]" />
        <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='facet_person'][int]" />
        <xsl:apply-templates select="lst[@name='facet_pivot']/arr[@name='nid_type,nid_type']" />
      </xsl:for-each>
    </xinclude>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='year']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.year'))" />
    
    <div id="chartYear" style="width:100%; height:350px;" />

    <script type="text/javascript">
      jQuery(document).ready(function() {
        new Highcharts.Chart({
          chart: {
            renderTo: 'chartYear',
            defaultSeriesType: 'column',
            events: {
              click: function(e) {
                jQuery('#chartDialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 80,
                  height: jQuery(window).height() - 80,
                  draggable: false,
                  resizable: false,
                  modal: false
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chartDialog';
                dialogOptions.chart.events = null;
                dialogOptions.chart.zoomType = 'x';
                new Highcharts.Chart(dialogOptions);
              }
            }
          },
          title: { text: '<xsl:value-of select="$title" />' },
          legend: { enabled: false },
          tooltip: { formatter: function() { return '<b>' + Highcharts.dateFormat('%Y', this.point.x) +'</b>: '+ this.point.y; } },
          xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: { day: '%Y' }
          },
           yAxis: {
             title: { text: '<xsl:value-of select="$count" />' },
             labels: { formatter: function() { return this.value; } },
             endOnTick: false,          
             max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->          
           },
           plotOptions: {
              column: {
                pointPadding: 0.2,
                borderWidth: 0
              }
           },
           series: [{
                name: '<xsl:value-of select="$count" />',
                data: [
                  <xsl:for-each select="int">
                    <xsl:sort select="@name" data-type="number" order="ascending" />
                    [Date.UTC(<xsl:value-of select="@name"/>, 0, 1), <xsl:value-of select="text()"/>]
                    <xsl:if test="position() != last()">, </xsl:if>
                  </xsl:for-each>
                ]
            }]
          });
        });
    </script>
  </xsl:template>
  
  <xsl:variable name="subjects" select="document('resource:fachreferate.xml')/fachreferate" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='subject']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.subject'))" />

    <div id="chartSubject" style="width:100%; height:{50 + count(int) * 30}px" />

    <script type="text/javascript">
      jQuery(document).ready(function() {
        new Highcharts.Chart({
          chart: {
            renderTo: 'chartSubject',
            type: 'bar',
            events: {
              click: function(e) {
                jQuery('#chartDialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 80,
                  height: jQuery(window).height() - 80,
                  draggable: false,
                  resizable: false,
                  modal: false
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chartDialog';
                dialogOptions.chart.events = null;
                dialogOptions.chart.zoomType = 'x';
                new Highcharts.Chart(dialogOptions);
              }
            }
          },
          title: { text: '<xsl:value-of select="$title" />' },
          legend: { enabled: false },
          xAxis: { categories: [
            <xsl:for-each select="int">
              <xsl:sort select="text()" data-type="number" order="descending" />
              '<xsl:value-of select="$subjects/item[@value=current()/@name]/@label"/>'
              <xsl:if test="position() != last()">, </xsl:if>
            </xsl:for-each>
            ],
            labels: {
              align: 'right',
              style: { font: 'normal 13px Verdana, sans-serif' }
            }
          },
          yAxis: {
             title: { text: '<xsl:value-of select="$count" />' },
             labels: { formatter: function() { return this.value; } },
             endOnTick: false,          
             max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->          
          },
          tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
          plotOptions: { series: { pointWidth: 15 } },
          series: [{
            name: '<xsl:value-of select="$title" />',
            data: [
              <xsl:for-each select="int">
                <xsl:sort select="text()" data-type="number" order="descending" />
                <xsl:value-of select="text()"/>
                <xsl:if test="position() != last()">, </xsl:if>
              </xsl:for-each>
            ],
            dataLabels: {
              enabled: true,
              align: 'right', 
              formatter: function() { return this.y; },
              style: { font: 'normal 15px Verdana, sans-serif' }
            }
          }]
        });
      });
    </script>
  </xsl:template>

  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='genre']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.genre'))" />

    <div id="chartGenre" style="width:100%; height:350px" />
    
    <script type="text/javascript">
     jQuery(document).ready(function() {
       Highcharts.getOptions().plotOptions.pie.colors = ['#4572A7','#AA4643','#89A54E','#80699B','#3D96AE','#DB843D','#92A8CD','#A47D7C','#B5CA92'];
       new Highcharts.Chart({
         chart: {
            renderTo: 'chartGenre',         
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            defaultSeriesType: 'pie',
            events: {
              click: function(e) {
                jQuery('#chartDialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 80,
                  height: jQuery(window).height() - 80,
                  draggable: false,
                  resizable: false,
                  modal: false
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chartDialog';
                dialogOptions.chart.events = null;
                dialogOptions.chart.zoomType = 'x';
                new Highcharts.Chart(dialogOptions);
              }
            }
         },
         title: { text: '<xsl:value-of select="$title" />' },
         legend: { enabled: false },
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
                 formatter: function() {
                    return '<b>'+ this.point.name +'</b>: '+ this.y;
                 }
              }
            }
         },
         series: [{
              name: '<xsl:value-of select="$title" />',
              data: [
                <xsl:for-each select="int">
                  <xsl:sort select="text()" data-type="number" order="descending" />
                  ['<xsl:value-of select="$genres//category[@ID=current()/@name]/label[lang($CurrentLang)]/@text"/>' , <xsl:value-of select="text()"/>]
                  <xsl:if test="position()!=last()">,</xsl:if>
                </xsl:for-each>
              ]
          }
        ]
    });
 });
    </script>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='facet_person']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.person'))" />

    <div id="chartPerson" style="width:100%; height:{50 + count(int) * 30}px" />
    
    <script type="text/javascript">
      jQuery(document).ready(function() {
        new Highcharts.Chart({
          chart: {
            renderTo: 'chartPerson',
            type: 'bar',
            events: {
              click: function(e) {
                jQuery('#chartDialog').dialog({
                  position: 'center',
                  width: jQuery(window).width() - 80,
                  height: jQuery(window).height() - 80,
                  draggable: false,
                  resizable: false,
                  modal: false
                });
                var dialogOptions = this.options;
                dialogOptions.chart.renderTo = 'chartDialog';
                dialogOptions.chart.events = null;
                dialogOptions.chart.zoomType = 'x';
                new Highcharts.Chart(dialogOptions);
              }
            }
          },
          title: { text: '<xsl:value-of select="$title" />' },
          legend: { enabled: false },
          xAxis: { categories: [
            <xsl:for-each select="int">
              <xsl:sort select="text()" data-type="number" order="descending" />
              "<xsl:value-of select="@name"/>"
              <xsl:if test="position() != last()">, </xsl:if>
            </xsl:for-each>
            ],
            labels: {
              align: 'right',
              style: { font: 'normal 13px Verdana, sans-serif' }
            }
          },
          yAxis: {
             title: { text: '<xsl:value-of select="$count" />' },
             labels: { formatter: function() { return this.value; } },
             endOnTick: false,          
             max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->          
          },
          tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
          plotOptions: { series: { pointWidth: 15 } },
          series: [{
            name: '<xsl:value-of select="$title" />',
            data: [
              <xsl:for-each select="int">
                <xsl:sort select="text()" data-type="number" order="descending" />
                <xsl:value-of select="text()"/>
                <xsl:if test="position() != last()">, </xsl:if>
              </xsl:for-each>
            ],
            dataLabels: {
              enabled: true,
              align: 'right', 
              formatter: function() { return this.y; },
              style: { font: 'normal 15px Verdana, sans-serif' }
            }
          }]
        });
      });
    </script>
  </xsl:template>

  <xsl:template match="lst/arr">
    <xsl:variable name="base" select="." />

    <table class="ubo-chart"> 
      <tr>
        <th>/</th>
        <xsl:for-each select="$base/lst">
          <th>
            <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
          </th>
        </xsl:for-each>
      </tr>
      <xsl:for-each select="$base/lst">
        <xsl:variable name="a" select="str[@name='value']" />
        <tr>
          <th class="identifier">
            <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
          </th>
          <xsl:for-each select="$base/lst">
            <xsl:variable name="b" select="str[@name='value']" />
            <td class="identifier">
              <xsl:value-of select="$base/lst[str[@name='value']=$a]/arr/lst[str[@name='value']=$b]/int[@name='count']" />
            </td>
          </xsl:for-each>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>