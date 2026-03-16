# MoneyKeeper 前端联调文档

编码要求：本文件必须以 `UTF-8` 保存，禁止使用 `GB2312`、`GBK` 或其他本地编码。

本文档基于当前仓库代码整理，供前端联调使用。后端一旦修改接口、参数、错误码或权限规则，必须同步更新本文档。

## 1. 全局约定

### 1.1 服务地址

- 默认端口：`8081`
- 默认 Base URL：`http://{host}:8081`
- 业务接口统一前缀：`/api`

### 1.2 鉴权规则

以下接口不需要 JWT：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/google`
- `POST /api/payments/webhooks/stripe`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

除此之外，所有 `/api/**` 接口都需要：

```http
Authorization: Bearer <token>
```

### 1.3 角色规则

- `user`：普通用户，只能访问自己的用户、分类、记录、通知、导出、搜索、支付订单和订阅。
- `admin`：管理员，可以跨用户访问，并可使用管理类接口。

### 1.4 日期与金额格式

- 日期参数：`yyyy-MM-dd`
- 金额字段：十进制，例如 `99.99`
- `amountMinor`：最小货币单位，例如 `990` 代表 `9.90 GBP`

### 1.5 `type` 字段约定

- 后端标准枚举值只有两个：`income`、`expense`
- 前端做 i18 时，应该自行做文案映射，不要把中文文案直接当成请求枚举值
- 兼容旧请求时，后端会接受 `收入`、`支出` 并自动归一化为 `income`、`expense`
- 后端返回值统一只会返回 `income`、`expense`

### 1.6 三种返回形态

#### A. JWT 拦截器直接拒绝

未带 token、token 格式不对、token 无效或过期时，请求会在进入 controller 之前被拒绝：

```http
HTTP/1.1 401 Unauthorized
Content-Type: text/plain

Unauthorized
```

注意：这里不是 JSON。

#### B. `ApiErrorResponse`

`ResponseEntity` 风格接口，以及请求解析失败、参数类型错误、缺少必填参数等异常，会返回：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "具体错误信息",
  "path": "/api/xxx",
  "timestamp": "2026-03-10T02:00:00",
  "traceId": "TRACE-20260311-0001"
}
```

补充说明：`ApiErrorResponse` 中会携带 `traceId`，响应头也会返回同一个 `traceId`，前端记录报错时建议一并带上。

#### C. `MkApiResponse<T>`

认证、支付、Kafka 等模块返回：

```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {},
  "timestamp": "2026-03-10T02:00:00",
  "requestId": "REQ-1741572000000"
}
```

注意：

- `MkApiResponse` 需要同时判断 HTTP 状态码和 `code`
- 即使某些接口业务失败，HTTP 也可能仍然是 `200`，失败信息在 `code/message`
- 但如果 JSON 解析失败，这类接口也可能直接返回 `400 + ApiErrorResponse`

## 2. 统一错误码说明

### 2.1 常见 HTTP 状态

- `200`：成功
- `400`：参数错误、请求体错误、JSON 解析失败
- `401`：未认证或 token 无效
- `403`：已认证但无权限
- `404`：资源不存在
- `409`：状态冲突
- `500`：服务内部错误
- `503`：依赖未就绪或功能模块未启用

### 2.2 前端统一处理建议

前端建议按以下顺序处理响应：

1. 先看 HTTP 是否是 `401` 纯文本 `Unauthorized`
2. 再看是否是 `ApiErrorResponse`
3. 再看是否是 `MkApiResponse`
4. 对 `MkApiResponse` 还要继续判断 `code === 200`

## 3. 认证模块 `/api/auth`

### 3.1 登录

- Method：`POST`
- URL：`/api/auth/login`
- 认证：否

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

成功返回：`MkApiResponse<LoginResponse>`

```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {
    "userId": 1,
    "userPin": "MK123456",
    "username": "alice",
    "token": "jwt-token"
  }
}
```

失败：

- `code=400`：用户名或密码为空
- `code=401`：密码错误
- `code=404`：用户不存在
- `code=500`：登录失败

### 3.2 登出

- Method：`POST`
- URL：`/api/auth/logout`
- 认证：是

请求头：

```http
Authorization: Bearer <token>
```

成功返回：

```json
{
  "code": 200,
  "message": "Logout successful",
  "data": true
}
```

失败：

- HTTP `401` 纯文本：JWT 无效或缺失
- `code=401`：`Authorization` 头格式不正确，或 token 无效

### 3.3 注册

- Method：`POST`
- URL：`/api/auth/register`
- 认证：否

请求体：

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Zhang",
  "phoneNumber": "07123456789"
}
```

字段约束：

- `username`：必填，长度 `3-50`
- `password`：必填，长度 `6-255`
- `email`：必填，合法邮箱
- `firstName`：必填，长度不超过 `50`
- `lastName`：必填，长度不超过 `50`
- `phoneNumber`：可选，如传必须匹配 `10-11` 位数字

成功返回：`MkApiResponse<User>`

注意：`password` 字段是 write-only，不会出现在返回体里。

失败：

- `code=400`：字段校验失败
- `code=409`：用户名或邮箱重复
- `code=500`：注册失败

### 3.4 Google 登录

- Method：`POST`
- URL：`/api/auth/google`
- 认证：否

请求体：

```json
{
  "idToken": "google-id-token"
}
```

成功返回：`MkApiResponse<LoginResponse>`

失败：

- `code=400`：`idToken` 为空
- `code=401`：Google token 无效，或邮箱未验证
- `code=500`：Google 登录失败

## 4. 用户模块 `/api/users`

返回形态：`ResponseEntity<User>`，失败时返回 `ApiErrorResponse`

### 4.1 创建用户

- Method：`POST`
- URL：`/api/users`
- 认证：是
- 权限：仅 `admin`

请求体：

```json
{
  "username": "bob",
  "password": "123456",
  "email": "bob@example.com",
  "firstName": "Bob",
  "lastName": "Li",
  "phoneNumber": "07123456789",
  "role": "user"
}
```

字段约束：

- `username`：必填，长度 `3-50`
- `password`：必填，长度 `6-255`
- `email`：可选，如传必须为合法邮箱
- `phoneNumber`：可选，如传必须为 `10-11` 位数字
- `role`：可选，只能为 `user` 或 `admin`

失败：

- `400`：请求体为空、字段校验失败、用户名重复、邮箱重复
- `401`：未认证
- `403`：非管理员

### 4.2 根据 ID 查询用户

- Method：`GET`
- URL：`/api/users/{id}`
- 认证：是
- 权限：本人或管理员

失败：

- `401`：未认证
- `403`：不是本人且不是管理员
- `404`：用户不存在

### 4.3 根据用户名查询用户

- Method：`GET`
- URL：`/api/users/username/{username}`
- 认证：是
- 权限：本人或管理员

失败：

- `400`：用户名为空
- `403`：非本人且非管理员
- `404`：用户不存在

### 4.4 更新用户

- Method：`PUT`
- URL：`/api/users/{id}`
- 认证：是
- 权限：本人或管理员

请求体：全部字段可选

```json
{
  "username": "alice2",
  "password": "new-password",
  "email": "alice2@example.com",
  "firstName": "Alice",
  "lastName": "Wang",
  "phoneNumber": "07123456789",
  "role": "admin"
}
```

补充规则：

- 普通用户不能修改 `role`
- 只要传了字段，就会做对应校验
- 空字符串会被视为非法

失败：

- `400`：请求体为空、字段值非法、用户名重复、邮箱重复
- `403`：无权限，或普通用户试图改角色
- `404`：用户不存在

### 4.5 删除用户

- Method：`DELETE`
- URL：`/api/users/{id}`
- 认证：是
- 权限：本人或管理员

成功返回：HTTP `200` 空体

失败：

- `403`：无权限
- `404`：用户不存在

## 5. 分类模块 `/api/categories`

返回形态：`ResponseEntity<Category>` / `ResponseEntity<List<Category>>`

### 5.1 创建分类

- Method：`POST`
- URL：`/api/categories/{id}`
- 认证：是
- 权限：`id` 对应本人或管理员

请求体：

```json
{
  "name": "Food",
  "icon": "utensils",
  "color": "#ff6600",
  "type": "expense"
}
```

字段约束：

- `name`、`icon`、`color`、`type` 全部必填

注意：

- `type` 当前按业务使用 `income` / `expense`
- 当前创建成功后返回体里的 `id` 会回填，前端可以直接使用返回的 `id`

失败：

- `400`：请求体为空或字段为空
- `403`：无权限

### 5.2 分类详情

- `GET /api/categories/{id}`
- 权限：分类所属用户本人或管理员

失败：

- `403`：无权限
- `404`：分类不存在

### 5.3 按用户查询分类

- `GET /api/categories/user/{userId}`
- 权限：本人或管理员

### 5.4 按类型查询分类

- `GET /api/categories/type/{type}`
- 非管理员只能查自己

失败：

- `400`：`type` 为空

### 5.5 更新分类

- Method：`PUT`
- URL：`/api/categories/{id}`
- 权限：本人或管理员

请求体：字段全部可选，但至少传一个

```json
{
  "name": "Dining",
  "icon": "fork-knife",
  "color": "#ff9900",
  "type": "expense"
}
```

失败：

- `400`：请求体为空、所有字段都没传、或某个字段传了空字符串
- `403`：无权限
- `404`：分类不存在

### 5.6 删除分类

- `DELETE /api/categories/{id}`
- 权限：本人或管理员

成功返回：HTTP `200` 空体

失败：

- `403`：无权限
- `404`：分类不存在

### 5.7 查询全部分类

- `GET /api/categories/list`
- 非管理员只返回自己的分类

### 5.8 按用户和类型查询分类

- `GET /api/categories/user/{userId}/type/{type}`

失败：

- `400`：`type` 为空
- `403`：无权限

### 5.9 按类型查询分类，可选带用户过滤

- `GET /api/categories/list/{type}?userId=1`

规则：

- 管理员可选传 `userId`
- 普通用户如果传了别人的 `userId`，会返回 `403`

## 6. 记录模块 `/api/records`

返回形态：`ResponseEntity<MoneyKeeper>` / `ResponseEntity<List<...>>`

### 6.1 创建记录

- Method：`POST`
- URL：`/api/records`
- 认证：是

请求体：

```json
{
  "userId": 1,
  "categoryId": 10,
  "type": "expense",
  "amount": 12.5,
  "transactionDate": "2026-03-10",
  "notes": "Lunch"
}
```

规则：

- 普通用户即使传了 `userId`，也会按当前登录用户处理
- 管理员可以指定 `userId`
- `type` 必须与所选分类的 `type` 一致

字段约束：

- `categoryId`：必填
- `type`：必填
- `amount`：必填，且必须大于 `0`
- `transactionDate`：必填
- `notes`：可选

注意：

- 当前创建成功后返回体里的 `id` 会回填，前端可以直接使用返回的 `id` 做下一步

失败：

- `400`：参数缺失、金额小于等于 `0`、分类不存在、分类不属于目标用户、记录类型与分类类型不一致
- `401`：未认证

### 6.2 记录详情

- `GET /api/records/{id}`
- 权限：本人或管理员

失败：

- `403`：无权限
- `404`：记录不存在

### 6.3 按用户和日期范围查询记录

- `GET /api/records/user/{userId}?startDate=2026-03-01&endDate=2026-03-31`
- `startDate`、`endDate` 都是必填

失败：

- `400`：开始或结束日期缺失，或结束日期早于开始日期
- `403`：无权限

### 6.4 按用户和类型查询记录

- `GET /api/records/user/{userId}/type/{type}`

失败：

- `400`：`type` 为空
- `403`：无权限

### 6.5 更新记录

- Method：`PUT`
- URL：`/api/records/{id}`
- 权限：本人或管理员

请求体：至少传一个字段

```json
{
  "categoryId": 10,
  "type": "expense",
  "amount": 20,
  "transactionDate": "2026-03-10",
  "notes": "Dinner"
}
```

失败：

- `400`：请求体为空、没有任何更新字段、金额非法、分类非法、类型与分类类型不一致
- `403`：无权限
- `404`：记录不存在

### 6.6 删除记录

- `DELETE /api/records/{id}`

成功返回：HTTP `200` 空体

失败：

- `403`：无权限
- `404`：记录不存在

### 6.7 查询当前用户全部记录

- `GET /api/records/list`
- 非管理员只返回自己的记录

### 6.8 查询带分类名的记录

- `GET /api/records/listWithCategoryName/{userId}?startDate=2026-03-01&endDate=2026-03-31`

返回元素：`MoneyKeeperDTO`

- `id`
- `userId`
- `categoryId`
- `categoryName`
- `type`
- `amount`
- `transactionDate`
- `updatedAt`
- `notes`

### 6.9 按分类名称查询记录

- `GET /api/records/listByCategoryName/{categoryName}/{userId}?startDate=...&endDate=...`

失败：

- `400`：`categoryName` 为空，或日期范围非法
- `403`：无权限

### 6.10 按用户查询记录，可选过滤

- `GET /api/records/list/{userId}?type=expense&startDate=2026-03-01&endDate=2026-03-31`

### 6.11 汇总

- `GET /api/records/summary/{userId}?startDate=2026-03-01&endDate=2026-03-31`

返回：

```json
{
  "totalIncome": 1000,
  "totalExpense": 500,
  "balance": 500
}
```

失败：

- `400`：日期范围非法
- `403`：无权限

## 7. 搜索模块 `/api/search/records`

返回形态：`ResponseEntity`

### 7.1 搜索个人记录

- Method：`GET`
- URL：`/api/search/records`
- 认证：是

查询参数：

- `userId`：可选；普通用户不允许查询别人
- `query`：可选，全文关键字
- `type`：可选
- `categoryId`：可选
- `categoryName`：可选
- `startDate`：可选
- `endDate`：可选
- `limit`：可选，默认 `20`，范围 `1-100`

返回元素：`RecordSearchResultDTO`

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

失败：

- `400`：日期范围非法，或 `limit` 超范围
- `403`：普通用户查询他人数据
- `503`：Elasticsearch 模块关闭或未就绪

### 7.2 搜索账本记录

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/search/records`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `userId`：可选；按账本成员过滤
- `query`：可选，全文关键字
- `type`：可选
- `categoryId`：可选
- `categoryName`：可选
- `startDate`：可选
- `endDate`：可选
- `limit`：可选，默认 `20`，范围 `1-100`

返回元素：`RecordSearchResultDTO`

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

前端注意：

- 账本搜索依赖 Elasticsearch 索引里的 `ledgerId`
- 新创建、更新、删除的账本记录会自动同步到 Elasticsearch，正常联调无需每次手动执行重建索引
- 如果这是老环境升级后的首次使用，管理员应先调用 `POST /api/search/records/reindex` 或 `POST /api/search/records/reindex/ledger?ledgerId=...` 给历史索引补齐 `ledgerId`

失败：

- `400`：日期范围非法，或 `limit` 超范围
- `403`：无权限
- `503`：Elasticsearch 模块关闭或未就绪

### 7.3 全量或按用户重建索引

- Method：`POST`
- URL：`/api/search/records/reindex?userId=1`
- 权限：仅 `admin`

返回：`RecordSearchReindexResultDTO`

- `scope`
- `userId`
- `ledgerId`
- `indexedCount`
- `indexName`
- `reindexedAt`

失败：

- `403`：非管理员
- `503`：Elasticsearch 未就绪

### 7.4 按账本重建索引

- Method：`POST`
- URL：`/api/search/records/reindex/ledger?ledgerId=31`
- 权限：仅 `admin`

返回：`RecordSearchReindexResultDTO`

- `scope`：固定为 `ledger`
- `ledgerId`
- `indexedCount`
- `indexName`
- `reindexedAt`

失败：

- `400`：缺少 `ledgerId`
- `403`：非管理员
- `503`：Elasticsearch 未就绪

### 7.5 索引统计

- Method：`GET`
- URL：`/api/search/records/stats?userId=1`
- 权限：仅 `admin`

返回：`RecordSearchIndexStatsDTO`

- `scope`
- `userId`
- `ledgerId`
- `enabled`
- `ready`
- `indexName`
- `indexExists`
- `indexedDocumentCount`
- `databaseRecordCount`
- `statsCollectedAt`

失败：

- `403`：非管理员
- `503`：Elasticsearch 未就绪

### 7.6 按账本查看索引统计

- Method：`GET`
- URL：`/api/search/records/stats/ledger?ledgerId=31`
- 权限：仅 `admin`

返回：`RecordSearchIndexStatsDTO`

- `scope`：固定为 `ledger`
- `ledgerId`
- `enabled`
- `ready`
- `indexName`
- `indexExists`
- `indexedDocumentCount`
- `databaseRecordCount`
- `statsCollectedAt`

失败：

- `400`：缺少 `ledgerId`
- `403`：非管理员
- `503`：Elasticsearch 未就绪

## 8. Excel 导出 `/api/excel`

### 8.1 下载用户记录

- Method：`GET`
- URL：`/api/excel/download/{userId}`
- 认证：是
- 权限：本人或管理员

查询参数：

- `type`：可选，`income` / `expense`
- `startDate`：可选
- `endDate`：可选

返回：

- 文件流：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

失败：

- `400`：日期范围非法
- `403`：无权限
- `500`：文件生成失败

### 8.2 下载账本记录

- Method：`GET`
- URL：`/api/excel/ledgers/{ledgerId}/download`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `userId`：可选；按账本成员过滤
- `type`：可选，`income` / `expense`
- `startDate`：可选
- `endDate`：可选

返回：

- 文件流：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

失败：

- `400`：日期范围非法
- `403`：无权限
- `500`：文件生成失败

## 9. 通知模块 `/api/notifications`

### 9.1 SSE 订阅

- Method：`GET`
- URL：`/api/notifications/subscribe/{userId}`
- 认证：是
- 权限：本人或管理员
- 响应类型：`text/event-stream`

前端注意：

- 该接口仍然需要 `Authorization: Bearer <token>`
- 浏览器原生 `EventSource` 不能方便地自定义请求头
- 如果前端要接这个接口，建议使用支持自定义 header 的 SSE 方案、polyfill，或改用 `fetch` 流式方案

失败：

- HTTP `401` 纯文本：未认证
- `403`：无权限

### 9.2 自定义发送通知

- `POST /api/notifications/send/{userId}`

请求体：

```json
{
  "title": "Budget alert",
  "message": "You are near the limit",
  "type": "warning",
  "timestamp": 1741572000000
}
```

`type` 可选值：

- `success`
- `warning`
- `info`
- `error`
- `heartbeat`
- `connect`

失败：

- `400`：请求体为空、标题为空、消息为空、类型为空或不支持
- `403`：无权限

### 9.3 广播通知

- `POST /api/notifications/broadcast`
- 权限：仅 `admin`
- 请求体同上

失败：

- `400`：请求体非法
- `403`：非管理员

### 9.4 发送成功通知

- `POST /api/notifications/send/{userId}/success?title=xxx&message=xxx`

### 9.5 发送错误通知

- `POST /api/notifications/send/{userId}/error?title=xxx&message=xxx`

### 9.6 广播成功通知

- `POST /api/notifications/broadcast/success?title=xxx&message=xxx`
- 权限：仅 `admin`

### 9.7 广播错误通知

- `POST /api/notifications/broadcast/error?title=xxx&message=xxx`
- 权限：仅 `admin`

## 10. SSE 管理模块 `/api/notifications/manage`

返回形态：`ResponseEntity`

### 9.8 查询通知日志

- Method：`GET`
- URL：`/api/notifications/logs?unreadOnly=true&type=warning&limit=20`
- 认证：是
- 权限：当前登录用户

查询参数：

- `unreadOnly`：可选；`true` 时只返回未读通知
- `type`：可选；支持 `success` / `warning` / `info` / `error`
- `limit`：可选；默认 `20`，范围 `1-100`

成功返回：`NotificationLogDTO[]`

`NotificationLogDTO` 字段：

- `id`
- `userId`
- `title`
- `message`
- `type`
- `channel`：当前固定为 `sse`
- `status`：当前固定为 `sent`
- `read`
- `readAt`
- `createdAt`
- `updatedAt`

说明：

- 自定义发送、广播、余额预警、导出完成通知都会写入这里
- 广播通知会按用户展开成各自的日志记录，所以前端可以直接按“我的通知”展示

### 9.9 查询未读通知数量

- Method：`GET`
- URL：`/api/notifications/logs/unread-count?type=warning`
- 认证：是
- 权限：当前登录用户

成功返回：

```json
{
  "unreadCount": 3
}
```

### 9.10 查询单条通知日志

- Method：`GET`
- URL：`/api/notifications/logs/{notificationId}`
- 认证：是
- 权限：当前登录用户

失败：

- `404`：通知不存在，或不属于当前用户

### 9.11 标记单条通知为已读

- Method：`PUT`
- URL：`/api/notifications/logs/{notificationId}/read`
- 认证：是
- 权限：当前登录用户

成功返回：更新后的 `NotificationLogDTO`

### 9.12 全部标记为已读

- Method：`PUT`
- URL：`/api/notifications/logs/read-all?type=warning`
- 认证：是
- 权限：当前登录用户

查询参数：

- `type`：可选；如果传了，只会标记该类型通知

成功返回：

```json
{
  "markedCount": 5
}
```
### 10.1 查看连接信息

- `GET /api/notifications/manage/connections`
- 权限：仅 `admin`

返回：

```json
{
  "connectedUsers": [1, 2],
  "totalConnections": 2
}
```

### 10.2 检查某个用户是否连接

- `GET /api/notifications/manage/check/{userId}`
- 权限：本人或管理员

### 10.3 手动断开连接

- `POST /api/notifications/manage/disconnect/{userId}`
- 权限：本人或管理员

### 10.4 连接统计

- `GET /api/notifications/manage/stats`
- 权限：仅 `admin`

## 11. 支付模块 `/api/payments`

返回形态：`MkApiResponse`

### 11.1 支付状态

- Method：`GET`
- URL：`/api/payments/status`
- 认证：是

返回字段：

- `enabled`
- `provider`
- `defaultCurrency`
- `implemented`
- `apiReady`
- `webhookReady`
- `ready`
- `billingPortalReturnUrlConfigured`

前端使用建议：

- 只有 `data.ready === true` 时，才允许点击订阅

### 11.2 套餐列表

- `GET /api/payments/plans`

返回：`PaymentPlanDTO[]`

- `code`
- `name`
- `description`
- `currency`
- `amountMinor`
- `billingInterval`

### 11.3 订单列表

- `GET /api/payments/orders`
- 返回当前登录用户自己的订单

返回元素：`PaymentOrderDTO`

- `orderNo`
- `planCode`
- `planName`
- `orderType`
- `status`
- `currency`
- `amountMinor`
- `stripeCheckoutSessionId`
- `stripeInvoiceId`
- `stripeSubscriptionId`
- `paidAt`
- `createdAt`
- `updatedAt`

常见 `status`：

- `pending`
- `checkout_created`
- `paid`
- `payment_failed`
- `canceled`
- `refunded`

### 11.4 当前订阅

- `GET /api/payments/subscriptions/current`

返回：`PaymentSubscriptionDTO`

无订阅时返回：

```json
{
  "active": false,
  "status": "none",
  "cancelAtPeriodEnd": false
}
```

有订阅时字段包括：

- `active`
- `status`
- `planCode`
- `planName`
- `currency`
- `amountMinor`
- `billingInterval`
- `currentPeriodStart`
- `currentPeriodEnd`
- `cancelAtPeriodEnd`
- `canceledAt`
- `stripeSubscriptionId`

### 11.5 创建 PaymentIntent

- `POST /api/payments/intents`

请求体：

```json
{
  "amount": 990,
  "currency": "gbp"
}
```

说明：当前未实现，保留占位。

失败：

- `code=400`：参数缺失或金额不合法
- `code=501`：未实现，请改用 hosted checkout
- `code=503`：支付模块未启用

### 11.6 确认 PaymentIntent

- `POST /api/payments/intents/{paymentIntentId}/confirm`
- 当前未实现

### 11.7 取消 PaymentIntent

- `POST /api/payments/intents/{paymentIntentId}/cancel`
- 当前未实现

### 11.8 创建 Checkout Session

- Method：`POST`
- URL：`/api/payments/checkout-sessions`
- 认证：是

请求体：

```json
{
  "planCode": "pro_monthly",
  "successUrl": "http://your-frontend.example.com/billing/success",
  "cancelUrl": "http://your-frontend.example.com/billing/cancel"
}
```

字段约束：

- `planCode`：必填
- `successUrl`：必填，必须是绝对 `http(s)` URL
- `cancelUrl`：必填，必须是绝对 `http(s)` URL

成功返回：`PaymentCheckoutSessionDTO`

- `orderNo`
- `planCode`
- `checkoutSessionId`
- `checkoutUrl`
- `status`

失败：

- `code=400`：请求体为空、套餐编码为空、成功页地址非法、取消页地址非法
- `code=404`：套餐不存在，或当前用户不存在
- `code=409`：用户已有受管订阅，应改走 billing portal
- `code=500`：创建 Stripe Checkout Session 失败
- `code=503`：支付模块未启用、Stripe secret 未配置、webhook secret 未配置

前端注意：

- 前端不要自己拼 Stripe 链接
- 只能使用返回的 `data.checkoutUrl`
- 成功页回跳不等于支付成功，必须回查订单或订阅状态

### 11.9 取消当前订阅

- Method：`POST`
- URL：`/api/payments/subscriptions/current/cancel`

成功语义：

- 不是立刻删除订阅
- 是设置 `cancelAtPeriodEnd=true`

失败：

- `code=404`：当前订阅不存在
- `code=409`：订阅已经是非活跃状态
- `code=500`：取消失败
- `code=503`：支付 API 未就绪

### 11.10 创建 Billing Portal Session

- Method：`POST`
- URL：`/api/payments/billing-portal-sessions`

请求体：

```json
{
  "returnUrl": "http://your-frontend.example.com/account/billing"
}
```

说明：

- 请求体可省略
- 如果不传 `returnUrl`，后端会尝试使用服务端配置的默认回跳地址

成功返回：

```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {
    "url": "https://billing.stripe.com/..."
  }
}
```

失败：

- `code=400`：`returnUrl` 非法
- `code=404`：当前用户没有 Stripe customer
- `code=500`：创建 billing portal 失败
- `code=503`：支付 API 未就绪

### 11.11 Stripe Webhook

- Method：`POST`
- URL：`/api/payments/webhooks/stripe`
- 认证：否
- 调用方：仅 Stripe
- Content-Type：`application/json`
- Header：`Stripe-Signature`

返回：

- `200 ok`：处理成功
- `400`：payload 缺失、签名缺失、签名错误、payload 非法
- `500`：webhook 处理失败
- `503`：支付模块关闭或 webhook secret 未配置

前端绝对不要调用这个接口。

## 12. Kafka 模块 `/api/kafka`

返回形态：`MkApiResponse`

全部接口仅 `admin` 可用。

### 12.1 发送消息

- `POST /api/kafka/send?topic=test&key=message&message=hello`

失败：

- `code=400`：`message` 为空
- `code=403`：非管理员
- `code=503`：Kafka 未启用
- `code=500`：发送失败

### 12.2 调试监听

- `GET /api/kafka/listen?topic=test&key=message&message=hello`

说明：本质上是调试代理，并不是真正的消费接口。

### 12.3 Kafka 状态

- `GET /api/kafka/status`

返回字段：

- `enabled`
- `consumerEnabled`
- `consumedCount`
- `implemented`

### 12.4 最近消息

- `GET /api/kafka/messages?limit=20`

约束：

- `limit` 默认 `20`
- 范围 `1-100`

返回元素：`KafkaMessageRecord`

- `topic`
- `key`
- `value`
- `receivedAt`

## 13. 集成状态 `/api/integrations`

返回形态：`ResponseEntity`

全部接口仅 `admin` 可用。

### 13.1 全部模块状态

- `GET /api/integrations/status`

返回元素：`IntegrationModuleStatusDTO`

- `module`
- `enabled`
- `ready`
- `implemented`
- `summary`
- `metadata`

### 13.2 单个模块状态

- `GET /api/integrations/status/{module}`

失败：

- `403`：非管理员
- `404`：模块不存在

## 14. 前端支付改造清单

前端接支付时，必须按下面流程改：

1. 不要再写死 Stripe Payment Link。
2. 进入订阅页先调用：
   - `GET /api/payments/status`
   - `GET /api/payments/plans`
   - `GET /api/payments/subscriptions/current`
3. 只有 `status.data.ready === true` 时才允许购买。
4. 点击购买时调用：

```json
POST /api/payments/checkout-sessions
{
  "planCode": "pro_monthly",
  "successUrl": "http://你的前端地址/billing/success",
  "cancelUrl": "http://你的前端地址/billing/cancel"
}
```

5. 前端收到 `data.checkoutUrl` 后直接跳转 Stripe Hosted Checkout。
6. 成功页不要本地直接把会员状态改成已开通。
7. 从 Stripe 成功页跳回后，轮询：
   - `GET /api/payments/subscriptions/current`
   - `GET /api/payments/orders`
8. 轮询建议：
   - 每 `2` 秒一次
   - 最多 `10` 次
   - 当 `subscription.status === "active"` 或最新订单 `status === "paid"` 时停止
9. 如果用户已有有效订阅：
   - 管理订阅：`POST /api/payments/billing-portal-sessions`
   - 取消订阅：`POST /api/payments/subscriptions/current/cancel`
10. 前端不要调用 `/api/payments/webhooks/stripe`。

当前测试套餐：

- `planCode = pro_monthly`
- `currency = gbp`
- `amountMinor = 990`

## 15. 已知联调注意点

### 15.1 SSE 需要自定义鉴权头

`/api/notifications/subscribe/{userId}` 需要 JWT。浏览器原生 `EventSource` 不方便携带 `Authorization` 头，前端请提前确认 SSE 方案。

### 15.2 创建分类和记录后可以直接使用返回 `id`

当前创建分类、创建记录已开启 generated keys，返回体里的 `id` 会回填。前端可以直接把“创建成功响应的 `id`”用于下一步请求。

### 15.3 `MkApiResponse` 与 `ApiErrorResponse` 会混用

尤其是支付模块：

- 正常业务分支一般返回 `MkApiResponse`
- 但请求体 JSON 格式错误时，会直接返回 `400 + ApiErrorResponse`

前端请不要只按一种 JSON 结构写死解析。
### 15.4 生产环境域名建议

当前推荐的生产环境访问方式：

- REST API：`https://api.money-keeper.com/api`
- SSE：`https://money-keeper.com/api/notifications/subscribe/{userId}`

说明：

- 普通 REST 请求走 `api` 子域名直连。
- SSE 保持走主域名同域 `/api` 反代，继续使用支持自定义请求头的 SSE client，并携带 `Authorization: Bearer <token>`。
- 如果生产环境主域名代理 SSE，请确保该链路支持流式转发，且不要缓存、不要缓冲响应。

## 16. 账本模块 `/api/ledgers`

返回形态：`MkApiResponse`

当前这一批账本接口已经覆盖：

- 账本创建
- 账本成员查看
- 邀请与接受邀请
- 账本维度分类管理
- 账本维度记录管理与汇总
- 账本维度搜索索引
- 账本维度周期统计
- 账本维度预算与阈值提醒
- 账本维度导出任务

前端注意：

- 个人账本仍可继续使用原有 `/api/categories` 和 `/api/records`
- 共享账本场景请改用 `/api/ledgers/{ledgerId}/...` 这组嵌套路由

### 16.1 创建账本

- Method：`POST`
- URL：`/api/ledgers`
- 认证：是

请求体：

```json
{
  "name": "Family Ledger",
  "type": "family"
}
```

字段约束：

- `name`：必填
- `type`：可选，默认 `shared`
- `type` 允许值：`shared`、`family`、`project`
- 不允许手工创建 `personal` 账本

成功返回：`MkApiResponse<LedgerSummaryDTO>`

```json
{
  "code": 200,
  "message": "Ledger created",
  "data": {
    "id": 31,
    "name": "Family Ledger",
    "type": "family",
    "ownerUserId": 1,
    "memberRole": "owner",
    "defaultLedger": false
  }
}
```

说明：

- 创建成功后，创建人会自动成为该账本的 `owner`

### 16.2 我的账本列表

- Method：`GET`
- URL：`/api/ledgers`
- 认证：是

返回：当前用户参与的全部账本，元素为 `LedgerSummaryDTO`

字段：

- `id`
- `name`
- `type`
- `ownerUserId`
- `memberRole`
- `defaultLedger`

### 16.3 默认个人账本

- Method：`GET`
- URL：`/api/ledgers/default`
- 认证：是

返回：当前用户的默认个人账本 `LedgerSummaryDTO`

### 16.4 账本成员列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/members`
- 认证：是
- 权限：账本成员或平台 `admin`

返回元素：`LedgerMemberDTO`

- `userId`
- `username`
- `email`
- `role`
- `status`
- `joinedAt`

当前 `role` 可能值：

- `owner`
- `admin`
- `member`

### 16.5 创建邀请

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/invites`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：

```json
{
  "invitedEmail": "bob@example.com",
  "role": "member",
  "expiresInDays": 7
}
```

字段约束：

- `invitedEmail`：必填
- `role`：可选，默认 `member`
- `role` 允许值：`admin`、`member`
- `expiresInDays`：可选，默认 `7`，范围 `1-30`

成功返回：`MkApiResponse<LedgerInviteDTO>`

字段：

- `id`
- `ledgerId`
- `ledgerName`
- `invitedByUserId`
- `invitedEmail`
- `inviteCode`
- `role`
- `status`
- `expiresAt`
- `acceptedAt`
- `createdAt`

失败：

- `400`：请求体为空、邮箱为空、角色非法、过期天数超范围、尝试分享 `personal` 账本
- `403`：无邀请权限
- `404`：账本不存在
- `409`：目标邮箱对应用户已经是该账本成员

### 16.6 查看账本邀请列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/invites`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

返回元素：`LedgerInviteDTO`

说明：

- 过期的待处理邀请会显示为 `status=expired`

### 16.7 查看我的待接受邀请

- Method：`GET`
- URL：`/api/ledgers/invites`
- 认证：是

返回：当前用户邮箱匹配到的待处理邀请列表，元素为 `LedgerInviteDTO`

前端注意：

- 这个接口按“当前账号邮箱”匹配邀请
- 如果用户账号没有邮箱，返回空列表
- 接受邀请前，前端应提示用户确认当前登录邮箱与受邀邮箱一致

### 16.8 接受邀请

- Method：`POST`
- URL：`/api/ledgers/invites/{inviteCode}/accept`
- 认证：是

成功返回：`MkApiResponse<LedgerSummaryDTO>`

```json
{
  "code": 200,
  "message": "Invite accepted",
  "data": {
    "id": 31,
    "name": "Family Ledger",
    "type": "family",
    "ownerUserId": 1,
    "memberRole": "member",
    "defaultLedger": false
  }
}
```

失败：

- `400`：邀请码为空、邀请码已过期、邀请码已失效
- `403`：当前登录用户邮箱与受邀邮箱不一致
- `404`：邀请码不存在，或账本不存在

### 16.9 账本分类列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/categories?type=expense`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `type`：可选，`income` / `expense`

返回：`Category[]`

说明：

- 返回的 `Category` 会带 `ledgerId`
- 共享账本前端应优先基于这组分类接口渲染分类选择器

### 16.10 账本分类详情

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/categories/{categoryId}`
- 认证：是
- 权限：账本成员或平台 `admin`

失败：

- `403`：无权限
- `404`：账本或分类不存在，或分类不属于该账本

### 16.11 创建账本分类

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/categories`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：

```json
{
  "name": "Transport",
  "icon": "car",
  "color": "#3B82F6",
  "type": "expense"
}
```

规则：

- 当前创建人会写入返回体里的 `userId`
- 分类归属账本以路径中的 `ledgerId` 为准
- 普通 `member` 当前不能新增或维护共享账本分类

失败：

- `400`：请求体为空、字段为空
- `403`：无权限
- `404`：账本不存在

### 16.12 更新账本分类

- Method：`PUT`
- URL：`/api/ledgers/{ledgerId}/categories/{categoryId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：字段全部可选，但至少传一个

失败：

- `400`：请求体为空、没有更新字段、或字段为空字符串
- `403`：无权限
- `404`：账本或分类不存在

### 16.13 删除账本分类

- Method：`DELETE`
- URL：`/api/ledgers/{ledgerId}/categories/{categoryId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

成功返回：HTTP `200` 空体

### 16.14 账本记录列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/records?userId=2&categoryId=8&type=expense&startDate=2026-03-01&endDate=2026-03-31`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `userId`：可选，按成员过滤
- `categoryId`：可选，按分类过滤
- `type`：可选，`income` / `expense`
- `startDate`：可选
- `endDate`：可选

返回：`MoneyKeeper[]`

### 16.15 账本记录详情

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/records/{recordId}`
- 认证：是
- 权限：账本成员或平台 `admin`

失败：

- `403`：无权限
- `404`：账本或记录不存在，或记录不属于该账本

### 16.16 创建账本记录

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/records`
- 认证：是
- 权限：账本成员或平台 `admin`

请求体：

```json
{
  "userId": 2,
  "categoryId": 8,
  "type": "expense",
  "amount": 18.5,
  "transactionDate": "2026-03-11",
  "notes": "Team lunch"
}
```

规则：

- 普通用户即使传了 `userId`，也会按当前登录用户处理
- 平台 `admin` 可以指定 `userId`，但目标用户必须是该账本有效成员
- `categoryId` 必须属于当前账本
- `type` 必须与所选分类的 `type` 一致
- 如果部署环境开启了 Kafka 记录事件链路，搜索索引和预算提醒会异步处理，前端可能看到几百毫秒到几秒的轻微延迟

失败：

- `400`：参数缺失、金额非法、分类不属于该账本、记录类型与分类类型不一致、目标用户不是账本成员
- `403`：无权限
- `404`：账本或分类不存在

### 16.17 更新账本记录

- Method：`PUT`
- URL：`/api/ledgers/{ledgerId}/records/{recordId}`
- 认证：是
- 权限：
  - 账本 `owner` / `admin` 可修改任意记录
  - 普通 `member` 只能修改自己创建的记录
  - 平台 `admin` 可直接修改

请求体：字段全部可选，但至少传一个

失败：

- `400`：请求体为空、没有更新字段、金额非法、分类不属于该账本、类型与分类类型不一致
- `403`：无权限
- `404`：账本、记录或分类不存在

### 16.18 删除账本记录

- Method：`DELETE`
- URL：`/api/ledgers/{ledgerId}/records/{recordId}`
- 认证：是
- 权限规则同 16.17

成功返回：HTTP `200` 空体

### 16.19 账本记录汇总

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/records/summary?startDate=2026-03-01&endDate=2026-03-31`
- 认证：是
- 权限：账本成员或平台 `admin`

返回：`RecordSummary`

```json
{
  "totalIncome": 1000,
  "totalExpense": 500,
  "balance": 500
}
```

### 16.20 账本周期统计

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/statistics?period=month&anchorDate=2026-03-12&userId=2`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `period`：可选；`week` / `month` / `year`，默认 `month`
- `anchorDate`：可选；统计锚点日期，默认今天
- `userId`：可选；按账本成员过滤

返回：`LedgerStatisticsDTO`

主要字段：

- `ledgerId`
- `userId`：如果当前查询没有按成员过滤，则为 `null`
- `period`
- `bucketGranularity`：`day` / `month`
- `anchorDate`
- `startDate`
- `endDate`
- `previousStartDate`
- `previousEndDate`
- `totalIncome`
- `totalExpense`
- `balance`
- `recordCount`
- `incomeRecordCount`
- `expenseRecordCount`
- `incomeDelta`
- `expenseDelta`
- `balanceDelta`
- `incomeChangePercentage`
- `expenseChangePercentage`
- `balanceChangePercentage`
- `buckets`
- `expenseCategories`
- `incomeCategories`

`buckets` 元素字段：

- `bucketKey`
- `label`
- `startDate`
- `endDate`
- `totalIncome`
- `totalExpense`
- `balance`
- `recordCount`

`expenseCategories` / `incomeCategories` 元素字段：

- `categoryId`
- `categoryName`
- `type`
- `totalAmount`
- `percentage`
- `recordCount`

说明：

- `week` / `month` 会按天分桶，`year` 会按月分桶
- `incomeChangePercentage` / `expenseChangePercentage` / `balanceChangePercentage` 在上一周期为 `0` 且当前周期非 `0` 时会返回 `null`
- 如果传了 `userId`，该用户必须是当前账本的有效成员

示例返回：

```json
{
  "ledgerId": 31,
  "userId": 2,
  "period": "month",
  "bucketGranularity": "day",
  "anchorDate": "2026-03-12",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "previousStartDate": "2026-02-01",
  "previousEndDate": "2026-02-28",
  "totalIncome": 200,
  "totalExpense": 60,
  "balance": 140,
  "recordCount": 4,
  "incomeRecordCount": 1,
  "expenseRecordCount": 3,
  "incomeDelta": 50,
  "expenseDelta": 45,
  "balanceDelta": 5,
  "incomeChangePercentage": 33.33,
  "expenseChangePercentage": 300.0,
  "balanceChangePercentage": 3.7,
  "buckets": [
    {
      "bucketKey": "2026-03-10",
      "label": "03-10",
      "startDate": "2026-03-10",
      "endDate": "2026-03-10",
      "totalIncome": 200,
      "totalExpense": 10,
      "balance": 190,
      "recordCount": 2
    }
  ],
  "expenseCategories": [
    {
      "categoryId": 9,
      "categoryName": "Coffee",
      "type": "expense",
      "totalAmount": 40,
      "percentage": 66.67,
      "recordCount": 2
    }
  ],
  "incomeCategories": [
    {
      "categoryId": 10,
      "categoryName": "Salary",
      "type": "income",
      "totalAmount": 200,
      "percentage": 100,
      "recordCount": 1
    }
  ]
}
```

### 16.21 账本记录搜索

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/search/records?userId=2&query=lunch&type=expense&startDate=2026-03-01&endDate=2026-03-31`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `userId`：可选，按账本成员过滤
- `query`：可选，全文关键字
- `type`：可选
- `categoryId`：可选
- `categoryName`：可选
- `startDate`：可选
- `endDate`：可选
- `limit`：可选，默认 `20`，范围 `1-100`

返回元素：`RecordSearchResultDTO`

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

前端注意：

- 账本记录正常写入后会自动同步搜索索引，通常不需要前端额外触发重建
- 如果部署环境开启了 Kafka 记录事件链路，搜索结果会有轻微异步延迟；前端不要假设“创建记录后搜索一定同步可见”
- 只有老环境历史数据补齐或索引异常修复时，才需要管理员调用重建接口

### 16.22 导出账本记录 Excel

- Method：`GET`
- URL：`/api/excel/ledgers/{ledgerId}/download?userId=2&type=expense&startDate=2026-03-01&endDate=2026-03-31`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `userId`：可选，按账本成员过滤
- `type`：可选，`income` / `expense`
- `startDate`：可选
- `endDate`：可选

返回：

- 文件流：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

失败：

- `400`：日期范围非法
- `403`：无权限
- `404`：账本不存在
### 16.23 账本预算列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/budgets?year=2026&month=3&type=expense&categoryId=8`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `year`：可选；按预算年份过滤
- `month`：可选；按预算月份过滤
- `type`：可选，`income` / `expense`
- `categoryId`：可选；按账本分类过滤

返回：`LedgerBudgetDTO[]`

`LedgerBudgetDTO` 主要字段：

- `id`
- `ledgerId`
- `createdByUserId`
- `categoryId`
- `categoryName`
- `name`
- `periodType`：当前固定为 `monthly`
- `budgetYear`
- `budgetMonth`
- `startDate`
- `endDate`
- `type`
- `amount`
- `notes`
- `createdAt`
- `updatedAt`
- `progress`
- `rules`

`progress` 字段：

- `spentAmount`
- `remainingAmount`
- `usagePercentage`
- `exceeded`
- `triggeredThresholdPercentages`

前端注意：

- 当前预算基础层只支持“月度预算”
- 列表和详情都会直接返回预算进度与阈值规则，无需额外再拼一次进度接口

### 16.24 账本预算详情

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}`
- 认证：是
- 权限：账本成员或平台 `admin`

返回：`LedgerBudgetDTO`

失败：

- `403`：无权限
- `404`：账本或预算不存在，或预算不属于该账本

### 16.25 创建账本预算

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/budgets`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：

```json
{
  "name": "March Coffee Budget",
  "categoryId": 8,
  "type": "expense",
  "amount": 200,
  "budgetYear": 2026,
  "budgetMonth": 3,
  "notes": "Team coffee"
}
```

规则：

- `name`：必填
- `type`：必填，只允许 `income` / `expense`
- `amount`：必填，必须大于 `0`
- `budgetYear`：必填，范围 `2000-2100`
- `budgetMonth`：必填，范围 `1-12`
- `categoryId`：可选；如果传了，必须属于当前账本，且分类 `type` 要与预算 `type` 一致
- 当前会自动生成该月的 `startDate` / `endDate`
- 同一个账本下，同月 + 同类型 + 同分类范围的预算不能重复创建

成功返回：`LedgerBudgetDTO`

失败：

- `400`：参数缺失、金额非法、年月非法、分类不属于该账本、预算类型与分类类型不一致
- `403`：无权限
- `404`：账本不存在
- `409`：相同范围预算已存在

### 16.26 更新账本预算

- Method：`PUT`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：字段全部可选，但至少传一个

说明：

- 更新 `budgetYear` / `budgetMonth` 后，会自动重算该预算的 `startDate` / `endDate`
- 当前 `categoryId` 只支持更新为另一个分类，不支持通过 `null` 主动清空成“无分类范围”

失败：

- `400`：请求体为空、没有更新字段、金额非法、年月非法、分类不属于该账本、预算类型与分类类型不一致
- `403`：无权限
- `404`：账本或预算不存在
- `409`：更新后与现有预算范围冲突

### 16.27 删除账本预算

- Method：`DELETE`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

成功返回：HTTP `200` 空体

### 16.28 新增预算阈值规则

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}/rules`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：

```json
{
  "thresholdPercentage": 80,
  "enabled": true,
  "notificationTitle": "Budget alert",
  "notificationMessage": "Monthly budget is almost used up"
}
```

规则：

- `thresholdPercentage`：必填，范围 `(0, 200]`
- `enabled`：可选，默认 `true`
- 当前规则类型固定为 `threshold`
- 阈值规则会在账本记录新增、更新、删除后自动重新评估
- 同一预算规则在同一预算周期内首次跨过阈值时会自动写入通知日志；如果后续因改记录回落到阈值以下，再次跨过会重新提醒
- 自动提醒会出现在 `/api/notifications/logs`，消息类型为 `warning`
- 如果部署环境开启了 Kafka 记录事件链路，提醒写入也会通过异步消费完成，前端不要依赖“保存记录后本次请求里立刻拿到提醒”

成功返回：`BudgetRuleDTO`

`BudgetRuleDTO` 字段：

- `id`
- `budgetId`
- `ruleType`
- `thresholdPercentage`
- `enabled`
- `notificationTitle`
- `notificationMessage`
- `createdAt`
- `updatedAt`

### 16.29 更新预算阈值规则

- Method：`PUT`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}/rules/{ruleId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

请求体：字段全部可选，但至少传一个

失败：

- `400`：请求体为空、没有更新字段、阈值非法
- `403`：无权限
- `404`：账本、预算或规则不存在

### 16.30 删除预算阈值规则

- Method：`DELETE`
- URL：`/api/ledgers/{ledgerId}/budgets/{budgetId}/rules/{ruleId}`
- 认证：是
- 权限：账本 `owner` / `admin`，或平台 `admin`

成功返回：HTTP `200` 空体
### 16.31 创建账本导出任务

- Method：`POST`
- URL：`/api/ledgers/{ledgerId}/export-jobs`
- 认证：是
- 权限：账本成员或平台 `admin`

请求体：全部字段可选

```json
{
  "userId": 2,
  "type": "expense",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31"
}
```

规则：

- `userId`：可选；按账本成员过滤
- `type`：可选；`income` / `expense`
- `startDate` / `endDate`：可选；若同时传，结束日期不能早于开始日期
- 当前实现会先创建 `pending` 状态任务，由后端异步 worker 生成导出文件
- 如果部署环境开启了 Kafka 导出事件链路，任务会优先由 Kafka consumer 触发处理；如果 Kafka 不可用，scheduler 仍会兜底继续处理 `pending` 任务
- 创建成功后，系统会先给创建人写入一条 `info` 通知日志，提示导出任务已入队
- 任务完成后，系统会再写一条 `info` 通知日志，提示导出已可下载
- 如果异步生成失败，任务会变成 `failed`，并给创建人写一条 `error` 通知日志

成功返回：`ExportJobDTO`

`ExportJobDTO` 字段：

- `id`
- `ledgerId`
- `requestedByUserId`
- `targetUserId`
- `recordType`
- `startDate`
- `endDate`
- `fileName`
- `fileFormat`：当前固定为 `xlsx`
- `status`：`pending` / `running` / `completed` / `failed`
- `errorMessage`：任务失败时的错误信息；成功任务通常为 `null`
- `recordCount`
- `downloadCount`
- `startedAt`
- `completedAt`
- `lastDownloadedAt`
- `createdAt`
- `updatedAt`
- `downloadUrl`

### 16.32 查询账本导出任务列表

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/export-jobs?limit=20`
- 认证：是
- 权限：账本成员或平台 `admin`

查询参数：

- `limit`：可选；默认 `20`，范围 `1-100`

说明：

- 普通账本成员只会看到自己创建的导出任务
- 平台 `admin` 可以看到当前账本下的全部导出任务
- 前端建议轮询列表或单任务详情，直到 `status=completed` 再开放下载按钮
- `status=failed` 时，可以直接展示 `errorMessage`

成功返回：`ExportJobDTO[]`

### 16.33 查询单个账本导出任务

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/export-jobs/{jobId}`
- 认证：是
- 权限：任务创建人，或平台 `admin`

失败：

- `403`：不是任务创建人且不是平台 `admin`
- `404`：账本或任务不存在

### 16.34 下载账本导出任务文件

- Method：`GET`
- URL：`/api/ledgers/{ledgerId}/export-jobs/{jobId}/download`
- 认证：是
- 权限：任务创建人，或平台 `admin`
- 返回：Excel 二进制文件（`attachment`）

说明：

- 只有 `status=completed` 的任务可以下载
- `status=pending` / `running` 时会返回 `409`，提示任务仍在处理中
- `status=failed` 时会返回 `409`，并尽量带上失败原因
- 成功下载后，任务的 `downloadCount` 会自增
- 成功下载后，任务的 `lastDownloadedAt` 会更新
- 如果前端只想“直接下载”，仍可继续用老的 `/api/excel/ledgers/{ledgerId}/download`
- 如果前端需要“历史记录 + 已完成状态 + 通知提醒”，推荐改用导出任务接口

失败：

- `409`：任务仍在处理中，或任务生成失败
- `403`：不是任务创建人且不是平台 `admin`
- `404`：账本或任务不存在
