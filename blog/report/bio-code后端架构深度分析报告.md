# bio-code 后端架构深度分析报告

---

## 1. 整体架构

### 1.1 架构风格：模块化单体

项目采用**模块化单体架构（Modular Monolith）**，整个应用打包为一个 Spring Boot JAR，内部按业务域划分为多个 `biz/` 子模块。不是微服务，但通过清晰的包边界实现了高内聚低耦合。

- 构建产物：单一 JAR (`wproc-main:1.0.0`)
- Java 版本：21
- Spring Boot 版本：4.0.6 (基于 Spring Framework 7)
- 主启动类：`com.ksptool.bio.BioRunner`

### 1.2 分层模型

标准三层架构：**Controller → Service → Repository**

```
Controller (@RestController) ← HTTP 入口，参数校验，@PreAuthorize
 ↕
Service (@Service) ← 业务逻辑唯一承载层，事务管理，BizException
 ↕
Repository (@Repository) ← JPA 接口 + MyBatis Mapper，数据库访问
```

严格遵循规范：
- Controller 只做接入与响应包装，不写业务逻辑
- Service 是业务逻辑唯一承载层，封装 Dto↔Po↔Vo 转换
- Repository 只定义查询，不包含业务判断
- Dto / Vo / Po 严格分层在不同包中

### 1.3 启动与生命周期

`BioRunner` 类加载的关键注解：

```java
@MapperScan(basePackages = "com.ksptool.bio.biz")
@EnableTransactionManagement
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
```

启动过程：
1. `@PostConstruct` 阶段：初始化中继服务器
2. `ApplicationRunner` Bean：检查注册表版本号，若当前版本 > 注册表版本则触发升级向导
3. 虚拟线程已启用：`spring.threads.virtual.enabled: true`
4. 懒加载模式：`spring.main.lazy-initialization: true`

---

## 2. 包结构设计

### 2.1 根包层级

```
com.ksptool/
└── bio/
    ├── BioRunner.java ← 主启动类
    ├── BioRunnerWithH2.java ← H2 数据库模式启动
    ├── IconResolver.java ← 图标解析
    ├── commons/ ← 通用基础设施
    │   ├── annotation/ ← @PrintLog 等注解
    │   ├── aop/ ← AOP 切面 (RequestLogAspect, GlobalExceptionHandler)
    │   ├── config/ ← 全局配置类 (CacheConfig, FlywayConfig, WebMvcConfig 等)
    │   ├── dataprocess/ ← 导入导出框架 (ImportWizard, ExportWizard, validators, converters)
    │   ├── ratelimit/ ← 限流实现 (RateLimit, RateLimitAspect, CaffeineRateLimitCounter)
    │   ├── routeselector/ ← HTTP 路由选择
    │   ├── model/ ← HTTP 中继模型
    │   ├── utils/ ← 工具类 (GsonUtils, SHA256, PinyinUtils, IdWorker 等)
    │   └── web/ ← ResultCode, WebUtils
    └── biz/ ← 业务模块
        ├── core/ ← 核心域
        │   ├── common/ ← AppRegistry, TupleMapper, TreeBuilder, BizEvent, Switch, AppVersion
        │   ├── controller/ ← CoreRootController, UserController, OrgController, ...
        │   ├── service/ ← UserService, OrgService, RegistrySdk, ...
        │   ├── repository/ ← 各 Repository
        │   └── model/ ← Dto/Vo/Po 按实体分包
        ├── auth/ ← 认证授权域
        │   ├── common/ ← SecurityConfig, PermissionCode, RowScopes, RsCalculator
        │   │   ├── aop/ ← RowScopeAspect, SystemScopeAspect, UserSessionAuthFilter
        │   │   ├── mybatis/ ← RsBuilder, RsContext
        │   │   ├── jpa/ ← RowScopesConv
        │   │   └── exception/ ← RootBindingException, AuthUnavailableException
        │   ├── controller/ ← AuthController, SessionController, GroupController
        │   ├── service/ ← AuthService, SessionService, PermissionService
        │   └── repository/ ← 各 Repository
        ├── audit/ ← 审计域
        ├── qf/ ← 流程域 (Flowable)
        ├── qt/ ← 任务调度域 (Quartz)
        └── assembly/ ← 装配域
```

---

## 3. 层次架构

### 3.1 Controller 层设计

**请求映射约定**：
- 类级 `@RequestMapping("/<camelCase实体>")`
- 方法统一 `@PostMapping`，路径等于方法名
- 集成部署模式下通过 `IntegratedDeployConfig` 自动加 `/api` 前缀

**参数校验机制**：
- Controller 入参加 `@Valid`，触发 Jakarta Bean Validation
- 支持自定义校验器：`@ConditionalNotBlank`、`@In`（值集合校验）
- 校验失败由 `GlobalExceptionHandler` 统一捕获并返回 `Result.error()`

**响应包装规范**：
- 列表：`PageResult<Vo>`，空列表用 `PageResult.successWithEmpty()`
- 详情：`Result<Vo>`
- 增改删：`Result<String>`
- 导出：`void` + `HttpServletResponse`

**权限标注方式**：
- 每个接口必须 `@PreAuthorize("@auth.hasCode('域:实体:动作')")`
- 类级可加 `@RowScope` 和 `@SystemScope` 实现数据权限过滤
- 类级加 `@PrintLog` 启用请求日志

### 3.2 Service 层设计

**接口/实现分离策略**：
- 默认**具体类 + @Service**，不创建 interface/impl 对
- 仅多实现换绑场景才拆 interface + impl

**事务管理**：
- 写操作 (add/edit/remove/状态变更) 必须 `@Transactional(rollbackFor = Exception.class)`
- 读操作不加事务

**业务异常体系**：
- 基类：`BizException`
- 子类：`RootBindingException`、`AuthUnavailableException`
- Service 通过 `throw new BizException("中文提示")` 抛出业务错误

**Dto↔Po↔Vo 转换模式**：
- `Entities.as(dto, Po.class)` — 创建新对象
- `Entities.assign(dto, po)` — 更新已有对象
- `TupleMapper.tupleAs(page.getContent(), Vo.class)` — JPQL Tuple 投影映射
- MyBatis 直出 Vo：SQL 列别名与 Vo 字段 camelCase 一致

### 3.3 Repository 层设计

**JPA Repository 基类**：
- 所有 Repository 继承 `JpaRepository<XxxPo, Long>`
- `@Query` JPQL + SpEL 动态条件
- 分页签名：`Page<XxxPo> getXxxList(@Param("po") XxxPo po, Pageable pageable)`

**Tuple 投影模式**：
- 关联查询返回 `Page<Tuple>`，Service 层用 `TupleMapper.tupleAs()` 转为 Vo

**MyBatis Mapper 使用场景**：
- 多表关联投影、复杂统计、方言 SQL
- 数据权限通过 `RsBuilder.build(alias)` 在 XML 中注入 `${rsSql}`

---

## 4. 数据架构

### 4.1 多租户数据隔离

**CoreRoot 机制**：`CoreRootPo` 代表一个租户/企业根节点，用户通过 `rootId` 绑定，`SuperEntities.ROOT`（ID=-1）为超级租户。

**RowScope 机制（RS 7 级数据权限）**：
- 业务表使用 2ID 方案：`root_id` + `org_id`
- RS 等级枚举：
  - `ALL(0)` — 全部
  - `COMPANY_AND_SUBS(10)` — 本公司+下级公司
  - `COMPANY_ONLY(20)` — 仅本公司
  - `DEPT_AND_SUBS(30)` — 本部门+下级部门
  - `DEPT_ONLY(40)` — 仅本部门
  - `SELF_ONLY(50)` — 仅本人
  - `SPECIFIED_ORG(60)` — 指定组织
  - `DENY_ALL(100)` — 拒绝所有

### 4.2 ID 生成策略

- 使用雪花算法（`snowflake` 库）
- `SnowflakeIdGenerator`：JPA 层 ID 生成器
- `QfSnowflakeIdGenerator`：Flowable 引擎专用 ID 生成器

### 4.3 数据库迁移策略

Flyway 配置：`enabled: true`，但自定义策略仅当有待迁移脚本时打印警告，不自动执行（需通过维护中心手动迁移）。

### 4.4 JPA 审计与懒加载

- `@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})` 双监听
- `@CreatedRootId` 自定义注解 + `RsAuditingEntityListener` 实现创建时自动注入租户 ID
- `spring.jpa.open-in-view: true` 启用视图层懒加载
- `spring.main.lazy-initialization: true` 全局懒加载

---

## 5. 安全架构

### 5.1 Spring Security 配置

`SecurityConfig` 核心打造：
- **CSRF 禁用**：前后端使用 Authorization 头鉴权
- **Session 无状态**：`SessionCreationPolicy.STATELESS` + `NullSecurityContextRepository`
- **禁用传统特性**：Form Login / HTTP Basic / RememberMe / Logout / Headers
- **密码编码器**：BCryptPasswordEncoder
- **方法级安全**：`@EnableMethodSecurity`

### 5.2 认证链路（USAF）

`UserSessionAuthFilter`（USAF）认证流程：
1. 从 `Authorization: Bearer <sessionId>` 头获取 sessionId
2. 若未获取到，检查 Cookie（需注册表 `FA_COOKIE_ALLOWED=1`）
3. 若仍未获取到，从请求参数 `token` 获取
4. 从数据库查询 `core_user_session` 记录（缓存 5 分钟）
5. 检查数据版本是否变更，若变更则刷新会话
6. 构建 `UsernamePasswordAuthenticationToken` 并设置到 `SecurityContextHolder`

### 5.3 授权体系

**端点级权限**：`DynamicGlobalWhiteManager` 白名单路径直接放行，非白名单必须已认证

**方法级权限**：`@PreAuthorize("@auth.hasCode('core:user:view')")`，支持通配符 `*:*:*`（超级操作权限 SA）

**行级权限**：`RowScopeAspect` 启用 Hibernate Filter `rsFilter`，注入 `rootId`/`orgIds`/`userId`

**系统级权限**：`SystemScopeAspect` 控制 `is_system` 列过滤

**超级权限**：
- `SA`（`*:*:*`）— 超级操作权限
- `SR`（`*:*:*:*`）— 超级数据权限
- `PERSP`（`*:*:*:*:PS`）— 透视权限

### 5.4 密码策略与验证码

密码策略通过注册表动态配置：
- `FA_ASP_MIN_LENGTH`：最小长度（默认 8）
- `FA_ASP_REQUIRE_SPECIAL`：是否要求特殊字符
- `FA_ASP_ALLOW_WEAK_PASSWORD`：是否允许弱密码
- `FA_ASP_MAX_ATTEMPTS_USER`：最大登录尝试次数
- `FA_ASP_LOCK_TIME_USER`：锁定时间

验证码集成 `tianai-captcha` 框架。

---

## 6. 横切关注点（AOP）

### 6.1 请求日志

- `@PrintLog` 注解：支持 `sensitiveFields` 字段级脱敏
- `RequestLogAspect`：AOP 环绕记录请求/响应/耗时
- `RequestLogFilter`：Filter 层记录无法被 AOP 捕获的异常请求（`@Order(Integer.MIN_VALUE)`）

### 6.2 限流

- `@RateLimit` 注解：支持 `GLOBAL`/`IP_ADDRESS`/`USER_ID` 三种维度
- `CaffeineRateLimitCounter`：基于 Caffeine 的滑动窗口限流
- `failOpen: true`：存储异常时放行

### 6.3 行级权限切面

`RowScopeAspect`：
1. 从 SecurityContext 获取认证信息
2. 超级数据权限直接放行
3. 根据 `mode`（FULL/ROOT_ONLY/USER_ONLY）决定过滤策略
4. 启用 Hibernate Filter `rsFilter`
5. 设置 MyBatis `RsContextHolder`
6. 后置清理

### 6.4 系统权限切面

`SystemScopeAspect`：持有超级数据权限或透视权限直接放行，否则启用 `systemScopeFilter` 过滤系统内置数据。

---

## 7. 事件架构

- 事件基类：`BizEvent<T> extends ApplicationEvent`
- 用户生命周期事件：`UserCreateEvent`、`UserRemoveEvent`
- 租户生命周期事件：`RootCreateEvent`、`RootRemoveEvent`
- 流程域事件体系：`QfProcStartedEvent`、`QfTaskCreatedEvent`、`QfTaskAssignedEvent`、`QfTaskFinishedEvent`、`QfTaskCancelledEvent`、`QfProcFinishedEvent`
- `@EnableAsync` 全局启用异步处理

---

## 8. 配置体系

### 8.1 自研注册表系统

**AppRegistry**：枚举化配置项定义，按域分组：`CM_*`（系统配置）、`CC_*`（缓存配置）、`CIW_*`（安装向导）、`FA_*`（认证域）、`FG_*`（通用域）。

**RegistrySdk**：带 Caffeine 缓存的类型安全读写 SDK：
- 读操作：`@Cacheable(value = "registry")`，缓存 15 分钟
- 写操作：`@CacheEvict(value = "registry")`，自动清除缓存
- 支持字串、整数、浮点、日期时间四种数据类型
- 支持级联创建节点树

### 8.2 application.yml 主要配置

- MySQL 驱动，支持 H2 内存测试
- JPA：`ddl-auto: none`（由 Flyway 管理）
- Flyway：仅警告，不自动迁移
- Quartz：`auto-startup: false`
- 附件模块：默认本地存储
- MyBatis：`mapper-locations` 配置
- Flowable：`database-schema-update: true`
- Swagger/OpenAPI：springdoc 配置

---

## 9. 中间件集成策略

### 9.1 数据库双轨制（JPA + MyBatis）

| 场景 | 技术 | 接入方式 |
|------|------|---------|
| 单表 CRUD、常规条件列表 | JPA Repository | `@Autowired XxxRepository` |
| 多表关联投影、复杂统计、方言 SQL | MyBatis | `@Autowired XxxMapper` + XML |
| 标准 Po 映射 | `Entities.as` / `assign` | JPA 路径 |
| 数据权限 MyBatis 侧 | `RsBuilder.build(alias)` | XML `${rsSql}` |

### 9.2 缓存架构

Caffeine 本地缓存，6 个命名空间：

| 缓存名 | 用途 | 过期时间 | 最大条目 |
|--------|------|---------|---------|
| `userSession` | 用户会话信息 | 5 分钟 | 1000 |
| `endpoint` | 端点接口动态权限配置 | 30 分钟 | 2000 |
| `userProfile` | 用户个人信息 | 5 分钟 | 1000 |
| `menuTree` | 菜单与按钮树 | 30 分钟 | 100 |
| `registry` | 注册表配置 | 15 分钟 | 10000 |
| `rateLimit_30` | 限流计数 | 31 秒 | 1000 |

### 9.3 任务调度集成

- Quartz：`auto-startup: false`，JDBC JobStore，由 `QtTaskService` 动态管理
- QuickTask 机制：`QuickTask` 泛型接口 + `QuickTaskRegistry` 注册器
- `LocalBeanExecutionJob`：将 @Service Bean 方法包装为 Quartz Job

### 9.4 工作流集成

- Flowable 8.0.0，`database-schema-update: true` 自动创建表
- QF（QuickFlow）域封装完整 BPMN 工作流引擎
- `QfFlowableConfig`：注册 5 个类型化事件监听器
- `QfeBpmnModel`、`QfeUserTask`：自定义 BPMN 扩展元素

---

## 10. 构建与部署

### 10.1 Maven POM 结构

```
groupId: com.ksptool
artifactId: wproc-main
version: 1.0.0
parent: spring-boot-starter-parent:4.0.6
```

### 10.2 前后端一体打包

Maven Profile `with-web-ui` 流程：
1. `frontend-maven-plugin` 自动安装 Node.js 20.19.0 + pnpm 10.14.0
2. 执行 `pnpm install` + `pnpm run build`
3. `maven-resources-plugin` 将 `web-ui/dist` 复制到 `classpath:/web-static/`

### 10.3 集成部署配置

`IntegratedDeployConfig`（`@ConditionalOnResource(resources = "classpath:web-static/index.html")`）：
1. 所有 `@RestController` 请求路径前添加 `/api` 前缀
2. 静态资源映射到 `classpath:/web-static/`
3. 根路径 `/` 和 `/login` 转发到 `index.html`
4. 支持 SPA History Mode

---

## 总结

该项目的后端架构是一个**高质量的企业级模块化单体应用**，具有以下突出特点：

1. **严格的分层与编码规范**：Controller/Service/Repository 三层职责清晰，强制短路优先和早返回模式
2. **成熟的多租户 + 数据权限体系**：RS 7 级数据权限通过 Hibernate Filter 和 MyBatis RsBuilder 双轨实现
3. **自研注册表系统**：数据库级动态配置管理，带 Caffeine 缓存，替代传统配置文件
4. **完善的审计与可观测性**：请求日志、限流、异常审计异步记录
5. **安全架构成熟可靠**：无状态 Token 认证、通配符权限码、动态白名单、BCrypt 加密
6. **双数据访问技术**：JPA 处理标准 CRUD，MyBatis 处理复杂查询
7. **一体化部署策略**：Maven Profile 控制前端集成，自动添加 `/api` 前缀和 SPA 资源映射
