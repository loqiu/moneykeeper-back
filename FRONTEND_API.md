## MoneyKeeper 前端联调文档

本文档基于当前 `codex/test` 分支代码整理，供前端联调使用。

接口、参数、返回结构、错误码一旦发生变更，必须同步更新本文档。

## 1. 全局约定

### 1.1 服务地址

- 默认端口：`8081`
- 默认 Base URL：`http://{host}:8081`
- 业务接口统一前缀：`/api`

### 1.2 公开接口

以下接口不需要 JWT：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/google`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

除上面之外，其余 `/api/**` 接口都要带：

```http
Authorization: Bearer <token>
```

### 1.3 角色规则

- `user`：普通用户，只能访问自己的用户资料、分类、记账记录、通知和导出数据
- `admin`：管理员，可以跨用户访问数据，也可以调用管理接口

### 1.4 统一认证失败返回

这是前端最需要先处理的分支。

对于所有需要 JWT 的接口：

- 如果没有带 `Authorization` 头
- 或 token 不是 `Bearer <token>` 格式
- 或 token 无效/过期/被登出

请求会在 JWT 拦截器里直接被拒绝，真实返回是：

- HTTP 状态码：`401`
- Body：纯文本 `Unauthorized`
- 不是 JSON
- 也不是 `MkApiResponse`
- 也不是 `ApiErrorResponse`

前端需要优先按这个分支处理，例如跳登录、清 token、提示会话失效。

### 1.5 两套响应风格

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

注意：

- `code` 是业务码
- controller 内部失败时，HTTP 通常仍然是 `200`
- 前端要优先看 `code`，不能只看 HTTP 状态码

常见业务码：

- `200`：成功
- `400`：请求参数错误
- `401`：业务层认证失败
- `403`：无权限
- `404`：资源不存在
- `409`：资源冲突
- `500`：服务内部错误
- `501`：功能骨架已接入但尚未实现
- `503`：功能未启用或依赖未就绪

#### B. 直接返回实体 / DTO

用于：

- `/api/users/**`
- `/api/categories/**`
- `/api/records/**`
- `/api/search/records/**`
- `/api/integrations/**`
- `/api/notifications/**`
- `/api/notifications/manage/**`

成功时直接返回对象或数组，不包 `data`。

#### C. `ApiErrorResponse`

上述直接返回实体/DTO 的接口，在进入 controller 后发生错误时，统一返回：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero",
  "path": "/api/records",
  "timestamp": "2026-03-09T01:00:00"
}
```

### 1.6 日期与枚举

- 日期：`yyyy-MM-dd`
- 日期时间：ISO-8601，例如 `2026-03-09T01:00:00`
- 记录类型 `type`：建议统一使用 `income` / `expense`
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

```json
{
  "email": "alice.new@example.com",
  "firstName": "Alice",
  "phoneNumber": "13800138001"
}
```

### 2.7 User

注意：响应里不会返回 `password`。

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

普通用户传 `userId` 会被忽略，后端使用当前登录用户；管理员可以代其他用户创建。

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

## 3. 认证模块 `/api/auth`

说明：本模块使用 `MkApiResponse<T>`。除 JWT 拦截失败外，controller 内部错误通常仍然是 HTTP `200`，前端要读 `code`。

### 3.1 `POST /api/auth/login`

- 认证：否
- Body：`LoginRequest`
- 成功：`MkApiResponse<LoginResponse>`
- 业务错误码：
  - `400`：用户名或密码为空
  - `404`：用户不存在
  - `401`：密码错误
  - `500`：登录失败

### 3.2 `POST /api/auth/logout`

- 认证：是
- Header：`Authorization: Bearer <token>`
- 成功：`MkApiResponse<Boolean>`
- 错误：
  - HTTP `401` + 文本 `Unauthorized`：token 缺失、非法、过期、已失效

### 3.3 `POST /api/auth/register`

- 认证：否
- Body：`RegisterRequest`
- 成功：`MkApiResponse<User>`
- 业务错误码：
  - `400`：字段为空、长度非法、邮箱格式非法、手机号格式非法
  - `409`：用户名已存在或邮箱已存在
  - `500`：注册失败

### 3.4 `POST /api/auth/google`

- 认证：否
- Body：`GoogleAuthRequest`
- 成功：`MkApiResponse<LoginResponse>`
- 业务错误码：
  - `400`：`idToken` 为空
  - `401`：Google 登录失败或 token 非法
  - `500`：服务内部异常

## 4. 用户模块 `/api/users`

说明：

- 成功返回实体 `User`
- 进入 controller 后失败返回 `ApiErrorResponse`
- 但 token 无效时，仍然先走全局 `401 Unauthorized` 文本响应

### 4.1 `POST /api/users`

- 认证：是
- 权限：`admin`
- Body：`UserCreateRequest`
- 成功：`User`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `400`：请求体为空、用户名/密码为空、用户名长度非法、密码长度非法、邮箱格式非法、手机号格式非法、角色非法、用户名已存在、邮箱已存在
  - `500`：未处理异常

### 4.2 `GET /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 用户 ID
- 成功：`User`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：用户不存在

### 4.3 `GET /api/users/username/{username}`

- 认证：是
- 权限：
  - 管理员：可查任意用户名
  - 普通用户：只能查自己的用户名
- Path：`username`
- 成功：`User`
- 错误：
  - `401`：token 缺失/非法/过期
  - `400`：用户名为空
  - `403`：普通用户查询他人用户名
  - `404`：用户不存在

### 4.4 `PUT /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 用户 ID
- Body：`UserUpdateRequest`
- 成功：`User`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员；普通用户试图改 `role`
  - `400`：请求体为空、传入空字符串、用户名长度非法、密码长度非法、邮箱格式非法、手机号格式非法、角色非法、用户名重复、邮箱重复
  - `404`：用户不存在

### 4.5 `DELETE /api/users/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 用户 ID
- 成功：空 body，HTTP `200`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：用户不存在

## 5. 分类模块 `/api/categories`

说明：

- 成功返回实体 `Category` 或数组
- 失败返回 `ApiErrorResponse`

### 5.1 `POST /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 目标用户 ID
- Body：`CategoryRequest`
- 成功：`Category`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：请求体为空、`name/icon/color/type` 缺失或为空白

注意：这里的 `{id}` 是用户 ID，不是分类 ID。

### 5.2 `GET /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 分类 ID
- 成功：`Category`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：分类不存在

### 5.3 `GET /api/categories/user/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- 成功：`Category[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员

### 5.4 `GET /api/categories/type/{type}`

- 认证：是
- 权限：
  - 管理员：查所有该类型分类
  - 普通用户：只查自己的该类型分类
- Path：`type`
- 成功：`Category[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `400`：`type` 为空

### 5.5 `PUT /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 分类 ID
- Body：`CategoryRequest`
- 成功：`Category`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：请求体为空、所有字段都没传、某个传入字段是空白字符串
  - `404`：分类不存在

### 5.6 `DELETE /api/categories/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 分类 ID
- 成功：空 body，HTTP `200`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：分类不存在，或分类已被逻辑删除

### 5.7 `GET /api/categories/list`

- 认证：是
- 权限：
  - 管理员：查所有分类
  - 普通用户：只查自己的分类
- 成功：`Category[]`
- 错误：
  - `401`：token 缺失/非法/过期

### 5.8 `GET /api/categories/user/{userId}/type/{type}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`、`type`
- 成功：`Category[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：`type` 为空

### 5.9 `GET /api/categories/list/{type}`

- 认证：是
- 权限：
  - 管理员：可用 `userId` 查任意用户
  - 普通用户：若传 `userId`，必须等于自己
- Path：`type`
- Query：
  - `userId` 可选
- 成功：`Category[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `400`：`type` 为空
  - `403`：普通用户传了其他人的 `userId`

## 6. 记录模块 `/api/records`

说明：

- 成功返回实体 `MoneyKeeper`、数组、`MoneyKeeperDTO[]` 或 `RecordSummary`
- 失败返回 `ApiErrorResponse`

### 6.1 `POST /api/records`

- 认证：是
- 权限：本人或管理员
- Body：`MoneyKeeperCreateRequest`
- 成功：`MoneyKeeper`
- 错误：
  - `401`：token 缺失/非法/过期
  - `400`：请求体为空、`categoryId` 缺失、`type` 缺失、`amount` 非法、`transactionDate` 缺失、分类不属于目标用户、记录类型与分类类型不一致
  - `404`：分类不存在

注意：普通用户传 `userId` 会被忽略，后端使用当前登录用户；管理员可代其他用户创建。

### 6.2 `GET /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 记录 ID
- 成功：`MoneyKeeper`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：记录不存在

### 6.3 `GET /api/records/user/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：
  - `startDate` 必填
  - `endDate` 必填
- 成功：`MoneyKeeper[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：开始/结束日期缺失，或结束日期早于开始日期

### 6.4 `GET /api/records/user/{userId}/type/{type}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`、`type`
- 成功：`MoneyKeeper[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：`type` 为空

### 6.5 `PUT /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 记录 ID
- Body：`MoneyKeeperUpdateRequest`
- 成功：`MoneyKeeper`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：请求体为空、没有任何可更新字段、`type` 空白、`amount` 非法、分类不属于记录所属用户、记录类型与分类类型不一致
  - `404`：记录不存在或分类不存在

### 6.6 `DELETE /api/records/{id}`

- 认证：是
- 权限：本人或管理员
- Path：`id` 记录 ID
- 成功：空 body，HTTP `200`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `404`：记录不存在

### 6.7 `GET /api/records/list`

- 认证：是
- 权限：
  - 管理员：所有记录
  - 普通用户：自己的记录
- 成功：`MoneyKeeper[]`
- 错误：
  - `401`：token 缺失/非法/过期

### 6.8 `GET /api/records/listWithCategoryName/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：
  - `startDate` 可选
  - `endDate` 可选
- 成功：`MoneyKeeperDTO[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：结束日期早于开始日期

### 6.9 `GET /api/records/listByCategoryName/{categoryName}/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`categoryName`、`userId`
- Query：
  - `startDate` 可选
  - `endDate` 可选
- 成功：`MoneyKeeperDTO[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：`categoryName` 为空，或结束日期早于开始日期

### 6.10 `GET /api/records/list/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：
  - `type` 可选
  - `startDate` 可选
  - `endDate` 可选
- 成功：`MoneyKeeper[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：结束日期早于开始日期

### 6.11 `GET /api/records/summary/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：
  - `startDate` 可选
  - `endDate` 可选
- 成功：`RecordSummary`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：结束日期早于开始日期

## 7. 搜索模块 `/api/search/records`

说明：

- 成功返回 DTO
- 失败返回 `ApiErrorResponse`
- 依赖 Elasticsearch，未启用或未就绪时可能返回 `503`

### 7.1 `GET /api/search/records`

- 认证：是
- 权限：
  - 普通用户默认查自己
  - 管理员可通过 `userId` 查询任意用户
- Query：
  - `userId` 可选
  - `query` 可选
  - `type` 可选
  - `categoryId` 可选
  - `categoryName` 可选
  - `startDate` 可选
  - `endDate` 可选
  - `limit` 可选，默认 `20`，范围 `1-100`
- 成功：`RecordSearchResultDTO[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：普通用户搜索他人数据
  - `400`：结束日期早于开始日期，或 `limit` 不在 `1-100`
  - `503`：Elasticsearch 功能关闭或 client 未就绪

### 7.2 `POST /api/search/records/reindex`

- 认证：是
- 权限：`admin`
- Query：
  - `userId` 可选；不传表示全量重建
- 成功：`RecordSearchReindexResultDTO`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `503`：Elasticsearch 功能关闭或 client 未就绪

### 7.3 `GET /api/search/records/stats`

- 认证：是
- 权限：`admin`
- Query：
  - `userId` 可选；不传表示全索引统计
- 成功：`RecordSearchIndexStatsDTO`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `503`：Elasticsearch 功能关闭或 client 未就绪

## 8. Excel 导出模块 `/api/excel`

### 8.1 `GET /api/excel/download/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：
  - `type` 可选
  - `startDate` 可选
  - `endDate` 可选
- 成功：Excel 二进制文件
- Header：
  - `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - `Content-Disposition: attachment;filename*=UTF-8''records_yyyy-MM-dd.xlsx`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：结束日期早于开始日期
  - `500`：Excel 文件生成失败

前端建议：

- 请求时使用 `responseType: 'blob'`
- 失败时尝试把响应解析成 JSON 或文本错误

## 9. 通知与 SSE 模块 `/api/notifications`

说明：

- 普通 HTTP 通知接口成功时返回字符串
- 失败时返回 `ApiErrorResponse`
- SSE 订阅接口成功时返回 `text/event-stream`

### 9.1 `GET /api/notifications/subscribe/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- 成功：`text/event-stream`
- SSE 事件：
  - `connect`
  - `heartbeat`
  - `message`
- 错误：
  - `401`：token 缺失/非法/过期，返回纯文本 `Unauthorized`
  - `403`：不是本人且不是管理员

前端注意：浏览器原生 `EventSource` 不方便携带 `Authorization` 头。如果前端使用原生 `EventSource(url)`，这个接口大概率会在服务端被 JWT 拦截成 `401`。需要使用支持自定义 header 的方案，或改造认证方式。

### 9.2 `POST /api/notifications/send/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Body：`NotificationMessage`
- 成功：字符串 `"Message sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：请求体为空、`title` 为空、`message` 为空、`type` 为空

### 9.3 `POST /api/notifications/broadcast`

- 认证：是
- 权限：`admin`
- Body：`NotificationMessage`
- 成功：字符串 `"Broadcast sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `400`：请求体为空、`title` 为空、`message` 为空、`type` 为空

### 9.4 `POST /api/notifications/send/{userId}/success`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：`title`、`message`
- 成功：字符串 `"Success message sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：`title` 或 `message` 为空

### 9.5 `POST /api/notifications/send/{userId}/error`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- Query：`title`、`message`
- 成功：字符串 `"Error message sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员
  - `400`：`title` 或 `message` 为空

### 9.6 `POST /api/notifications/broadcast/success`

- 认证：是
- 权限：`admin`
- Query：`title`、`message`
- 成功：字符串 `"Success broadcast sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `400`：`title` 或 `message` 为空

### 9.7 `POST /api/notifications/broadcast/error`

- 认证：是
- 权限：`admin`
- Query：`title`、`message`
- 成功：字符串 `"Error broadcast sent"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `400`：`title` 或 `message` 为空

## 10. SSE 管理模块 `/api/notifications/manage`

### 10.1 `GET /api/notifications/manage/connections`

- 认证：是
- 权限：`admin`
- 成功：

```json
{
  "connectedUsers": [1, 2, 3],
  "totalConnections": 3
}
```

- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员

### 10.2 `GET /api/notifications/manage/check/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- 成功：

```json
{
  "userId": 1,
  "connected": true
}
```

- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员

### 10.3 `POST /api/notifications/manage/disconnect/{userId}`

- 认证：是
- 权限：本人或管理员
- Path：`userId`
- 成功：
  - `"Connection disconnected"`
  - 或 `"User is not connected"`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：不是本人且不是管理员

### 10.4 `GET /api/notifications/manage/stats`

- 认证：是
- 权限：`admin`
- 成功：

```json
{
  "totalConnections": 3,
  "connectedUsers": [1, 2, 3],
  "timestamp": 1741482000000
}
```

- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员

## 11. 集成状态模块 `/api/integrations`

### 11.1 `GET /api/integrations/status`

- 认证：是
- 权限：`admin`
- 成功：`IntegrationModuleStatusDTO[]`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员

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
- Path：`module`
- 成功：`IntegrationModuleStatusDTO`
- 错误：
  - `401`：token 缺失/非法/过期
  - `403`：非管理员
  - `404`：模块名不存在

## 12. Kafka 调试模块 `/api/kafka`

说明：

- 本模块使用 `MkApiResponse<T>`
- token 校验失败时仍会先返回真实 HTTP `401` 文本 `Unauthorized`
- 进入 controller 后的错误，多数仍是 HTTP `200` + `code != 200`

### 12.1 `POST /api/kafka/send`

- 认证：是
- 权限：`admin`
- Query：
  - `topic` 可选，默认 `quickstart-events`
  - `key` 可选，默认 `message`
  - `message` 必填
- 成功：`MkApiResponse<String>`
- 业务错误码：
  - `403`：非管理员
  - `400`：`message` 为空
  - `503`：Kafka 模块关闭或 template 不可用
  - `500`：发送失败

### 12.2 `GET /api/kafka/listen`

- 认证：是
- 权限：`admin`
- Query：
  - `topic` 可选
  - `key` 可选，默认 `message`
  - `message` 必填
- 成功：`MkApiResponse<String>`
- 业务错误码：
  - `403`：非管理员
  - `400`：`message` 为空
  - `503`：Kafka 模块关闭或 template 不可用
  - `500`：转发失败

注意：这个接口当前只是再次把消息发到 Kafka，用于调试，不是正式消费订阅接口。

### 12.3 `GET /api/kafka/status`

- 认证：是
- 权限：`admin`
- 成功：`MkApiResponse<Map<String, Object>>`
- 业务错误码：
  - `403`：非管理员

### 12.4 `GET /api/kafka/messages`

- 认证：是
- 权限：`admin`
- Query：
  - `limit` 可选，默认 `20`，范围 `1-100`
- 成功：`MkApiResponse<KafkaMessageRecord[]>`
- 业务错误码：
  - `403`：非管理员
  - `400`：`limit` 不在 `1-100`

## 13. 支付模块 `/api/payments`

说明：

- 本模块使用 `MkApiResponse<T>`
- token 校验失败时，先返回真实 HTTP `401` 文本 `Unauthorized`
- 进入 controller 后的错误，多数仍然是 HTTP `200` + 业务 `code`
- 当前支付模块是扩展骨架，大多数写接口还未真正实现

### 13.1 `GET /api/payments/status`

- 认证：是
- 权限：任意已登录用户
- 成功：`MkApiResponse<Map<String, Object>>`
- 错误：
  - `401`：token 缺失/非法/过期

### 13.2 `POST /api/payments/intents`

- 认证：是
- 权限：任意已登录用户
- Body：`PaymentIntentRequest`
- 成功：`MkApiResponse<MkPaymentIntentDTO>`
- 业务错误码：
  - `400`：请求体为空、`amount` 为空或小于等于 0、币种为空且默认币种也为空
  - `503`：支付模块未启用
  - `501`：支付模块已启用但功能未实现

### 13.3 `POST /api/payments/intents/{paymentIntentId}/confirm`

- 认证：是
- 权限：任意已登录用户
- Path：`paymentIntentId`
- 成功：`MkApiResponse<MkPaymentIntentDTO>`
- 业务错误码：
  - `400`：`paymentIntentId` 为空
  - `503`：支付模块未启用
  - `501`：功能未实现

### 13.4 `POST /api/payments/intents/{paymentIntentId}/cancel`

- 认证：是
- 权限：任意已登录用户
- Path：`paymentIntentId`
- 成功：`MkApiResponse<MkPaymentIntentDTO>`
- 业务错误码：
  - `400`：`paymentIntentId` 为空
  - `503`：支付模块未启用
  - `501`：功能未实现

### 13.5 `POST /api/payments/checkout-sessions`

- 认证：是
- 权限：任意已登录用户
- Body：`MkCheckoutSession`
- 成功：`MkApiResponse<MkPaymentIntentDTO>`
- 业务错误码：
  - `400`：请求体为空、`mode` 为空、`success_url` 为空、`cancel_url` 为空、`line_items` 为空
  - `503`：支付模块未启用
  - `501`：功能未实现

## 14. 前端联调重点说明

### 14.1 先处理 401 纯文本分支

所有受保护接口都可能在进入 controller 之前被拦截成：

- HTTP `401`
- 文本 `Unauthorized`

前端不要假定失败一定是 JSON。

### 14.2 `MkApiResponse` 模块不能只看 HTTP 状态码

认证、Kafka、支付这三类接口，controller 内部很多错误都还是 HTTP `200`，只是 body 里的 `code != 200`。

前端判断逻辑建议：

1. 先看是不是 HTTP `401` 文本 `Unauthorized`
2. 如果是 `MkApiResponse` 模块，再看 `body.code`
3. 如果是普通 JSON 模块，再看 HTTP 状态码和 `ApiErrorResponse`

### 14.3 SSE 接口的 header 限制

`GET /api/notifications/subscribe/{userId}` 需要 JWT，但浏览器原生 `EventSource` 不方便带自定义 `Authorization` 头，联调时极易直接收到 `401`。

### 14.4 SSE `message` 事件 payload 需要兼容两种格式

当前 `SseEmitterServiceImpl` 存在实现差异：

- 单发通知 `sendMessage()` 推送的是 JSON 字符串
- 广播通知 `sendMessageToAll()` 推送的是对象

前端监听 `message` 事件时建议：

- 如果 `data` 是字符串，先尝试 `JSON.parse`
- 如果已经是对象，直接使用

### 14.5 Excel 下载错误处理

Excel 成功时返回二进制文件，失败时可能是 JSON 错误体，前端下载逻辑要兼容这两种情况。

### 14.6 当前后端已知风险

以下问题已经在代码 review 中确认，前端联调时请重点关注：

- 新建记录后，搜索索引可能不会立刻包含该条记录
- 汇总接口的收入/支出统计存在实现风险，联调时请重点核对数值
- 分类修改 `type` 后，旧记录与分类类型可能出现不一致

## 15. 前端推荐接入顺序

1. 先接 `/api/auth/login` 获取 token
2. 统一封装 `Authorization` 注入和 `401 Unauthorized` 文本处理
3. 再接用户、分类、记录、汇总
4. 然后接 Excel、通知、SSE、搜索
5. Kafka、支付、集成状态建议作为管理端或调试页面使用