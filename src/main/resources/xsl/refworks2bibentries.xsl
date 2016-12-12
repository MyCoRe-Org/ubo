<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:s='uuid:BDC6E3F0-6DA3-11d1-A2A3-00AA00C14882'
 xmlns:dt='uuid:C2F41010-65B3-11d1-A29F-00AA00C14882'
 xmlns:rs='urn:schemas-microsoft-com:rowset'
 xmlns:z='#RowsetSchema'
 exclude-result-prefixes="xsl s dt rs z">
<s:Schema id='RowsetSchema'>
	<s:ElementType name='row' content='eltOnly' rs:CommandTimeout='30'
	 rs:updatable='true'>
		<s:AttributeType name='UserID' rs:number='1' rs:writeunknown='true'
			 rs:basecatalog='refworks' rs:basetable='refs' rs:basecolumn='UserID'
			 rs:keycolumn='true'>
			<s:datatype dt:type='int' dt:maxLength='4' rs:precision='10'
			 rs:fixedlength='true' rs:maybenull='false'/>
		</s:AttributeType>
		<s:AttributeType name='InternalRefID' rs:number='2'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='InternalRefID' rs:keycolumn='true'>
			<s:datatype dt:type='int' dt:maxLength='4' rs:precision='10'
			 rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='Created' rs:number='3' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Created'>
			<s:datatype dt:type='dateTime' rs:dbtype='timestamp'
			 dt:maxLength='16' rs:scale='3' rs:precision='23' rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='LastModified' rs:number='4' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='LastModified'>
			<s:datatype dt:type='dateTime' rs:dbtype='timestamp'
			 dt:maxLength='16' rs:scale='3' rs:precision='23' rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='Marked' rs:number='5' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Marked'>
			<s:datatype dt:type='string' dt:maxLength='10' rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='UserRefID' rs:number='6' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='UserRefID' rs:keycolumn='true'>
			<s:datatype dt:type='int' dt:maxLength='4' rs:precision='10'
			 rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='RefType' rs:number='7' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='RefType'>
			<s:datatype dt:type='int' dt:maxLength='4' rs:precision='10'
			 rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='AuthorPrimary' rs:number='8'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AuthorPrimary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='AuthorSecondary' rs:number='9'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AuthorSecondary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='AuthorTertiary' rs:number='10'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AuthorTertiary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='TitlePrimary' rs:number='11'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='TitlePrimary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='TitleSecondary' rs:number='12'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='TitleSecondary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='TitleTertiary' rs:number='13'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='TitleTertiary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='Keyword' rs:number='14' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Keyword'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='PubYear' rs:number='15' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='PubYear'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='PubDateFreeForm' rs:number='16'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='PubDateFreeForm'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='PeriodicalFull' rs:number='17'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='PeriodicalFull'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='PeriodicalAbbrev' rs:number='18'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='PeriodicalAbbrev'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Volume' rs:number='19' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Volume'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Issue' rs:number='20' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Issue'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='StartPage' rs:number='21' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='StartPage'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='OtherPages' rs:number='22' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='OtherPages'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Edition' rs:number='23' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Edition'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Publisher' rs:number='24' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Publisher'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='PlaceOfPublication' rs:number='25'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='PlaceOfPublication'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='ISSN_ISBN' rs:number='26' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='ISSN_ISBN'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Availability' rs:number='27'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Availability'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Author_Address_Affiliation' rs:number='28'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Author_Address_Affiliation'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='AccessionNumber' rs:number='29'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AccessionNumber'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Language' rs:number='30' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Language'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Classification' rs:number='31'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Classification'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='SubFile_Database' rs:number='32'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='SubFile_Database'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='OriginalForeignTitle' rs:number='33'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='OriginalForeignTitle'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='Links' rs:number='34' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Links'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='DOI' rs:number='35' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='DOI'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='Abstract' rs:number='36' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Abstract'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='Notes' rs:number='37' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Notes'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='Folder' rs:number='38' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Folder'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='User1' rs:number='39' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='User1'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='User2' rs:number='40' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='User2'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='User3' rs:number='41' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='User3'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='User4' rs:number='42' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='User4'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='User5' rs:number='43' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='User5'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='CallNumber' rs:number='44' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='CallNumber'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='DatabaseName' rs:number='45'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='DatabaseName'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='DataSource' rs:number='46' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='DataSource'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='IdentifyingPhrase' rs:number='47'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='IdentifyingPhrase'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='RetrievedDate' rs:number='48'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='RetrievedDate'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='ShortenedTitle' rs:number='49'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='ShortenedTitle'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='TextAttributes' rs:number='50'
			 rs:nullable='true' rs:maydefer='true' rs:writeunknown='true'
			 rs:basecatalog='refworks' rs:basetable='refs' rs:basecolumn='TextAttributes'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='AuthorQuaternary' rs:number='51'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AuthorQuaternary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='AuthorQuinary' rs:number='52'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='AuthorQuinary'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='LinkVendorData' rs:number='53'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='LinkVendorData'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='URL' rs:number='54' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='URL'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='SponsoringLibrary' rs:number='55'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='SponsoringLibrary'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='SponsoringLibraryLocation' rs:number='56'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='SponsoringLibraryLocation'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='CitedRefs' rs:number='57' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='CitedRefs'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='WebsiteTitle' rs:number='58'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='WebsiteTitle'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='WebsiteEditor' rs:number='59'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='WebsiteEditor'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='WebsiteVersion' rs:number='60'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='WebsiteVersion'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='PubDateElectronic' rs:number='61'
			 rs:nullable='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='PubDateElectronic'>
			<s:datatype dt:type='string' dt:maxLength='256'/>
		</s:AttributeType>
		<s:AttributeType name='SourceType' rs:number='62' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='SourceType' rs:keycolumn='true'>
			<s:datatype dt:type='int' dt:maxLength='4' rs:precision='10'
			 rs:fixedlength='true'/>
		</s:AttributeType>
		<s:AttributeType name='OverFlow' rs:number='63' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Overflow'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:AttributeType name='Objects' rs:number='64' rs:nullable='true'
			 rs:writeunknown='true' rs:basecatalog='refworks' rs:basetable='refs'
			 rs:basecolumn='Objects'>
			<s:datatype dt:type='string' dt:maxLength='2000'/>
		</s:AttributeType>
		<s:AttributeType name='Comments' rs:number='65' rs:nullable='true'
			 rs:maydefer='true' rs:writeunknown='true' rs:basecatalog='refworks'
			 rs:basetable='refs' rs:basecolumn='Comments'>
			<s:datatype dt:type='string' dt:maxLength='1073741823'
			 rs:long='true'/>
		</s:AttributeType>
		<s:extends type='rs:rowbase'/>
	</s:ElementType>
</s:Schema>

<xsl:output method="xml" encoding="UTF-8" indent="yes" />

<xsl:variable name="offset"  select="'2200'" />
<xsl:variable name="subject" select="'wiwi'" />

<xsl:template match="/xml/rs:data">
  <bibentries>
    <xsl:for-each select="z:row">
      <bibentry id="{number($offset) + number(@InternalRefID)}" lastModified="{translate(@LastModified,'T',' ')}">

        <xsl:attribute name="type">
          <xsl:if test="@RefType='0'">article</xsl:if>
          <xsl:if test="@RefType='1'">article</xsl:if>
          <xsl:if test="@RefType='3'">book</xsl:if>
          <xsl:if test="@RefType='4'">chapter</xsl:if>
          <xsl:if test="@RefType='5'">chapter</xsl:if>
          <xsl:if test="@RefType='9'">book</xsl:if>
        </xsl:attribute>

        <xsl:apply-templates mode="title" select="@TitlePrimary" />
        <xsl:apply-templates mode="title" select="@TitleSecondary" />
        <xsl:apply-templates mode="title" select="@TitleTertiary" />

        <xsl:apply-templates mode="author" select="@AuthorPrimary" />
        <xsl:apply-templates mode="author" select="@AuthorSecondary" />
        <xsl:apply-templates mode="author" select="@AuthorTertiary" />
        <xsl:apply-templates mode="author" select="@AuthorQuarternary" />
        <xsl:apply-templates mode="author" select="@AuthorQuinary" />

        <subject><xsl:value-of select="$subject" /></subject>      
        
        <xsl:if test="string-length(concat(@PeriodicalFull,@PeriodicalAbbrev)) &gt; 0">
          <journal>
            <xsl:choose>
              <xsl:when test="string-length(@PeriodicalFull) &gt; 0">
                <xsl:value-of select="normalize-space(@PeriodicalFull)" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="normalize-space(@PeriodicalAbbrev)" />
              </xsl:otherwise>
            </xsl:choose>
          </journal>
        </xsl:if>

        <xsl:if test="string-length(@PlaceOfPublication) &gt; 0">
          <place><xsl:value-of select="normalize-space(@PlaceOfPublication)" /></place>
        </xsl:if>

        <xsl:if test="string-length(@PubYear) &gt; 0">
          <year><xsl:value-of select="normalize-space(@PubYear)" /></year>
        </xsl:if>
        
        <xsl:if test="string-length(@Publisher) &gt; 0">
          <publisher><xsl:value-of select="normalize-space(@Publisher)" /></publisher>
        </xsl:if>

        <xsl:if test="string-length(@Edition) &gt; 0">
          <edition><xsl:value-of select="normalize-space(@Edition)" /></edition>
        </xsl:if>

        <xsl:if test="string-length(@Volume) &gt; 0">
          <volume><xsl:value-of select="normalize-space(@Volume)" /></volume>
        </xsl:if>
        
        <xsl:if test="string-length(@StartPage) &gt; 0">
          <size>
            <xsl:text>S. </xsl:text>
            <xsl:value-of select="normalize-space(@StartPage)" />
            <xsl:if test="string-length(@OtherPages) &gt; 0">
              <xsl:text> - </xsl:text>
              <xsl:value-of select="normalize-space(@OtherPages)" />
            </xsl:if>
          </size>
        </xsl:if>

        <xsl:if test="string-length(@Issue) &gt; 0">
          <issue><xsl:value-of select="normalize-space(@Issue)" /></issue>
        </xsl:if>

        <xsl:if test="(string-length(concat(@Notes,@Keyword)) &gt; 0) or (@RefType='9')">
          <comment>
            <xsl:value-of select="normalize-space(@Keyword)" />
            <xsl:if test="(string-length(@Keyword) &gt; 0) and (string-length(@Notes) &gt; 0)"><xsl:text>
            
</xsl:text>
            </xsl:if>
            <xsl:value-of select="normalize-space(@Notes)" />
            <xsl:if test="@RefType='9'">
              <xsl:text>
              
Dissertation</xsl:text>
            </xsl:if>
          </comment>
        </xsl:if>

        <xsl:if test="string-length(@Abstract) &gt; 0">
          <abstract><xsl:value-of select="normalize-space(@Abstract)" /></abstract>
        </xsl:if>
        
        <xsl:if test="string-length(translate(@ISSN_ISBN,'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz äöüßÄÖÜ','')) &gt; 0">
          <xsl:choose>
			<xsl:when test="(string-length(@ISSN_ISBN) = 9) and (substring(@ISSN_ISBN,5,1) = '-')">
              <issn><xsl:value-of select="normalize-space(@ISSN_ISBN)" /></issn>
			</xsl:when>
			<xsl:otherwise>
              <isbn><xsl:value-of select="normalize-space(@ISSN_ISBN)" /></isbn>
			</xsl:otherwise>
          </xsl:choose>
        </xsl:if>

        <xsl:if test="string-length(@URL) &gt; 0">
          <url><xsl:value-of select="normalize-space(@URL)" /></url>
        </xsl:if>

        <xsl:if test="string-length(@DOI) &gt; 0">
          <url><xsl:value-of select="normalize-space(@DOI)" /></url>
        </xsl:if>
        
      </bibentry>
    </xsl:for-each>
  </bibentries>    
</xsl:template>

<xsl:template match="@*" mode="title">
  <xsl:if test="string-length(.) &gt; 0">
    <title>
      <xsl:value-of select="normalize-space(.)" />
    </title>
  </xsl:if>
</xsl:template>

<xsl:template match="@*" mode="author">
  <xsl:call-template name="singleAuthor">
    <xsl:with-param name="string" select="." />
  </xsl:call-template>  
</xsl:template>

<xsl:template name="singleAuthor">
  <xsl:param name="string" />
  
 <xsl:if test="string-length(normalize-space($string)) &gt; 0">
  <xsl:variable name="name">
    <xsl:choose>
	  <xsl:when test="contains($string,'|')">
	    <xsl:value-of select="substring-before($string,'|')" />
	  </xsl:when>
	  <xsl:otherwise><xsl:value-of select="$string" /></xsl:otherwise>
	</xsl:choose>
  </xsl:variable>

  <xsl:variable name="lastName">
    <xsl:choose>
	  <xsl:when test="contains($name,',')">
	    <xsl:value-of select="substring-before($name,',')" />
	  </xsl:when>
	  <xsl:otherwise><xsl:value-of select="$name" /></xsl:otherwise>
	</xsl:choose>
  </xsl:variable>
  
  <contributor role="aut">
    <xsl:if test="contains($name,',')">
      <firstName><xsl:value-of select="normalize-space(substring-after($name,','))" /></firstName>
    </xsl:if>
    <lastName><xsl:value-of select="normalize-space($lastName)" /></lastName>
  </contributor>

  <xsl:if test="contains($string,'|')">
	<xsl:call-template name="singleAuthor">
	  <xsl:with-param name="string" select="substring-after($string,'|')" />
	</xsl:call-template>
  </xsl:if>
 </xsl:if>
</xsl:template>

</xsl:stylesheet>
