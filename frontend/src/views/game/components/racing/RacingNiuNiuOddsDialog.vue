<script setup lang="ts">
type OddsMode = 'double' | 'flat'

interface Props {
  show: boolean
  mode: OddsMode
}

defineProps<Props>()

const emit = defineEmits<{
  close: []
}>()

const typeLabels = ['无牛', '牛一', '牛二', '牛三', '牛四', '牛五', '牛六', '牛七', '牛八', '牛九', '牛牛']
const doubleOdds = ['1.97', '1.97', '2.97', '3.97', '4.97', '5.97', '6.97', '7.97', '8.97', '9.97', '10.97']
const flatOdds = typeLabels.map(() => '1.97')

const getTitle = (mode: OddsMode) => mode === 'double' ? '牌型翻倍赔率介绍' : '牌型平倍赔率介绍'
const getIntro = (mode: OddsMode) => (
  mode === 'double'
    ? '下注金额会暂时冻结下注金额的 9 倍加本金，开奖后连本带利一并返还。'
    : '平倍玩法赔率相同且无需冻结资金'
)
const getOdds = (mode: OddsMode) => mode === 'double' ? doubleOdds : flatOdds
</script>

<template>
  <div v-if="show" class="niuniu-odds-mask">
    <div class="niuniu-odds-dialog">
      <div class="niuniu-odds-header">
        <h4>{{ getTitle(mode) }}</h4>
        <button class="niuniu-odds-close" type="button" @click="emit('close')">×</button>
      </div>
      <div class="niuniu-odds-body">
        <table class="niuniu-odds-table">
          <thead>
            <tr>
              <th>牌型</th>
              <th>赔率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(label, index) in typeLabels" :key="label">
              <td>{{ label }}</td>
              <td class="odds-value">{{ getOdds(mode)[index] }}</td>
            </tr>
          </tbody>
        </table>
        <p class="niuniu-odds-intro">{{ getIntro(mode) }}</p>
        <p class="niuniu-odds-note">
          注：当庄家与闲家点数相等时，牛一以上（包含牛一）的点数第一张牌比大小（例如：庄家：15462牛八，闲家：46297牛八，4比1大，闲家赢）。且庄家通吃闲家无牛。
        </p>
        <div class="niuniu-odds-footer">
          <button class="niuniu-odds-confirm" type="button" @click="emit('close')">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.niuniu-odds-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, .18);
}

.niuniu-odds-dialog {
  width: 400px;
  background: #fff;
  border: 1px solid var(--bw-border-color, #efba84);
  box-shadow: 0 12px 32px 4px rgba(0, 0, 0, .04), 0 8px 20px rgba(0, 0, 0, .08);
  font-family: Microsoft YaHei, Tahoma, HelveticaNeue-Light, Helvetica Neue Light, Helvetica Neue, Helvetica, Arial, sans-serif;
  color: #000;
}

.niuniu-odds-header {
  height: 34px;
  padding: 0 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  background: #fffaf5;
}

.niuniu-odds-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
}

.niuniu-odds-close {
  width: 25px;
  height: 25px;
  border: 1px solid #d86f5d;
  border-radius: 3px;
  color: #fff;
  font-size: 26px;
  line-height: 20px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(180deg, #ffb6a5 0%, #e5492d 52%, #c82917 100%);
}

.niuniu-odds-body {
  padding: 16px 6px 8px;
}

.niuniu-odds-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.niuniu-odds-table th,
.niuniu-odds-table td {
  height: 30px;
  line-height: 30px;
  border: 1px solid var(--bw-border-color, #efba84);
  text-align: center;
  font-size: 14px;
  font-weight: 700;
}

.niuniu-odds-table th {
  background: #fffaf5;
}

.odds-value {
  color: #ed2c25;
  font-size: 16px;
}

.niuniu-odds-intro {
  margin: 10px 0 0;
  font-size: 12px;
  line-height: 18px;
  text-align: left;
}

.niuniu-odds-note {
  margin: 10px 0 0;
  color: #ff2d20;
  font-size: 12px;
  line-height: 18px;
  text-align: left;
}

.niuniu-odds-footer {
  margin-top: 18px;
  text-align: center;
}

.niuniu-odds-confirm {
  width: 46px;
  height: 20px;
  border: 0;
  border-radius: 2px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(180deg, #ff9c00, #ff5100);
}
</style>
