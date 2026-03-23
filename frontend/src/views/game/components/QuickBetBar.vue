<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import type { QuickMode } from '../composables/useBetting'

interface Props {
  quickMode: QuickMode
  showRecentButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showRecentButton: false
})

const emit = defineEmits<{
  'update:quickMode': [mode: QuickMode]
  'explain': []
  'recent': []
}>()

const setQuickMode = (mode: QuickMode) => {
  emit('update:quickMode', mode)
}

const onExplainClick = () => {
  ElMessageBox.alert(
    '保存可以将金额保存為常用筹码，最多可以保存三个。',
    'xxobudi.gl7f25n0.com显示',
    {
      confirmButtonText: '确定',
      showClose: false,
      closeOnClickModal: true,
      customClass: 'explain-messagebox',
    }
  )
}
</script>

<template>
  <div class="quick-bar">
    <span
      class="quick-tab"
      :class="{ active: quickMode === 'quick' }"
      @click="setQuickMode('quick')"
    >
      快捷
    </span>
    <span
      class="quick-tab"
      :class="{ active: quickMode === 'normal' }"
      @click="setQuickMode('normal')"
    >
      一般
    </span>
    <span class="text-blue ml10">金额</span>
    <input class="amount-input" type="text" />
    <button class="btn btn-ok">确定</button>
    <button class="btn btn-clear">清空</button>
    <button class="btn btn-save">保存</button>
    <span class="ml10 explain-link" role="button" tabindex="0" @click="onExplainClick">（说明）</span>
    <button v-if="showRecentButton" class="btn btn-recent" @click="$emit('recent')">最近开奖</button>
  </div>
</template>

<style scoped>
.quick-bar {
  width: 720px;
  height: 49px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  border-bottom: none;
  box-sizing: border-box;
  font-size: 12px;
  gap: 6px;
}

.quick-tab {
  width: 35px;
  text-align: center;
  cursor: pointer;
  color: #ff0000;
}

.quick-tab.active {
  background: #ffffbf;
  border: 1px solid var(--bw-border-color, #efba84);
  height: 25px;
  line-height: 25px;
  color: #ff0000;
}

.text-blue {
  color: blue;
}

.ml10 {
  margin-left: 10px;
}

.amount-input {
  width: 55px;
  height: 24px;
  border: 1px solid #a0b4d8;
  border-radius: 0.5px;
  box-sizing: border-box;
}

.amount-input:focus,
.amount-input:focus-visible {
  outline: none;
  box-shadow: none;
  border-color: #000;
}

.btn {
  width: 46px;
  height: 20px;
  border: none;
  font-size: 12px;
  cursor: pointer;
  color: #fff;
}

.btn-ok {
  background: #63a35c;
}

.btn-clear {
  background: #4a90e2;
}

.btn-save {
  background: #f5a623;
}

.btn-recent {
  width: auto;
  min-width: 72px;
  height: 20px;
  padding: 0 10px;
  line-height: 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ff9c00, #ff5100);
}

.explain-link {
  cursor: pointer;
}

.explain-link:hover {
  text-decoration: underline;
}
</style>
