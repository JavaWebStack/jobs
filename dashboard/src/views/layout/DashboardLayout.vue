<template>
  <div>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <div class="container-fluid">
        <a class="navbar-brand" href="#">
          <img src="https://raw.githubusercontent.com/JavaWebStack/Docs/master/docs/assets/img/icon-dark.svg" alt="" width="24" height="24" class="d-inline-block align-text-top">
          Jobs
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarColor02" aria-controls="navbarColor02" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarColor02">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" active-class="active" :to="{ name: 'overview' }">Overview</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" active-class="active" :to="{ name: 'jobs' }">Jobs</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" active-class="active" :to="{ name: 'workers' }">Workers</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <div class="container-fluid mt-3">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: "DashboardLayout"
}
</script>

<style scoped>

</style>