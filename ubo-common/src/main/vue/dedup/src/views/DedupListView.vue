<template>
  <main>

    <div class="form-row mb-1">
      <div class="col-2">
        <label>{{ i18n["dedup.nav.DedupListView.filter"] }}</label>
      </div>
      <div class="col-5">
        <select id="filterType" v-model="model.filterType" class="form-control">
          <option value="all">{{ i18n["dedup.nav.DedupListView.filter.all"] }}</option>
          <option value="ta">{{ i18n["dedup.nav.DedupListView.filter.ta"] }}</option>
          <option value="identifier">{{ i18n["dedup.nav.DedupListView.filter.identifier"] }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="model.loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="sr-only">Loading...</span>
      </div>
    </div>
    <div v-else-if="model.error!=null">
      <div class="alert alert-danger" role="alert">
        <template v-if="model.error==403">
          {{ i18n["navigation.notAllowedToSeeThisPage"] }}
        </template>
        <template v-else>
          {{ model.error }}
        </template>
      </div>
    </div>
    <div v-else class="row">
      <div class="col-12">
        <table class="table table-hover">
          <thead>
          <tr>
            <th class="pointer nobreak" v-on:click="toggleTypeSort">
              {{ i18n["dedup.nav.DedupListView.table.dedupType"] }}
              <SortArrow :direction="model.typeSort"/>
            </th>
            <th class="pointer nobreak" v-on:click="toggleIdSort">
              {{ i18n["dedup.nav.DedupListView.table.mcrId1"] }}
              <SortArrow :direction="model.idSort"/>
            </th>
            <th class="pointer nobreak" v-on:click="toggleIdSort">
              {{ i18n["dedup.nav.DedupListView.table.mcrId2"] }}
              <SortArrow :direction="model.idSort"/>
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="entry in model.dedupList"
              :key="entry.deduplicationKey+'-'+entry.deduplicationType+'-'+entry.mcrId1+'-'+entry.mcrId2">
            <td>
              <span
                v-if="entry.deduplicationType == 'ta'">{{ i18n["dedup.nav.DedupListView.filter.ta"] }}</span>
              <span
                v-else-if="entry.deduplicationType == 'identifier'">{{ i18n["dedup.nav.DedupListView.filter.identifier"] }}</span>
            </td>
            <td>
              <PublicationDisplay :mcr-id="entry.mcrId1"/>
            </td>
            <td>
              <PublicationDisplay :mcr-id="entry.mcrId2"/>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

  </main>
</template>


<script lang="ts" setup>

import {getMCRApplicationBaseURL} from "@/api/Utils.ts";
import {onMounted, reactive, watch} from "vue";
import type {DedupList} from "@/api/Model.ts";
import SortArrow from "@/components/SortArrow.vue";
import {resolveiI18N} from "@/api/I18N.ts";
import PublicationDisplay from "@/components/PublicationDisplay.vue";

const model = reactive({
  loading: true,
  dedupList: [] as DedupList,
  idSort: "none" as "asc" | "desc" | "none",
  typeSort: "none" as "asc" | "desc" | "none",
  filterType: "all" as "ta" | "identifier" | "all",
  filterDocType: "all" as string,
  error: null as number | null
});


const i18n = reactive({
  "dedup.nav.DedupListView.table.dedupType": "dedupType",
  "dedup.nav.DedupListView.table.mcrId1": "mcrId1",
  "dedup.nav.DedupListView.table.mcrId2": "mcrId2",
  "dedup.nav.DedupListView.filter": "filter",
  "dedup.nav.DedupListView.filter.all": "all",
  "dedup.nav.DedupListView.filter.ta": "ta",
  "dedup.nav.DedupListView.filter.identifier": "identifier",
  "navigation.notAllowedToSeeThisPage": "notAllowedToSeeThisPage"
});


const toggleTypeSort = () => {
  if (model.typeSort === "asc") {
    model.typeSort = "desc";
  } else if (model.typeSort === "desc") {
    model.typeSort = "none";
  } else {
    model.typeSort = "asc";
  }
  resolveDedupList();
}

const toggleIdSort = () => {
  if (model.idSort === "asc") {
    model.idSort = "desc";
  } else if (model.idSort === "desc") {
    model.idSort = "none";
  } else {
    model.idSort = "asc";
  }
  resolveDedupList();
}

onMounted(() => {
  resolveDedupList();
  resolveiI18N(getMCRApplicationBaseURL(), i18n);
})

const resolveDedupList = async () => {
  const params = [
    (model.idSort === "none" ? null : "idSort=" + model.idSort),
    (model.typeSort === "none" ? null : "typeSort=" + model.typeSort),
    (model.filterType === "all" ? null : "type=" + model.filterType),
    (model.filterDocType === "all" ? null : "docType=" + model.filterDocType)
  ];

  const realParams = params.filter(p => p !== null).join("&");
  const request = await fetch(getMCRApplicationBaseURL() + "rsc/dedup/list/duplicates" + (realParams.length > 0 ? "?" + realParams : ""));
  if (request.status != 200) {
    model.error = request.status;
    model.loading = false;
  }

  const json = await request.json();
  model.dedupList = json;
  model.loading = false;
}

watch(() => model.filterType, resolveDedupList);
watch(() => model.filterDocType, resolveDedupList);

</script>

<style scoped>

.pointer {
  cursor: pointer;
}

.nobreak {
  white-space: nowrap;
}

</style>
