const GAME_MOBILE_CLIENT_PATTERN = /Mobi|Android|iPhone|iPad|iPod/i
const RESPONSIVE_MOBILE_CLIENT_PATTERN = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i

const getDefaultUserAgent = () => (
  typeof navigator === 'undefined' ? '' : navigator.userAgent
)

export const isGameMobileClient = (userAgent = getDefaultUserAgent()) => (
  GAME_MOBILE_CLIENT_PATTERN.test(userAgent)
)

export const isResponsiveMobileClient = (userAgent = getDefaultUserAgent()) => (
  RESPONSIVE_MOBILE_CLIENT_PATTERN.test(userAgent)
)
