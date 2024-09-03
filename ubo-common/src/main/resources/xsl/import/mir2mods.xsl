<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ This file is part of ***  M y C o R e  ***
  ~ See http://www.mycore.de/ for details.
  ~
  ~ MyCoRe is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ MyCoRe is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl mods">

    <xsl:param name="WebApplicationBaseURL"/>
    <xsl:param name="UBO.RepositoryBaseURL"/>

    <xsl:template match="/mycoreobject">
        <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods"/>
    </xsl:template>


    <xsl:template match="mods:mods">
        <xsl:copy>
            <xsl:call-template name="modsContent"/>
            <xsl:call-template name="recordInfo"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="modsContent">
        <xsl:apply-templates select="mods:genre"/>
        <xsl:apply-templates select="mods:titleInfo"/>
        <xsl:apply-templates select="mods:name"/>
        <xsl:apply-templates select="mods:abstract"/>

        <xsl:call-template name="modsLocation"/>

        <xsl:copy-of select="mods:originInfo"/>
        <!-- convert language to old format ? -->
        <xsl:copy-of select="mods:language"/>
        <xsl:copy-of select="mods:identifier"/>
        <xsl:copy-of select="mods:subject[count(mods:topic)&gt;0]"/>
        <xsl:apply-templates select="mods:relatedItem"/>
    </xsl:template>

    <xsl:template match="mods:abstract">
        <xsl:if test="string-length(text()) &gt; 0">
            <xsl:copy>
                <xsl:copy-of select="@xml:lang"/>
                <xsl:copy-of select="text()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xsl:template name="recordInfo">
        <mods:recordInfo>
            <mods:recordContentSource>
                <xsl:value-of select="$UBO.RepositoryBaseURL"/>
            </mods:recordContentSource>
            <mods:recordOrigin>
                <xsl:value-of select="concat($UBO.RepositoryBaseURL, 'receive/', /mycoreobject/@ID)"/>
            </mods:recordOrigin>
            <mods:recordIdentifier>
                <xsl:value-of select="/mycoreobject/@ID"/>
            </mods:recordIdentifier>
        </mods:recordInfo>
    </xsl:template>

    <xsl:template name="modsLocation">
        <xsl:choose>
            <xsl:when test="count(mods:location)&gt;0">
                <xsl:copy-of select="mods:location"/>
            </xsl:when>
            <xsl:otherwise>
                <mods:location>
                    <mods:url>
                        <xsl:choose>
                            <xsl:when test="count(ancestor::mods:relatedItem)=0 and not(local-name()='relatedItem')">
                                <xsl:value-of select="concat($UBO.RepositoryBaseURL,'receive/', /mycoreobject/@ID)"/>
                            </xsl:when>
                            <xsl:when test="string-length(@xlink:href)&gt;0">
                                <xsl:value-of select="concat($UBO.RepositoryBaseURL,'receive/', @xlink:href)"/>
                            </xsl:when>
                        </xsl:choose>
                    </mods:url>
                </mods:location>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="mods:name">
        <xsl:choose>
            <xsl:when test="contains(@authorityURI,'mir_institutes')">
                <!-- TODO: only do that for his?-->
                <xsl:variable name="uri" select="@valueURI"/>
                <xsl:variable name="type" select="substring-after($uri, '#')"/>
                <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN/ORIGIN#{$type}"
                                     authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
            </xsl:when>
            <xsl:otherwise>
                <mods:name>
                    <xsl:copy-of select="@type"/>
                    <xsl:copy-of select="mods:role"/>
                    <xsl:copy-of select="mods:namePart"/>
                    <xsl:copy-of select="mods:nameIdentifier"/>
                </mods:name>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="mods:titleInfo">
        <xsl:if test="@xlink:type='simple'">
            <xsl:copy>
                <mods:title>
                    <xsl:apply-templates select="mods:nonSort"/>
                    <xsl:value-of select="mods:title"/>
                </mods:title>
                <xsl:copy-of select="mods:subTitle"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mods:genre">
        <xsl:variable name="uri" select="@valueURI"/>
        <xsl:variable name="type" select="substring-after($uri, '#')"/>

        <xsl:if test="string-length($type) &gt; 0">
            <!-- todo: maybe need mapping -->
            <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre" valueURI="{$WebApplicationBaseURL}classifications/ubogenre#{$type}" />
        </xsl:if>
    </xsl:template>

    <xsl:template match="mods:relatedItem">
        <mods:relatedItem xlink:type="simple">
            <xsl:copy-of select="mods:part"/>
            <xsl:call-template name="modsContent"/>
        </mods:relatedItem>
    </xsl:template>

</xsl:stylesheet>
