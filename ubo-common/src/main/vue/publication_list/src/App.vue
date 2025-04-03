<template>
  <div class="card mb1" v-if="i18n['listWizard.info.text1'].length > 0">
    <div class="card-body">
      <section v-html="i18n['listWizard.info.text1']"></section>
    </div>
  </div>
  <div class="card mb1" v-if="i18n['listWizard.info.text2'].length > 0">
    <div class="card-body">
      <section v-html="i18n['listWizard.info.text2']"></section>
    </div>
  </div>
  <div class="card mb1" v-if="i18n['listWizard.info.text3'].length > 0">
    <div class="card-body">
      <section v-html="i18n['listWizard.info.text3']"></section>
    </div>
  </div>
  <article class="card mb1">
    <div class="card-body">
      <div class="ubo-vue-form">
        <section>
          <div class="form-group form-inline">
            <label class="mycore-form-label"
                   for="personSearch">{{ i18n["listWizard.search"] }}</label>
            <input id="personSearch" class="mycore-form-input" type="text"
                   v-model="searchModel.text"
                   v-on:keypress.enter.prevent="startSearch">
            <span class="input-group-btn">
          <button class="btn btn-secondary" v-on:click.prevent="startSearch">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"
                  v-if="searchModel.searching"></span>
            {{ i18n["button.search"] }}
          </button>
        </span>
          </div>
        </section>
        <section v-if="searchModel.errored">
          <div class="ubo-input-error">
            {{ i18n["error.occurred"] }}
          </div>
        </section>
        <section v-if="searchModel.searchResultUsers.length>0 || searchModel.noresults">
          <div class="ubo-pl-noresults">
            <b>{{ i18n["result.dozbib.results"] }}</b>
            <div v-if="searchModel.noresults" v-on:click="resetSearch()">
              <a href="javascript:void(0)">{{ i18n["index.person.found.0"] }}</a>
            </div>
          </div>
          <div class="ubo-pl-results">
            <transition-group name="results" tag="div" class="list-group list-group-flush">
              <a class="list-group-item list-group-item-action"
                 v-for="autocompleteUser in searchModel.searchResultUsers"
                 :key="autocompleteUser.pid"
                 v-on:click="addUser(autocompleteUser)"
                 href="javascript:void(0)">
                <i class="fas fa-plus-circle ubo-pl-adduser"></i>
                {{ autocompleteUser.name }}
                <i v-if="Object.keys(autocompleteUser.otherIds).length>0"
                   v-on:click.prevent.stop="userInfoShown[autocompleteUser.pid] = !userInfoShown[autocompleteUser.pid]"
                   :id="'popover-results-' + autocompleteUser.pid"
                   class="fa fa-info-circle"></i>
                <div class="card card-body" v-on:click.prevent.stop=""
                     v-if="userInfoShown[autocompleteUser.pid]">
                  <dl>
                    <template v-for="(arr,type) in autocompleteUser.otherIds" :key="type">
                      <dt>{{ i18n["index.person.id." + type] }} {{ type }}</dt>
                      <dd>{{ arr.join(", ") }}</dd>
                    </template>
                  </dl>
                </div>
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
                   v-on:click.prevent.stop="userInfoShown[user.pid] = !userInfoShown[user.pid]"
                   class="fa fa-info-circle"></i>
                <div class="card card-body" v-on:click.prevent.stop=""
                     v-if="userInfoShown[user.pid]">
                  <dl>
                    <template v-for="(arr, type) in user.otherIds" :key="type">
                      <dt>{{ i18n["index.person.id." + type] }}</dt>
                      <dd>{{ arr.join(", ") }}</dd>
                    </template>
                  </dl>
                </div>
              </a>
            </transition-group>
          </div>
        </section>
        <section>
          <div class="form-group row">
            <label class="mycore-form-label" for="yearIssued">
              {{ i18n["search.dozbib.year.publication"] }}
            </label>
            <div class="col-8 form-check mycore-list list-group list-group-flush">
              <div class="list-group-item d-flex align-items-center">
                <div class="col-4">
                  <input id="dateRangeLabel" v-model="exportModel.yearPeriod"
                         class="form-check-input" type="checkbox" v-on:change="yearChange">
                  <label class="form-check-label"
                         for="dateRangeLabel">{{ i18n["search.dozbib.year.period"] }}</label>
                </div>
                <div class="col-8">
                  <input
                    v-if="!exportModel.yearPeriod"
                    id="yearIssued"
                    v-model="exportModel.year"
                    class="mycore-form-input"
                    type="number"
                    min="1900"
                    max="2099"
                    step="1"
                    v-bind:class="{ 'is-invalid' : isInvalidYear(exportModel.year) }"
                    v-on:change="yearChange">
                  <div v-else class="input-group yearRange">
                    <input id="searchDate" v-model="exportModel.yearFrom"
                           class="form-control" placeholder="" type="number" min="1900" max="2099"
                           step="1"
                           v-bind:class="{ 'is-invalid' : isInvalidYear(exportModel.yearFrom, exportModel.yearTo) }"
                           v-on:change="yearChange">
                  <div class="input-group-between">
                    <div class="input-group-text">-</div>
                  </div>
                    <input id="searchDate" v-model="exportModel.yearTo" class="form-control"
                           placeholder="" type="number" min="1900" max="2099" step="1"
                           v-bind:class="{ 'is-invalid' : isInvalidYear(exportModel.yearFrom, exportModel.yearTo) }"
                           v-on:change="yearChange">
                  </div>
                </div>
              </div>
            </div>
            <div class="ubo-input-invalid invalid-feedback">
              {{ i18n["search.dozbib.year.invalid"] }}
            </div>
          </div>
        </section>
        <section v-if="isPartOfEnabled()">
          <div class="form-group row form-linline">
            <label class="mycore-form-label" for="partOf">{{ i18n["ubo.partOf"] }}</label>
            <div class="input-group col-8">
              <input id="partOf" v-model="exportModel.partOf"
                     type="checkbox"
                     v-on:change="partOfChange">
            </div>
          </div>
        </section>
        <section>
          <div class="form-group row">
            <label class="mycore-form-label">{{ i18n["search.sort"] }}</label>
            <transition-group name="plSort" tag="div"
                              class="col-8 mycore-list list-group list-group-flush">
              <div v-for="(sort,i) in exportModel.sort" :key="sort.field"
                   class="list-group-item d-flex align-items-center">
                <div class="col">
                  <input class="form-check-input" :id="'ps_select_' + sort.field"
                         v-on:change="sortChange" type="checkbox"
                         v-model="sort.active">
                  <label class="form-check-label"
                         :for="'ps_select_' + sort.field">{{ i18n[sort.i18nKey] }}</label>
                </div>
                <div class="col">
                  <select class="mycore-form-input custom-select"
                          :id="'ps_radio_' + sort.field"
                          v-on:change="sortChange"
                          v-model="sort.asc">
                    <option v-bind:value="true">{{ i18n["search.sort.asc"] }}</option>
                    <option v-bind:value="false">{{ i18n["search.sort.desc"] }}</option>
                  </select>
                </div>
                <div class="col">
                  <div class="btn-group">
                    <button v-bind:disabled="i<=0" v-on:click.prevent="moveSortUp(sort)"
                            class="btn btn-primary up"
                            tabindex="999">
                      <i class="fas fa-arrow-up"></i>
                    </button>
                    <button v-bind:disabled="!(i<exportModel.sort.length-1)"
                            v-on:click.prevent="moveSortDown(sort)"
                            class="btn btn-primary down">
                      <i class="fas fa-arrow-down"></i>
                    </button>
                  </div>
                </div>
              </div>
            </transition-group>
          </div>
          <div class="form-group form-inline">
            <label class="mycore-form-label" for="formatSelect">{{
                i18n["listWizard.format"]
              }}</label>
            <select id="formatSelect" class="mycore-form-input custom-select"
                    v-on:change="formatChange" v-model="exportModel.format">
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
          <div class="form-group form-inline"
               v-if="exportModel.format==='html' || exportModel.format==='pdf'">
            <label class="mycore-form-label" for="styleSelect">{{
                i18n["listWizard.citation"]
              }}</label>
            <select id="styleSelect" class="mycore-form-input custom-select"
                    v-on:change="styleChanged" v-model="exportModel.style">
              <option v-bind:value="''"></option>
              <option v-for="style in styles" v-bind:key="style.id" v-bind:value="style.id">
                {{ style.title }}
              </option>
            </select>
          </div>
        </section>
        <section v-if="result.link.length>0">
          <div class="row">
            <div class="col-12">
              <div class="jumbotron">
                <!-- when the format is html, we will show the link (which is a iframe) as code -->
                <p class="text-primary" v-if="exportModel.format==='html'">
                  {{ i18n["listWizard.code"] }}</p>
                <code v-if="exportModel.format==='html'">&lt;iframe scrolling=&quot;yes&quot; width=&quot;90%&quot;
                  height=&quot;300&quot;
                  src=&quot;{{ result.link }}&quot;/&gt;</code>

                <!-- when format is not html, we just show it as link -->
                <p class="text-primary" v-if="exportModel.format!=='html'">
                  {{ i18n["listWizard.link"] }}</p>
                <a v-if="exportModel.format!=='html'" :href="result.link">{{ result.link }}</a>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </article>
  <div class="card mb1" v-if="i18n['listWizard.info.text4'].length > 0">
    <div class="card-body">
      <section v-html="i18n['listWizard.info.text4']"></section>
    </div>
  </div>
</template>

<script lang="ts" setup>

import {onMounted, reactive} from "vue";
import type {
  ExportModel,
  Identifier,
  PropResponse,
  SearchModel,
  SortField,
  StyleDescription,
  User
} from "./Model";

const userInfoShown = reactive({} as { [key: string]: boolean });

const searchModel = reactive({
  text: "",
  searchResultUsers: [] as User[],
  searching: false,
  noresults: false,
  errored: false
} as SearchModel);

const exportModel = reactive({
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
} as ExportModel);

const propResponse = reactive({} as PropResponse)

const result = reactive({
  link: ""
})

const i18n = reactive({
  "button.search": "",
  "error.occurred": "",
  "index.person.found.0": "",
  "listWizard.users": "",
  "search.select": "",
  "search.sort": "",
  "search.sort.year": "",
  "search.sort.name": "",
  "search.sort.title": "",
  "search.sort.asc": "",
  "search.sort.desc": "",
  "listWizard.link": "",
  "listWizard.code": "",
  "listWizard.format": "",
  "listWizard.citation": "",
  "listWizard.info.text1": "",
  "listWizard.info.text2": "",
  "listWizard.info.text3": "",
  "listWizard.info.text4": "",
  "result.dozbib.results": "",
  "search.dozbib.year.publication": "",
  "search.dozbib.year.invalid": "",
  "listWizard.search": "",
  "index.person.id.*": "",
  "ubo.partOf": "",
  "search.dozbib.year.period": "null"
} as { [key: string]: string });

const styles = reactive([] as StyleDescription[]);

const users = reactive([] as User[]);


onMounted(() => {
  resolveStyles();
  resolveiI18N();
  resolveWizardConfig();
});

const getWebApplicationBaseURL = () => {
  return (window as never)["webApplicationBaseURL"];
}


const resolveStyles = async () => {
  const response = await fetch(getWebApplicationBaseURL() + "rsc/export/styles");
  const resp = await response.json();
  for (const style of resp) {
    styles.push(style);
  }
}

const resolveiI18N = async () => {
  Object.keys(i18n)
    .map(k => {
      const url = getWebApplicationBaseURL() + "rsc/locale/translate/" + k;
      fetch(url).then(result => {
        if (k.endsWith("*")) {
          result.json().then(obj => {
            Object.keys(obj).forEach(key => {
              i18n[key] = obj[key];
            })
          })
        } else {
          result.text().then(translation => {
            i18n[k] = translation;
          });
        }
      })
    });
}

const resolveWizardConfig = async () => {
  const response = await fetch(getWebApplicationBaseURL() + "/wizard-config.json");
  const resp = await response.json();

  if (typeof resp["UBO.Editor.PartOf.Enabled"] == "string") {
    propResponse["UBO.Editor.PartOf.Enabled"] = resp["UBO.Editor.PartOf.Enabled"];
  }

  if (typeof resp["UBO.Search.PersonalList.Ids"] == "string") {
    propResponse["UBO.Search.PersonalList.Ids"] = resp["UBO.Search.PersonalList.Ids"];
  }

  if (typeof resp["UBO.Search.PersonalList.Roles"] == "string") {
    propResponse["UBO.Search.PersonalList.Roles"] = resp["UBO.Search.PersonalList.Roles"];
  }

  if (typeof resp["MCR.user2.matching.lead_id"] == "string") {
    propResponse["MCR.user2.matching.lead_id"] = resp["MCR.user2.matching.lead_id"];
  }

}


const isPartOfEnabled = () => {
  return typeof propResponse["UBO.Editor.PartOf.Enabled"] == "string" && propResponse["UBO.Editor.PartOf.Enabled"].toLowerCase() === "true"
}

const isInvalidYear = (yearStr: string, yearStr2?: string) => {
  if (yearStr2) {
    if (isInvalidYear(yearStr)) {
      return true;
    }
    if (isInvalidYear(yearStr2)) {
      return true;
    }

    const yearF = yearStr == "" ? 0 : parseInt(yearStr);
    const yearT = yearStr2 == "" ? new Date().getFullYear() : parseInt(yearStr2);

    return yearF > yearT;
  } else {
    if (yearStr == "") {
      return false;
    }
    const year = parseInt(yearStr);
    return isNaN(year) || year > new Date().getFullYear() || year < 0;
  }
}

const resetSearch = () => {
  while (searchModel.searchResultUsers.pop() != undefined) {
    // make it empty
  }
  searchModel.noresults = false;
}

const startSearch = () => {
  console.log("trigger");
  resetSearch();
  const currentPromise: Promise<User[]> = searchSolrForPerson(searchModel.text);
  currentPromise.then(userArray => {
    if (userArray.length == 0) {
      searchModel.noresults = true;
    }
    for (const foundUser of userArray) {
      searchModel.searchResultUsers.push(foundUser);
    }
  }).catch((e) => {
    console.error(e);
    searchModel.errored = true;
  });
}

const addUser = (user: User) => {
  if (users.filter(u => u.pid == user.pid).length == 0) {
    users.push(user);
  }
  searchModel.searchResultUsers.splice(searchModel.searchResultUsers.indexOf(user), 1);
  createLink();
}

const removeUser = (user: User) => {
  const number = users.indexOf(user);
  users.splice(number, 1);
  createLink();
}

const clearLink = () => {
  result.link = "";
}

const createLink = () => {
  clearLink();

  const yearTo = exportModel.yearTo;
  const yearFrom = exportModel.yearFrom;
  if (exportModel.yearPeriod) {
    if (isInvalidYear(yearFrom, yearTo)) {
      return;
    }
  } else {
    if (isInvalidYear(exportModel.year)) {
      return;
    }
  }


  if (users.length == 0) {
    return;
  }

  if (exportModel.format.length === 0) {
    return;
  }

  let yearQuery;
  if (exportModel.yearPeriod) {
    const yearFromQ = yearFrom == '' ? '*' : yearFrom;
    const yearToQ = yearTo == '' ? '*' : yearTo;
    yearQuery = "yearNew=%5B" + yearFromQ + "%20TO%20" + yearToQ + "%5D&";
  } else {
    yearQuery = exportModel.year == "" ? "" : "yearNew=" + exportModel.year + "&";
  }
  const query = users.map(u => `${u.pid}`).join(",");
  const partOf = isPartOfEnabled() ? `partOf=${exportModel.partOf}&` : ``;

  if (exportModel.format == 'pdf' || exportModel.format == 'html') {
    result.link =
      `${getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}${partOf}` +
      `${getSortString()}${exportModel.style.length == 0 ? '' : '&style=' + exportModel.style}`;
    return;
  } else {
    result.link =
      `${getWebApplicationBaseURL()}rsc/export/link/${exportModel.format}/${query}?${yearQuery}${partOf}` +
      `${getSortString()}`;
  }
}

const formatChange = () => {
  createLink();
}

const sortChange = () => {
  createLink();
}

const yearChange = () => {
  createLink();
}

const partOfChange = () => {
  createLink();
}

const getSortString = () => {
  const filtered = exportModel.sort
    .filter((sort) => sort.active);
  return filtered
    .map((sort) => {
      return `sortField=${sort.field}&sortDirection=${sort.asc ? "asc" : "desc"}`;
    }).join("&");
}

const styleChanged = () => {
  clearLink();
  createLink();
}

const moveSortDown = (field: SortField) => {
  const number = exportModel.sort.indexOf(field);
  exportModel.sort.splice(number, 1);
  exportModel.sort.splice(number + 1, 0, field);
  sortChange();
}

const moveSortUp = (field: SortField) => {
  const number = exportModel.sort.indexOf(field);
  moveSortDown(exportModel.sort[number - 1]);
  sortChange();
}


const searchSolrForPerson = async (name: string): Promise<User[]> => {
  searchModel.errored = false;
  searchModel.searching = true;
  const nameSearch = name.replace(/[, ]/g, "*");
  const nameReversed = nameSearch.split('*').reverse().join("*");
  const roleQuery = getRoleQuery();
  const response = await
    fetch(`${getWebApplicationBaseURL()}servlets/solr/select?q=name_id_connection:* AND (name:*${nameSearch}* OR name:*${nameReversed}* OR name_id_${propResponse["MCR.user2.matching.lead_id"]}:*${name}*)${roleQuery}&group=true&group.field=name_id_connection&fl=*&wt=json`);
  if (response.ok) {
    const json = await response.json();
    searchModel.searching = false;

    const results = [];
    for (const {doclist} of json.grouped.name_id_connection.groups) {
      const doc = doclist.docs[0];

      const otherIds: Identifier = {};
      for (const prop in doc) {
        if (prop.startsWith("name_id_") && doc[prop] != undefined && doc[prop].length > 0) {
          const idName = prop.substr("name_id_".length);
          if (propResponse["UBO.Search.PersonalList.Ids"]?.split(",").indexOf(idName) != -1) {
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

const getRoleQuery = () => {
  const roles = propResponse["UBO.Search.PersonalList.Roles"];
  if (roles != undefined && roles.trim().length > 0) {
    return " AND (" + roles.split(",")
      .map(role => `role:${role}`)
      .join(" OR ") + ")";
  }
  return "";
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
