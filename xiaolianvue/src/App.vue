<script setup lang="js">
import axios from 'axios';
import Device from './components/Device.vue';

</script>

<template>
    <a href="https://github.com/aquamarine5/XiaolianWebHelper">
        Github项目链接
    </a>
    <div>
        当前统计洗浴次数：{{ data.washCount }}，平均洗浴时间：{{ data.avgWashTimeText }}，第 {{ data.requestTimes }} 个使用本工具。
    </div>
    <div class="app_container" v-for="device in devicesList">
        <Device :name="device.name" :id="device.id" :status="device.status" :tme="device.time" />
    </div>
</template>

<script lang="js">

export default {
    data() {
        return {
            devicesList: [],
            data: {}
        }
    },
    created() {
        this.getDevices()
        setInterval(() => {
            this.refreshDevices()
        }, 10000)
    },
    methods: {
        formatDate(t) {
            var seconds = Math.floor((t / 1000) % 60),
                minutes = Math.floor((t / (1000 * 60)) % 60),
                hours = Math.floor((t / (1000 * 60 * 60)) % 24)

            hours = (hours < 10) ? "0" + hours : hours
            minutes = (minutes < 10) ? "0" + minutes : minutes
            seconds = (seconds < 10) ? "0" + seconds : seconds
            if (hours == "00")
                return minutes + " 分 " + seconds + " 秒"

            return hours + " 时 " + minutes + " 分 " + seconds + " 秒"
        },
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
                    this.data = {
                        washCount: json.avgWashCount,
                        avgWashTimeText:this.formatDate(json.avgWashTime),
                        requestTimes:json.requestTimes
                    }
                    console.log(out)
                    this.devicesList = out;
                }).catch(function (err) {
                    console.log(err)
                    return [];
                })
        },
        refreshDevices() {
            axios.get("http://47.96.24.132/api/refresh")
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