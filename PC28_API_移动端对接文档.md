# PC28 开奖 API 对接文档

> **版本**: v1.0.0
> **更新日期**: 2026-03-19
> **适用平台**: iOS / Android
> **接口协议**: HTTPS + RESTful
> **数据格式**: JSON

---

## 基础信息

| 项目 | 值 |
|------|----|
| 生产接口根地址 | `https://www.bcbbs3.cn/api` |
| 备用域名 | `https://www.18118bw.cn/api` |
| Content-Type | `application/json` |
| 是否需要 Token | ❌ 无需认证，公开接口 |
| 超时建议 | `10秒` |

---

## 通用响应格式

所有接口均返回统一格式：

```json
{
  "code": 200,
  "message": "Success",
  "data": { }
}
```

**出错时：**

```json
{
  "code": 500,
  "message": "错误描述",
  "errorId": "A1B2C3D4",
  "timestamp": "2026-03-19T10:30:00"
}
```

---

## 接口一：获取当前期信息

```
GET /api/public/lottery/info
```

> 返回**最新一期开奖结果**以及**下一期期号和开奖时间**。

### 请求参数

| 参数 | 类型 | 必须 | 默认值 | 说明 |
|------|------|:----:|--------|------|
| `lotCode` | int | ❌ | `720` | 彩票代码，PC28 固定传 `720` |
| `t` | long | ❌ | - | 当前时间戳（毫秒），防缓存用 |

**请求示例：**

```
GET https://www.bcbbs3.cn/api/public/lottery/info?lotCode=720&t=1742378400000
```

### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "preDrawCode": "1,0,8",
    "preDrawIssue": "20260319-001",
    "preDrawTime": "2026-03-19 10:00:00",
    "drawIssue": "20260319-002",
    "drawTime": "2026-03-19 10:05:00",
    "lotCode": 720,
    "lotName": "加拿大PC28",
    "sumValue": "9",
    "sizeLabel": "小",
    "parityLabel": "奇",
    "sizeParity": "小奇",
    "patternLabel": "杂六"
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `preDrawCode` | string | 上期三个开奖号码，逗号分隔，如 `"1,0,8"` |
| `preDrawIssue` | string | 上期期号 |
| `preDrawTime` | string | 上期开奖时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `drawIssue` | string | 当前/下期期号 |
| `drawTime` | string | 当前/下期开奖时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `lotCode` | int | 彩票代码（720） |
| `lotName` | string | 彩票名称（加拿大PC28） |
| `sumValue` | string | 三个号码之和，范围 `0-27` |
| `sizeLabel` | string | 大小：`大`（14~27）/ `小`（0~13） |
| `parityLabel` | string | 奇偶：`奇` / `偶` |
| `sizeParity` | string | 大小+奇偶组合：`大奇` / `大偶` / `小奇` / `小偶` |
| `patternLabel` | string | 号码形态，见下表 |

### 形态说明

| 形态 | 规则 | 示例 |
|------|------|------|
| `豹子` | 三个数字完全相同 | `2,2,2` |
| `顺子` | 三个数字连续（任意顺序） | `1,2,3` / `3,1,2` |
| `对子` | 两个数字相同，一个不同 | `1,1,2` |
| `半顺` | 两个数字相差1，第三个不相邻 | `1,2,5` |
| `杂六` | 不属于以上任何形态 | `1,3,7` |

---

## 接口二：获取历史开奖列表

```
GET /api/public/lottery/list
```

> 返回**分页历史开奖记录**，按时间降序（最新一期在最前）。

### 请求参数

| 参数 | 类型 | 必须 | 默认值 | 说明 |
|------|------|:----:|--------|------|
| `lotCode` | int | ❌ | `720` | 彩票代码，PC28 固定传 `720` |
| `pageNo` | int | ❌ | `1` | 页码，从 `1` 开始 |
| `pageSize` | int | ❌ | `30` | 每页条数，最大 `100` |
| `t` | long | ❌ | - | 当前时间戳（毫秒），防缓存用 |

**请求示例：**

```
GET https://www.bcbbs3.cn/api/public/lottery/list?lotCode=720&pageNo=1&pageSize=30&t=1742378400000
```

### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "list": [
      {
        "preDrawIssue": "20260319-001",
        "preDrawCode": "1,0,8",
        "preDrawTime": "2026-03-19 10:00:00",
        "sumValue": "9",
        "sizeLabel": "小",
        "parityLabel": "奇",
        "sizeParity": "小奇",
        "patternLabel": "杂六"
      },
      {
        "preDrawIssue": "20260319-000",
        "preDrawCode": "9,5,3",
        "preDrawTime": "2026-03-19 09:55:00",
        "sumValue": "17",
        "sizeLabel": "大",
        "parityLabel": "奇",
        "sizeParity": "大奇",
        "patternLabel": "杂六"
      }
    ],
    "total": 500,
    "pageNo": 1,
    "pageSize": 30
  }
}
```

### 字段说明

**外层字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | array | 历史开奖记录列表 |
| `total` | long | 总记录数 |
| `pageNo` | int | 当前页码 |
| `pageSize` | int | 每页条数 |

**list 每条记录字段：**（同接口一的 `preDrawXxx` 字段，含义相同）

| 字段 | 类型 | 说明 |
|------|------|------|
| `preDrawIssue` | string | 期号 |
| `preDrawCode` | string | 三个开奖号码，逗号分隔 |
| `preDrawTime` | string | 开奖时间 |
| `sumValue` | string | 和值 |
| `sizeLabel` | string | 大/小 |
| `parityLabel` | string | 奇/偶 |
| `sizeParity` | string | 大小+奇偶组合 |
| `patternLabel` | string | 号码形态 |

---

## 集成建议

### 轮询策略

PC28 每约 **5分钟**开一期，建议：

- **App 前台**：每 **30秒** 请求一次接口一（当期信息）
- **App 后台**：停止轮询，节省电量
- 对比 `drawIssue` 字段判断是否产生了新一期数据

### 防缓存

请求时必须带上时间戳参数 `t`，避免客户端或运营商缓存旧数据：

```
?lotCode=720&t=1742378400000
```

### 错误处理

| code | 建议处理 |
|------|---------|
| 200 | 正常解析 data |
| 500 | 提示"数据获取失败，请稍后重试"，3~5秒后重试 |

---

## 接口汇总

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 当期开奖信息 | GET | `/api/public/lottery/info?lotCode=720` | 最新一期结果 + 下期时间 |
| 历史开奖列表 | GET | `/api/public/lottery/list?lotCode=720` | 分页历史记录 |
