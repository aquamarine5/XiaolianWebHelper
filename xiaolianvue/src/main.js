import './assets/main.css'

import { createApp, VueElement } from 'vue'
import App from './App.vue'

import axios from 'axios'

VueElement.prototype.$axios=axios
createApp(App).mount('#app')
