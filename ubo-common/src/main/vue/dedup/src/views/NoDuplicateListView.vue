<template>
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
  <div class="row" v-else>
    <div class="col-12">
      <table class="table table-hover">
        <thead>
        <tr>
          <!-- with sort -->
          <td class="pointer nobreak" v-on:click="toggleSort('mcrId1')">
            {{ i18n["dedup.nav.NoDuplicateListView.table.mcrId1"] }}
            <SortArrow v-if="model.sortField=='mcrId1'" :direction="model.sortDirection"/>
          </td>
          <td class="pointer nobreak" v-on:click="toggleSort('mcrId2')">
            {{ i18n["dedup.nav.NoDuplicateListView.table.mcrId2"] }}
            <SortArrow v-if="model.sortField=='mcrId2'" :direction="model.sortDirection"/>
          </td>
          <td class="pointer nobreak" v-on:click="toggleSort('creator')">
            {{ i18n["dedup.nav.NoDuplicateListView.table.creator"] }}
            <SortArrow v-if="model.sortField=='creator'" :direction="model.sortDirection"/>
          </td>
          <td class="pointer nobreak" v-on:click="toggleSort('date')">
            {{ i18n["dedup.nav.NoDuplicateListView.table.date"] }}
            <SortArrow v-if="model.sortField=='date'" :direction="model.sortDirection"/>
          </td>
          <td>

          </td>
        </tr>
        </thead>
        <tbody>
        <tr v-for="entry in model.noDupList" :key="entry.id">
          <td>
            <publication-display :mcr-id="entry.mcrId1"/>
          </td>
          <td>
            <publication-display :mcr-id="entry.mcrId2"/>
          </td>
          <td>
            {{ entry.creator }}
          </td>
          <td>
            {{ entry.date }}
          </td>
          <td>
            <button class="btn btn-danger"
                    v-on:click="showDeleteEntryModel(entry.id, entry.mcrId1, entry.mcrId2)">
              {{ i18n["dedup.nav.NoDuplicateListView.delete"] }}
            </button>
          </td>
        </tr>
        </tbody>

      </table>

      <modal ref="deleteModalRef" v-on:hide="deleteModalResult('hide')">
        <template #titel>
          {{ i18n["dedup.nav.NoDuplicateListView.delete"] }}
        </template>
        <template #default>
          <p>{{ i18n["dedup.nav.NoDuplicateListView.delete.confirm"] }}</p>
          <p class="mt-1">
            <publication-display v-if="model.modalMcrId1!=''" :mcr-id="model.modalMcrId1"/>
          </p>
          <p class="mt-1">
            <publication-display v-if="model.modalMcrId2!=''" :mcr-id="model.modalMcrId2"/>
          </p>
        </template>
        <template #footer>
          <button class="btn btn-danger" v-on:click="deleteModalResult('yes')">
            {{ i18n["button.deleteYes"] }}
          </button>
          <button class="btn btn-secondary" v-on:click="deleteModalResult('no')">
            {{ i18n["button.cancelNo"] }}
          </button>
        </template>
      </modal>
    </div>
  </div>

</template>

<script setup lang="ts">
import {onMounted, reactive, useTemplateRef, watch} from "vue";
import type {NoDupList} from "@/api/Model.ts";
import {getMCRApplicationBaseURL} from "@/api/Utils.ts";
import PublicationDisplay from "@/components/PublicationDisplay.vue";
import SortArrow from "@/components/SortArrow.vue";
import {resolveiI18N} from "@/api/I18N.ts";
import Modal from "@/components/Modal.vue";

const deleteModal = useTemplateRef("deleteModalRef");

const model = reactive({
  loading: true,
  noDupList: [] as NoDupList,
  sortDirection: "desc" as "asc" | "desc" | "none",
  sortField: "date" as SortField,
  error: null as number | null,
  modalId: -1,
  modalMcrId1: "",
  modalMcrId2: "",
});

type SortField = "mcrId1" | "mcrId2" | "date" | "creator" | null;

const i18n = reactive({
  "dedup.nav.NoDuplicateListView.table.creator": "creator",
  "dedup.nav.NoDuplicateListView.table.mcrId1": "mcrId1",
  "dedup.nav.NoDuplicateListView.table.mcrId2": "mcrId2",
  "dedup.nav.NoDuplicateListView.table.date": "date",
  "dedup.nav.NoDuplicateListView.delete": "delete",
  "dedup.nav.NoDuplicateListView.delete.confirm": "Do you really want to delete this entry?",
  "button.deleteYes": "yes",
  "button.cancelNo": "no",
  "navigation.notAllowedToSeeThisPage": "notAllowedToSeeThisPage"
});

onMounted(() => {
  resolveNoDupList();
  resolveiI18N(getMCRApplicationBaseURL(), i18n);
});

const toggleSort = (field: SortField) => {
  if (field === model.sortField) {
    if (model.sortDirection === "asc") {
      model.sortDirection = "desc";
    } else if (model.sortDirection === "desc") {
      model.sortDirection = "none";
      model.sortField = null;
    } else {
      model.sortDirection = "asc";
    }
  } else {
    model.sortField = field;
    model.sortDirection = "asc";
  }
}


const resolveNoDupList = async () => {
  const params = [
    (model.sortField === null) ? null : "sortBy=" + model.sortField,
    (model.sortDirection === "none" ? null : "sortOrder=" + model.sortDirection),
  ];

  const realParams = params.filter(p => p !== null).join("&");
  const request = await fetch(getMCRApplicationBaseURL() + "rsc/dedup/list/no-duplicates" + (realParams.length > 0 ? "?" + realParams : ""));
  if (request.status != 200) {
    model.error = request.status;
    model.loading = false;
  }

  const json = await request.json();
  model.noDupList = json;
  model.loading = false;
}

const showDeleteEntryModel = async (id: number, mcrId1: string, mcrId2: string) => {
  model.modalMcrId1 = mcrId1;
  model.modalMcrId2 = mcrId2;
  model.modalId = id;

  deleteModal.value?.show();
}

const deleteModalResult = async (result: "yes" | "no" | "hide") => {
  deleteModal.value?.hide();

  if (result !== 'yes') {
    return;
  }

  const request = await fetch(getMCRApplicationBaseURL() + "rsc/dedup/delete/no-duplicates/" + model.modalId, {
    method: "DELETE"
  });

  model.modalMcrId1 = "";
  model.modalMcrId2 = "";
  model.modalId = -1;

  if (request.status != 200) {
    model.error = request.status;
  } else {
    resolveNoDupList();
  }


}

watch(() => model.sortField, resolveNoDupList);
watch(() => model.sortDirection, resolveNoDupList);

</script>

<style scoped>

</style>
