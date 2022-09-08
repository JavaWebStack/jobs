import { createRouter, createWebHistory } from 'vue-router'
import DashboardLayout from '../views/layout/DashboardLayout'

const routes = [
  {
    path: '/',
    component: DashboardLayout,
    children: [
      {
        path: '/overview',
        name: 'overview',
        component: () => import(/* webpackChunkName: "overview" */ '../views/Overview.vue')
      },
      {
        path: '/jobs',
        name: 'jobs',
        component: () => import(/* webpackChunkName: "jobs" */ '../views/jobs/Jobs.vue')
      },
      {
        path: '/jobs/:id',
        name: 'job',
        component: () => import(/* webpackChunkName: "job" */ '../views/jobs/Job.vue')
      },
      {
        path: '/workers',
        name: 'workers',
        component: () => import(/* webpackChunkName: "workers" */ '../views/workers/Workers.vue')
      }
    ]
  },

]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
