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
  <xsl:param name="UBO.LSF.Link" />

<xsl:template match="/">
  <html>
    <head>
      <title>
        <xsl:value-of select="i18n:translate('lsf.search')" />
      </title>
      <script type="text/javascript" src="{$WebApplicationBaseURL}external/datatables-1.8.2/media/js/jquery.dataTables.min.js"><xsl:text> </xsl:text></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}external/jquery.jsonp.js"></script>
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
      <xsl:apply-templates select="lsfpidsearch" />
    </body>
  </html>
</xsl:template>

<!--  navigation  -->
<xsl:template name="breadcrumb">
  <ul id="breadcrumb">
    <li>Suche in HIS LSF</li>
  </ul>
</xsl:template>

<xsl:variable name="qm">'</xsl:variable>
<xsl:variable name="xpathLSF"    select="encoder:encode(concat('mods:nameIdentifier[@type=',$qm,'lsf',$qm,']'),'UTF-8')" />
<xsl:variable name="xpathFamily" select="encoder:encode(concat('mods:namePart[@type=',$qm,'family',$qm,']'),'UTF-8')" />
<xsl:variable name="xpathGiven"  select="encoder:encode(concat('mods:namePart[@type=',$qm,'given',$qm,']'),'UTF-8')" />

<!--  Suche in HIS LSF  -->

<xsl:template match="lsfpidsearch">
  
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
            <xsl:value-of select="i18n:translate('lsf.name')"/>
      </label>
      <input id="lastName" name="lastName" type="text" size="40" value="{@lastName}" class="mycore-form-input"/>
    </div>
    
    <div class="form-group form-inline">
      <label for="firstName" class="mycore-form-label">
            <xsl:value-of select="i18n:translate('lsf.nameFirst')"/>
      </label>
      <input id="firstName" name="firstName" type="text" size="40" value="{@firstName}" class="mycore-form-input"/>
    </div>

    <!-- Allow manual input of LSF PID for admins, in special cases or for testing purposes -->
    <xsl:if xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()">
      <div class="form-group form-inline">
            <label for="pid" class="mycore-form-label">
          LSF PID:
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
            
          <script type="text/javascript">
            function popup( url ) 
            {
              fenster = window.open( url, "LSF", "width=800,height=600,resizable=yes,scrollbars=yes" );
              fenster.focus();
              return false;
            }
          </script>
          
          <p> 
            <table id="lsfpersons">
              <xsl:apply-templates select="person">
                <xsl:sort select="nachname" />
                <xsl:sort select="vorname" />
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
      <a class="roundedButton" href="{$ServletsBaseURL}XEditor?_xed_submit_return=&amp;_xed_session={/lsfpidsearch/@session}&amp;{$xpathLSF}={id}&amp;{$xpathFamily}={encoder:encode(nachname,'UTF-8')}&amp;{$xpathGiven}={encoder:encode(vorname,'UTF-8')}">
        <xsl:value-of select="i18n:translate('lsf.selectPerson')"/>
      </a>
    </td>
    <td>
      <a target="_blank" href="{$UBO.LSF.Link}{id}" onclick="return popup(this.href);">
        <xsl:value-of select="nachname" />
        <xsl:if test="vorname">
          <xsl:text>, </xsl:text>
          <xsl:value-of select="vorname" />
        </xsl:if>
      </a>
      <xsl:if test="akadgrad">
        <xsl:text> </xsl:text>
        <xsl:value-of select="akadgrad" />
      </xsl:if>
    </td>
    <td>
      <xsl:apply-templates select="document(concat('ires:detail:pid=',id))/Person" />
    </td>
  </tr>
</xsl:template>

<xsl:template match="Person">
  <xsl:for-each select="Funktion/EinDtx[string-length(text()) &gt; 0]|Kontakt/FBZEDez[string-length(text()) &gt; 0]">
    <xsl:value-of select="text()" />
    <xsl:if test="position() != last()">; </xsl:if>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
