<template>
  <div id="app" class="row" v-if="guiReady">
    <template v-if="!model.formSend">
      <div class="col-12 col-lg-6">
        <PersonSearchForm v-on:search="search"
                          :firstname="model.personModel.firstName"
                          :lastname="model.personModel.lastName"
                          :baseurl="getWebApplicationBaseURL()"/>
        <PersonSearchResult :searchresults="model.searchResults"
                            :error="model.error"
                            :searching="model.searching"
                            :baseurl="getWebApplicationBaseURL()"
                            v-on:person_submitted="personSubmitted"
                            v-on:person_applied="personResultApplied"
                            v-on:id_applied="idApplied"
        />
      </div>
      <div class="col-12 col-lg-6">
        <PersonEditForm :person="model.personModel"
                        :is-admin="model.isAdmin"
                        :searched="model.searched"
                        v-on:submit="personSubmitted"
                        v-on:cancel="cancel"
                        :baseurl="getWebApplicationBaseURL()"

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

<script lang="ts" setup>
import PersonSearchForm from './components/PersonSearchForm.vue';
import PersonEditForm from './components/PersonEditForm.vue';
import PersonSearchResult from "@/components/PersonSearchResult.vue";
import type {PersonResult, SearchResult} from "@/components/SearchResult";
import {computed, onMounted, reactive} from "vue";
import type {PropertyResponse} from "@/Model.ts";

const config: PropertyResponse = reactive({
});

const model = reactive({
  jwt: null as string | null,
  searched: false,
  searchResults:  null as SearchResult|null,
  searching: false,
  error:false,
  formSend: false,
  personModel: {
    firstName: "",
    lastName: "",
    pid: ""
  },
  isAdmin: false,
  pidType: null as string | null,
  sessionID: null as string | null
});

const guiReady = computed(() => {
  return model.sessionID !== null &&
    model.pidType !== null &&
    model.jwt !== null &&
    config["MCR.user2.matching.lead_id"] !== null;
});

const resolveJWT = async () => {
  model.jwt = await fetch(getWebApplicationBaseURL() + "rsc/jwt")
    .then(response => response.json())
    .then(json => json.access_token);
}

onMounted(async () => {
  await resolveConfig();
  await resolveJWT();
  model.pidType = getPidTypeFromConfig();
  model.isAdmin = getAdminStatusFromJWT();
  model.sessionID = getSessionIdFromURL();
  model.personModel.firstName = getFirstNameFromURL();
  model.personModel.lastName = getLastNameFromURL();
  model.personModel.pid = getPidFromURL();

  console.log(model.personModel);
  console.log(getFlattenedParamsFromURL());
});

const getWebApplicationBaseURL = () => {
  return (window as never)["webApplicationBaseURL"];
}

const resolveConfig = async () =>{
  const response = await fetch(getWebApplicationBaseURL() + "author-search-config.json");
  const jsonRepsponse = await response.json();
  if(!jsonRepsponse["MCR.user2.matching.lead_id"]) {
    throw new Error("Missing MCR.user2.matching.lead_id in config");
  }
  config["MCR.user2.matching.lead_id"] = jsonRepsponse["MCR.user2.matching.lead_id"];
};

const getFlattenedParamsFromURL = () => {
  let searchParams = decodeURIComponent(decodeURIComponent(window.location.search));
  const params = new Map<string, string>();

  if (searchParams.length == 0) {
    return params;
  }

  if (searchParams.startsWith("?")) {
    searchParams = searchParams.substring(1);
  }

  searchParams.split("&").forEach((param) => {
    const [key, value] = param.split("=");
    params.set(key, value);
  });
  return params;
};

const getFirstNameFromURL = () => {
  return getFlattenedParamsFromURL().get("firstName") || "";
};

const getLastNameFromURL = () => {
  return getFlattenedParamsFromURL().get("lastName") || "";
};

const getPidFromURL = () => {
  const pidName = getPidTypeFromConfig();
  return getFlattenedParamsFromURL().get(pidName) || "";
};

const getPidTypeFromConfig = () => {
  return config["MCR.user2.matching.lead_id"] || "";
};

const getSessionIdFromURL = () => {
  return new URL(window.location.toString()).searchParams.get("_xed_subselect_session");
};

const getAdminStatusFromJWT = () => {
  if(!model.jwt) {
    return false;
  }
  return JSON.parse(atob(model.jwt.split(".")[1]))["mcr:roles"].indexOf("admin") != -1
};


const search = ({term}: { term: string }) => {
  model.searching = true;
  performSearch(term).then(result => model.searchResults = result);
}

const performSearch = async (query: string): Promise<SearchResult> => {
  model.searching = true;
  model.error = false;
  model.searched = true;
  try {
    const response = await fetch(getWebApplicationBaseURL() + "rsc/search/person?query=" + encodeURIComponent(query));
    return await response.json();
  } catch (e) {
    model.error = true;
    throw e;
  } finally {
    model.searching = false;
  }
}

const personApplied = (person: { firstName?:string, lastName?:string, pid:string, displayName?:string }) => {
  if (person.firstName && person.lastName) {
    model.personModel.firstName = person.firstName;
    model.personModel.lastName = person.lastName;
  } else if (person.displayName){
    [model.personModel.firstName, model.personModel.lastName] = person.displayName.split(" ");
  }
  model.personModel.pid = person.pid;
}

const personResultApplied = (person: PersonResult) => {
  personApplied({firstName:person.firstName, lastName:person.lastName, pid:person.pid, displayName:person.displayName});
};

const personSubmitted = (person:  { firstName?:string, lastName?:string, pid:string, displayName?:string }) => {
  if (person !== null) {
    personApplied(person);
  }
  let url = getWebApplicationBaseURL() + "servlets/XEditor?_xed_submit_return=&_xed_session=" + model.sessionID;

  const xpFam = "mods:namePart[@type='family']";
  const xpGiven = "mods:namePart[@type='given']";
  const xpPid = "mods:nameIdentifier[@type='" + model.pidType + "']";


  const parms = [encodeURIComponent(xpFam) + "=" + encodeURIComponent(model.personModel.lastName),
    encodeURIComponent(xpGiven) + "=" + encodeURIComponent(model.personModel.firstName)]

  if (model.personModel.pid.length > 0 || model.personModel.pid != "") {
    parms.push(encodeURIComponent(xpPid) + "=" + encodeURIComponent(model.personModel.pid));
  }

  const urlParams = new URLSearchParams(window.location.href);
  url += "&" + parms.join("&") + (urlParams.has("xEditorHook") ? "#" + urlParams.get("xEditorHook") : "");

  if (!model.formSend) {
    window.location.assign(url);
    model.formSend = true;
  }
};



const personResultSubmitted = (person: PersonResult) => {
  personSubmitted({firstName:person.firstName, lastName:person.lastName, pid:person.pid, displayName:person.displayName});
};



const cancel = () => {
  const urlParams = new URLSearchParams(window.location.href);
  let url = getWebApplicationBaseURL() + "servlets/XEditor?_xed_submit_return_cancel=&_xed_session=" + model.sessionID;
  url += (urlParams.has("xEditorHook") ? "#" + urlParams.get("xEditorHook") : "");

  window.location.assign(url);
}

const idApplied = (pid: string) => {
  model.personModel.pid = pid;
}

</script>

<style>

</style>
