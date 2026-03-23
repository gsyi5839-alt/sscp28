/**
 * Notice/announcement data
 */

export interface NoticeItem {
  id: number
  time: string
  content: string
  isHighlight?: boolean
}

export const noticeTabItems = [
  { key: '特别通知', label: '特别通知' },
  { key: '通知', label: '通知' },
  { key: '安全通知', label: '安全通知' },
  { key: '站点通知', label: '站点通知' }
]

export const noticeListData: Record<string, NoticeItem[]> = {
  '特别通知': [
    { id: 1, time: '2026-02-27 12:03', content: '尊敬的会员您好！值此马年新春之际，谨向一直以来支持与信赖我们的广大用户朋友致以衷心感谢和新春祝福！新的一年，我们将持续提升系统稳定性与服务效率，优化产品体验，为您提供更加安全、便捷、优质的服务保障。感谢您一直以来对本系统的支持！请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)', isHighlight: true },
    { id: 2, time: '2026-01-20 05:11', content: '尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)' },
    { id: 3, time: '2024-09-14 06:59', content: '尊敬的会员，您好！为了公平公正的原则，以及更好的游戏氛围及体验，本系统新添加官方游戏，加拿大PC28和加拿大时时彩，开奖数据由加拿大官方提供(https://lotto.bclc.com)同时每个游戏添加新玩法（宝斗，牛牛，斗牛）' }
  ],
  '通知': [
    { id: 4, time: '2026-02-15 10:00', content: '请各位会员注意保管好自己的账号密码，不要向任何人透露您的账户信息。' }
  ],
  '安全通知': [
    { id: 5, time: '2026-02-10 08:30', content: '为了保障您的账户安全，建议定期修改密码，并开启双重验证。' }
  ],
  '站点通知': [
    { id: 6, time: '2026-02-01 09:00', content: '欢迎访问本站，祝您使用愉快！如有任何问题请联系在线客服。' }
  ]
}

// Session storage key for notice dialog
export const NOTICE_SHOWN_KEY = 'bw-notice-shown-session'
