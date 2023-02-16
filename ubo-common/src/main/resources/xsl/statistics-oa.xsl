<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n">

  <xsl:param name="CurrentLang" />

  <xsl:variable name="title" select="i18n:translate('stats.oa.title')" />
  <xsl:variable name="yAxis" select="i18n:translate('stats.count')" />
  <xsl:variable name="ofTotal" select="i18n:translate('stats.ofTotal')" />
  <xsl:variable name="oaGeneral" select="i18n:translate('stats.oa.unspecified')" />
  <xsl:variable name="labelNoOA" select="i18n:translate('stats.oa.notOA')" />
  <xsl:variable name="colorNoOA">#5858FA</xsl:variable>

  <xsl:variable name="categories" select="document('classification:metadata:-1:children:oa')/mycoreclass/categories" />
  <xsl:variable name="facets" select="/response/lst[@name='facets']/lst[@name='year']" />

  <xsl:template match="/">
    <xsl:apply-templates select="." mode="oa-statistics"/>
  </xsl:template>

  <xsl:template match="/" mode="oa-statistics">
    <xsl:for-each select="$facets">
      <section class="card mb-3">
        <div class="card-body">
          <div id="chartOA" style="width:100%; height:600px" />
        </div>

        <script type="text/javascript">
           $(document).ready(function() {
            Highcharts.chart( 'chartOA', {

                chart: { type: 'column',
                events: {
                  click: function(e) {
                    $('#chartDialog').dialog({
                      position: 'center',
                      width: $(window).width() - 40,
                      height: $(window).height() - 40,
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
                title: { text: '<xsl:value-of select="$title" />'    },
                xAxis: { categories: [ <xsl:apply-templates select="arr[@name='buckets']" mode="years"/> ] },

                yAxis: {
                    min: 0,
                    title: { text: '<xsl:value-of select="$yAxis" />' },
                    stackLabels: {
                        enabled: true,
                        style: {
                            color: ( Highcharts.defaultOptions.title.style &amp;&amp; Highcharts.defaultOptions.title.style.color ) || 'gray'
                        }
                    }
                },
                legend: {
                    align: 'right',
                    x: -30,
                    verticalAlign: 'top',
                    y: 25,
                    floating: true,
                    backgroundColor: Highcharts.defaultOptions.legend.backgroundColor || 'white',
                    borderColor: '#CCC',
                    borderWidth: 1,
                    shadow: false
                },
                tooltip: {
                    headerFormat: '&lt;b&gt;{point.x}&lt;/b&gt;&lt;br/&gt;',
                    pointFormat: '{series.name}: {point.y} ({point.percentage:.1f} %)&lt;br/&gt;<xsl:value-of select="$ofTotal"/> {point.stackTotal}&lt;br/&gt;'
                },
                plotOptions: {
                    column: {
                        stacking: 'normal',
                        dataLabels: {
                          enabled: true,
                          allowOverlap: false,
                          format: '{y} ({point.percentage:.1f} %)'
                       }
                    }
                },
                colors: [
                  '<xsl:value-of select="$colorNoOA" />'
                ],
                series: [
                  <xsl:call-template name="seriesNoOA" />
                  <xsl:text>, </xsl:text>
                  <xsl:apply-templates select="$categories/category" mode="series" />
                ]
            });

           });
        </script>
      </section>
    </xsl:for-each>
  </xsl:template>

  <xsl:variable name="apos">'</xsl:variable>

  <xsl:template name="seriesNoOA">
    {
      name: '<xsl:value-of select="$labelNoOA" />',
      data: [
        <xsl:for-each select="$facets/arr[@name='buckets']/lst">
          <!-- #unspecified = #totalPub - #inCategoryOAwhichIncludesSubCat - #closedAccess -->
          <xsl:value-of select="int[@name='count']" /> 
          <xsl:for-each select="lst[@name='oa']/arr[@name='buckets']/lst[(str[@name='val']='oa') or (str[@name='val']='closed')]">
            <xsl:text> - </xsl:text>
            <xsl:value-of select="int[@name='count']" />
          </xsl:for-each>
          <xsl:if test="position() != last()">, </xsl:if>
        </xsl:for-each>
      ]
    }
  </xsl:template>

  <xsl:template match="arr[@name='buckets']" mode="years">
    <xsl:for-each select="lst">
      <xsl:value-of select="concat($apos,int[@name='val'],$apos)" />
      <xsl:if test="position() != last()">, </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="category" mode="color">
    <xsl:value-of select="concat($apos,label[lang('x-color')]/@text,$apos)" />
    <xsl:if test="category|following::category">, </xsl:if>
    <xsl:apply-templates select="category" mode="color" />
  </xsl:template>

  <xsl:template match="category" mode="series">
    <xsl:text>{ </xsl:text>
    <xsl:apply-templates select="." mode="label" />
    <xsl:text>, data: [</xsl:text>
    <xsl:apply-templates select="." mode="values" />
    <xsl:text>], className: &quot;</xsl:text>
    <xsl:value-of select="concat('ubo-statistic-oa-', @ID)"/>
    <xsl:text>&quot; }</xsl:text>

    <xsl:if test="category|following::category">, </xsl:if>
    <xsl:apply-templates select="category" mode="series" />
  </xsl:template>

  <xsl:template match="category" mode="label">
    <xsl:value-of select="concat('name: ',$apos,label[lang($CurrentLang)]/@text,$apos)" />
  </xsl:template>

  <xsl:template match="category[@ID='oa']" mode="label">
    <xsl:value-of select="concat('name: ',$apos,label[lang($CurrentLang)]/@text,$oaGeneral,$apos)" />
  </xsl:template>

  <xsl:template match="category" mode="values">
    <xsl:apply-templates select="$facets/arr[@name='buckets']/lst" mode="value">
      <xsl:with-param name="category" select="." />
    </xsl:apply-templates>
  </xsl:template>

  <!-- output single value of a single year -->
  <xsl:template match="lst" mode="value">
    <xsl:param name="category" />

    <xsl:choose>
      <xsl:when test="lst[@name='oa']/arr[@name='buckets']/lst[str[@name='val']=$category/@ID]/int[@name='count']">
        <xsl:for-each select="lst[@name='oa']/arr[@name='buckets']">
          <xsl:value-of select="lst[str[@name='val']=$category/@ID]/int[@name='count']" /> <!-- category's value -->
          <xsl:for-each select="lst">
            <xsl:if test="$category/category[@ID=current()/str[@name='val']]">
               <!-- let JavaScript substract the values of this child category -->
              <xsl:text> - </xsl:text>
              <xsl:value-of select="int[@name='count']" />
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>

    <xsl:if test="position() != last()">, </xsl:if>
  </xsl:template>

</xsl:stylesheet>
