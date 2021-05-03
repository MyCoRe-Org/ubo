import Vue from 'vue'
import App from './App.vue'

Vue.config.productionTip = false

const vue = new Vue({
  render: h => h(App, {props:{
      baseurl:"http://localhost:8080/",
      bootstrap:"http://localhost:8080/rsc/sass/scss/bootstrap-ubo.css",
      leadid:"local"
    }}),
});
vue.$mount('#app')

