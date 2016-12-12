<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan i18n"
>

<xsl:include href="layout.xsl" />

<xsl:param name="LSF.PID.Link" />

<!--  page title  -->
<xsl:variable name="page.title" select="i18n:translate('lsf.search')" />

<!--  navigation  -->
<xsl:variable name="breadcrumb.extensions">
  <item label="Suche in HIS LSF" />
</xsl:variable>

<xsl:variable name="head.additional">
  <script type="text/javascript" src="{$WebApplicationBaseURL}external/datatables-1.8.2/media/js/jquery.dataTables.min.js"><xsl:text> </xsl:text></script>
  <script type="text/javascript" src="{$WebApplicationBaseURL}js/jQuery.dataTables.date.sort.js"><xsl:text> </xsl:text></script>
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
</xsl:variable>

<xsl:variable name="qm">'</xsl:variable>
<xsl:variable name="xpathLSF"    select="encoder:encode(concat('mods:nameIdentifier[@type=',$qm,'lsf',$qm,']'),'UTF-8')" />
<xsl:variable name="xpathFamily" select="encoder:encode(concat('mods:namePart[@type=',$qm,'family',$qm,']'),'UTF-8')" />
<xsl:variable name="xpathGiven"  select="encoder:encode(concat('mods:namePart[@type=',$qm,'given',$qm,']'),'UTF-8')" />

<!--  Suche in HIS LSF  -->

<xsl:template match="lsfpidsearch">

  <article class="highlight1">
    <p>
      <xsl:value-of select="i18n:translate('lsf.searchText')"/>
    </p>
  </article>

  <form action="lsfpidsearch.html" method="post" role="form" class="ubo-form">
    <input type="hidden" name="_xed_subselect_session" value="{@session}" />
    <div style="margin-bottom:0.5ex;">
      <label for="lastName">
        <xsl:value-of select="i18n:translate('lsf.name')"/>
      </label>
      <input id="lastName" name="lastName" type="text" size="40" value="{@lastName}" />
    </div>
    <div style="margin-bottom:0.5ex;">
      <label for="firstName">
        <xsl:value-of select="i18n:translate('lsf.nameFirst')"/>
      </label>
      <input id="firstName" name="firstName" type="text" size="40" value="{@firstName}" />
    </div>
    <!-- Allow manual input of LSF PID for admins, in special cases or for testing purposes -->
    <xsl:if xmlns:check="xalan://unidue.ubo.AccessControl" test="check:currentUserIsAdmin()">
      <div style="margin-bottom:0.5ex;">
        <label for="pid">LSF PID:</label>
        <input id="pid" name="pid" type="text" size="6" />
      </div>
    </xsl:if>
    <div class="cancel-submit">
      <input type="submit" class="roundedButton" name="search" value="{i18n:translate('button.search')}" />
      <input type="submit" class="roundedButton" style="margin-left:1ex;" name="cancel" value="{i18n:translate('button.cancel')}" />
    </div>
    <xsl:if test="results">
      <div class="cancel-submit">
        <input type="submit" class="roundedButton" name="notLSF" value="{i18n:translate('lsf.buttonFree')}" />
      </div>
    </xsl:if>
  </form>
  
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
    
    <article class="highlight1">
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
    </article>
    
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
      <a target="_blank" href="{$LSF.PID.Link}{id}" onclick="return popup(this.href);">
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
      <xsl:apply-templates select="document(concat('lsf:pid=',id))/Person" />
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
