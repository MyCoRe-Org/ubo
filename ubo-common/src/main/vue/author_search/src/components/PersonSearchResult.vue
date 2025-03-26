<!--
  - This file is part of ***  M y C o R e  ***
  - See http://www.mycore.de/ for details.
  -
  - MyCoRe is free software: you can redistribute it and/or modify
  - it under the terms of the GNU General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - MyCoRe is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU General Public License for more details.
  -
  - You should have received a copy of the GNU General Public License
  - along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
  -->

<template>
  <article v-if="searching||error||searchresults!=null" class="card mb-2">
    <div v-if="searching" class="d-flex justify-content-center p-3">
      <div class="spinner-border" role="status">
        <span class="sr-only">Loading...</span>
      </div>
    </div>
    <div v-if="error" class="card-body text-warning">
      <p>{{ i18n["error.occurred"] }}</p>
    </div>
    <div v-else-if="searchresults!=null && searchresults.count===0" class="card-body">
      <p>
        <strong>{{ i18n["index.person.found.0"] }}</strong>
      </p>
    </div>
    <div v-else-if="searchresults!=null && searchresults.count>0" class="card-body">
      <p>
        <strong>{{ i18n["lsf.found"] }}</strong>
      </p>
      <div>
        <table class="table table-sm">
          <tbody>
          <tr v-for="person in searchresults.personList" :key="person.pid">
            <td class="align-top" >
              <i v-bind:class="'fas ubo-picker-service-' + person.service" v-bind:title="person.service"/>

              {{ person.displayName }}

              <br />
              <ul v-if="(person.affiliation && person.affiliation.length>0) || (person.information && person.information.length>0)">
                <li v-for="aff in person.affiliation" :key="aff">
                  {{ aff }}
                </li>
                <template v-for="info in person.information">
                  <li v-if="info!=null && info.length > 0" :key="info">
                    {{ info }}
                  </li>
                </template>
              </ul>
            </td>
            <td class="align-top">
              <button :title="i18n['index.person.datatoeditor']" class="btn btn-secondary" v-on:click.prevent="submit(person)">
                {{ i18n['lsf.selectPerson']}}
              </button>
            </td>
            <td class="align-top">
              <a :title="i18n['index.person.datatoform']" href="#" class="roundedButton text-secondary"
                 v-on:click.prevent="apply(person)">
                <i class="far fa-arrow-alt-circle-right fa-lg"></i>
              </a>
              <a :title="i18n['index.person.idtoform']" href="#" class="roundedButton text-secondary d-block"
                 v-on:click.prevent="applyId(person)">
                <i class="far fa-id-card fa-lg"></i>
              </a>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </article>
</template>

<script lang="ts" setup>
import type {PersonResult, SearchResult} from "@/components/SearchResult";
import {resolveiI18N} from "@/components/I18N";
import {onMounted, reactive} from "vue";

const props = defineProps<{
  searchresults: SearchResult | null;
  searching: boolean;
  error: boolean;
  baseurl: string;
}>();

const emit = defineEmits<{
  person_submitted: [person: PersonResult],
  person_applied: [person: PersonResult],
  id_applied: [pid: string]
}>()

const i18n = reactive({
  "error.occurred": "",
  "index.person.found.0": "",
  "index.person.datatoform": "",
  "index.person.datatoeditor": "",
  "index.person.idtoform": "",
  "lsf.found": "",
  "lsf.selectPerson": "",
  "lsf.details": "",
  "ubo.search.name": ""
});

onMounted(() => {
  resolveiI18N(props.baseurl, i18n);
})

const submit = (person: PersonResult) => {
  emit('person_submitted', person);
}

const apply = (person: PersonResult) => {
  emit('person_applied', person);
}

const applyId = (person: PersonResult) => {
  emit('id_applied', person.pid);
}

</script>

<style scoped>

</style>
