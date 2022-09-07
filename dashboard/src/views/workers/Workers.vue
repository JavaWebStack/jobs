<template>
  <div>
    <h2>Workers</h2>
    <bs-table :headers="[
        { key: 'id', text: 'ID' },
        { key: 'queue', text: 'Queue' },
        { key: 'hostname', text: 'Hostname' },
        { key: 'last_heartbeat_at', text: 'Last Heartbeat' },
        { key: 'created_at', text: 'Created At' }
    ]" :items="workers"></bs-table>
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