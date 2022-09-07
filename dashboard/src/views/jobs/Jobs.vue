<template>
  <div>
    <h2>Jobs</h2>
    <bs-tab :tabs="Object.keys(statuses).map(k => ({ key: k, text: statuses[k] }))" v-model="tab">
      <template v-slot:[`item`]="{ item }">
        {{ item.text }} <span class="badge rounded-pill text-bg-primary">{{ item.key === tab ? jobs.length : 0 }}</span>
      </template>
    </bs-tab>
    <div v-for="status in Object.keys(statuses)" :key="status" :style="{ display: status === tab ? 'block' : 'none' }">
      <bs-table :headers="[
        { key: 'bulk', text: '' },
        { key: 'id', text: 'ID' },
        { key: 'type', text: 'Type' },
        { key: 'created_at', text: 'Created At' }
    ]" :items="jobs">
        <template v-slot:[`header.bulk`]>
          <input type="checkbox" class="form-check" v-model="checkAll" @input="e => toggleCheckAll(e.target.checked)">
        </template>
        <template v-slot:[`item.bulk`]="{ item }">
          <input type="checkbox" class="form-check" v-model="checked[item.id]" @input="e => toggleChecked(item.id, e.target.checked)">
        </template>
        <template v-slot:[`item.id`]="{ item }">
          <router-link :to="{ name: 'jobs' }">{{ item.id }}</router-link>
        </template>
      </bs-table>
    </div>
  </div>
</template>

<script>
import BsTable from "@/components/BSTable";
import api from "@/api";
import BsTab from "@/components/BSTab";

export default {
  name: "Jobs",
  data() {
    return {
      statuses: {
        'SCHEDULED': 'Scheduled',
        'ENQUEUED': 'Enqueued',
        'PROCESSING': 'Processing',
        'SUCCESS': 'Succeeded',
        'FAILED': 'Failed'
      },
      tab: 'SCHEDULED',
      jobs: [],
      checkAll: false,
      checked: {}
    }
  },
  created() {
    this.fetchJobs()
  },
  watch: {
    tab() {
      this.fetchJobs()
    }
  },
  methods: {
    fetchJobs() {
      api.get('/jobs', { params: { status: this.tab } }).then(res => {
        this.checkAll = false
        this.checked = {}
        this.jobs = res.data.data
      })
    },
    toggleChecked(id, value) {
      this.checked[id] = value
      this.checkAll = this.jobs.reduce((c, v) => c && this.checked[v.id], true)
    },
    toggleCheckAll(value) {
      if(value) {
        this.checkAll = true
        this.jobs.forEach(j => this.checked[j.id] = true)
      } else {
        this.checkAll = false
        this.checked = {}
      }
    }
  },
  components: {BsTab, BsTable}
}
</script>

<style scoped>

</style>