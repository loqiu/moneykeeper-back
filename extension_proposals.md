# MoneyKeeper Backend 扩展方案

基于对现有代码库的分析，以下是针对 MoneyKeeper 后端服务的详细功能扩展建议。这些建议旨在提升系统的实用性、灵活性和技术先进性。

## 1. 核心业务功能扩展

### 1.1 预算管理 (Budget Management)
**现状**: 系统仅支持记账，缺乏支出控制手段。
**建议**:
- **新增实体**: `Budget`
    - 关联 `User` 和 `Category` (可选，若为空则为总预算)。
    - 字段: `amount` (预算金额), `period` (周期: MONTHLY/YEARLY), `startDate`, `endDate`。
- **业务逻辑**:
    - 在用户新增支出记录时，异步检查是否超出预算。
    - 提供 API 查询当前预算使用情况 (已用/剩余/百分比)。
- **通知集成**:
    - 当支出达到预算的 80%, 90%, 100% 时，通过 `NotificationController` (SSE) 推送实时警报。

### 1.2 多币种支持 (Multi-Currency Support)
**现状**: `MoneyKeeper` 实体仅有 `amount` 字段，默认单币种。
**建议**:
- **数据库变更**:
    - `moneykeeper` 表增加 `currency_code` (如 CNY, USD) 和 `exchange_rate` (记录时的汇率)。
    - `users` 表增加 `default_currency` 字段。
- **功能增强**:
    - 记账时支持选择币种。
    - 统计报表时，根据实时或记录时的汇率将所有支出折算为用户的默认币种进行汇总。
    - 集成第三方汇率 API (如 Open Exchange Rates) 定时更新汇率缓存。

### 1.3 周期性/固定收支 (Recurring Transactions)
**现状**: 用户需手动输入每一笔重复发生的账单（如房租、订阅费）。
**建议**:
- **新增实体**: `RecurringTransaction`
    - 字段: `frequency` (DAILY, WEEKLY, MONTHLY), `next_execution_time`, `template_data` (金额, 分类, 备注)。
- **技术实现**:
    - 使用 Spring `@Scheduled` 定时任务，每天扫描到期的周期性事务。
    - 自动在 `moneykeeper` 表生成记录，并发送“已自动记账”的通知。

### 1.4 灵活标签系统 (Tagging System)
**现状**: `Category` 是扁平结构，且一笔账单只能属于一个分类。
**建议**:
- **新增实体**: `Tag` 和关联表 `moneykeeper_tags`。
- **功能**:
    - 允许用户为一笔账单打多个标签 (e.g., `#出差`, `#报销`, `#杭州之旅`)。
    - 提供基于标签的搜索和统计 API。
    - 相比分类，标签更适合跨维度的分析。

## 2. 技术架构优化

### 2.1 数据导出与报表增强
**现状**: 仅支持 Excel 下载 (`ExcelDownloadController`)。
**建议**:
- **PDF 报表**: 使用 iText 或 JasperReports 生成精美的月度消费分析报告（包含图表）。
- **账单导入**: 支持解析常见银行或支付宝/微信的 CSV 账单文件，批量导入数据。

### 2.2 AI 智能消费洞察
**现状**: 简单的 CRUD 和统计。
**建议**:
- **集成 AI**:
    - 利用 Spring AI 或直接调用 LLM API。
    - 功能: "分析我上个月的餐饮支出，并给出省钱建议"。
- **异常检测**:
    - 基于历史数据，自动识别异常的大额消费或频率异常的支出。

### 2.3 微服务架构演进 (可选)
**现状**: 单体应用，但已引入 Dubbo/Nacos 依赖。
**建议**:
- 如果计划扩展，可以将非核心业务拆分：
    - **Notification Service**: 独立处理 SSE、邮件、短信推送。
    - **Report Service**: 专门处理耗时的报表生成和数据导出。
- 利用 Kafka 解耦记账主流程和后续的通知/分析流程。

## 3. 用户体验 (API 层面)

### 3.1 图表优化接口
**现状**: 前端可能需要拉取大量明细数据自行计算。
**建议**:
- 后端直接返回聚合好的图表数据 (JSON 格式)，适配 ECharts 或 Chart.js。
    - e.g., `GET /api/stats/trend?type=expense&period=year` 返回 `[{"month": "Jan", "amount": 1000}, ...]`。

### 3.2 移动端适配
**建议**:
- 开发专门的 Mobile BFF (Backend for Frontend) 接口，聚合首页所需的所有数据 (总览、最近几笔记录、预算状态)，减少移动端的网络请求次数。
