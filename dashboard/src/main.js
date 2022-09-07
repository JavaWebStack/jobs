import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import 'bootswatch/dist/zephyr/bootstrap.min.css'
import './assets/scss/app.scss'

import 'bootstrap/dist/js/bootstrap.min'

import BSTable from "@/components/BSTable";
import BSTab from "@/components/BSTab";

createApp(App)
    .component('bs-table', BSTable)
    .component('bs-tab', BSTab)
    .use(store)
    .use(router)
    .mount('#app')
