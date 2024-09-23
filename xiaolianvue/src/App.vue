<script setup lang="js">
import axios from 'axios';
import Device from './components/Device.vue';
var washCount = defineModel('washCount')
var avgWashTimeText = defineModel('avgWashTimeText')
var requestTimes = defineModel('requestTimes')
var devicesList=[]

function formatDate(t) {
    var seconds = Math.floor((t / 1000) % 60),
        minutes = Math.floor((t / (1000 * 60)) % 60),
        hours = Math.floor((t / (1000 * 60 * 60)) % 24)

    hours = (hours < 10) ? "0" + hours : hours
    minutes = (minutes < 10) ? "0" + minutes : minutes
    seconds = (seconds < 10) ? "0" + seconds : seconds
    if (hours == "00")
        return minutes + " 分 " + seconds + " 秒"

    return hours + " 时 " + minutes + " 分 " + seconds + " 秒"
}

function getDevices() {
    axios.get("http://47.96.24.132/api/wash")
        .then(response => {
            var json = response.data
            var out = []
            json["devices"].forEach(element => {
                out[element.id - 1] = {
                    id: element.id,
                    status: element.status,
                    time: element.time,
                    name: element.name,
                    wtime: element.wtime
                }
            })
            washCount.value = json.avgWashCount
            avgWashTimeText.value = formatDate(json.avgWashTime)
            requestTimes.value = json.requestTimes

            console.log(out)
            devicesList = out;
        }).catch(function (err) {
            console.log(err)
            return [];
        })
}
function refreshDevices() {
    axios.get("http://47.96.24.132/api/refresh")
        .then(response => {
            var json = response.data
            json["devices"].forEach(element => {
                devicesList[element.id - 1].status = element.status
                devicesList[element.id - 1].time = element.time
            })
            
            washCount.value = json.avgWashCount
            avgWashTimeText.value = formatDate(json.avgWashTime)
            requestTimes.value = json.requestTimes
            console.log("refresh devices")
        }).catch(function (err) {
            console.log(err)
            return out;
        })
}

getDevices()
setInterval(() => {
    refreshDevices()
}, 10000)

</script>

<template>
    <a href="https://github.com/aquamarine5/XiaolianWebHelper">
        Github项目链接
    </a>
    <div class="app_detail">
        当前统计洗浴次数：{{ washCount }}，平均洗浴时间：{{ avgWashTimeText }}，第 {{ requestTimes }} 个使用本工具。
    </div>
    <div class="app_container" v-for="device in devicesList">
        <Device :name="device.name" :id="device.id" :status="device.status" :tme="device.time" :wtime="device.wtime" />
    </div>
</template>

<style>
</style>