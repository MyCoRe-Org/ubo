<template>
  <div class="entry" ref="entry">
    <template v-if="modsTitle">
      {{ modsTitle }}
    </template>
    <template v-else-if="model.loadingData">
      <div class="spinner-border" role="status">
        <span class="sr-only">Loading...</span>
      </div>
    </template>
    ({{ getShortID(props.mcrId) }})
    <a
      :href="getMCRApplicationBaseURL() + 'servlets/DozBibEntryServlet?mode=show&id=' + props.mcrId"
      :aria-label="i18n['dedup.nav.DedupListView.go.metadata']">
      <span class="fas fa-file-alt d-inline-block ml-2"> </span>
    </a>
    <a
      :href="getMCRApplicationBaseURL() + 'servlets/DozBibEntryServlet?mode=show&XSL.Style=structure&id=' + props.mcrId"
      :aria-label="i18n['dedup.nav.DedupListView.go.stuctureEditor']">
      <span class="fas fa-project-diagram pointer d-inline-block d-inline-block ml-2"> </span>
    </a>
  </div>

</template>

<script setup lang="ts">

import {getMCRApplicationBaseURL, getMODSTitle, getShortID} from "@/api/Utils.ts";
import type {MODSMyCoReObject} from "@/api/ModsModel.ts";
import {computed, onMounted, reactive, useTemplateRef, watch} from "vue";
import {resolveiI18N} from "@/api/I18N.ts";
import {useElementVisibility} from "@vueuse/core";

const i18n = reactive({
  "dedup.nav.DedupListView.go.metadata": "",
  "dedup.nav.DedupListView.go.stuctureEditor": "",
});

onMounted(() => {
  resolveiI18N(getMCRApplicationBaseURL(), i18n);
});

const props = defineProps<{
  mcrId: string;
}>();

const model = reactive({
  mods: null as MODSMyCoReObject | null,
  loadingData: false
})

const entry = useTemplateRef("entry")
const visible = useElementVisibility(entry);

watch(() => visible.value, (newVal) => {
  if (newVal) {
    loadData();
  }
})

const loadData = async () => {
  if (!model.loadingData) {
    model.loadingData = true;
    const url = getMCRApplicationBaseURL() + "api/v2/objects/" + props.mcrId;
    const settings = {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }

    const result = await fetch(url, settings);
    if (!result.ok) {
      return;
    }
    model.mods = await result.json();
  }
}

const modsTitle = computed(() => {
  if (model.mods === null) {
    return null;
  } else {
    return getMODSTitle(model.mods);
  }
});

if (visible.value) {
  loadData();
}

</script>

<style scoped>
.entry {
  min-height: 21px;
}
</style>
