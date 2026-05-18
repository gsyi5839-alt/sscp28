export interface BettingSealStateInput {
  nowMs: number
  sealTimestamp: number
  drawTimestamp: number
  drawIssue: string
  pendingResultIssue?: string
  preDrawIssue?: string
}

export const isBettingSealed = ({
  nowMs,
  sealTimestamp,
  drawTimestamp,
  drawIssue,
  pendingResultIssue,
  preDrawIssue,
}: BettingSealStateInput): boolean => {
  if (pendingResultIssue && preDrawIssue !== pendingResultIssue) return true
  if (!drawIssue || sealTimestamp <= 0 || drawTimestamp <= 0) return false
  return nowMs >= sealTimestamp
}

export const resolvePendingResultIssue = (
  pendingResultIssue: string,
  preDrawIssue: string,
): string => {
  if (!pendingResultIssue) return ''
  return preDrawIssue === pendingResultIssue ? '' : pendingResultIssue
}
