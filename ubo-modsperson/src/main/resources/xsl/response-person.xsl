<?xml version="1.0" encoding="UTF-8"?>

<!-- Displays a navigable result list of a SOLR search for bibliography entries -->

<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
        xmlns:mods="http://www.loc.gov/mods/v3"
        xmlns:mcr="http://www.mycore.org/"
        xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:str="xalan://java.lang.String"
        xmlns:basket="xalan://org.mycore.ubo.basket.BasketUtils"
        exclude-result-prefixes="xsl xalan i18n mods mcr mcrxml encoder str basket">

    <xsl:param name="RequestURL"/>

    <xsl:decimal-format name="WesternEurope" decimal-separator="," grouping-separator="."/>

    <xsl:template name="page.title.person">
        <title>
            <xsl:value-of select="i18n:translate('result.dozbib.results')"/>
            <xsl:text>: </xsl:text>
            <xsl:choose>
                <xsl:when test="$numFound > 1">
                    <xsl:value-of select="format-number($numFound, '##.###', 'WesternEurope')"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="i18n:translate('result.dozbib.personMany')"/>
                </xsl:when>
                <xsl:when test="$numFound = 1">
                    <xsl:value-of select="i18n:translate('result.dozbib.personOne')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="i18n:translate('result.dozbib.personNo')"/>
                </xsl:otherwise>
            </xsl:choose>
        </title>
    </xsl:template>

    <xsl:template match="response" mode="person">
        <xsl:apply-templates select="result[@name='response']" mode="person"/>
    </xsl:template>

    <xsl:template match="result[@name='response']" mode="person">
        <script type="text/javascript"
                src="{$WebApplicationBaseURL}external/datatables-1.8.2/media/js/jquery.dataTables.min.js">
        </script>
        <script type="text/javascript" src="{$WebApplicationBaseURL}external/jquery.jsonp.js"></script>
        <script type="text/javascript">
            jQuery(document).ready( function() {
            jQuery('#trefferliste').addClass('jQDataTable').css('display', 'table');

            oTable = jQuery('#trefferliste').dataTable({
            "bAutoWidth": true,
            "bPaginate": true,
            "bJQueryUI": true,
            "sDom": '&lt;lr&gt;t',
            "bStateSave": true,
            "iCookieDuration": 0,
            "aoColumns": [
            {  "bSortable": true },
            { "bSortable": true },
            { "bSortable": true },
            { "bSortable": true },
            { "bSortable": true },
            { "bSortable": true },
            ],
            "oLanguage": {
            "sSearch": "<xsl:value-of select="i18n:translate('search.jq.datatable')"/>"
            }
            } );

            });
        </script>

        <div>
            <xsl:if test="$numFound = 0">
                <p>
                    <xsl:value-of select="i18n:translate('result.dozbib.personNo')"/>
                </p>
            </xsl:if>
            <xsl:if test="$numFound &gt; 0">
                <div class="results">
                    <table id="trefferliste">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>LSF-ID</th>
                                <th>Scopus-ID</th>
                                <th>Orcid-ID</th>
                                <th>GND-ID</th>
                                <th>Researcher-ID</th>
                            </tr>
                        </thead>
                        <tbody>
                            <xsl:apply-templates select="doc" mode="person">
                                <xsl:with-param name="start" select="@start"/>
                            </xsl:apply-templates>
                        </tbody>
                    </table>

                </div>
            </xsl:if>
        </div>
        <div class="clear"></div>
    </xsl:template>

    <xsl:template match="doc" mode="person">
        <xsl:param name="start"/>
        <xsl:variable name="hitNo" select="$start + position()"/>
        <xsl:variable name="id" select="str[@name='id']" />
        <xsl:variable name="mycoreobject" select="document(concat('mcrobject:',$id))/mycoreobject" />
        <xsl:for-each select="$mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
            <tr>
                <td>  <!-- Name -->
                    <xsl:variable name="family" select="mods:name[@type='personal']/mods:namePart[@type='family']" />
                    <xsl:variable name="given" select="mods:name[@type='personal']/mods:namePart[@type='given']" />
                    <a href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}">
                        <xsl:value-of select="$family"/>, <xsl:value-of select="$given"/>
                    </a>

                </td>
                <td>  <!-- LDF-ID -->
                    <xsl:variable name="lsf" select="mods:name[@type='personal']/mods:nameIdentifier[@type='lsf']" />
                    <xsl:value-of select="$lsf"/>
                </td>
                <td>  <!-- Scopus-ID -->
                    <xsl:variable name="scopus" select="mods:name[@type='personal']/mods:nameIdentifier[@type='scopus']" />
                    <xsl:value-of select="$scopus"/>
                </td>
                <td>  <!-- Orcid-ID -->
                    <xsl:variable name="orcid" select="mods:name[@type='personal']/mods:nameIdentifier[@type='orcid']" />
                    <xsl:value-of select="$orcid"/>
                </td>
                <td>  <!-- GND-ID -->
                    <xsl:variable name="gnd" select="mods:name[@type='personal']/mods:nameIdentifier[@type='gnd']" />
                    <xsl:value-of select="$gnd"/>
                </td>
                <td>  <!-- Researcher-ID -->
                    <xsl:variable name="researcher" select="mods:name[@type='personal']/mods:nameIdentifier[@type='researcherid']" />
                    <xsl:value-of select="$researcher"/>
                </td>
            </tr>
        </xsl:for-each>

    </xsl:template>


</xsl:stylesheet>
