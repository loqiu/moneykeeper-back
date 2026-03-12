# Platform Frontend Handoff

这份文档给前端做页面开发用，范围是本轮 `Ledger / Budget / Search / Export Job / Notification / Statistics` 功能。

当前结论：

- 后端功能代码已经基本完成
- `codex/platform` 已在宿主机 `quant` 上做过真实联调
- 目前剩下的主要工作是把正式 `moneykeeper-backend` 切到共享主库 `moneykeeper` 并上线

原始全量接口底稿仍然是 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md)。
前端优先看这几个区段：

- 全局返回和错误规则：[FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L7)
- 通知模块：[FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L859)
- 账本模块总入口：[FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1454)

## 1. 页面范围建议

前端可以按下面这些页面拆：

- 账本列表 / 默认账本切换
- 账本成员与邀请管理
- 账本分类管理
- 账本记录列表 / 新增 / 编辑 / 删除 / 汇总
- 账本统计页
- 账本预算与阈值规则页
- 账本搜索页
- 导出任务中心
- 通知中心

## 2. 全局规则

### 2.1 认证

除登录、注册等白名单接口外，全部需要：

```http
Authorization: Bearer <token>
```

### 2.2 返回类型

这批接口会混用两种风格：

- `MkApiResponse<T>`：主要用于账本壳层接口，例如创建账本、账本列表、邀请
- `ResponseEntity<T>`：主要用于分类、记录、预算、通知、导出、统计

错误时通常返回 `ApiErrorResponse`：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "具体错误信息",
  "path": "/api/xxx",
  "timestamp": "2026-03-12T10:00:00",
  "traceId": "TRACE-20260312-0001"
}
```

### 2.3 常用错误码

- `400`：参数错误、请求体缺失、日期区间非法、类型不匹配
- `401`：JWT 缺失或失效
- `403`：已登录，但不是账本成员，或不是 owner/admin
- `404`：账本、分类、记录、预算、通知、导出任务不存在
- `409`：状态冲突，例如邀请已不可用、预算重复、导出任务未完成
- `500`：后端内部异常
- `503`：集成模块未启用或依赖未就绪，前端一般不会在正常页面路径上主动遇到

### 2.4 权限模型

- 平台 `admin`：可跨用户、跨账本访问
- 账本 `owner/admin`：可管理分类、预算、邀请，可修改所有记录
- 账本 `member`：可查看账本、可创建属于自己的记录、只能修改自己的记录
- 非成员：不能看账本数据

## 3. 核心返回对象

### 3.1 LedgerSummaryDTO

```json
{
  "id": 3,
  "name": "Home Budget",
  "type": "shared",
  "ownerUserId": 1,
  "memberRole": "owner",
  "defaultLedger": false
}
```

### 3.2 Category

```json
{
  "id": 12,
  "name": "Coffee",
  "icon": "coffee",
  "color": "#6F4E37",
  "type": "expense",
  "userId": 1,
  "ledgerId": 3,
  "createdAt": "2026-03-12T10:00:00",
  "updatedAt": "2026-03-12T10:00:00"
}
```

### 3.3 MoneyKeeper

```json
{
  "id": 100,
  "userId": 2,
  "ledgerId": 3,
  "categoryId": 12,
  "type": "expense",
  "amount": 12.50,
  "transactionDate": "2026-03-12",
  "notes": "Lunch",
  "createdAt": "2026-03-12T10:00:00",
  "updatedAt": "2026-03-12T10:00:00"
}
```

## 4. 账本壳层接口

这些接口的详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1475) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1639)。

### 4.1 创建账本

- `POST /api/ledgers`
- 返回：`MkApiResponse<LedgerSummaryDTO>`
- 请求体：

```json
{
  "name": "Family Ledger",
  "type": "shared"
}
```

规则：

- `type` 支持 `shared`、`family`、`project`
- 不传 `type` 时默认按 `shared` 处理
- 不能创建 `personal` 类型

常见错误：

- `400 Ledger name is required`
- `400 Ledger type must be one of: shared, family, project`

### 4.2 账本列表与默认账本

- `GET /api/ledgers`
- `GET /api/ledgers/default`
- 返回：`MkApiResponse<List<LedgerSummaryDTO>>` 或 `MkApiResponse<LedgerSummaryDTO>`

说明：

- 每个用户都有默认个人账本
- 个人账本会在首次访问时自动补齐

### 4.3 成员列表

- `GET /api/ledgers/{ledgerId}/members`
- 返回：`MkApiResponse<List<LedgerMemberDTO>>`

成员字段：

- `userId`
- `username`
- `email`
- `role`: `owner` / `admin` / `member`
- `status`: 目前主要是 `active`
- `joinedAt`

### 4.4 邀请

- `POST /api/ledgers/{ledgerId}/invites`
- `GET /api/ledgers/{ledgerId}/invites`
- `GET /api/ledgers/invites`
- `POST /api/ledgers/invites/{inviteCode}/accept`

创建邀请请求体：

```json
{
  "invitedEmail": "teammate@example.com",
  "role": "member",
  "expiresInDays": 7
}
```

规则：

- 个人账本不能分享
- `role` 只支持 `admin`、`member`
- 不传 `role` 默认 `member`
- `expiresInDays` 范围 `1-30`，默认 `7`
- 接受邀请时，当前登录用户邮箱必须和邀请邮箱一致

常见错误：

- `400 Personal ledgers cannot be shared`
- `400 Invite role must be one of: admin, member`
- `400 Invite expiry must be between 1 and 30 days`
- `403 You do not have permission to accept this invite`
- `404 Invite not found`
- `409 User is already a member of this ledger`

## 5. 分类接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1668) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1743)。

接口：

- `GET /api/ledgers/{ledgerId}/categories?type=expense`
- `GET /api/ledgers/{ledgerId}/categories/{categoryId}`
- `POST /api/ledgers/{ledgerId}/categories`
- `PUT /api/ledgers/{ledgerId}/categories/{categoryId}`
- `DELETE /api/ledgers/{ledgerId}/categories/{categoryId}`

创建请求体：

```json
{
  "name": "Coffee",
  "icon": "coffee",
  "color": "#6F4E37",
  "type": "expense"
}
```

规则：

- 只有 `owner/admin` 能增删改分类
- `member` 只能查看
- 创建成功后会直接回填 `id`

常见错误：

- `400 Category name is required`
- `400 Category icon is required`
- `400 Category color is required`
- `400 Category type is required`
- `403 You do not have permission to manage this ledger's categories`
- `404 Category not found`

## 6. 记录接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1752) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1842)。

接口：

- `GET /api/ledgers/{ledgerId}/records`
- `GET /api/ledgers/{ledgerId}/records/{recordId}`
- `POST /api/ledgers/{ledgerId}/records`
- `PUT /api/ledgers/{ledgerId}/records/{recordId}`
- `DELETE /api/ledgers/{ledgerId}/records/{recordId}`
- `GET /api/ledgers/{ledgerId}/records/summary`

列表查询参数：

- `userId`
- `categoryId`
- `type`
- `startDate`
- `endDate`

创建请求体：

```json
{
  "userId": 2,
  "categoryId": 12,
  "type": "expense",
  "amount": 23.45,
  "transactionDate": "2026-03-12",
  "notes": "Coffee beans"
}
```

更新请求体：五个字段都可选，但至少传一个。

规则：

- `type` 必须和分类 `type` 一致
- `amount` 必须大于 `0`
- `member` 默认只能创建自己的记录
- 平台 `admin` 创建时可显式传 `userId`
- `owner/admin` 可修改全部记录
- 普通 `member` 只能修改自己的记录

常见错误：

- `400 Category id is required`
- `400 Record type is required`
- `400 Amount must be greater than zero`
- `400 Transaction date is required`
- `400 Record type must match the selected category type`
- `400 Target user is not an active member of this ledger`
- `403 You do not have permission to modify this record`
- `404 Record not found`

异步说明：

- 记录创建、更新、删除后，会触发搜索索引刷新和预算阈值检查
- Kafka 开启时，这个过程是轻微异步的，前端要允许极短延迟

## 7. 统计接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1859)。

接口：

- `GET /api/ledgers/{ledgerId}/statistics`

查询参数：

- `period`: `week` / `month` / `year`
- `anchorDate`: `yyyy-MM-dd`
- `userId`: 可选，按账本成员过滤

返回重点字段：

- 总览：`totalIncome`、`totalExpense`、`balance`
- 数量：`recordCount`、`incomeRecordCount`、`expenseRecordCount`
- 对比：`incomeDelta`、`expenseDelta`、`balanceDelta`
- 变化率：`incomeChangePercentage`、`expenseChangePercentage`、`balanceChangePercentage`
- 趋势：`buckets`
- 分类占比：`expenseCategories`、`incomeCategories`

规则：

- `period` 默认 `month`
- `week` 以周一到周日为窗口
- `month` 以自然月
- `year` 以自然年

常见错误：

- `400 Period must be one of: week, month, year`
- `400 Target user is not an active member of this ledger`

## 8. 搜索接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1987)。

接口：

- `GET /api/ledgers/{ledgerId}/search/records`

查询参数：

- `userId`
- `query`
- `type`
- `categoryId`
- `categoryName`
- `startDate`
- `endDate`
- `limit`，范围 `1-100`，默认 `20`

返回字段：

- `id`
- `ledgerId`
- `userId`
- `categoryId`
- `categoryName`
- `type`
- `amount`
- `transactionDate`
- `updatedAt`
- `notes`
- `score`

常见错误：

- `400 End date cannot be before start date`
- `400 Limit must be between 1 and 100`
- `403 You do not have permission to search this ledger`

## 9. 预算接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L2048) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L2240)。

接口：

- `GET /api/ledgers/{ledgerId}/budgets`
- `GET /api/ledgers/{ledgerId}/budgets/{budgetId}`
- `POST /api/ledgers/{ledgerId}/budgets`
- `PUT /api/ledgers/{ledgerId}/budgets/{budgetId}`
- `DELETE /api/ledgers/{ledgerId}/budgets/{budgetId}`
- `POST /api/ledgers/{ledgerId}/budgets/{budgetId}/rules`
- `PUT /api/ledgers/{ledgerId}/budgets/{budgetId}/rules/{ruleId}`
- `DELETE /api/ledgers/{ledgerId}/budgets/{budgetId}/rules/{ruleId}`

预算请求体：

```json
{
  "name": "Coffee budget",
  "categoryId": 12,
  "type": "expense",
  "amount": 50.00,
  "budgetYear": 2026,
  "budgetMonth": 3,
  "notes": "March coffee"
}
```

规则请求体：

```json
{
  "thresholdPercentage": 80.00,
  "enabled": true,
  "notificationTitle": "Budget alert",
  "notificationMessage": "Coffee budget is almost used up"
}
```

预算规则：

- 目前只支持月预算，后端内部 `periodType` 固定为 `monthly`
- `type` 必须是 `income` 或 `expense`
- `budgetYear` 范围 `2000-2100`
- `budgetMonth` 范围 `1-12`
- 同一个 `ledger + 月份 + type + category scope` 不能重复
- `categoryId` 为空表示整类总预算，不为空表示分类预算
- 只有 `owner/admin` 可管理预算

规则说明：

- `thresholdPercentage` 范围 `(0, 200]`
- 记录变更后会自动重算预算
- 同一预算周期首次跨过阈值时，会自动写一条 `warning` 通知
- 如果后续回落到阈值以下，再次跨过时会重新提醒

返回重点字段：

- `progress.spentAmount`
- `progress.remainingAmount`
- `progress.usagePercentage`
- `progress.exceeded`
- `progress.triggeredThresholdPercentages`
- `rules`

常见错误：

- `400 Budget name is required`
- `400 Budget type is required`
- `400 Budget year is required`
- `400 Budget month is required`
- `400 Threshold percentage is required`
- `400 Threshold percentage must be between 0 and 200`
- `409 A budget already exists for the same month, type, and category scope`

## 10. 导出任务接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L2248) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L2333)。

接口：

- `POST /api/ledgers/{ledgerId}/export-jobs`
- `GET /api/ledgers/{ledgerId}/export-jobs`
- `GET /api/ledgers/{ledgerId}/export-jobs/{jobId}`
- `GET /api/ledgers/{ledgerId}/export-jobs/{jobId}/download`

创建请求体：

```json
{
  "userId": 2,
  "type": "expense",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31"
}
```

状态机：

- `pending`
- `running`
- `completed`
- `failed`

返回重点字段：

- `id`
- `status`
- `errorMessage`
- `recordCount`
- `downloadCount`
- `startedAt`
- `completedAt`
- `downloadUrl`

规则：

- 导出是异步任务，不要在前端假设创建后立刻可下载
- 创建后建议轮询 `GET /{jobId}` 或列表接口
- 完成后再调 `/download`
- 请求人只能看自己的导出任务，平台 `admin` 除外

常见错误：

- `400 End date cannot be before start date`
- `400 Target user is not an active member of this ledger`
- `403 You do not have permission to access this export job`
- `404 Export job not found`
- `409 Export job is still processing`
- `409 Export job failed`

## 11. 通知接口

详细原文在 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L859) 到 [FRONTEND_API.md](C:/WorkSpace/Java/moneykeeper-back/FRONTEND_API.md#L1011)。

前端页面常用的是这几组：

- `GET /api/notifications/logs`
- `GET /api/notifications/logs/unread-count`
- `GET /api/notifications/logs/{notificationId}`
- `PUT /api/notifications/logs/{notificationId}/read`
- `PUT /api/notifications/logs/read-all`
- `GET /api/notifications/subscribe/{userId}`，SSE

查询参数：

- `unreadOnly`
- `type`
- `limit`

通知类型：

- `success`
- `warning`
- `info`
- `error`
- `heartbeat`
- `connect`

建议前端重点关注：

- 导出完成通知：`info`
- 导出失败通知：`error`
- 预算阈值提醒：`warning`

SSE 说明：

- 订阅接口路径是 `/api/notifications/subscribe/{userId}`
- 只能订阅自己，平台 `admin` 例外
- SSE 主要用于实时推送，通知日志列表用于补历史和未读态

常见错误：

- `400 Unsupported message type: xxx`
- `403 You do not have permission to access this notification resource`
- `404 Notification log not found`

## 12. 前端联调建议

建议前端按这个顺序联调：

1. 登录，拿 token
2. 获取 `/api/ledgers` 和 `/api/ledgers/default`
3. 进入某个账本后，拉分类、记录、统计
4. 再补预算、导出任务、通知中心
5. 最后再做成员和邀请管理

如果页面首次上线需要最小闭环，我建议前端先做：

- 账本切换器
- 记录列表和创建弹窗
- 分类管理
- 统计卡片
- 预算列表
- 通知下拉框
- 导出任务列表

## 13. 这批功能是否可以直接开工

可以。

就当前代码状态来说，前端已经可以直接按文档做页面。后端剩下的不是“补接口”，而是“正式把 `moneykeeper-backend` 切到主库 `moneykeeper` 并上线”。
