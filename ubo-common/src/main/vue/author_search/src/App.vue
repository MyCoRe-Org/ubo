<template>
  <div id="app" class="row">
    <link v-if="bootstrap" v-bind:href="bootstrap" rel="stylesheet">
    <link v-if="fontawesome" v-bind:href="fontawesome" rel="stylesheet">
    <template v-if="!formSend">
      <div class="col-12 col-lg-6">
        <PersonSearchForm v-on:search="search"
                          :firstname="firstname"
                          :lastname="lastname"
                          :baseurl="baseurl"/>
        <PersonSearchResult :searchresults="searchResults"
                            :error="error"
                            :searching="searching"
                            :baseurl="baseurl"
                            v-on:person_submitted="personSubmitted"
                            v-on:person_applied="personApplied"
                            v-on:id_applied="idApplied"
        />
      </div>
      <div class="col-12 col-lg-6">
        <PersonEditForm :person="personModel"
                        :isadmin="isadmin"
                        :searched="searched"
                        v-on:submit="personSubmitted"
                        v-on:cancel="cancel"
                        :baseurl="baseurl"

        />
      </div>
    </template>
    <div v-else class="col-12 d-flex justify-content-center">
      <div class="spinner-border" role="status">
        <span class="sr-only">Loading...</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import PersonSearchForm from './components/PersonSearchForm.vue';
import PersonEditForm from './components/PersonEditForm.vue';
import PersonSearchResult from "@/components/PersonSearchResult.vue";
import {PersonResult, SearchResult} from "@/components/SearchResult";

@Component({
  components: {
    PersonSearchForm: PersonSearchForm,
    PersonEditForm: PersonEditForm,
    PersonSearchResult: PersonSearchResult,
  },
})
export default class App extends Vue {

  @Prop({
    default: "http://localhost:8080/"
  }) baseurl!: string;

  @Prop(
      {default: "http://localhost:8080/rsc/sass/scss/bootstrap-ubo.min.css"}
  ) bootstrap!: string;

  @Prop({
    default: "http://localhost:8080/webjars/font-awesome/5.13.0/css/all.css"
  }) fontawesome!: string;

  @Prop({
    default: ""
  }) firstname!: string;

  @Prop({
    default: ""
  }) lastname!: string;

  @Prop({
    default: ""
  }) pid!: string;

  @Prop({
    default: ""
  }) sessionid!: string;

  @Prop({
    default: ""
  }) pidtype!: string;

  @Prop({
    default: "false",
  }) isadmin!: string;

  private searched = false;

  searchResults: SearchResult | null = null;
  searching = false;
  error = false;
  formSend = false;

  personModel = {
    firstName: this.firstname,
    lastName: this.lastname,
    pid: this.pid
  }

  public search({term}: { term: string }) {
    this.searching = true;
    this.performSearch(term).then(result => this.searchResults = result);
  }

  public async performSearch(query: string): Promise<SearchResult> {
    this.searching = true;
    this.error = false;
    this.searched = true;
    try {
      let response = await fetch(this.baseurl + "rsc/search/person?query=" + encodeURIComponent(query));
      return await response.json();
    } catch (e) {
      this.error = true;
      throw e;
    } finally {
      this.searching = false;
    }
  }

  public personSubmitted(person: PersonResult | null) {
    if (person !== null) {
      this.personApplied(person);
    }
    let url = this.baseurl + "servlets/XEditor?_xed_submit_return=&_xed_session=" + this.sessionid;

    const xpFam = "mods:namePart[@type='family']";
    const xpGiven = "mods:namePart[@type='given']";
    const xpPid = "mods:nameIdentifier[@type='" + this.pidtype + "']";

    const parms = [encodeURIComponent(xpFam) + "=" + encodeURIComponent(this.personModel.lastName),
      encodeURIComponent(xpGiven) + "=" + encodeURIComponent(this.personModel.firstName)]

    if (this.personModel.pid.length > 0 || this.pid != "") {
      parms.push(encodeURIComponent(xpPid) + "=" + encodeURIComponent(this.personModel.pid));
    }

    let urlParams = new URLSearchParams(window.location.href);
    url += "&" + parms.join("&") + (urlParams.has("xEditorHook") ? "#" + urlParams.get("xEditorHook") : "");

    if (!this.formSend) {
      window.location.assign(url);
      this.formSend = true;
    }
  }

  public cancel() {
    window.location.assign(this.baseurl + "servlets/XEditor?_xed_submit_return_cancel=&_xed_session=" + this.sessionid);
  }

  public personApplied(person: PersonResult) {
    if (person.firstName && person.lastName) {
      this.personModel.firstName = person.firstName;
      this.personModel.lastName = person.lastName;
    } else {
      [this.personModel.firstName, this.personModel.lastName] = person.displayName.split(" ");
    }
    this.personModel.pid = person.pid;
  }

  public idApplied(pid: string) {
    this.personModel.pid = pid;
  }
}
</script>

<style>

</style>
