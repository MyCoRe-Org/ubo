<template>
  <article class="card mb-2">
    <div class="card-body">
      <h2>Personangaben</h2>
      <div>
        <p>
          {{ i18n["person.search.instruction1"] }}
          <i v-on:click="hint=!hint" role="button" class="fas fa-question-circle ml-1 text-secondary"></i>
        </p>
        <div v-show="hint">
          <p class="border-secondary border-top border-bottom pt-2 pb-2">{{ i18n["person.search.help1"] }}</p>
        </div>
      </div>
      <form role="form" v-on:submit.prevent="apply()">
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="firstName">
            {{ i18n["lsf.nameFirst"] }}
          </label>
          <input class="mycore-form-input" type="text"
                 :class="{ 'is-invalid': this.firstNameInvalid  }"
                 v-model="person.firstName"
                 id="firstName">
          <div class="invalid-feedback">
            {{ i18n["person.search.invalid.firstName"] }}
          </div>
        </div>
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="lastName">
            {{ i18n["lsf.name"] }}
          </label>
          <input class="mycore-form-input" type="text"
                 :class="{ 'is-invalid': this.lastNameInvalid  }"
                 v-model="person.lastName" id="lastName">
          <div class="invalid-feedback">
            {{ i18n["person.search.invalid.lastName"] }}
          </div>
        </div>
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="pid">
            {{ i18n["search.dozbib.pid"] }}
          </label>
          <input class="form-control col-sm-2" size="6" type="text" v-model="person.pid" id="pid">
        </div>
        <div class="cancel-submit form-group form-inline">
          <label class="mycore-form-label"></label>
          <input :value="i18n['lsf.selectPerson']"  class="btn btn-secondary mr-2" type="submit">
          <input :value="i18n['button.cancel']"  class="btn btn-primary" type="button"
                 v-on:click="$emit('cancel')">
        </div>
      </form>
    </div>
  </article>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {resolveiI18N} from "@/components/I18N";

@Component
export default class PersonEditForm extends Vue {

  @Prop() person!: { firstName: string, lastName: string, pid: string };
  @Prop({default: ""}) baseurl!: string;

  i18n = {
    "person.search.invalid.firstName": null,
    "person.search.invalid.lastName": null,
    "person.search.instruction1": null,
    "person.search.help1": null,
    "lsf.name": null,
    "lsf.nameFirst": null,
    "search.dozbib.pid": null,
    "button.cancel": null,
    "lsf.selectPerson": null
  };

  firstNameInvalid = false;
  lastNameInvalid = false;
  hint = false;

  mounted(){
    resolveiI18N(this.baseurl, this.i18n);
  }

  public apply() {
    let firstName = this.person.firstName.trim();
    let lastName = this.person.lastName.trim();

    this.person.firstName = firstName;
    this.person.lastName = lastName;
    this.person.pid = this.person.pid.trim();

    this.firstNameInvalid = firstName.length == 0;
    this.lastNameInvalid = lastName.length == 0;

    if (firstName.length > 0 && lastName.length > 0) {
      this.$emit("submit", this.person);
    }
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
