<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan mods xlink i18n">

  <xsl:include href="mods-display.xsl" />

  <xsl:param name="UBO.System.ReadOnly" />

  <xsl:variable name="baseID" select="/mycoreobject/@ID" />

<!-- ========== Navigation ========== -->

  <xsl:variable name="title">
    <xsl:value-of select="i18n:translate('result.dozbib.entry')" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="number(substring-after(/mycoreobject/@ID,'_mods_'))" />
  </xsl:variable>

  <xsl:template name="breadcrumb">
    <ul id="breadcrumb">
      <li>
        <a href="{$ServletsBaseURL}DozBibEntryServlet?id={/mycoreobject/@ID}">
          <xsl:value-of select="$title" />
        </a>
      </li>
      <li>Struktur-Editor</li>
    </ul>
  </xsl:template>

<!-- ============ Seite ============ -->

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="$title" />
          <xsl:text>: Struktur bearbeiten</xsl:text>
        </title>
      </head>
      <body>
        <xsl:call-template name="breadcrumb" />
        <div class="section">
          <xsl:apply-templates select="mycoreobject" />
        </div>
      </body>
    </html>
  </xsl:template>

<!-- ============ Struktur-Ausgabe ============ -->

  <xsl:template match="mycoreobject">

    <xsl:apply-templates select="structure/parents/parent" />

    <xsl:if test="not(structure/parents/parent)">
      <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]">
        <xsl:apply-templates select="mods:extension[dedup]">
          <xsl:with-param name="from">host</xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:if>

    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods/mods:extension[dedup]">
      <xsl:with-param name="from">base</xsl:with-param>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pub-info">
      <xsl:with-param name="role">base</xsl:with-param>
    </xsl:apply-templates>

    <xsl:apply-templates select="structure/children/child">
      <xsl:sort select="number(substring-after(@xlink:href,'_mods_'))" data-type="number" order="descending" />
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="structure/parents/parent">
    <xsl:variable name="id" select="@xlink:href" />

    <xsl:for-each select="document(concat('notnull:mcrobject:',$id))">
      <xsl:for-each select="mycoreobject">
        <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods/mods:extension[dedup]">
          <xsl:with-param name="from">parent</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates select="." mode="pub-info">
          <xsl:with-param name="role">parent</xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
      <xsl:for-each select="*[not(name()='mycoreobject')]">
        <xsl:call-template name="alert">
          <xsl:with-param name="id" select="$id" />
          <xsl:with-param name="text">Verkn�pfte �berordnung (parent)</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="structure/children/child">
    <xsl:variable name="id" select="@xlink:href" />

    <xsl:for-each select="document(concat('notnull:mcrobject:',$id))">
      <xsl:apply-templates select="mycoreobject" mode="pub-info">
        <xsl:with-param name="role">child</xsl:with-param>
      </xsl:apply-templates>
      <xsl:for-each select="*[not(name()='mycoreobject')]">
        <xsl:call-template name="alert">
          <xsl:with-param name="id" select="$id" />
          <xsl:with-param name="text">Verkn�pfte Publikation (child)</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="alert">
    <xsl:param name="id" />
    <xsl:param name="text" />

    <div class="alert alert-danger ml-5" role="alert">
      <xsl:value-of select="concat('Fehler: ',$text,' ')" />
      <a href="DozBibEntryServlet?id={$id}">
        <xsl:value-of select="$id" />
      </a>
      <xsl:text> existiert nicht!</xsl:text>
    </div>
  </xsl:template>

  <xsl:template match="mods:extension[dedup]">
    <xsl:param name="from" />

    <xsl:variable name="myID" select="ancestor::mycoreobject/@ID" />

    <xsl:variable name="duplicatesURI">
      <xsl:call-template name="buildFindDuplicatesURI" />
    </xsl:variable>

    <xsl:for-each select="document($duplicatesURI)/response/result[@name='response']/doc">
      <xsl:sort select="number(substring-after(str[@name='id'],'_mods_'))" data-type="number" order="descending" />
      
      <xsl:variable name="duplicateID" select="str[@name='id']" />
      <xsl:if test="not($duplicateID = $myID)">
        <xsl:apply-templates select="document(concat('mcrobject:',str[@name='id']))/mycoreobject" mode="pub-info">
          <xsl:with-param name="role">duplicate</xsl:with-param>
          <xsl:with-param name="from" select="$from" />
          <xsl:with-param name="duplicateOfID" select="$myID" />
        </xsl:apply-templates>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="mycoreobject" mode="pub-info">
    <xsl:param name="role" />
    <xsl:param name="from" />
    <xsl:param name="duplicateOfID" />

    <div class="row">
      <xsl:call-template name="link-symbol" />
      <div>
        <xsl:attribute name="class">
        <xsl:text>col-11 border rounded p-2</xsl:text>
        <xsl:choose>
          <xsl:when test="$role='base'"> bg-info text-white</xsl:when>
          <xsl:when test="$role='duplicate'"> bg-warning text-white</xsl:when>
          <xsl:otherwise> bg-white</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>

        <xsl:call-template name="badges">
          <xsl:with-param name="role" select="$role" />
        </xsl:call-template>
        <xsl:call-template name="actions">
          <xsl:with-param name="role" select="$role" />
          <xsl:with-param name="from" select="$from" />
          <xsl:with-param name="duplicateOfID" select="$duplicateOfID" />
        </xsl:call-template>
        <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="citation" />
        <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="details" />
        <xsl:apply-templates select="structure/children" mode="badge" />

        <xsl:if test="$role='base'">
          <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host']/@xlink:href">
            <xsl:if test="not(/mycoreobject/structure/parents/parent/@xlink:href=.)">
              <xsl:call-template name="alert">
                <xsl:with-param name="id" select="." />
                <xsl:with-param name="text">Verkn�pfte Publikation (host)</xsl:with-param>
              </xsl:call-template>
            </xsl:if>
          </xsl:for-each>
        </xsl:if>

      </div>
    </div>
  </xsl:template>

  <xsl:template name="link-symbol">
    <xsl:if test="structure/parents/parent">
      <div class="col-1">
        <i class="fas fa-retweet fa-4x m-1" />
      </div>
    </xsl:if>
    <xsl:if test="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]">
      <div class="col-1">
        <i class="fas fa-question fa-4x m-1" />
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="badges">
    <xsl:param name="role" />

    <a class="label-info badge badge-primary mr-1" href="{$ServletsBaseURL}DozBibEntryServlet?id={@ID}">
      <xsl:text>ID </xsl:text>
      <xsl:value-of select="number(substring-after(@ID,'_mods_'))" />
    </a>
    <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
      <xsl:call-template name="pubtype" />
    </xsl:for-each>
    <xsl:if test="$role='duplicate'">
      <span class="label-info badge badge-primary mr-1">Evtl. Dublette</span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:mods" mode="citation">
    <div>
      <xsl:apply-templates select="." mode="cite">
        <xsl:with-param name="mode">divs</xsl:with-param>
      </xsl:apply-templates>
    </div>
  </xsl:template>

  <xsl:template match="mods:mods" mode="details">
    <div class="collapse mt-2 border-top" id="details-{/mycoreobject/@ID}">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </xsl:template>

  <xsl:template match="structure/children" mode="badge">
    <div>
      <a class="badge badge-info" href="solr/select?q=link:{/mycoreobject/@ID}&amp;sort=year+desc">
        <xsl:value-of select="count(child)" />
        <xsl:text> Publikation(en) verkn�pft</xsl:text>
      </a>
    </div>
  </xsl:template>

  <xsl:template name="actions">
    <xsl:param name="role" />
    <xsl:param name="from" />
    <xsl:param name="duplicateOfID" />

    <div class="mt-1 mx-1 float-right">
      <a role="button" class="btn btn-primary btn-sm mr-1" href="#details-{@ID}" data-toggle="collapse"
        aria-expanded="false" aria-controls="details-{@ID}">
        <i class="fa fa-info" />
        <xsl:text> Details</xsl:text>
      </a>

      <xsl:if test="not($role='base')">
        <a role="button" class="btn btn-primary btn-sm mr-1"
          href="{$ServletsBaseURL}DozBibEntryServlet?id={@ID}&amp;XSL.Style=structure">
          <i class="fa fa-arrows-alt" aria-hidden="true"></i>
          <xsl:text> Struktur</xsl:text>
        </a>
      </xsl:if>
      
      <xsl:if test="($baseID = @ID) and not(//mods:mods/mods:relatedItem[@type='host'])">
        <xsl:variable name="title" select="//mods:mods/mods:titleInfo[not(@type)][1]/mods:title" />
        <a role="button" class="btn btn-primary btn-sm mr-1"
          href='{$ServletsBaseURL}solr/select?q=(host_title:"{$title}"+AND+-link:{@ID})'>
          <i class="fas fa-search" aria-hidden="true"></i>
          <xsl:text> Waisen suchen</xsl:text>
        </a>
      </xsl:if>

      <xsl:if test="check:currentUserIsAdmin() and not($UBO.System.ReadOnly = 'true')"
        xmlns:check="xalan://org.mycore.ubo.AccessControl">
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="not(structure/children/child or (@ID=$baseID))" />
          <xsl:with-param name="action" select="'delete'" />
          <xsl:with-param name="icon" select="'trash'" />
          <xsl:with-param name="button" select="'L�schen'" />
          <xsl:with-param name="title" select="'Diese Publikation l�schen?'" />
          <xsl:with-param name="params" select="concat('id=',@ID,'&amp;base=',$baseID)" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and not ($from='base')" />
          <xsl:with-param name="action" select="'linkHost'" />
          <xsl:with-param name="icon" select="'link'" />
          <xsl:with-param name="button" select="'Als �berordnung w�hlen'" />
          <xsl:with-param name="title" select="'Diese Publikation als neue �berordnung verkn�pfen?'" />
          <xsl:with-param name="params" select="concat('child=',$baseID,'&amp;parent=',@ID,'&amp;base=',$baseID)" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="(($role='child') or ($role='base')) and (structure/parents/parent)" />
          <xsl:with-param name="action" select="'unlinkHost'" />
          <xsl:with-param name="icon" select="'unlink'" />
          <xsl:with-param name="button" select="'Verkn�pfung l�sen'" />
          <xsl:with-param name="title" select="'Verkn�pfung mit der �berordnung l�sen?'" />
          <xsl:with-param name="params" select="concat('child=',@ID,'&amp;base=',$baseID)" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='base') and //mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]" />
          <xsl:with-param name="action" select="'extractHost'" />
          <xsl:with-param name="icon" select="'link'" />
          <xsl:with-param name="button" select="'�berordnung herausl�sen'" />
          <xsl:with-param name="title" select="'�berordnung als separaten Eintrag herausl�sen und verlinken?'" />
          <xsl:with-param name="params" select="concat('id=',@ID,'&amp;base=',$baseID)" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and (($from='parent') or ($from='base'))" />
          <xsl:with-param name="action" select="'merge'" />
          <xsl:with-param name="icon" select="'copy'" />
          <xsl:with-param name="button" select="'Zusammenf�hren'" />
          <xsl:with-param name="title" select="'Dublette mit aktueller �berordnung zusammenf�hren?'" />
          <xsl:with-param name="params" select="concat('from=',@ID,'&amp;into=',$duplicateOfID,'&amp;base=',$baseID)" />
        </xsl:call-template>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template name="button-with-confirm-dialog">
    <xsl:param name="if" />
    <xsl:param name="button" />
    <xsl:param name="icon" select="'trash'" />
    <xsl:param name="action" />
    <xsl:param name="title" />
    <xsl:param name="params" />

    <xsl:if test="$if">
      <a role="button" class="btn btn-primary btn-sm" data-toggle="modal" data-target="#{$action}-{/mycoreobject/@ID}" href="#">
        <i class="fa fa-{$icon}" aria-hidden="true" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="$button" />
      </a>
      <div class="modal fade" id="{$action}-{/mycoreobject/@ID}" tabindex="-1" role="dialog" aria-labelledby="{$title}" aria-hidden="true">
        <div class="modal-dialog" role="document">
          <div class="modal-content bg-secondary">
            <div class="modal-header">
              <h5 class="modal-title">
                <xsl:value-of select="$title" />
              </h5>
              <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                <i class="fa fa-times" aria-hidden="true" />
              </button>
            </div>
            <div class="modal-body">
              <span class="label-info badge badge-primary mr-1">
                <xsl:text>ID </xsl:text>
                <xsl:value-of select="number(substring-after(@ID,'_mods_'))" />
              </span>
              <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
                <xsl:apply-templates select="." mode="cite">
                  <xsl:with-param name="mode">divs</xsl:with-param>
                </xsl:apply-templates>
              </xsl:for-each>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">
                <i class="fa fa-times" aria-hidden="true" />
                <xsl:text> Abbrechen</xsl:text>
              </button>
              <a href="{$ServletsBaseURL}RelationEditorServlet?action={$action}&amp;{$params}" class="btn btn-primary"
                role="button">
                <i class="fa fa-{$icon}" aria-hidden="true" />
                <xsl:text> </xsl:text>
                <xsl:value-of select="$button" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>