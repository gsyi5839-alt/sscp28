export type LotteryDataClientConfig = {
  infoPollInterval: number
  historyPollInterval: number
  fullHistoryPollInterval: number
  fastHistoryListSize: number
  initialFullHistoryDelay: number
  switchGameFullHistoryDelay: number
}

const DESKTOP_LOTTERY_DATA_CONFIG: LotteryDataClientConfig = {
  infoPollInterval: 4000,
  historyPollInterval: 5000,
  fullHistoryPollInterval: 60000,
  fastHistoryListSize: 120,
  initialFullHistoryDelay: 0,
  switchGameFullHistoryDelay: 0,
}

const MOBILE_LOTTERY_DATA_CONFIG: LotteryDataClientConfig = {
  infoPollInterval: 6000,
  historyPollInterval: 12000,
  fullHistoryPollInterval: 60000,
  fastHistoryListSize: 80,
  initialFullHistoryDelay: 8000,
  switchGameFullHistoryDelay: 6000,
}

export const getLotteryDataClientConfig = (isMobileClient: boolean): LotteryDataClientConfig => (
  isMobileClient ? MOBILE_LOTTERY_DATA_CONFIG : DESKTOP_LOTTERY_DATA_CONFIG
)
