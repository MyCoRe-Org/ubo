<template>
  <div class="ubo-vue-form">
    <link v-if="bootstrap" rel="stylesheet" :href="bootstrap">
    <link v-if="css" rel="stylesheet" :href="css">
    <link v-if="fontawesome" rel="stylesheet" :href="fontawesome">
    <section>
      <div class="form-group form-inline">
        <label class="mycore-form-label" for="personSearch">{{i18n["listWizard.search"]}}</label>
        <input id="personSearch" class="mycore-form-input" type="text" v-model="search.text"
               v-on:keyup.enter="startSearch">
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
          </a>
        </transition-group>
      </div>
    </section>
    <section>
      <div class="form-group form-inline">
        <label class="mycore-form-label" for="yearIssued">{{i18n["search.dozbib.year.publication"]}}</label>
        <div class="input-group">
          <div class="input-group-prepend">
            <span class="input-group-text">&ge;</span>
          </div>
          <input id="yearIssued" v-model="exportM.year" class="mycore-form-input"
                 v-bind:class="{ 'is-invalid' : isInvalidYear() }" v-on:change="yearChange"
                 type="number">
        </div>
        <div class="ubo-input-invalid invalid-feedback">
          {{i18n["search.dozbib.year.invalid"]}}
        </div>
      </div>
    </section>
    <section>
      <div class="form-group row">
        <label class="mycore-form-label">{{i18n["search.sort"]}}</label>
        <transition-group name="plSort" tag="div" class="col-8 list-group list-group-flush">
          <div v-for="(sort,i) in exportM.sort" :key="sort.field" class="list-group-item d-flex align-items-center">
            <div class="col">
              <input class="form-check-input" :id="'ps_select_' + sort.field" v-on:change="sortChange" type="checkbox"
                     v-model="sort.active">
              <label class="form-check-label" :for="'ps_select_' + sort.field">{{i18n[sort.i18nKey]}}</label>
            </div>
            <div class="col">
              <input class="form-check-input"
                     :id="'ps_radio_asc_' + sort.field"
                     v-on:change="sortChange"
                     type="radio"
                     v-model="sort.asc"
                     v-bind:value="true">
              <label class="form-check-label" :for="'ps_radio_asc_' + sort.field">{{i18n["search.sort.asc"]}}</label>
            </div>
            <div class="col">
              <input class="form-check-input"
                     :id="'ps_radio_desc_' + sort.field"
                     v-on:change="sortChange"
                     type="radio"
                     v-model="sort.asc"
                     v-bind:value="false">
              <label class="form-check-label" :for="'ps_radio_desc_' + sort.field">{{i18n["search.sort.desc"]}}</label>
            </div>
            <div class="col">
              <div class="btn-group">
              <button v-bind:disabled="i<=0" v-on:click.prevent="moveSortUp(sort)" class="btn btn-secondary"
                      tabindex="999">
                ↑
              </button>
              <button v-bind:disabled="!(i<exportM.sort.length-1)" v-on:click.prevent="moveSortDown(sort)"
                      class="btn btn-secondary">
                ↓
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
          <option v-bind:value="'mods2csv'">CSV</option>
        </select>
      </div>
      <div class="form-group form-inline" v-if="exportM.format==='html' || exportM.format==='pdf'">
        <label class="mycore-form-label" for="styleSelect">{{ i18n["listWizard.citation"] }}</label>
        <select id="styleSelect" class="mycore-form-input custom-select" v-on:change="styleChange" v-model="exportM.style">
          <option v-bind:value="''">{{ i18n["search.select"] }}</option>
          <option v-for="style in styles" v-bind:key="style.id" v-bind:value="style.id">{{style.title}}</option>
        </select>
      </div>
    </section>
    <section v-if="result.link.length>0">
      <div class="row">
        <div class="col-12">
          <!-- when the format is html, we will show the link (which is a iframe) as code -->
          <p v-if="exportM.format==='html'">{{ i18n["listWizard.code"] }}</p>
          <code v-if="exportM.format==='html'">&lt;iframe scrolling=&quot;yes&quot; width=&quot;90%&quot; height=&quot;300&quot;
            src=&quot;{{result.link}}&quot;/&gt;</code>

          <!-- when format is not html, we just show it as link -->
          <p v-if="exportM.format!=='html'">{{ i18n["listWizard.link"] }}</p>
          <a v-if="exportM.format!=='html'" :href="result.link">{{result.link}}</a>
        </div>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import 'whatwg-fetch'


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
   * The url to fontawesome
   * @private
   */
  @Prop() private fontawesome!: string;

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
      {active: true, field: "sortby_person", asc: true, i18nKey: "search.sort.name"},
      {active: true, field: "sortby_title", asc: true, i18nKey: "search.sort.title"}
    ],
    year: "",
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
    "listWizard.search": null
  };

  private users: User[] = [];

  created(): void {
    this.resolveStyles();
    this.resolveiI18N();
  }

  private isInvalidYear() {
    if(this.exportM.year==""){
      return false;
    }
    let year = parseInt(this.exportM.year);
    return isNaN(year) || year > new Date().getFullYear() || year < 0;
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
            result.text().then(translation => {
              this.i18n[k] = translation;
            });
          })
        });
  }

  private startSearch(): void {
    this.resetSearch();
    let currentPromise: Promise<User[]> = this.searchSolrForPerson(this.search.text);
    currentPromise.then(userArray => {
      if (userArray.length == 0) {
        this.search.noresults = true;
      }
      for (const foundUser of userArray) {
        this.search.searchResultUsers.push(foundUser);
      }
    }).catch(() => {
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

  private createLink() {
    this.clearLink();

    if (this.isInvalidYear()) {
      return;
    }

    if (this.users.length == 0) {
      return;
    }

    let exportModel = this.exportM;
    if (exportModel.format.length === 0) {
      return;
    }

    const yearQuery = this.exportM.year==""?"":"year=" + this.exportM.year+"&";
    let query = this.users.map(u => `${u.pid}`).join(",");
    if (exportModel.format == 'pdf' || exportModel.format == 'html') {
      if (exportModel.style.length == 0) {
        return;
      }

      this.result.link =
          `${this.getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}` +
          `${this.getSortString()}style=${exportModel.style}`;
      return;
    } else {
      this.result.link =
          `${this.getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}` +
          `${this.getSortString()}`;
    }
  }

  private getSortString(): string {
    let filtered = this.exportM.sort
        .filter((sort) => sort.active);
    return filtered
        .map((sort) => {
          return `sortField=${sort.field}&sortDirection=${sort.asc ? "asc" : "desc"}`;
        }).join("&") + (filtered.length > 0 ? "&" : "");
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
    let response = await
        fetch(`${this.getWebApplicationBaseURL()}servlets/solr/select?q=name_id_connection:* AND (name:*${name}* OR name_id_${this.leadid}:*${name}*)&group=true&group.field=name&fl=*&wt=json`);
    if (response.ok) {
      let json = await response.json();
      this.search.searching = false;

      let results = [];
      for (const {doclist} of json.grouped.name.groups) {
        let doc = doclist.docs[0];
        const result = {name: doc.name, pid: doc.name_id_connection[0]};
        if (result.name && result.pid) {
          results.push(result);
        }
      }
      return results;
    }
    throw new Error("Error while request person from solr: " + response.statusText);
  }

  private getWebApplicationBaseURL() {
    return this.baseurl ;
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
  year: string
}

export interface StyleDescription {
  id: string,
  title: string
}

export interface User {
  name: string;
  pid: string;
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


</style>
