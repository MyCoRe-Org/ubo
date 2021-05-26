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

      <form class="ubo-vue-form" role="form" v-on:submit.prevent="search">
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

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {resolveiI18N} from "@/components/I18N";

@Component
export default class PersonSearchForm extends Vue {

  @Prop({default: ""}) baseurl!: string;

  i18n = {
    "person.search.instruction2": null,
    "person.search.help2": null,
    "lsf.searchFor": null,
    "person.search.invalid.search": null,
    "button.search": null
  };
  hint = false;

  private searchModel = {
    term: "",
    validated: true
  };

  mounted() {
    resolveiI18N(this.baseurl, this.i18n);
  }

  search() {
    if (this.searchModel.term.length == 0) {
      this.searchModel.validated = false;
      return;
    }

    this.$emit("search", {term: this.searchModel.term});
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
