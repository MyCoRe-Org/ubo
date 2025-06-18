<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl xalan encoder mods xlink i18n">

  <xsl:include href="mods-display.xsl" />
  <xsl:include href="coreFunctions.xsl" />

  <xsl:param name="UBO.System.ReadOnly" />

  <xsl:variable name="baseID" select="/mycoreobject/@ID" />

  <xsl:variable name="displayLimit" select="number('10')" />

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
      <li><xsl:value-of select="i18n:translate('structure.editor')"/></li>
    </ul>
  </xsl:template>

<!-- ============ Seite ============ -->

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="$title" />
          <xsl:value-of select="i18n:translate('structure.editor.edit')"/>
        </title>
      </head>
      <body>
        <xsl:call-template name="breadcrumb" />
        <div class="section ubo-structure">
          <xsl:apply-templates select="mycoreobject" />
        </div>
        <xsl:call-template name="javascript" />
      </body>
    </html>
  </xsl:template>

<!-- ============ Struktur-Ausgabe ============ -->

  <xsl:template match="mycoreobject">
    <xsl:variable name="id" select="@ID"/>
    <xsl:apply-templates select="structure/parents/parent" />

    <xsl:if test="not(structure/parents/parent)">
      <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]">
        <xsl:call-template name="duplicates">
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="from">host</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>

    <xsl:call-template name="duplicates">
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="from">base</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="." mode="pub-info">
      <xsl:with-param name="role">base</xsl:with-param>
    </xsl:apply-templates>

    <xsl:apply-templates select="structure/children" />
    <xsl:call-template name="showOrphans" />

  </xsl:template>

  <xsl:template match="structure/parents/parent">
    <xsl:variable name="id" select="@xlink:href" />

    <xsl:for-each select="document(concat('notnull:mcrobject:',$id))">
      <xsl:for-each select="mycoreobject">
        <xsl:call-template name="duplicates">
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="from">parent</xsl:with-param>
        </xsl:call-template>
        <xsl:apply-templates select="." mode="pub-info">
          <xsl:with-param name="role">parent</xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
      <xsl:for-each select="*[not(name()='mycoreobject')]">
        <xsl:call-template name="alert">
          <xsl:with-param name="id" select="$id" />
          <xsl:with-param name="text">
            <xsl:value-of select="i18n:translate('error.noParent')"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="structure/children" mode="badge">
    <div>
      <span class="ubo-badge-children badge badge-light">
        <a href="solr/select?q=parent:{ancestor::mycoreobject/@ID}&amp;sort=id+desc">
          <xsl:value-of select="count(child)" />
          <xsl:value-of select="i18n:translate('structure.editor.linked')"/>
        </a>
        <xsl:if test="(ancestor::mycoreobject/@ID=$baseID) and (count(child) &gt; $displayLimit)">
          <xsl:value-of select="concat(i18n:translate('structure.editor.limit') ,$displayLimit,i18n:translate('structure.editor.limitDisplay'))" />
        </xsl:if>
      </span>
    </div>
  </xsl:template>

  <xsl:template match="structure/children">
    <xsl:apply-templates select="child">
      <xsl:sort select="number(substring-after(@xlink:href,'_mods_'))" data-type="number" order="descending" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="structure/children/child">
    <xsl:variable name="id" select="@xlink:href" />
    <xsl:variable name="pos" select="position()" />

    <xsl:for-each select="document(concat('notnull:mcrobject:',$id))">
      <xsl:if test="$pos &lt;= $displayLimit">
        <xsl:apply-templates select="mycoreobject" mode="pub-info">
          <xsl:with-param name="role">child</xsl:with-param>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:for-each select="*[not(name()='mycoreobject')]">
        <xsl:call-template name="alert">
          <xsl:with-param name="id" select="$id" />
          <xsl:with-param name="text">
            <xsl:value-of select="i18n:translate('error.noChild')"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="alert">
    <xsl:param name="id" />
    <xsl:param name="text" />

    <div class="ubo-alert alert alert-danger" role="alert">
      <xsl:value-of select="concat(i18n:translate('error.editor'), ': ',$text,' ')" />
      <a href="DozBibEntryServlet?id={$id}">
        <xsl:value-of select="$id" />
      </a>
      <xsl:value-of select="i18n:translate('error.noDocument')"/>
    </div>
  </xsl:template>


    <xsl:template name="duplicates">
        <xsl:param name="from"/>
        <xsl:param name="id"/>

        <xsl:variable name="duplicates" select="document(concat('dedup:search:', $from ,':', $id))"/>

        <xsl:for-each select="document($duplicates)/result/duplicate">
            <xsl:apply-templates select="document(concat('mcrobject:',@id))/mycoreobject" mode="pub-info">
                <xsl:with-param name="role">duplicate</xsl:with-param>
                <xsl:with-param name="from" select="$from"/>
                <xsl:with-param name="duplicateOfID" select="$id"/>
            </xsl:apply-templates>
        </xsl:for-each>

    </xsl:template>

  <xsl:template name="countOrphans">
    <xsl:if test="not(//mods:mods/mods:relatedItem[@type='host'])">

      <xsl:variable name="solrURI">
        <xsl:text>q=-parent%3A*+AND+facet_host_title%3A"</xsl:text>
        <xsl:value-of select="encoder:encode(//mods:mods/mods:titleInfo[not(@type)][1]/mods:title,'UTF-8')" />
        <xsl:text>"</xsl:text>
      </xsl:variable>

      <xsl:variable name="numOrphans" select="document(concat('notnull:solr:rows=0&amp;',$solrURI))/response/result[@name='response']/@numFound" />

      <xsl:if test="$numOrphans &gt; 0">
        <div class="ubo-orphans">
          <span class="ubo-badge-orphans badge badge-light">
            <a href="solr/select?{$solrURI}&amp;sort=id+desc">
              <xsl:value-of select="$numOrphans" />
              <xsl:value-of select="i18n:translate('structure.editor.orphans')"/>
            </a>
            <xsl:if test="(@ID=$baseID) and ($numOrphans &gt; $displayLimit)">
              <xsl:value-of select="concat(i18n:translate('structure.editor.limit') ,$displayLimit,i18n:translate('structure.editor.limitDisplay'))" />
            </xsl:if>
          </span>
        </div>
      </xsl:if>

    </xsl:if>
  </xsl:template>

  <xsl:template name="showOrphans">
    <xsl:if test="not(//mods:mods/mods:relatedItem[@type='host'])">

      <xsl:variable name="solrURI">
        <xsl:text>notnull:solr:fl=id&amp;rows=999&amp;sort%3Aid+desc&amp;q=-parent%3A*+AND+facet_host_title%3A"</xsl:text>
        <xsl:value-of select="encoder:encode(//mods:mods/mods:titleInfo[not(@type)][1]/mods:title,'UTF-8')" />
        <xsl:text>"</xsl:text>
      </xsl:variable>

      <xsl:for-each select="document($solrURI)/response/result[@name='response']/doc">
        <xsl:sort select="number(substring-after(str[@name='id'],'_mods_'))" data-type="number" order="descending" />

        <xsl:if test="position() &lt;= $displayLimit">
          <xsl:apply-templates select="document(concat('mcrobject:',str[@name='id']))/mycoreobject" mode="pub-info">
            <xsl:with-param name="role">orphan</xsl:with-param>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:for-each>

    </xsl:if>
  </xsl:template>

  <xsl:template match="mycoreobject" mode="pub-info">
    <xsl:param name="role" />
    <xsl:param name="from" />
    <xsl:param name="duplicateOfID" />

    <div class="row">
      <xsl:call-template name="link-symbol">
        <xsl:with-param name="role" select="$role" />
      </xsl:call-template>
      <div>
        <xsl:attribute name="class">
          <xsl:text>col-11 ubo-structure__content</xsl:text>
          <xsl:choose>
            <xsl:when test="$role='base'"> ubo-structure__content--info</xsl:when>
            <xsl:when test="$role='duplicate'"> ubo-structure__content--warning</xsl:when>
            <xsl:when test="$role='orphan'"> ubo-structure__content--success</xsl:when>
            <xsl:otherwise> ubo-structure__content--default</xsl:otherwise>
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
        <xsl:apply-templates select="structure/children[child]" mode="badge" />

        <xsl:if test="$role='base'">
          <xsl:call-template name="countOrphans" />

          <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host']/@xlink:href">
            <xsl:if test="not(/mycoreobject/structure/parents/parent/@xlink:href=.)">
              <xsl:call-template name="alert">
                <xsl:with-param name="id" select="." />
                <xsl:with-param name="text">Verknüpfte Publikation (host)</xsl:with-param>
              </xsl:call-template>
            </xsl:if>
          </xsl:for-each>
        </xsl:if>

      </div>
    </div>

  </xsl:template>

  <xsl:template name="link-symbol">
    <xsl:param name="role" />

    <xsl:choose>
      <xsl:when test="$role = 'orphan'">
        <div class="col-1">
          <i class="fas fa-baby fa-4x" />
        </div>
      </xsl:when>
      <xsl:when test="structure/parents/parent">
        <div class="col-1">
          <i class="fas fa-retweet fa-4x" />
        </div>
      </xsl:when>
      <xsl:when test="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]">
        <div class="col-1">
          <i class="fas fa-question fa-4x" />
        </div>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="badges">
    <xsl:param name="role" />

    <xsl:apply-templates select="@ID" mode="badge" />
    <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
      <xsl:call-template name="pubtype" />
    </xsl:for-each>
    <xsl:for-each select="service/servflags/servflag[@type='status']">
      <span class="label-info ubo-badge-status badge badge-light">
        <xsl:value-of select="i18n:translate(concat('search.dozbib.status.',text()))" />
      </span>
    </xsl:for-each>
    <xsl:if test="$role='duplicate'">
      <span class="label-info ubo-badge-dublicate badge badge-primary">Evtl. Dublette</span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@ID" mode="badge">
    <span class="label-info ubo-badge-id badge badge-light">
      <a href="{$ServletsBaseURL}DozBibEntryServlet?id={.}">
        <xsl:value-of select="concat('ID ',number(substring-after(.,'_mods_')))" />
      </a>
    </span>
  </xsl:template>

  <xsl:template match="mods:mods" mode="citation">
    <div>
      <xsl:apply-templates select="." mode="cite">
        <xsl:with-param name="mode">divs</xsl:with-param>
      </xsl:apply-templates>
    </div>
  </xsl:template>

  <xsl:template match="mods:mods" mode="details">
    <div class="collapse ubo-details" id="details-{/mycoreobject/@ID}">
      <xsl:apply-templates select="." mode="details_lines" />
    </div>
  </xsl:template>

  <xsl:template name="actions">
    <xsl:param name="role" />
    <xsl:param name="from" />
    <xsl:param name="duplicateOfID" />

    <div class="ubo-actions">
      <a role="button" class="ubo-btn-details btn btn-primary btn-sm" href="#details-{@ID}" data-toggle="collapse"
        aria-expanded="false" aria-controls="details-{@ID}">
        <i class="fa fa-info" />
        <xsl:text> Details</xsl:text>
      </a>

      <xsl:if test="not($role='base')">
        <a role="button" class="ubo-btn-structure btn btn-primary btn-sm"
          href="{$ServletsBaseURL}DozBibEntryServlet?id={@ID}&amp;XSL.Style=structure">
          <i class="fa fa-arrows-alt" aria-hidden="true"></i>
          <xsl:value-of select="i18n:translate('button.structureBlank')"/>
        </a>
      </xsl:if>

      <xsl:if test="check:currentUserIsAdmin() and not($UBO.System.ReadOnly = 'true')" xmlns:check="xalan://org.mycore.ubo.AccessControl">
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="not(structure/children/child or (@ID=$baseID))" />
          <xsl:with-param name="action" select="'delete'" />
          <xsl:with-param name="icon" select="'trash'" />
          <xsl:with-param name="button" >
            <xsl:value-of select="i18n:translate('button.delete')"/>
          </xsl:with-param>
          <xsl:with-param name="text">
            <xsl:value-of select="concat(i18n:translate('button.deletePublication'), '{id=',@ID,'}')" />             
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and not ($from='base')" />
          <xsl:with-param name="action" select="'linkHost'" />
          <xsl:with-param name="icon" select="'link'" />
          <xsl:with-param name="button" >
             <xsl:value-of select="i18n:translate('button.selectHost')"/>
          </xsl:with-param>
          <xsl:with-param name="text" >
            <xsl:value-of select="concat(i18n:translate('structure.editor.relink'), '{child=',$baseID,'}', i18n:translate('structure.editor.relinkHost'), '{parent=',@ID,'}', i18n:translate('structure.editor.relinkQMark'))" />
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="(($role='child') or ($role='base')) and (structure/parents/parent)" />
          <xsl:with-param name="action" select="'unlinkHost'" />
          <xsl:with-param name="icon" select="'unlink'" />
          <xsl:with-param name="button">
            <xsl:value-of select="i18n:translate('button.unlink')"/>
          </xsl:with-param>
          <xsl:with-param name="text" >
            <xsl:value-of select="concat(i18n:translate('structure.editor.unlink'), '{child=',@ID,'}', i18n:translate('structure.editor.unlinkHost'), '{parent=',structure/parents/parent/@xlink:href,'}', i18n:translate('structure.editor.unlinkQMark'))" />
          </xsl:with-param>
          <xsl:with-param name="base" select="@ID" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='base') and //mods:mods/mods:relatedItem[@type='host'][not(@xlink:href)]" />
          <xsl:with-param name="action" select="'extractHost'" />
          <xsl:with-param name="icon" select="'external-link-alt'" />
          <xsl:with-param name="button" >
            <xsl:value-of select="i18n:translate('button.extractHost')"/>
          </xsl:with-param>
          <xsl:with-param name="text" >
            <xsl:value-of select="concat(i18n:translate('structure.editor.extractHost'), '{id=',@ID,'}', i18n:translate('structure.editor.extractHostLink'))" />
          </xsl:with-param> 
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and (($from='parent') or ($from='base'))" />
          <xsl:with-param name="action" select="'markAsFalseDuplicate'" />
          <xsl:with-param name="icon" select="'not-equal'" />
          <xsl:with-param name="button">
            <xsl:value-of select="i18n:translate('button.markAsFalseDuplicate')"/>
          </xsl:with-param>
          <xsl:with-param name="text">
            <xsl:value-of select="concat(i18n:translate('structure.editor.markAsFalseDuplicate'), '{id=',@ID,'}', i18n:translate('structure.editor.markAsFalseDuplicateOf') , '{duplicateOf=',$duplicateOfID,'}', i18n:translate('structure.editor.markAsFalseDuplicateQMark'))" />
          </xsl:with-param>
          <xsl:with-param name="preview" select="false()" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and (($from='parent') or ($from='base'))" />
          <xsl:with-param name="action" select="'mergeMetadata'" />
          <xsl:with-param name="icon" select="'copy'" />
          <xsl:with-param name="button" >
            <xsl:value-of select="i18n:translate('button.merge')"/>
          </xsl:with-param>
          <xsl:with-param name="text" >
            <xsl:value-of select="concat(i18n:translate('structure.editor.merge'), '{from=',@ID,'}', i18n:translate('structure.editor.mergeInto'), '{into=',$duplicateOfID,'}',i18n:translate('structure.editor.mergeQMark'))" /> 
          </xsl:with-param>
          <xsl:with-param name="preview" select="true()" />
        </xsl:call-template>
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="($role='duplicate') and (($from='parent') or ($from='base')) and (structure/children/child)" />
          <xsl:with-param name="action" select="'adoptChildren'" />
          <xsl:with-param name="icon" select="'baby-carriage'" />
          <xsl:with-param name="button" >
            <xsl:value-of select="concat(i18n:translate('button.adoptEn'),count(structure/children/child), i18n:translate('button.adoptDe'))" />            
          </xsl:with-param>
          <xsl:with-param name="text" >
            <xsl:value-of select="concat(i18n:translate('structure.editor.move'), count(structure/children/child), i18n:translate('structure.editor.moveHost'), '{from=',@ID,'}', i18n:translate('structure.editor.moveInto'), '{into=',$duplicateOfID,'}', i18n:translate('structure.editor.moveQMark'))" />
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template name="button-with-confirm-dialog">
    <xsl:param name="if" />
    <xsl:param name="button" />
    <xsl:param name="icon" />
    <xsl:param name="action" />
    <xsl:param name="text" />
    <xsl:param name="base" select="$baseID" />
    <xsl:param name="preview" select="false()" />

    <xsl:if test="$if">
      <a role="button" class="ubo-btn-dialog btn btn-primary btn-sm" data-toggle="modal" data-target="#{$action}-{/mycoreobject/@ID}" href="#">
        <i class="fa fa-{$icon}" aria-hidden="true" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="$button" />
      </a>

      <div class="modal fade" id="{$action}-{/mycoreobject/@ID}" tabindex="-1" role="dialog" aria-labelledby="{$text}" aria-hidden="true">
        <div class="modal-dialog" role="document">
          <div class="modal-content">

            <div class="modal-header">
              <h5 class="modal-title">
                <xsl:value-of select="concat($button,' ?')" />
              </h5>
              <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                <i class="fa fa-times" aria-hidden="true" />
              </button>
            </div>

            <form action="{$ServletsBaseURL}RelationEditorServlet" method="post">
              <input type="hidden" name="action" value="{$action}" />
              <input type="hidden" name="base" value="{$base}" />

              <div class="modal-body">
                <xsl:for-each select="xalan:tokenize($text,'{}')">
                  <div>
                    <xsl:choose>
                      <xsl:when test="contains(.,'=')">
                        <xsl:attribute name="class">ubo-modal-body__box--1</xsl:attribute>

                        <xsl:variable name="name" select="substring-before(.,'=')" />
                        <xsl:variable name="id" select="substring-after(.,'=')" />
                        <input type="hidden" name="{$name}" value="{$id}" />

                        <xsl:for-each select="document(concat('notnull:mcrobject:',$id))/mycoreobject">
                          <xsl:call-template name="badges">
                            <xsl:with-param name="role" select="'current'" />
                          </xsl:call-template>
                          <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="citation" />
                        </xsl:for-each>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:attribute name="class">ubo-modal-body__box--2</xsl:attribute>
                        <xsl:value-of select="." />
                      </xsl:otherwise>
                    </xsl:choose>
                  </div>
                </xsl:for-each>
              </div>

              <div class="modal-footer">
                <button type="button" class="ubo-btn-cancel btn btn-secondary" data-dismiss="modal">
                  <i class="fa fa-times mr-1" aria-hidden="true" />
                  <xsl:value-of select="i18n:translate('button.cancel')"/>
                </button>
                <xsl:if test="$preview">
                  <button type="submit" name="preview" value="true" class="ubo-btn-preview btn btn-secondary">
                    <i class="fa fa-{$icon} mr-1" aria-hidden="true" />
                    <xsl:value-of select="i18n:translate('button.preview')"/>
                  </button>
                </xsl:if>
                <button type="submit" class="ubo-btn-submit btn btn-primary structure-action">
                  <i class="fa fa-{$icon} mr-1" aria-hidden="true" />
                  <xsl:value-of select="$button" />
                </button>
              </div>

            </form>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="javascript">
    <script>
      $(document).ready(function() {
          $(".structure-action").click(function() {
              $(this).prop("disabled", true);
              $(this).html("&lt;i class='fa fa-spinner fa-spin'&gt;&lt;/i&gt; Moment bitte...");
              $(this).parents("form").submit();
          });
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
