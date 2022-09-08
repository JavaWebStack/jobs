<template>
  <div>
    <div class="card border-secondary mb-3">
      <div class="card-header"><h2>Job - {{ job.id }}</h2></div>
      <div class="card-body">
        <h4 class="card-title">{{ job.type }}</h4>
        <p class="card-text">
          <pre v-html="JSON.stringify(payload, null, 2)" />
        </p>
      </div>
    </div>
    <div class="card border-secondary mb-3">
      <div class="card-header">History</div>
      <div class="card-body">
        <job-event-box v-for="event in events" :key="event.id" :event="event" />
      </div>
    </div>
  </div>
</template>

<script>
import api from "@/api";
import JobEventBox from "@/components/JobEventBox";

export default {
  name: "Job",
  components: {JobEventBox},
  data() {
    return {
      job: {
        id: this.$route.params.id,
        type: ''
      },
      payload: {},
      events: [],
      refreshTimer: undefined
    }
  },
  created() {
    this.fetchJob(true);
    this.fetchEvents();
    this.refreshTimer = setInterval(this.fetchJob, 3000)
  },
  beforeUnmount() {
    clearInterval(this.refreshTimer)
  },
  methods: {
    fetchJob(payload = false) {
      api.get('/jobs/' + this.job.id, { params: { payload: true } }).then(res => {
        if(this.job.status !== res.data.data.status) {
          this.fetchEvents()
        }
        if(payload) {
          this.payload = res.data.data.payload
        }
        this.job = res.data.data
      }, () => {
        this.$router.push({
          name: 'jobs'
        })
      })
    },
    fetchEvents() {
      api.get('/jobs/' + this.job.id + '/events').then(res => this.events = res.data.data)
    }
  }
}
</script>

<style scoped>

</style>