<template>
    <div>
        <h2>Jobs</h2>
        <bs-tab :tabs="Object.keys(statuses).map(k => ({ key: k, text: statuses[k] }))" v-model="tab">
            <template v-slot:[`item`]="{ item }">
                <i class="bi" :class="{ ['bi-' + jobIcons[item.key]]: true }" style="margin-right: 10px;"></i>
                {{ item.text }}
                <span class="badge rounded-pill"
                      :class="{ [item.key === tab ? 'text-bg-primary' : 'text-bg-dark']: true }"
                      style="margin-left: 10px;">{{ jobCounts[item.key] }}</span>
            </template>
        </bs-tab>
        <div v-for="status in Object.keys(statuses)" :key="status"
             :style="{ display: status === tab ? 'block' : 'none' }">
            <bs-table :headers="[
                { key: 'bulk', text: '' },
                { key: 'id', text: 'ID' },
                { key: 'type', text: 'Type' },
                { key: 'created_at', text: 'Created At' }
            ]" :items="jobs">
                <template v-slot:[`header.bulk`]>
                    <input type="checkbox" class="form-check" v-model="checkAll"
                           @input="e => toggleCheckAll(e.target.checked)">
                </template>
                <template v-slot:[`item.bulk`]="{ item }">
                    <input type="checkbox" class="form-check" v-model="checked[item.id]"
                           @input="e => toggleChecked(item.id, e.target.checked)">
                </template>
                <template v-slot:[`item.id`]="{ item }">
                    <router-link :to="{ name: 'job', params: { id: item.id } }">{{ item.id }}</router-link>
                </template>
            </bs-table>
        </div>
        <job-creator ref="jobCreator" @created="this.fetchJobCounts()" :scheduled="tab === 'SCHEDULED'" />
        <button class="btn btn-primary" @click="$refs.jobCreator.show()" v-if="['ENQUEUED', 'SCHEDULED'].includes(tab)">Create Job</button>
        <div class="float-end">
            <nav>
                <ul class="pagination">
                    <li class="page-item">
                        <a class="page-link" @click="prevPage()" style="cursor: pointer; user-select: none;">
                            <span aria-hidden="true">&laquo;</span>
                        </a>
                    </li>
                    <li class="page-item"><a class="page-link">{{ page }}</a></li>
                    <li class="page-item">
                        <a class="page-link" @click="nextPage()" style="cursor: pointer; user-select: none;">
                            <span aria-hidden="true">&raquo;</span>
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    </div>
</template>

<script>
import BsTable from "@/components/BSTable";
import api from "@/api";
import BsTab from "@/components/BSTab";
import JobCreator from "@/components/JobCreator";

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
            jobCounts: {
                SCHEDULED: 0,
                ENQUEUED: 0,
                PROCESSING: 0,
                SUCCESS: 0,
                FAILED: 0
            },
            jobIcons: {
                SCHEDULED: 'clock',
                ENQUEUED: 'hourglass',
                PROCESSING: 'gear',
                SUCCESS: 'check-circle',
                FAILED: 'exclamation-circle'
            },
            page: 1,
            checkAll: false,
            checked: {},
            countTimer: undefined
        }
    },
    created() {
        this.fetchJobs()
        this.fetchJobCounts()
        this.countTimer = setInterval(this.fetchJobCounts, 3000)
    },
    beforeUnmount() {
        clearInterval(this.countTimer)
    },
    watch: {
        tab() {
            this.page = 1
            this.fetchJobs()
        }
    },
    methods: {
        spawnJob(queue, schedule_at, type, payload) {
            api.post('/jobs', {
                queue,
                schedule_at: schedule_at || undefined,
                type,
                payload
            }).then(() => {
                this.fetchJobs()
                this.fetchJobCounts()
            })
        },
        nextPage() {
            this.page++;
            this.fetchJobs();
        },
        prevPage() {
            if(this.page === 1)
                return;
            this.page--;
            this.fetchJobs();
        },
        fetchJobs() {
            this.jobs = []
            api.get('/jobs', {params: {status: this.tab, page: this.page}}).then(res => {
                this.checkAll = false
                this.checked = {}
                this.jobs = res.data.data
            })
        },
        fetchJobCounts() {
            api.get('/status/job-counts').then(res => {
                if (this.jobCounts[this.tab] !== res.data.data[this.tab]) {
                    this.fetchJobs();
                }
                this.jobCounts = res.data.data
            })
        },
        toggleChecked(id, value) {
            this.checked[id] = value
            this.checkAll = this.jobs.reduce((c, v) => c && this.checked[v.id], true)
        },
        toggleCheckAll(value) {
            if (value) {
                this.checkAll = true
                this.jobs.forEach(j => this.checked[j.id] = true)
            } else {
                this.checkAll = false
                this.checked = {}
            }
        }
    },
    components: {JobCreator, BsTab, BsTable}
}
</script>

<style scoped>

</style>