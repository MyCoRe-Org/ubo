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
        <xsl:if test="$permission.admin">
            <xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">
                <xsl:for-each select="mods:name[@type='personal'][1]">
                    <xsl:apply-templates select="mods:namePart[@type='family']"/>
                    <xsl:apply-templates select="mods:namePart[@type='given']"/>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>


    <xsl:template match="mycoreobject/@ID[contains(.,'_modsperson_')]" mode="breadcrumb">
        <xsl:text>Person Nr. </xsl:text>
        <xsl:value-of select="number(substring-after(.,'_modsperson_'))"/>
    </xsl:template>

    <xsl:template match="mycoreobject[contains(@ID,'_modsperson_')]">
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/ModsDisplayUtils.js"/>
        <xsl:choose>
            <xsl:when test="$permission.admin">
                <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
                    <div class="section">
                        <div class="ubo_details card">
                            <div class="card-body">
                                <xsl:for-each select="mods:name[@type='personal']">
                                    <xsl:apply-templates select="." mode="modsperson"/>
                                    <xsl:apply-templates select="mods:nameIdentifier" mode="modsperson"/>
                                    <xsl:apply-templates select="mods:alternativeName" mode="modsperson"/>
                                    <xsl:apply-templates select="mods:affiliation" mode="modsperson"/>
                                </xsl:for-each>
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
                               href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.step=ask.delete">
                                <xsl:value-of select="i18n:translate('button.delete')"/>
                            </a>
                        </xsl:if>
                        <a class="action btn btn-sm btn-outline-primary mb-1"
                           href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}&amp;XSL.Style=structure">
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

</xsl:stylesheet>
