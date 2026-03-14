[**CN 中文**](./README_zh.md) | [**GB English**](./README.md)

# MoneyKeeper Backend

MoneyKeeper Backend 是一个基于 Spring Boot 3 的记账后端服务，支持个人账本、共享账本、预算、通知、导出、搜索和支付能力。

## 项目亮点

- 支持个人账本和共享账本
- 支持成员角色、邀请和接受邀请
- 支持账本维度的分类、记录、汇总和统计
- 支持预算、阈值规则和通知日志
- 支持异步 Excel 导出任务
- 支持 Elasticsearch 记录搜索
- 支持 Kafka 事件链路，并保留本地兜底
- 支持 Stripe 支付能力
- 使用 Flyway 管理数据库迁移

## 技术栈

- Java 17
- Spring Boot 3.2
- MyBatis-Plus 3.5
- MySQL 8
- Redis
- Elasticsearch
- Kafka
- Flyway
- Log4j2
- JWT
- Springdoc OpenAPI

## 目录结构

```text
src/main/java/com/loqiu/moneykeeper/
|- config/               应用与中间件配置
|- controller/           REST 接口层
|- dto/                  返回 DTO
|- entity/               MyBatis-Plus 实体
|- exception/            异常处理
|- health/               Actuator 健康检查
|- interceptor/          请求拦截器
|- mapper/               MyBatis Mapper 接口
|- service/              服务接口
|- service/impl/         业务实现
|- util/                 通用工具
|- vo/                   请求对象

src/main/resources/
|- application*.properties
|- db/migration/         Flyway 迁移脚本
|- mapper/               MyBatis XML
```

## 核心模块

- 认证与用户：用户名密码登录、JWT、用户资料接口
- 账本协作：默认个人账本、共享账本、成员管理、邀请与接受邀请
- 分类与记录：账本维度 CRUD、汇总能力
- 周期统计：周 / 月 / 年统计分析
- 预算：月度预算、阈值规则、进度计算
- 通知中心：未读数、标记已读、预算提醒、导出完成通知
- 导出任务：异步 Excel 导出与下载
- 搜索：Elasticsearch 记录索引与查询
- 支付：会员计划、结账、订阅、Webhook

## 文档入口

- 前端接口文档：[FRONTEND_API.md](./FRONTEND_API.md)
- 前端交接文档：[PLATFORM_FRONTEND_HANDOFF.md](./PLATFORM_FRONTEND_HANDOFF.md)
- 部署说明：[deploy/README.md](./deploy/README.md)
- Swagger UI：`/swagger-ui.html`
- OpenAPI JSON：`/v3/api-docs`

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Redis

如果需要完整能力，建议同时准备：

- Elasticsearch
- Kafka

### 本地启动

1. 创建数据库：

```sql
CREATE DATABASE moneykeeper CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 设置环境变量：

```powershell
$env:MONEYKEEPER_DB_URL="jdbc:mysql://localhost:3306/moneykeeper?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:MONEYKEEPER_DB_USERNAME="root"
$env:MONEYKEEPER_DB_PASSWORD="your-password"
$env:MONEYKEEPER_REDIS_HOST="localhost"
$env:MONEYKEEPER_REDIS_PORT="6379"
$env:SPRING_PROFILES_ACTIVE="dev"
```

3. 启动项目：

```powershell
mvn spring-boot:run
```

4. 检查健康状态：

```powershell
curl http://localhost:8081/actuator/health
```

应用启动时会自动执行 Flyway 迁移。

## 配置说明

项目当前主要通过环境变量控制配置，常用项包括：

- `SPRING_PROFILES_ACTIVE`
- `MONEYKEEPER_DB_URL`
- `MONEYKEEPER_DB_USERNAME`
- `MONEYKEEPER_DB_PASSWORD`
- `MONEYKEEPER_REDIS_HOST`
- `MONEYKEEPER_REDIS_PORT`
- `MONEYKEEPER_REDIS_DATABASE`
- `MONEYKEEPER_ELASTICSEARCH_ENABLED`
- `MONEYKEEPER_ELASTICSEARCH_HOST`
- `MONEYKEEPER_ELASTICSEARCH_PORT`
- `MONEYKEEPER_ELASTICSEARCH_INDEX_NAME`
- `MONEYKEEPER_KAFKA_ENABLED`
- `MONEYKEEPER_KAFKA_BOOTSTRAP_SERVERS`
- `MONEYKEEPER_KAFKA_RECORD_EVENT_TOPIC`
- `MONEYKEEPER_KAFKA_EXPORT_JOB_TOPIC`
- `MONEYKEEPER_EXPORT_JOB_STORAGE_DIR`
- `MONEYKEEPER_PAYMENT_ENABLED`
- `MONEYKEEPER_PAYMENT_SECRET_KEY`
- `MONEYKEEPER_PAYMENT_WEBHOOK_SECRET`

完整默认值和开关定义见：

- [src/main/resources/application.properties](./src/main/resources/application.properties)
- [src/main/resources/application-dev.properties](./src/main/resources/application-dev.properties)
- [src/main/resources/application-prod.properties](./src/main/resources/application-prod.properties)

## 测试

执行测试：

```powershell
mvn test
```

当前仓库已经覆盖了控制器、服务、健康检查和事件流相关测试。

## 部署说明

- 生产数据现在统一落在共享 MySQL 的 `moneykeeper` 数据库
- 生产环境复用共享的 MySQL、Redis、Elasticsearch、Kafka
- 数据库结构以 Flyway 迁移为准
- 导出文件建议挂载到宿主机持久化目录

宿主机侧部署辅助文件位于 [deploy](./deploy)。

## 当前状态

已完成并上线：

- 共享账本模型
- 预算基础能力
- 通知中心
- 导出任务链路
- 搜索与统计
- Kafka 事件链路
- 主库 `moneykeeper` 的正式切换

仍在持续迭代：

- 前端联调细节
- 个别历史字段的请求归一化
- 更多运维与稳定性强化

## 许可证

MIT，详见 [LICENSE](./LICENSE)。
