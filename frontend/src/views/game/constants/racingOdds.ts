/**
 * Racing game two-side odds configuration.
 * Used by Australian Lucky 10 and other 10-ball racing games.
 */

export interface RacingBetItem {
  key: string
  label: string
  odd: string
  value?: number
  disabled?: boolean
}

export interface RacingPositionGroup {
  key: string
  title: string
  items: RacingBetItem[]
}

export const RACING_TOP_SUM_ITEMS: RacingBetItem[] = [
  { key: 'sum_big', label: '大', odd: '1.9' },
  { key: 'sum_small', label: '小', odd: '1.6' },
  { key: 'sum_odd', label: '单', odd: '1.6' },
  { key: 'sum_even', label: '双', odd: '1.9' },
]

const fullSideItems = (groupKey: string): RacingBetItem[] => [
  { key: `${groupKey}_big`, label: '大', odd: '1.9806' },
  { key: `${groupKey}_small`, label: '小', odd: '1.9806' },
  { key: `${groupKey}_odd`, label: '单', odd: '1.9806' },
  { key: `${groupKey}_even`, label: '双', odd: '1.9806' },
  { key: `${groupKey}_dragon`, label: '龙', odd: '1.9806' },
  { key: `${groupKey}_tiger`, label: '虎', odd: '1.9806' },
]

const rankSideItems = (
  groupKey: string,
  odds: Partial<Record<'big' | 'small' | 'odd' | 'even', string>> = {},
): RacingBetItem[] => [
  { key: `${groupKey}_big`, label: '大', odd: odds.big ?? '1.9806' },
  { key: `${groupKey}_small`, label: '小', odd: odds.small ?? '1.9806' },
  { key: `${groupKey}_odd`, label: '单', odd: odds.odd ?? '1.9806' },
  { key: `${groupKey}_even`, label: '双', odd: odds.even ?? '1.9806' },
]

export const RACING_POSITION_GROUPS: RacingPositionGroup[] = [
  { key: 'pos1', title: '冠军', items: fullSideItems('pos1') },
  { key: 'pos2', title: '亚军', items: fullSideItems('pos2') },
  { key: 'pos3', title: '第三名', items: fullSideItems('pos3') },
  { key: 'pos4', title: '第四名', items: fullSideItems('pos4') },
  { key: 'pos5', title: '第五名', items: fullSideItems('pos5').map(item => (
    item.key === 'pos5_even' ? { ...item, odd: '1.9606' } : item
  )) },
  { key: 'pos6', title: '第六名', items: rankSideItems('pos6', { big: '1.9606' }) },
  { key: 'pos7', title: '第七名', items: rankSideItems('pos7', { big: '1.9606', even: '1.9606' }) },
  { key: 'pos8', title: '第八名', items: rankSideItems('pos8', { big: '1.9606' }) },
  { key: 'pos9', title: '第九名', items: rankSideItems('pos9') },
  { key: 'pos10', title: '第十名', items: rankSideItems('pos10') },
]

export type RacingPanelMode = 'all' | 'topSum' | 'position' | 'baoDou' | 'niuNiu'

export interface RacingBetSection {
  key: string
  title: string
  items: RacingBetItem[]
}

const RACING_DEFAULT_NUMBER_ODDS = ['9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9']
const RACING_POSITION_NUMBER_ODDS: Record<string, string[]> = {
  pos1: ['9.9', '9.86', '9.88', '9.9', '9.9', '9.88', '9.9', '9.9', '9.9', '9.9'],
  pos2: ['9.9', '9.88', '9.9', '9.9', '9.88', '9.9', '9.9', '9.9', '9.9', '9.9'],
  pos3: ['9.9', '9.88', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9'],
  pos4: ['9.88', '9.86', '9.88', '9.9', '9.86', '9.9', '9.88', '9.88', '9.88', '9.9'],
  pos6: ['9.9', '9.88', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9'],
  pos7: ['9.9', '9.88', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9', '9.9'],
}

const createRacingNumberItems = (positionKey: string): RacingBetItem[] => {
  const odds = RACING_POSITION_NUMBER_ODDS[positionKey] ?? RACING_DEFAULT_NUMBER_ODDS
  return odds.map((odd, index) => {
    const value = index + 1
    return {
      key: `racing_${positionKey}_${value}`,
      label: String(value),
      odd,
      value,
    }
  })
}

export const RACING_POSITION_NUMBER_GROUPS: RacingBetSection[] = [
  { key: 'pos1', title: '冠军', items: createRacingNumberItems('pos1') },
  { key: 'pos2', title: '亚军', items: createRacingNumberItems('pos2') },
  { key: 'pos3', title: '第三名', items: createRacingNumberItems('pos3') },
  { key: 'pos4', title: '第四名', items: createRacingNumberItems('pos4') },
  { key: 'pos5', title: '第五名', items: createRacingNumberItems('pos5') },
  { key: 'pos6', title: '第六名', items: createRacingNumberItems('pos6') },
  { key: 'pos7', title: '第七名', items: createRacingNumberItems('pos7') },
  { key: 'pos8', title: '第八名', items: createRacingNumberItems('pos8') },
  { key: 'pos9', title: '第九名', items: createRacingNumberItems('pos9') },
  { key: 'pos10', title: '第十名', items: createRacingNumberItems('pos10') },
]

export const RACING_TOP_SUM_NUMBER_ITEMS: RacingBetItem[] = [
  { key: 'racing_top_sum_3', label: '3', odd: '39.88' },
  { key: 'racing_top_sum_4', label: '4', odd: '39.88' },
  { key: 'racing_top_sum_5', label: '5', odd: '19' },
  { key: 'racing_top_sum_6', label: '6', odd: '19' },
  { key: 'racing_top_sum_7', label: '7', odd: '12' },
  { key: 'racing_top_sum_8', label: '8', odd: '12' },
  { key: 'racing_top_sum_9', label: '9', odd: '9' },
  { key: 'racing_top_sum_10', label: '10', odd: '9' },
  { key: 'racing_top_sum_11', label: '11', odd: '8' },
  { key: 'racing_top_sum_12', label: '12', odd: '9' },
  { key: 'racing_top_sum_13', label: '13', odd: '9' },
  { key: 'racing_top_sum_14', label: '14', odd: '12' },
  { key: 'racing_top_sum_15', label: '15', odd: '12' },
  { key: 'racing_top_sum_16', label: '16', odd: '19' },
  { key: 'racing_top_sum_17', label: '17', odd: '19' },
  { key: 'racing_top_sum_18', label: '18', odd: '39.88' },
  { key: 'racing_top_sum_19', label: '19', odd: '39.88' },
]

export const RACING_BAODOU_SECTIONS: RacingBetSection[] = [
  {
    key: 'baoDouOld',
    title: '宝斗-古',
    items: [
      { key: 'bd_old_in', label: '入古', odd: '3.94' },
      { key: 'bd_old_dragon', label: '龙古', odd: '3.94' },
      { key: 'bd_old_out', label: '出古', odd: '3.94' },
      { key: 'bd_old_tiger', label: '虎古', odd: '3.94' },
    ],
  },
  {
    key: 'baoDouSame',
    title: '宝斗-同',
    items: [
      { key: 'bd_same_in', label: '入同', odd: '2.95' },
      { key: 'bd_same_dragon', label: '龙同', odd: '2.95' },
      { key: 'bd_same_out', label: '出同', odd: '2.95' },
      { key: 'bd_same_tiger', label: '虎同', odd: '2.95' },
    ],
  },
  {
    key: 'baoDouChain',
    title: '宝斗-串',
    items: [
      { key: 'bd_chain_in', label: '入串', odd: '1.97' },
      { key: 'bd_chain_dragon', label: '龙串', odd: '1.97' },
      { key: 'bd_chain_out', label: '出串', odd: '1.97' },
      { key: 'bd_chain_tiger', label: '虎串', odd: '1.97' },
    ],
  },
  {
    key: 'baoDouCorner',
    title: '宝斗-角',
    items: [
      { key: 'bd_corner_dragon_in', label: '龙入角', odd: '1.97' },
      { key: 'bd_corner_dragon_out', label: '龙出角', odd: '1.97' },
      { key: 'bd_corner_tiger_out', label: '虎出角', odd: '1.97' },
      { key: 'bd_corner_tiger_in', label: '虎入角', odd: '1.97' },
    ],
  },
  {
    key: 'baoDouRead',
    title: '宝斗-念',
    items: [
      { key: 'bd_read_dragon_in', label: '龙正念入', odd: '2.95' },
      { key: 'bd_read_dragon_out', label: '龙正念出', odd: '2.95' },
      { key: 'bd_read_out_dragon', label: '出正念龙', odd: '2.95' },
      { key: 'bd_read_out_tiger', label: '出正念虎', odd: '2.95' },
      { key: 'bd_read_tiger_out', label: '虎正念出', odd: '2.95' },
      { key: 'bd_read_tiger_in', label: '虎正念入', odd: '2.95' },
      { key: 'bd_read_in_tiger', label: '入正念虎', odd: '2.95' },
      { key: 'bd_read_in_dragon', label: '入正念龙', odd: '2.95' },
    ],
  },
]

export const RACING_NIUNIU_SECTIONS: RacingBetSection[] = [
  {
    key: 'niuNiuDouble',
    title: '牛牛 [翻倍]',
    items: ['闲一', '闲二', '闲三', '闲四', '闲五'].map(label => ({
      key: `racing_nn_double_${label}`,
      label,
      odd: '1.97',
    })),
  },
  {
    key: 'niuNiuFlat',
    title: '牛牛 [平倍]',
    items: ['闲一', '闲二', '闲三', '闲四', '闲五'].map(label => ({
      key: `racing_nn_flat_${label}`,
      label,
      odd: '1.97',
    })),
  },
]
