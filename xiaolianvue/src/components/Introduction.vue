<script setup lang="js">
import { ElIcon } from 'element-plus';
</script>

<template>
    <div class="introduction_bg">
        <ElIcon size="28" color="white" class="introduction_icon">
            <StarFilled/>
        </ElIcon>
        <div class="introduction_container">
            <div class="introduction_title">
                这是什么？
            </div>
            <div class="introduction_text">
                XiaolianWebHelper 可以监控每个淋浴头的使用情况，并帮助分析如何最快找到没有人的淋浴头。
            </div>
        </div>
    </div>
</template>

<style>
.introduction_icon{
    padding-right: 8px;
    padding-left: 5px;
}
.introduction_bg{
    margin-block: 9px;
    background-color: #ffd000;
    display: flex;
    align-items: center;
    padding: 7px;
    border-radius: 10px;
    font-weight: 400;
    
}
.introduction_title{
    color: white;
    font-size: medium;
    font-weight: 600;
}
.introduction_text{
    color: white;
    font-size: smaller;
    font-weight: 500;
}
</style>