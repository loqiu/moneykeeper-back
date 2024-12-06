# MoneyKeeper Backend

MoneyKeeper是一个现代化的个人记账管理系统的后端服务，旨在帮助用户更好地管理个人财务。

## 项目概览

### 项目简介
MoneyKeeper后端服务提供了完整的记账功能API支持，包括用户管理、收支记录、分类管理等核心功能。项目采用Spring Boot 3.x + MyBatis-Plus构建，提供RESTful API接口。

### 主要特性
- 用户管理：注册、登录、信息修改
- 分类管理：支持自定义收支分类
- 记账管理：收入支出记录的CRUD操作
- 数据统计：按日期、类型查询统计
- 完整的日志记录
- 软删除支持

### 项目状态
- 当前版本：0.0.1-SNAPSHOT
- 开发状态：积极开发中
- 稳定性：开发测试阶段

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 安装步骤
1. 克隆项目
bash
git clone https://github.com/loqiu/moneykeeper-back.git

2. 配置数据库
sql
CREATE DATABASE moneykeeper;

3. 修改配置文件
```java
properties
src/main/resources/application-dev.properties
spring.datasource.url=jdbc:mysql://localhost:3306/moneykeeper
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. 运行项目
bash
mvn spring-boot:run


## 项目架构

### 技术栈
- Spring Boot 3.4.0
- MyBatis-Plus 3.5.9
- MySQL 8.0
- Log4j2
- JWT (用于身份认证)
- Swagger (API文档)

### 项目结构
```
src/main/java/com/loqiu/moneykeeper/
├── config/ # 配置类
├── controller/ # 控制器层
├── entity/ # 实体类
├── mapper/ # MyBatis映射层
├── service/ # 服务层接口
│ └── impl/ # 服务层实现
└── vo/ # 视图对象
```

## API文档

### 用户相关API
```http
POST /api/auth/login # 用户登录
GET /api/users/{id} # 获取用户信息
POST /api/users # 创建用户
PUT /api/users/{id} # 更新用户信息
DELETE /api/users/{id} # 删除用户
```
### 分类相关API
```http
GET /api/categories/list # 获取所有分类
GET /api/categories/user/{id} # 获取用户分类
POST /api/categories/{id} # 创建分类
PUT /api/categories/{id} # 更新分类
DELETE /api/categories/{id} # 删除分类
```
### 记账相关API
```http
GET /api/records/list # 获取所有记录
GET /api/records/list/{userId} # 获取用户记录
POST /api/records # 创建记录
PUT /api/records/{id} # 更新记录
DELETE /api/records/{id} # 删除记录
```
## 开发指南

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Log4j2进行日志记录
- 统一使用ResponseEntity封装响应

### 分支管理

- main: 主分支，用于发布
- develop: 开发分支
- feature/*: 特性分支
- hotfix/*: 紧急修复分支

## 测试

### 单元测试

bash
```
mvn test
```
### 接口测试

使用DatabaseTest类测试数据库连接：
```
bash
mvn test -Dtest=DatabaseTest
```
## 部署

### 打包
```
bash
mvn clean package
```
### 运行
```
bash
java -jar target/moneykeeper-back-0.0.1-SNAPSHOT.war
```
## 维护与支持

### 问题反馈
- 通过GitHub Issues提交问题
- 邮件联系: loqiu.software@gmail.com

### 更新日志
详见[CHANGELOG.md](./CHANGELOG.md)

## 许可证
本项目采用MIT许可证，详见[LICENSE](./LICENSE)文件。

## 贡献者
- [loqiu](https://github.com/loqiu)

## 致谢
感谢所有为这个项目做出贡献的开发者。


