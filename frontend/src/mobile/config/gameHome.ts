export const GAME_HOME_NOTICE_DELAY_MS = {
  desktop: 500,
  mobile: 1800,
} as const

export const getGameHomeNoticeDelay = (isMobileClient: boolean) => (
  isMobileClient ? GAME_HOME_NOTICE_DELAY_MS.mobile : GAME_HOME_NOTICE_DELAY_MS.desktop
)
