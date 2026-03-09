## MoneyKeeper 前端联调文档

本文档基于当前 `codex/test` 分支代码整理，面向前端联调与页面改造使用。

## 1. 基础信息

### 1.1 服务地址

- 默认端口：`8081`
- 默认 Base URL：`http://{host}:8081`
- 业务接口统一前缀：`/api`

### 1.2 认证方式

除以下公开接口外，其余 `/api/**` 接口都需要携带 JWT：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/google`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

请求头格式：

```http
Authorization: Bearer <token>
```

### 1.3 角色规则

- `user`：普通用户，只能访问自己的用户资料、分类、记账记录、通知和导出数据
- `admin`：管理员，可以跨用户访问大部分资源，也能访问管理类接口

### 1.4 响应模型

当前项目存在两套成功/失败响应风格，前端需要按模块区分。

#### A. `MkApiResponse<T>`

用于：

- `/api/auth/**`
- `/api/kafka/**`
- `/api/payments/**`

结构：

```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {},
  "timestamp": "2026-03-09T01:00:00",
  "requestId": "REQ-1741482000000"
}
```

字段说明：

- `code`：业务状态码，不一定等于 HTTP 状态码
- `message`：提示信息
- `data`：实际返回数据
- `timestamp`：响应生成时间
- `requestId`：请求标识

#### B. 直接返回实体或 DTO

用于：

- `/api/users/**`
- `/api/categories/**`
- `/api/records/**`
- `/api/search/records/**`
- `/api/integrations/**`
- `/api/notifications/**`
- `/api/notifications/manage/**`

成功时直接返回 JSON 对象或数组，不再包 `data`。

#### C. `ApiErrorResponse`

上述直接返回实体/DTO 的接口，在失败时统一返回：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero",
  "path": "/api/records",
  "timestamp": "2026-03-09T01:00:00"
}
```

### 1.5 日期与枚举约定

- 日期：`yyyy-MM-dd`
- 日期时间：ISO-8601，例如 `2026-03-09T01:00:00`
- 记录类型 `type`：当前代码建议使用 `income` 或 `expense`
- 通知类型 `type`：`success`、`warning`、`info`、`error`、`heartbeat`、`connect`

## 2. 常用数据结构

### 2.1 LoginRequest

```json
{
  "username": "alice",
  "password": "123456"
}
```

### 2.2 LoginResponse

```json
{
  "userId": 1,
  "userPin": "Ab12Cd34Ef",
  "username": "alice",
  "token": "jwt-token"
}
```

### 2.3 RegisterRequest

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000"
}
```

### 2.4 GoogleAuthRequest

```json
{
  "idToken": "google-id-token"
}
```

### 2.5 UserCreateRequest

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000",
  "role": "user"
}
```

### 2.6 UserUpdateRequest

所有字段都可选，至少传一个：

```json
{
  "email": "alice.new@example.com",
  "firstName": "Alice",
  "phoneNumber": "13800138001"
}
```

### 2.7 User

注意：`password` 字段为 write-only，不会在响应里返回。

```json
{
  "id": 1,
  "userPin": "Ab12Cd34Ef",
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000",
  "role": "user",
  "createdAt": "2026-03-09T01:00:00",
  "updatedAt": "2026-03-09T01:00:00",
  "deletedAt": 0,
  "deletedTime": null,
  "registrationCompletedAt": "2026-03-09T01:00:00"
}
```

### 2.8 CategoryRequest

创建时字段必填；更新时字段可选。

```json
{
  "name": "Food",
  "icon": "utensils",
  "color": "#FF6B6B",
  "type": "expense"
}
```

### 2.9 Category

```json
{
  "id": 5,
  "name": "Food",
  "icon": "utensils",
  "color": "#FF6B6B",
  "type": "expense",
  "userId": 1,
  "createdAt": "2026-03-09T01:00:00",
  "updatedAt": "2026-03-09T01:00:00",
  "deletedAt": 0,
  "deletedTime": null
}
```

### 2.10 MoneyKeeperCreateRequest

普通用户传 `userId` 会被忽略，后端使用当前登录用户；管理员可代其他用户创建。

```json
{
  "userId": 1,
  "categoryId": 5,
  "type": "expense",
  "amount": 88.5,
  "transactionDate": "2026-03-08",
  "notes": "Lunch"
}
```

### 2.11 MoneyKeeperUpdateRequest

至少传一个字段：

```json
{
  "amount": 99.0,
  "transactionDate": "2026-03-09",
  "notes": "Dinner"
}
```

### 2.12 MoneyKeeper

```json
{
  "id": 10,
  "userId": 1,
  "categoryId": 5,
  "type": "expense",
  "amount": 99.0,
  "transactionDate": "2026-03-09",
  "notes": "Dinner",
  "createdAt": "2026-03-09T01:00:00",
  "updatedAt": "2026-03-09T01:00:00",
  "deletedAt": 0,
  "deletedTime": null
}
```

### 2.13 MoneyKeeperDTO

用于带分类名的记录列表、搜索、Excel 导出。

```json
{
  "id": 10,
  "userId": 1,
  "categoryId": 5,
  "categoryName": "Food",
  "type": "expense",
  "amount": 99.0,
  "transactionDate": "2026-03-09",
  "updatedAt": "2026-03-09T01:00:00",
  "notes": "Dinner"
}
```

### 2.14 RecordSummary

```json
{
  "totalIncome": 1000.00,
  "totalExpense": 600.00,
  "balance": 400.00
}
```

### 2.15 NotificationMessage

```json
{
  "title": "Budget warning",
  "message": "Your balance is low",
  "type": "warning",
  "timestamp": 1741482000000
}
```

### 2.16 RecordSearchResultDTO

```json
{
  "id": 10,
  "userId": 1,
  "categoryId": 5,
  "categoryName": "Food",
  "type": "expense",
  "amount": 99.0,
  "transactionDate": "2026-03-09",
  "updatedAt": "2026-03-09T01:00:00",
  "notes": "Dinner",
  "score": 1.25
}
```

### 2.17 RecordSearchReindexResultDTO

```json
{
  "scope": "all",
  "userId": null,
  "indexedCount": 12,
  "indexName": "moneykeeper-records",
  "reindexedAt": "2026-03-09T01:00:00"
}
```

### 2.18 RecordSearchIndexStatsDTO

```json
{
  "scope": "user",
  "userId": 2,
  "enabled": true,
  "ready": true,
  "indexName": "moneykeeper-records",
  "indexExists": true,
  "indexedDocumentCount": 8,
  "databaseRecordCount": 9,
  "statsCollectedAt": "2026-03-09T01:00:00"
}
```

### 2.19 IntegrationModuleStatusDTO

```json
{
  "module": "kafka",
  "enabled": false,
  "ready": false,
  "implemented": true,
  "summary": "Kafka module is disabled via app.kafka.enabled",
  "metadata": {
    "producerReady": false,
    "consumerReady": false,
    "consumedCount": 0
  }
}
```

### 2.20 KafkaMessageRecord

```json
{
  "topic": "quickstart-events",
  "key": "message",
  "value": "hello",
  "receivedAt": "2026-03-09T01:00:00"
}
```

### 2.21 PaymentIntentRequest

```json
{
  "amount": 1000,
  "currency": "usd"
}
```

### 2.22 MkPaymentIntentDTO

当前支付模块尚未真正落地，成功数据结构预留如下：

```json
{
  "id": "pi_xxx",
  "amount": 1000,
  "currency": "usd",
  "status": "requires_payment_method",
  "clientSecret": "secret_xxx",
  "paymentMethod": "card",
  "metadata": {
    "orderId": "123"
  }
}
```

### 2.23 MkCheckoutSession

```json
{
  "payment_method_types": ["card"],
  "line_items": [
    {
      "price_data": {
        "currency": "usd",
        "unit_amount": 1000,
        "product_data": {
          "name": "VIP",
          "description": "Monthly subscription"
        }
      },
      "quantity": 1
    }
  ],
  "mode": "payment",
  "success_url": "https://example.com/success",
  "cancel_url": "https://example.com/cancel",
  "metadata": {
    "orderId": "123"
  },
  "customer_email": "alice@example.com"
}
```

## 3. 认证模块

### 3.1 `POST /api/auth/login`

- 认证：否
- Body：`LoginRequest`
- 成功响应：`MkApiResponse<LoginResponse>`
- 失败业务码：
  - `400`：用户名或密码缺失
  - `404`：用户不存在
  - `401`：密码错误
  - `500`：登录失败

### 3.2 `POST /api/auth/logout`

- 认证：是
- Header：`Authorization: Bearer <token>`
- Body：无
- 成功响应：`MkApiResponse<Boolean>`

### 3.3 `POST /api/auth/register`

- 认证：否
- Body：`RegisterRequest`
- 成功响应：`MkApiResponse<User>`
- 失败业务码：
  - `400`：字段校验失败
  - `409`：用户名或邮箱已存在
  - `500`：注册失败

注意：

- 注册成功返回 `User`，但不返回 `password`
- `email`、`firstName`、`lastName` 为必填

### 3.4 `POST /api/auth/google`

- 认证：否
- Body：`GoogleAuthRequest`
- 成功响应：`MkApiResponse<LoginResponse>`
- 失败业务码：
  - `400`：`idToken` 为空
  - `401`：Google 登录失败或 token 非法
  - `500`：服务内部异常

## 4. 用户模块

### 4.1 `POST /api/users`

- 认证：是
- 权限：`admin`
- Body：`UserCreateRequest`
- 成功响应：`User`
- 失败响应：`ApiErrorResponse`

### 4.2 `GET /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 用户 ID
- 成功响应：`User`

### 4.3 `GET /api/users/username/{username}`

- 认证：是
- 权限：
  - 管理员可查任意用户名
  - 普通用户只能查自己的 `username`
- Path 参数：
  - `username`: 用户名
- 成功响应：`User`

### 4.4 `PUT /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- Body：`UserUpdateRequest`
- 成功响应：`User`

联调说明：

- 普通用户不能改 `role`
- 所有字段可选，但如果传了空串会被判为非法
- 修改 `password` 时，响应里仍不会返回密码

### 4.5 `DELETE /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- 成功响应：空 body，HTTP `200`
- 实际行为：逻辑删除

## 5. 分类模块

### 5.1 `POST /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 目标用户 ID
- Body：`CategoryRequest`
- 成功响应：`Category`

注意：

- 这里的 `{id}` 表示用户 ID，不是分类 ID
- 创建时 `name`、`icon`、`color`、`type` 都必填

### 5.2 `GET /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 分类 ID
- 成功响应：`Category`

### 5.3 `GET /api/categories/user/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- 成功响应：`Category[]`

### 5.4 `GET /api/categories/type/{type}`

- 认证：是
- 权限：
  - 管理员：查询所有该类型分类
  - 普通用户：仅查询自己的该类型分类
- Path 参数：
  - `type`: 分类类型，建议 `income` / `expense`
- 成功响应：`Category[]`

### 5.5 `PUT /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 分类 ID
- Body：`CategoryRequest`
- 成功响应：`Category`

联调说明：

- 更新时字段可选，但至少要传一个
- 更新成功后会刷新该分类下记录的搜索索引

### 5.6 `DELETE /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 分类 ID
- 成功响应：空 body，HTTP `200`
- 实际行为：逻辑删除

### 5.7 `GET /api/categories/list`

- 认证：是
- 权限：
  - 管理员：查所有分类
  - 普通用户：只查自己的分类
- 成功响应：`Category[]`

### 5.8 `GET /api/categories/user/{userId}/type/{type}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
  - `type`: 分类类型
- 成功响应：`Category[]`

### 5.9 `GET /api/categories/list/{type}`

- 认证：是
- 权限：
  - 管理员：可用 `userId` 查询任意用户
  - 普通用户：若传 `userId`，必须等于自己
- Path 参数：
  - `type`: 分类类型
- Query 参数：
  - `userId`：可选
- 成功响应：`Category[]`

## 6. 记录模块

### 6.1 `POST /api/records`

- 认证：是
- 权限：本人或管理员
- Body：`MoneyKeeperCreateRequest`
- 成功响应：`MoneyKeeper`

校验规则：

- `categoryId` 必填
- `type` 必填
- `amount` 必须大于 0
- `transactionDate` 必填
- 记录类型必须和分类类型一致

### 6.2 `GET /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 记录 ID
- 成功响应：`MoneyKeeper`

### 6.3 `GET /api/records/user/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `startDate`: 必填
  - `endDate`: 必填
- 成功响应：`MoneyKeeper[]`

### 6.4 `GET /api/records/user/{userId}/type/{type}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
  - `type`: 记录类型
- 成功响应：`MoneyKeeper[]`

### 6.5 `PUT /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 记录 ID
- Body：`MoneyKeeperUpdateRequest`
- 成功响应：`MoneyKeeper`

联调说明：

- 至少传一个字段
- 如果修改 `categoryId` 或 `type`，仍要求记录类型和目标分类类型一致
- 更新成功后会触发该条记录的搜索同步

### 6.6 `DELETE /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `id`: 记录 ID
- 成功响应：空 body，HTTP `200`
- 实际行为：逻辑删除

### 6.7 `GET /api/records/list`

- 认证：是
- 权限：
  - 管理员：所有记录
  - 普通用户：自己的记录
- 成功响应：`MoneyKeeper[]`

### 6.8 `GET /api/records/listWithCategoryName/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `startDate`: 可选
  - `endDate`: 可选
- 成功响应：`MoneyKeeperDTO[]`

### 6.9 `GET /api/records/listByCategoryName/{categoryName}/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `categoryName`: 分类名称
  - `userId`: 用户 ID
- Query 参数：
  - `startDate`: 可选
  - `endDate`: 可选
- 成功响应：`MoneyKeeperDTO[]`

### 6.10 `GET /api/records/list/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `type`: 可选
  - `startDate`: 可选
  - `endDate`: 可选
- 成功响应：`MoneyKeeper[]`

### 6.11 `GET /api/records/summary/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `startDate`: 可选
  - `endDate`: 可选
- 成功响应：`RecordSummary`

## 7. 搜索模块

### 7.1 `GET /api/search/records`

- 认证：是
- 权限：
  - 普通用户默认查自己
  - 管理员可通过 `userId` 查任意用户
- Query 参数：
  - `userId`: 可选
  - `query`: 可选，全文关键字
  - `type`: 可选
  - `categoryId`: 可选
  - `categoryName`: 可选
  - `startDate`: 可选
  - `endDate`: 可选
  - `limit`: 可选，默认 `20`，范围 `1-100`
- 成功响应：`RecordSearchResultDTO[]`
- 失败响应：`ApiErrorResponse`

### 7.2 `POST /api/search/records/reindex`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `userId`: 可选；不传表示全量重建
- 成功响应：`RecordSearchReindexResultDTO`

### 7.3 `GET /api/search/records/stats`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `userId`: 可选；不传表示整个索引
- 成功响应：`RecordSearchIndexStatsDTO`

## 8. Excel 导出模块

### 8.1 `GET /api/excel/download/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `type`: 可选
  - `startDate`: 可选
  - `endDate`: 可选
- 成功响应：二进制 Excel 文件
- `Content-Type`：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

前端建议：

- 请求时使用 `responseType: 'blob'`
- 若后端返回非 `200`，需要尝试按 JSON 解析错误体

## 9. 通知与 SSE 模块

### 9.1 `GET /api/notifications/subscribe/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- 返回：`text/event-stream`

SSE 事件说明：

- `connect`：建立连接时立即推送
- `heartbeat`：大约每 30 秒推送一次
- `message`：业务通知消息

### 9.2 `POST /api/notifications/send/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Body：`NotificationMessage`
- 成功响应：字符串 `"Message sent"`

### 9.3 `POST /api/notifications/broadcast`

- 认证：是
- 权限：`admin`
- Body：`NotificationMessage`
- 成功响应：字符串 `"Broadcast sent"`

### 9.4 `POST /api/notifications/send/{userId}/success`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `title`: 必填
  - `message`: 必填
- 成功响应：字符串 `"Success message sent"`

### 9.5 `POST /api/notifications/send/{userId}/error`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- Query 参数：
  - `title`: 必填
  - `message`: 必填
- 成功响应：字符串 `"Error message sent"`

### 9.6 `POST /api/notifications/broadcast/success`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `title`: 必填
  - `message`: 必填
- 成功响应：字符串 `"Success broadcast sent"`

### 9.7 `POST /api/notifications/broadcast/error`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `title`: 必填
  - `message`: 必填
- 成功响应：字符串 `"Error broadcast sent"`

## 10. SSE 管理模块

### 10.1 `GET /api/notifications/manage/connections`

- 认证：是
- 权限：`admin`
- 成功响应：

```json
{
  "connectedUsers": [1, 2, 3],
  "totalConnections": 3
}
```

### 10.2 `GET /api/notifications/manage/check/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- 成功响应：

```json
{
  "userId": 1,
  "connected": true
}
```

### 10.3 `POST /api/notifications/manage/disconnect/{userId}`

- 认证：是
- 权限：本人或管理员
- Path 参数：
  - `userId`: 用户 ID
- 成功响应：字符串
  - `"Connection disconnected"`
  - 或 `"User is not connected"`

### 10.4 `GET /api/notifications/manage/stats`

- 认证：是
- 权限：`admin`
- 成功响应：

```json
{
  "totalConnections": 3,
  "connectedUsers": [1, 2, 3],
  "timestamp": 1741482000000
}
```

## 11. 集成状态模块

### 11.1 `GET /api/integrations/status`

- 认证：是
- 权限：`admin`
- 成功响应：`IntegrationModuleStatusDTO[]`

当前模块名包括：

- `kafka`
- `elasticsearch`
- `payment`
- `dubbo`
- `nacos-discovery`
- `nacos-config`

### 11.2 `GET /api/integrations/status/{module}`

- 认证：是
- 权限：`admin`
- Path 参数：
  - `module`: 模块名
- 成功响应：`IntegrationModuleStatusDTO`

## 12. Kafka 调试模块

### 12.1 `POST /api/kafka/send`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `topic`: 可选，默认 `quickstart-events`
  - `key`: 可选，默认 `message`
  - `message`: 必填
- 成功响应：`MkApiResponse<String>`

### 12.2 `GET /api/kafka/listen`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `topic`: 可选
  - `key`: 可选，默认 `message`
  - `message`: 必填
- 成功响应：`MkApiResponse<String>`

注意：

- 这个接口当前只是把消息再次发到 Kafka，用于调试，不是“真正的服务端订阅接口”

### 12.3 `GET /api/kafka/status`

- 认证：是
- 权限：`admin`
- 成功响应：`MkApiResponse<Map<String, Object>>`

### 12.4 `GET /api/kafka/messages`

- 认证：是
- 权限：`admin`
- Query 参数：
  - `limit`: 可选，默认 `20`，范围 `1-100`
- 成功响应：`MkApiResponse<KafkaMessageRecord[]>`

## 13. 支付模块

### 13.1 `GET /api/payments/status`

- 认证：是
- 权限：任意已登录用户
- 成功响应：`MkApiResponse<Map<String, Object>>`

### 13.2 `POST /api/payments/intents`

- 认证：是
- 权限：任意已登录用户
- Body：`PaymentIntentRequest`
- 成功响应：`MkApiResponse<MkPaymentIntentDTO>`

当前行为：

- 模块关闭时返回 `503`
- 模块开启但未实现时返回 `501`

### 13.3 `POST /api/payments/intents/{paymentIntentId}/confirm`

- 认证：是
- 权限：任意已登录用户
- Path 参数：
  - `paymentIntentId`: 支付意图 ID
- 成功响应：`MkApiResponse<MkPaymentIntentDTO>`

### 13.4 `POST /api/payments/intents/{paymentIntentId}/cancel`

- 认证：是
- 权限：任意已登录用户
- Path 参数：
  - `paymentIntentId`: 支付意图 ID
- 成功响应：`MkApiResponse<MkPaymentIntentDTO>`

### 13.5 `POST /api/payments/checkout-sessions`

- 认证：是
- 权限：任意已登录用户
- Body：`MkCheckoutSession`
- 成功响应：`MkApiResponse<MkPaymentIntentDTO>`

当前行为：

- 参数合法时，如果支付模块未启用，返回 `503`
- 启用后当前仍返回 `501`，表示骨架已接入、能力未实现

## 14. 前端联调注意事项

### 14.1 记录类型统一

- 当前控制器与测试都按 `income` / `expense` 处理
- 前端请统一使用这两个值，不要混用中文“收入/支出”

### 14.2 SSE 事件数据要兼容两种 `message` payload

`SseEmitterServiceImpl` 当前存在实现差异：

- 单发通知 `sendMessage()` 推送的是 JSON 字符串
- 广播通知 `sendMessageToAll()` 推送的是对象

前端监听 `message` 事件时，建议兼容：

- `typeof data === 'string'` 时先尝试 `JSON.parse`
- 否则按对象直接处理

### 14.3 Excel 下载错误处理

Excel 成功时返回二进制文件，失败时返回 JSON。前端下载逻辑要兼容这两种情况。

### 14.4 搜索功能依赖 Elasticsearch 开关

- `app.elasticsearch.enabled=false` 时，搜索和索引统计接口会返回 `503`
- 前端应根据错误提示展示“搜索未启用”或“服务暂不可用”

### 14.5 支付与 Kafka 目前属于扩展骨架

- 支付接口已经固定，但大多数操作目前是 `501/503`
- Kafka 是管理调试接口，不建议作为正式用户功能入口

### 14.6 当前已知后端问题

以下问题是当前代码层面的已知风险，前端联调时请预期可能出现异常表现：

- 新建记录后，搜索索引可能不会立刻包含该条记录
- 汇总接口的收入/支出统计存在实现风险，联调时请重点核对数值
- 分类修改 `type` 后，旧记录与分类类型可能出现不一致

## 15. 推荐的前端接入顺序

1. 先接 `/api/auth/login` 获取 token
2. 把 token 注入到所有受保护接口的 `Authorization` 头
3. 再接用户信息、分类列表、记录列表
4. 然后接 Excel、SSE、搜索等增强功能
5. Kafka、支付、集成状态建议作为后台管理或开发调试页面使用