<script setup>

import axios from 'axios';
import { ElOption, ElSelect } from 'element-plus';
var selectedValue = defineModel('residenceId')
var placeholderText = defineModel('residenceText')
var residences = []
axios.get("http://47.96.24.132/api/list")
    .then(response => {
        residences = response.data
        console.log(residences)
    })
function onChangeSelectedValue(value) {
    sessionStorage.setItem("residenceId", value)
    sessionStorage.setItem("requireRequest", true)
}
var s = sessionStorage
function f() {
    window.location.reload()
}
</script>

<template>
    <div style="display: flex;">
        <div class="residenceList_btn" @click="s.setItem('residenceId', 1215856), f()">
            选择德明5宿舍（1215856）
        </div>
        <div class="residenceList_btn" @click="s.setItem('residenceId', 1215847), f()">
            选择德明2宿舍（1215847）
        </div>
    </div>
    <div class="residenceList" style="display: none;">
        <div class="residenceList_tips">
            请选择要查询的宿舍：
        </div>
        <div class="residenceList_select">
            <ElSelect v-model="selectedValue" :placeholder="placeholderText" style="width: 240px;"
                @change="onChangeSelectedValue">
                <ElOption v-for="residence in residences" :key="residence.residenceId" :value="residence.residenceId"
                    :label="residence.name" />
            </ElSelect>
        </div>

    </div>
    <div class="residenceList_notice">
        没有你所在的宿舍？请联系开发人员：抖音@aquamarine5, 微信+13373223536.
    </div>
</template>

<style>
.residenceList_btn {
    border-width: 2px;
    border-color: black;
    cursor: pointer;
    padding: 5px;
    border-radius: 6px;
    border-style: solid;
    width: fit-content;
    margin: 5px;
}

.residenceList_select {
    display: flex;
    align-items: center;
    gap: 40px;
    flex-wrap: wrap;
}

.residenceList_notice {
    font-weight: 400;
    color: white;
    font-size: smaller;
    background-color: rgb(83, 83, 83);
    border-radius: 3px;
}
</style>