# 🎰 外部开奖API接口文档

> 从 https://bw1284.cc 抓取的完整开奖API接口规范
>
> 抓包日期：2026-01-17
> 抓包来源：bw1284.cc

---

## 📋 API接口总览

| 接口名称 | 请求方式 | 接口路径 | 功能说明 |
|---------|---------|---------|---------|
| 获取所有彩种 | GET | `/api/lottery_code/allLottery` | 获取系统支持的全部彩种列表 |
| 获取最新开奖列表 | GET | `/api/lottery_code/getLotteryLatestOutcomeList` | 按类型获取最新开奖 |
| 获取彩种信息(含倒计时) | GET | `/api/lottery_code/getLotteryInfo` | 获取指定彩种当前期信息 |
| 获取历史开奖列表 | GET | `/api/lottery_code/getLotteryList` | 获取历史开奖记录分页列表 |

---

## 🔐 API基本信息

### 基础URL
```
https://bw1284.cc/api/lottery_code
```

### 请求头
```http
Accept: application/json
Content-Type: application/json
User-Agent: Mozilla/5.0 ...
```

### 响应格式
```json
{
  "code": 1,           // 1=成功, 其他=失败
  "message": "成功",   // 响应消息
  "data": { ... }      // 响应数据
}
```

---

## 📖 接口详细说明

### 1️⃣ 获取所有彩种列表

**接口地址**
```
GET /api/lottery_code/allLottery
```

**请求参数**
```
无
```

**请求示例**
```bash
curl -X GET "https://bw1284.cc/api/lottery_code/allLottery"
```

**成功响应 (code=1)**
```json
{
  "code": 1,
  "message": "成功",
  "data": [
    {
      "lotType": 1,
      "lotTypeName": "PC蛋蛋",
      "lotteries": [
        {
          "lotCode": 762,
          "lotName": "加拿大PC28",
          "lotIcon": "jndpc28",
          "intervalTime": 210,
          "enable": true
        },
        {
          "lotCode": 795,
          "lotName": "加拿大时时彩",
          "lotIcon": "jndssc",
          "intervalTime": 60,
          "enable": true
        }
      ]
    },
    {
      "lotType": 2,
      "lotTypeName": "PK10",
      "lotteries": [
        {
          "lotCode": 763,
          "lotName": "幸运飞艇",
          "lotIcon": "xyft",
          "intervalTime": 300,
          "enable": true
        },
        {
          "lotCode": 764,
          "lotName": "极速赛车",
          "lotIcon": "jssc",
          "intervalTime": 60,
          "enable": true
        }
      ]
    },
    {
      "lotType": 3,
      "lotTypeName": "时时彩",
      "lotteries": [
        {
          "lotCode": 765,
          "lotName": "极速时时彩",
          "lotIcon": "jsssc",
          "intervalTime": 60,
          "enable": true
        }
      ]
    },
    {
      "lotType": 4,
      "lotTypeName": "澳洲彩",
      "lotteries": [
        {
          "lotCode": 766,
          "lotName": "澳洲幸运5",
          "lotIcon": "aozxy5",
          "intervalTime": 300,
          "enable": true
        },
        {
          "lotCode": 767,
          "lotName": "澳洲幸运10",
          "lotIcon": "aozxy10",
          "intervalTime": 300,
          "enable": true
        }
      ]
    }
  ]
}
```

**失败响应 (code≠1)**
```json
{
  "code": 0,
  "message": "系统繁忙，请稍后重试",
  "data": null
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| lotType | int | 彩种类型：1=PC蛋蛋, 2=PK10, 3=时时彩, 4=澳洲彩 |
| lotTypeName | string | 彩种类型名称 |
| lotCode | int | 彩种代码（唯一标识） |
| lotName | string | 彩种名称 |
| lotIcon | string | 彩种图标代码 |
| intervalTime | int | 开奖间隔（秒） |
| enable | boolean | 是否启用 |

---

### 2️⃣ 获取最新开奖列表（按类型）

**接口地址**
```
GET /api/lottery_code/getLotteryLatestOutcomeList
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| lotType | int | 是 | 彩种类型：1=PC蛋蛋, 2=PK10, 3=时时彩, 4=澳洲彩 |

**请求示例**
```bash
# PC蛋蛋类
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryLatestOutcomeList?lotType=1"

# PK10类（赛车/飞艇）
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryLatestOutcomeList?lotType=2"

# 时时彩类
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryLatestOutcomeList?lotType=3"

# 澳洲彩类
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryLatestOutcomeList?lotType=4"
```

**成功响应 (code=1)**
```json
{
  "code": 1,
  "message": "成功",
  "data": [
    {
      "lotCode": 762,
      "lotName": "加拿大PC28",
      "issue": "3385240",
      "openCode": "7,9,2",
      "openTime": "2026-01-17 07:17:00",
      "sumValue": 18,
      "bigSmall": "大",
      "oddEven": "双",
      "nextIssue": "3385241",
      "nextOpenTime": "2026-01-17 07:20:30",
      "countdown": 180
    },
    {
      "lotCode": 795,
      "lotName": "加拿大时时彩",
      "issue": "51281348",
      "openCode": "5,9,3",
      "openTime": "2026-01-17 07:16:00",
      "nextIssue": "51281349",
      "nextOpenTime": "2026-01-17 07:17:00",
      "countdown": 45
    }
  ]
}
```

**失败响应**
```json
{
  "code": 0,
  "message": "参数错误",
  "data": null
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| lotCode | int | 彩种代码 |
| lotName | string | 彩种名称 |
| issue | string | 当前期号 |
| openCode | string | 开奖号码（逗号分隔） |
| openTime | string | 开奖时间 |
| sumValue | int | 和值（PC28专用） |
| bigSmall | string | 大/小 |
| oddEven | string | 单/双 |
| nextIssue | string | 下一期期号 |
| nextOpenTime | string | 下一期开奖时间 |
| countdown | int | 距离下一期开奖倒计时（秒） |

---

### 3️⃣ 获取彩种详细信息（含实时倒计时）

**接口地址**
```
GET /api/lottery_code/getLotteryInfo
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| lotCode | int | 是 | 彩种代码 |
| issue | string | 否 | 期号（用于轮询，获取指定期开奖结果） |

**请求示例**
```bash
# 获取最新彩种信息
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryInfo?lotCode=762"

# 轮询指定期号（用于等待开奖结果）
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryInfo?lotCode=762&issue=3385240"
```

**成功响应 - 等待开奖 (code=1)**
```json
{
  "code": 1,
  "message": "成功",
  "data": {
    "lotCode": 762,
    "lotName": "加拿大PC28",
    "lotIcon": "jndpc28",
    "lotType": 1,
    "intervalTime": 210,
    
    "currentIssue": "3385241",
    "currentStatus": "WAITING",
    "countdown": 156,
    "stopBetTime": 30,
    
    "lastIssue": "3385240",
    "lastOpenCode": "7,9,2",
    "lastOpenTime": "2026-01-17 07:17:00",
    "lastSumValue": 18,
    "lastBigSmall": "大",
    "lastOddEven": "双",
    
    "nextIssue": "3385242",
    "nextOpenTime": "2026-01-17 07:24:00",
    
    "serverTime": "2026-01-17 07:18:24"
  }
}
```

**成功响应 - 已开奖 (code=1)**
```json
{
  "code": 1,
  "message": "成功",
  "data": {
    "lotCode": 762,
    "lotName": "加拿大PC28",
    "lotIcon": "jndpc28",
    "lotType": 1,
    "intervalTime": 210,
    
    "currentIssue": "3385240",
    "currentStatus": "OPENED",
    "openCode": "7,9,2",
    "openTime": "2026-01-17 07:17:00",
    "sumValue": 18,
    "bigSmall": "大",
    "oddEven": "双",
    
    "nextIssue": "3385241",
    "nextOpenTime": "2026-01-17 07:20:30",
    "countdown": 180,
    
    "serverTime": "2026-01-17 07:17:30"
  }
}
```

**失败响应**
```json
{
  "code": 0,
  "message": "彩种不存在",
  "data": null
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| lotCode | int | 彩种代码 |
| lotName | string | 彩种名称 |
| lotIcon | string | 彩种图标 |
| lotType | int | 彩种类型 |
| intervalTime | int | 开奖间隔（秒） |
| currentIssue | string | 当前期号 |
| currentStatus | string | 状态：WAITING=等待开奖, OPENED=已开奖 |
| countdown | int | 倒计时（秒） |
| stopBetTime | int | 距离停止投注时间（秒） |
| openCode | string | 开奖号码 |
| openTime | string | 开奖时间 |
| sumValue | int | 和值 |
| bigSmall | string | 大/小 |
| oddEven | string | 单/双 |
| lastIssue | string | 上期期号 |
| lastOpenCode | string | 上期开奖号码 |
| nextIssue | string | 下期期号 |
| nextOpenTime | string | 下期开奖时间 |
| serverTime | string | 服务器时间 |

---

### 4️⃣ 获取历史开奖列表

**接口地址**
```
GET /api/lottery_code/getLotteryList
```

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| lotCode | int | 是 | - | 彩种代码 |
| pageNo | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 20 | 每页数量（最大100） |
| date | string | 否 | 当天 | 日期：yyyy-MM-dd |

**请求示例**
```bash
# 获取加拿大PC28今日历史开奖
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=762&pageSize=100&pageNo=1&date=2026-01-17"

# 获取加拿大时时彩历史开奖
curl -X GET "https://bw1284.cc/api/lottery_code/getLotteryList?lotCode=795&pageSize=50&pageNo=1&date=2026-01-17"
```

**成功响应 (code=1)**
```json
{
  "code": 1,
  "message": "成功",
  "data": {
    "lotCode": 762,
    "lotName": "加拿大PC28",
    "pageNo": 1,
    "pageSize": 100,
    "totalCount": 256,
    "totalPages": 3,
    "list": [
      {
        "issue": "3385240",
        "openCode": "7,9,2",
        "openTime": "2026-01-17 07:17:00",
        "num1": 7,
        "num2": 9,
        "num3": 2,
        "sumValue": 18,
        "bigSmall": "大",
        "oddEven": "双"
      },
      {
        "issue": "3385239",
        "openCode": "5,9,3",
        "openTime": "2026-01-17 07:13:30",
        "num1": 5,
        "num2": 9,
        "num3": 3,
        "sumValue": 17,
        "bigSmall": "大",
        "oddEven": "单"
      },
      {
        "issue": "3385238",
        "openCode": "7,4,8",
        "openTime": "2026-01-17 07:10:00",
        "num1": 7,
        "num2": 4,
        "num3": 8,
        "sumValue": 19,
        "bigSmall": "大",
        "oddEven": "单"
      }
    ]
  }
}
```

**失败响应**
```json
{
  "code": 0,
  "message": "暂无数据",
  "data": {
    "list": [],
    "totalCount": 0
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNo | int | 当前页码 |
| pageSize | int | 每页数量 |
| totalCount | int | 总记录数 |
| totalPages | int | 总页数 |
| issue | string | 期号 |
| openCode | string | 开奖号码（逗号分隔） |
| openTime | string | 开奖时间 |
| num1, num2, num3... | int | 拆分后的各位号码 |
| sumValue | int | 和值 |
| bigSmall | string | 大/小 |
| oddEven | string | 单/双 |

---

## 🎮 彩种代码对照表

### 已确认的彩种代码

| lotCode | 彩种名称 | lotIcon | lotType | 开奖间隔 | 号码格式 |
|---------|---------|---------|---------|---------|---------|
| 762 | 加拿大PC28 | jndpc28 | 1 | 210秒 | 3个号码(0-9) |
| 795 | 加拿大时时彩 | jndssc | 1 | 60秒 | 5个号码(0-9) |
| 763 | 幸运飞艇 | xyft | 2 | 300秒 | 10个号码(1-10) |
| 764 | 极速赛车 | jssc | 2 | 60秒 | 10个号码(1-10) |
| 765 | 极速时时彩 | jsssc | 3 | 60秒 | 5个号码(0-9) |
| 766 | 澳洲幸运5 | aozxy5 | 4 | 300秒 | 5个号码(1-10) |
| 767 | 澳洲幸运10 | aozxy10 | 4 | 300秒 | 10个号码(1-10) |

### 彩种类型说明

| lotType | 类型名称 | 包含彩种 |
|---------|---------|---------|
| 1 | PC蛋蛋 | 加拿大PC28, 加拿大时时彩 |
| 2 | PK10 | 幸运飞艇, 极速赛车, 168幸运飞艇 |
| 3 | 时时彩 | 极速时时彩, 欢乐时时彩 |
| 4 | 澳洲彩 | 澳洲幸运5, 澳洲幸运10 |

---

## ⚡ 轮询策略说明

### 实时开奖轮询

网站使用轮询方式获取实时开奖数据：

```javascript
// 轮询间隔：2秒
setInterval(() => {
  fetch(`/api/lottery_code/getLotteryInfo?lotCode=${lotCode}&issue=${currentIssue}`)
    .then(response => response.json())
    .then(data => {
      if (data.data.currentStatus === 'OPENED') {
        // 已开奖，更新界面
        updateDrawResult(data.data);
        // 切换到下一期
        currentIssue = data.data.nextIssue;
      } else {
        // 更新倒计时
        updateCountdown(data.data.countdown);
      }
    });
}, 2000);
```

### 抓包发现的轮询特征

```
请求频率：每2秒一次
接口：/api/lottery_code/getLotteryInfo?lotCode=795&issue=51281348
连续请求时间戳：
  - 1768606419147
  - 1768606421156 (+2009ms)
  - 1768606423156 (+2000ms)
  - 1768606425146 (+1990ms)
  - 1768606427150 (+2004ms)
```

---

## 📊 响应状态码

### 业务状态码

| code | 说明 | 处理方式 |
|------|------|---------|
| 1 | 成功 | 正常处理数据 |
| 0 | 失败/系统错误 | 检查message，重试 |
| -1 | 参数错误 | 检查请求参数 |
| -2 | 数据不存在 | 忽略或重试 |
| -3 | 未开奖 | 继续轮询 |

### HTTP状态码

| 状态码 | 说明 | 处理方式 |
|--------|------|---------|
| 200 | 成功 | 解析响应体 |
| 204 | No Content | 忽略（CDN监控） |
| 400 | 请求错误 | 检查参数 |
| 403 | 禁止访问 | 检查认证 |
| 429 | 请求频率过高 | 降低频率 |
| 500 | 服务器错误 | 重试 |
| 502/503/504 | 网关错误 | 重试 |

---

## 🔒 安全与限制

### 请求频率限制

```
单IP限制：
- 每分钟最多 60 次请求
- 每秒最多 3 次请求

超出限制返回：
HTTP 429 Too Many Requests
{
  "code": -429,
  "message": "请求频率过高，请稍后重试"
}
```

### 推荐的请求策略

```java
// 1. 使用代理池轮换IP
// 2. 随机化请求间隔
int baseInterval = 2000; // 2秒
int randomDelay = new Random().nextInt(500); // 0-500ms随机
Thread.sleep(baseInterval + randomDelay);

// 3. 添加适当的请求头
HttpHeaders headers = new HttpHeaders();
headers.set("User-Agent", "Mozilla/5.0 ...");
headers.set("Referer", "https://bw1284.cc/");
headers.set("Accept", "application/json");
```

---

## 💻 Java接入示例

### Maven依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

### API调用封装类

```java
package com.bcbbs.backend.external;

import com.google.gson.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
public class ExternalLotteryApiClient {

    private static final String BASE_URL = "https://bw1284.cc/api/lottery_code";
    private final RestTemplate restTemplate;
    private final Gson gson;

    public ExternalLotteryApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }

    /**
     * 获取所有彩种列表
     */
    public List<LotteryType> getAllLotteries() {
        String url = BASE_URL + "/allLottery";
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                String.class
            );

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (json.get("code").getAsInt() != 1) {
                log.error("获取彩种列表失败: {}", json.get("message").getAsString());
                return new ArrayList<>();
            }

            JsonArray dataArray = json.getAsJsonArray("data");
            List<LotteryType> types = new ArrayList<>();
            for (JsonElement element : dataArray) {
                types.add(gson.fromJson(element, LotteryType.class));
            }
            return types;
        } catch (Exception e) {
            log.error("调用彩种列表API异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取最新开奖列表（按类型）
     */
    public List<LotteryLatestResult> getLatestOutcomeList(int lotType) {
        String url = BASE_URL + "/getLotteryLatestOutcomeList?lotType=" + lotType;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                String.class
            );

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (json.get("code").getAsInt() != 1) {
                log.error("获取最新开奖失败: {}", json.get("message").getAsString());
                return new ArrayList<>();
            }

            JsonArray dataArray = json.getAsJsonArray("data");
            List<LotteryLatestResult> results = new ArrayList<>();
            for (JsonElement element : dataArray) {
                results.add(gson.fromJson(element, LotteryLatestResult.class));
            }
            return results;
        } catch (Exception e) {
            log.error("调用最新开奖API异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取彩种详细信息（含倒计时）
     */
    public LotteryInfo getLotteryInfo(int lotCode) {
        return getLotteryInfo(lotCode, null);
    }

    /**
     * 获取彩种详细信息（指定期号轮询）
     */
    public LotteryInfo getLotteryInfo(int lotCode, String issue) {
        StringBuilder url = new StringBuilder(BASE_URL)
            .append("/getLotteryInfo?lotCode=").append(lotCode);
        if (issue != null && !issue.isEmpty()) {
            url.append("&issue=").append(issue);
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                createHttpEntity(),
                String.class
            );

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (json.get("code").getAsInt() != 1) {
                log.warn("获取彩种信息失败: {}", json.get("message").getAsString());
                return null;
            }

            return gson.fromJson(json.getAsJsonObject("data"), LotteryInfo.class);
        } catch (Exception e) {
            log.error("调用彩种信息API异常", e);
            return null;
        }
    }

    /**
     * 获取历史开奖列表
     */
    public LotteryHistoryResponse getLotteryHistory(int lotCode, int pageNo, int pageSize, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = String.format(
            "%s/getLotteryList?lotCode=%d&pageNo=%d&pageSize=%d&date=%s",
            BASE_URL, lotCode, pageNo, pageSize, dateStr
        );

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                String.class
            );

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (json.get("code").getAsInt() != 1) {
                log.warn("获取历史开奖失败: {}", json.get("message").getAsString());
                return null;
            }

            return gson.fromJson(json.getAsJsonObject("data"), LotteryHistoryResponse.class);
        } catch (Exception e) {
            log.error("调用历史开奖API异常", e);
            return null;
        }
    }

    /**
     * 创建HTTP请求实体（带请求头）
     */
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Referer", "https://bw1284.cc/");
        headers.set("Origin", "https://bw1284.cc");
        return new HttpEntity<>(headers);
    }

    // ================== DTO类 ==================

    @Data
    public static class LotteryType {
        private int lotType;
        private String lotTypeName;
        private List<LotteryItem> lotteries;
    }

    @Data
    public static class LotteryItem {
        private int lotCode;
        private String lotName;
        private String lotIcon;
        private int intervalTime;
        private boolean enable;
    }

    @Data
    public static class LotteryLatestResult {
        private int lotCode;
        private String lotName;
        private String issue;
        private String openCode;
        private String openTime;
        private Integer sumValue;
        private String bigSmall;
        private String oddEven;
        private String nextIssue;
        private String nextOpenTime;
        private Integer countdown;
    }

    @Data
    public static class LotteryInfo {
        private int lotCode;
        private String lotName;
        private String lotIcon;
        private int lotType;
        private int intervalTime;
        private String currentIssue;
        private String currentStatus;
        private Integer countdown;
        private Integer stopBetTime;
        private String openCode;
        private String openTime;
        private Integer sumValue;
        private String bigSmall;
        private String oddEven;
        private String lastIssue;
        private String lastOpenCode;
        private String lastOpenTime;
        private Integer lastSumValue;
        private String lastBigSmall;
        private String lastOddEven;
        private String nextIssue;
        private String nextOpenTime;
        private String serverTime;
    }

    @Data
    public static class LotteryHistoryResponse {
        private int lotCode;
        private String lotName;
        private int pageNo;
        private int pageSize;
        private int totalCount;
        private int totalPages;
        private List<LotteryDrawRecord> list;
    }

    @Data
    public static class LotteryDrawRecord {
        private String issue;
        private String openCode;
        private String openTime;
        private Integer num1;
        private Integer num2;
        private Integer num3;
        private Integer num4;
        private Integer num5;
        private Integer num6;
        private Integer num7;
        private Integer num8;
        private Integer num9;
        private Integer num10;
        private Integer sumValue;
        private String bigSmall;
        private String oddEven;
    }
}
```

### 定时任务拉取开奖数据

```java
package com.bcbbs.backend.scheduler;

import com.bcbbs.backend.external.ExternalLotteryApiClient;
import com.bcbbs.backend.external.ExternalLotteryApiClient.LotteryInfo;
import com.bcbbs.backend.service.LotteryDrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryDrawScheduler {

    private final ExternalLotteryApiClient apiClient;
    private final LotteryDrawService drawService;

    // 彩种代码列表
    private static final List<Integer> LOT_CODES = Arrays.asList(
        762,  // 加拿大PC28
        795,  // 加拿大时时彩
        763,  // 幸运飞艇
        764,  // 极速赛车
        765,  // 极速时时彩
        766,  // 澳洲幸运5
        767   // 澳洲幸运10
    );

    // 记录每个彩种当前轮询的期号
    private final Map<Integer, String> currentIssues = new HashMap<>();

    /**
     * 每10秒轮询一次所有彩种
     */
    @Scheduled(fixedDelay = 10000)
    public void pollAllLotteries() {
        log.info("开始轮询所有彩种开奖数据...");

        for (Integer lotCode : LOT_CODES) {
            try {
                pollSingleLottery(lotCode);
                // 添加随机延迟，避免请求过于集中
                Thread.sleep(500 + new Random().nextInt(500));
            } catch (Exception e) {
                log.error("轮询彩种{}失败: {}", lotCode, e.getMessage());
            }
        }
    }

    /**
     * 轮询单个彩种
     */
    private void pollSingleLottery(int lotCode) {
        String currentIssue = currentIssues.get(lotCode);
        
        LotteryInfo info = apiClient.getLotteryInfo(lotCode, currentIssue);
        if (info == null) {
            log.warn("彩种{}获取信息失败", lotCode);
            return;
        }

        // 检查是否已开奖
        if ("OPENED".equals(info.getCurrentStatus()) && info.getOpenCode() != null) {
            // 保存开奖结果
            drawService.saveDrawResult(
                lotCode,
                info.getCurrentIssue(),
                info.getOpenCode(),
                info.getOpenTime(),
                info.getSumValue(),
                info.getBigSmall(),
                info.getOddEven()
            );

            log.info("彩种{}期号{}已开奖: {}", 
                info.getLotName(), 
                info.getCurrentIssue(), 
                info.getOpenCode()
            );

            // 更新到下一期
            currentIssues.put(lotCode, info.getNextIssue());
        } else {
            // 更新当前期号
            if (info.getCurrentIssue() != null) {
                currentIssues.put(lotCode, info.getCurrentIssue());
            }
            
            log.debug("彩种{}等待开奖，倒计时: {}秒", 
                info.getLotName(), 
                info.getCountdown()
            );
        }
    }
}
```

---

## 🌐 前端Vue3接入示例

```typescript
// api/lottery-external.ts

import axios from 'axios';

const BASE_URL = 'https://bw1284.cc/api/lottery_code';

export interface LotteryInfo {
  lotCode: number;
  lotName: string;
  currentIssue: string;
  currentStatus: 'WAITING' | 'OPENED';
  countdown: number;
  openCode?: string;
  openTime?: string;
  sumValue?: number;
  bigSmall?: string;
  oddEven?: string;
  nextIssue: string;
  nextOpenTime: string;
}

export interface LotteryHistoryItem {
  issue: string;
  openCode: string;
  openTime: string;
  sumValue?: number;
  bigSmall?: string;
  oddEven?: string;
}

export const externalLotteryApi = {
  // 获取所有彩种
  getAllLotteries: async () => {
    const { data } = await axios.get(`${BASE_URL}/allLottery`);
    return data;
  },

  // 获取最新开奖
  getLatestOutcome: async (lotType: number) => {
    const { data } = await axios.get(
      `${BASE_URL}/getLotteryLatestOutcomeList?lotType=${lotType}`
    );
    return data;
  },

  // 获取彩种信息（含倒计时）
  getLotteryInfo: async (lotCode: number, issue?: string): Promise<LotteryInfo> => {
    let url = `${BASE_URL}/getLotteryInfo?lotCode=${lotCode}`;
    if (issue) {
      url += `&issue=${issue}`;
    }
    const { data } = await axios.get(url);
    return data.data;
  },

  // 获取历史开奖
  getLotteryHistory: async (
    lotCode: number, 
    date: string, 
    pageNo = 1, 
    pageSize = 100
  ): Promise<{ list: LotteryHistoryItem[], totalCount: number }> => {
    const { data } = await axios.get(
      `${BASE_URL}/getLotteryList?lotCode=${lotCode}&date=${date}&pageNo=${pageNo}&pageSize=${pageSize}`
    );
    return data.data;
  }
};
```

### Vue组件轮询开奖

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { externalLotteryApi, type LotteryInfo } from '@/api/lottery-external';

const props = defineProps<{
  lotCode: number;
}>();

const lotteryInfo = ref<LotteryInfo | null>(null);
const countdown = ref(0);
let pollTimer: number | null = null;
let countdownTimer: number | null = null;

// 轮询获取开奖信息
const pollLotteryInfo = async () => {
  try {
    const info = await externalLotteryApi.getLotteryInfo(
      props.lotCode, 
      lotteryInfo.value?.currentIssue
    );
    
    if (info.currentStatus === 'OPENED' && info.openCode) {
      // 已开奖
      lotteryInfo.value = info;
      // 播放开奖动画
      playDrawAnimation(info.openCode);
    } else {
      lotteryInfo.value = info;
      countdown.value = info.countdown;
    }
  } catch (error) {
    console.error('获取开奖信息失败', error);
  }
};

// 倒计时更新
const startCountdown = () => {
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    }
  }, 1000);
};

onMounted(() => {
  pollLotteryInfo();
  // 每2秒轮询一次
  pollTimer = setInterval(pollLotteryInfo, 2000);
  startCountdown();
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
  if (countdownTimer) clearInterval(countdownTimer);
});
</script>
```

---

## 📝 注意事项

### 1. 跨域问题

```javascript
// 前端直接调用会遇到CORS问题
// 解决方案1：通过后端代理
// 解决方案2：使用JSONP（如果API支持）
// 解决方案3：配置Nginx反向代理
```

### 2. 数据一致性

```java
// 重要：保存前需要验证数据
public void saveDrawResult(...) {
    // 1. 检查期号是否已存在
    if (existsByIssue(issue)) {
        log.warn("期号{}已存在，跳过", issue);
        return;
    }
    
    // 2. 验证开奖号码格式
    if (!validateOpenCode(openCode, lotCode)) {
        log.error("开奖号码格式错误: {}", openCode);
        return;
    }
    
    // 3. 验证时间合理性
    if (openTime.isAfter(LocalDateTime.now())) {
        log.error("开奖时间异常: {}", openTime);
        return;
    }
    
    // 4. 保存数据
    ...
}
```

### 3. 容错处理

```java
// API可能返回空数据或错误
// 必须做好异常处理
try {
    LotteryInfo info = apiClient.getLotteryInfo(lotCode);
    if (info == null) {
        // 重试或跳过
        return;
    }
    // 处理数据...
} catch (HttpClientErrorException e) {
    if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
        // 请求过于频繁，降低频率
        Thread.sleep(5000);
    }
} catch (Exception e) {
    log.error("API调用异常", e);
}
```

---

## 📅 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|---------|
| 2026-01-17 | 1.0 | 初始版本，完成所有接口抓包分析 |

---

**文档版本**: 1.0  
**抓包来源**: https://bw1284.cc  
**抓包日期**: 2026-01-17  
**维护者**: BCBBS3 开发团队
