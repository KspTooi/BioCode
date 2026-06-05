# bio-code 项目设计哲学与工程规范深度分析报告

> 本报告基于对 `.cursor/rules/` 全部 13 个规范文件、`CLAUDE.md`、前端架构规范文档、通用基础设施源码、前端核心模块源码的完整阅读，覆盖项目的架构设计思想、编码规范体系与工程化方法论。

---

## 一、前后端对接设计

### 1.1 通信协议与约定

**HTTP 方法：统一 POST**

项目采用**全 POST** 策略——除附件预览等极少数特例外，所有 Controller 方法统一 `@PostMapping`。这不是对 RESTful 的否定，而是一种**务实的工程取舍**：

- 所有请求参数统一走 `@RequestBody`（JSON 体），避免路径参数和查询字符串的复杂编解码问题
- 复杂查询条件（如多字段模糊搜索、嵌套过滤）天然不适合塞入 GET URL
- 统一 POST 简化了前端 `Http.postEntity` 的单一路径调用模型
- 安全层面避免了敏感参数出现在 URL 访问日志中

```1:3:d:\ComRd\Private\bio-code\.cursor\rules\web-service\module\WebServiceControllerMR.mdc
Controller 是 HTTP 请求的**唯一入口**，只做接入与响应包装，不含业务逻辑。

1. 接收 `@RequestBody` 入参，配合 `@Valid` 触发 Dto 校验
```

URL 命名规则为 `/<camelCase实体>/<methodName>`，如 `/notice/getNoticeList`。前端 Api 层的 URL 格式与后端完全对齐，方法名也一一对应：`getXxxList` / `addXxx` / `editXxx` / `getXxxDetails` / `removeXxx`。

Content-Type 默认为 `application/json`（`@RequestBody`）。文件上传走 `multipart/form-data`。所有请求携带自定义头 `AE-Request-With: XHR` 用于标识异步请求。

### 1.2 请求/响应模型

**Result\<T\> 三层结构**

```1:23:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\commons\model\Result.ts
export default interface Result<T> {
 code: number; // 0:成功 1:业务异常 2:内部错误
 message: string; // 描述信息
 data: T; // 返回数据
}
```

这是一个**经典的工业级响应包装**，将业务成功/失败与 HTTP 传输状态解耦：
- `code === 0` → 业务成功，`data` 含有效载荷
- `code === 1` → 业务异常（可预期的错误，如"数据不存在"、"名称重复"）
- `code === 2` → 系统内部错误（不可预期的异常）
- `code === 401` → 未认证 / 会话过期
- `code === 403` → 权限不足

**PageResult\<T\> 分页格式**

```1:27:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\commons\model\PageResult.ts
export default interface PageResult<T> {
 code: number;
 message: string;
 data: T[];
 total: number;
}
```

分页数据平铺在 `data` 数组中，总记录数在 `total` 字段。空结果使用 `PageResult.successWithEmpty()` 统一处理。

**PageQuery 分页请求基类**

```1:14:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\commons\model\PageQuery.ts
export default interface PageQuery {
 pageNum: number;
 pageSize: number;
}
```

所有列表查询 Dto 必须 `extends PageQuery`，禁止手写分页字段。保证了前后端分页参数命名的一致性：`pageNum`（从 1 开始）和 `pageSize`。

### 1.3 错误码体系

```1:43:d:\ComRd\Private\bio-code\src\main\java\com\ksptool\bio\commons\web\ResultCode.java
public enum ResultCode {
 SUCCESS(0, HttpStatus.OK, "操作成功"),
 BIZ_ERROR(1, HttpStatus.OK, "业务异常"),
 INTERNAL_ERROR(2, HttpStatus.INTERNAL_SERVER_ERROR, "系统内部错误"),
 PARAM_ERROR(3, HttpStatus.BAD_REQUEST, "参数异常"),
 UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "用户会话异常"),
 FORBIDDEN(403, HttpStatus.FORBIDDEN, "权限不足"),
 REQUIRE_ROOT(101, HttpStatus.OK, "用户未绑定租户"),
 INSTALL_WIZARD_ACTIVE(102, HttpStatus.OK, "安装向导模式已激活"),
}
```

核心设计要点：
- **code 不是 HTTP 状态码**，而是独立的业务状态码。`code=0` 成功，`code=1` 业务异常，两者在 HTTP 层面都返回 200
- HTTP 401/403 由 Spring Security 过滤器链在未到达 Controller 时直接拦截，配合前端 Axios 拦截器自动跳转登录页或无权限页
- code 101/102 是项目特有的中间状态码（未绑定租户 / 安装向导激活），体现了"单体系统自举"的设计理念

### 1.4 集成部署模式

`IntegratedDeployConfig` 是项目单体部署的**核心机制**：

- **条件触发**：`@ConditionalOnResource(resources = "classpath:web-static/index.html")`——仅当 Maven `with-web-ui` Profile 将前端构建产物放入 `web-static` 目录时才激活
- **`/api` 前缀自动追加**：通过 `configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class))` 为所有 `@RestController` 自动加 `/api` 前缀
- **SPA 路由重写**：`addViewControllers` 将 `/` 和 `/login` 转发到 `forward:/index.html`，配合 Vue Router 的 Hash 模式实现前端路由接管
- **静态资源映射**：`/js/**`、`/css/**`、`/assets/**` 映射到 `classpath:/web-static/` 目录

这意味着**同一份后端代码，集成部署时自动套 `/api` 前缀，前后端分离开发时不套**——实现了零配置的部署模式切换。

### 1.5 开发模式（前后端分离）

Vite 代理配置实现了开发时的前后端分离：

```59:77:d:\ComRd\Private\bio-code\src\main\resources\web-ui\vite.config.ts
 server: {
 host: "0.0.0.0",
 port: 27501,
 proxy: {
 "/api": {
 target: "http://127.0.0.1:27500",
 changeOrigin: true,
 rewrite: (path) => path.replace(/^\/api/, ""),
 ws: true,
 secure: false,
 },
 },
 },
```

- 前端开发服务器运行在 **端口 27501**
- 后端运行在 **端口 27500**
- 所有 `/api/*` 请求被代理到后端，并**移除 `/api` 前缀**——因为开发模式下后方 Controller 不带 `/api` 前缀
- `baseURL` 硬编码为 `/api`，开发/生产环境无缝切换

### 1.6 认证对接

**Bearer Token 机制**：

- 前端登录后，`sessionId` 存入 Pinia AuthStore + localStorage
- Axios 请求拦截器自动注入 `Authorization: Bearer ${sessionId}`
- 后端 USAF（UserSessionAuthFilter）在 Spring Security 过滤器链中解析 Token，重建认证上下文

```17:24:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\commons\Http.ts
axiosInstance.interceptors.request.use((config) => {
 const sessionId = UserAuthService.AuthStore().getSessionId;
 if (sessionId) {
 config.headers.Authorization = `Bearer ${sessionId}`;
 }
 return config;
});
```

**Axios 响应拦截器的三层防护**：

1. HTTP 403 → 跳转无权限页面
2. HTTP 401 → 跳转登录页 + 清除 AuthStore
3. HTTP 400 + `code===1` → 抛出 `Error`，由调用方（Service 层）catch 展示 `ElMessage.error`

### 1.7 权限码体系

**后端 `@PreAuthorize`**：

```java
@PreAuthorize("@auth.hasCode('xxx:xxx:view')")
```

每个 Controller 方法**强制**声明权限码，格式为 `<域>:<实体>:<动作>`。Spring Security 的 `@EnableMethodSecurity` + 自定义 `@auth.hasCode()` SpEL 表达式，使得权限检查发生在方法调用层面，而非粗糙的 URL 匹配。

**前端 `v-hasCode` 指令**：

```92:117:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\views\auth\service\UserAuthService.ts
 const hasCode = (codes: string | string[]): boolean => {
 if (AuthStore().userInfo?.authorities?.includes("*:*:*")) {
 return true;
 }
 const authorities = AuthStore().userInfo?.authorities ?? [];
 const codeList = Array.isArray(codes) ? codes : [codes];
 return codeList.some((code) => authorities.includes(code));
 };

 const vHasCode: Directive = {
 mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
 if (!hasCode(binding.value)) {
 el.style.display = "none";
 }
 },
```

**双层防护**：后端 `@PreAuthorize` 做**强制门禁**（不可绕过），前端 `v-hasCode` 做**UI 体验优化**（隐藏按钮）。即使前端被篡改，后端仍然会拦截。

### 1.8 Dto/Vo 类型同步

项目采用**手动对齐 + 命名约定**的方式保持前后端类型同步：

- 前后端的 Dto/Vo **命名完全一致**：`GetXxxListDto`、`GetXxxListVo`、`AddXxxDto` 等
- 前后端的字段结构**必须一一对应**（类型映射：`Long`→`string`、`Integer`→`number`、`LocalDateTime`→`string`、`Boolean`→`number(0/1)`）
- 前端禁止使用 `Partial`/`Pick`/`Omit` 缝合类型，必须独立定义
- **没有自动生成机制**——这是有意为之。各域各自独立演进，自动生成反而会造成跨域耦合

### 1.9 文件上传对接

"attach 模块"采用**三段式**上传流程（directUpload → preCheckAttach → applyChunk），不走 WebSocket/Stream 等重协议，而是通过 HTTP 分段+校验确保可靠性。

### 1.10 分页约定

- 前端：`pageNum`（从 1 开始）、`pageSize`
- 后端：`PageQuery` 基类定义相同字段，Controller `@RequestBody` 接收，Service 调用 `dto.pageRequest()` 转为 Spring Data `Pageable`
- 分页格式统一为 `data: T[]` + `total: number`，不分页时 `total` 为 0

---

## 二、后端工程化

### 2.1 包结构约定

```
com.ksptool.bio
├── BioRunner.java # 启动入口
├── commons/ # 全局通用基础设施（不依赖任何 biz 包）
│ ├── web/ # ResultCode
│ ├── config/ # IntegratedDeployConfig, EntityMapperConfig, WebMvcConfig
│ ├── aop/ # GlobalExceptionHandler, RequestLogAspect
│ ├── utils/ # FileSlice, SHA256, GsonUtils, IdWorker
│ └── model/ # HttpRelay 系列中继模型
└── biz/ # 业务模块（按域分包）
 ├── core/ # 核心域
 │ ├── common/ # core 域共享工具（Switch, AppRegistry, TupleMapper, TreeBuilder...）
 │ └── model/<实体>/ # 按实体分子包: po/ dto/ vo/
 ├── auth/ # 认证授权域
 ├── audit/ # 审计域
 ├── qf/ # 轻工作流域
 ├── qt/ # 轻任务调度域
 └── ... # 其他业务域
```

**核心约定**：
- `commons/` 只放不依赖任何 biz 包的全局通用代码
- `biz/<域>/common/` 放该域内部共享的工具（如 `core/common/` 下的 `Switch`、`TupleMapper`、`AppRegistry`）
- `model/<实体>/` 下按 `po/`、`dto/`、`vo/` 严格分子包，禁止混放

### 2.2 Controller 规范

Controller 是纯粹的 HTTP 适配层，职责极简：

| 规则 | 说明 |
|------|------|
| 类名 `<Entity>Controller` | 一个 Controller 对应一个业务实体 |
| `@RequestMapping("/<camelCase实体>")` | 如 `/notice` |
| 统一 `@PostMapping` | 禁止 GET/DELETE（附件预览等特例除外） |
| `@PreAuthorize` 必加 | 每个接口必须有权限码 |
| `@Operation` 必加 | Swagger 文档描述 |
| 入参 `@RequestBody @Valid` | 配合 Dto 层校验 |
| 禁止注入 Repository / 操作 Po | 不越界 |

**方法签名速查**：

| 方法 | 路径 | 入参 | 返回 | Controller 动作 |
|------|------|------|------|-----------------|
| `getXxxList` | `/xxx/getXxxList` | `GetXxxListDto` | `PageResult<XxxListVo>` | 直接 return Service |
| `addXxx` | `/xxx/addXxx` | `AddXxxDto` | `Result<String>` | 调 Service → success 文案 |
| `editXxx` | `/xxx/editXxx` | `EditXxxDto` | `Result<String>` | 同上 |
| `getXxxDetails` | `/xxx/getXxxDetails` | `CommonIdDto` | `Result<XxxDetailsVo>` | null → error |
| `removeXxx` | `/xxx/removeXxx` | `CommonIdDto` | `Result<String>` | 调 Service → success 文案 |

### 2.3 Service 规范

Service 是**唯一的业务逻辑层**，核心特点：

- **默认无接口**：直接 `@Service` 具体类，仅多实现换绑时才拆 `interface + impl`
- **事务管理**：写操作必须 `@Transactional(rollbackFor = Exception.class)`；查询不加事务
- **异常处理**：可预期错误 `throw new BizException("中文提示")`，由 `GlobalExceptionHandler` 统一捕获包装为 `Result`
- **对象映射**：统一使用 `Entities.as()` / `Entities.assign()`，通过 ModelMapper + `EntityMapperConfig` 实现 Dto↔Po、Po↔Vo 的自动映射
- **数据访问分层**：默认 JPA Repository，JPA/JPQL 搞不定的场景走 MyBatis Mapper；禁止 Service 内裸 SQL
- **空列表安全**：`PageResult.successWithEmpty()` 统一处理空结果，禁止返回 null

```60:72:d:\ComRd\Private\bio-code\.cursor\rules\web-service\module\WebServiceServiceMR.mdc
public PageResult<GetXxxListVo> getXxxList(GetXxxListDto dto) {
 XxxPo query = new XxxPo();
 assign(dto, query);
 Page<XxxPo> page = repository.getXxxList(query, dto.pageRequest());
 if (page.isEmpty()) {
 return PageResult.successWithEmpty();
 }
 List<GetXxxListVo> vos = as(page.getContent(), GetXxxListVo.class);
 return PageResult.success(vos, (int) page.getTotalElements());
}
```

### 2.4 Repository 规范

Repository 是数据库访问的**唯一出口**，核心约定：

- 继承 `JpaRepository<Po, Long>`，主键类型固定 `Long`
- 命名双轨制：**派生查询** → `find` 前缀 + 无 `@Query`（Spring Data 自动生成）；**手写 JPQL** → `get` 前缀 + `@Query` 文本块
- 动态条件使用 SpEL：`(:#{#po.field} IS NULL OR u.field = :#{#po.field})`
- 关联投影返回 `Page<Tuple>` 或 `List<Tuple>`，由 Service 用 `TupleMapper.tupleAs()` 映射为 Vo
- `@Modifying` 仅用于 UPDATE/DELETE，事务由 Service 承担
- 禁止在 Repository 做业务判断、组装 Vo

```59:81:d:\ComRd\Private\bio-code\.cursor\rules\web-service\module\WebServiceRepositoryMR.mdc
@Repository
public interface XxxRepository extends JpaRepository<XxxPo, Long> {

 @Query("""
 SELECT u FROM XxxPo u
 WHERE
 (:#{#po.title} IS NULL OR u.title LIKE CONCAT('%', :#{#po.title}, '%'))
 AND (:#{#po.status} IS NULL OR u.status = :#{#po.status})
 ORDER BY u.createTime DESC
 """)
 Page<XxxPo> getXxxList(@Param("po") XxxPo po, Pageable pageable);
}
```

### 2.5 Dto 规范

| 类型 | 命名 | 继承 | 含 id | 校验 |
|------|------|------|-------|------|
| 列表查询 | `GetXxxListDto` | `extends PageQuery` | 否 | 按需 |
| 新增 | `AddXxxDto` | — | 否 | 必填 `@NotNull` |
| 编辑 | `EditXxxDto` | — | 是 `@NotNull` | 同新增 |
| 删除/详情 | — | 用 `CommonIdDto` | — | — |
| 导入 | `ImportXxxDto` | `extends AbstractImportDto` | — | `validate()` |

- 列表 Dto 必须继承 `PageQuery`（禁止手写分页字段）
- 删除/详情统一用 `CommonIdDto`（含 `id` 和 `ids` 字段，支持单删/批量删）
- 统一 `@Getter @Setter`，禁止 `@Data`

### 2.6 Vo 规范

| 类型 | 命名 | 用途 |
|------|------|------|
| 列表行 | `GetXxxListVo` | 表格列（最少字段） |
| 详情 | `GetXxxDetailsVo` | 表单回显（全量字段） |
| 导出 | `ExportXxxVo` | EasyExcel 导出列 |
| 片段 | `<名词>Vo` | 嵌套列表项 |

- 列表 Vo 只放表格展示字段；详情 Vo 放表单回显所需全量字段
- 关联展示用 `xxxName` 冗余字段，不嵌套 Po
- 禁止在 Vo 上加 `@NotNull` 等校验注解

### 2.7 Po 规范

Po 的实体设计是项目**数据层工程化**的核心体现：

- **Snowflake 雪花 ID**：通过 `@SnowflakeIdGenerated` 注解自动生成分布式唯一 ID。关键约束：使用此注解后禁止手动设置 ID 字段，否则 JPA 会误判为已存在实体执行 Merge
- **软删除**：`@SQLDelete(sql = "UPDATE core_user SET delete_time = NOW() WHERE id = ?")` + `@SQLRestriction("delete_time IS NULL")`，删除操作自动变为逻辑删除，查询自动过滤已删除数据
- **审计自动填充**：`@CreatedDate` / `@CreatedBy` / `@LastModifiedDate` / `@LastModifiedBy` 通过 `JpaAuditConfig` 从 `SessionService` 获取当前用户 ID 自动填充
- **行级数据权限**：`RowScopePo` 基类 + `@Filter` 注解提供 `systemScopeFilter`，配合 `@RowScope` 注解在 Controller 层声明行级过滤策略

```31:33:d:\ComRd\Private\bio-code\src\main\java\com\ksptool\bio\biz\core\model\user\UserPo.java
@SQLDelete(sql = "UPDATE core_user SET delete_time = NOW() WHERE id = ?")
@SQLRestriction("delete_time IS NULL")
@Filter(name = "systemScopeFilter", condition = "is_system = :isSystem")
```

### 2.8 控制流规范

这是项目**最鲜明的编码风格**，贯穿所有 Java 代码：

1. **短路优先**：`if (!ok) return/continue/throw`，禁止 `else`/`else if`/`switch`
2. **嵌套深度上限 2**：超过 2 层必须用守卫展平
3. **方法抽取四问**：仅满足（1）≥2 处调用（2）屏蔽第三方 quirk（3）降低认知复杂度（4）跨运行时边界之一才允许抽取
4. **"先面条后拆分"**：AI 首次生成走线性流程，抽取时机由"第二次出现重复"驱动，不由"长度"驱动
5. **每个方法必须有 Javadoc**（含 private 方法）：写意图 + 约束，禁止复述方法名

```66:89:d:\ComRd\Private\bio-code\.cursor\rules\web-service\module\WebServiceMethodMR.mdc
public void handle(Task task) {
 //---- 守卫：边界条件 ----
 if (task == null) return;
 var po = repo.find(task.getId());
 if (po == null) return;
 if (po.getStatus() != 0) return;

 //---- 主体：happy path ----
 var vars = service.getVariables(task.getId());
 var memberType = QfMemberKinds.of(task).getMemberType();
 po.setMemberType(memberType);
 po.setUpdateTime(LocalDateTime.now());
 repo.save(po);

 //---- 事件发布 ----
 var event = new TaskAssignedEvent(this);
 assign(po, event);
 aep.publishEvent(event);
}
```

### 2.9 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| Controller 类 | `<Entity>Controller` | `NoticeController` |
| Service 类 | `<Entity>Service` | `NoticeService` |
| DTO 类 | `<动词><Entity>Dto` | `AddNoticeDto`, `GetNoticeListDto` |
| VO 类 | `<动词><Entity>Vo` | `GetNoticeListVo`, `GetNoticeDetailsVo` |
| Controller 路径 | `/<camelCase(Entity)>/<methodName>` | `/notice/getNoticeList` |

### 2.10 常量规范

**`Switch` 替代魔法数字**：项目没有 `0`/`1` 散落各处，而是通过 `Switch.on()` / `Switch.off()` / `Switch.yes()` / `Switch.no()` 语义化表达。`SwitchConv` 作为 JPA 转换器，自动处理 PO 的 Switch 类型与数据库 Integer 的映射。

```16:37:d:\ComRd\Private\bio-code\src\main\java\com\ksptool\bio\biz\core\common\Switch.java
 public static final int ON = 1;
 public static final int OFF = 0;

 public static int on() { return ON; }
 public static int off() { return OFF; }
 public static int yes() { return ON; }
 public static int no() { return OFF; }
```

**`AppRegistry` 枚举管理配置项**：将系统配置项（缓存过期时间、验证码开关、密码策略等）集中枚举化，每项包含 nodeKeyPath / nkey / value（默认值）/ kind / label / remark。这不是 YAML 配置文件，而是数据库驱动的动态配置中心——每个枚举条目对应 RegistryPo 中的一条动态配置记录，由 RegistrySdk 在运行时读取。

### 2.11 集合处理规范

- 空列表统一返回 `PageResult.successWithEmpty()`
- `IdsDiff` 工具类提供安全的 ID 集合差异计算（新增 ID / 删除 ID）
- JPA 列表类型字段通过 `ListLongConv`/`ListStringConv` 系列 AttributeConverter 安全序列化

### 2.12 跨域与数据传输

- **Gson vs Jackson 分工**：Jackson 是 Spring MVC 的主体 JSON 框架（Controller 序列化），Gson 主要用于 `GsonUtils` 的内部序列化场景
- **`EntityMapperConfig`**：通过 `ModelMapper` + `Entities.setObjectMapper()` 注册全局类型转换器，处理 `Long→String`、`LocalDateTime↔LocalDate`、`BigDecimal↔Double` 等跨类型映射

---

## 三、前端工程化

### 3.1 目录结构规范

每个业务域的标准分包：

```
views/<域>/
├── api/ # Api 层（HTTP 请求 + 类型声明）
├── service/ # Service 层（响应式状态 + 业务编排）
├── route/ # 路由注册器（XxxRouteRegister.ts）
├── components/ # 业务组件
└── sfc_private/ # 私有 .vue / .ts（禁止跨模块引用）
```

**跨域约束**：禁止直接跨域引用其他业务模块的 `service/` 和 `components/`，跨域复用必须下沉到 `soa/` 或 `commons/`。

### 3.2 Api 层规范

Api 层是模块的**唯一数据边界**，承担两项不可拆分的职责：

1. **全部类型声明**：Dto / Vo / Enum / Options 常量，禁止 Service / SFC 自行声明
2. **全部 HTTP 请求方法**：禁止 Service / SFC 直接调用 `Http.postEntity`

**5 种标准 DTO 类型**：

| 类型 | 命名 | 继承 | 说明 |
|------|------|------|------|
| 列表 Dto | `GetXxxListDto` | `extends PageQuery` | 查询条件 |
| 列表 Vo | `GetXxxListVo` | — | 表格行 |
| 详情 Vo | `GetXxxDetailsVo` | — | 表单回显 |
| 新增 Dto | `AddXxxDto` | — | 新增表单 |
| 编辑 Dto | `EditXxxDto` | — | 编辑表单（含 id） |

所有请求统一走 `Http.postEntity`（文件下载走 `responseType: "blob"`）。Api 方法内禁止 `try/catch`（错误由 Service 层处理）。

**前端 TypeScript 类型映射**：

| 后端 Java 类型 | 前端 TS 类型 | 原因 |
|:--|:--|:--|
| `Long` / `BigInteger` | `string` | 防止 JS 精度丢失 |
| `Integer` / `int` | `number` | |
| `Boolean` / `boolean` | `number` | 项目约定 0/1 |
| `LocalDateTime` | `string` | 已格式化的字符串 |
| ID 字段（任意类型） | `string` | 统一 string，防精度丢失 |

### 3.3 Service 层规范

Service 层是模块的**业务逻辑层**，遵循 `useXxxList()` + `useXxxModal()` 固定模式：

**`useXxxList` 暴露清单**：

| 名称 | 类别 | 说明 |
|------|------|------|
| `listForm` | 状态 | 查询条件（`ref<GetXxxListDto>`） |
| `listData` | 状态 | 列表数据（`ref<GetXxxListVo[]>`） |
| `listTotal` | 状态 | 总记录数 |
| `listLoading` | 状态 | 加载状态 |
| `loadList` | 方法 | 加载/刷新列表 |
| `resetList` | 方法 | 逐字段重置查询并刷新 |
| `removeList` | 方法 | 删除单条（含 ElMessageBox 二次确认） |

**`useXxxModal` 暴露清单**：

| 名称 | 类别 | 说明 |
|------|------|------|
| `modalVisible` | 状态 | 模态框可见性 |
| `modalLoading` | 状态 | 提交/加载状态 |
| `modalMode` | 状态 | 模式（`"add" \| "edit"`） |
| `modalForm` | 状态 | 表单数据（`reactive<GetXxxDetailsVo>`） |
| `modalRules` | 状态 | 表单校验规则 |
| `openModal` | 方法 | 打开模态框 `(mode, row?)` |
| `resetModal` | 方法 | 重置表单状态 |
| `submitModal` | 方法 | 校验 + 提交 |

### 3.4 SFC 与 Service 边界

这是项目前端架构最独特的设计哲学——**SFC 与 Service 的绝对边界**：

- **SFC 只做三件事**：渲染 UI（`<template>`）+ 绑定事件（`@click="onXxx"`）+ 解构 Service
- **Service 管理一切状态和流程**：查询条件、列表数据、分页、模态框可见性、表单数据、loading、提交流程
- **派生展示留在模板**：单点 `v-if`/`v-show` 直接用模板表达式，不进 Service 的 `computed`——除非需要 ≥2 个消费点
- **禁止 SFC 内声明 `interface | enum | type`**：类型集中在 Api 文件

### 3.5 组件规范

- **Com 系列（COM-Series）**：含业务逻辑的通用组件——`ComFramework`（主框架）、`ComLeftMenu`（菜单）、`ComMultiTab`（多标签）、`ComSeqFixer`（排序修改）、`ComLayoutProvider`（布局管理器）等
- **Std 系列（STD-Series）**：不含业务逻辑的标准化组件——`StdListContainer`、`StdListAreaQuery`、`StdListAreaAction`、`StdListAreaTable`、`StdTimeRange`、`StdIconPicker` 等
- Props 定义在对应 Service 文件中，使用 `defineProps<Props>()`
- 图标必须 `markRaw()` 包裹，防止 Vue 响应式系统的性能开销

### 3.6 SOA 组件体系

SOA 不是微服务意义上的 SOA，而是**组件分层管理体系**：

| 子系统 | 定位 | 代表组件 |
|--------|------|----------|
| **COM 系列** | 通用业务组件（有状态） | ComFramework, ComLeftMenu, ComMultiTab, ComSeqFixer, ComLayoutProvider |
| **STD 系列** | 标准化无业务组件（无状态） | StdListContainer, StdListAreaQuery, StdListAreaTable, StdTimeRange |
| **GenricRoute** | 跨域路由统一注册与跳转 | GenricRouteRegister, GenricRouteService |

业务页统一通过 `StdListContainer` + `StdListAreaQuery` + `StdListAreaTable` + `StdListAreaAction` 组装，不允许业务页直接堆叠原生 Element Plus 组件重新发明轮子。

### 3.7 路由管理：GRS 体系

GRS（GenricRouteService）是整个项目路由管理的**单一事实来源**，其核心理念是：

- **禁止外部直接操作 Vue Router**：`router.addRoute` 和 `router.removeRoute` 被 `Object.defineProperty` 重写为抛出异常的占位函数
- **所有路由通过 GRS 的 `addRoute()` / `addRoutes()` 注册**——内部保存原始的 `rawAddRoute` / `rawRemoveRoute` 引用
- **注册器模式**：每个域创建 `XxxRouteRegister extends GenricRouteRegister`，实现 `doRegister()` 返回 `RouteEntryPo[]`
- **冲突检测**：初始化时自动检测 biz 重名和 path 重复，冲突时渲染 `GrConflictOverlay` 全屏提示
- **RouteEntryPo 约定**：`path` 不允许包含 `/`，最终路由路径由 GRS 根据 `biz + path` 自动拼接

```41:46:d:\ComRd\Private\bio-code\src\main\resources\web-ui\src\soa\genric-route\service\GenricRouteService.ts
const buildForbiddenFn = (methodName: string) => {
 return () => {
 throw new Error(`Vue路由管理器不支持直接调用, router.${methodName} 请通过 GenricRouteService(GRS) 进行路由管理`);
 };
};
```

### 3.8 状态管理

- **Pinia** 作为唯一状态管理方案
- **持久化**：AuthStore 内部通过 `localStorage` 手动管理（非插件持久化），`sessionId` + `userInfo` 在登录时写入，登出时清除
- **Store 内聚在 Service 模块**：不单独定义全局 Store 文件，而是放在 `views/<域>/service/` 中，与业务 Service 同目录

### 3.9 TypeScript 类型体系

- ID 一律 `string`（后端 `Long` → 前端 `string`，`String(row.id)`）
- Boolean 一律 `number`（后端 `Boolean` → 前端 `number(0/1)`）
- Dto 必须 `extends PageQuery`（列表查询 Dto）
- 禁止 `Partial`/`Pick`/`Omit` 缝合类型
- 初始化 `modalForm` 使用 `GetXxxDetailsVo` 类型（而非 `Partial<AddXxxDto & EditXxxDto>`）

### 3.10 样式管理

- **Tailwind CSS 4**：作为工具类样式方案（class 形式，禁止属性化写法）
- **Element Plus 主题**：全局覆盖集中在 `src/styles/`
- **SCSS**：处理复杂样式
- 禁止 UnoCSS（尤其是 attributify 模式）

### 3.11 构建规范

| 配置项 | 值 |
|--------|-----|
| 包管理 | pnpm |
| 构建工具 | Vite |
| 输出目录 | `dist/` |
| 输出命名 | 扁平化：`assets/[name].js`、`assets/[name].css` |
| 入口 | `index.html → AdminMain.ts` |
| TypeScript 检查 | `vite-plugin-checker`（仅 dev 模式） |

### 3.12 代码生成模板

`soa/template/` 下的 `Example.vue` + `ExampleApi.ts` + `ExampleService.ts` 作为新建业务域时的**代码生成基准**——不是简单的示例，而是所有强制性规范的**可编译可运行的模板**，确保 AI 和开发者生成代码时结构一致。

---

## 四、核心设计哲学总结

### 4.1 "单体为一"

项目选择了 **Spring Boot 单体架构** 而非微服务，这不是技术选型的"妥协"，而是深思熟虑的工程决策：

- **部署即交付**：一个 Jar 包 = 完整系统，无需编排容器、注册中心、配置中心
- **开发体验统一**：前后端源码在同一仓库，`mvn clean package -Pwith-web-ui` 一键打包
- **事务语义简单**：`@Transactional` 覆盖全流程，无需分布式事务
- **调试成本极低**：一个进程运行所有逻辑，IDE 断点直达问题现场
- **运维友好**：无需 K8s、无需服务网格，传统部署即可

### 4.2 "约定优于配置"（Convention over Configuration）

Rails 式的约定思想贯穿项目的每一层：

| 层面 | 约定 | 效果 |
|------|------|------|
| 包结构 | `biz/<域>/model/<实体>/{po,dto,vo}` | 无需配置扫描路径，按约定即可找到代码 |
| 命名 | Dto→`GetXxxListDto`, Vo→`GetXxxListVo` | AI 生成代码时能"猜"出正确命名 |
| Controller | `@RequestMapping("/xxx")` + `@PostMapping("/getXxxList")` | 前端 URL 自动对齐 |
| 分页 | `PageQuery` + `PageResult` + `pageNum`/`pageSize` | 全系统统一 |
| 路由 | `GenricRouteRegister.doRegister()` 返回 `RouteEntryPo[]` | 新增页面只需调用 `RouteEntryPo.build({...})` |
| 列表页 | `StdListContainer` + 5 个固定插槽 | 所有列表页布局统一 |

### 4.3 "自包含优先"

项目不引入 Redis、MQ、Nacos 等外部中间件：

- **Caffeine 替代 Redis**：JVM 内本地缓存和限流
- **RegistrySdk 替代 Nacos**：`AppRegistry` 枚举 + 数据库表 = 动态配置中心，可通过管理页面热更新
- **Spring Event 替代 MQ**：`BizEvent` + `ApplicationEventPublisher` 处理领域事件
- **H2 数据库**：支持零依赖启动（开发/演示环境）

### 4.4 "前后端一体化"

- **单向部署**：前端构建到 `web-static/` → Maven 打包进 Jar → 一个 Jar 跑一切
- **边界清晰，部署一体**：前后端代码严格分目录（`src/main/java/` vs `src/main/resources/web-ui/`），构建独立（Maven vs pnpm），但最终合体为一个 artifact
- **零配置部署切换**：开发时 pnpm dev + Vite proxy 前后端分离；生产时 `@ConditionalOnResource` 自动识别集成部署模式

### 4.5 "代码生成驱动"

Assembly 模块作为代码生成引擎，`Example.vue` + `ExampleApi.ts` + `ExampleService.ts` 定义了所有生成代码的**刚性骨架**。生成的代码直接符合 Controller/Service/Repository/Dto/Vo 的所有命名和结构约定。

### 4.6 "标准即框架"

**Std 系列组件将列表页标准化提升到了"不可协商的框架约束"级别**：

- 插槽强制分割（`#query`、`#actions`、`#table`、`#pagination`、`#modal`）
- 反模式精确打击："禁止在业务页面内部直接维护 10 个以上 dialog / modal 状态而不抽象"
- 单页超过 400 行触发审视，强制下沉到 Service 或拆分子组件

### 4.7 "路由管控"

GRS 禁止外部直接操作 Vue Router 的原因：
- 路由 ID 无法追溯
- 冲突无法检测
- 权限审计不可行
- 布局管理失控

### 4.8 "分层不可逾越"

Controller / Service / Repository 三层的边界是**刚性的**：

| 层 | 能做 | 绝对不能做 |
|----|------|-----------|
| Controller | 接收请求、调用 Service、包装 Result | 访问 Repository、操作 Po、写业务逻辑 |
| Service | 业务编排、Dto↔Po 映射、调用 Repository | 声明 `@RequestMapping`、返回 HTTP 响应对象 |
| Repository | 数据库查询/持久化 | 抛 BizException、组装 Vo、被 Controller 调用 |

### 4.9 "安全深度防御"

项目的权限体系是典型的**纵深防御**设计：

```
请求到达
 ↓
[端点级] DynamicGlobalWhiteManager — 全局白名单
 ↓
[方法级] @PreAuthorize("@auth.hasCode('xxx:xxx:view')") — 方法级权限
 ↓
[行级] @RowScope + Hibernate @Filter — 行级数据过滤
 ↓
[系统级] @SystemScope — 系统内置数据可见性控制
```

- 前端 `v-hasCode` 指令 + 后端 `@PreAuthorize` = **前端体验优化 + 后端强制门禁**的双层防护
- 权限码格式 `<域>:<实体>:<动作>` 自描述且可审计
- `*:*:*` 超级权限通配符贯穿前后端

### 4.10 "无状态即简单"

项目**彻底关闭了所有依赖 Session 的机制**：

- **无 Session**：`SessionCreationPolicy.STATELESS` + `NullSecurityContextRepository`
- **无 CSRF**：Bearer Token 天然免疫 CSRF
- **无 FormLogin**：前后端分离，不走表单登录
- **无 RememberMe**：Token 机制自带过期策略
- **无 Logout**：登出由前端清除 Token + 跳转登录页实现

USAF 过滤器在**每请求**重建认证上下文——从 `sessionId` → 查 `core_user_session` 表 → 重建 `SecurityContext`。这种"每请求重建"模式比 Session 更简单：没有 Session 复制、没有过期策略的并发竞争、没有内存泄漏风险。

---

## 总结

bio-code 项目的设计哲学可以浓缩为一句话：

**"单体完整性优先于分布式灵活性，约定一致性优先于配置自由度，内聚自包含优先于外部依赖集成，刚性分层优先于灵活复用。"**

这不是一种"小而美"的妥协，而是一套**高度纪律化的工程方法论**——每一层有不可逾越的边界，每一个命名有精确的约定，每一个组件有固定的组装方式。这种纪律性使得 AI 代码生成在严格的约束框架下产出高度一致、可读性和可维护性极佳的代码，也让新成员能在极短时间内理解整个系统的运作方式。
