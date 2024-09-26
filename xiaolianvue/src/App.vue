<script setup lang="js">
import axios from 'axios';
import Device from './components/Device.vue';
import SuggestedDevice from './components/SuggestedDevice.vue';

var washCount = defineModel('washCount')
var avgWashTimeText = defineModel('avgWashTimeText')
var requestTimes = defineModel('requestTimes')
var devicesList = []
var suggestedWaitDevicesList = []
var suggestedTryDevicesList = []

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
            out.forEach(element => {
                if (element.status == 1 && (new Date().getTime() - element.wtime) > 360000) {
                    suggestedTryDevicesList.push(element)
                }
                if (element.status == 2 && (new Date().getTime() - element.time) > json.avgWashTime) {
                    suggestedWaitDevicesList.push(element)
                }
            })
            suggestedTryDevicesList.sort((a, b) => a.wtime - b.wtime)
            suggestedWaitDevicesList.sort((a, b) => a.time - b.time)

            suggestedTryDevicesList = suggestedTryDevicesList.slice(0, 20)
            suggestedWaitDevicesList = suggestedWaitDevicesList.slice(0, 20)

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
            suggestedTryDevicesList = []
            suggestedWaitDevicesList = []
            devicesList.forEach(element => {
                if (element.status == 1 && (new Date().getTime() - element.wtime) > 360000) {
                    suggestedTryDevicesList.push(element)
                }
                if (element.status == 2 && (new Date().getTime() - element.time) > json.avgWashTime) {
                    suggestedWaitDevicesList.push(element)
                }
            })
            suggestedTryDevicesList.sort((a, b) => a.wtime - b.wtime)
            suggestedWaitDevicesList.sort((a, b) => a.time - b.time)
            suggestedTryDevicesList = suggestedTryDevicesList.slice(0, 20)
            suggestedWaitDevicesList = suggestedWaitDevicesList.slice(0, 20)
            console.log("refresh devices")
        }).catch(function (err) {
            console.log(err)
            return out;
        })
}

var showTryMoreStatus = defineModel('showTryMoreStatus')
var showWaitMoreStatus = defineModel('showWaitMoreStatus')
showTryMoreStatus.value = false
showWaitMoreStatus.value = false

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
    
    <div class="top_container">
        <div class="suggested_tips">
            推荐去尝试可能没人的淋浴头：
        </div>
        <div class="suggested_container" v-for="device in suggestedTryDevicesList.slice(0, 6)">
            <SuggestedDevice :name="device.name" :id="device.id" :status="device.status" :tme="device.wtime" />
        </div>
        <div class="suggested_more_container" v-if="showTryMoreStatus">
            <div class="suggested_container" v-for="mdevice in suggestedTryDevicesList.slice(6, 20)">
                <SuggestedDevice :name="mdevice.name" :id="mdevice.id" :status="mdevice.status" :tme="mdevice.wtime" />
            </div>
        </div>
        <div class="suggested_more_btn" @click="showTryMoreStatus = !showTryMoreStatus">
            {{ showTryMoreStatus ? "收起" : "展开" }}
        </div>
    </div>
    <div class="top_container">

        <div class="suggested_tips">
            推荐去尝试马上使用完毕的淋浴头：
        </div>
        <div class="suggested_container" v-for="device in suggestedWaitDevicesList.slice(0, 6)">
            <SuggestedDevice :name="device.name" :id="device.id" :status="device.status" :tme="device.wtime" />
        </div>
        <div class="suggested_more_container" v-if="showWaitMoreStatus">
            <div class="suggested_container" v-for="mdevice in suggestedWaitDevicesList.slice(6, 20)">
                <SuggestedDevice :name="mdevice.name" :id="mdevice.id" :status="mdevice.status" :tme="mdevice.wtime" />
            </div>
        </div>
        <div class="suggested_more_btn" @click="showWaitMoreStatus = !showWaitMoreStatus">
            {{ showWaitMoreStatus ? "收起" : "展开" }}
        </div>
    </div>
    <div class="app_container">
        <div class="device_container" v-for="device in devicesList">
            <Device :name="device.name" :id="device.id" :status="device.status" :tme="device.time"
                :wtime="device.wtime" />
        </div>
    </div>
</template>

<style>
.suggested_more_btn {
    cursor: pointer;
}

.app_container {
    padding-top: 10px;
}
</style>