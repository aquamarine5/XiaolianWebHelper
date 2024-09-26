<script setup>

import axios from 'axios';
import { ElOption, ElSelect } from 'element-plus';
var selectedValue=defineModel('residenceId')
var placeholderText=defineModel('residenceText')
var residences=[]
axios.get("http://47.96.24.132/api/list")
        .then(response => {
            residences=response.data
        })
function onChangeSelectedValue(value){
    sessionStorage.setItem("residenceId",value)
}
</script>

<template>
    <div class="residenceList">
        <div class="residenceList_tips">
            请选择要查询的宿舍：
        </div>
        <ElSelect v-model="selectedValue" :placeholder="placeholderText" style="width: 240px;" @change="onChangeSelectedValue">
            <ElOption v-for="residence in residences" :key="residence.residenceId" :value="residence.residenceId" :label="residence.name"/>
        </ElSelect>
        <div class="residenceList_notice">
            没有你所在的宿舍？请联系开发人员：抖音@aquamarine5, 微信+13373223536.
        </div>
    </div>
</template>

<style>
.residenceList_notice{
    font-weight: 400;
    color: white;
    font-size: smaller;
    background-color: rgb(51, 51, 51);
    border-radius: 3px;
}
</style>