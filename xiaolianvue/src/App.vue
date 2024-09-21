<script setup>
import axios from 'axios';

</script>

<template>

</template>

<script lang="js">

export default {
    data(){
        return {
            devicesList:[]
        }
    },
    mounted(){
        this.devicesList=this.getDevices()
    },
    methods:{
        getDevices(){
            axios.get("127.0.0.1:8080/wash")
                .then(function(response){
                    var json=JSON.parse(response.data)
                    var out=[]
                    json["devices"].forEach(element => {
                        var m_status=defineModel()
                        m_status.value=element.status
                        var m_time=defineModel()
                        m_time=element.time
                        out[element.id]={
                            status:m_status,
                            time:m_time,
                            name:element.name
                        }
                    })
                    return out;
            }).catch(function(err){
                console.log(err)
                return out;
            })
        },
        refreshDevices(){
            axios.get("127.0.0.1:8080/wash")
                .then(function(response){
                    var json=JSON.parse(response.data)
                    json["devices"].forEach(element => {
                        this.devicesList[element.id].status.value=element.status
                        this.devicesList[element.id].time=element.time
                    })
            }).catch(function(err){
                console.log(err)
                return out;
            })
        }
        
    }
}
</script>