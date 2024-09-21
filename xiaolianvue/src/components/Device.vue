<script setup lang="js">

const props = defineProps({
    name: {
        type: String
    },
    id: {
        type: Number
    },
    status: {
        type: Number
    },
    tme: {
        type: Number
    }
})
function formatDate(t) {
    var seconds = Math.floor((t / 1000) % 60),
        minutes = Math.floor((t / (1000 * 60)) % 60),
        hours = Math.floor((t / (1000 * 60 * 60)) % 24)

    hours = (hours < 10) ? "0" + hours : hours
    minutes = (minutes < 10) ? "0" + minutes : minutes
    seconds = (seconds < 10) ? "0" + seconds : seconds
    if (hours == "00")
        return minutes + " 分 " + seconds

    return hours + " 时 " + minutes + " 秒 " + seconds
}
function formatColor(s) {
    if (s == 1) return '#000'
    else if (s == 2) return '#00F'
    else return '#FF0'
}
var time = defineModel()
var timer;
time.value = props.status == 2 ? formatDate(new Date().getTime() - props.tme) : ""
timer = setInterval(function () {
    time.value = props.status == 2 ? formatDate(new Date().getTime() - props.tme) : ""
}, 1000)
</script>

<template>
    <div class="device_container">
        <div class="device_container_top">
            <div class="device_status" :style="{ 'background-color': formatColor(props.status) }"></div>
            <div class="device_id">{{ props.id }}</div>
            <div class="device_name">{{ props.name }}</div>
        </div>
        <div class="device_time">
            <div class="device_time_text">已经洗了：</div>
            <div class="device_time_dynamic">{{ time }}</div>
        </div>
    </div>
</template>

<script lang="js">

export default {
    data() {
        return {
            timer: null,
            time: 0
        }
    }
}
</script>

<style>
.device_id {
    font-weight: 600;
    padding-right: 10px;
}

.device_name {
    color: rgb(75, 75, 75)
}

.device_container {
    height: 50px;
}

.device_status {
    height: 13px;
    width: 13px;
    border-radius: 3px;
    margin-right: 10px;
}

.device_container_top {
    display: flex;
    align-items: center;
    padding-bottom: 1px;
}

.device_time {
    display: flex;
    align-items: center;
}
</style>