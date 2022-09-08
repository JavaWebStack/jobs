<template>
  <div>
    <h2>Workers</h2>
    <bs-table :headers="[
        { key: 'id', text: 'ID' },
        { key: 'queue', text: 'Queue' },
        { key: 'hostname', text: 'Hostname' },
        { key: 'threads', text: 'Threads' },
        { key: 'last_heartbeat_at', text: 'Last Heartbeat' },
        { key: 'online', text: 'Online' },
        { key: 'created_at', text: 'Created At' }
    ]" :items="workers">
      <template v-slot:[`item.online`]="{ item }">
        <i class="bi bi-play-circle" v-if="item.online"></i>
        <i class="bi bi-stop-circle" v-else></i>
      </template>
    </bs-table>
  </div>
</template>

<script>
import api from "@/api";

export default {
  name: "Workers",
  data() {
    return {
      workers: []
    }
  },
  created() {
    this.fetchWorkers()
  },
  methods: {
    fetchWorkers() {
      api.get('/workers').then(res => {
        this.workers = res.data.data
      })
    }
  }
}
</script>

<style scoped>

</style>