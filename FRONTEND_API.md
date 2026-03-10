## MoneyKeeper 鍓嶇鑱旇皟鏂囨。

鏈枃妗ｅ熀浜庡綋鍓?`codex/test` 鍒嗘敮浠ｇ爜鏁寸悊锛屼緵鍓嶇鑱旇皟浣跨敤銆?
鎺ュ彛銆佸弬鏁般€佽繑鍥炵粨鏋勩€侀敊璇爜涓€鏃﹀彂鐢熷彉鏇达紝蹇呴』鍚屾鏇存柊鏈枃妗ｃ€?
## 1. 鍏ㄥ眬绾﹀畾

### 1.1 鏈嶅姟鍦板潃

- 榛樿绔彛锛歚8081`
- 榛樿 Base URL锛歚http://{host}:8081`
- 涓氬姟鎺ュ彛缁熶竴鍓嶇紑锛歚/api`

### 1.2 公开接口

以下接口不需要 JWT：
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/google`
- `POST /api/payments/webhooks/stripe`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

除上述之外，其余 `/api/**` 接口都需要带：

```http
Authorization: Bearer <token>
```
### 1.3 瑙掕壊瑙勫垯

- `user`锛氭櫘閫氱敤鎴凤紝鍙兘璁块棶鑷繁鐨勭敤鎴疯祫鏂欍€佸垎绫汇€佽璐﹁褰曘€侀€氱煡鍜屽鍑烘暟鎹?- `admin`锛氱鐞嗗憳锛屽彲浠ヨ法鐢ㄦ埛璁块棶鏁版嵁锛屼篃鍙互璋冪敤绠＄悊鎺ュ彛

### 1.4 缁熶竴璁よ瘉澶辫触杩斿洖

杩欐槸鍓嶇鏈€闇€瑕佸厛澶勭悊鐨勫垎鏀€?
瀵逛簬鎵€鏈夐渶瑕?JWT 鐨勬帴鍙ｏ細

- 濡傛灉娌℃湁甯?`Authorization` 澶?- 鎴?token 涓嶆槸 `Bearer <token>` 鏍煎紡
- 鎴?token 鏃犳晥/杩囨湡/琚櫥鍑?
璇锋眰浼氬湪 JWT 鎷︽埅鍣ㄩ噷鐩存帴琚嫆缁濓紝鐪熷疄杩斿洖鏄細

- HTTP 鐘舵€佺爜锛歚401`
- Body锛氱函鏂囨湰 `Unauthorized`
- 涓嶆槸 JSON
- 涔熶笉鏄?`MkApiResponse`
- 涔熶笉鏄?`ApiErrorResponse`

鍓嶇闇€瑕佷紭鍏堟寜杩欎釜鍒嗘敮澶勭悊锛屼緥濡傝烦鐧诲綍銆佹竻 token銆佹彁绀轰細璇濆け鏁堛€?
### 1.5 两套响应风格

#### A. `MkApiResponse<T>`

用于：
- `/api/auth/**`
- `/api/kafka/**`
- `/api/payments/**`（但不包含 `/api/payments/webhooks/stripe`）

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
- 如果请求在进入 controller 前就失败，例如 JSON 格式错误、缺少必填 query 参数、参数类型不匹配，HTTP 会直接返回 `400 ApiErrorResponse`，不是 `MkApiResponse`

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

常见触发场景：
- JSON 语法错误，返回 `message = "Malformed JSON request"`
- 必填 `@RequestParam` 缺失
- 参数类型不匹配，例如把数字参数传成字符串

#### D. 纯文本 / 非 JSON

用于：
- JWT 拦截器直接拒绝的 `401 Unauthorized`
- `POST /api/payments/webhooks/stripe`

Webhook 的返回：
- 成功：HTTP `200`，body = `ok`
- 失败：HTTP `400/503/500`，body 为纯文本错误信息
### 1.6 鏃ユ湡涓庢灇涓?
- 鏃ユ湡锛歚yyyy-MM-dd`
- 鏃ユ湡鏃堕棿锛欼SO-8601锛屼緥濡?`2026-03-09T01:00:00`
- 璁板綍绫诲瀷 `type`锛氬缓璁粺涓€浣跨敤 `income` / `expense`
- 閫氱煡绫诲瀷 `type`锛歚success`銆乣warning`銆乣info`銆乣error`銆乣heartbeat`銆乣connect`

## 2. 甯哥敤鏁版嵁缁撴瀯

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

瀛楁绾︽潫锛?
- `username`锛氬繀濉紝鍘婚灏剧┖鏍煎悗闀垮害 `3-50`
- `password`锛氬繀濉紝闀垮害 `6-255`
- `email`锛氬繀濉紝鍚堟硶閭鏍煎紡
- `firstName`锛氬繀濉紝鍘婚灏剧┖鏍煎悗闀垮害 `1-50`
- `lastName`锛氬繀濉紝鍘婚灏剧┖鏍煎悗闀垮害 `1-50`
- `phoneNumber`锛氬彲閫夛紱濡傛灉浼狅紝蹇呴』鏄?`10-11` 浣嶆暟瀛?
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

瀛楁绾︽潫锛?
- `username`锛氬繀濉紝鍘婚灏剧┖鏍煎悗闀垮害 `3-50`
- `password`锛氬繀濉紝闀垮害 `6-255`
- `email`锛氬彲閫夛紱濡傛灉浼狅紝涓嶈兘涓虹┖鐧戒笖蹇呴』鏄悎娉曢偖绠辨牸寮?- `firstName`锛氬彲閫?- `lastName`锛氬彲閫?- `phoneNumber`锛氬彲閫夛紱濡傛灉浼狅紝涓嶈兘涓虹┖鐧戒笖蹇呴』鏄?`10-11` 浣嶆暟瀛?- `role`锛氬彲閫夛紱濡傛灉浼狅紝鍙兘鏄?`user` 鎴?`admin`

### 2.6 UserUpdateRequest

```json
{
  "email": "alice.new@example.com",
  "firstName": "Alice",
  "phoneNumber": "13800138001"
}
```

瀛楁绾︽潫锛?
- 鎵€鏈夊瓧娈甸兘鍙€?- `username`锛氬鏋滀紶锛屼笉鑳戒负绌虹櫧锛屽幓棣栧熬绌烘牸鍚庨暱搴?`3-50`
- `password`锛氬鏋滀紶锛屼笉鑳戒负绌虹櫧锛岄暱搴?`6-255`
- `email`锛氬鏋滀紶锛屼笉鑳戒负绌虹櫧涓斿繀椤绘槸鍚堟硶閭鏍煎紡
- `phoneNumber`锛氬鏋滀紶锛屼笉鑳戒负绌虹櫧涓斿繀椤绘槸 `10-11` 浣嶆暟瀛?- `role`锛氬鏋滀紶锛屽彧鑳芥槸 `user` 鎴?`admin`锛涗笖鍙湁绠＄悊鍛樺彲浠ヤ慨鏀?
### 2.7 User

娉ㄦ剰锛氬搷搴旈噷涓嶄細杩斿洖 `password`銆?
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

瀛楁绾︽潫锛?
- 鏂板缓鏃讹細`name`銆乣icon`銆乣color`銆乣type` 閮藉繀濉紝涓斾笉鑳戒负绌虹櫧
- 鏇存柊鏃讹細鑷冲皯浼犱竴涓瓧娈碉紱浼犲叆鐨勫瓧娈典笉鑳戒负绌虹櫧

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

娉ㄦ剰锛氬綋鍓?`POST /api/categories/{id}` 鎴愬姛鍚庯紝鍝嶅簲浣撻噷鐨?`id` 涓嶄繚璇佸凡缁忓洖濉€傚墠绔鏋滃垱寤哄悗绔嬪埢渚濊禆鏂?`id`锛屽缓璁噸鏂版媺涓€娆″垎绫诲垪琛ㄦ垨璇︽儏銆?
### 2.10 MoneyKeeperCreateRequest

鏅€氱敤鎴蜂紶 `userId` 浼氳蹇界暐锛屽悗绔娇鐢ㄥ綋鍓嶇櫥褰曠敤鎴凤紱绠＄悊鍛樺彲浠ヤ唬鍏朵粬鐢ㄦ埛鍒涘缓銆?
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

瀛楁绾︽潫锛?
- `userId`锛氬彲閫夛紱鏅€氱敤鎴蜂紶浜嗕篃浼氳蹇界暐锛岀鐞嗗憳鍙唬鍏朵粬鐢ㄦ埛鍒涘缓
- `categoryId`锛氬繀濉?- `type`锛氬繀濉紝涓嶈兘涓虹┖鐧斤紝涓斿繀椤诲拰鎵€閫夊垎绫?`type` 涓€鑷?- `amount`锛氬繀濉紝蹇呴』澶т簬 `0`
- `transactionDate`锛氬繀濉紝鏍煎紡 `yyyy-MM-dd`
- `notes`锛氬彲閫夛紱濡傛灉浼犵┖鐧藉瓧绗︿覆锛屽悗绔細鎸?`null` 澶勭悊

### 2.11 MoneyKeeperUpdateRequest

```json
{
  "amount": 99.0,
  "transactionDate": "2026-03-09",
  "notes": "Dinner"
}
```

瀛楁绾︽潫锛?
- 鎵€鏈夊瓧娈甸兘鍙€?- 浣嗚嚦灏戣浼犱竴涓彲鏇存柊瀛楁
- `type`锛氬鏋滀紶锛屼笉鑳戒负绌虹櫧锛屼笖鏈€缁堢敓鏁堝€煎繀椤诲拰鐩爣鍒嗙被 `type` 涓€鑷?- `amount`锛氬鏋滀紶锛屽繀椤诲ぇ浜?`0`
- `transactionDate`锛氬鏋滀紶锛屾牸寮?`yyyy-MM-dd`
- `notes`锛氬鏋滀紶绌虹櫧瀛楃涓诧紝鍚庣浼氭寜 `null` 澶勭悊

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

## 3. 璁よ瘉妯″潡 `/api/auth`

璇存槑锛氭湰妯″潡浣跨敤 `MkApiResponse<T>`銆傞櫎 JWT 鎷︽埅澶辫触澶栵紝controller 鍐呴儴閿欒閫氬父浠嶇劧鏄?HTTP `200`锛屽墠绔璇?`code`銆?
### 3.1 `POST /api/auth/login`

- 璁よ瘉锛氬惁
- Body锛歚LoginRequest`
- 鎴愬姛锛歚MkApiResponse<LoginResponse>`
- 涓氬姟閿欒鐮侊細
  - `400`锛氱敤鎴峰悕鎴栧瘑鐮佷负绌?  - `404`锛氱敤鎴蜂笉瀛樺湪
  - `401`锛氬瘑鐮侀敊璇?  - `500`锛氱櫥褰曞け璐?- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛氳姹備綋涓嶆槸鍚堟硶 JSON

### 3.2 `POST /api/auth/logout`

- 璁よ瘉锛氭槸
- Header锛歚Authorization: Bearer <token>`
- 鎴愬姛锛歚MkApiResponse<Boolean>`
- 閿欒锛?  - HTTP `401` + 鏂囨湰 `Unauthorized`锛歵oken 缂哄け銆侀潪娉曘€佽繃鏈熴€佸凡澶辨晥

### 3.3 `POST /api/auth/register`

- 璁よ瘉锛氬惁
- Body锛歚RegisterRequest`
- 鎴愬姛锛歚MkApiResponse<User>`
- 涓氬姟閿欒鐮侊細
  - `400`锛氬瓧娈典负绌恒€侀暱搴﹂潪娉曘€侀偖绠辨牸寮忛潪娉曘€佹墜鏈哄彿鏍煎紡闈炴硶
  - `409`锛氱敤鎴峰悕宸插瓨鍦ㄦ垨閭宸插瓨鍦?  - `500`锛氭敞鍐屽け璐?- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛氳姹備綋涓嶆槸鍚堟硶 JSON

### 3.4 `POST /api/auth/google`

- 璁よ瘉锛氬惁
- Body锛歚GoogleAuthRequest`
- 鎴愬姛锛歚MkApiResponse<LoginResponse>`
- 涓氬姟閿欒鐮侊細
  - `400`锛歚idToken` 涓虹┖
  - `401`锛欸oogle 鐧诲綍澶辫触鎴?token 闈炴硶
  - `500`锛氭湇鍔″唴閮ㄥ紓甯?- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛氳姹備綋涓嶆槸鍚堟硶 JSON

## 4. 鐢ㄦ埛妯″潡 `/api/users`

璇存槑锛?
- 鎴愬姛杩斿洖瀹炰綋 `User`
- 杩涘叆 controller 鍚庡け璐ヨ繑鍥?`ApiErrorResponse`
- 浣?token 鏃犳晥鏃讹紝浠嶇劧鍏堣蛋鍏ㄥ眬 `401 Unauthorized` 鏂囨湰鍝嶅簲

### 4.1 `POST /api/users`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Body锛歚UserCreateRequest`
- 鎴愬姛锛歚User`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆佺敤鎴峰悕/瀵嗙爜涓虹┖銆佺敤鎴峰悕闀垮害闈炴硶銆佸瘑鐮侀暱搴﹂潪娉曘€侀偖绠辨牸寮忛潪娉曘€佹墜鏈哄彿鏍煎紡闈炴硶銆佽鑹查潪娉曘€佺敤鎴峰悕宸插瓨鍦ㄣ€侀偖绠卞凡瀛樺湪
  - `500`锛氭湭澶勭悊寮傚父

### 4.2 `GET /api/users/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鐢ㄦ埛 ID
- 鎴愬姛锛歚User`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氱敤鎴蜂笉瀛樺湪

### 4.3 `GET /api/users/username/{username}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 绠＄悊鍛橈細鍙煡浠绘剰鐢ㄦ埛鍚?  - 鏅€氱敤鎴凤細鍙兘鏌ヨ嚜宸辩殑鐢ㄦ埛鍚?- Path锛歚username`
- 鎴愬姛锛歚User`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `400`锛氱敤鎴峰悕涓虹┖
  - `403`锛氭櫘閫氱敤鎴锋煡璇粬浜虹敤鎴峰悕
  - `404`锛氱敤鎴蜂笉瀛樺湪

### 4.4 `PUT /api/users/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鐢ㄦ埛 ID
- Body锛歚UserUpdateRequest`
- 鎴愬姛锛歚User`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛橈紱鏅€氱敤鎴疯瘯鍥炬敼 `role`
  - `400`锛氳姹備綋涓虹┖銆佷紶鍏ョ┖瀛楃涓层€佺敤鎴峰悕闀垮害闈炴硶銆佸瘑鐮侀暱搴﹂潪娉曘€侀偖绠辨牸寮忛潪娉曘€佹墜鏈哄彿鏍煎紡闈炴硶銆佽鑹查潪娉曘€佺敤鎴峰悕閲嶅銆侀偖绠遍噸澶?  - `404`锛氱敤鎴蜂笉瀛樺湪

### 4.5 `DELETE /api/users/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鐢ㄦ埛 ID
- 鎴愬姛锛氱┖ body锛孒TTP `200`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氱敤鎴蜂笉瀛樺湪

## 5. 鍒嗙被妯″潡 `/api/categories`

璇存槑锛?
- 鎴愬姛杩斿洖瀹炰綋 `Category` 鎴栨暟缁?- 澶辫触杩斿洖 `ApiErrorResponse`

### 5.1 `POST /api/categories/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鐩爣鐢ㄦ埛 ID
- Body锛歚CategoryRequest`
- 鎴愬姛锛歚Category`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆乣name/icon/color/type` 缂哄け鎴栦负绌虹櫧

娉ㄦ剰锛氳繖閲岀殑 `{id}` 鏄敤鎴?ID锛屼笉鏄垎绫?ID銆?
鑱旇皟娉ㄦ剰锛氬綋鍓嶆垚鍔熷搷搴旈噷鐨勫垎绫诲璞′笉淇濊瘉宸茬粡甯﹀洖鏁版嵁搴撶敓鎴愮殑 `id`銆?
### 5.2 `GET /api/categories/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鍒嗙被 ID
- 鎴愬姛锛歚Category`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氬垎绫讳笉瀛樺湪

### 5.3 `GET /api/categories/user/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- 鎴愬姛锛歚Category[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?
### 5.4 `GET /api/categories/type/{type}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 绠＄悊鍛橈細鏌ユ墍鏈夎绫诲瀷鍒嗙被
  - 鏅€氱敤鎴凤細鍙煡鑷繁鐨勮绫诲瀷鍒嗙被
- Path锛歚type`
- 鎴愬姛锛歚Category[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `400`锛歚type` 涓虹┖

### 5.5 `PUT /api/categories/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鍒嗙被 ID
- Body锛歚CategoryRequest`
- 鎴愬姛锛歚Category`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆佹墍鏈夊瓧娈甸兘娌′紶銆佹煇涓紶鍏ュ瓧娈垫槸绌虹櫧瀛楃涓?  - `404`锛氬垎绫讳笉瀛樺湪

### 5.6 `DELETE /api/categories/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 鍒嗙被 ID
- 鎴愬姛锛氱┖ body锛孒TTP `200`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氬垎绫讳笉瀛樺湪锛屾垨鍒嗙被宸茶閫昏緫鍒犻櫎

### 5.7 `GET /api/categories/list`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 绠＄悊鍛橈細鏌ユ墍鏈夊垎绫?  - 鏅€氱敤鎴凤細鍙煡鑷繁鐨勫垎绫?- 鎴愬姛锛歚Category[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡

### 5.8 `GET /api/categories/user/{userId}/type/{type}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`銆乣type`
- 鎴愬姛锛歚Category[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛歚type` 涓虹┖

### 5.9 `GET /api/categories/list/{type}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 绠＄悊鍛橈細鍙敤 `userId` 鏌ヤ换鎰忕敤鎴?  - 鏅€氱敤鎴凤細鑻ヤ紶 `userId`锛屽繀椤荤瓑浜庤嚜宸?- Path锛歚type`
- Query锛?  - `userId` 鍙€?- 鎴愬姛锛歚Category[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `400`锛歚type` 涓虹┖
  - `403`锛氭櫘閫氱敤鎴蜂紶浜嗗叾浠栦汉鐨?`userId`

## 6. 璁板綍妯″潡 `/api/records`

璇存槑锛?
- 鎴愬姛杩斿洖瀹炰綋 `MoneyKeeper`銆佹暟缁勩€乣MoneyKeeperDTO[]` 鎴?`RecordSummary`
- 澶辫触杩斿洖 `ApiErrorResponse`

### 6.1 `POST /api/records`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Body锛歚MoneyKeeperCreateRequest`
- 鎴愬姛锛歚MoneyKeeper`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `400`锛氳姹備綋涓虹┖銆乣categoryId` 缂哄け銆乣type` 缂哄け銆乣amount` 闈炴硶銆乣transactionDate` 缂哄け銆佸垎绫讳笉灞炰簬鐩爣鐢ㄦ埛銆佽褰曠被鍨嬩笌鍒嗙被绫诲瀷涓嶄竴鑷?  - `404`锛氬垎绫讳笉瀛樺湪

娉ㄦ剰锛氭櫘閫氱敤鎴蜂紶 `userId` 浼氳蹇界暐锛屽悗绔娇鐢ㄥ綋鍓嶇櫥褰曠敤鎴凤紱绠＄悊鍛樺彲浠ｅ叾浠栫敤鎴峰垱寤恒€?
鑱旇皟娉ㄦ剰锛氬綋鍓嶆垚鍔熷搷搴旈噷鐨勮褰曞璞′笉淇濊瘉宸茬粡甯﹀洖鏁版嵁搴撶敓鎴愮殑 `id`銆?
### 6.2 `GET /api/records/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 璁板綍 ID
- 鎴愬姛锛歚MoneyKeeper`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氳褰曚笉瀛樺湪

### 6.3 `GET /api/records/user/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛?  - `startDate` 蹇呭～
  - `endDate` 蹇呭～
- 鎴愬姛锛歚MoneyKeeper[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氬紑濮?缁撴潫鏃ユ湡缂哄け锛屾垨缁撴潫鏃ユ湡鏃╀簬寮€濮嬫棩鏈?
### 6.4 `GET /api/records/user/{userId}/type/{type}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`銆乣type`
- 鎴愬姛锛歚MoneyKeeper[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛歚type` 涓虹┖

### 6.5 `PUT /api/records/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 璁板綍 ID
- Body锛歚MoneyKeeperUpdateRequest`
- 鎴愬姛锛歚MoneyKeeper`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆佹病鏈変换浣曞彲鏇存柊瀛楁銆乣type` 绌虹櫧銆乣amount` 闈炴硶銆佸垎绫讳笉灞炰簬璁板綍鎵€灞炵敤鎴枫€佽褰曠被鍨嬩笌鍒嗙被绫诲瀷涓嶄竴鑷?  - `404`锛氳褰曚笉瀛樺湪鎴栧垎绫讳笉瀛樺湪

### 6.6 `DELETE /api/records/{id}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚id` 璁板綍 ID
- 鎴愬姛锛氱┖ body锛孒TTP `200`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `404`锛氳褰曚笉瀛樺湪

### 6.7 `GET /api/records/list`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 绠＄悊鍛橈細鎵€鏈夎褰?  - 鏅€氱敤鎴凤細鑷繁鐨勮褰?- 鎴愬姛锛歚MoneyKeeper[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡

### 6.8 `GET /api/records/listWithCategoryName/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛?  - `startDate` 鍙€?  - `endDate` 鍙€?- 鎴愬姛锛歚MoneyKeeperDTO[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氱粨鏉熸棩鏈熸棭浜庡紑濮嬫棩鏈?
### 6.9 `GET /api/records/listByCategoryName/{categoryName}/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚categoryName`銆乣userId`
- Query锛?  - `startDate` 鍙€?  - `endDate` 鍙€?- 鎴愬姛锛歚MoneyKeeperDTO[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛歚categoryName` 涓虹┖锛屾垨缁撴潫鏃ユ湡鏃╀簬寮€濮嬫棩鏈?
### 6.10 `GET /api/records/list/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛?  - `type` 鍙€?  - `startDate` 鍙€?  - `endDate` 鍙€?- 鎴愬姛锛歚MoneyKeeper[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氱粨鏉熸棩鏈熸棭浜庡紑濮嬫棩鏈?
### 6.11 `GET /api/records/summary/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛?  - `startDate` 鍙€?  - `endDate` 鍙€?- 鎴愬姛锛歚RecordSummary`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氱粨鏉熸棩鏈熸棭浜庡紑濮嬫棩鏈?
## 7. 鎼滅储妯″潡 `/api/search/records`

璇存槑锛?
- 鎴愬姛杩斿洖 DTO
- 澶辫触杩斿洖 `ApiErrorResponse`
- 渚濊禆 Elasticsearch锛屾湭鍚敤鎴栨湭灏辩华鏃跺彲鑳借繑鍥?`503`

### 7.1 `GET /api/search/records`

- 璁よ瘉锛氭槸
- 鏉冮檺锛?  - 鏅€氱敤鎴烽粯璁ゆ煡鑷繁
  - 绠＄悊鍛樺彲閫氳繃 `userId` 鏌ヨ浠绘剰鐢ㄦ埛
- Query锛?  - `userId` 鍙€?  - `query` 鍙€?  - `type` 鍙€?  - `categoryId` 鍙€?  - `categoryName` 鍙€?  - `startDate` 鍙€?  - `endDate` 鍙€?  - `limit` 鍙€夛紝榛樿 `20`锛岃寖鍥?`1-100`
- 瀛楁绾︽潫锛?  - 鏅€氱敤鎴蜂笉搴斾紶鍏朵粬鐢ㄦ埛鐨?`userId`
  - `limit` 瓒呭嚭 `1-100` 浼氳鎷掔粷
  - `startDate/endDate` 濡傚悓鏃朵紶鍏ワ紝`endDate` 涓嶈兘鏃╀簬 `startDate`
- 鎴愬姛锛歚RecordSearchResultDTO[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氭櫘閫氱敤鎴锋悳绱粬浜烘暟鎹?  - `400`锛氱粨鏉熸棩鏈熸棭浜庡紑濮嬫棩鏈燂紝鎴?`limit` 涓嶅湪 `1-100`
  - `503`锛欵lasticsearch 鍔熻兘鍏抽棴鎴?client 鏈氨缁?
### 7.2 `POST /api/search/records/reindex`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛?  - `userId` 鍙€夛紱涓嶄紶琛ㄧず鍏ㄩ噺閲嶅缓
- 鎴愬姛锛歚RecordSearchReindexResultDTO`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `503`锛欵lasticsearch 鍔熻兘鍏抽棴鎴?client 鏈氨缁?
### 7.3 `GET /api/search/records/stats`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛?  - `userId` 鍙€夛紱涓嶄紶琛ㄧず鍏ㄧ储寮曠粺璁?- 鎴愬姛锛歚RecordSearchIndexStatsDTO`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `503`锛欵lasticsearch 鍔熻兘鍏抽棴鎴?client 鏈氨缁?
## 8. Excel 瀵煎嚭妯″潡 `/api/excel`

### 8.1 `GET /api/excel/download/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛?  - `type` 鍙€?  - `startDate` 鍙€?  - `endDate` 鍙€?- 鎴愬姛锛欵xcel 浜岃繘鍒舵枃浠?- Header锛?  - `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - `Content-Disposition: attachment;filename*=UTF-8''records_yyyy-MM-dd.xlsx`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氱粨鏉熸棩鏈熸棭浜庡紑濮嬫棩鏈?  - `500`锛欵xcel 鏂囦欢鐢熸垚澶辫触

鍓嶇寤鸿锛?
- 璇锋眰鏃朵娇鐢?`responseType: 'blob'`
- 澶辫触鏃跺皾璇曟妸鍝嶅簲瑙ｆ瀽鎴?JSON 鎴栨枃鏈敊璇?
## 9. 閫氱煡涓?SSE 妯″潡 `/api/notifications`

璇存槑锛?
- 鏅€?HTTP 閫氱煡鎺ュ彛鎴愬姛鏃惰繑鍥炲瓧绗︿覆
- 澶辫触鏃惰繑鍥?`ApiErrorResponse`
- SSE 璁㈤槄鎺ュ彛鎴愬姛鏃惰繑鍥?`text/event-stream`

### 9.1 `GET /api/notifications/subscribe/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- 鎴愬姛锛歚text/event-stream`
- SSE 浜嬩欢锛?  - `connect`
  - `heartbeat`
  - `message`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡锛岃繑鍥炵函鏂囨湰 `Unauthorized`
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?
鍓嶇娉ㄦ剰锛氭祻瑙堝櫒鍘熺敓 `EventSource` 涓嶆柟渚挎惡甯?`Authorization` 澶淬€傚鏋滃墠绔娇鐢ㄥ師鐢?`EventSource(url)`锛岃繖涓帴鍙ｅぇ姒傜巼浼氬湪鏈嶅姟绔 JWT 鎷︽埅鎴?`401`銆傞渶瑕佷娇鐢ㄦ敮鎸佽嚜瀹氫箟 header 鐨勬柟妗堬紝鎴栨敼閫犺璇佹柟寮忋€?
### 9.2 `POST /api/notifications/send/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Body锛歚NotificationMessage`
- 鎴愬姛锛氬瓧绗︿覆 `"Message sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆乣title` 涓虹┖銆乣message` 涓虹┖銆乣type` 涓虹┖

### 9.3 `POST /api/notifications/broadcast`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Body锛歚NotificationMessage`
- 鎴愬姛锛氬瓧绗︿覆 `"Broadcast sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `400`锛氳姹備綋涓虹┖銆乣title` 涓虹┖銆乣message` 涓虹┖銆乣type` 涓虹┖

### 9.4 `POST /api/notifications/send/{userId}/success`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛歚title`銆乣message`
- 鎴愬姛锛氬瓧绗︿覆 `"Success message sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛歚title` 鎴?`message` 涓虹┖

### 9.5 `POST /api/notifications/send/{userId}/error`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- Query锛歚title`銆乣message`
- 鎴愬姛锛氬瓧绗︿覆 `"Error message sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?  - `400`锛歚title` 鎴?`message` 涓虹┖

### 9.6 `POST /api/notifications/broadcast/success`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛歚title`銆乣message`
- 鎴愬姛锛氬瓧绗︿覆 `"Success broadcast sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `400`锛歚title` 鎴?`message` 涓虹┖

### 9.7 `POST /api/notifications/broadcast/error`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛歚title`銆乣message`
- 鎴愬姛锛氬瓧绗︿覆 `"Error broadcast sent"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `400`锛歚title` 鎴?`message` 涓虹┖

## 10. SSE 绠＄悊妯″潡 `/api/notifications/manage`

### 10.1 `GET /api/notifications/manage/connections`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- 鎴愬姛锛?
```json
{
  "connectedUsers": [1, 2, 3],
  "totalConnections": 3
}
```

- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?
### 10.2 `GET /api/notifications/manage/check/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- 鎴愬姛锛?
```json
{
  "userId": 1,
  "connected": true
}
```

- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?
### 10.3 `POST /api/notifications/manage/disconnect/{userId}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛氭湰浜烘垨绠＄悊鍛?- Path锛歚userId`
- 鎴愬姛锛?  - `"Connection disconnected"`
  - 鎴?`"User is not connected"`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氫笉鏄湰浜轰笖涓嶆槸绠＄悊鍛?
### 10.4 `GET /api/notifications/manage/stats`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- 鎴愬姛锛?
```json
{
  "totalConnections": 3,
  "connectedUsers": [1, 2, 3],
  "timestamp": 1741482000000
}
```

- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?
## 11. 闆嗘垚鐘舵€佹ā鍧?`/api/integrations`

### 11.1 `GET /api/integrations/status`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- 鎴愬姛锛歚IntegrationModuleStatusDTO[]`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?
褰撳墠妯″潡鍚嶅寘鎷細

- `kafka`
- `elasticsearch`
- `payment`
- `dubbo`
- `nacos-discovery`
- `nacos-config`

### 11.2 `GET /api/integrations/status/{module}`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Path锛歚module`
- 鎴愬姛锛歚IntegrationModuleStatusDTO`
- 閿欒锛?  - `401`锛歵oken 缂哄け/闈炴硶/杩囨湡
  - `403`锛氶潪绠＄悊鍛?  - `404`锛氭ā鍧楀悕涓嶅瓨鍦?
## 12. Kafka 璋冭瘯妯″潡 `/api/kafka`

璇存槑锛?
- 鏈ā鍧椾娇鐢?`MkApiResponse<T>`
- token 鏍￠獙澶辫触鏃朵粛浼氬厛杩斿洖鐪熷疄 HTTP `401` 鏂囨湰 `Unauthorized`
- 杩涘叆 controller 鍚庣殑閿欒锛屽鏁颁粛鏄?HTTP `200` + `code != 200`

### 12.1 `POST /api/kafka/send`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛?  - `topic` 鍙€夛紝榛樿 `quickstart-events`
  - `key` 鍙€夛紝榛樿 `message`
  - `message` 蹇呭～
- 鎴愬姛锛歚MkApiResponse<String>`
- 涓氬姟閿欒鐮侊細
  - `403`锛氶潪绠＄悊鍛?  - `400`锛歚message` 涓虹┖
  - `503`锛欿afka 妯″潡鍏抽棴鎴?template 涓嶅彲鐢?  - `500`锛氬彂閫佸け璐?- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛氱己灏戝繀濉?`message` query 鍙傛暟

### 12.2 `GET /api/kafka/listen`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛?  - `topic` 鍙€?  - `key` 鍙€夛紝榛樿 `message`
  - `message` 蹇呭～
- 鎴愬姛锛歚MkApiResponse<String>`
- 涓氬姟閿欒鐮侊細
  - `403`锛氶潪绠＄悊鍛?  - `400`锛歚message` 涓虹┖
  - `503`锛欿afka 妯″潡鍏抽棴鎴?template 涓嶅彲鐢?  - `500`锛氳浆鍙戝け璐?- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛氱己灏戝繀濉?`message` query 鍙傛暟

娉ㄦ剰锛氳繖涓帴鍙ｅ綋鍓嶅彧鏄啀娆℃妸娑堟伅鍙戝埌 Kafka锛岀敤浜庤皟璇曪紝涓嶆槸姝ｅ紡娑堣垂璁㈤槄鎺ュ彛銆?
### 12.3 `GET /api/kafka/status`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- 鎴愬姛锛歚MkApiResponse<Map<String, Object>>`
- 涓氬姟閿欒鐮侊細
  - `403`锛氶潪绠＄悊鍛?
### 12.4 `GET /api/kafka/messages`

- 璁よ瘉锛氭槸
- 鏉冮檺锛歚admin`
- Query锛?  - `limit` 鍙€夛紝榛樿 `20`锛岃寖鍥?`1-100`
- 鎴愬姛锛歚MkApiResponse<KafkaMessageRecord[]>`
- 涓氬姟閿欒鐮侊細
  - `403`锛氶潪绠＄悊鍛?  - `400`锛歚limit` 涓嶅湪 `1-100`
- 杩涘叆 controller 鍓嶇殑閿欒锛?  - HTTP `400` + `ApiErrorResponse`锛歚limit` 涓嶆槸鍚堟硶鏁存暟

## 13. 支付模块 `/api/payments`

说明：
- 当前正式支付流程基于 Stripe Hosted Checkout
- 前端不再直接拼固定 Stripe 链接，而是先调用后端创建 Checkout Session，再跳转到后端返回的 `checkoutUrl`
- 后端会在本地维护 `membership_plan`、`payment_order`、`payment_subscription`、`payment_webhook_event` 四张表
- `/api/payments/webhooks/stripe` 是 Stripe 服务器回调接口，不给前端直接调用
- 除 webhook 外，本模块其他接口仍使用 `MkApiResponse<T>`

### 13.1 `GET /api/payments/status`

- 认证：是
- 权限：任意已登录用户
- 成功：`MkApiResponse<Map<String, Object>>`
- `data` 字段：
  - `enabled`：是否开启支付模块
  - `provider`：当前支付提供方，默认 `stripe`
  - `defaultCurrency`：默认币种
  - `implemented`：当前固定为 `true`
  - `apiReady`：是否已配置 `app.payment.secret-key`
  - `webhookReady`：是否已配置 `app.payment.webhook-secret`
  - `ready`：是否满足完整 Stripe 订阅流程所需配置
  - `billingPortalReturnUrlConfigured`：是否已配置默认 Billing Portal 返回地址
- 错误：
  - `401`：token 缺失/非法/过期

### 13.2 `GET /api/payments/plans`

- 认证：是
- 权限：任意已登录用户
- 成功：`MkApiResponse<PaymentPlanDTO[]>`

`PaymentPlanDTO` 示例：
```json
{
  "code": "pro_monthly",
  "name": "Pro Monthly",
  "description": "Monthly membership subscription",
  "currency": "gbp",
  "amountMinor": 990,
  "billingInterval": "month"
}
```

- 返回内容：只返回本地 `membership_plan` 表里 `active = 1` 的套餐
- 错误：
  - `401`：token 缺失/非法/过期

### 13.3 `GET /api/payments/orders`

- 认证：是
- 权限：任意已登录用户，仅返回当前登录用户自己的订单
- 成功：`MkApiResponse<PaymentOrderDTO[]>`

`PaymentOrderDTO` 示例：
```json
{
  "orderNo": "PO-20260309123000123-ABC123",
  "planCode": "pro_monthly",
  "planName": "Pro Monthly",
  "orderType": "subscription_checkout",
  "status": "paid",
  "currency": "gbp",
  "amountMinor": 990,
  "stripeCheckoutSessionId": "cs_test_123",
  "stripeInvoiceId": "in_123",
  "stripeSubscriptionId": "sub_123",
  "paidAt": "2026-03-09T12:31:00",
  "createdAt": "2026-03-09T12:30:00",
  "updatedAt": "2026-03-09T12:31:00"
}
```

- 订单类型：
  - `subscription_checkout`：用户主动创建 Checkout Session 的本地订单
  - `subscription_invoice`：Stripe 发票/续费同步回本地的订单
- 订单状态：
  - `pending`
  - `checkout_created`
  - `checkout_completed`
  - `paid`
  - `payment_failed`
- 错误：
  - `401`：token 缺失/非法/过期

### 13.4 `GET /api/payments/subscriptions/current`

- 认证：是
- 权限：任意已登录用户，仅返回当前登录用户自己的订阅
- 成功：`MkApiResponse<PaymentSubscriptionDTO>`

`PaymentSubscriptionDTO` 示例：
```json
{
  "active": true,
  "status": "active",
  "planCode": "pro_monthly",
  "planName": "Pro Monthly",
  "currency": "gbp",
  "amountMinor": 990,
  "billingInterval": "month",
  "currentPeriodStart": "2026-03-09T00:00:00",
  "currentPeriodEnd": "2026-04-09T00:00:00",
  "cancelAtPeriodEnd": false,
  "canceledAt": null,
  "stripeSubscriptionId": "sub_123"
}
```

无订阅时返回：
```json
{
  "active": false,
  "status": "none",
  "planCode": null,
  "planName": null,
  "currency": null,
  "amountMinor": null,
  "billingInterval": null,
  "currentPeriodStart": null,
  "currentPeriodEnd": null,
  "cancelAtPeriodEnd": false,
  "canceledAt": null,
  "stripeSubscriptionId": null
}
```

- 错误：
  - `401`：token 缺失/非法/过期

### 13.5 `POST /api/payments/checkout-sessions`

- 认证：是
- 权限：任意已登录用户
- Body：`PaymentCheckoutSessionRequest`

```json
{
  "planCode": "pro_monthly",
  "successUrl": "https://app.example.com/billing/success",
  "cancelUrl": "https://app.example.com/billing/cancel"
}
```

字段约束：
- `planCode`：必填，不能为空白
- `successUrl`：必填，必须是绝对 `http://` 或 `https://` URL
- `cancelUrl`：必填，必须是绝对 `http://` 或 `https://` URL

成功：`MkApiResponse<PaymentCheckoutSessionDTO>`

```json
{
  "orderNo": "PO-20260309123000123-ABC123",
  "planCode": "pro_monthly",
  "checkoutSessionId": "cs_test_123",
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_123",
  "status": "checkout_created"
}
```

业务错误码：
- `400`：请求体为空、`planCode` 为空、`successUrl/cancelUrl` 非法
- `404`：套餐不存在，或当前用户不存在
- `409`：当前用户已经有一个仍在管理中的订阅，应该改走 Billing Portal
- `503`：支付模块未启用，或 Stripe `secret-key / webhook-secret` 未配置完整
- `500`：Stripe 创建 Checkout Session 失败

联调动作：
1. 前端先调用本接口拿到 `data.checkoutUrl`
2. 再执行跳转到 Stripe Hosted Checkout
3. 不要继续使用写死的前端 Stripe 链接

### 13.6 `POST /api/payments/subscriptions/current/cancel`

- 认证：是
- 权限：任意已登录用户
- Body：无
- 成功：`MkApiResponse<PaymentSubscriptionDTO>`
- 语义：调用后不是立刻退订，而是设置 Stripe `cancel_at_period_end = true`
- 业务错误码：
  - `404`：当前用户不存在本地订阅记录，或本地没有 Stripe subscription id
  - `409`：当前订阅已经是非活跃状态
  - `503`：支付模块未启用，或 Stripe secret key 未配置
  - `500`：Stripe 更新订阅失败

### 13.7 `POST /api/payments/billing-portal-sessions`

- 认证：是
- 权限：任意已登录用户
- Body：可选 `PaymentBillingPortalSessionRequest`

```json
{
  "returnUrl": "https://app.example.com/account/billing"
}
```

字段约束：
- `returnUrl`：可选；如果不传，后端会回退到 `app.payment.billing-portal-return-url`
- 最终生效的返回地址必须是绝对 `http://` 或 `https://` URL

成功：`MkApiResponse<PaymentBillingPortalSessionDTO>`

```json
{
  "url": "https://billing.stripe.com/p/session/test_123"
}
```

业务错误码：
- `400`：`returnUrl` 非法，且配置里的默认返回地址也不可用
- `404`：当前用户还没有可用的 Stripe customer
- `503`：支付模块未启用，或 Stripe secret key 未配置
- `500`：Stripe 创建 Billing Portal Session 失败

### 13.8 `POST /api/payments/webhooks/stripe`

- 认证：否
- 用途：仅供 Stripe 服务器回调，不给前端直接调用
- Header：`Stripe-Signature` 必填
- Body：Stripe 原始 JSON payload
- 成功返回：HTTP `200`，纯文本 `ok`
- 失败返回：
  - HTTP `400`：payload 非法、签名缺失或签名校验失败
  - HTTP `503`：支付模块关闭，或 webhook secret 未配置
  - HTTP `500`：后端处理 webhook 失败

### 13.9 兼容保留接口（暂未实现）

以下接口仍保留，但当前不建议前端继续接入新流程：
- `POST /api/payments/intents`
- `POST /api/payments/intents/{paymentIntentId}/confirm`
- `POST /api/payments/intents/{paymentIntentId}/cancel`

这些接口当前仍返回：
- `503`：支付模块未启用
- `501`：接口保留但未实现

### 13.10 支付联调重点

- 前端支付入口现在应该是：
  1. `GET /api/payments/plans`
  2. 用户选择套餐
  3. `POST /api/payments/checkout-sessions`
  4. 跳转 `checkoutUrl`
- `successUrl` 页面只能代表 Stripe 页面跳回成功，不能当作本地会员一定已开通
- 真正写库和确认订阅状态，依赖 Stripe webhook
- 成功页建议在展示成功文案后，再请求：
  - `GET /api/payments/subscriptions/current`
  - `GET /api/payments/orders`
- 如果 success 页刚回来时 webhook 还没处理完，本地订阅可能短暂还是 `status = none` 或订单还没变成 `paid`，前端建议做短轮询而不是立即下结论
### 13.11 前端改造清单（支付）

前端如果之前是“点击按钮后直接跳固定 Stripe 链接”，现在必须改成下面这套流程：

- 结算入口改成先调用 `POST /api/payments/checkout-sessions`，再跳转返回的 `data.checkoutUrl`
- 不要再在前端写死 Payment Link、Checkout Link、`buy.stripe.com` 或 `checkout.stripe.com` 固定地址
- 支付页或订阅页初始化时，建议先请求：
  - `GET /api/payments/status`
  - `GET /api/payments/plans`
- 如果 `status.data.ready = false`，前端应禁用订阅按钮，并提示“支付暂不可用”
- 如果 `GET /api/payments/subscriptions/current` 返回 `active = true`，前端不要继续走 `checkout-sessions`，而是优先提供：
  - “管理订阅” -> `POST /api/payments/billing-portal-sessions`
  - “取消订阅” -> `POST /api/payments/subscriptions/current/cancel`
- 前端不要调用 `/api/payments/webhooks/stripe`，这个接口只给 Stripe 服务器和 Stripe CLI 转发使用
- 订单页或会员页建议展示本地订单状态，至少识别：
  - `checkout_created`
  - `paid`
  - `payment_failed`
- 成功页不要在前端本地直接把会员状态改成已开通，必须以后端接口结果为准

推荐页面行为：

1. 订阅页加载
   - 请求 `GET /api/payments/status`
   - 请求 `GET /api/payments/plans`
   - 请求 `GET /api/payments/subscriptions/current`
2. 如果当前无有效订阅
   - 展示套餐
   - 点击“订阅”时请求 `POST /api/payments/checkout-sessions`
   - 拿到 `checkoutUrl` 后立即跳转
3. 如果当前已有有效订阅
   - 展示当前套餐和到期时间
   - 展示“管理订阅”和“取消订阅”入口
4. 从 Stripe `successUrl` 返回后
   - 不要直接宣布“会员已开通”
   - 先轮询 `GET /api/payments/subscriptions/current`
   - 再轮询 `GET /api/payments/orders`

### 13.12 真实测试步骤（给前端联调）

当前开发环境已验证：

- `GET /api/payments/status` 返回 `ready = true`
- 当前测试套餐存在：
  - `planCode = pro_monthly`
  - `currency = gbp`
  - `amountMinor = 990`

建议前端按下面步骤做真实联调：

1. 登录获取 JWT
2. 打开订阅页，调用：
   - `GET /api/payments/status`
   - `GET /api/payments/plans`
3. 选择 `pro_monthly`
4. 调用 `POST /api/payments/checkout-sessions`

请求示例：
```json
{
  "planCode": "pro_monthly",
  "successUrl": "http://192.168.1.102/billing/success",
  "cancelUrl": "http://192.168.1.102/billing/cancel"
}
```

5. 浏览器跳转到返回的 `checkoutUrl`
6. 在 Stripe Hosted Checkout 完成测试支付
7. 跳回 `successUrl` 页面后，前端连续轮询以下接口，直到状态稳定：
   - `GET /api/payments/subscriptions/current`
   - `GET /api/payments/orders`

推荐轮询策略：

- 间隔：2 秒
- 次数：最多 10 次
- 停止条件：
  - 订阅 `status = active`，或
  - 最新订单 `status = paid`

真实测试通过的判定标准：

- `GET /api/payments/subscriptions/current` 返回：
  - `active = true`
  - `status = active`
  - `planCode = pro_monthly`
- `GET /api/payments/orders` 最新一条订单返回：
  - `status = paid`
  - `stripeSubscriptionId` 非空
- 后续再次进入订阅页时，前端应优先展示“当前订阅中”状态，而不是再次显示购买入口

如果 success 页面刚回来时仍看到：

- 订阅 `status = none`
- 或订单仍是 `checkout_created`

这通常表示 Stripe webhook 还在路上，前端应继续短轮询，不要立即判定失败。
## 14. 鍓嶇鑱旇皟閲嶇偣璇存槑

## 14.1 鍏堝鐞?401 绾枃鏈垎鏀?
鎵€鏈夊彈淇濇姢鎺ュ彛閮藉彲鑳藉湪杩涘叆 controller 涔嬪墠琚嫤鎴垚锛?
- HTTP `401`
- 鏂囨湰 `Unauthorized`

鍓嶇涓嶈鍋囧畾澶辫触涓€瀹氭槸 JSON銆?
## 14.2 `MkApiResponse` 妯″潡涓嶈兘鍙湅 HTTP 鐘舵€佺爜

璁よ瘉銆並afka銆佹敮浠樿繖涓夌被鎺ュ彛锛宑ontroller 鍐呴儴寰堝閿欒閮借繕鏄?HTTP `200`锛屽彧鏄?body 閲岀殑 `code != 200`銆備絾杩欎笁绫绘帴鍙ｄ篃鍙兘鍦ㄨ繘鍏?controller 鍓嶏紝鍏堣繑鍥?HTTP `400` 鐨?`ApiErrorResponse`銆?
鍓嶇鍒ゆ柇閫昏緫寤鸿锛?
1. 鍏堢湅鏄笉鏄?HTTP `401` 鏂囨湰 `Unauthorized`
2. 濡傛灉 body 鏄?`ApiErrorResponse` 缁撴瀯锛屾寜 HTTP 鐘舵€佺爜鍜?`message` 澶勭悊
3. 濡傛灉 body 鏄?`MkApiResponse` 缁撴瀯锛屽啀鐪?`body.code`
4. 鏅€?JSON 妯″潡鎸?HTTP 鐘舵€佺爜鍜?`ApiErrorResponse` 澶勭悊

## 14.3 SSE 鎺ュ彛鐨?header 闄愬埗

`GET /api/notifications/subscribe/{userId}` 闇€瑕?JWT锛屼絾娴忚鍣ㄥ師鐢?`EventSource` 涓嶆柟渚垮甫鑷畾涔?`Authorization` 澶达紝鑱旇皟鏃舵瀬鏄撶洿鎺ユ敹鍒?`401`銆?
## 14.4 SSE `message` 浜嬩欢 payload 闇€瑕佸吋瀹逛袱绉嶆牸寮?
褰撳墠 `SseEmitterServiceImpl` 瀛樺湪瀹炵幇宸紓锛?
- 鍗曞彂閫氱煡 `sendMessage()` 鎺ㄩ€佺殑鏄?JSON 瀛楃涓?- 骞挎挱閫氱煡 `sendMessageToAll()` 鎺ㄩ€佺殑鏄璞?
鍓嶇鐩戝惉 `message` 浜嬩欢鏃跺缓璁細

- 濡傛灉 `data` 鏄瓧绗︿覆锛屽厛灏濊瘯 `JSON.parse`
- 濡傛灉宸茬粡鏄璞★紝鐩存帴浣跨敤

## 14.5 Excel 涓嬭浇閿欒澶勭悊

Excel 鎴愬姛鏃惰繑鍥炰簩杩涘埗鏂囦欢锛屽け璐ユ椂鍙兘鏄?JSON 閿欒浣擄紝鍓嶇涓嬭浇閫昏緫瑕佸吋瀹硅繖涓ょ鎯呭喌銆?
## 14.6 褰撳墠鍚庣宸茬煡椋庨櫓

浠ヤ笅闂宸茬粡鍦ㄤ唬鐮?review 涓‘璁わ紝鍓嶇鑱旇皟鏃惰閲嶇偣鍏虫敞锛?
- 鏂板缓鍒嗙被鎴栨柊寤鸿褰曟垚鍔熷悗锛屽搷搴斾綋閲岀殑 `id` 鍙兘杩樻病鏈夊洖濉?- 鏂板缓璁板綍鍚庯紝鎼滅储绱㈠紩鍙兘涓嶄細绔嬪埢鍖呭惈璇ユ潯璁板綍
- 姹囨€绘帴鍙ｇ殑鏀跺叆/鏀嚭缁熻瀛樺湪瀹炵幇椋庨櫓锛岃仈璋冩椂璇烽噸鐐规牳瀵规暟鍊?- 鍒嗙被淇敼 `type` 鍚庯紝鏃ц褰曚笌鍒嗙被绫诲瀷鍙兘鍑虹幇涓嶄竴鑷?
## 15. 鍓嶇鎺ㄨ崘鎺ュ叆椤哄簭

1. 鍏堟帴 `/api/auth/login` 鑾峰彇 token
2. 缁熶竴灏佽 `Authorization` 娉ㄥ叆鍜?`401 Unauthorized` 鏂囨湰澶勭悊
3. 鍐嶆帴鐢ㄦ埛銆佸垎绫汇€佽褰曘€佹眹鎬?4. 鐒跺悗鎺?Excel銆侀€氱煡銆丼SE銆佹悳绱?5. Kafka銆佹敮浠樸€侀泦鎴愮姸鎬佸缓璁綔涓虹鐞嗙鎴栬皟璇曢〉闈娇鐢