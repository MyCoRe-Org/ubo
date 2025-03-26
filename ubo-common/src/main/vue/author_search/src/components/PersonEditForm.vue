<template>
  <article class="card mb-2">
    <div class="card-body">
      <h2>{{ i18n["person.search.information"] }}</h2>
      <div>
        <p>
          {{ i18n["person.search.instruction1"] }}
          <i v-on:click="hint=!hint" role="button" class="fas fa-question ml-1 text-secondary"></i>
        </p>
        <div v-show="hint">
          <p class="border-secondary border-top border-bottom pt-2 pb-2">
            {{ i18n["person.search.help1"] }}</p>
        </div>
      </div>
      <form class="ubo-vue-form" role="form" v-on:submit.prevent="apply()">
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="firstName">
            {{ i18n["lsf.nameFirst"] }}
          </label>
          <input class="mycore-form-input" type="text"
                 :class="{ 'is-invalid': firstNameInvalid  }"
                 v-model="internModel.person.firstName"
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
                 :class="{ 'is-invalid': lastNameInvalid  }"
                 v-model="internModel.person.lastName" id="lastName">
          <div class="invalid-feedback">
            {{ i18n["person.search.invalid.lastName"] }}
          </div>
        </div>
        <div class="form-group form-inline">
          <label class="mycore-form-label" for="pid">
            {{ i18n["editor.identity.picker.lead_id"] }}:
          </label>
          <input class="mycore-form-input" size="6" type="text" :readonly="!props.isAdmin"
                 v-model="internModel.person.pid"
                 id="pid" :placeholder="i18n['editor.identity.picker.lead_id']">
        </div>
        <div class="cancel-submit form-group form-inline">
          <label class="mycore-form-label"></label>
          <input :title="i18n['index.person.datatoeditor.try.search']"
                 v-if="!searched&&person.pid.length===0"
                 disabled
                 :value="i18n['lsf.selectPerson']"
                 class="btn btn-secondary mr-2" type="submit">
          <input :title="i18n['index.person.datatoeditor']"
                 v-if="searched||person.pid.trim().length>0"
                 :value="i18n['lsf.selectPerson']"
                 class="btn btn-secondary mr-2"
                 type="submit">
          <input :value="i18n['button.cancel']" class="btn btn-primary" type="button"
                 v-on:click="$emit('cancel')">
        </div>
      </form>
    </div>
  </article>
</template>

<script lang="ts" setup>
import {resolveiI18N} from "@/components/I18N";
import {onMounted, reactive, ref, watch} from "vue";

const props = defineProps<{
  person: { firstName: string, lastName: string, pid: string },
  baseurl: string,
  isAdmin: boolean,
  searched: boolean
}>();

const emit = defineEmits<{
  submit: [person: { firstName: string, lastName: string, pid: string }],
  cancel: []
}>();

watch(() => props.person, (newVal) => {
  internModel.person = newVal;
}, {deep: true});

const internModel = reactive({
  person: {firstName: "", lastName: "", pid: ""}
});

const i18n = reactive({
  "person.search.information": "",
  "person.search.invalid.firstName": "",
  "person.search.invalid.lastName": "",
  "person.search.instruction1": "",
  "person.search.help1": "",
  "index.person.datatoeditor": "",
  "lsf.name": "",
  "lsf.nameFirst": "",
  "editor.identity.picker.lead_id": "",
  "button.cancel": "",
  "lsf.selectPerson": "",
  "index.person.datatoeditor.try.search": ""
});

const firstNameInvalid = ref(false);
const lastNameInvalid = ref(false);
const hint = ref(false);

onMounted(() => {
  resolveiI18N(props.baseurl, i18n);
});


const apply = () => {
  const firstName = internModel.person.firstName.trim();
  const lastName = internModel.person.lastName.trim();

  internModel.person.firstName = firstName;
  internModel.person.lastName = lastName;
  internModel.person.pid = internModel.person.pid.trim();

  firstNameInvalid.value = firstName.length == 0;
  lastNameInvalid.value = lastName.length == 0;

  if (firstName.length > 0 && lastName.length > 0) {
    emit('submit', internModel.person);
  }
}

internModel.person = props.person;

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
