import axios from 'axios'

const client = axios.create({
    baseURL: window.location.origin + '/api'
})

export default client