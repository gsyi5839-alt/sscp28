<script setup lang="ts">
interface Props {
  showMore: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'toggleMore': []
}>()
</script>

<template>
  <div class="footer-bar">
    <div class="marquee-box">
      <div class="marquee-content">
        <b class="footer-marquee-text">尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)</b>
      </div>
    </div>
    <div class="footer-more" @click="$emit('toggleMore')">{{ showMore ? '返回' : '更多' }}</div>
  </div>
</template>

<style scoped>
.footer-bar {
  position: relative;
  z-index: 100;
  width: 100%;
  height: 34px;
  margin: 0 auto;
  margin-top: auto;
  line-height: 30px;
  background-color: #fdfdfd;
  border-top: 1px solid var(--el-border-color, #EFBA84);
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.marquee-box {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  position: relative;
}

.marquee-content {
  display: inline-block;
  white-space: nowrap;
  line-height: 34px;
  padding-left: 100%;
  animation: marquee-scroll 35s linear infinite;
}

@keyframes marquee-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

.footer-marquee-text {
  color: var(--bw-default-color, #351c0c);
  font-size: 13px;
  font-weight: bolder;
  font-family: "Microsoft YaHei", Tahoma, "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.footer-more {
  flex-shrink: 0;
  padding: 0 15px;
  color: #ff0000;
  font-size: 13px;
  cursor: pointer;
}

.footer-more:hover {
  text-decoration: underline;
}
</style>
