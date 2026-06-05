# bio-code 五大核心模块深度分析报告

---

# 一、Core 模块（biz/core/）：核心系统底座

## 1. 模块概述

- **职责定位**：整个系统的基石，提供用户管理、组织机构、菜单权限、租户管理、岗位管理、注册表（配置中心）、附件服务、消息通知、Excel 模板、系统维护等通用基础设施。
- **包路径**：`com.ksptool.bio.biz.core`
- **文件数量**：175 个 Java 文件
- **核心入口类**：14 个 Controller、15 个 Service、1 个 SDK（`RegistrySdk`）

## 2. 子域与文件清单

### 2.1 用户管理（user）

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/UserController.java` — 14 个端点 |
| **Service** | `service/UserService.java` — 668 行，含 CRUD/批量/导入/版本管理 |
| **Repository** | `repository/UserRepository.java` |
| **Po** | `model/user/UserPo.java` — 映射 `core_user` 表 |
| **Dto** | `GetUserListDto`, `AddUserDto`, `EditUserDto`, `ImportUserDto`, `BatchEditUserDto` |
| **Vo** | `GetUserListVo`, `GetUserDetailsVo`, `UserPermissionVo`, `UserGroupVo` |
| **事件** | `common/event/UserCreateEvent.java`, `common/event/UserRemoveEvent.java` |

### 2.2 组织机构管理（org）

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/OrgController.java` — 6 个端点 |
| **Service** | `service/OrgService.java` |
| **Repository** | `repository/OrgRepository.java` |
| **Po** | `model/org/OrgPo.java` — 映射 `core_org` 表 |

### 2.3 菜单管理（menu）

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/MenuController.java` — 6 个端点 |
| **Service** | `service/MenuService.java` |
| **Repository** | `repository/MenuRepository.java` |
| **Po** | `model/menu/MenuPo.java` — 映射 `core_menu` 表 |

### 2.4 租户管理（root/CoreRoot）

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/CoreRootController.java` — 6 个端点 |
| **Service** | `service/CoreRootService.java` |
| **Repository** | `repository/CoreRootRepository.java` |
| **Po** | `model/root/CoreRootPo.java` — 映射 `core_root` 表 |

### 2.5 岗位管理（post）

`PostPo` 映射 `core_post` 表，字段：`name`、`code`（32字符）、`seq`、`status`、`remark`(TEXT)

### 2.6 菜单包（pack）

`PackPo` 映射 `core_pack` 表，字段：`name`、`code`（16字符）、`status`、`seq`

菜单包是菜单到租户的中间抽象层，一个租户绑定多个菜单包（`RootPackPo`），菜单包包含多组菜单。

### 2.7 注册表（registry）— 配置中心

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/RegistryController.java` — 11 个端点 |
| **Service** | `service/RegistryService.java` |
| **SDK** | `service/RegistrySdk.java` — 727 行，**核心基础设施** |
| **Repository** | `repository/RegistryRepository.java` |
| **Po** | `model/registry/RegistryPo.java` — 映射 `core_registry` 表 |

### 2.8 附件管理（attach）

| 类别 | 文件 |
|------|------|
| **Controller** | `controller/AttachController.java` — 4 个端点 |
| **Service** | `service/AttachService.java` |
| **Po** | `model/attach/AttachPo.java` — 映射 `core_attach` 表 |

传输机制：
- 小文件：`directUpload`（一次上传）+ `directPreview`（GET 预览）
- 大文件：`preCheckAttach`（预检获取上传ID）→ `applyChunk`（逐块上传，每块 ≤6MB）→ 后台自动合并校验

### 2.9 消息通知（notice / noticeRcd / noticeTemplate）

| 子域 | 核心 Po | 表名 | 关键字段 |
|------|---------|------|----------|
| 通知本体 | `NoticePo` | `core_notice` | `title`、`kind`(0:公告 1:业务提醒 2:私信)、`content`(LONGTEXT)、`priority`(0-3)、`targetKind`(0:全员 1:指定部门 2:指定用户)、`forward`、`params`(JSON) |
| 用户记录 | `NoticeRcdPo` | `core_notice_rcd` | 关联通知和用户，记录已读/未读状态 |
| 模板 | `NoticeTemplatePo` | `core_notice_template` | 通知模板管理 |

### 2.10 Excel 导入模板（excelTemplate）

`ExcelTemplatePo` 映射 `core_excel_template` 表，Controller 6 个端点，含上传（命名规则：`名称-唯一标识.xlsx`）/下载。

### 2.11 维护中心（maintain）

`MaintainController` 包含：校验内置权限节点、用户体系冷启动、从 SQL 脚本重置菜单、升级数据库、修复注册表、安装向导模式。

### 2.12 应用状态监控（appStatus）

`AppStatusController` — 2 个端点：`getRtStatus`（实时性能数据）、`getSystemInfo`（含磁盘信息）。

### 2.13 基础设施（common）

| 类别 | 文件 | 说明 |
|------|------|------|
| **ID 生成器** | `common/jpa/SnowflakeIdGenerator.java` | 基于雪花算法，通过 `@SnowflakeIdGenerated` 注解自动触发 `@PrePersist` |
| **JPA 转换器** | `ListEFAJConv`, `SetStringConv` 等 | 列表/集合类型 ↔ JSON 转换 |
| **Switch** | `common/Switch.java` | `on()=1, off()=0` — 全系统统一布尔常量 |
| **AppVersion** | `common/AppVersion.java` | 版本号类（格式：`1.6.14N45`），支持比较 |
| **TupleMapper** | `common/TupleMapper.java` | `Tuple` → Vo 映射工具（多表联查投影） |
| **事件** | `common/event/` | 用户/租户生命周期事件 |

## 3. 核心流程分析

### 3.1 用户会话体系（版本控制）

```
用户信息变更
 → UserService.increaseDv(userIds)
 → 查询在线用户 → 仅操作在线用户
 → userRepository.increaseDv(onlineUserIds) 递增 dataVersion
 → cacheManager.getCache("userSession").put("user_dv_changed_" + userId, "0")
 → 下次请求时会话拦截器检测到版本变更 → 强制刷新用户缓存
```

### 3.2 菜单权限计算

```
用户登录 → MenuService.getUserMenuTree(userId)
 → 获取用户所属用户组 → 获取组拥有的权限码
 → 遍历 MenuPo 树结构
 → MenuPo.hasPermission(permissions) 逐节点校验
 → 空权限集合 = 公开访问；*:*:* = 超级管理员
 → 过滤出可见菜单树 → 缓存到 "menuTree" 缓存
```

### 3.3 注册表读写流程

```
读取（带缓存）:
 RegistrySdk.getXxx(keyPath, defaultValue)
 → @Cacheable("registry") 先查缓存
 → repository.getRegistryEntryByKeyPath(keyPath)
 → 校验 nvalueKind 类型 → 返回类型化值

写入（清缓存）:
 RegistrySdk.setXxx(keyPath, value)
 → @CacheEvict("registry") 清除缓存
 → repository.getRegistryEntryByKeyPath → 类型校验 → save
```

## 4. API 接口清单（摘要）

| Controller | 路径 | 端点数 | 核心功能 |
|------------|------|--------|----------|
| UserController | `/user` | 7+ | 用户 CRUD/批量/导入/版本管理 |
| OrgController | `/org` | 6 | 组织机构树 CRUD |
| MenuController | `/menu` | 6 | 菜单/按钮树 CRUD、用户菜单树 |
| CoreRootController | `/coreRoot` | 6 | 租户 CRUD、菜单包绑定 |
| PostController | `/post` | 5 | 岗位 CRUD |
| PackController | `/pack` | 7 | 菜单包 CRUD、菜单绑定 |
| RegistryController | `/registry` | 10 | 注册表节点树/条目 CRUD、导入导出、清除缓存 |
| AttachController | `/attach` | 4 | 文件上传（小文件直传 + 大文件分块） |
| NoticeController | `/notice` | 5 | 消息通知 CRUD |
| NoticeRcdController | `/noticeRcd` | 5 | 用户通知记录（未读计数、全部已读） |
| NoticeTemplateController | `/noticeTemplate` | 5 | 通知模板 CRUD |
| ExcelTemplateController | `/excelTemplate` | 4 | 模板上传/下载 |
| MaintainController | `/maintain` | 7 | 权限校验、冷启动、菜单重置、数据库迁移 |
| AppStatusController | `/appStatus` | 2 | 实时性能监控 |

## 5. 数据模型

| 表名 | Po | 核心字段 |
|------|-----|----------|
| `core_user` | `UserPo extends RowScopePo` | username(uk), password, nickname, rootId, orgId, deptId, dataVersion, isSystem |
| `core_org` | `OrgPo extends RowScopePo` | rootId, topId, orgId, parentId, orgPathIds, kind(0/1/2), level, name |
| `core_menu` | `MenuPo` | rootId, parentId, kind(0-4), path, icon, permissionCode(JSON), hide, seq |
| `core_root` | `CoreRootPo` | name, expireTime, adminUserId, adminGroupId, isSystem, status |
| `core_post` | `PostPo` | name, code, seq, status, remark(TEXT) |
| `core_pack` | `PackPo` | name, code, status, seq |
| `core_registry` | `RegistryPo` | parentId, keyPath, kind(0/1), nkey, nvalueKind(0-3), nvalue(LONGTEXT), label, metadata(JSON) |
| `core_attach` | `AttachPo` | rootId, name, kind, suffix, path(2048), sha256(64), totalSize, receiveSize, status(0-3) |
| `core_notice` | `NoticePo` | title, kind(0-2), content(LONGTEXT), priority(0-2), targetKind(0-2), params(JSON) |

## 6. 设计亮点与模式

1. **EntityListener 审计体系**：`@EntityListeners({AuditingEntityListener.class, RsAuditingEntityListener.class})` 双监听
2. **`@SnowflakeIdGenerated`**：`SnowflakeIdGenerator` 实现 `IdentifierGenerator`，`@PrePersist` 自动注入雪花 ID
3. **软删除统一模式**：`@SQLDelete(sql = "UPDATE xxx SET delete_time = NOW() WHERE id = ?")` + `@SQLRestriction("delete_time IS NULL")`
4. **RegistrySdk 缓存层**：`@Cacheable("registry")` + `@CacheEvict("registry")` 实现动态配置中心
5. **数据版本控制（dataVersion）**：用户信息变更时仅递增在线用户版本号，避免全局缓存清除
6. **RowScopePo 数据权限基类**：包含 `rootId` + `creatorId`，配合 `@RowScope` AOP 注解实现数据行级隔离
7. **Switch 常量类**：`on()/off()/yes()/no()` 替代魔法数字
8. **AppVersion 版本号类**：标准化版本比较（支持 `1.6.14N45` 格式）

---

# 二、Assembly 模块（biz/assembly/）：装配与代码生成引擎

## 1. 模块概述

- **职责定位**：代码生成引擎，负责从数据库表元数据出发，通过"数据源→原始模型→聚合模型→输出方案"流程生成完整 CRUD 代码
- **包路径**：`com.ksptool.bio.biz.assembly`
- **文件数量**：77 个 Java 文件
- **核心入口类**：7 个 Controller、7 个 Service

## 2. 子域与文件清单

### 2.1 数据源管理（dataSource）

`DataSourceController` 6 个端点，含测试连接、获取表列表。

### 2.2 SCM 管理（scm）

`ScmController` 7 个端点，含测试连接、获取导航锚点。支持 4 种认证方式：公开/账号密码/SSH KEY/PAT。

### 2.3 类型映射方案（tymSchema + tymSchemaField）

定义数据库类型 → Java/Vue 类型的映射规则。

### 2.4 原始模型（rawModel）

2 个端点：从数据源同步表结构为原始模型 `RawModelPo`。

### 2.5 聚合模型（polyModel）

6 个端点。`PolyModelPo` 包含字段级别的 CRUD 可见性策略（ADD/EDIT/DETAILS/LIST_QUERY/LIST_VIEW）和显示策略（文本框/文本域/下拉/日期等）。

### 2.6 输出方案（opSchema）

`OpSchemaController` 12 个端点，**最复杂的 Controller**。使用 `ReentrantLock` 保护并发执行。

## 3. 核心流程

```
【配置阶段】
DataSource → TymSchema → SCM → OpSchema

【准备阶段】
syncRawModelFromDataSource → importFromRaw

【生成阶段】
previewQbeModel → previewOpBluePrint → executeOpSchema
```

## 4. 设计亮点

1. **ReentrantLock 单线程保护**：防止并发执行代码生成
2. **Git URL 标准化**：统一处理各种 Git URL 格式
3. **多阶段流水线**：每阶段可独立操作
4. **SCM 多认证方式**：公开/账号密码/SSH KEY/PAT，密码加密存储
5. **policyCrudJson 可见性策略**：JSON `Set<String>` 存储字段显隐配置

---

# 三、Audit 模块（biz/audit/）：审计模块

## 1. 模块概述

- **职责定位**：系统级审计日志，记录用户登录审计和系统错误记录
- **文件数量**：19 个 Java 文件
- **核心入口类**：2 个 Controller、2 个 Service、1 个事件监听器

## 2. 核心流程

### 登录审计自动记录

```
用户登录尝试
 → Spring Security 触发事件
 → AuthAuditListener 监听
 → 从 Authentication 获取用户信息
 → 从 RequestContextHolder 提取 IP/User-Agent
 → 记录审计日志
```

### 失败原因链

```
onFailure → 检查异常链
 ├── RootUnavailableException → "租户不可用"
 ├── AuthUnavailableException → "认证不可用"
 ├── 用户名不存在 → "用户不存在"
 └── 默认 → "用户密码错误"
```

## 3. 设计亮点

1. **非侵入式审计**：通过 `@EventListener` 自动拦截，不侵入业务代码
2. **失败原因分类**：区分 4 种失败场景
3. **RequestContextHolder 提取上下文**：无需从方法参数传递 `HttpServletRequest`

---

# 四、Qt 模块（biz/qt/）：快速任务调度

## 1. 模块概述

- **职责定位**：基于 Quartz 的定时任务调度引擎，支持本地 Bean 和远程 HTTP 任务
- **文件数量**：40 个 Java 文件
- **核心入口类**：3 个 Controller、3 个 Service、`QuickTaskRegistry`、`LocalBeanExecutionJob`

## 2. 核心流程

### 任务注册流程

```
应用启动 → ApplicationReadyEvent
 → QuickTaskRegistry 扫描所有 QuickTask Bean
 → 存入 QT_BEAN_MAP（ConcurrentHashMap）
 → 冲突检测 → 启动 Quartz Scheduler
```

### 任务执行流程（LocalBeanExecutionJob）

```
Quartz 触发器 → executeInternal
 → 从 JobDataMap 提取 taskId/beanName/jsonParams
 → 查找 QuickTask Bean
 → 通过 ResolvableType 解析泛型参数类型
 → JSON → 业务对象自动转换
 → 执行业务逻辑
 → 根据 policyError/policyRcd 处理异常
```

## 3. 数据模型

| 表名 | Po | 核心字段 |
|------|-----|----------|
| `qt_task` | `QtTaskPo` | groupId, name, kind(0/1), cron(64), target(1000), targetParam(JSON), concurrent, policyMisfire, policyError, policyRcd, status(0/1/2) |
| `qt_task_group` | `QtTaskGroupPo` | name(unique), remark |
| `qt_task_rcd` | `QtTaskRcdPo` | taskId, startTime, endTime, costTime, status, targetResult |

## 4. 设计亮点

1. **QuickTask<T> 泛型接口**：通过 `ResolvableType` + `AopUtils.getTargetClass()` 运行时解析泛型参数类型
2. **QuickTaskRegistry 延迟启动**：`ApplicationReadyEvent` 后注册，避免循环依赖
3. **Quartz JobDataMap 传参**：taskId/beanName/params 塞入 JobDataMap
4. **多种失败策略**：`policyError`（默认/自动暂停）、`policyRcd`（全部/仅异常/不记录）
5. **并行控制**：`concurrent` 字段控制是否允许并发
6. **Misfire 策略**：放弃/立即执行/全部执行

---

# 五、Qf 模块（biz/qf/）：流程引擎

## 1. 模块概述

- **职责定位**：基于 Flowable 工作流引擎的企业级审批流程系统
- **文件数量**：103 个 Java 文件
- **核心入口类**：8 个 Controller、8+ Service、5 个事件监听器

## 2. 核心流程：发起→审批→完成

```
【模型设计】→ 【部署】→ 【发起流程】→ 【引擎事件】→ 【待办创建】→ 【审批】→ 【流程完成】
```

关键细则：
- 部署时通过 `QfFlowableConfig` 注册 5 个类型化事件监听器
- 30 秒窗口去重：区分 Flowable "初始注入 assignee" 与用户 "转办/加签"
- 待办状态流转：0(待办) → 1(已办) / 10(已作废)

## 3. 数据模型

| 表名 | Po | 核心字段 |
|------|-----|----------|
| `qf_model` | `QfModelPo` | rootId, deptId, activeDeployId, name, code, bpmnXml(LONGTEXT), version, status(0/1/2) |
| `qf_todo` | `QfTodoPo` | engTaskId, engProcId, tableName, dataId, nodeName, memberType(0/1), memberId, initiatorId, status(0/1/10), action(0/1), comment, duration |
| `qf_biz_form` | `QfBizFormPo` | tableName, 表单配置 |
| `qf_cc` | `QfCcPo` | 抄送记录 |

## 4. 设计亮点

1. **Flowable 事件驱动架构**：5 个类型化监听器覆盖全生命周期
2. **30 秒窗口去重**：区分初始注入与主动转办
3. **`QfProcTools` 工具类**：类型安全地从流程变量读取值
4. **节点名三阶段回落**：BPMN name → taskDefinitionKey → "审批"
5. **LaunchParam Builder 模式**：支持摘要模板变量和节点指定处理人
6. **模型版本控制**：草稿/已部署/历史三态
7. **部署挂起/激活**：运行时控制流程定义可用性
8. **双 ID 生成器**：JPA 和 Flowable 独立雪花 ID 生成器

---

## 模块间依赖关系

```
        ┌────────────────┐
        │     Core       │ ← 用户/组织/注册表/附件/雪花ID
        └───────┬────────┘
      ┌─────────┼─────────┐
      │         │         │
  ┌───┴───┐ ┌──┴──┐ ┌───┴───┐
  │  Qt   │ │ Qf  │ │Audit  │
  └───────┘ └─────┘ └───────┘
      │         │
      └────┬────┘
           │
     ┌─────┴─────┐
     │ Assembly   │
     └───────────┘

共享基础设施：
- RegistrySdk（动态配置中心）
- SessionService（租户/用户上下文）
- SnowflakeIdGenerator（全局 ID 生成）
- RowScopePo（数据权限基类）
- Entities 映射工具
- Result/PageResult 统一响应
```

**总结**：五大模块构成了系统的完整骨架 — Core 提供用户-组织-权限-配置基础设施，Assembly 实现数据库驱动的代码自动生成，Audit 提供非侵入式安全审计，Qt 基于 Quartz 的弹性任务调度，Qf 基于 Flowable 的企业级审批流程引擎。五者通过共享基础设施紧密协作，形成完整的企业应用开发平台架构。
