<script setup lang="js">
import axios from 'axios';
import Device from './components/Device.vue';

</script>

<template>
    <div class="app_container" v-for="device in devicesList">
        <Device :name="device.name" :id="device.id" :status="device.status" :tme="device.time" />
    </div>
</template>

<script lang="js">

export default {
    data() {
        return {
            devicesList: this.getDevices()
        }
    },
    created() {
        this.getDevices()
        setInterval(() => {
            this.refreshDevices()
        }, 10000)
    },
    methods: {
        getDevices() {
            axios.get("http://47.96.24.132/api/wash")
                .then(response => {
                    var json = response.data
                    var out = []
                    json["devices"].forEach(element => {
                        out[element.id - 1] = {
                            id: element.id,
                            status: element.status,
                            time: element.time,
                            name: element.name
                        }
                    })
                    console.log(out)
                    this.devicesList = out;
                }).catch(function (err) {
                    console.log(err)
                    return [];
                })
        },
        refreshDevices() {
            axios.get("http://47.96.24.132/api/wash")
                .then(response => {
                    var json = response.data
                    json["devices"].forEach(element => {
                        this.devicesList[element.id - 1].status = element.status
                        this.devicesList[element.id - 1].time = element.time
                    })
                    console.log("refresh devices")
                }).catch(function (err) {
                    console.log(err)
                    return out;
                })
        }

    }
}
</script>