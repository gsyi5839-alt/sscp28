<script setup lang="ts">
import { computed, ref } from 'vue'
import { calcNiuNiu10 } from '@/utils/lotteryCalc'
import { RACING_NIUNIU_SECTIONS, type RacingBetItem } from '../../constants/racingOdds'
import RacingNiuNiuOddsDialog from './RacingNiuNiuOddsDialog.vue'

interface Props {
  isSealed: boolean
  quickMode: 'quick' | 'normal'
  amounts: Record<string, string>
  isSelected: (key: string) => boolean
  preDrawBalls: number[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'toggle': [key: string]
  'ensure': [key: string]
  'update:amounts': [val: Record<string, string>]
}>()

interface NiuCard {
  value: number
  type: number
}

interface NiuHand {
  label: string
  cards: NiuCard[]
  result: string
  niuIndex: number
  isBanker: boolean
  isWin: boolean
}

const pokerSprite = new URL('../../../../assets/游戏/poker.png', import.meta.url).href
const niuWinSprite = new URL('../../../../assets/游戏/niu_win.png', import.meta.url).href
const niuLoseSprite = new URL('../../../../assets/游戏/niu_lose.png', import.meta.url).href
const winBadge = new URL('../../../../assets/游戏/niuniu/win.png', import.meta.url).href
const showOddsDialog = ref(false)
const oddsDialogMode = ref<'double' | 'flat'>('double')

const HAND_LABELS = ['庄', '闲一', '闲二', '闲三', '闲四', '闲五']
const CARD_TYPE_MATRIX = [
  [1, 2, 4, 4, 4],
  [4, 4, 4, 2, 1],
  [1, 1, 4, 3, 2],
  [4, 2, 4, 3, 4],
  [4, 4, 3, 3, 2],
  [2, 1, 4, 4, 2],
]

const niuAssetStyle = {
  '--racing-niu-poker': `url("${pokerSprite}")`,
  '--racing-niu-win': `url("${niuWinSprite}")`,
  '--racing-niu-lose': `url("${niuLoseSprite}")`,
  '--racing-niu-badge': `url("${winBadge}")`,
}

const validBalls = computed(() => {
  const nums = props.preDrawBalls
    .map(Number)
    .filter(num => Number.isFinite(num) && num >= 1 && num <= 10)
  return nums.length >= 10 ? nums.slice(0, 10) : []
})

const niuValue = (result: string): number => {
  if (result === '牛牛') return 10
  if (result === '无牛') return 0
  const chineseNums: Record<string, number> = {
    一: 1,
    二: 2,
    三: 3,
    四: 4,
    五: 5,
    六: 6,
    七: 7,
    八: 8,
    九: 9,
  }
  return chineseNums[result.replace('牛', '')] ?? 0
}

const isPlayerWin = (
  playerCards: NiuCard[],
  playerValue: number,
  bankerCards: NiuCard[],
  bankerValue: number,
): boolean => {
  if (playerValue <= 0) return false
  if (bankerValue <= 0) return true
  if (playerValue !== bankerValue) return playerValue > bankerValue
  return (playerCards[0]?.value ?? 0) > (bankerCards[0]?.value ?? 0)
}

const niuHands = computed<NiuHand[]>(() => {
  const nums = validBalls.value
  const results = calcNiuNiu10(nums)
  const bankerValue = niuValue(results[0] ?? '无牛')
  const bankerCards = nums.slice(0, 5).map((value, cardIndex) => ({
    value,
    type: CARD_TYPE_MATRIX[0]?.[cardIndex] ?? ((cardIndex % 4) + 1),
  }))

  const hands = HAND_LABELS.map((label, handIndex) => {
    const cards = nums.slice(handIndex, handIndex + 5).map((value, cardIndex) => ({
      value,
      type: CARD_TYPE_MATRIX[handIndex]?.[cardIndex] ?? ((cardIndex % 4) + 1),
    }))
    const result = results[handIndex] ?? '无牛'
    const value = niuValue(result)

    return {
      label,
      cards,
      result,
      niuIndex: value,
      isBanker: handIndex === 0,
      isWin: handIndex > 0 && isPlayerWin(cards, value, bankerCards, bankerValue),
    }
  })

  const hasPlayerWin = hands.some(hand => !hand.isBanker && hand.isWin)
  return hands.map(hand => ({
    ...hand,
    isWin: hand.isBanker ? !hasPlayerWin : hand.isWin,
  }))
})

const getCellKey = (item: RacingBetItem) => item.key

const onCellClick = (item: RacingBetItem) => {
  if (props.isSealed || item.disabled) return
  emit('toggle', getCellKey(item))
}

const updateAmount = (key: string, value: string) => {
  emit('update:amounts', { ...props.amounts, [key]: value })
}

const openOddsDialog = (mode: 'double' | 'flat') => {
  oddsDialogMode.value = mode
  showOddsDialog.value = true
}

const getPokerStyle = (card: NiuCard) => {
  const yIndex = card.value >= 10 ? 9 : card.value - 1
  return {
    backgroundPositionX: `${(card.type - 1) * 33.3333333333}%`,
    backgroundPositionY: `${yIndex * 8.3333333333}%`,
  }
}
</script>

<template>
  <div class="racing-niuniu-panel" :style="niuAssetStyle">
    <div class="niuniu-wrapper">
      <div
        v-for="hand in niuHands"
        :key="hand.label"
        class="item-wrapper"
      >
        <div class="poker-wrapper show" :class="{ win: hand.isWin }">
          <span
            v-for="(card, cardIndex) in hand.cards"
            :key="`${hand.label}-${cardIndex}-${card.value}`"
            class="poker"
            :class="[`type-${card.type}`, `card-${card.value}`]"
            :style="getPokerStyle(card)"
          ></span>
        </div>
        <div class="poker-info">
          <span class="two-hands" :class="hand.isBanker ? 'banker' : 'player'">
            {{ hand.label }}
          </span>
          <span class="game-info" :class="`niu-${hand.niuIndex}`"></span>
        </div>
      </div>
    </div>

    <div class="racing-niuniu-bet-wrap">
      <div
        v-for="section in RACING_NIUNIU_SECTIONS"
        :key="section.key"
        class="racing-niuniu-section"
      >
        <div class="racing-niuniu-title">
          {{ section.title }}
          <button
            type="button"
            class="racing-niuniu-odds-btn"
            @click.stop="openOddsDialog(section.key === 'niuNiuDouble' ? 'double' : 'flat')"
          >
            赔率说明
          </button>
        </div>
        <div class="racing-niuniu-row">
          <div
            v-for="item in section.items"
            :key="item.key"
            class="racing-niuniu-cell"
            :class="{
              'racing-cell--selected': isSelected(getCellKey(item)),
              'racing-cell--sealed': isSealed,
            }"
            @click="onCellClick(item)"
          >
            <div class="racing-niuniu-label">{{ item.label }}</div>
            <div class="racing-niuniu-odd">
              <b>{{ isSealed ? '--' : item.odd }}</b>
            </div>
            <div class="racing-niuniu-input-wrap">
              <input
                v-if="quickMode === 'normal'"
                :value="amounts[getCellKey(item)] ?? ''"
                class="racing-input racing-niuniu-input"
                type="text"
                :disabled="isSealed"
                @input="updateAmount(getCellKey(item), ($event.target as HTMLInputElement).value)"
                @focus="$emit('ensure', getCellKey(item))"
                @click.stop
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <RacingNiuNiuOddsDialog
      :show="showOddsDialog"
      :mode="oddsDialogMode"
      @close="showOddsDialog = false"
    />
  </div>
</template>

<style scoped>
.racing-niuniu-panel {
  width: 720px;
  font-family: Microsoft YaHei, Tahoma, HelveticaNeue-Light, Helvetica Neue Light, Helvetica Neue, Helvetica, Arial, sans-serif;
  font-size: 13px;
  text-align: center;
}

.niuniu-wrapper {
  height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-wrapper {
  flex: 1 1 16.6666666667%;
}

.poker-wrapper {
  display: flex;
  margin: auto;
  width: 110px;
  position: relative;
}

.poker-wrapper.win::before {
  content: "";
  position: absolute;
  width: 46px;
  height: 46px;
  top: -23px;
  right: -23px;
  z-index: 1;
  background: var(--racing-niu-badge) center / contain no-repeat;
}

.poker {
  width: 30px;
  height: 40px;
  border-radius: 6px;
  opacity: 1;
  transform: translate(0);
  box-shadow: 0 4px 10px rgba(55, 55, 55, .7);
  background-image: var(--racing-niu-poker);
  background-size: 400% 1300%;
  overflow: visible !important;
}

.poker:not(:first-child) {
  margin-left: -8px;
}

.poker-info {
  width: 120px;
  display: flex;
  align-items: center;
  margin: 20px auto 0;
  min-height: 37px;
}

.two-hands {
  height: 24px;
  width: 60px;
  border-radius: 48px;
  display: block;
  color: #fff;
  font-weight: 700;
  line-height: 24px;
  font-size: 16px;
  text-align: center;
  padding: 0 10px;
}

.two-hands.banker {
  background-color: #db0001;
}

.two-hands.player {
  background-color: #2e69a9;
}

.game-info {
  width: 60px;
  height: 37px;
  margin-left: auto;
  background-image: var(--racing-niu-lose);
  background-size: 60px 408px;
  background-position: 0 0;
}

.poker-wrapper.win + .poker-info .game-info {
  background-image: var(--racing-niu-win);
}

.game-info.niu-0 { background-position: 0 0; }
.game-info.niu-1 { background-position: 0 -37px; }
.game-info.niu-2 { background-position: 0 -74px; }
.game-info.niu-3 { background-position: 0 -111px; }
.game-info.niu-4 { background-position: 0 -148px; }
.game-info.niu-5 { background-position: 0 -185px; }
.game-info.niu-6 { background-position: 0 -222px; }
.game-info.niu-7 { background-position: 0 -259px; }
.game-info.niu-8 { background-position: 0 -296px; }
.game-info.niu-9 { background-position: 0 -333px; }
.game-info.niu-10 { background-position: 0 -370px; }

.racing-niuniu-bet-wrap {
  display: flex;
  flex-wrap: wrap;
  text-align: center;
}

.racing-niuniu-section {
  width: 100%;
}

.racing-niuniu-title {
  position: relative;
  height: 35px;
  line-height: 35px;
  font-size: 14px;
  font-weight: 700;
  background: #e0e0e0;
  border: 1px solid var(--bw-border-color, #efba84);
  border-bottom: none;
  box-sizing: border-box;
}

.racing-niuniu-section + .racing-niuniu-section .racing-niuniu-title {
  border-top: none;
}

.racing-niuniu-odds-btn {
  position: absolute;
  top: 8px;
  right: 10px;
  height: 20px;
  min-width: 64px;
  padding: 0 8px;
  border: 0;
  border-radius: 2px;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  line-height: 20px;
  background: linear-gradient(180deg, #ff9c00, #ff5100);
  cursor: pointer;
}

.racing-niuniu-row {
  display: flex;
  flex: 1;
}

.racing-niuniu-cell {
  flex: 1 1 20%;
  display: flex;
  justify-content: space-between;
  height: 30px;
  line-height: 30px;
  border: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.racing-niuniu-cell + .racing-niuniu-cell {
  border-left: none;
}

.racing-niuniu-cell:not(.racing-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.racing-niuniu-cell.racing-cell--selected {
  background: #ffc214;
}

.racing-niuniu-label {
  width: 40px;
  flex: 0 0 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  background: var(--bw-bg-3, #fff7ef);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.racing-niuniu-odd,
.racing-niuniu-input-wrap {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.racing-niuniu-input-wrap {
  border-right: 0;
}

.racing-niuniu-odd b {
  color: red;
  font-weight: 700;
}

.racing-niuniu-input {
  width: 50px;
  height: 22px;
  padding: 0 4px;
  border: 1px solid #abb2c5;
  border-radius: 4px;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: textfield;
  background: #fff;
  color: #000;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  box-sizing: border-box;
}
.racing-niuniu-cell.racing-cell--selected .racing-niuniu-label,
.racing-niuniu-cell.racing-cell--selected .racing-niuniu-odd,
.racing-niuniu-cell.racing-cell--selected .racing-niuniu-input-wrap {
  background: #ffc214;
}

.racing-niuniu-cell.racing-cell--selected .racing-niuniu-input {
  background: #fff;
}
</style>
