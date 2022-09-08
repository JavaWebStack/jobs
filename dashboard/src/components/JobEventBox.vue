<template>
  <div class="event-box" :class="{ active, [`event-type-${ event.type.toLowerCase() }`]: true }">
    <div class="event-box-header" @click="toggleActive()">
      <div class="summary">
        {{ eventTitles[event.type] }} <span class="float-end">{{ event.created_at }}</span>
      </div>
    </div>
    <div class="collapse event-box-content" :class="{ show: active }">
      <div v-if="loading" class="text-center">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
      <table v-else-if="logs.length > 0">
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td style="padding-right: 20px; vertical-align: top;"><pre>{{ log.created_at }}</pre></td>
            <td>
              <code v-html="log.message"></code>
            </td>
          </tr>
        </tbody>
      </table>
      <i v-else><small>No logs to display</small></i>
    </div>
  </div>
</template>

<script>
import api from "@/api";

export default {
  name: "JobEventBox",
  props: {
    event: {
      type: Object
    }
  },
  data() {
    return {
      active: false,
      logs: [],
      loading: false,
      eventTitles: {
        ENQUEUED: 'Job enqueued',
        PROCESSING: 'Processing job',
        FAILED: 'Job processing failed',
        SUCCESS: 'Job processing succeeded'
      }
    }
  },
  methods: {
    toggleActive() {
      this.active = !this.active
      if (this.active) {
        this.fetchLogs()
      } else {
        this.logs = []
      }
    },
    fetchLogs() {
      this.loading = true
      api.get('/jobs/' + this.event.job_id + '/events/' + this.event.id + '/logs').then(res => {
        this.logs = res.data.data
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" type="text/css" scoped>
.event-box {

  margin: 0;
  -webkit-box-shadow: 0px 2px 4px 1px #7A7A7A;
  box-shadow: 0px 2px 4px 1px #7A7A7A;

  &.active {
    margin-top: 10px;
    margin-bottom: 10px;

    .event-box-header .summary {
      padding-top: 20px;
      padding-bottom: 20px;
    }

  }

  .event-box-header {
    cursor: pointer;
    user-select: none;
    .summary {
      margin-left: 15px;
      margin-right: 15px;
      padding-top: 15px;
      padding-bottom: 15px;
    }
  }

  .event-box-content {
    padding: 15px;
  }

  &.event-type-enqueued {
    .event-box-header {
      color: rgb(13, 60, 97);
      background-color: rgb(232, 244, 253);
    }
  }

  &.event-type-processing {
    .event-box-header {
      color: rgb(102, 60, 0);
      background-color: rgb(255, 244, 229);
    }
  }

  &.event-type-failed {
    .event-box-header {
      color: rgb(97, 26, 21);
      background-color: rgb(253, 236, 234);
    }
  }

  &.event-type-success {
    .event-box-header {
      color: rgb(30, 70, 32);
      background-color: rgb(237, 247, 237);
    }
  }

}
</style>