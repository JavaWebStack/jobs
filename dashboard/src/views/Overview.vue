<template>
  <div>
    <h2>Overview</h2>
    <router-link to="workers">
      <b>Workers:</b>
      {{ workersCount }}
    </router-link>
  </div>
</template>

<script>
import api from "@/api";

export default {
  name: "Overview",
  data: () => ({
    workersCount: 0
  }),
  mounted() {
    api.get('/workers').then(res => {
      this.workersCount = res.data.data.length
    })
  }
}
</script>

<style scoped>

</style>