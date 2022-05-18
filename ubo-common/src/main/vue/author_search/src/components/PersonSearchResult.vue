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
            <td class="align-top">
              {{ person.displayName }}
              <br />
              <ul v-if="(person.affiliation && person.affiliation.length>0) || (person.information && person.information.length>0)">
                <li v-for="aff in person.affiliation" :key="aff">
                  {{ aff }}
                </li>
                <li v-for="info in person.information" :key="info">
                  {{ info }}
                </li>
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

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {PersonResult, SearchResult} from "@/components/SearchResult";
import {resolveiI18N} from "@/components/I18N";

@Component
export default class PersonSearchResult extends Vue {
  @Prop({default: null}) searchresults!: SearchResult | null;
  @Prop({default: false}) searching!: boolean;
  @Prop({default: false}) error!: boolean;
  @Prop({default: ""}) baseurl!: string;

  i18n = {
    "error.occurred": null,
    "index.person.found.0": null,
    "index.person.datatoform": null,
    "index.person.datatoeditor": null,
    "index.person.idtoform": null,
    "lsf.found": null,
    "lsf.selectPerson": null,
    "lsf.details": null,
    "ubo.search.name": null
  }

  mounted() {
    resolveiI18N(this.baseurl, this.i18n);
  }

  submit(person: PersonResult) {
    this.$emit("person_submitted", person);
  }

  apply(person: PersonResult) {
    this.$emit("person_applied", person);
  }

  applyId(person: PersonResult) {
    this.$emit("id_applied", person.pid);
  }
}
</script>

<style scoped>

</style>