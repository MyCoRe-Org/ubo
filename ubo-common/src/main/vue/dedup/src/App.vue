<template>

    <div class="card mb1">
      <div class="card-header">
        <ul class="nav nav-tabs card-header-tabs">
          <li class="nav-item">
            <router-link to="/" class="nav-link" active-class="active">{{ i18n["dedup.nav.doublettes"] }}</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/no-duplicates" class="nav-link" active-class="active">{{ i18n["dedup.nav.noDoublettes"]}}</router-link>
          </li>
        </ul>
      </div>
      <div class="card-body">
        <RouterView/>
      </div>
    </div>
    <link v-if="isDev()" rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
          crossorigin="anonymous">
  <link v-if="isDev()" rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.13.0/css/all.min.css"
          >

</template>

<script setup lang="ts">
import {RouterView} from 'vue-router'
import {getMCRApplicationBaseURL, isDev} from "@/api/Utils.ts";
import {onMounted, reactive} from "vue";
import {resolveiI18N} from "@/api/I18N.ts";

const i18n = reactive({
  "dedup.nav.doublettes": "doublettes",
  "dedup.nav.noDoublettes": "noDoublettes"
});

onMounted(()=> {
  resolveiI18N(getMCRApplicationBaseURL(), i18n);
});
</script>


<style scoped>

</style>
