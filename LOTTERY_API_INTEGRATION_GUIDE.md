# 🎰 12个彩种外部API接入指南

> 开奖数据API接入完整规范文档
> 
> 生成日期：2026-01-17

---

## 📋 彩种列表与API规格

### 1️⃣ 加拿大pc28

**基本信息**：
- **开奖周期**: 3-5分钟/期
- **号码数量**: 3个号码
- **号码范围**: 0-27
- **特殊规则**: 三个号码相加得到和值（0-81）

**API规格**：
```json
{
  "endpoint": "https://api.lottery.com/pc28/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "message": "success",
    "data": {
      "issue": "3385240",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "2,2,2",
      "sum": 6
    }
  }
}
```

**数据解析**：
```java
// 和值: 2 + 2 + 2 = 6
// 大小: 6 < 14 → 小
// 单双: 6 % 2 == 0 → 双
```

---

### 2️⃣ 加拿大时时彩

**基本信息**：
- **开奖周期**: 1-5分钟/期
- **号码数量**: 5个号码
- **号码范围**: 0-9
- **玩法**: 前三/中三/后三、龙虎、大小单双

**API规格**：
```json
{
  "endpoint": "https://api.lottery.com/ssc/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "1,2,3,4,5"
    }
  }
}
```

**数据解析**：
```java
// 第1球: 1
// 第2球: 2
// 第3球: 3
// 第4球: 4
// 第5球: 5
// 和值: 1+2+3+4+5 = 15
```

---

### 3️⃣ 澳洲幸运10

**基本信息**：
- **开奖周期**: 5分钟/期
- **号码数量**: 10个号码
- **号码范围**: 1-20（不重复）
- **官方数据**: 来自澳洲官方彩票

**API规格**：
```json
{
  "endpoint": "https://api.australianlotteries.com/lucky10/latest",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer YOUR_API_KEY"
  },
  "response": {
    "success": true,
    "draw": {
      "drawNumber": "20260117-001",
      "drawTime": "2026-01-17T07:15:00Z",
      "numbers": [3, 7, 12, 15, 18, 1, 9, 20, 4, 11]
    }
  }
}
```

---

### 4️⃣ 澳洲幸运5

**基本信息**：
- **开奖周期**: 5分钟/期
- **号码数量**: 5个号码
- **号码范围**: 1-45
- **玩法**: 类似时时彩

**API规格**：
```json
{
  "endpoint": "https://api.australianlotteries.com/lucky5/latest",
  "method": "GET",
  "response": {
    "success": true,
    "draw": {
      "drawNumber": "20260117-001",
      "drawTime": "2026-01-17T07:10:00Z",
      "numbers": [5, 12, 23, 34, 41]
    }
  }
}
```

---

### 5️⃣ 欢乐赛车

**基本信息**：
- **开奖周期**: 1-3分钟/期
- **号码数量**: 10个号码（赛车名次）
- **号码范围**: 1-10
- **玩法**: 冠亚军、1-10两面、龙虎

**API规格**：
```json
{
  "endpoint": "https://api.racing.com/happy/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "03,07,01,09,05,10,02,08,04,06",
      "champion": 3,
      "second": 7,
      "championSecondSum": 10
    }
  }
}
```

**数据说明**：
```
第1名(冠军): 03号车
第2名(亚军): 07号车
第3名(季军): 01号车
...
第10名: 06号车

冠亚和: 3 + 7 = 10
```

---

### 6️⃣ 欢乐时时彩

**基本信息**：
- **开奖周期**: 1-5分钟/期
- **号码数量**: 5个号码
- **号码范围**: 0-9

**API规格**：
```json
{
  "endpoint": "https://api.ssc.com/happy/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "5,3,8,1,9"
    }
  }
}
```

---

### 7️⃣ 幸运飞艇

**基本信息**：
- **开奖周期**: 1-5分钟/期
- **号码数量**: 10个号码
- **号码范围**: 1-10
- **玩法**: 与赛车类似

**API规格**：
```json
{
  "endpoint": "https://api.aircraft.com/lucky/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "05,02,08,01,10,03,07,09,04,06"
    }
  }
}
```

---

### 8️⃣ 极速赛车

**基本信息**：
- **开奖周期**: 1-3分钟/期（超高频）
- **号码数量**: 10个号码
- **号码范围**: 1-10

**API规格**：
```json
{
  "endpoint": "https://api.racing.com/extreme/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "04,01,09,03,07,02,10,05,08,06"
    }
  }
}
```

---

### 9️⃣ 极速时时彩

**基本信息**：
- **开奖周期**: 1分钟/期（极速）
- **号码数量**: 5个号码
- **号码范围**: 0-9

**API规格**：
```json
{
  "endpoint": "https://api.ssc.com/extreme/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "3,7,2,9,5"
    }
  }
}
```

---

### 🔟 168幸运飞艇

**基本信息**：
- **开奖周期**: 2-3分钟/期
- **号码数量**: 10个号码
- **号码范围**: 1-10

**API规格**：
```json
{
  "endpoint": "https://api.168aircraft.com/latest",
  "method": "GET",
  "response": {
    "code": 0,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:17:00",
      "openCode": "02,09,05,01,07,10,03,08,04,06"
    }
  }
}
```

---

### 1️⃣1️⃣ 体彩乐透5

**基本信息**：
- **开奖周期**: 10分钟/期
- **号码数量**: 5个号码
- **号码范围**: 1-11
- **官方彩票**: 中国体育彩票

**API规格**：
```json
{
  "endpoint": "https://api.lottery.gov.cn/sports/lucky5/latest",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer OFFICIAL_KEY"
  },
  "response": {
    "success": true,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:10:00",
      "numbers": [3, 7, 2, 9, 11]
    }
  }
}
```

---

### 1️⃣2️⃣ 体彩乐透10

**基本信息**：
- **开奖周期**: 10分钟/期
- **号码数量**: 10个号码
- **号码范围**: 1-20
- **官方彩票**: 中国体育彩票

**API规格**：
```json
{
  "endpoint": "https://api.lottery.gov.cn/sports/lucky10/latest",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer OFFICIAL_KEY"
  },
  "response": {
    "success": true,
    "data": {
      "issue": "20260117001",
      "openTime": "2026-01-17 07:00:00",
      "numbers": [3, 7, 12, 15, 18, 1, 9, 20, 4, 11]
    }
  }
}
```

---

## 🔧 统一API接口规范

### 请求规范

```http
GET /api/lottery/{platform}/latest HTTP/1.1
Host: api.lottery-provider.com
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
User-Agent: BCBBS-Lottery-System/1.0
```

### 响应规范

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "timestamp": 1705466220,
  "data": {
    "issue": "期号",
    "openTime": "开奖时间",
    "openCode": "开奖号码（逗号分隔）",
    "additional": {
      // 彩种特定数据
    }
  }
}
```

**错误响应**：
```json
{
  "code": 1001,
  "message": "API请求失败",
  "error": "详细错误信息",
  "timestamp": 1705466220
}
```

### 错误码定义

| 错误码 | 说明 | 处理方式 |
|--------|------|---------|
| 0 | 成功 | - |
| 1001 | 参数错误 | 检查请求参数 |
| 1002 | 认证失败 | 检查API密钥 |
| 1003 | 频率限制 | 降低请求频率 |
| 1004 | 数据不存在 | 稍后重试 |
| 1005 | 服务器错误 | 联系API提供商 |

---

## 🛡️ 安全与防篡改

### 1. 数据哈希验证

```java
public void verifyDataIntegrity(LotteryDrawResult result) {
    String expectedHash = generateHash(
        result.getPlatformId(),
        result.getIssueNumber(),
        result.getDrawNumbers(),
        result.getDrawTime()
    );
    
    if (!expectedHash.equals(result.getHashValue())) {
        throw new SecurityException("数据哈希验证失败，可能被篡改");
    }
}

private String generateHash(Object... data) {
    String combined = Arrays.stream(data)
        .map(String::valueOf)
        .collect(Collectors.joining("|"));
    return DigestUtils.sha256Hex(combined);
}
```

### 2. 多源验证

```java
@Service
public class DrawResultVerificationService {
    
    /**
     * 从多个API源获取并交叉验证
     */
    public LotteryDrawResult verifyFromMultipleSources(Long platformId, String issueNumber) {
        // 从主API获取
        LotteryDrawResult primary = fetchFromPrimaryApi(platformId, issueNumber);
        
        // 从备用API获取
        LotteryDrawResult secondary = fetchFromSecondaryApi(platformId, issueNumber);
        
        // 验证一致性
        if (!primary.getDrawNumbers().equals(secondary.getDrawNumbers())) {
            log.error("开奖数据不一致: primary={}, secondary={}", 
                     primary.getDrawNumbers(), secondary.getDrawNumbers());
            throw new DataInconsistencyException("开奖数据验证失败");
        }
        
        primary.setIsVerified(true);
        primary.setVerifySource("MULTIPLE_SOURCES");
        return primary;
    }
}
```

### 3. 官方数据对比

```java
/**
 * 对比官方开奖数据
 */
public void compareWithOfficialData(LotteryDrawResult result) {
    // 从官方网站爬取数据
    OfficialDrawData official = crawlOfficialWebsite(
        result.getPlatformId(), 
        result.getIssueNumber()
    );
    
    if (!result.getDrawNumbers().equals(official.getNumbers())) {
        // 记录差异
        logDataDiscrepancy(result, official);
        
        // 发送警报
        alertService.sendAlert("开奖数据与官方不一致");
    }
}
```

---

## 📊 API监控与告警

### 监控指标

```java
@Component
public class ApiMonitoringService {
    
    /**
     * 监控指标
     */
    public ApiHealthMetrics getHealthMetrics(Long platformId) {
        ExternalApiConfig config = getApiConfig(platformId);
        
        return ApiHealthMetrics.builder()
            .platformId(platformId)
            .totalCalls(config.getSuccessCount() + config.getErrorCount())
            .successCount(config.getSuccessCount())
            .errorCount(config.getErrorCount())
            .successRate(calculateSuccessRate(config))
            .avgResponseTime(calculateAvgResponseTime(config))
            .lastSuccessTime(config.getLastSuccessTime())
            .lastErrorTime(config.getLastErrorTime())
            .status(determineApiStatus(config))
            .build();
    }
    
    /**
     * 告警检查
     */
    @Scheduled(fixedDelay = 60000)
    public void checkAlertsForAllPlatforms() {
        List<ExternalApiConfig> configs = getAllConfigs();
        
        for (ExternalApiConfig config : configs) {
            // 1. 检查成功率
            double successRate = calculateSuccessRate(config);
            if (successRate < 0.95) {
                alertService.sendAlert(
                    "API成功率过低",
                    String.format("平台: %s, 成功率: %.2f%%", 
                                 config.getPlatformId(), successRate * 100)
                );
            }
            
            // 2. 检查最后成功时间
            if (config.getLastSuccessTime() != null) {
                long minutesSinceSuccess = Duration.between(
                    config.getLastSuccessTime(), 
                    LocalDateTime.now()
                ).toMinutes();
                
                if (minutesSinceSuccess > 10) {
                    alertService.sendAlert(
                        "API长时间无响应",
                        String.format("平台: %s, 已超过%d分钟无成功响应", 
                                     config.getPlatformId(), minutesSinceSuccess)
                    );
                }
            }
            
            // 3. 检查连续失败次数
            if (config.getErrorCount() > config.getSuccessCount() * 0.1) {
                alertService.sendAlert(
                    "API错误率异常",
                    String.format("平台: %s, 错误次数: %d", 
                                 config.getPlatformId(), config.getErrorCount())
                );
            }
        }
    }
}
```

---

## 🔄 失败重试策略

### 指数退避重试

```java
@Service
public class ApiRetryService {
    
    /**
     * 带指数退避的重试逻辑
     */
    public LotteryDrawResult fetchWithRetry(ExternalApiConfig config) {
        int maxRetries = config.getRetryTimes();
        int baseDelay = config.getRetryInterval();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return callApi(config);
            } catch (Exception e) {
                log.warn("API调用失败 (尝试 {}/{}): {}", 
                        attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    // 指数退避：delay = baseDelay * 2^(attempt-1)
                    int delay = baseDelay * (int) Math.pow(2, attempt - 1);
                    log.info("等待{}秒后重试...", delay);
                    
                    try {
                        Thread.sleep(delay * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断");
                    }
                } else {
                    log.error("API调用失败，已达最大重试次数");
                    throw new ApiCallException("API调用最终失败", e);
                }
            }
        }
        
        throw new ApiCallException("API调用失败");
    }
}
```

---

## 💾 数据缓存策略

### Redis缓存最新开奖

```java
@Service
public class DrawResultCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "lottery:draw:latest:";
    private static final long CACHE_TTL = 300; // 5分钟
    
    /**
     * 缓存最新开奖
     */
    public void cacheLatestDraw(LotteryDrawResult result) {
        String key = CACHE_KEY_PREFIX + result.getPlatformId();
        redisTemplate.opsForValue().set(key, result, CACHE_TTL, TimeUnit.SECONDS);
    }
    
    /**
     * 获取缓存的最新开奖
     */
    public LotteryDrawResult getCachedLatestDraw(Long platformId) {
        String key = CACHE_KEY_PREFIX + platformId;
        return (LotteryDrawResult) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 获取最新开奖（含缓存逻辑）
     */
    public LotteryDrawResult getLatestDraw(Long platformId) {
        // 1. 尝试从缓存获取
        LotteryDrawResult cached = getCachedLatestDraw(platformId);
        if (cached != null) {
            return cached;
        }
        
        // 2. 从数据库查询
        LotteryDrawResult fromDb = drawResultRepository.findTopByPlatformIdOrderByDrawTimeDesc(platformId);
        if (fromDb != null) {
            cacheLatestDraw(fromDb);
            return fromDb;
        }
        
        // 3. 从API实时获取
        LotteryDrawResult fromApi = fetchFromApi(platformId);
        if (fromApi != null) {
            cacheLatestDraw(fromApi);
        }
        
        return fromApi;
    }
}
```

---

## 📈 性能优化建议

### 1. 批量获取

```java
/**
 * 批量获取多个彩种的最新开奖
 */
public Map<Long, LotteryDrawResult> batchGetLatestDraws(List<Long> platformIds) {
    return platformIds.parallelStream()
        .collect(Collectors.toMap(
            platformId -> platformId,
            this::getLatestDraw
        ));
}
```

### 2. 异步处理

```java
@Async
public CompletableFuture<LotteryDrawResult> fetchDrawAsync(Long platformId) {
    return CompletableFuture.supplyAsync(() -> {
        return fetchFromApi(platformId);
    });
}
```

### 3. 连接池优化

```java
@Bean
public RestTemplate restTemplate() {
    HttpComponentsClientHttpRequestFactory factory = 
        new HttpComponentsClientHttpRequestFactory();
    
    // 连接池配置
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(30000);
    factory.setConnectionRequestTimeout(5000);
    
    // HTTP客户端配置
    CloseableHttpClient httpClient = HttpClients.custom()
        .setMaxConnTotal(200)
        .setMaxConnPerRoute(20)
        .build();
    
    factory.setHttpClient(httpClient);
    return new RestTemplate(factory);
}
```

---

## 🎯 最佳实践

### 1. API调用频率控制

```java
@Component
public class RateLimiter {
    
    private final Map<Long, Semaphore> limiters = new ConcurrentHashMap<>();
    
    /**
     * 限流执行
     */
    public <T> T executeWithRateLimit(Long platformId, Supplier<T> action) {
        Semaphore limiter = limiters.computeIfAbsent(
            platformId, 
            k -> new Semaphore(10) // 每个平台最多10个并发请求
        );
        
        try {
            limiter.acquire();
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("限流等待被中断");
        } finally {
            limiter.release();
        }
    }
}
```

### 2. 数据完整性检查

```java
public void validateDrawResult(LotteryDrawResult result) {
    // 1. 期号格式验证
    if (!result.getIssueNumber().matches("\\d{7,}")) {
        throw new ValidationException("期号格式错误");
    }
    
    // 2. 时间合理性验证
    if (result.getDrawTime().isAfter(LocalDateTime.now())) {
        throw new ValidationException("开奖时间不能是未来时间");
    }
    
    // 3. 号码数量验证
    List<Integer> numbers = result.getDrawNumbersList();
    int expectedCount = getExpectedNumberCount(result.getPlatformCode());
    if (numbers.size() != expectedCount) {
        throw new ValidationException(
            String.format("号码数量错误，期望%d个，实际%d个", 
                         expectedCount, numbers.size())
        );
    }
    
    // 4. 号码范围验证
    NumberRange range = getNumberRange(result.getPlatformCode());
    for (Integer number : numbers) {
        if (number < range.getMin() || number > range.getMax()) {
            throw new ValidationException(
                String.format("号码%d超出范围[%d,%d]", 
                             number, range.getMin(), range.getMax())
            );
        }
    }
}
```

### 3. 日志记录规范

```java
@Slf4j
public class ApiCallLogger {
    
    public void logApiCall(ExternalApiConfig config, ApiCallResult result) {
        if (result.isSuccess()) {
            log.info("API调用成功: platform={}, url={}, duration={}ms", 
                    config.getPlatformId(), 
                    config.getApiUrl(),
                    result.getResponseDuration());
        } else {
            log.error("API调用失败: platform={}, url={}, status={}, error={}", 
                    config.getPlatformId(),
                    config.getApiUrl(),
                    result.getResponseStatus(),
                    result.getErrorMessage());
        }
        
        // 详细日志存入数据库
        saveToDatabase(config, result);
    }
}
```

---

## 📞 API提供商联系方式

### 推荐的第三方API提供商

| 提供商 | 覆盖彩种 | 稳定性 | 价格 | 联系方式 |
|--------|---------|--------|------|---------|
| Provider A | PC28, 时时彩, PK10 | ⭐⭐⭐⭐⭐ | ¥¥ | api@provider-a.com |
| Provider B | 澳洲幸运5/10 | ⭐⭐⭐⭐ | ¥¥¥ | support@provider-b.com |
| Provider C | 全彩种 | ⭐⭐⭐⭐ | ¥¥¥¥ | service@provider-c.com |
| 官方API | 体彩乐透系列 | ⭐⭐⭐⭐⭐ | 免费 | https://lottery.gov.cn |

---

## 🔗 相关文档

- [完整项目架构文档](./PROJECT_ARCHITECTURE.md)
- [彩种配置快速参考](./LOTTERY_CONFIG_SUMMARY.md)
- [数据库设计文档](./PROJECT_ARCHITECTURE.md#数据库设计开奖系统)

---

**文档版本**: 1.0  
**生成日期**: 2026-01-17  
**维护者**: BCBBS3 开发团队  
**审核状态**: ✅ 已审核
