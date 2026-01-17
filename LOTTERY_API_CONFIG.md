# 彩票开奖数据API接口配置

> 基于 bw1284.cc 平台的开奖数据接口配置
> 更新日期: 2026-01-17

---

## 一、API基础信息

### 1.1 接口域名
```
Base URL: https://bw1284.cc/api
```

### 1.2 通用接口

| 接口名称 | 接口地址 | 方法 | 说明 |
|---------|---------|------|------|
| 获取所有彩种 | `/lottery_code/allLottery` | GET | 返回所有彩种列表 |
| 获取开奖列表 | `/lottery_code/getLotteryList` | GET | 分页获取开奖记录 |
| 获取彩种信息 | `/lottery_code/getLotteryInfo` | GET | 获取单个彩种详情 |

---

## 二、12个彩种配置

### 2.1 彩种与lotCode对照表

| 序号 | 彩种名称 | lotCode | 开奖页面URL |
|------|---------|---------|------------|
| 1 | 加拿大PC28 | 720 | https://bw1284.cc/#/detail/1768606664171?lotCode=720 |
| 2 | 加拿大时时彩 | 719 | https://bw1284.cc/#/detail/1768606681555?lotCode=719 |
| 3 | 澳洲幸运10 | 797 | https://bw1284.cc/#/detail/1768606689644?lotCode=797 |
| 4 | 澳洲幸运5 | 795 | https://bw1284.cc/#/detail/1768606695411?lotCode=795 |
| 5 | 欢乐赛车 | 763 | https://bw1284.cc/#/detail/1768606704531?lotCode=763 |
| 6 | 欢乐时时彩 | 762 | https://bw1284.cc/#/detail/1768606714147?lotCode=762 |
| 7 | 幸运飞艇 | 765 | https://bw1284.cc/#/detail/1768606722939?lotCode=765 |
| 8 | 极速赛车 | 768 | https://bw1284.cc/#/detail/1768606739626?lotCode=768 |
| 9 | 极速时时彩 | 769 | https://bw1284.cc/#/detail/1768606748595?lotCode=769 |
| 10 | 168幸运飞艇 | 726 | https://bw1284.cc/#/detail/1768606758314?lotCode=726 |
| 11 | 体彩乐透5 | 766 | https://bw1284.cc/#/detail/1768606772834?lotCode=766 |
| 12 | 体彩乐透10 | 767 | https://bw1284.cc/#/detail/1768606777410?lotCode=767 |

### 2.2 开奖时间配置表 ⏱️

| 彩种名称 | lotCode | 开奖间隔 | 开盘时间 | 封盘时间 | 每日期数 | 营业时段 |
|---------|---------|---------|---------|---------|---------|---------|
| 加拿大PC28 | 720 | **3分30秒** | 00:00:00 | 23:59:59 | ~411期 | 24小时 |
| 加拿大时时彩 | 719 | **3分30秒** | 00:00:00 | 23:59:59 | ~411期 | 24小时 |
| 澳洲幸运10 | 797 | **5分钟** | 04:00:00 | 次日04:00 | ~288期 | 24小时 |
| 澳洲幸运5 | 795 | **5分钟** | 04:00:00 | 次日04:00 | ~288期 | 24小时 |
| 欢乐赛车 | 763 | **3分钟** | 09:03:00 | 次日04:03 | ~381期 | 19小时 |
| 欢乐时时彩 | 762 | **3分钟** | 09:03:00 | 次日04:03 | ~381期 | 19小时 |
| 幸运飞艇 | 765 | **5分钟** | 13:09:00 | 次日04:04 | ~180期 | 15小时 |
| 极速赛车 | 768 | **1分钟** | 09:03:00 | 次日04:00 | ~1138期 | 19小时 |
| 极速时时彩 | 769 | **1分钟** | 09:03:00 | 次日04:00 | ~1138期 | 19小时 |
| 168幸运飞艇 | 726 | **5分钟** | 13:08:45 | 次日04:03 | ~180期 | 15小时 |
| 体彩乐透5 | 766 | **5分钟** | 09:05:00 | 次日04:05 | ~229期 | 19小时 |
| 体彩乐透10 | 767 | **5分钟** | 09:05:00 | 次日04:05 | ~229期 | 19小时 |

### 2.3 开奖间隔速查 (秒)

```
720 加拿大PC28    = 210秒 (3分30秒)
719 加拿大时时彩   = 210秒 (3分30秒)
797 澳洲幸运10    = 300秒 (5分钟)
795 澳洲幸运5     = 300秒 (5分钟)
763 欢乐赛车      = 180秒 (3分钟)
762 欢乐时时彩    = 180秒 (3分钟)
765 幸运飞艇      = 300秒 (5分钟)
768 极速赛车      = 60秒  (1分钟)
769 极速时时彩    = 60秒  (1分钟)
726 168幸运飞艇   = 300秒 (5分钟)
766 体彩乐透5     = 300秒 (5分钟)
767 体彩乐透10    = 300秒 (5分钟)
```

---

## 三、API接口详情

### 3.1 获取开奖列表

**请求URL:**
```
GET https://bw1284.cc/api/lottery_code/getLotteryList
```

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|-------|------|------|------|------|
| lotCode | Integer | 是 | 彩种代码 | 720 |
| pageSize | Integer | 否 | 每页条数(默认100) | 100 |
| pageNo | Integer | 否 | 页码(默认1) | 1 |
| date | String | 否 | 指定日期 | 2026-01-17 |

**请求示例:**
```bash
# 加拿大PC28 - 今日开奖
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=720&pageSize=100&pageNo=1&date=2026-01-17"

# 极速赛车 - 昨日开奖
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=768&pageSize=100&pageNo=1&date=2026-01-16"
```

### 3.2 获取彩种信息

**请求URL:**
```
GET https://bw1284.cc/api/lottery_code/getLotteryInfo
```

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|-------|------|------|------|------|
| lotCode | Integer | 是 | 彩种代码 | 720 |

**请求示例:**
```bash
# 获取加拿大PC28详情
curl "https://bw1284.cc/api/lottery_code/getLotteryInfo?lotCode=720"
```

### 3.3 获取所有彩种

**请求URL:**
```
GET https://bw1284.cc/api/lottery_code/allLottery
```

**请求示例:**
```bash
curl "https://bw1284.cc/api/lottery_code/allLottery"
```

---

## 四、API返回格式

### 4.1 通用响应结构

```json
{
  "code": 0,           // 0=成功
  "version": "0.01",   // API版本
  "data": {
    "list": [...],     // 开奖记录列表
    "total": 430       // 总记录数
  },
  "msg": ""            // 错误消息
}
```

### 4.2 PC28类彩种返回格式 (3个号码)

**适用彩种**: 加拿大PC28 (lotCode=720)

```json
{
  "preDrawCode": "6,3,7",              // 开奖号码(3个)
  "preDrawIssue": "3385247",           // 期号
  "preDrawTime": "2026-01-17 07:41:30", // 开奖时间
  "serviceTime": "2026-01-17 07:41:46", // 服务器时间
  "attr1": "16",                       // 总和(6+3+7=16)
  "attr2": "大",                       // 大小(0-13小, 14-27大)
  "attr3": "双",                       // 单双
  "attr4": "大双",                     // 大小+单双组合
  "attr5": "半顺",                     // 特殊属性(豹子/顺子/对子/半顺/杂六)
  "attr6": null,
  "attr7": null,
  "attr8": null
}
```

### 4.3 时时彩类彩种返回格式 (5个号码)

**适用彩种**: 加拿大时时彩(719), 澳洲幸运5(795), 欢乐时时彩(762), 极速时时彩(769), 体彩乐透5(766)

```json
{
  "preDrawCode": "3,8,2,5,9",           // 开奖号码(5个)
  "preDrawIssue": "202601170001",        // 期号
  "preDrawTime": "2026-01-17 00:00:00",  // 开奖时间
  "serviceTime": "2026-01-17 00:00:10",  // 服务器时间
  "attr1": "27",                         // 总和
  "attr2": "大",                         // 大小
  "attr3": "单",                         // 单双
  "attr4": "龙",                         // 龙虎(首尾比较)
  "attr5": null,
  "attr6": null,
  "attr7": null,
  "attr8": null
}
```

### 4.4 赛车/飞艇类彩种返回格式 (10个号码)

**适用彩种**: 澳洲幸运10(797), 欢乐赛车(763), 幸运飞艇(765), 极速赛车(768), 168幸运飞艇(726), 体彩乐透10(767)

```json
{
  "preDrawCode": "8,9,4,3,10,1,5,6,2,7", // 开奖号码(10个,表示1-10名次)
  "preDrawIssue": "102601161138",        // 期号
  "preDrawTime": "2026-01-17 04:00:00",  // 开奖时间
  "serviceTime": "2026-01-17 07:41:54",  // 服务器时间
  "attr1": "17",                         // 冠亚和(第1名+第2名)
  "attr2": "大",                         // 冠亚和大小(3-11小, 12-19大)
  "attr3": "单",                         // 冠亚和单双
  "attr4": "龙",                         // 1vs10龙虎
  "attr5": "龙",                         // 2vs9龙虎
  "attr6": "虎",                         // 3vs8龙虎
  "attr7": "虎",                         // 4vs7龙虎
  "attr8": "龙"                          // 5vs6龙虎
}
```

### 4.5 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| preDrawCode | 开奖号码(逗号分隔) | "6,3,7" 或 "8,9,4,3,10,1,5,6,2,7" |
| preDrawIssue | 期号 | "3385247" |
| preDrawTime | 开奖时间 | "2026-01-17 07:41:30" |
| serviceTime | 服务器时间 | "2026-01-17 07:41:46" |
| attr1 | 总和/冠亚和 | "16" |
| attr2 | 大小 | "大" / "小" |
| attr3 | 单双 | "单" / "双" |
| attr4 | 组合/龙虎 | PC28: "大双" / 赛车: "龙"/"虎" |
| attr5 | 特殊属性/龙虎 | PC28: "豹子"等 / 赛车: 2vs9龙虎 |
| attr6-8 | 龙虎(仅赛车类) | "龙" / "虎" |

---

## 五、Java代码实现

### 5.1 彩种枚举类 (含开奖时间配置)

```java
package com.bcbbs.lottery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalTime;

/**
 * 彩种枚举 - 12个彩种完整配置
 */
@Getter
@AllArgsConstructor
public enum LotteryTypeEnum {
    
    // 加拿大系列 (24小时, 3分30秒 = 210秒)
    CANADA_PC28(720, "加拿大PC28", "canada_pc28", 3, 210, 
            LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59), true),
    CANADA_SSC(719, "加拿大时时彩", "canada_ssc", 5, 210, 
            LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59), true),
    
    // 澳洲系列 (24小时, 5分钟 = 300秒)
    AUSSIE_10(797, "澳洲幸运10", "aussie_10", 10, 300, 
            LocalTime.of(4, 0, 0), LocalTime.of(3, 59, 59), true),
    AUSSIE_5(795, "澳洲幸运5", "aussie_5", 5, 300, 
            LocalTime.of(4, 0, 0), LocalTime.of(3, 59, 59), true),
    
    // 欢乐系列 (09:03-04:03, 3分钟 = 180秒)
    HAPPY_RACING(763, "欢乐赛车", "happy_racing", 10, 180, 
            LocalTime.of(9, 3, 0), LocalTime.of(4, 3, 0), false),
    HAPPY_SSC(762, "欢乐时时彩", "happy_ssc", 5, 180, 
            LocalTime.of(9, 3, 0), LocalTime.of(4, 3, 0), false),
    
    // 幸运飞艇 (13:09-04:04, 5分钟 = 300秒)
    LUCKY_AIRSHIP(765, "幸运飞艇", "lucky_airship", 10, 300, 
            LocalTime.of(13, 9, 0), LocalTime.of(4, 4, 0), false),
    
    // 极速系列 (09:03-04:00, 1分钟 = 60秒) ⚡高频
    SPEED_RACING(768, "极速赛车", "speed_racing", 10, 60, 
            LocalTime.of(9, 3, 0), LocalTime.of(4, 0, 0), false),
    SPEED_SSC(769, "极速时时彩", "speed_ssc", 5, 60, 
            LocalTime.of(9, 3, 0), LocalTime.of(4, 0, 0), false),
    
    // 168幸运飞艇 (13:08:45-04:03:45, 5分钟 = 300秒)
    AIRSHIP_168(726, "168幸运飞艇", "airship_168", 10, 300, 
            LocalTime.of(13, 8, 45), LocalTime.of(4, 3, 45), false),
    
    // 体彩乐透系列 (09:05-04:05, 5分钟 = 300秒)
    LOTTERY_5(766, "体彩乐透5", "lottery_5", 5, 300, 
            LocalTime.of(9, 5, 0), LocalTime.of(4, 5, 0), false),
    LOTTERY_10(767, "体彩乐透10", "lottery_10", 10, 300, 
            LocalTime.of(9, 5, 0), LocalTime.of(4, 5, 0), false);
    
    /** 彩种代码 - 对应API的lotCode */
    private final Integer lotCode;
    /** 彩种中文名称 */
    private final String name;
    /** 系统内部代码 */
    private final String code;
    /** 开奖号码数量 */
    private final Integer numberCount;
    /** 开奖间隔(秒) */
    private final Integer intervalSeconds;
    /** 每日开盘时间 */
    private final LocalTime openTime;
    /** 每日封盘时间 */
    private final LocalTime closeTime;
    /** 是否24小时运营 */
    private final Boolean is24Hours;
    
    /** 根据lotCode获取枚举 */
    public static LotteryTypeEnum getByLotCode(Integer lotCode) {
        for (LotteryTypeEnum type : values()) {
            if (type.getLotCode().equals(lotCode)) {
                return type;
            }
        }
        return null;
    }
    
    /** 获取格式化的开奖间隔 */
    public String getIntervalFormatted() {
        int seconds = this.intervalSeconds;
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int remainSeconds = seconds % 60;
            return remainSeconds > 0 
                ? String.format("%d分%d秒", minutes, remainSeconds)
                : String.format("%d分钟", minutes);
        }
        return seconds + "秒";
    }
}
```

### 5.2 倒计时服务类

```java
package com.bcbbs.lottery.service;

import com.bcbbs.lottery.enums.LotteryTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 开奖倒计时服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryCountdownService {
    
    private final ExternalLotteryApiService apiService;
    
    /**
     * 获取彩种倒计时信息
     */
    public CountdownInfo getCountdown(LotteryTypeEnum lotteryType) {
        JsonNode info = apiService.getLotteryInfo(lotteryType);
        if (info == null || info.get("data") == null) {
            return null;
        }
        
        JsonNode data = info.get("data");
        
        CountdownInfo countdown = new CountdownInfo();
        countdown.setLotCode(lotteryType.getLotCode());
        countdown.setLotName(lotteryType.getName());
        countdown.setCurrentIssue(data.get("drawIssue").asText());
        countdown.setLastIssue(data.get("preDrawIssue").asText());
        countdown.setLastDrawNumbers(data.get("preDrawCode").asText());
        countdown.setIntervalSeconds(lotteryType.getIntervalSeconds());
        countdown.setIntervalFormatted(lotteryType.getIntervalFormatted());
        
        // 解析下期开奖时间
        String drawTimeStr = data.get("drawTime").asText();
        LocalDateTime drawTime = LocalDateTime.parse(drawTimeStr, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        countdown.setNextDrawTime(drawTimeStr);
        
        // 解析服务器时间
        String serverTimeStr = data.get("serviceTime").asText();
        LocalDateTime serverTime = LocalDateTime.parse(serverTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 计算倒计时(秒)
        long remainingSeconds = Duration.between(serverTime, drawTime).getSeconds();
        countdown.setRemainingSeconds(Math.max(0, remainingSeconds));
        countdown.setRemainingFormatted(formatSeconds(remainingSeconds));
        
        // 是否在营业时间
        countdown.setIsOpen(isInOperationTime(lotteryType, serverTime.toLocalTime()));
        
        return countdown;
    }
    
    /**
     * 判断是否在营业时间内
     */
    private boolean isInOperationTime(LotteryTypeEnum type, LocalTime currentTime) {
        if (type.getIs24Hours()) {
            return true;
        }
        
        LocalTime open = type.getOpenTime();
        LocalTime close = type.getCloseTime();
        
        // 跨天情况 (如 09:03 - 04:00)
        if (close.isBefore(open)) {
            return currentTime.isAfter(open) || currentTime.isBefore(close);
        }
        return currentTime.isAfter(open) && currentTime.isBefore(close);
    }
    
    /**
     * 格式化秒数为 mm:ss 或 hh:mm:ss
     */
    private String formatSeconds(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "00:00";
        }
        
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * 倒计时信息DTO
     */
    @lombok.Data
    public static class CountdownInfo {
        private Integer lotCode;           // 彩种代码
        private String lotName;            // 彩种名称
        private String currentIssue;       // 当前期号
        private String lastIssue;          // 上期期号
        private String lastDrawNumbers;    // 上期开奖号码
        private String nextDrawTime;       // 下期开奖时间
        private Long remainingSeconds;     // 剩余秒数
        private String remainingFormatted; // 格式化倒计时 (mm:ss)
        private Integer intervalSeconds;   // 开奖间隔(秒)
        private String intervalFormatted;  // 格式化间隔 (3分30秒)
        private Boolean isOpen;            // 是否营业中
    }
}
```

### 5.3 API服务类

```java
package com.bcbbs.lottery.service;

import com.bcbbs.lottery.enums.LotteryTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 外部彩票API服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalLotteryApiService {
    
    private static final String BASE_URL = "https://bw1284.cc/api";
    private static final String GET_LIST_URL = BASE_URL + "/lottery_code/getLotteryList";
    private static final String GET_INFO_URL = BASE_URL + "/lottery_code/getLotteryInfo";
    private static final String ALL_LOTTERY_URL = BASE_URL + "/lottery_code/allLottery";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取指定彩种的开奖列表
     * @param lotteryType 彩种枚举
     * @param date 日期(可为null,默认今天)
     * @param pageNo 页码
     * @param pageSize 每页条数
     */
    public JsonNode getLotteryList(LotteryTypeEnum lotteryType, LocalDate date, int pageNo, int pageSize) {
        String dateStr = date != null ? date.format(DateTimeFormatter.ISO_DATE) : LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        String url = String.format("%s?lotCode=%d&pageSize=%d&pageNo=%d&date=%s",
                GET_LIST_URL, lotteryType.getLotCode(), pageSize, pageNo, dateStr);
        
        log.info("请求开奖列表: {}", url);
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("获取开奖列表失败: lotCode={}, error={}", lotteryType.getLotCode(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取彩种信息
     * @param lotteryType 彩种枚举
     */
    public JsonNode getLotteryInfo(LotteryTypeEnum lotteryType) {
        String url = String.format("%s?lotCode=%d", GET_INFO_URL, lotteryType.getLotCode());
        
        log.info("请求彩种信息: {}", url);
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("获取彩种信息失败: lotCode={}, error={}", lotteryType.getLotCode(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 批量获取所有12个彩种的最新开奖
     */
    public List<JsonNode> getAllLatestDraws() {
        List<JsonNode> results = new ArrayList<>();
        
        for (LotteryTypeEnum type : LotteryTypeEnum.values()) {
            JsonNode result = getLotteryList(type, LocalDate.now(), 1, 1);
            if (result != null) {
                results.add(result);
            }
        }
        
        return results;
    }
}
```

### 5.4 定时任务配置

```java
package com.bcbbs.lottery.scheduler;

import com.bcbbs.lottery.enums.LotteryTypeEnum;
import com.bcbbs.lottery.service.ExternalLotteryApiService;
import com.bcbbs.lottery.service.LotteryDrawResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 开奖数据轮询任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryDrawScheduler {
    
    private final ExternalLotteryApiService apiService;
    private final LotteryDrawResultService drawResultService;
    
    /**
     * 每30秒轮询高频彩种
     * 极速赛车(768)、极速时时彩(769)、欢乐赛车(763)、欢乐时时彩(762)、澳洲幸运10(797)
     */
    @Scheduled(fixedRate = 30000)
    public void pollHighFrequencyLotteries() {
        log.debug("开始轮询高频彩种...");
        
        LotteryTypeEnum[] highFrequency = {
            LotteryTypeEnum.SPEED_RACING,      // 768
            LotteryTypeEnum.SPEED_SSC,         // 769
            LotteryTypeEnum.HAPPY_RACING,      // 763
            LotteryTypeEnum.HAPPY_SSC,         // 762
            LotteryTypeEnum.AUSSIE_10          // 797
        };
        
        for (LotteryTypeEnum type : highFrequency) {
            try {
                drawResultService.fetchAndSaveLatestDraw(type);
            } catch (Exception e) {
                log.error("轮询彩种失败: {}, error={}", type.getName(), e.getMessage());
            }
        }
    }
    
    /**
     * 每60秒轮询中频彩种
     * 168幸运飞艇(726)、加拿大PC28(720)、加拿大时时彩(719)、澳洲幸运5(795)
     */
    @Scheduled(fixedRate = 60000)
    public void pollMediumFrequencyLotteries() {
        log.debug("开始轮询中频彩种...");
        
        LotteryTypeEnum[] mediumFrequency = {
            LotteryTypeEnum.AIRSHIP_168,       // 726
            LotteryTypeEnum.CANADA_PC28,       // 720
            LotteryTypeEnum.CANADA_SSC,        // 719
            LotteryTypeEnum.AUSSIE_5           // 795
        };
        
        for (LotteryTypeEnum type : mediumFrequency) {
            try {
                drawResultService.fetchAndSaveLatestDraw(type);
            } catch (Exception e) {
                log.error("轮询彩种失败: {}, error={}", type.getName(), e.getMessage());
            }
        }
    }
    
    /**
     * 每5分钟轮询低频彩种
     * 幸运飞艇(765)、体彩乐透5(766)、体彩乐透10(767)
     */
    @Scheduled(fixedRate = 300000)
    public void pollLowFrequencyLotteries() {
        log.debug("开始轮询低频彩种...");
        
        LotteryTypeEnum[] lowFrequency = {
            LotteryTypeEnum.LUCKY_AIRSHIP,     // 765
            LotteryTypeEnum.LOTTERY_5,         // 766
            LotteryTypeEnum.LOTTERY_10         // 767
        };
        
        for (LotteryTypeEnum type : lowFrequency) {
            try {
                drawResultService.fetchAndSaveLatestDraw(type);
            } catch (Exception e) {
                log.error("轮询彩种失败: {}, error={}", type.getName(), e.getMessage());
            }
        }
    }
}
```

### 5.5 前端Vue3倒计时组件

```vue
<template>
  <div class="lottery-countdown-grid">
    <div 
      v-for="lottery in lotteryList" 
      :key="lottery.lotCode"
      class="lottery-card"
      :class="{ 'is-closed': !lottery.isOpen }"
    >
      <div class="lottery-header">
        <span class="lottery-name">{{ lottery.lotName }}</span>
        <span class="lottery-interval">{{ lottery.intervalFormatted }}</span>
      </div>
      
      <div class="lottery-issue">
        <span class="label">第</span>
        <span class="issue-no">{{ lottery.currentIssue }}</span>
        <span class="label">期</span>
      </div>
      
      <div class="countdown-display">
        <span class="countdown-time" :class="{ 'urgent': lottery.remainingSeconds < 30 }">
          {{ lottery.remainingFormatted }}
        </span>
      </div>
      
      <div class="last-draw">
        <span class="label">上期:</span>
        <div class="draw-numbers">
          <span 
            v-for="(num, idx) in lottery.lastDrawNumbers.split(',')" 
            :key="idx"
            class="number-ball"
          >
            {{ num }}
          </span>
        </div>
      </div>
      
      <div class="status-bar" :class="lottery.isOpen ? 'open' : 'closed'">
        {{ lottery.isOpen ? '● 开盘中' : '○ 已封盘' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

interface LotteryCountdown {
  lotCode: number
  lotName: string
  currentIssue: string
  lastIssue: string
  lastDrawNumbers: string
  nextDrawTime: string
  remainingSeconds: number
  remainingFormatted: string
  intervalSeconds: number
  intervalFormatted: string
  isOpen: boolean
}

// 12个彩种配置
const LOTTERY_CODES = [720, 719, 797, 795, 763, 762, 765, 768, 769, 726, 766, 767]

const lotteryList = ref<LotteryCountdown[]>([])
let timer: number | null = null
let refreshTimer: number | null = null

// 格式化秒数
const formatSeconds = (seconds: number): string => {
  if (seconds <= 0) return '00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 获取倒计时数据
const fetchCountdowns = async () => {
  try {
    const response = await axios.get('/api/lottery/countdown/all')
    lotteryList.value = response.data.data
  } catch (error) {
    console.error('获取倒计时失败:', error)
  }
}

// 本地倒计时更新
const updateCountdowns = () => {
  lotteryList.value.forEach(lottery => {
    if (lottery.remainingSeconds > 0) {
      lottery.remainingSeconds--
      lottery.remainingFormatted = formatSeconds(lottery.remainingSeconds)
    }
  })
}

onMounted(() => {
  fetchCountdowns()
  // 每秒更新倒计时
  timer = window.setInterval(updateCountdowns, 1000)
  // 每30秒从服务器刷新
  refreshTimer = window.setInterval(fetchCountdowns, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.lottery-countdown-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  padding: 16px;
}

.lottery-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 12px;
  padding: 16px;
  color: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.lottery-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
}

.lottery-card.is-closed {
  opacity: 0.6;
}

.lottery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.lottery-name {
  font-size: 18px;
  font-weight: bold;
  color: #00d4ff;
}

.lottery-interval {
  font-size: 12px;
  color: #888;
  background: rgba(255,255,255,0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.lottery-issue {
  text-align: center;
  margin-bottom: 8px;
  color: #aaa;
}

.issue-no {
  font-size: 16px;
  color: #fff;
  margin: 0 4px;
}

.countdown-display {
  text-align: center;
  margin: 16px 0;
}

.countdown-time {
  font-size: 36px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  color: #4ade80;
  text-shadow: 0 0 10px rgba(74, 222, 128, 0.5);
}

.countdown-time.urgent {
  color: #ef4444;
  animation: blink 0.5s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.last-draw {
  margin-top: 12px;
}

.last-draw .label {
  font-size: 12px;
  color: #888;
}

.draw-numbers {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
}

.number-ball {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}

.status-bar {
  margin-top: 12px;
  padding: 6px;
  border-radius: 6px;
  text-align: center;
  font-size: 12px;
}

.status-bar.open {
  background: rgba(74, 222, 128, 0.2);
  color: #4ade80;
}

.status-bar.closed {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}
</style>
```

### 5.6 REST API 接口

```java
package com.bcbbs.lottery.controller;

import com.bcbbs.lottery.enums.LotteryTypeEnum;
import com.bcbbs.lottery.service.LotteryCountdownService;
import com.bcbbs.lottery.service.LotteryCountdownService.CountdownInfo;
import com.bcbbs.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/lottery/countdown")
@RequiredArgsConstructor
public class LotteryCountdownController {
    
    private final LotteryCountdownService countdownService;
    
    /**
     * 获取所有12个彩种的倒计时
     */
    @GetMapping("/all")
    public ApiResponse<List<CountdownInfo>> getAllCountdowns() {
        List<CountdownInfo> list = new ArrayList<>();
        for (LotteryTypeEnum type : LotteryTypeEnum.values()) {
            CountdownInfo info = countdownService.getCountdown(type);
            if (info != null) {
                list.add(info);
            }
        }
        return ApiResponse.success(list);
    }
    
    /**
     * 获取单个彩种的倒计时
     */
    @GetMapping("/{lotCode}")
    public ApiResponse<CountdownInfo> getCountdown(@PathVariable Integer lotCode) {
        LotteryTypeEnum type = LotteryTypeEnum.getByLotCode(lotCode);
        if (type == null) {
            return ApiResponse.fail("无效的彩种代码");
        }
        return ApiResponse.success(countdownService.getCountdown(type));
    }
}
```

---

## 六、数据库配置

### 6.1 外部API配置表 (含开奖时间)

```sql
-- 外部API配置表 (12个彩种完整配置)
CREATE TABLE `external_api_configs` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `lottery_code` VARCHAR(50) NOT NULL COMMENT '系统内部彩种代码',
    `lottery_name` VARCHAR(50) NOT NULL COMMENT '彩种名称',
    `lot_code` INT NOT NULL COMMENT '外部API的lotCode',
    `api_url` VARCHAR(255) DEFAULT 'https://bw1284.cc/api/lottery_code/getLotteryList' COMMENT 'API地址',
    `interval_seconds` INT NOT NULL COMMENT '开奖间隔(秒)',
    `poll_interval` INT DEFAULT 60 COMMENT '轮询间隔(秒)',
    `open_time` TIME NOT NULL COMMENT '每日开盘时间',
    `close_time` TIME NOT NULL COMMENT '每日封盘时间',
    `is_24hours` TINYINT(1) DEFAULT 0 COMMENT '是否24小时运营',
    `daily_issues` INT DEFAULT 0 COMMENT '每日期数',
    `number_count` INT NOT NULL COMMENT '开奖号码数量',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_lottery_code` (`lottery_code`),
    UNIQUE KEY `uk_lot_code` (`lot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部彩票API配置表';

-- 初始化12个彩种配置 (含开奖时间)
INSERT INTO `external_api_configs` 
(`lottery_code`, `lottery_name`, `lot_code`, `interval_seconds`, `poll_interval`, `open_time`, `close_time`, `is_24hours`, `daily_issues`, `number_count`) 
VALUES
('canada_pc28',    '加拿大PC28',    720, 210, 60,  '00:00:00', '23:59:59', 1, 411,  3),
('canada_ssc',     '加拿大时时彩',   719, 210, 60,  '00:00:00', '23:59:59', 1, 411,  5),
('aussie_10',      '澳洲幸运10',    797, 300, 30,  '04:00:00', '03:59:59', 1, 288,  10),
('aussie_5',       '澳洲幸运5',     795, 300, 60,  '04:00:00', '03:59:59', 1, 288,  5),
('happy_racing',   '欢乐赛车',      763, 180, 30,  '09:03:00', '04:03:00', 0, 381,  10),
('happy_ssc',      '欢乐时时彩',    762, 180, 30,  '09:03:00', '04:03:00', 0, 381,  5),
('lucky_airship',  '幸运飞艇',      765, 300, 60,  '13:09:00', '04:04:00', 0, 180,  10),
('speed_racing',   '极速赛车',      768, 60,  30,  '09:03:00', '04:00:00', 0, 1138, 10),
('speed_ssc',      '极速时时彩',    769, 60,  30,  '09:03:00', '04:00:00', 0, 1138, 5),
('airship_168',    '168幸运飞艇',   726, 300, 60,  '13:08:45', '04:03:45', 0, 180,  10),
('lottery_5',      '体彩乐透5',     766, 300, 60,  '09:05:00', '04:05:00', 0, 229,  5),
('lottery_10',     '体彩乐透10',    767, 300, 60,  '09:05:00', '04:05:00', 0, 229,  10);
```

### 6.2 开奖结果表

```sql
-- 开奖结果表
CREATE TABLE `lottery_draw_results` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `lottery_code` VARCHAR(50) NOT NULL COMMENT '彩种代码',
    `lot_code` INT NOT NULL COMMENT '外部lotCode',
    `issue_no` VARCHAR(50) NOT NULL COMMENT '期号',
    `draw_numbers` VARCHAR(100) NOT NULL COMMENT '开奖号码(逗号分隔)',
    `draw_time` DATETIME NOT NULL COMMENT '开奖时间',
    `total_sum` INT DEFAULT NULL COMMENT '号码总和',
    `big_small` VARCHAR(10) DEFAULT NULL COMMENT '大小',
    `odd_even` VARCHAR(10) DEFAULT NULL COMMENT '单双',
    `dragon_tiger` VARCHAR(20) DEFAULT NULL COMMENT '龙虎',
    `data_hash` VARCHAR(64) DEFAULT NULL COMMENT '数据哈希',
    `source` VARCHAR(50) DEFAULT 'bw1284' COMMENT '数据来源',
    `is_settled` TINYINT(1) DEFAULT 0 COMMENT '是否已结算',
    `settled_at` DATETIME DEFAULT NULL COMMENT '结算时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_lottery_issue` (`lottery_code`, `issue_no`),
    INDEX `idx_lottery_code` (`lottery_code`),
    INDEX `idx_draw_time` (`draw_time`),
    INDEX `idx_is_settled` (`is_settled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开奖结果表';
```

---

## 七、12个彩种API调用示例

### 7.1 完整调用示例

```bash
# 1. 加拿大PC28 (lotCode=720)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=720&pageSize=10&pageNo=1"

# 2. 加拿大时时彩 (lotCode=719)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=719&pageSize=10&pageNo=1"

# 3. 澳洲幸运10 (lotCode=797)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=797&pageSize=10&pageNo=1"

# 4. 澳洲幸运5 (lotCode=795)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=795&pageSize=10&pageNo=1"

# 5. 欢乐赛车 (lotCode=763)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=763&pageSize=10&pageNo=1"

# 6. 欢乐时时彩 (lotCode=762)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=762&pageSize=10&pageNo=1"

# 7. 幸运飞艇 (lotCode=765)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=765&pageSize=10&pageNo=1"

# 8. 极速赛车 (lotCode=768)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=768&pageSize=10&pageNo=1"

# 9. 极速时时彩 (lotCode=769)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=769&pageSize=10&pageNo=1"

# 10. 168幸运飞艇 (lotCode=726)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=726&pageSize=10&pageNo=1"

# 11. 体彩乐透5 (lotCode=766)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=766&pageSize=10&pageNo=1"

# 12. 体彩乐透10 (lotCode=767)
curl "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=767&pageSize=10&pageNo=1"
```

---

## 八、彩种分类汇总

### 8.1 按开奖频率分类

| 分类 | 彩种 | lotCode | 轮询间隔 |
|------|------|---------|---------|
| 高频(30秒) | 极速赛车 | 768 | 30s |
| 高频(30秒) | 极速时时彩 | 769 | 30s |
| 高频(30秒) | 欢乐赛车 | 763 | 30s |
| 高频(30秒) | 欢乐时时彩 | 762 | 30s |
| 高频(30秒) | 澳洲幸运10 | 797 | 30s |
| 中频(60秒) | 168幸运飞艇 | 726 | 60s |
| 中频(60秒) | 加拿大PC28 | 720 | 60s |
| 中频(60秒) | 加拿大时时彩 | 719 | 60s |
| 中频(60秒) | 澳洲幸运5 | 795 | 60s |
| 低频(5分钟) | 幸运飞艇 | 765 | 300s |
| 低频(5分钟) | 体彩乐透5 | 766 | 300s |
| 低频(5分钟) | 体彩乐透10 | 767 | 300s |

### 8.2 按开奖号码数量分类

| 号码数量 | 彩种 | lotCode |
|---------|------|---------|
| 3个号码 | 加拿大PC28 | 720 |
| 5个号码 | 加拿大时时彩 | 719 |
| 5个号码 | 澳洲幸运5 | 795 |
| 5个号码 | 欢乐时时彩 | 762 |
| 5个号码 | 极速时时彩 | 769 |
| 5个号码 | 体彩乐透5 | 766 |
| 10个号码 | 澳洲幸运10 | 797 |
| 10个号码 | 欢乐赛车 | 763 |
| 10个号码 | 幸运飞艇 | 765 |
| 10个号码 | 极速赛车 | 768 |
| 10个号码 | 168幸运飞艇 | 726 |
| 10个号码 | 体彩乐透10 | 767 |

---

## 九、快速参考

### lotCode 速查表

```
720 = 加拿大PC28
719 = 加拿大时时彩
797 = 澳洲幸运10
795 = 澳洲幸运5
763 = 欢乐赛车
762 = 欢乐时时彩
765 = 幸运飞艇
768 = 极速赛车
769 = 极速时时彩
726 = 168幸运飞艇
766 = 体彩乐透5
767 = 体彩乐透10
```

### API快速调用模板

```
获取开奖列表: https://bw1284.cc/api/lottery_code/getLotteryList?lotCode={lotCode}&pageSize=100&pageNo=1
获取彩种信息: https://bw1284.cc/api/lottery_code/getLotteryInfo?lotCode={lotCode}
```

---

**文档版本**: v1.0  
**创建日期**: 2026-01-17  
**接口来源**: bw1284.cc
