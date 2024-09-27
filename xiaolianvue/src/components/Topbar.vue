<script setup lang="js">
import { ElIcon } from 'element-plus';

const props = defineProps({
    washCount: {
        type: Number
    },
    washAvgTime: {
        type: String
    },
    useCount: {
        type: Number
    }
})

function jumpToGithub() {
    window.open("https://github.com/aquamarine5/XiaolianWebHelper", "_blanks")
}
</script>

<template>
    <div class="topbar_bg">
        <div class="topbar_container">
            <div class="topbar_title">
                XiaolianWebHelper
            </div>
            <svg class="topbar_github" @click="jumpToGithub">
                <path
                    d="M12.5.75C6.146.75 1 5.896 1 12.25c0 5.089 3.292 9.387 7.863 10.91.575.101.79-.244.79-.546 0-.273-.014-1.178-.014-2.142-2.889.532-3.636-.704-3.866-1.35-.13-.331-.69-1.352-1.18-1.625-.402-.216-.977-.748-.014-.762.906-.014 1.553.834 1.769 1.179 1.035 1.74 2.688 1.25 3.349.948.1-.747.402-1.25.733-1.538-2.559-.287-5.232-1.279-5.232-5.678 0-1.25.445-2.285 1.178-3.09-.115-.288-.517-1.467.115-3.048 0 0 .963-.302 3.163 1.179.92-.259 1.897-.388 2.875-.388.977 0 1.955.13 2.875.388 2.2-1.495 3.162-1.179 3.162-1.179.633 1.581.23 2.76.115 3.048.733.805 1.179 1.825 1.179 3.09 0 4.413-2.688 5.39-5.247 5.678.417.36.776 1.05.776 2.128 0 1.538-.014 2.774-.014 3.162 0 .302.216.662.79.547C20.709 21.637 24 17.324 24 12.25 24 5.896 18.854.75 12.5.75Z">
                </path>
            </svg>
        </div>
    </div>
    <div class="tipsbar_bg">
        <ElIcon class="tipsbar_icon" size="32" color="white">
            <PieChart/>
        </ElIcon>
        <div class="tipsbar_container">
            <div class="tipsbar_column">
                <div class="tipsbar_text">
                    当前统计洗浴次数：
                </div>
                <div class="tipsbar_value">
                    {{ props.washCount }}
                </div>
            </div>
            <div class="tipsbar_column">
                <div class="tipsbar_text">
                    平均洗浴时间：
                </div>
                <div class="tipsbar_value">
                    {{ props.washAvgTime }}
                </div>
            </div>
            <div class="tipsbar_column">
                <div class="tipsbar_text">
                    工具使用次数：
                </div>
                <div class="tipsbar_value">
                    {{ props.useCount }}
                </div>
            </div>
        </div>
    </div>
</template>

<style>
.tipsbar_column{
    display: flex;
}
.tipsbar_container{
    padding: 8px;
    padding-left: 20px;
    padding-bottom: 12px;
    font-size:small;
    font-weight: 600;
}
.tipsbar_icon{
    padding-left: 20px;
}
.tipsbar_bg {
    align-items: center;
    display: flex;
    background-color: #03a8f4a8;
    border-radius: 0px 0px 10px 10px;
}

.tipsbar_text {
    color: #b3e5fc;
}

.tipsbar_value {
    color: white;
    padding-left: 5px;
}

.topbar_bg {
    background-color: #03a9f4;
}

.topbar_container {
    padding-top: 15px;
    padding-inline: 20px;
    display: flex;
    height: 40px;
}

.topbar_title {
    font-weight: 800;
    font-size: larger;
    color: whitesmoke;
}

.topbar_github {
    cursor: pointer;
    justify-self: end;
    margin-left: auto;
    width: 24px;
    height: 24px;
}
</style>