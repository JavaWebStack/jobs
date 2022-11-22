<template>
  <div>
    <bs-modal v-model="open" @cancel="resetForm()" @ok="createJob()" title="Create Job" size="lg" ok-text="Create">
      <bs-select v-model="queue" :options="queues">Queue</bs-select>
      <bs-select v-model="type" :options="types" class="mt-2">Type</bs-select>
      <bs-input type="datetime-local" v-model="scheduleAt" v-if="scheduled" class="mt-2">Schedule At</bs-input>
      <div class="form-group mt-2">
        <label class="form-label">Payload</label>
        <textarea class="form-control" v-model="payload" rows="7" />
      </div>
    </bs-modal>
  </div>
</template>

<script>
import api from "@/api";
import BsInput from "@/components/BSInput";

export default {
  name: "JobCreator",
    components: {BsInput},
    data() {
    return {
      open: false,
      types: [],
      queues: [],
      queue: '',
      type: '',
      scheduleAt: '',
      payload: '{}'
    }
  },
  props: {
    scheduled: {
      type: Boolean,
      default: false
    }
  },
  created() {
    this.fetchTypesAndQueues();
  },
    watch: {
      scheduleAt(val) {
          console.log(val)
      }
    },
  methods: {
    fetchTypesAndQueues() {
      api.get('/status/job-types').then(res => this.types = res.data.data)
      api.get('/workers').then(res => {
        this.queues = res.data.data.reduce((c, v) => {
          if(!c.includes(v.queue))
            c.push(v.queue)
          return c
        }, [])
      })
    },
    show() {
      this.open = true
    },
    resetForm() {
      this.queue = ''
      this.type = ''
      this.scheduleAt = ''
      this.payload = ''
    },
    formatDate(date) {
      return date.replace('T', ' ') + ':00'
    },
    createJob() {
      api.post('/jobs', {
        queue: this.queue,
        type: this.type,
        schedule_at: this.scheduled ? this.formatDate(this.scheduleAt) : undefined,
        payload: JSON.parse(this.payload)
      }).then(res => this.$emit('created', res.data.data))
    }
  }
}
</script>

<style scoped>

</style>