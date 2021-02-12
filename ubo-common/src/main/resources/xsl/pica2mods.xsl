<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="3.0"
>

    <xsl:import href="default/pica2mods-default-titleInfo.xsl"/>
    <xsl:import href="default/pica2mods-default-name.xsl"/>
    <xsl:import href="default/pica2mods-default-identifier.xsl"/>
    <xsl:import href="default/pica2mods-default-language.xsl"/>
    <xsl:import href="default/pica2mods-default-location.xsl"/>
    <xsl:import href="default/pica2mods-default-physicalDescription.xsl"/>
    <xsl:import href="default/pica2mods-default-originInfo.xsl"/>
    <xsl:import href="default/pica2mods-default-genre.xsl"/>
    <xsl:import href="default/pica2mods-default-recordInfo.xsl"/>
    <xsl:import href="default/pica2mods-default-note.xsl"/>
    <xsl:import href="default/pica2mods-default-abstract.xsl"/>
    <xsl:import href="default/pica2mods-default-relatedItem.xsl"/>


    <xsl:import href="picaMode.xsl"/>
    <xsl:import href="picaURLResolver.xsl"/>
    <xsl:import href="picaDate.xsl"/>
    <xsl:param name="CONVERTER_VERSION" select="'Pica2Mods 2.0'"/>

    <xsl:template match="p:record">
        <xsl:message>HALLO WELT!</xsl:message>
        <mods:mods>
            <xsl:call-template name="modsTitleInfo"/>
            <xsl:call-template name="modsAbstract"/>
            <xsl:call-template name="modsName"/>
            <xsl:call-template name="modsIdentifier"/>
            <xsl:call-template name="modsLanguage"/>
            <xsl:call-template name="modsPhysicalDescription"/>
            <xsl:call-template name="modsOriginInfo"/>
            <xsl:call-template name="modsGenre"/>
            <xsl:call-template name="modsLocation"/>
            <xsl:call-template name="modsRecordInfo"/>
            <xsl:call-template name="modsNote"/>
            <xsl:call-template name="modsRelatedItem"/>
        </mods:mods>
    </xsl:template>

</xsl:stylesheet>
