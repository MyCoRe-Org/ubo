<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl mods"
>

    <xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]" mode="pageTitle">
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js"/>
        <xsl:if test="$permission.admin">
            <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
                <xsl:for-each select="mods:name[@type='personal'][1]">
                    <xsl:apply-templates select="mods:namePart[@type='family']"/>
                    <xsl:apply-templates select="mods:namePart[@type='given']"/>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="steps.and.actions.modsperson">
        <xsl:choose>
            <xsl:when test="$step='ask.delete.modsperson'">
                <xsl:call-template name="ask.delete" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="mycoreobject/@ID[contains(.,'_modsperson_')]" mode="breadcrumb">
        <xsl:text>Person Nr. </xsl:text>
        <xsl:value-of select="number(substring-after(.,'_modsperson_'))"/>
    </xsl:template>

    <xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]">
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js"/>
        <xsl:variable name="modsperson_id" select="@ID"/>
        <xsl:choose>
            <xsl:when test="$permission.admin">
                <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
                    <xsl:call-template name="steps.and.actions.modsperson" />
                    <div class="section row m-1">
                        <div class="col pl-0">
                            <div class="row">
                                <div class="col">
                                    <xsl:apply-templates select="/mycoreobject/service/servflags/servflag[@type='status']" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="section">
                        <div class="ubo_details card mb-3">
                            <div class="card-body">
                                <xsl:for-each select="mods:name[@type='personal']">
                                    <xsl:apply-templates select="." mode="modsperson"/>
                                    <xsl:apply-templates select="mods:nameIdentifier" mode="modsperson"/>
                                    <xsl:apply-templates select="mods:alternativeName" mode="modsperson"/>
                                    <xsl:apply-templates select="mods:affiliation" mode="modsperson"/>
                                </xsl:for-each>
                            </div>
                        </div>
                        <div class="ubo_details card mb-3">
                            <div class="card-body">
                                <xsl:call-template name="publications">
                                    <xsl:with-param name="modsperson_id" select="$modsperson_id" />
                                </xsl:call-template>
                            </div>
                        </div>
                    </div>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <h3>
                    <xsl:value-of select="i18n:translate('navigation.notAllowedToSeeThisPage')"/>
                </h3>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="mycoreobject/@ID[contains(.,'_modsperson_')]" mode="actions">
        <xsl:if test="$permission.admin">
            <div id="buttons" class="btn-group mb-3 flex-wrap">
                <xsl:if test="not($UBO.System.ReadOnly = 'true')">
                    <xsl:if test="(string-length($step) = 0) or contains($step,'merged')">
                        <a class="action btn btn-sm btn-outline-primary mb-1"
                           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-person.xed&amp;id={/mycoreobject/@ID}">
                            <xsl:value-of select="i18n:translate('button.edit')"/>
                        </a>
                        <a class="action btn btn-sm btn-outline-primary mb-1"
                           href="{$WebApplicationBaseURL}servlets/MCRLockServlet?url=../edit-mods.xed&amp;id={/mycoreobject/@ID}">
                            MODS
                        </a>
                    </xsl:if>
                    <xsl:if test="string-length($step) = 0">
                        <xsl:if test="not(/mycoreobject/structure/children/child)">
                            <a class="action btn btn-sm btn-outline-primary mb-1"
                               href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.step=ask.delete.modsperson">
                                <xsl:value-of select="i18n:translate('button.delete')"/>
                            </a>
                        </xsl:if>
                        <a class="action btn btn-sm btn-outline-primary mb-1"
                           href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.Style=modsperson-structure">
                            <xsl:value-of select="i18n:translate('button.structure')"/>
                        </a>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="$step='confirm.submitted'">
                    <a class="action btn btn-sm btn-outline-primary mb-1"
                       href="{$WebApplicationBaseURL}servlets/DozBibEntryServlet?mode=show&amp;id={/mycoreobject/@ID}">
                        <xsl:value-of select="i18n:translate('button.preview')"/>
                    </a>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mods:name" mode="modsperson">
        <div class="row">
            <div class="col-3">
                <xsl:value-of select="i18n:translate('result.dozbib.name')"/>
                <xsl:text>:</xsl:text>
            </div>
            <div class="col-9">
                <xsl:apply-templates select="."/>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="mods:nameIdentifier" mode="modsperson">
        <xsl:variable name="type" select="@type"/>
        <xsl:variable name="classNode" select="$nameIdentifierClassification//category[@ID=$type]"/>

        <div class="row">
            <div class="col-3">
                <xsl:value-of select="$classNode/label[lang($CurrentLang)]/@text"/>
                <xsl:text>:</xsl:text>
            </div>
            <div class="col-9">
                <xsl:apply-templates select="." mode="value">
                    <xsl:with-param name="classNode" select="$classNode"/>
                </xsl:apply-templates>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="mods:alternativeName" mode="modsperson">
        <div class="row">
            <div class="col-3">
                <xsl:value-of select="i18n:translate('result.dozbib.alternativeName')"/>
                <xsl:text>:</xsl:text>
            </div>
            <div class="col-9">
                <xsl:apply-templates select="mods:namePart[@type='family']" />
                <xsl:apply-templates select="mods:namePart[@type='given']" />
            </div>
        </div>
    </xsl:template>

    <xsl:template match="mods:affiliation" mode="modsperson">
        <div class="row">
            <div class="col-3">
                <xsl:value-of select="i18n:translate('ubo.person.affiliation')"/>
                <xsl:text>:</xsl:text>
            </div>
            <div class="col-9">
                <xsl:value-of select="text()" />
            </div>
        </div>
    </xsl:template>

    <xsl:template name="publications">
        <xsl:param name="modsperson_id" />
        <article class="card mb-3" xml:lang="de">
            <div class="card-body">
                <h3>
                    <xsl:value-of select="i18n:translate('user.profile.publications')" />
                </h3>
                <ul>
                    <xsl:call-template name="numPublicationsModsperson" >
                        <xsl:with-param name="modsperson_id" select="$modsperson_id" />
                    </xsl:call-template>
                </ul>
            </div>
        </article>
    </xsl:template>

    <xsl:template name="numPublicationsModsperson">
        <xsl:param name="modsperson_id" />
        <xsl:variable name="solr_query_confirmed" select="concat('q=status%3Aconfirmed+objectType%3Amods+ref_person%3A',$modsperson_id)" />
        <xsl:variable name="solr_query_all" select="concat('q=objectType%3Amods+ref_person%3A',$modsperson_id)" />
        <xsl:variable name="numFoundConfirmed" select="document(concat('solr:rows=0&amp;',$solr_query_confirmed))/response/result/@numFound"/>
        <xsl:variable name="numFoundAll" select="document(concat('solr:rows=0&amp;',$solr_query_all))/response/result/@numFound"/>

        <xsl:variable name="numPubsConfirmedText">
            <xsl:call-template name="numPublications">
                <xsl:with-param name="num" select="$numFoundConfirmed" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="numPubsAllText">
            <xsl:call-template name="numPublications">
                <xsl:with-param name="num" select="$numFoundAll" />
            </xsl:call-template>
        </xsl:variable>

        <li>
            <xsl:value-of select="concat(i18n:translate('user.profile.publications.ubo.intro'), ' ')" />
            <a href="{$ServletsBaseURL}solr/select?{$solr_query_all}&amp;sort=year+desc">
                <xsl:value-of select="$numPubsAllText"/>
                <xsl:value-of select="concat(i18n:translate(concat('user.profile.publications.ubo.outro.plural.modsperson.', $numFoundAll &gt; 1)), '.')" />
            </a>

            <xsl:if test="$numFoundAll &gt; 1 and $numFoundConfirmed != $numFoundAll">
                <xsl:variable name="isMulti" select="($numFoundConfirmed = 0 or $numFoundConfirmed &gt; 1)"/>

                <xsl:value-of select="concat(' ', i18n:translate(concat('user.profile.publications.ubo.published.intro.plural.', $isMulti)), ' ')"/>
                <a href="{$ServletsBaseURL}solr/select?{$solr_query_confirmed}&amp;sort=year+desc">
                    <xsl:value-of select="$numPubsConfirmedText"/>
                </a>
                <xsl:value-of select="concat(' ', i18n:translate(concat('user.profile.publications.ubo.published.extro.plural.', $isMulti)))" disable-output-escaping="yes" />
            </xsl:if>
        </li>
    </xsl:template>

    <xsl:template name="numPublications">
        <xsl:param name="num" />
        <xsl:choose>
            <xsl:when test="$num = 0">
                <xsl:value-of select="i18n:translate('user.profile.publications.num.none')" />
            </xsl:when>
            <xsl:when test="$num = 1">
                <xsl:value-of select="i18n:translate('user.profile.publications.num.one')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="i18n:translate('user.profile.publications.num.multiple',$num)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
