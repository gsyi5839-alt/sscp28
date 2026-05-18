<script setup lang="ts">
import {
  RACING_BAODOU_SECTIONS,
  RACING_POSITION_GROUPS,
  RACING_POSITION_NUMBER_GROUPS,
  RACING_TOP_SUM_ITEMS,
  RACING_TOP_SUM_NUMBER_ITEMS,
  type RacingBetItem,
  type RacingPanelMode,
} from '../../constants/racingOdds'
import { getRacingBallSrc } from '../../composables/useOddsStyles'
import { computed } from 'vue'
import RacingNiuNiuPanel from './RacingNiuNiuPanel.vue'

interface Props {
  isSealed: boolean
  quickMode: 'quick' | 'normal'
  amounts: Record<string, string>
  isSelected: (key: string) => boolean
  preDrawBalls: number[]
  mode?: RacingPanelMode
  positionKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'all',
  positionKey: undefined,
})

const emit = defineEmits<{
  'toggle': [key: string]
  'ensure': [key: string]
  'update:amounts': [val: Record<string, string>]
}>()

interface RacingPanelSection {
  key: string
  title: string
  items: RacingBetItem[]
}

interface BaoDouBoardCell {
  label: string
  className: string
  item: RacingBetItem
}

const TOP_SUM_BLANK_ITEMS: RacingBetItem[] = [
  { key: 'racing_top_sum_blank_1', label: '', odd: '--', disabled: true },
  { key: 'racing_top_sum_blank_2', label: '', odd: '--', disabled: true },
  { key: 'racing_top_sum_blank_3', label: '', odd: '--', disabled: true },
]

const updateAmount = (amounts: Record<string, string>, key: string, value: string) => {
  emit('update:amounts', { ...amounts, [key]: value })
}

const chunkItems = (items: RacingBetItem[], size: number): RacingBetItem[][] => {
  const rows: RacingBetItem[][] = []
  for (let index = 0; index < items.length; index += size) {
    rows.push(items.slice(index, index + size))
  }
  return rows
}

const positionGroups = computed(() => {
  if (props.mode === 'position' && props.positionKey) {
    return RACING_POSITION_GROUPS.filter(group => group.key === props.positionKey)
  }

  if (props.mode !== 'all') {
    return []
  }

  return RACING_POSITION_GROUPS
})

const positionNumberGroups = computed<RacingPanelSection[]>(() => {
  if (props.mode !== 'position') {
    return []
  }
  if (!props.positionKey) return RACING_POSITION_NUMBER_GROUPS
  return RACING_POSITION_NUMBER_GROUPS.filter(group => group.key === props.positionKey)
})

const showTopSumNumbers = computed(() => props.mode === 'topSum')
const showTopSumSection = computed(() => props.mode === 'all')
const topSumCombinationRows = computed(() => [
  ...chunkItems([...RACING_TOP_SUM_NUMBER_ITEMS, ...TOP_SUM_BLANK_ITEMS], 4),
  RACING_TOP_SUM_ITEMS,
])
const isBaoDouMode = computed(() => props.mode === 'baoDou')
const isNiuNiuMode = computed(() => props.mode === 'niuNiu')
const specialSections = computed<RacingPanelSection[]>(() => {
  return []
})

const baoDouItemMap = computed(() => {
  return new Map(
    RACING_BAODOU_SECTIONS.flatMap((section) =>
      section.items.map((item) => [item.label, item] as const),
    ),
  )
})

const getBaoDouItemByLabel = (label: string): RacingBetItem => {
  return baoDouItemMap.value.get(label) ?? {
    key: `baodou_missing_${label}`,
    label,
    odd: '--',
    disabled: true,
  }
}

const baoDouBoardCells = computed<BaoDouBoardCell[]>(() => [
  { label: '虎入角', className: 'bd-corner-tl', item: getBaoDouItemByLabel('虎入角') },
  { label: '入正念虎', className: 'bd-top-left', item: getBaoDouItemByLabel('入正念虎') },
  { label: '入正念龙', className: 'bd-top-right', item: getBaoDouItemByLabel('入正念龙') },
  { label: '龙入角', className: 'bd-corner-tr', item: getBaoDouItemByLabel('龙入角') },
  { label: '虎正念入', className: 'bd-side-left-top', item: getBaoDouItemByLabel('虎正念入') },
  { label: '虎正念出', className: 'bd-side-left-bottom', item: getBaoDouItemByLabel('虎正念出') },
  { label: '龙正念入', className: 'bd-side-right-top', item: getBaoDouItemByLabel('龙正念入') },
  { label: '龙正念出', className: 'bd-side-right-bottom', item: getBaoDouItemByLabel('龙正念出') },
  { label: '虎出角', className: 'bd-corner-bl', item: getBaoDouItemByLabel('虎出角') },
  { label: '出正念虎', className: 'bd-bottom-left', item: getBaoDouItemByLabel('出正念虎') },
  { label: '出正念龙', className: 'bd-bottom-right', item: getBaoDouItemByLabel('出正念龙') },
  { label: '龙出角', className: 'bd-corner-br', item: getBaoDouItemByLabel('龙出角') },
  { label: '入同', className: 'bd-green-top-left', item: getBaoDouItemByLabel('入同') },
  { label: '入串', className: 'bd-green-top-right', item: getBaoDouItemByLabel('入串') },
  { label: '虎同', className: 'bd-green-left-top', item: getBaoDouItemByLabel('虎同') },
  { label: '虎串', className: 'bd-green-left-bottom', item: getBaoDouItemByLabel('虎串') },
  { label: '龙同', className: 'bd-green-right-top', item: getBaoDouItemByLabel('龙同') },
  { label: '龙串', className: 'bd-green-right-bottom', item: getBaoDouItemByLabel('龙串') },
  { label: '出同', className: 'bd-green-bottom-left', item: getBaoDouItemByLabel('出同') },
  { label: '出串', className: 'bd-green-bottom-right', item: getBaoDouItemByLabel('出串') },
  { label: '入古', className: 'bd-pink-top', item: getBaoDouItemByLabel('入古') },
  { label: '虎古', className: 'bd-pink-left', item: getBaoDouItemByLabel('虎古') },
  { label: '龙古', className: 'bd-pink-right', item: getBaoDouItemByLabel('龙古') },
  { label: '出古', className: 'bd-pink-bottom', item: getBaoDouItemByLabel('出古') },
])

const baoDouCoreLabels = ['虎', '入', '出', '龙']

const getCellKey = (item: RacingBetItem) => item.key

const onCellClick = (item: RacingBetItem) => {
  if (item.disabled) return
  if (props.isSealed) return
  emit('toggle', getCellKey(item))
}

const getRacingNumber = (item: RacingBetItem) => item.value ?? Number(item.label)
</script>

<template>
  <div
    class="racing-two-side-panel"
    :class="{ 'racing-two-side-panel--position': mode === 'position' }"
  >
    <section v-if="showTopSumNumbers" class="racing-section">
      <div class="racing-section-title racing-section-title--first">冠、亚军和</div>
      <div class="racing-top-combo-grid">
        <div
          v-for="(row, rowIndex) in topSumCombinationRows"
          :key="`top-sum-row-${rowIndex}`"
          class="racing-top-combo-row"
        >
          <div
            v-for="item in row"
            :key="item.key"
            class="racing-cell racing-top-combo-cell"
            :class="{
              'racing-cell--selected': !item.disabled && isSelected(getCellKey(item)),
              'racing-cell--sealed': isSealed || item.disabled,
              'racing-cell--disabled': item.disabled,
            }"
            @click="onCellClick(item)"
          >
            <div class="racing-top-combo-label">{{ item.label }}</div>
            <div class="racing-odd">
              <b>{{ isSealed || item.disabled ? '--' : item.odd }}</b>
            </div>
            <div v-if="quickMode === 'normal' && !item.disabled" class="racing-input-wrap">
              <input
                :value="amounts[getCellKey(item)] ?? ''"
                class="racing-input"
                type="text"
                :disabled="isSealed"
                @input="updateAmount(amounts, getCellKey(item), ($event.target as HTMLInputElement).value)"
                @focus="$emit('ensure', getCellKey(item))"
                @click.stop
              />
            </div>
            <div v-else class="racing-input-wrap"></div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="showTopSumSection" class="racing-section">
      <div class="racing-section-title" :class="{ 'racing-section-title--first': mode === 'all' }">冠亚和两面</div>
      <div class="racing-top-sum-grid">
        <div
          v-for="item in RACING_TOP_SUM_ITEMS"
          :key="item.key"
          class="racing-cell racing-top-cell"
          :class="{
            'racing-cell--selected': isSelected(getCellKey(item)),
            'racing-cell--sealed': isSealed,
          }"
          @click="onCellClick(item)"
        >
          <div class="racing-label">{{ item.label }}</div>
          <div class="racing-odd">
            <b>{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal'" class="racing-input-wrap">
            <input
              :value="amounts[getCellKey(item)] ?? ''"
              class="racing-input"
              type="text"
              :disabled="isSealed"
              @input="updateAmount(amounts, getCellKey(item), ($event.target as HTMLInputElement).value)"
              @focus="$emit('ensure', getCellKey(item))"
              @click.stop
            />
          </div>
        </div>
      </div>
    </section>

    <section v-if="positionNumberGroups.length > 0" class="racing-section racing-position-section">
      <div class="racing-position-number-grid">
        <div
          v-for="(group, groupIndex) in positionNumberGroups"
          :key="group.key"
          class="racing-position-number-group"
          :class="{ 'racing-position-number-group--line-start': groupIndex === 0 || groupIndex === 5 }"
        >
          <div class="racing-rank-title">{{ group.title }}</div>
          <div
            v-for="item in group.items"
            :key="item.key"
            class="racing-cell racing-position-number-cell"
            :class="{
              'racing-cell--selected': isSelected(getCellKey(item)),
              'racing-cell--sealed': isSealed,
            }"
            @click="onCellClick(item)"
          >
            <div class="racing-rank-ball-wrap">
              <img
                class="racing-rank-ball"
                :src="getRacingBallSrc(getRacingNumber(item))"
                :alt="item.label"
              />
            </div>
            <div class="racing-odd">
              <b>{{ isSealed ? '--' : item.odd }}</b>
            </div>
            <div v-if="quickMode === 'normal'" class="racing-input-wrap">
              <input
                :value="amounts[getCellKey(item)] ?? ''"
                class="racing-input"
                type="text"
                :disabled="isSealed"
                @input="updateAmount(amounts, getCellKey(item), ($event.target as HTMLInputElement).value)"
                @focus="$emit('ensure', getCellKey(item))"
                @click.stop
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="isBaoDouMode" class="racing-section racing-baodou-section">
      <div class="racing-baodou-board">
        <span class="racing-baodou-line line-tl"></span>
        <span class="racing-baodou-line line-tr"></span>
        <span class="racing-baodou-line line-bl"></span>
        <span class="racing-baodou-line line-br"></span>
        <div
          v-for="cell in baoDouBoardCells"
          :key="cell.item.key"
          class="racing-baodou-board-cell"
          :class="[cell.className, {
            'racing-cell--selected': !isSealed && isSelected(getCellKey(cell.item)),
            'racing-cell--sealed': isSealed || cell.item.disabled,
          }]"
          @click="onCellClick(cell.item)"
        >
          <div class="racing-baodou-board-cell-content">
            <div class="racing-baodou-board-label">{{ cell.label }}</div>
            <div class="racing-odd">
              <b>{{ cell.item.disabled ? '--' : cell.item.odd }}</b>
            </div>
            <div v-if="quickMode === 'normal'" class="racing-input-wrap">
              <input
                :value="amounts[getCellKey(cell.item)] ?? ''"
                class="racing-input"
                type="text"
                :disabled="isSealed || cell.item.disabled"
                @input="updateAmount(amounts, getCellKey(cell.item), ($event.target as HTMLInputElement).value)"
                @focus="$emit('ensure', getCellKey(cell.item))"
                @click.stop
              />
            </div>
          </div>
        </div>
        <div class="racing-baodou-core">
          <span v-for="label in baoDouCoreLabels" :key="label">{{ label }}</span>
        </div>
      </div>
    </section>

    <RacingNiuNiuPanel
      v-if="isNiuNiuMode"
      :is-sealed="isSealed"
      :quick-mode="quickMode"
      :amounts="amounts"
      :is-selected="isSelected"
      :pre-draw-balls="preDrawBalls"
      @toggle="$emit('toggle', $event)"
      @ensure="$emit('ensure', $event)"
      @update:amounts="$emit('update:amounts', $event)"
    />

    <section
      v-for="(section, sectionIndex) in specialSections"
      :key="section.key"
      class="racing-section"
    >
      <div
        class="racing-section-title"
        :class="{ 'racing-section-title--first': sectionIndex === 0 }"
      >
        {{ section.title }}
      </div>
      <div class="racing-special-grid">
        <div
          v-for="item in section.items"
          :key="item.key"
          class="racing-cell racing-special-cell"
          :class="{
            'racing-cell--selected': isSelected(getCellKey(item)),
            'racing-cell--sealed': isSealed,
          }"
          @click="onCellClick(item)"
        >
          <div class="racing-special-label">{{ item.label }}</div>
          <div class="racing-odd">
            <b>{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal'" class="racing-input-wrap">
            <input
              :value="amounts[getCellKey(item)] ?? ''"
              class="racing-input"
              type="text"
              :disabled="isSealed"
              @input="updateAmount(amounts, getCellKey(item), ($event.target as HTMLInputElement).value)"
              @focus="$emit('ensure', getCellKey(item))"
              @click.stop
            />
          </div>
        </div>
      </div>
    </section>

    <section v-if="positionGroups.length > 0" class="racing-section">
      <div class="racing-rank-grid">
        <div
          v-for="(group, groupIndex) in positionGroups"
          :key="group.key"
          class="racing-rank-group"
          :class="{ 'racing-rank-group--line-start': groupIndex === 0 || groupIndex === 5 }"
        >
          <div class="racing-rank-title">{{ group.title }}</div>
          <div
            v-for="item in group.items"
            :key="item.key"
            class="racing-cell racing-rank-cell"
            :class="{
              'racing-cell--selected': isSelected(getCellKey(item)),
              'racing-cell--sealed': isSealed,
            }"
            @click="onCellClick(item)"
          >
            <div class="racing-label">{{ item.label }}</div>
            <div class="racing-odd">
              <b>{{ isSealed ? '--' : item.odd }}</b>
            </div>
            <div v-if="quickMode === 'normal'" class="racing-input-wrap">
              <input
                :value="amounts[getCellKey(item)] ?? ''"
                class="racing-input"
                type="text"
                :disabled="isSealed"
                @input="updateAmount(amounts, getCellKey(item), ($event.target as HTMLInputElement).value)"
                @focus="$emit('ensure', getCellKey(item))"
                @click.stop
              />
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped src="./RacingTwoSidePanel.css"></style>
<style scoped src="./RacingTwoSideBaoDou.css"></style>
