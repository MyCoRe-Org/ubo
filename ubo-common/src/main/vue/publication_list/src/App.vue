<template>
  <div class="container">
    <link v-if="bootstrap" rel="stylesheet" :href="bootstrap">
    <link v-if="css" rel="stylesheet" :href="css">
    <section class="row">
      <div class="col-12">
        <div class="input-group">
          <input class="form-control" type="text" v-model="search.text" v-on:keyup.enter="startSearch">
          <span class="input-group-btn">
            <button class="btn btn-secondary" v-on:click="startSearch">
              <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"
                    v-if="search.searching"></span>
              {{ i18n["button.search"] }}
            </button>
          </span>
        </div>
      </div>
    </section>
    <section class="row mt-2" v-if="search.errored">
      <div class="col-12">
        <div class="p-3 mb-2 bg-danger text-white">
          {{ i18n["error.occurred"] }}
        </div>
      </div>
    </section>
    <section class="row mt-2" v-if="search.searchResultUsers.length>0 || search.noresults">
      <div class="col-12">
        <b>{{ i18n["result.dozbib.results"] }}</b>
        <div v-if="search.noresults" v-on:click="resetSearch()" class="p-3 mb-2 bg-info text-white">
          <a href="javascript:void(0)">{{ i18n["index.person.found.0"] }}</a>
        </div>
        <div class="list-group list-group-flush">
          <a class="list-group-item list-group-item-action"
             v-for="autocompleteUser in search.searchResultUsers"
             :key="autocompleteUser.pid"
             v-on:click="addUser(autocompleteUser)"
             href="javascript:void(0)">
            {{ autocompleteUser.name }}
          </a>
        </div>
      </div>
    </section>
    <section class="row mt-2" v-if="users.length>0">
      <div class="col-12">
        <b>{{ i18n["listWizard.users"] }}</b>
        <div class="list-group list-group-flush">
          <a class="list-group-item list-group-item-action"
             v-for="user in users"
             :key="user.pid"
             v-on:click="removeUser(user)"
             href="javascript:void(0)">
            {{ user.name }}
          </a>
        </div>
      </div>
    </section>
    <section class="row mt-2">
      <div class="col-12">
        <label for="sortSelect">{{i18n["search.sort"]}}</label>
        <select id="sortSelect" class="form-control" v-on:change="sortChange" v-model="exportM.sortField">
          <option v-bind:value="'year'">{{i18n["search.sort.year"]}}</option>
          <option v-bind:value="'sortby_person'">{{ i18n["search.sort.name"] }}</option>
          <option v-bind:value="'sortby_title'">{{ i18n["search.sort.title"] }}</option>
        </select>
        <div class="form-check">
          <input class="form-check-input" v-on:change="sortChange" type="checkbox" id="ascSort" v-model="exportM.asc">
          <label class="form-check-label" for="ascSort">{{i18n["search.sort.asc"]}}</label>
        </div>
      </div>
      <div class="col-12 mt-2">
        <label for="formatSelect">{{ i18n["listWizard.format"] }}</label>
        <select id="formatSelect" class="form-control" v-on:change="formatChange" v-model="exportM.format">
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
      <div class="col-12" v-if="exportM.format==='html' || exportM.format==='pdf'">
        <label for="styleSelect">{{ i18n["listWizard.citation"] }}</label>
        <select id="styleSelect" class="form-control" v-on:change="styleChange" v-model="exportM.style">
          <option v-bind:value="''">{{ i18n["search.select"] }}</option>
          <option v-for="style in styles" v-bind:key="style.id" v-bind:value="style.id">{{style.title}}</option>
        </select>
      </div>
    </section>
    <section class="row mt-2" v-if="result.link.length>0">
      <div class="col-12">
        <!-- when the format is html, we will show the link (which is a iframe) as code -->
        <p v-if="exportM.format==='html'">{{ i18n["listWizard.code"] }}</p>
        <code v-if="exportM.format==='html'">&lt;iframe scrolling=&quot;yes&quot; width=&quot;90%&quot; height=&quot;300&quot;
          src=&quot;{{result.link}}&quot;/&gt;</code>

        <!-- when format is not html, we just show it as link -->
        <p v-if="exportM.format!=='html'">{{ i18n["listWizard.link"] }}</p>
        <a v-if="exportM.format!=='html'" :href="result.link">{{result.link}}</a>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';


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
    sortField: "year",
    asc: true
  };

  private result = {
    link: ""
  }

  private styles: any[] = []

  private i18n: any = {
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
    "listWizard.link": null,
    "listWizard.code": null,
    "listWizard.format": null,
    "listWizard.citation": null,
    "result.dozbib.results": null
  };

  private users: User[] = [];

  created() {
    this.resolveStyles();
    this.resolveiI18N();
  }

  private async resolveStyles() {
    let response = await fetch(this.getWebApplicationBaseURL() + "rsc/csl/styles");
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
    this.search.searchResultUsers.length = 0;
    this.search.noresults = false;
  }

  private addUser(user: User) {
    if (this.users.filter(u => u.pid == user.pid).length == 0) {
      this.users.push(user);
    }
    this.resetSearch();
    this.createLink();
  }

  private removeUser(user: User): void {
    let number = this.users.indexOf(user);
    this.users.splice(number, 1);
    this.createLink();
  }

  private formatChange() {
    this.createLink();
  }

  private sortChange() {
    this.createLink();
    return;
  }

  private createLink() {
    this.clearLink();

    if (this.users.length == 0) {
      return;
    }

    let exportModel = this.exportM;
    if (exportModel.format.length === 0) {
      return;
    }

    if (exportModel.format == 'pdf' || exportModel.format == 'html') {
      if (exportModel.style.length == 0) {
        return;
      }

      let query = this.users.map(u => `"${u.pid}"`).join(" OR ");
      let link =
          `${this.getWebApplicationBaseURL()}servlets/solr/select?q=nid_connection:(${query})&rows=99999` +
          `&XSL.Transformer=response-csl-${exportModel.format}` +
          `&sort=${exportModel.sortField} ${exportModel.asc ? "asc" : "desc"}&XSL.style=${exportModel.style}`;

      this.result.link = link;
      return;
    } else {
      let query = this.users.map(u => `"${u.pid}"`).join(" OR ");
      this.result.link =
          `${this.getWebApplicationBaseURL()}servlets/solr/select?q=nid_connection:(${query})&rows=99999&XSL.Transformer=${exportModel.format}` +
          `&sort=${exportModel.sortField} ${exportModel.asc ? "asc" : "desc"}`;
    }
  }

  private styleChange() {
    this.clearLink();
    this.createLink();
    return;
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

export interface ExportModel {
  format: string;
  style: string;
  sortField: string;
  asc: boolean;
}

export interface User {
  name: string;
  pid: string;
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
