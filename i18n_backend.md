# MoneyKeeper 后端国际化改造计划

## 1. 目标

这份文档只描述后端为了支持前端国际化需要做的改造，不包含前端页面翻译细节。

目标是：

- 后端协议字段保持稳定、语言无关
- 前端掌管最终展示文案
- 后端提供足够稳定的机器可读语义，供前端做 i18n 映射
- 后端保留必要的 fallback 文本和排障信息，但不再主导界面语言

## 2. 当前基线

当前已经满足的部分：

- `type` 协议值已统一为英文 `income` / `expense`
- 数据库存储中的 `type` 已统一为英文
- 角色和状态字段大多已经是英文稳定值
- API 文档已开始标注协议字段约定

当前仍然缺失的部分：

- 错误响应缺少稳定 `errorKey`
- 错误响应缺少 `errorParams`
- 通知和预算提醒缺少 `eventKey + payload`
- API 文档还没有完整列出错误键和事件键

## 3. 协议层原则

### 3.1 枚举字段必须语言无关

后端协议字段必须坚持返回英文稳定值，例如：

- `income` / `expense`
- `owner` / `admin` / `member`
- `pending` / `accepted` / `expired`
- `pending` / `running` / `completed` / `failed`
- `info` / `warning` / `success` / `error`

这些字段不能返回中文展示文案。

### 3.2 请求参数同样必须语言无关

前端请求里的协议枚举也必须使用英文标准值。

例如：

- 正确：`expense`
- 错误：`支出`

这类字段属于协议，不属于展示层。后端应严格校验，不再兼容中文别名。

### 3.3 展示文案归前端

后端不负责最终界面语言，只提供：

- 协议字段
- 稳定错误键
- 稳定事件键
- 业务参数
- fallback message

前端负责：

- 页面文案
- 枚举文案
- 错误文案
- 通知展示文案

## 4. 错误响应改造

### 4.1 目标结构

建议统一错误响应为：

```json
{
  "status": 400,
  "errorKey": "record.type_mismatch",
  "errorParams": {
    "expectedType": "expense"
  },
  "message": "Record type must match the selected category type",
  "path": "/api/records",
  "timestamp": "2026-03-16T22:00:00",
  "traceId": "TRACE-..."
}
```

字段说明：

- `status`：HTTP 状态码语义
- `errorKey`：稳定、语言无关的错误键
- `errorParams`：前端翻译时需要插值的参数
- `message`：fallback 文本
- `path`：请求路径
- `timestamp`：时间戳
- `traceId`：排障用

### 4.2 为什么保留 `message`

虽然前端最终不应依赖 `message` 作为主文案，但仍建议保留：

- 兼容旧前端和调试脚本
- Postman / curl / 日志里可直接阅读
- 当前端尚未配置对应 `errorKey` 翻译时可做 fallback
- 联调和排障时更高效

规则应明确为：

- 前端优先使用 `errorKey + errorParams`
- `message` 仅作为 fallback

### 4.3 第一批应补的错误键

建议优先覆盖这些高频错误：

- `common.bad_request`
- `common.unauthorized`
- `common.forbidden`
- `common.not_found`
- `common.conflict`
- `auth.invalid_credentials`
- `category.invalid_type`
- `record.invalid_type`
- `record.type_mismatch`
- `ledger.invite.already_member`
- `ledger.invite.email_mismatch`
- `budget.duplicate_budget`
- `budget.invalid_period`
- `export.job_not_ready`

## 5. 通知与业务事件改造

### 5.1 目标

通知、预算提醒、导出完成提醒，不应只返回纯文案，而应返回稳定事件语义。

推荐结构：

```json
{
  "id": 1,
  "type": "warning",
  "eventKey": "budget.threshold_reached",
  "payload": {
    "budgetName": "Coffee",
    "threshold": 40
  },
  "message": "Coffee budget is almost used up",
  "read": false,
  "createdAt": "2026-03-16T22:00:00"
}
```

### 5.2 前端使用规则

前端处理顺序：

1. 优先使用 `eventKey + payload`
2. 如果没有对应翻译，则使用 `message`

### 5.3 第一批建议补的事件键

- `budget.threshold_reached`
- `budget.threshold_cleared`
- `export.job_ready`
- `export.job_failed`
- `ledger.invite_received`
- `ledger.invite_accepted`

## 6. API 文档改造

`FRONTEND_API.md` 需要继续补充以下内容：

- 哪些字段是协议枚举
- 哪些字段只允许英文标准值
- 哪些错误会返回 `errorKey`
- 哪些通知会返回 `eventKey`
- 哪些字段只是 fallback message

## 7. 实施顺序

建议按这个顺序做：

1. 保持协议字段严格英文
2. 给全局错误响应加 `errorKey`
3. 给高频错误补 `errorParams`
4. 给通知与预算提醒加 `eventKey + payload`
5. 更新 `FRONTEND_API.md`
6. 再由前端全面切换到 i18n 渲染

## 8. 暂不做的内容

这一轮后端不需要做：

- 页面文案翻译
- 日期格式国际化
- 金额格式国际化
- 数字格式国际化
- 前端 locale 切换逻辑

这些都属于前端展示层职责。

## 9. 验收标准

后端国际化支持完成的标准应是：

- 协议枚举字段全部语言无关
- 请求协议字段也只接受标准英文值
- 错误响应具备 `errorKey`
- 高频错误具备 `errorParams`
- 通知/提醒具备 `eventKey + payload`
- API 文档明确区分协议字段和展示字段

## 10. 当前结论

当前最优先的后端 i18n 支撑项，不是继续调整 `type`，而是：

1. 错误响应 `errorKey`
2. 错误参数 `errorParams`
3. 通知事件 `eventKey + payload`

这三项补齐后，前端 i18n 才有稳定的后端语义基础。
