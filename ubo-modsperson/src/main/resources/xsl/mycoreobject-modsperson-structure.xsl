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
    <xsl:value-of select="number(substring-after(/mycoreobject/@ID,'_modsperson_'))" />
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

    <xsl:call-template name="duplicates">
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="from">person</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="." mode="pub-info">
      <xsl:with-param name="role">base</xsl:with-param>
    </xsl:apply-templates>

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

      </div>
    </div>

  </xsl:template>

  <xsl:template name="link-symbol">
    <xsl:param name="role" />

    <xsl:choose>
      <xsl:when test="$role = 'base'">
      <div class="col-1">
        <i class="fas fa-user fa-4x" />
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
    <xsl:if test="$role='duplicate'">
      <span class="label-info ubo-badge-dublicate badge badge-primary">Evtl. Dublette</span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@ID" mode="badge">
    <span class="label-info ubo-badge-id badge badge-light">
      <a href="{$ServletsBaseURL}DozBibEntryServlet?id={.}">
        <xsl:value-of select="concat('ID ',number(substring-after(.,'_modsperson_')))" />
      </a>
    </span>
  </xsl:template>

  <xsl:template match="mods:mods" mode="citation">
    <div>
      <xsl:apply-templates select="." mode="showmodsperson">
        <xsl:with-param name="mode">divs</xsl:with-param>
      </xsl:apply-templates>
    </div>
  </xsl:template>

  <xsl:template match="mods:mods" mode="details">
    <div class="collapse ubo-details" id="details-{/mycoreobject/@ID}">
      <xsl:apply-templates select="." mode="details_lines_modsperson" />
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
          href="{$ServletsBaseURL}DozBibEntryServlet?id={@ID}&amp;XSL.Style=modsperson-structure">
          <i class="fa fa-arrows-alt" aria-hidden="true"></i>
          <xsl:value-of select="i18n:translate('button.structureBlank')"/>
        </a>
      </xsl:if>

      <xsl:if test="check:currentUserIsAdmin() and not($UBO.System.ReadOnly = 'true')" xmlns:check="xalan://org.mycore.ubo.AccessControl">
        <xsl:call-template name="button-with-confirm-dialog">
          <xsl:with-param name="if" select="not(@ID=$baseID)" />
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
          <xsl:with-param name="if" select="($role='duplicate') and ($from='person')" />
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
