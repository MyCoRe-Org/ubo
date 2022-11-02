<template>
  <div class="ubo-vue-form">
    <link v-if="bootstrap" rel="stylesheet" :href="bootstrap">
    <link v-if="css" rel="stylesheet" :href="css">
    <link v-if="fontawesome" rel="stylesheet" :href="fontawesome">
    <section>
      <div class="form-group form-inline">
        <label class="mycore-form-label" for="personSearch">{{i18n["listWizard.search"]}}</label>
        <input id="personSearch" class="mycore-form-input" type="text" v-model="search.text"
               v-on:keypress.enter.prevent="startSearch">
        <span class="input-group-btn">
          <button class="btn btn-secondary" v-on:click.prevent="startSearch">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"
                  v-if="search.searching"></span>
            {{ i18n["button.search"] }}
          </button>
        </span>
      </div>
    </section>
    <section v-if="search.errored">
      <div class="ubo-input-error">
        {{ i18n["error.occurred"] }}
      </div>
    </section>
    <section v-if="search.searchResultUsers.length>0 || search.noresults">
      <div class="ubo-pl-noresults">
        <b>{{ i18n["result.dozbib.results"] }}</b>
        <div v-if="search.noresults" v-on:click="resetSearch()">
          <a href="javascript:void(0)">{{ i18n["index.person.found.0"] }}</a>
        </div>
      </div>
      <div class="ubo-pl-results">
        <transition-group name="results" tag="div" class="list-group list-group-flush">
          <a class="list-group-item list-group-item-action"
             v-for="autocompleteUser in search.searchResultUsers"
             :key="autocompleteUser.pid"
             v-on:click="addUser(autocompleteUser)"
             href="javascript:void(0)">
            <i class="fas fa-plus-circle ubo-pl-adduser"></i>
            {{ autocompleteUser.name }}
            <i v-if="Object.keys(autocompleteUser.otherIds).length>0" :id="'popover-results-' + autocompleteUser.pid"
               class="fa fa-info-circle"></i>
            <b-popover triggers="hover" :target="'popover-results-' + autocompleteUser.pid">
              <div>
                <dl>
                  <template v-for="arr,type in autocompleteUser.otherIds">
                    <dt :key="type">{{ i18n["index.person.id." + type] }}</dt>
                    <dd :key="type">{{arr.join(", ")}}</dd>
                  </template>
                </dl>
              </div>
            </b-popover>
          </a>
        </transition-group>
      </div>
    </section>
    <section v-if="users.length>0">
      <div class="ubo-pl-userlist">
        <b>{{ i18n["listWizard.users"] }}</b>
        <transition-group name="used" tag="div" class="list-group list-group-flush">
          <a class="list-group-item list-group-item-action"
             v-for="user in users"
             :key="user.pid"
             v-on:click="removeUser(user)"
             href="javascript:void(0)">
            <i class="fas fa-minus-circle ubo-pl-deluser"></i>
            {{ user.name }}
            <i v-if="Object.keys(user.otherIds).length>0" :id="'popover-list-' + user.pid"
               class="fa fa-info-circle"></i>
            <b-popover triggers="hover" :target="'popover-list-' + user.pid">
              <div>
                <dl>
                  <template v-for="arr,type in user.otherIds">
                    <dt :key="type">{{ i18n["index.person.id." + type] }}</dt>
                    <dd :key="type">{{arr.join(", ")}}</dd>
                  </template>
                </dl>
              </div>
            </b-popover>
          </a>
        </transition-group>
      </div>
    </section>
    <section>
      <div class="form-group row">
        <label class="mycore-form-label" for="yearIssued">
          {{i18n["search.dozbib.year.publication"]}}
        </label>
        <div class="col-8 form-check mycore-list list-group list-group-flush">
          <div class="list-group-item d-flex align-items-center">
            <div class="col-4">
              <input id="dateRangeLabel" v-model="exportM.yearPeriod" class="form-check-input" type="checkbox" v-on:change="yearChange"></input>
              <label class="form-check-label" for="dateRangeLabel">{{ i18n["search.dozbib.year.period"] }}</label>
            </div>
            <div class="col-8">
              <input
                v-if="!exportM.yearPeriod"
                id="yearIssued"
                v-model="exportM.year"
                class="mycore-form-input"
                type="number"
                min="1900"
                max="2099"
                step="1"
                v-bind:class="{ 'is-invalid' : isInvalidYear(exportM.year) }"
                v-on:change="yearChange">
              <div v-else class="input-group yearRange">
                <input id="searchDate" v-model="exportM.yearFrom"
                  class="form-control" placeholder="" type="number" min="1900" max="2099" step="1"
                  v-bind:class="{ 'is-invalid' : isInvalidYear(exportM.yearFrom, exportM.yearTo) }"
                  v-on:change="yearChange">
                  <div class="input-group-between">
                    <div class="input-group-text">-</div>
                  </div>
                <input id="searchDate" v-model="exportM.yearTo" class="form-control" placeholder="" type="number" min="1900" max="2099" step="1"
                   v-bind:class="{ 'is-invalid' : isInvalidYear(exportM.yearFrom, exportM.yearTo) }"
                   v-on:change="yearChange">
              </div>
            </div>
          </div>
        </div>
        <div class="ubo-input-invalid invalid-feedback">
          {{i18n["search.dozbib.year.invalid"]}}
        </div>
      </div>
    </section>
    <section v-if="isPartOfEnabled()">
      <div class="form-group form-linline">
        <div class="input-group">
          <label class="mycore-form-label" for="partOf">{{i18n["ubo.partOf"]}}</label>
          <input id="partOf" v-model="exportM.partOf"
                 type="checkbox"
                 v-on:change="partOfChange">
        </div>
      </div>
    </section>
    <section>
      <div class="form-group row">
        <label class="mycore-form-label">{{i18n["search.sort"]}}</label>
        <transition-group name="plSort" tag="div" class="col-8 mycore-list list-group list-group-flush">
          <div v-for="(sort,i) in exportM.sort" :key="sort.field" class="list-group-item d-flex align-items-center">
            <div class="col">
              <input class="form-check-input" :id="'ps_select_' + sort.field" v-on:change="sortChange" type="checkbox"
                     v-model="sort.active">
              <label class="form-check-label" :for="'ps_select_' + sort.field">{{i18n[sort.i18nKey]}}</label>
            </div>
            <div class="col">
              <select class="mycore-form-input custom-select"
                      :id="'ps_radio_' + sort.field"
                       v-on:change="sortChange"
                       v-model="sort.asc">
                <option v-bind:value="true">{{i18n["search.sort.asc"]}}</option>
                <option v-bind:value="false">{{i18n["search.sort.desc"]}}</option>
              </select>
            </div>
            <div class="col">
              <div class="btn-group">
              <button v-bind:disabled="i<=0" v-on:click.prevent="moveSortUp(sort)" class="btn btn-primary up"
                      tabindex="999">
                <i class="fas fa-arrow-up"></i>
              </button>
              <button v-bind:disabled="!(i<exportM.sort.length-1)" v-on:click.prevent="moveSortDown(sort)"
                      class="btn btn-primary down">
                <i class="fas fa-arrow-down"></i>
              </button>
              </div>
            </div>
          </div>
        </transition-group>
      </div>
      <div class="form-group form-inline">
        <label class="mycore-form-label" for="formatSelect">{{ i18n["listWizard.format"] }}</label>
        <select id="formatSelect" class="mycore-form-input custom-select" v-on:change="formatChange" v-model="exportM.format">
          <option v-bind:value="''">{{ i18n["search.select"] }}</option>
          <option v-bind:value="'pdf'">PDF</option>
          <option v-bind:value="'html'">HTML</option>
          <option v-bind:value="'mods'">MODS</option>
          <option v-bind:value="'bibtex'">BibTex</option>
          <option v-bind:value="'endnote'">Endnote</option>
          <option v-bind:value="'ris'">RIS</option>
          <option v-bind:value="'isi'">ISI</option>
          <option v-bind:value="'mods2csv2'">CSV</option>
        </select>
      </div>
      <div class="form-group form-inline" v-if="exportM.format==='html' || exportM.format==='pdf'">
        <label class="mycore-form-label" for="styleSelect">{{ i18n["listWizard.citation"] }}</label>
        <select id="styleSelect" class="mycore-form-input custom-select" v-on:change="styleChange" v-model="exportM.style">
          <option v-bind:value="''"></option>
          <option v-for="style in styles" v-bind:key="style.id" v-bind:value="style.id">{{style.title}}</option>
        </select>
      </div>
    </section>
    <section v-if="result.link.length>0">
      <div class="row">
        <div class="col-12">
          <div class="jumbotron">
            <!-- when the format is html, we will show the link (which is a iframe) as code -->
            <p class="text-primary" v-if="exportM.format==='html'">{{ i18n["listWizard.code"] }}</p>
            <code v-if="exportM.format==='html'">&lt;iframe scrolling=&quot;yes&quot; width=&quot;90%&quot; height=&quot;300&quot;
              src=&quot;{{result.link}}&quot;/&gt;</code>

            <!-- when format is not html, we just show it as link -->
            <p class="text-primary" v-if="exportM.format!=='html'">{{ i18n["listWizard.link"] }}</p>
            <a v-if="exportM.format!=='html'" :href="result.link">{{result.link}}</a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import 'whatwg-fetch'
import {BPopover} from 'bootstrap-vue'

Vue.component('b-popover', BPopover)

@Component
export default class PublicationList extends Vue {
  /**
   * The webapplication base url.
   * @private
   */
  @Prop() private baseurl!: string;

  /**
   * The url to the bootstrap.css
   * @private
   */
  @Prop() private bootstrap!: string;

  /**
   * The url to a custom.css
   * @private
   */
  @Prop() private css!: string;

  /**
   * The name of the leadid
   * @private
   */
  @Prop() private leadid!: string;

  /**
   * Comma sperated list of roles which are considered "authors"
   */
  @Prop() private roles!: string;

  /**
   * Comma sperated list of ids which should be shown in the search results
   */
  @Prop() private personids!: string;

  /**
   * The url to fontawesome
   * @private
   */
  @Prop() private fontawesome!: string;

  @Prop() private partofenabled!:string;

  private search: SearchModel = {
    text: "",
    searchResultUsers: [],
    searching: false,
    noresults: false,
    errored: false
  };

  private exportM: ExportModel = {
    format: "",
    style: "",
    sort: [
      {active: true, field: "year", asc: true, i18nKey: "search.sort.year"},
      {active: false, field: "sortby_person", asc: true, i18nKey: "search.sort.name"},
      {active: false, field: "sortby_title", asc: true, i18nKey: "search.sort.title"}
    ],
    year: "",
    partOf: false,
    yearPeriod: false,
    yearFrom: "",
    yearTo: ""
  };

  private result = {
    link: ""
  }

  private styles: StyleDescription[] = []

  private i18n: { [key: string]: string | null; } = {
    "button.search": null,
    "error.occurred": null,
    "index.person.found.0": null,
    "listWizard.users": null,
    "search.select": null,
    "search.sort": null,
    "search.sort.year": null,
    "search.sort.name": null,
    "search.sort.title": null,
    "search.sort.asc": null,
    "search.sort.desc": null,
    "listWizard.link": null,
    "listWizard.code": null,
    "listWizard.format": null,
    "listWizard.citation": null,
    "result.dozbib.results": null,
    "search.dozbib.year.publication": null,
    "search.dozbib.year.invalid": null,
    "listWizard.search": null,
    "index.person.id.*": null,
    "ubo.partOf": null,
    "search.dozbib.year.period": null
  };

  private users: User[] = [];

  created(): void {
    this.resolveStyles();
    this.resolveiI18N();
  }

  private isPartOfEnabled() {
    return typeof this.partofenabled == "string" && this.partofenabled.toLowerCase()==="true"
  }

  private isInvalidYear(yearStr: string, yearStr2?: string) {
    if (yearStr2) {
      if (this.isInvalidYear(yearStr)) {
        return true;
      }
      if (this.isInvalidYear(yearStr2)) {
        return true;
      }

      let yearF = yearStr == "" ? 0 : parseInt(yearStr);
      let yearT = yearStr2 == "" ? new Date().getFullYear() : parseInt(yearStr2);

      return yearF > yearT;
    } else {
      if (yearStr == "") {
        return false;
      }
      let year = parseInt(yearStr);
      return isNaN(year) || year > new Date().getFullYear() || year < 0;
    }
  }

  private async resolveStyles() {
    let response = await fetch(this.getWebApplicationBaseURL() + "rsc/export/styles");
    let styles = await response.json();
    for (const style of styles) {
      this.styles.push(style);
    }
  }

  private async resolveiI18N() {
    Object.keys(this.i18n)
        .map(k => {
          let url = this.getWebApplicationBaseURL() + "rsc/locale/translate/" + k;
          fetch(url).then(result => {
            if(k.endsWith("*")){
              result.json().then(obj => {
                Object.keys(obj).forEach(key => {
                  this.i18n[key] = obj[key];
                })
              })
            } else {
              result.text().then(translation => {
                this.i18n[k] = translation;
              });
            }
          })
        });
  }

  private startSearch(): void {
    console.log("trigger");
    this.resetSearch();
    let currentPromise: Promise<User[]> = this.searchSolrForPerson(this.search.text);
    currentPromise.then(userArray => {
      if (userArray.length == 0) {
        this.search.noresults = true;
      }
      for (const foundUser of userArray) {
        this.search.searchResultUsers.push(foundUser);
      }
    }).catch((e) => {
      console.error(e);
      this.search.errored = true;
    });
  }

  private resetSearch() {
    while(this.search.searchResultUsers.pop()!=undefined){
      // make it empty
    }
    this.search.noresults = false;
  }

  private addUser(user: User) {
    if (this.users.filter(u => u.pid == user.pid).length == 0) {
      this.users.push(user);
    }
    this.search.searchResultUsers.splice(this.search.searchResultUsers.indexOf(user), 1);
    this.createLink();
  }

  private removeUser(user: User): void {
    let number = this.users.indexOf(user);
    this.users.splice(number, 1);
    this.createLink();
  }

  private formatChange(): void {
    this.createLink();
  }

  private sortChange(): void {
    this.createLink();
  }

  private yearChange(): void {
    this.createLink();
  }

  private partOfChange(): void {
    this.createLink();
  }

  private createLink() {
    this.clearLink();

    const yearTo = this.exportM.yearTo;
    const yearFrom = this.exportM.yearFrom;
    if(this.exportM.yearPeriod) {
      if(this.isInvalidYear(yearFrom, yearTo)){
        return;
      }
    } else {
      if (this.isInvalidYear(this.exportM.year)) {
        return;
      }
    }


    if (this.users.length == 0) {
      return;
    }

    let exportModel = this.exportM;
    if (exportModel.format.length === 0) {
      return;
    }

    let yearQuery;
    if(this.exportM.yearPeriod){
      const yearFromQ = yearFrom=='' ? '*' : yearFrom;
      const yearToQ = yearTo=='' ? '*' : yearTo;
      yearQuery = "yearNew=%5B" + yearFromQ +"%20TO%20"+ yearToQ +"%5D&";
    } else {
      yearQuery = this.exportM.year==""?"":"yearNew=" + this.exportM.year+"&";
    }
    let query = this.users.map(u => `${u.pid}`).join(",");
    let partOf = this.isPartOfEnabled() ? `partOf=${exportModel.partOf}&` : ``;

    if (exportModel.format == 'pdf' || exportModel.format == 'html') {
      this.result.link =
          `${this.getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}${partOf}` +
          `${this.getSortString()}${exportModel.style.length == 0 ? '' : '&style=' + exportModel.style}`;
      return;
    } else {
      this.result.link =
          `${this.getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}${partOf}` +
          `${this.getSortString()}`;
    }
  }

  private getSortString(): string {
    let filtered = this.exportM.sort
        .filter((sort) => sort.active);
    return filtered
        .map((sort) => {
          return `sortField=${sort.field}&sortDirection=${sort.asc ? "asc" : "desc"}`;
        }).join("&");
  }

  private styleChange() {
    this.clearLink();
    this.createLink();
    return;
  }

  private moveSortDown(field: SortField): void {
    let number = this.exportM.sort.indexOf(field);
    this.exportM.sort.splice(number, 1);
    this.exportM.sort.splice(number + 1, 0, field);
    this.sortChange();
  }

  private moveSortUp(field: SortField): void {
    let number = this.exportM.sort.indexOf(field);
    this.moveSortDown(this.exportM.sort[number - 1]);
    this.sortChange();
  }

  private clearLink() {
    this.result.link = "";
  }

  async searchSolrForPerson(name: string): Promise<User[]> {
    this.search.errored = false;
    this.search.searching = true;
    const nameSearch = name.replace(/[, ]/g,"*");
    const nameReversed = nameSearch.split('*').reverse().join("*");
    let roleQuery = this.getRoleQuery();
    let response = await
        fetch(`${this.getWebApplicationBaseURL()}servlets/solr/select?q=name_id_connection:* AND (name:*${nameSearch}* OR name:*${nameReversed}* OR name_id_${this.leadid}:*${name}*)${roleQuery}&group=true&group.field=name_id_connection&fl=*&wt=json`);
    if (response.ok) {
      let json = await response.json();
      this.search.searching = false;

      let results = [];
      for (const {doclist} of json.grouped.name_id_connection.groups) {
        let doc = doclist.docs[0];

        const otherIds: Identifier = {};
        for (const prop in doc) {
          if (prop.startsWith("name_id_") && doc[prop] != undefined && doc[prop].length > 0) {
            const idName = prop.substr("name_id_".length);
            if(this.personids.split(",").indexOf(idName) != -1){
              otherIds[idName] = doc[prop];
            }
          }
        }

        const result: User = {name: doc.name, pid: doc.name_id_connection[0], otherIds: otherIds};
        if (result.name && result.pid) {
          results.push(result);
        }
      }
      return results;
    }
    throw new Error("Error while request person from solr: " + response.statusText);
  }

  private getRoleQuery() {
    console.log(this.roles);
    if (this.roles != undefined && this.roles.trim().length > 0) {
      return " AND (" + this.roles.split(",")
          .map(role => `role:${role}`)
          .join(" OR ") + ")";
    }
    return "";
  }

  private getWebApplicationBaseURL() {
    return this.baseurl;
  }

}

export interface SearchModel {
  text: string,
  searchResultUsers: User[],
  searching: boolean,
  noresults: boolean,
  errored: boolean
}

export type SortField = { active: boolean, field: string, asc: boolean, i18nKey: string };

export interface ExportModel {
  format: string;
  style: string;
  sort: SortField[];
  year: string,
  partOf: boolean
  yearPeriod: false,
  yearFrom: string,
  yearTo: string
}

export interface StyleDescription {
  id: string,
  title: string
}

export interface User {
  name: string;
  pid: string;
  otherIds: Identifier;
}

export interface Identifier {
  [key: string]: string[]
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.plSort-move {
  transition: transform 0.5s;
}

.results-enter,.results-move, .results-leave, .used-enter, .used-move, .used-leave {
  transition: all 1s;
}

.yearRange {
  width: 60%;
}

</style>
