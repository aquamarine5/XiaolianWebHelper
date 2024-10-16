<script setup>
import { bus } from '@/bus';
import axios from 'axios';
import MapRoad from './MapRoad.vue';

var isShowMap=defineModel('isShowMap')
var mapData=[]
isShowMap.value=false
axios.get("http://47.96.24.132/api/map?id="+sessionStorage.getItem("residenceId"))
    .then(response=>{
        isShowMap.value=response.data.isShowMap
        mapData=response.data.mapData
    })
bus.$on('getData',(out)=>{
    if(!isShowMap) return;
})

bus.$on('refreshData',(devicesList)=>{
    if(!isShowMap) return;
})
</script>

<template>
    <div class="map_container">
        <div class="map_view" v-if="isShowMap">
            <MapRoad v-for="road in mapData" :roadData="road"/>
        </div>
        <div class="map_nodata" v-else>

        </div>
    </div>
</template>