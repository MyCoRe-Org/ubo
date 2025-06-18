import { createRouter, createWebHistory } from 'vue-router'
import DedupListView from '../views/DedupListView.vue'
import {getWebApplicationBaseURL} from "@/api/Utils.ts";
import NoDuplicateListView from "@/views/NoDuplicateListView.vue";

export function getContext(): string {
  if (import.meta.env.DEV) {
    return import.meta.env.BASE_URL
  }
  const el = document.createElement('a');
  el.href = getWebApplicationBaseURL();
  return el.pathname + "dedup/";
}

const router = createRouter({
  history: createWebHistory(getContext()),
  routes: [
    {
      path: '/',
      name: 'listDuplicates',
      component: DedupListView,
    },
    {
      path: '/no-duplicates',
      name: 'noDuplicates',
      component: NoDuplicateListView
    }
  ],
})

export default router
