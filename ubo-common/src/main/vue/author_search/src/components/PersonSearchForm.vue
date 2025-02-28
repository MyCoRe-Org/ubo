<template>

  <article class="card mb-2">
    <div class="card-body">
      <h2>{{ i18n["person.search"] }}</h2>
      <div>
        <p>
          {{ i18n["person.search.instruction2"] }}
          <i v-on:click="hint=!hint" role="button" class="fas fa-question ml-1 text-secondary"></i>
        </p>
        <div v-show="hint">
          <p class="border-secondary border-top border-bottom pt-2 pb-2">
            {{ i18n["person.search.help2"] }}
          </p>
        </div>
      </div>

      <form class="ubo-vue-form" role="form"
            v-on:submit.prevent="emit('search', {term:searchModel.term})">
        <input name="_xed_subselect_session" type="hidden">
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="lastName">
            {{ i18n["lsf.searchFor"] }}
          </label>
          <input class="mycore-form-input"
                 v-bind:class="{'is-invalid': !searchModel.validated }"
                 v-model="searchModel.term" size="40"
                 v-on:keypress="searchModel.validated=true"
                 type="text" name="term"
                 id="lastName">
          <div class="invalid-feedback">
            {{ i18n["person.search.invalid.search"] }}
          </div>
        </div>
        <div class="cancel-submit form-group form-inline">
          <label class="mycore-form-label"></label>
          <input :value="i18n['button.search']" name="search" class="btn btn-primary" type="submit">
        </div>
      </form>
    </div>
  </article>
</template>

<script lang="ts" setup>

import {onMounted, reactive, ref, watch} from "vue";
import {resolveiI18N} from "@/components/I18N.ts";

const props = defineProps<{
  baseurl: string;
  firstname: string;
  lastname: string;
}>();

const emit = defineEmits<{
  search: [term: { term: string }]
}>();

const i18n = reactive({
  "person.search": "",
  "person.search.instruction2": "",
  "person.search.help2": "",
  "lsf.searchFor": "",
  "person.search.invalid.search": "",
  "button.search": ""
});

const hint = ref(false);

const searchModel = reactive({
  term: "",
  validated: true
});

watch(() => props.firstname, () => {
  applyNamesToTerm();
});

watch(() => props.lastname, () => {
  applyNamesToTerm();
});

const applyNamesToTerm = () => {
  searchModel.term = [props.firstname, props.lastname]
    .filter(s => s !== undefined && s.trim().length > 0).join(" ");
};

onMounted(() => {
  resolveiI18N(props.baseurl, i18n);
  applyNamesToTerm();

  if (searchModel.term.trim() !== "") {
    emit("search", {term: searchModel.term});
  }
});

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
