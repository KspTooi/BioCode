# bio-code 项目底座能力研究报告

---

## A) 项目总体概述

| 维度 | 详情 |
|------|------|
| **项目名称** | `bio-code`（服务名 `bio-code`，Maven artifact `wproc-main`） |
| **组织** | `com.ksptool` |
| **语言/版本** | Java 21 |
| **构建工具** | Maven（POM 继承 `spring-boot-starter-parent:4.0.6`），前端 `pnpm 10.33.0` |
| **主启动类** | `com.ksptool.bio.BioRunner` |
| **启动端口** | `27500` |
| **当前版本** | `1.6Z57` |
| **部署形态** | Spring Boot 单体 Jar + 内嵌前端 SPA（`with-web-ui` Maven profile 可打包前端产物进 classpath） |
| **运行时环境** | JDK 21, Windows/Linux, MySQL 5.7+ 或 H2 |
| **前端技术栈** | Vue 3.5 + TypeScript 6.0 + Vite 8.0 + Element Plus 2.13 + Pinia 3.0 |
| **来源风格** | 内部私有项目，作者 `KspTooi / KspTool`，许可证 Apache-2.0 |

---

## B) 模块清单与职责（排除业务域 document/drive/rdbg/relay）

### B.1 后端基础模块（按 biz 包划分）

#### 1. `core` — 核心系统底座 (`biz/core/`，约 175 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **用户管理** | `UserService`, `UserController`, `UserPo`, `UserRepository` | 用户 CRUD、注册、批量编辑、登录 |
| **组织架构** | `OrgService`, `OrgController`, `OrgRepository` | 部门树管理 |
| **菜单系统** | `MenuService`, `MenuController`, `MenuRepository` | 菜单与按钮树 CRUD、用户菜单树 |
| **岗位管理** | `PostService`, `PostController`, `PostPo` | 岗位 CRUD |
| **租户隔离** | `CoreRootService`, `CoreRootController`, `CoreRootPo`, `RootCreateEvent`, `RootRemoveEvent` | 多租户管理（Root 即租户） |
| **附件模块** | `AttachService`, `AttachPo`, `AttachChunkPo`, `AttachConfig` | 文件附件上传下载、分块存储 |
| **消息通知** | `NoticeService`, `NoticeTemplateService`, `NoticeRcdService`, `NoticePo`, `NoticeRcdPo` | 站内通知发送与模板管理 |
| **注册表系统** | `RegistryService`, `RegistrySdk`, `RegistryController`, `AppRegistry`（枚举） | 系统级动态配置管理（KV结构树） |
| **应用状态监控** | `AppStatusService` | 基于 OSHI 的实时硬件监控（CPU/内存/网络/磁盘 IO），定时采样 |
| **安装向导** | `AppInstallWizardInterceptor`, `AppVersion` | 版本升级检测与向导模式 |
| **菜单包** | `PackService`, `PackController`, `RootPackPo` | 菜单包分发与管理 |
| **维护中心** | `MaintainService`, `MaintainRepository` | 数据库维护操作 |
| **Excel 模板** | `ExcelTemplateService`, `ExcelTemplatePo` | Excel 导入导出模板管理 |
| **通用 JPA 基础设施** | `SnowflakeIdGenerated`, `SnowflakeIdGenerator`, `TupleMapper`, `TreeBuilder`, `IdsDiff`, `AppRegistry` | 雪花 ID 生成、Tuple→VO 映射、平面列表转树、ID 差异比较 |
| **通用事件** | `BizEvent`, `RootCreateEvent`, `RootRemoveEvent`, `UserCreateEvent`, `UserRemoveEvent` | 基类事件与租户/用户生命周期事件 |
| **Dto 校验 AOP** | `DtoCustomValidator`, `DtoCustomValidatorAspect` | 自定义 DTO 校验框架 |

#### 2. `auth` — 认证与授权 (`biz/auth/`，约 82 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **Spring Security 集成** | `SecurityConfig` | 关 CSRF、无状态 Session、仅保留 USAF 过滤器、统一 JSON 认证异常 |
| **用户会话认证** | `UserSessionAuthFilter` (USAF), `AuthUserSession`, `UserSessionPo` | Token→用户会话重建，Cookie/Header 鉴权 |
| **用户登录** | `AuthController`, `AuthService`, `UserLoginDto`, `UserLoginVo` | 登录/登出、验证码 |
| **用户组管理** | `GroupService`, `GroupController`, `GroupPo`, `UserGroupPo` | 用户组 CRUD、组成员管理、组权限分配 |
| **权限管理** | `PermissionService`, `PermissionController`, `PermissionPo`, `PermissionCode` | 权限端点 CRUD、权限定义 |
| **行级权限** | `RowScopeAspect`, `RowScope`, `RowScopes`, `RsAuditingEntityListener`, `RsCalculator`, `RsBuilder`, `RsContext`, `RowScopePo` | 数据行级权限过滤（基于 MyBatis Context） |
| **系统范围权限** | `SystemScopeAspect`, `SystemScope`, `SystemScopePo` | 用户跨租户权限控制 |
| **会话服务** | `SessionService`, `UserSessionRepository`, `AuthUserSession` | 会话 CRUD、过期管理 |
| **个人信息** | `UserProfileService`, `UserProfileController` | 个人资料、密码修改 |
| **动态全局白名单** | `DynamicGlobalWhiteManager` | 动态权限判定管理器 |
| **密码策略** | `BCryptPasswordEncoder` | 密码加密 |

#### 3. `assembly` — 装配与代码生成引擎 (`biz/assembly/`，约 77 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **数据源管理** | `DataSourceService`, `DataSourceController`, `DataSourcePo` | 外部数据库连接管理 |
| **原始模型** | `RawModelService`, `RawModelController`, `RawModelPo` | 表结构元数据定义 |
| **类型映射方案** | `TymSchemaService`, `TymSchemaController`, `TymSchemaPo` | 数据库类型 → Java/Vue 类型映射 |
| **表模型** | `TymSchemaFieldService`, `TymSchemaFieldController`, `TymSchemaFieldPo` | 表字段详细建模 |
| **聚合模型** | `PolyModelService`, `PolyModelPo` | 跨表聚合查询模型 |
| **输出方案** | `OpSchemaService`, `OpSchemaPo` | 蓝图输出配置与执行 |
| **SCM 管理** | `ScmService`, `ScmController`, `ScmPo` | 源代码/文件管理 |
| **蓝图快速构建引擎** | `QbeBlueprintReader`, `QbeBlueprint`, `QbeVelocityEngine`, `QbeModel` | 基于 Velocity 模板的代码生成引擎 |
| **HTTP 连接** | `InsecureHttpConnectionFactory` | 代码生成器内部 HTTP 连接工厂 |

#### 4. `audit` — 审计模块 (`biz/audit/`，约 19 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **登录审计** | `AuditLoginService`, `AuditLoginController`, `AuditLoginPo` | 用户登录行为记录 |
| **错误审计** | `AuditErrorRcdService`, `AuditErrorRcdController`, `AuditErrorRcdPo` | 系统错误记录与异步记录 |
| **认证监听** | `AuthAuditListener` | 认证事件监听 |

#### 5. `qf` — 流程引擎 (`biz/qf/`，约 103 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **Flowable 工作流集成** | `QfFlowableConfig`, Flowable 8.0.0 | 流程引擎配置与管理 |
| **流程模型管理** | `QfModelService`, `QfModelController`, `QfModelPo`, `DesignQfModelDto` | 流程模型 CRUD、设计 |
| **流程发起/执行** | `QfProcService`, `QfProcController` | 流程实例启动、变量注入 |
| **待办管理** | `QfTodoService`, `QfTodoController`, `QfTodoPo` | 待办 CRUD、审批、节点配置 |
| **抄送管理** | `QfCcService`, `QfCcController`, `QfCcPo` | 抄送人管理 |
| **部署记录** | `QfModelDeployRcdController`, `QfModelDeployRcdRepository` | 流程部署版本记录 |
| **业务表单** | `QfBizFormService`, `QfBizFormController`, `QfBizFormFieldService`, `QfBizFormFieldController` | 流程关联的业务表单与字段配置 |
| **流程事件监听** | `QfTaskCreatedListener`, `QfTaskAssignedListener`, `QfTaskFinishedListener`, `QfTaskCancelledListener` | 引擎任务事件回调处理 |
| **扩展模型** | `QfeBpmnModel`, `QfeUserTask`, `QfeDescriptor.json` | 自定义 BPMN 扩展（用户任务属性） |
| **成员管理** | `QfMemberService`, `QfMemberServiceImpl`, `QfMemberKinds` | 流程成员（用户/组/部门/任何人）解析 |
| **引擎工具** | `Flowable8NodeUtil`, `QfModelTools`, `QfVarsModel`, `QfVarsProc`, `LaunchParam` | 节点遍历、流程变量模型 |

#### 6. `qt` — 快速任务调度 (`biz/qt/`，约 41 文件)

| 子域 | 关键类/文件 | 职责 |
|------|-----------|------|
| **任务管理** | `QtTaskService`, `QtTaskController`, `QtTaskPo` | 任务 CRUD、导入导出、执行 |
| **任务组** | `QtTaskGroupService`, `QtTaskGroupController`, `QtTaskGroupPo` | 任务分组管理 |
| **执行记录** | `QtTaskRcdService`, `QtTaskRcdController`, `QtTaskRcdPo` | 任务执行历史记录 |
| **即时任务机制** | `QuickTask`, `QuickTaskRegistry`, `LocalBeanExecutionJob` | 通过注解将 Spring Bean 方法注册为可调度的快速任务 |
| **Cron 计算** | `cron-parser`, `cronstrue`（前端） | Cron 表达式计算与人类可读翻译 |
| **Quartz 集成** | `spring-boot-quartz 4.1.0-RC1`, `TestJobBean`, `TestJobExceptionBean` | 底层基于 Quartz JDBC JobStore |

---

### B.2 前端基础模块 (`web-ui/src/`)

| 子域 | 关键目录/文件 | 职责 |
|------|-----------|------|
| **SOA 通用组件** | `soa/com-series/` | 布局提供器(`ComLayoutProvider`)、序列修改器(`ComSeqFixer`)、面包屑、用户信息、标签页、Cron 修复器、图标选择器、公共组件选择器等 |
| **SOA 标准列表布局** | `soa/std-series/` | 标准列表容器(`StdListContainer`)、查询区(`StdListAreaQuery`)、表格区(`StdListAreaTable`)、操作区(`StdListAreaAction`)、分页、指标教程指示器、高级树等 |
| **SOA 通用路由** | `soa/genric-route/` | 通用路由注册器、冲突检测 |
| **SOA 模板** | `soa/template/` | 标准页面模板示例 (`Example.vue`) |
| **共享模块** | `commons/` | HTTP 客户端(`Http.ts`)、通用 DTO 模型(`CommonIdDto`)、分页模型(`PageResult`/`PageQuery`/`RestPageableView`)、响应模型(`Result`)、查询持久化服务、文件类型服务、快捷键服务等 |
| **核心业务前端** | `views/core/` | 用户/组织/岗位/菜单/通知/注册表/Excel模板/打包管理 |
| **认证前端** | `views/auth/` | 用户组/权限/会话/个人信息 |
| **装配前端** | `views/assembly/` | 数据源管理、原始模型、表模型、输出方案设计 |
| **流程前端** | `views/qf/` | 流程模型、流程管理、待办、抄送、表单设计、Flowable 设计器(基于 `bpmn-js`) |
| **任务前端** | `views/qt/` | 快速任务管理、Cron 计算器 |
| **审计前端** | `views/audit/` | 登录审计、错误记录 |
| **技术演示** | `views/playground/` | 各组件的 Playground 演示页 |

---

## C) 核心能力矩阵

### C.1 Web 服务框架

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| HTTP 服务 | Spring Boot 4.0.6 + Spring MVC | `BioRunner.java`, `pom.xml` |
| 嵌入式服务器 | Tomcat（Spring Boot 默认） | `application.yml` (`server.port: 27500`) |
| 数据校验 | `spring-boot-starter-validation` + `jakarta.validation` | `GlobalExceptionHandler.handleValidationExceptions()` |
| 文件上传 | Servlet Multipart（max 100MB） | `application.yml` (`spring.servlet.multipart`) |
| HTTP 调用 | `java.net.http.HttpClient` (JDK 21 内置) | `RepeaterConfig.java` |
| API 文档 | SpringDoc OpenAPI 3.0.1 | `OpenApiConfig.java`, `/v3/api-docs` |
| CORS | 未显式配置（默认允许同源），集成部署通过前缀 `/api` 区分前后端 | `IntegratedDeployConfig.java` |

### C.2 数据持久化

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **ORM** | JPA / Hibernate 6.x（Spring Boot 4.0.6 默认） | `pom.xml` (`spring-boot-starter-data-jpa`) |
| **方言** | MySQLDialect | `application.yml` (`jpa.database-platform`) |
| **DDL 策略** | `none`（禁用 Hibernate 自动 DDL） | `application.yml` (`jpa.hibernate.ddl-auto: none`) |
| **MyBatis** | MyBatis-Spring-Boot 4.0.1（复杂查询场景） | `pom.xml`, `application.yml` (`mybatis.mapper-locations`) |
| **数据库主库** | MySQL (Connector/J) | `application.yml` (`mysql-connector-j`) |
| **数据库备选** | H2 2.4.240（开发/嵌入式模式） | `pom.xml`, `H2Server.java` (TCP端口 1109) |
| **数据库迁移** | Flyway（手动触发，禁止自动迁移） | `FlywayConfig.java`, `sql/automatic/` (46 个 SQL) |
| **分页抽象** | `spring-data-commons` `Pageable` / `Page<T>` | `dto.pageRequest()` 模式 |
| **PO↔VO 映射** | ModelMapper（`Entities.as`/`assign`）+ 自定义 `EntityMapperConfig` | `EntityMapperConfig.java` |
| **Tuple→VO 映射** | 自研 `TupleMapper.tupleAs()` | `TupleMapper.java` |
| **JSON 序列化(PO字段)** | Gson（`JsonEntityMapperConfig`） | `JsonEntityMapperConfig.java` |
| **Jackson 配置** | `yyyy-MM-dd HH:mm:ss`, GMT+8 时区 | `application.yml`, `JacksonConfig.java` |
| **ID 生成** | xyz.downgoon Snowflake + 自定义 `SnowflakeIdGenerated` 注解（Hibernate IdGeneratorType） | `IdWorker.java`, `SnowflakeIdGenerator.java` |
| **懒加载** | `spring.main.lazy-initialization: true` | `application.yml` |

### C.3 安全认证

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **认证框架** | Spring Security (Web) | `SecurityConfig.java` |
| **会话模型** | 无状态 Token（Bearer Auth + Cookie 可选） | `UserSessionAuthFilter.java` |
| **密码编码** | BCryptPasswordEncoder | `SecurityConfig.java` |
| **CSRF** | 已禁用（纯 Token 鉴权） | `SecurityConfig.java` |
| **统一 JSON 认证异常** | `JsonAuthEntryPoint` | `JsonAuthEntryPoint.java` |
| **动态权限白名单** | `DynamicGlobalWhiteManager`（替代 Spring Security 默认规则） | `SecurityConfig.java`, `auth/common/` |
| **端点权限** | `PermissionPo` → 每个后端 API 端点可配置权限 | `PermissionService.java` |
| **行级权限(RS)** | 自研行级权限框架：`RowScope`注解 + `RowScopePo` 接口 + `RsAuditingEntityListener` + `RsCalculator` | `auth/common/aop/`, `auth/common/` |
| **系统级权限** | `SystemScope` 注解 + `SystemScopeAspect` 切面 | `SystemScopeAspect.java` |
| **方法级权限** | `@EnableMethodSecurity` → `@PreAuthorize` 等 | `SecurityConfig.java` |
| **验证码** | Tianai Captcha (滑块/旋转/文字点选)，100 张背景图 | `CaptchaResourceConfiguration.java`, `pom.xml` |
| **密码策略** | 注册表可配置：弱密码、用户名/密码包含、特殊字符、长度 | `AppRegistry.java` (`FA_ASP_*`) |
| **登录保护** | 最大尝试次数、锁定时间（注册表可配置） | `AppRegistry.java` (`FA_ASP_MAX_ATTEMPTS_*`) |
| **用户注册** | 注册表可控开关 + 验证码保护 | `AppRegistry.java` (`FA_ALLOW_USER_REGISTER`) |

### C.4 消息与事件

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **应用内事件** | Spring ApplicationEvent (`BizEvent` 基类) | `biz/core/common/BizEvent.java` |
|  |   | `biz/core/common/event/` (RootCreate, RootRemove, UserCreate, UserRemove) |
|  |   | `biz/qf/commons/event/` (QfTaskCreated, QfTaskAssigned, QfTaskFinished, QfTaskCancelled, QfProcStarted, QfProcFinished) |
| **站内通知** | `NoticeService` + `NoticePo` / `NoticeRcdPo` | `biz/core/service/NoticeService.java` |
| **异步支持** | `@EnableAsync` (Spring) | `BioRunner.java` |
| **外部消息队列** | **未引入** | — |

### C.5 配置管理

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **外部配置** | `application.yml` + Spring Boot 配置处理器 | `src/main/resources/application.yml` |
| **动态配置注册表** | 自研 `AppRegistry` 枚举 + `RegistryService` + `RegistrySdk` | `AppRegistry.java`, `RegistryService.java`, `RegistrySdk.java`, `RegistryController.java` |
| **注册表持久化** | MySQL (`core_registry` 表) | 通过 `RegistryService` 操作 |
| **注册表缓存** | Caffeine Cache（15分钟过期，10000 条上限） | `CacheConfig.java` (`registry` cache) |
| **Profile 切换** | Spring Profiles (`dev` 默认) | `application.yml` |
| **Spring 启动分析** | `spring-startup-analyzer.properties` | `support-static/spring-profiler/config/` |

### C.6 任务调度

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **定时任务框架** | Quartz Scheduler (JDBC JobStore) | `pom.xml` (`spring-boot-quartz 4.1.0-RC1`), `application.yml` |
| **调度管理** | 数据库持久化（`initialize-schema: never`, `auto-startup: false`，由应用层控制） | `application.yml` |
| **即时任务注册** | `QuickTask` 注解 + `QuickTaskRegistry` → 扫描 Spring Bean 方法为可调度任务 | `biz/qt/common/QuickTask.java`, `QuickTaskRegistry.java` |
| **本地 Bean 执行** | `LocalBeanExecutionJob`（实现 Quartz Job 接口，反射调用 Bean 方法） | `LocalBeanExecutionJob.java` |
| **任务管理界面** | `QtTaskService` (+ Controller + Vue 前端) | `biz/qt/` |
| **Cron 工具** | `cron-parser` + `cronstrue` (前端), `ComCronFixer.vue` (前端 Cron 快捷修改器) | `package.json`, `soa/com-series/ComCronFixer.vue` |
| **Spring 定时** | `@EnableScheduling` + `@Scheduled`（`AppStatusService.sample()`） | `BioRunner.java`, `AppStatusService.java` |

### C.7 缓存

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **缓存抽象** | `spring-boot-starter-cache` + `@EnableCaching` | `BioRunner.java`, `pom.xml` |
| **本地缓存引擎** | Caffeine（内存内高性能缓存） | `CacheConfig.java` |
| **缓存策略一览** | `userSession` — 用户会话（5min/1000条） | `CacheConfig.java` |
|  | `endpoint` — 端点权限（30min/2000条） |   |
|  | `userProfile` — 用户信息（5min/1000条） |   |
|  | `menuTree` — 菜单树（30min/100条） |   |
|  | `rateLimit_30` — 限流计数器（31秒/1000条） |   |
|  | `registry` — 注册表（15min/10000条） |   |
| **分布式缓存** | **未引入**（纯本地 Caffeine） | — |

### C.8 限流

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **限流注解** | `@RateLimit`（方法级声明式限流） | `RateLimit.java` |
| **限流切面** | `RateLimitAspect`（AOP Around 拦截） | `RateLimitAspect.java` |
| **限流计数器** | `CaffeineRateLimitCounter`（基于 Caffeine 的滑动窗口限流） | `CaffeineRateLimitCounter.java` |
| **限流维度** | `GLOBAL` / `IP_ADDRESS` / `USER` 三种范围 | `RateLimitScope.java` |
| **容灾模式** | `failOpen: true`（存储异常时放行） | `RateLimit.java` |

### C.9 工作流引擎

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **流程引擎** | Flowable 8.0.0 (Spring Boot Starter) | `pom.xml`, `application.yml` |
| **数据库** | Flowable 自带表（DDL 自动更新） | `application.yml` (`flowable.database-schema-update: true`) |
| **流程建模** | 前端 `bpmn-js` 集成 + 自定义 BPMN 扩展 | `views/qf/sfc_private/flowable-designer/` |
| **BPMN 扩展** | 自研 QFE 扩展 (`QfeBpmnModel`, `QfeUserTask`, `QfeDescriptor.json`) | `biz/qf/commons/qfe/` |
| **引擎事件** | 自定义 Listener (`QfTaskCreatedListener`, `QfTaskAssignedListener`, `QfTaskFinishedListener`, `QfTaskCancelledListener`) | `biz/qf/commons/listener/` |
| **业务表单** | 动态表单配置 (`QfBizForm`, `QfBizFormField`) | `biz/qf/model/qfbizform*/` |

### C.10 监控与日志

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **系统监控** | Spring Boot Actuator（JMX 已禁用） | `pom.xml`, `BioRunner.java` |
| **硬件监控** | OSHI 7.0.1（定时采样 CPU/内存/磁盘/网络/IO 实时指标） | `AppStatusService.java` |
| **请求日志** | `@PrintLog` 注解 + `RequestLogAspect`（AOP 环绕记录请求/响应/耗时） | `RequestLogAspect.java`, `PrintLog.java` |
| **增强请求日志** | `RequestLogFilter`（Filter 层，记录无法被 AOP 捕获的异常请求） | `RequestLogFilter.java` |
| **日志脱敏** | `PrintLog.sensitiveFields()` 字段级脱敏 | `RequestLogAspect.filterSensitiveFields()` |
| **全局异常处理** | `GlobalExceptionHandler`（统一的 `Result<T>` JSON 响应） | `GlobalExceptionHandler.java` |
| **访问日志配置** | `AccessLogConfiguration`（开关、载荷截断长度、应用名） | `AccessLogConfiguration.java` |
| **审计记录** | `AuditLoginService`（登录行为）+ `AuditErrorRcdService`（系统错误异步记录） | `biz/audit/` |
| **SLF4J/Logback** | Spring Boot 默认日志 | 所有 `@Slf4j` 类 |
| **虚拟线程** | `spring.threads.virtual.enabled: true` | `application.yml` |

### C.11 代码生成

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **模板引擎** | Apache Velocity 2.4.1 | `pom.xml` |
| **蓝图读取** | `QbeBlueprintReader`（扫描文件系统蓝图仓库） | `QbeBlueprintReader.java` |
| **Java 端生成** | `assembly-blueprint` (test scope) + `assembly-text-processor` | `pom.xml` |
| **模板仓库** | `support-static/code-generator/` | 30 个 `.vm` 模板文件 |
| **生成路径** | `blueprint_jpa`（JPA 架构）+ `blueprint_vue`（Vue 架构）+ `qbe_java` / `qbe_vue`（快速构建引擎） | `support-static/code-generator/` |
| **生成产物** | Java (Po/Repository/Service/Controller/Dto/Vo) + Vue (SFC/Api/Service/Route) + MyBatis Mapper/XML | 各 `.vm` 模板 |

### C.12 数据导入导出

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **Excel 处理** | Alibaba EasyExcel 4.0.3 | `pom.xml` |
| **通用导入向导** | `ImportWizardModal.vue` (前端) + `ImportWizard.java` (后端) | `soa/com-series/`, `commons/dataprocess/ImportWizard.java` |
| **注册表字典转换器** | `RegistryDictConverter`, `RegistryDict` | `commons/dataprocess/converter/` |
| **行转换器** | `IntegerRowConverter`, `StringRowConverter`, `AttachImagesConverter` | `commons/dataprocess/converter/` |
| **数据校验** | `@In` 注解校验器、`@ConditionalNotBlank` 条件非空校验器 | `commons/dataprocess/validator/` |

### C.13 通用工具

| 能力 | 技术选型 | 位置 |
|------|----------|------|
| **字符串处理** | Apache Commons Lang3 3.20.0 | `pom.xml` |
| **文件上传** | Apache Commons FileUpload2 (Jakarta Servlet6) | `pom.xml` |
| **文件类型检测** | Apache Tika Core 3.3.0 | `pom.xml` |
| **拼音处理** | pinyin4j 2.5.1 | `pom.xml` |
| **用户代理解析** | UserAgentUtils 1.21 | `pom.xml` |
| **Git 操作** | JGit 7.5.0 + JSch 2.28.0 | `pom.xml` |
| **实体对象映射** | `Entities.as` / `Entities.assign` (ModelMapper 后端) | `EntityMapperConfig.java` |
| **JSON 操作** | Gson 2.14.0 (全局) | `JsonEntityMapperConfig.java`, `GsonUtils.java` |
| **通用树构建** | `TreeBuilder<T>` 泛型工具 | `TreeBuilder.java` |
| **文件哈希** | `SHA256.java`, `FileSha256.java` | `commons/utils/` |
| **虚拟线程** | JDK 21 Virtual Threads | `application.yml` |

---

## D) 架构特征总结

### D.1 分层架构（严格）

```
┌─────────────────────────────────────────────┐
│  前端层 (Vue 3 SFC + Element Plus)           │
│  - 页面组件 (.vue)                            │
│  - Api 服务层 (Axios 请求封装)                 │
│  - Service 服务层 (业务状态与流程)              │
│  - SOA 通用组件 (布局/列表/查询/表格/表单)       │
├─────────────────────────────────────────────┤
│  控制器层 (Controller)                        │
│  - @RestController + 请求映射                 │
│  - 参数校验、响应包装、权限标注                 │
├─────────────────────────────────────────────┤
│  业务逻辑层 (Service)                         │
│  - @Service 具体类（非接口）                   │
│  - JPA Repository 默认数据访问                │
│  - MyBatis Mapper 复杂 SQL                   │
│  - @Transactional 事务管理                   │
│  - Dto↔Po↔Vo 对象映射 (Entities.as/assign)    │
│  - BizException 业务异常                     │
├─────────────────────────────────────────────┤
│  数据访问层                                   │
│  - JpaRepository + JPQL                     │
│  - MyBatis Mapper + XML SQL                 │
│  - TupleMapper（Tuple→VO 投影映射）            │
├─────────────────────────────────────────────┤
│  数据库层 (MySQL / H2)                        │
│  - Flyway 数据库版本迁移                       │
│  - Flowable 引擎表                            │
│  - Quartz 调度表                              │
└─────────────────────────────────────────────┘
```

### D.2 架构模式特征

| 特征 | 描述 |
|------|------|
| **整体架构** | 单体应用（Spring Boot Single JAR + 内嵌前端 SPA） |
| **分层模型** | 经典 Controller → Service → Repository 三层，前端有 SFC → Api → Service 三层 |
| **模块分割** | 以业务域（biz 包）为一级模块，每个模块内部按 model/controller/service/repository 分包 |
| **多租户架构** | `CoreRoot` 为核心租户隔离机制（Root = 租户），RowScope 实现行级数据隔离 |
| **事件驱动（轻量）** | 基于 Spring ApplicationEvent 的内部事件总线（用户/租户生命周期、流程引擎任务事件） |
| **无状态设计** | 关闭 HTTP Session，使用 Token（Bearer Auth）重建安全上下文 |
| **前后端一体** | Maven Profile `with-web-ui` 将前端 Vite 构建产物打包进 Spring Boot Jar 的 `/web-static/` 目录 |
| **延迟加载** | 全局开启 `lazy-initialization`，减少启动时 Bean 加载开销 |
| **虚拟线程** | 启用 JDK 21 Virtual Threads，提高 IO 密集型请求吞吐量 |
| **代码生成器** | Velocity 模板引擎 + 蓝图文件系统，可生成 Java + Vue 完整 CRUD 模块 |
| **数据库迁移保守策略** | Flyway 仅检查并提示，不自动执行迁移（需人工操作） |
| **JPA 优先，MyBatis 补充** | 单表 CRUD 走 JPA，复杂多表查询/统计/方言走 MyBatis |
| **本地缓存为主** | Caffeine 本地缓存 6 个命名空间，无分布式缓存依赖 |

### D.3 关键技术决策

| 决策 | 理由 |
|------|------|
| **Spring Boot 4.0.6 + Java 21** | 项目定位为新一代底座，采用最新的 LTS JDK 21 和 Spring Boot 最新主线版本，拥抱虚拟线程、记录模式等新特性 |
| **JPA + MyBatis 双轨制** | JPA 负责标准 CRUD（开发效率高），MyBatis 负责复杂查询（灵活性高），避免单轨的局限性 |
| **Hibernate DDL 禁用 + Flyway 手动迁移** | 数据库变更采用显式 SQL 脚本，确保可审计、可回滚，避免 Hibernate DDL 在生产环境的不可控风险 |
| **Flowable 8.0.0 工作流引擎** | 作为标准的 BPMN 2.0 引擎，提供可视化流程设计（bpmn-js 前端集成），替代自研流程系统 |
| **Caffeine 本地缓存 + @EnableCaching** | 轻量级方案，无外部中间件依赖，适用于单体部署场景；6 个命名空间覆盖会话、权限、菜单、注册表等高频读取数据 |
| **自研注册表系统 (AppRegistry + RegistryService)** | 替代 Spring Cloud Config/Apollo 等重型配置中心，通过枚举 + 数据库 KV 表实现动态配置，管理界面可操作 |
| **自研行级权限框架 (RowScope)** | 在 JPA Entity 和 MyBatis 层面实现行级权限过滤，比 Spring Security ACL 更轻量且与项目数据模型深度绑定 |
| **Quartz JDBC JobStore** | 任务定义和调度状态持久化到数据库，保障任务的高可用和可追溯性 |
| **前后端一体化部署** | 通过 `with-web-ui` Maven Profile 将前端构建为静态文件并打入 Spring Boot Jar，简化部署运维（单个 Jar = 完整应用） |
| **集成部署路径前缀 `/api`** | 通过 `IntegratedDeployConfig`（`@ConditionalOnResource`）自动为 `@RestController` 添加 `/api` 前缀，实现前后端同域部署时的路由隔离 |
| **JGit + JSch 集成** | 用于代码生成器的 Git 仓库操作（蓝图仓库多版本管理） |
| **Tianai Captcha（行为验证码）** | 开源的行为验证码方案，支持滑块/旋转/点选，接入简单无需外部服务；100 张预置背景图 |
| **废弃传统特性** | 关闭 CSRF、HTTP Session、Form Login、HttpBasic、RememberMe、SavedRequest、默认 Headers —— 全面拥抱无状态 Token 鉴权 |

---

## E) 项目依赖全景图（关键依赖）

### 后端核心依赖

```
spring-boot-starter-parent:4.0.6  # Spring Boot 主线
├── spring-boot-starter-web  # Web MVC
├── spring-boot-starter-data-jpa  # JPA/Hibernate
├── spring-boot-starter-security  # 安全框架
├── spring-boot-starter-validation  # 数据校验
├── spring-boot-starter-cache  # 缓存抽象
├── spring-boot-starter-actuator  # 监控端点
├── spring-boot-starter-flyway  # 数据库迁移
├── spring-boot-quartz:4.1.0-RC1  # 任务调度
├── spring-boot-configuration-processor  # 配置元数据
├── spring-data-commons:4.0.2  # 分页抽象
├── mybatis-spring-boot-starter:4.0.1  # MyBatis 集成
├── flowable-spring-boot-starter:8.0.0  # 工作流引擎
├── mysql-connector-j  # MySQL 驱动
├── h2:2.4.240  # H2 嵌入式/内存数据库
├── flyway-mysql  # Flyway MySQL 方言
├── caffeine  # 高性能本地缓存
├── lombok  # 代码简化
├── commons-lang3:3.20.0  # 语言工具
├── commons-fileupload2:2.0.0-M5  # 文件上传(Servlet6)
├── gson:2.14.0  # JSON 序列化
├── snowflake:1.0.0  # 雪花ID
├── easyexcel:4.0.3  # Excel 处理
├── tika-core:3.3.0  # 文件类型检测
├── pinyin4j:2.5.1  # 拼音转换
├── UserAgentUtils:1.21  # UA 解析
├── velocity-engine-core:2.4.1  # 代码生成模板引擎
├── oshi-core:7.0.1  # 硬件监控
├── tianai-captcha:1.5.5  # 行为验证码
├── springdoc-openapi:3.0.1  # OpenAPI 文档
├── jgit:7.5.0  # Git 操作
├── jsch:2.28.0  # SSH 连接
├── modelmapper (via assembly-entity)  # 对象映射
└── assembly-* (内部 library, 2.2S-RC1)  # 内部组件库 (entity/mapper/blueprint/text-processor)
```

### 前端核心依赖

```
vue:3.5.33, vite:8.0.10, typescript:6.0.3  # 核心框架
element-plus:2.13.7  # UI 组件库
pinia:3.0.4 + pinia-plugin-persistedstate  # 状态管理
vue-router:5.0.6  # 路由
axios:1.15.2  # HTTP 客户端
@vueuse/core:14.3.0  # 组合式工具函数
echarts:6.0.0 + vue-echarts:8.0.1  # 图表
bpmn-js:18.10.0  # BPMN 流程图
monaco-editor-vue3:0.1.10  # 代码编辑器
cron-parser:5.5.0 + cronstrue:3.12.0  # Cron 解析
hash-wasm:4.12.0  # 哈希计算（SHA256）
tailwindcss:4.2.4  # 原子化 CSS
unplugin-icons + unplugin-vue-components  # 按需图标/组件
splitpanes:4.0.4  # 面板分割
vuedraggable:4.1.0  # 拖拽排序
@vue-office/*  # Office 文档预览
```

---

## F) 总结

`bio-code` 是一个 **Java 21 + Spring Boot 4.0.6 + Vue 3** 的前后端一体化单体应用底座，核心定位为**内部业务系统的快速开发平台**。其核心能力矩阵覆盖了企业级应用的通用需求：

1. **稳固的 Web 框架层**：Spring Boot + Spring MVC + Spring Security，无状态 Token 鉴权，全面拥抱现代安全实践
2. **灵活的持久化层**：JPA + MyBatis 双轨制，雪花 ID 生成，Flyway 手动迁移，Tuple 投影映射
3. **完善的权限体系**：端点权限 + 行级权限(RS) + 系统级权限 + 动态白名单 + 方法级权限
4. **动态配置中心**：自研注册表系统，枚举 + 数据库 KV 存储 + Caffeine 缓存 + 管理界面
5. **工作流引擎**：Flowable 8.0.0 + bpmn-js 可视化设计 + 自定义 BPMN 扩展 + 事件驱动
6. **任务调度**：Quartz JDBC JobStore + QuickTask 注解即时注册 + Cron 前端工具
7. **代码生成器**：Velocity 模板引擎 + 蓝图系统，一键生成 Java + Vue 全栈 CRUD 代码
8. **监控与运维**：Actuator + OSHI 硬件监控 + @PrintLog 请求日志脱敏 + 审计记录 + 虚拟线程
9. **前后端一体化**：单个 Jar 部署，前端 SOA 标准组件库覆盖列表页、查询区、表格区、操作区等通用场景

该项目底座**无外部中间件强依赖**（无 Redis、无 MQ、无注册中心、无配置中心），设计哲学强调**自包含、轻量级、低运维门槛**，非常适合作为中小型团队或企业内部系统的快速起步平台。
