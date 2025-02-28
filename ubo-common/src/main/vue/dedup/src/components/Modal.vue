<template>
  <div class="modal" tabindex="-1" ref="modal">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <slot name="titel"></slot>
          </h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <slot/>
        </div>
        <div class="modal-footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">

import {onMounted, useTemplateRef} from "vue";

const show = () => {
  if (modal.value) {
    $(modal.value).modal("show");
  }
};

const hide = () => {
  if (modal.value) {
    $(modal.value).modal("hide");
  }
};

defineExpose({
  show,
  hide
});

const modal = useTemplateRef("modal");

declare const $: {
  (selector: HTMLElement): {
    modal: (modalOpts?: string | {
      keyboard: boolean;
    }) => void,
    on: (event: string, callback: () => void) => void
  };
};

const emit = defineEmits<{
  show: []
  hide: []
}>();


onMounted(() => {
  if (modal.value) {
    $(modal.value).modal("hide");
    $(modal.value).on("hidden.bs.modal", () => {
      emit('hide');
    });
    $(modal.value).on("shown.bs.modal", () => {
      emit('show');
    });
  }
});

</script>


<style scoped>

</style>
