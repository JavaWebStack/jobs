import axios from 'axios'

const DEBUG = process.env.NODE_ENV === 'DEBUG'

const client = axios.create({
    baseURL: (DEBUG ? 'http://localhost:8081' : window.location.origin) + '/api'
})

export default client