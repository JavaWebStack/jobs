import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import 'bootswatch/dist/zephyr/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.scss'
import './assets/scss/app.scss'

import 'bootstrap/dist/js/bootstrap.min'

import BSTable from "@/components/BSTable";
import BSTab from "@/components/BSTab";
import BSModal from "@/components/BSModal";
import BSInput from "@/components/BSInput";
import BSSelect from "@/components/BSSelect";

createApp(App)
    .component('bs-table', BSTable)
    .component('bs-tab', BSTab)
    .component('bs-modal', BSModal)
    .component('bs-input', BSInput)
    .component('bs-select', BSSelect)
    .use(store)
    .use(router)
    .mount('#app')
