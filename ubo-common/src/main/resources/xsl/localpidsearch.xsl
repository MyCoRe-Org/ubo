<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
        exclude-result-prefixes="xsl xalan i18n"
>

    <xsl:param name="WebApplicationBaseURL" />
    <xsl:param name="ServletsBaseURL" />
    <xsl:param name="MCR.user2.matching.lead_id" />
    <xsl:param name="MCR.user2.IdentityManagement.UserCreation.Affiliation" />

    <xsl:template match="/">
        <html>
            <head>
                <title>
                    <xsl:value-of select="i18n:translate('lsf.search')" />
                </title>
                <script type="text/javascript" src="{$WebApplicationBaseURL}external/datatables-1.8.2/media/js/jquery.dataTables.min.js"><xsl:text> </xsl:text></script>
                <script type="text/javascript" src="{$WebApplicationBaseURL}external/jquery.jsonp.js"><xsl:text> </xsl:text></script>
                <script type="text/javascript">
                    jQuery(document).ready( function() {
                    jQuery('#lsfpersons').addClass('jQDataTable').css('display', 'table');

                    oTable = jQuery('#lsfpersons').dataTable({
                    "bAutoWidth": false,
                    "bPaginate": false,
                    "bJQueryUI": true,
                    "sDom": '&lt;lr&gt;t',
                    "bStateSave": true,
                    "iCookieDuration": 0,
                    "aoColumns": [
                    { "sWidth": "100px;", "sType": "html", "bSortable": false },
                    { "sWidth": "250px;", "sType": "html", "bSortable": false },
                    { "sWidth": "390px;", "bSortable": false }
                    ],
                    "oLanguage": {
                    "sSearch": "<xsl:value-of select="i18n:translate('search.jq.datatable')"/>"
                    }
                    } );
                    });
                </script>
            </head>
            <body>
                <xsl:call-template name="breadcrumb" />
                <xsl:apply-templates select="localpidsearch" />
            </body>
        </html>
    </xsl:template>

    <!--  navigation  -->
    <xsl:template name="breadcrumb">
        <ul id="breadcrumb">
            <li>Suche im Personalverzeichnis der THK</li>
        </ul>
    </xsl:template>

    <xsl:variable name="qm">'</xsl:variable>
    <xsl:variable name="xpathID" select="encoder:encode(concat('mods:nameIdentifier[@type=',$qm,$MCR.user2.matching.lead_id,$qm,']'),'UTF-8')" />
    <xsl:variable name="xpathFamily" select="encoder:encode(concat('mods:namePart[@type=',$qm,'family',$qm,']'),'UTF-8')" />
    <xsl:variable name="xpathGiven"  select="encoder:encode(concat('mods:namePart[@type=',$qm,'given',$qm,']'),'UTF-8')" />
    <xsl:variable name="xpathAffiliation"  select="encoder:encode('mods:affiliation')" />

    <!--  Suche im Personalverzeichnis der THK  -->

    <xsl:template match="localpidsearch">

        <article class="card mb-2">
            <div class="card-body bg-alternative">
                <p>
                    <xsl:value-of select="i18n:translate('lsf.searchText.1')"/>
                    <xsl:if test="not(contains(@referrer,'list-wizard'))">
                        <xsl:value-of select="i18n:translate('lsf.searchText.2')"/>
                    </xsl:if>
                </p>
            </div>
        </article>

        <div class="card mb-2">
            <div class="card-body">
                <form action="identitypicker.html" method="post" role="form">
                    <input type="hidden" name="_xed_subselect_session" value="{@session}" />
                    <input type="hidden" name="_referrer" value="{@referrer}" />

                    <div class="form-group form-inline">
                        <label for="lastName" class="mycore-form-label">
                            <xsl:value-of select="i18n:translate('lsf.searchFor')"/>
                        </label>
                        <input id="lastName" name="term" type="text" size="40" value="{@term}" class="mycore-form-input"/>
                    </div>

                    <!-- Allow manual input of LSF PID for admins, in special cases or for testing purposes -->
                    <xsl:if xmlns:check="xalan://org.mycore.ubo.AccessControl" test="check:currentUserIsAdmin()">
                        <div class="form-group form-inline">
                            <label for="pid" class="mycore-form-label">
                                DHSB-ID:
                            </label>
                            <input id="pid" name="pid" type="text" size="6" class="form-control col-sm-2"/>
                        </div>
                    </xsl:if>
                    <div class="cancel-submit form-group form-inline">
                        <label class="mycore-form-label">
                        </label>
                        <input type="submit" class="btn btn-primary mr-2" name="search" value="{i18n:translate('button.search')}" />
                        <input type="submit" class="btn btn-primary" name="cancel" value="{i18n:translate('button.cancel')}" />
                    </div>
                    <xsl:if test="results and not(contains(@referrer,'list-wizard'))">
                        <div class="cancel-submit form-group form-inline">
                            <label class="mycore-form-label">
                            </label>
                            <input type="submit" class="btn btn-primary" name="notLSF" value="{i18n:translate('lsf.buttonFree')}" />
                        </div>
                    </xsl:if>
                </form>
            </div>
        </div>

        <xsl:for-each select="results">
            <div class="section" id="sectionlast">

                <xsl:choose>
                    <xsl:when test="person">
                        <p>
                            <strong>
                                <xsl:value-of select="i18n:translate('lsf.found')"/>
                            </strong>
                        </p>

                        <p>
                            <table id="lsfpersons">
                                <xsl:apply-templates select="person">
                                    <xsl:sort select="name" />
                                </xsl:apply-templates>
                            </table>
                        </p>

                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">background-color:#800000; color:white; padding:1ex;</xsl:attribute>
                        <p>
                            <strong>
                                <xsl:value-of select="i18n:translate('lsf.foundNot')"/>
                            </strong>
                        </p>
                    </xsl:otherwise>
                </xsl:choose>

            </div>

            <xsl:if test="not(contains(/lsfpidsearch/@referrer,'list-wizard'))">
                <article class="card">
                    <div class="card-body">
                        <p>
                            <strong>
                                <xsl:value-of select="i18n:translate('lsf.foundNotNow')"/>
                            </strong>
                        </p>
                        <ul>
                            <li>
                                <xsl:value-of select="i18n:translate('lsf.searchNew')"/>
                            </li>
                            <li>
                                <xsl:value-of select="i18n:translate('lsf.searchCancel')"/>
                            </li>
                            <li>
                                <xsl:value-of select="i18n:translate('lsf.nameFree')"/>
                            </li>
                        </ul>
                    </div>
                </article>
            </xsl:if>

        </xsl:for-each>

    </xsl:template>

    <xsl:template match="person">
        <tr>
            <td>
                <a class="roundedButton" href="{$ServletsBaseURL}XEditor?_xed_submit_return=&amp;_xed_session={/localpidsearch/@session}&amp;{$xpathID}={id/text()}&amp;{$xpathAffiliation}={encoder:encode(concat($MCR.user2.IdentityManagement.UserCreation.Affiliation, ' ',institute/text(),' ', institution/text(), ' ', faculty/text()),'UTF-8')}&amp;{$xpathFamily}={encoder:encode(lastName,'UTF-8')}&amp;{$xpathGiven}={encoder:encode(firstName,'UTF-8')}">
                    <xsl:value-of select="i18n:translate('lsf.selectPerson')"/>
                </a>
            </td>
            <td>
                <xsl:value-of select="realName" />
            </td>
            <td>

            </td>
        </tr>
    </xsl:template>


</xsl:stylesheet>
