# BioCode v1.7 更新日志

> 版本范围：1.7A1 → 1.7D54

---

## 1.7B1-探索与连接

Version 1.7B CheckPoint 1 Preview

长久以来，企业的信息化系统像一座沉默的仓库——数据在墙内运转，而外部的智能体对此一无所知。我们希望改变这一点。不需要炫技式的「AI 重构一切」，只需要一条安静的通道：当 Cursor 这样的 AI 工作台想要了解你的系统时，它能听到回答。这就是 Advanced AI Compatibility Project —— AACP 计划。

它的核心思想并不复杂：**让外部 AI 客户端通过标准协议，安全地发现和调用企业内部能力**。我们选择了 MCP（Model Context Protocol），一个由社区推动的开放协议。它朴素、务实，建立在 HTTP + SSE 和 JSON-RPC 2.0 之上——没有自定义二进制协议，没有复杂的认证中间层，标准 HTTP 栈即可承载。

这不是一个宏大叙事。它只是一个开始。AACP 在当前版本中完成了最基础的一环：从零构建了一个 Java 端的 MCP 服务端点，并验证了 Cursor 客户端通过 supergateway → SSE → JSON-RPC → 服务端硬编码响应的完整链路。你可以把它想象成一次握手——BioCode 第一次向 AI 说出了「你好」。

事实上，BioCode 一直在刻意保持谨慎。我们不追逐工具链的潮流，不在生产系统上押注尚未成熟的框架。AACP 的每一行代码都运行在 Spring Boot 上，SSE 连接器来自框架原生组件，序列化用的是 Gson——没有引入任何一处尚处试验阶段的依赖。这是我们一贯的风格：**用成熟的技术，做可靠的连接**。

AACP 目前作为 BioCode 的一个实验性子域（`biz.aacp`）存在。它不会影响已有业务，也不强制任何既有模块接入。你可以把它看作一座刚刚打下第一根桩的桥。

_让我们继续建造。_

后端改进

1. 新增 `AacpMcpController`：实现 MCP SSE 端点（`/upstream/{code}`）与 JSON-RPC 请求处理器（`/request`），完成 initialize、tools/list、tools/call 标准方法封装
2. 新增 `McpRpcDto`：JSON-RPC 2.0 请求体 DTO，兼容 initialize 与 tools/call 两种参数结构
3. 新增 `McpRpcResult`：JSON-RPC 2.0 通用响应体，内置标准错误码（-32700 解析错误 / -32600 无效请求 / -32601 方法未找到 / -32602 无效参数 / -32603 内部错误）
4. 新增 `AacpMcpCapabilityRepository` 与 `AacpCapabilityFuncRepository`：能力包与微函数被引用时的删除保护查询
5. AacpCapabilityService 与 AacpFuncService 删除保护：被 MCP 服务器或能力包引用时阻止删除，并明确告知引用数量；禁止批量删除
6. AddAacpMcpDto 与 EditAacpMcpDto：实现 `DtoCustomValidator` 接口，PSK 鉴权模式下自动校验预共享密钥必填
7. AllTinyIntPo：为所有 TINYINT 映射字段补齐 `columnDefinition = "tinyint"` 约束，确保 DDL 精度一致

前端改进

1. 全部 AACP 列表页升级为 Std 标准布局：`StdListContainer` + `StdListAreaQuery` + `StdListAreaAction` + `StdListAreaTable`（淘汰旧 `StdListLayout` + `#pagination` 写法）
2. 查询区统一采用 `inline class="flex justify-between"` 弹性布局，按钮独立包裹于 `<el-form-item>`
3. 所有输入框补齐 `maxlength` + `show-word-limit` 三位一体校验
4. 统一术语：创建 / 保存 / 关闭，移除「新增」「取消」「确定」等旧文案
5. 列表首列统一增加序号列（`type="index"`），操作按钮接入 View / Delete 图标
6. 模态框 `v-if` 正确放置于 `<el-form>`（关闭时销毁校验残留），Footer 统一为「关闭 + 创建/保存」按钮组合
7. aacp_mcp 表结构变更后，前端 Api、Service、SFC 三层同步移除 host/port 字段，通信协议放开 HTTP+SSE / WS 双选项

增量业务功能

1. 新增 AACP 域（`biz.aacp`）：MCP 服务器、能力包、微函数三模块完整 CRUD 后台管理界面
2. 菜单注册：AACP 配置 目录 + MCP 服务器 / 能力包 / 微函数 三项菜单，图标 ep:connection / ep:server / ep:folder-opened / ep:coin
3. MCP 服务器与 Cursor 握手成功：经 supergateway → SSE → JSON-RPC 通道，验证了 initialize → tools/list → tools/call 完整调用链路

---

## 1.7B47-微函数

Version 1.7B CheckPoint 47 Preview

CheckPoint 1 证明了通道能走通，但当时的工具列表是硬编码的哑数据，tools/call 也只返回固定文本。CheckPoint 47 完成了从"摆样子"到"真干活"的跨越：五个微函数全部通过 Cursor 实战调用验证，工具列表自动生成 JSON Schema 入参规范，新增 CURL 工具让 AI 代理具备了访问外部网络的能力。

后端改进

1. 工具列表的 inputSchema 不再返回空对象，改为自动解析微函数参数类型并生成完整 JSON Schema（类型、属性、必填项），客户端现在能正确渲染工具参数表单
2. 微函数调用链路修正：tools/call 通过工具标识查 DB 获取目标名称，再匹配注册中心，解决了标识与注解目标不一致导致的"微函数不存在"问题；单参数简单类型（如 String）的参数注入逻辑也一并修正
3. 微函数注解 `@MicroFunc` 中 `code` 属性重命名为 `target`，统一全链路命名（定义、注册、查询、前端展示），消除概念歧义
4. 微函数 Schema 生成逻辑从 Service 层收敛到 `MicroFuncDefinition` 单一入口，避免多处重复

前端改进

1. 已注册微函数列表展示字段从 `code` 更名为 `target`，与后端注解和注册中心命名对齐

增量业务功能

1. 新增 CURL 微函数：基于 Java 11 HttpClient 实现 HTTP GET 请求，AI 代理可通过此工具访问外部 API 或网页，响应超过 2000 字符自动截断，支持连接超时与读取超时控制
2. 微函数家族扩充为 5 个内部工具：问候（test.hello）、时间（test.time）、加法（test.add）、回声（test.echo）、HTTP 请求（test.curl），全部通过 Cursor 实战调用验证

---

## 1.7C35-累积质量改进与高级工作流

Version 1.7C CheckPoint 35

### QFE — Quick Flow Extension(高级 QF 扩展)

审批流程的实际需求远比"指定一个人审批"复杂：发起时我要选人、某个节点不审直接过、财务节点必须填意见否则不让提交、这个按钮叫「转办」不叫「转交」、科长审批时允许他直接改报销金额……这些逻辑如果每次都在 Java 代码里硬写，一个审批模板的改动就意味着改 Service、改 Listener、重新部署。

QFE 做了一件事：把这些反复变动的审批控制点从代码里抽出来，变成 BPMN 设计器右侧面板里的配置项。设计者拖拽节点、勾选开关、填写按钮文案、打钩哪些字段允许审批中编辑，保存后后端从 BPMN XML 里读出这些属性，运行时按配置驱动——不用改一行代码。

后端改进

1. 流程启动参数重构：将分散的启动参数（模型编码、数据 ID、摘要模板变量、发起选人）收敛为 `LaunchParam` 构建器，参数校验内聚，调用方不再需要感知内部字段拼装
2. 新增「发起时选人」节点类型：BPMN 中标记为 `utAprKind=1` 的节点在流程启动时要求发起人逐一指定办理人，后端校验所选成员是否落在节点配置的成员范围（指定人/用户组/任意人）内
3. 新增「发起时跳过」机制：标记 `utGeInitSkip=1` 的节点在流程启动后自动完成，无需进入待办，适用于自动通知、记录存证等无需审批的节点
4. 审批操作按钮动态配置：每个节点可通过 BPMN 扩展属性 `utAprActions`（操作值）和 `utAprActionNames`（显示文本）自定义审批页面的操作按钮，支持同意(0)、驳回(1)、转交(2)、驳回到指定节点(3) 四种操作，未配置时默认回退为「同意/驳回」
5. 审批意见填写动态开关：节点 `utAprComment` 属性控制是否要求审批人填写意见（1=必须填写，0=可跳过），前端根据此标记决定意见输入框是否必填
6. 转交功能：审批人可将待办转交给指定用户，底层通过 Flowable `setAssignee` 触发 `TASK_ASSIGNED` 事件，侦听器自动将原待办作废并创建新待办，与引擎任务初始分配的 30 秒窗口互不干扰
7. 驳回到历史节点：审批人可将待办驳回到上游已办节点，后端验证目标节点确属已处理过的历史节点（防止驳回越权），通过 Flowable `changeActivityState` 迁移流程状态
8. 摘要模板引擎：业务表单支持配置摘要模板（`summary_template`），流程启动时传入模板参数映射，后端用 `PreparedPrompt` 解析模板占位符生成动态摘要文本（如「张三提交的 5000 元差旅报销」）
9. 流程节点配置查询接口：`getProcNodeDefine` 按流程执行顺序返回所有节点及其成员配置（含实时成员姓名，不在设计器快照中读取），前端可据此展示完整的审批链路
10. 流程流转记录与着色 BPMN：提供审批流转历史记录查询（按时间序返回各节点的办理人、结果、意见）和着色 BPMN XML（根据流程进度高亮已完成/当前/未到达节点）
11. 或签多实例孤儿待办清理：或签场景下，任一办理人完成审批后引擎自动取消其余并行任务，但 `TASK_CANCELLED` 事件不可捕获，后端通过比对活跃任务集自动作废引擎已取消但业务待办仍为待办状态的记录
12. QF 域全线接入租户权限（`@CreatedRootId`），所有流程控制器和实体均支持行级数据隔离
13. 新增待办成员分类枚举（`TodoMemberCategory`：办理人/候选组）与待办状态枚举（`TodoStatus`：待办/已办/已作废）
14. 数据库新增 6 张 QF 域建表脚本（流程模型分组、流程模型、部署记录、业务表单、待办事项、抄送表），支持全新部署
15. BPMN 模型保存与部署时接入 Flowable `ProcessValidator` 双重校验：第一层语法校验（XML 结构合法性、标签正确性），第二层语义校验（连线是否断裂、网关出口数量、用户任务办理人配置、多实例参数完整性），只有通过全部 error 级别规则才允许保存或部署
16. 修复用户查询条件逻辑错误，修正组织服务代码格式一致性问题

前端改进

1. 框架布局组件化重构：默认布局（侧边菜单+多标签+面包屑）独立为 `DefaultLayout` 模块，注册到布局管理器按路由 `meta.layout` 切换页面框架
2. 新增多认证组件注册机制，支持按路由配置不同的登录界面组件
3. 新增标准日期时间范围选择组件 `StdDateTime`，提供统一的日期筛选交互
4. 流程节点配置展示：流程定义页面可按顺序加载所有审批节点及成员信息，发起时对「发起选人」节点提供人员选择入口
5. 审批操作按钮按节点自定义渲染：操作栏根据后端返回的 `allowActions` 列表动态生成按钮，按钮文案和操作类型均由节点配置驱动
6. 审批意见输入框按节点控制：根据后端返回的 `allowComment` 标记决定意见输入框是否展示及是否必填
7. 机构树组件交互优化，人员选择器兼容无姓名用户展示
8. 模型与部署记录页面优化业务表单关联显示和审批流程交互
9. 用户登录成功后自动清空多标签缓存，避免切换账户后残留旧标签页

增量业务功能

1. 多布局与多认证体系初步建成，为后续接入空白布局、移动端布局、外部 SSO 登录等场景奠定基础
2. QFE 流程扩展体系：在标准 BPMN 用户任务上扩展自定义属性，覆盖审批节点类型、办理成员范围、多实例策略、操作按钮、审批意见开关、可编辑表单字段、发起时跳过等全部流程控制维度，设计时所见即所得，运行时无需改代码
3. 发起时选人：流程启动时由发起人为标记为「发起时选人」的节点逐一指定办理人，非流水线固定审批人
4. 发起时跳过：标记为跳过的节点在流程启动后自动完成，适用于纯通知、记录存证等无需审批的环节
5. 审批操作按节点定制：每个节点可独立配置可见的操作按钮（同意/驳回/转交/驳回到指定节点）及其显示文案
6. 审批意见按节点控制：每个节点可独立设置是否需要审批人填写意见，意见输入框按配置动态展示和必填
7. 转交：审批人可将待办转给他人处理，系统自动作废旧待办并为新办理人生成新待办
8. 驳回到历史节点：审批人可将待办驳回到上游某个已办的节点重新处理，后端校验目标节点确属历史路径
9. 摘要模板：流程启动时根据预设模板和业务数据自动生成待办摘要（如「张三提交的 5000 元差旅报销」）
10. 审批中可编辑表单：设计时按节点勾选允许编辑的字段，审批人在待办中直接修改并随审批意见回写，无需退出流程
11. 流程节点可视化：发起时按流程顺序展示全部审批节点及配置，对「发起选人」节点提供人员选择入口
12. 流程流转记录与着色进度图：按时间序查看各节点的办理人、结果、意见，审批进度图高亮已完成/当前/未到达节点
13. 会签与或签：节点支持会签（全员同意通过）和或签（任一人同意即通过），或签场景自动清理被取消的孤儿待办
14. BPMN 模型双态校验：保存时语法校验，部署时语义校验（连线完整性、网关出口、办理人配置等），堵住非法模型流入引擎

---

## 1.7D-智能体数据源

Version 1.7D CheckPoint 54

后端改进

1. AACP 全链路术语统一：MCP 服务器 → 智能体枢纽（AgentHub），能力包 → Cap，涉及 Controller、Service、Repository、Model、SQL 迁移共 50+ 文件，表名同步重命名，数据库新增 AgentHub-Cap、Cap-MicroFunc、Cap-Datasource 三张多对多关联表
2. AACP 访问服务重构：原 AacpEndpoint 拆分为 AacpAccessController + AacpAccessService，SSE 连接管理、JSON-RPC 路由、微函数分发三者职责分离，权限注解与表名一致性修正
3. 微函数调用链拆分：从 MicroFuncService 中独立出 MicroFuncCallService，专注运行时微函数查找与参数注入，与后台 CRUD 管理解耦
4. 数据源管理模块：QBE 代码生成引擎批量生成完整 CRUD（Po + 3 Dto + 2 Vo + Repository + Service + Controller），随后整合到 aacp.model.datasource 子包，新增连接测试端点，编码输入时自动生成 JDBC URL，空用户名/密码容错处理
5. 数据源与能力包深度集成：Cap 编辑时可挂载多个数据源（CapDatasource 关联表），删除数据源时校验是否被 Cap 引用，防止误删
6. 在线会话管理：新增 AacpSessionController，SSE 会话注册/移除/查询/批量关闭，McpClientSession 扩展会话元数据（枢纽名称、连接时间），心跳机制避免 JDBC 连接空闲占用
7. GroupMenuRepository 查询修复：允许返回重复菜单项以匹配原有路由注册机制
8. AuthUserDetailsService 权限加载路径补全

前端改进

1. AACP 全术语同步重命名：AacpMcp → AacpAgentHub、AacpCapability → AacpCap、AacpFunc → AacpMicroFunc，Vue SFC、Api、Service、Route 四层全部对齐
2. 数据源管理页新增连接测试按钮、批处理状态列、编码失焦自动填 URL 交互，删除前校验引用关系
3. 在线会话管理页（AacpOnlineSession）：展示当前所有活跃 SSE 连接（枢纽名称、会话 ID、连接时间），支持单选/批量关闭，关闭后实时刷新列表
4. Cap 编辑页新增数据源选择区：可从已配置数据源中多选关联，列表展示已关联数据源名称
5. ComTabService 路由可用性检查：标签切换前校验路由有效性，防止导航到不存在页面导致白屏
6. 数据源管理页整合到 AACP 模块：与智能体枢纽、Cap、微函数并列，路由注册更新，菜单归入 AACP 配置目录

增量业务功能

1. 数据源连接测试：在后台配置 JDBC 数据源后，可一键验证连接是否可达，避免配置错误到运行时才发现
2. 在线会话管理：管理员可实时查看哪些 AI 客户端正在通过 MCP 连接系统，支持强制关闭异常或僵死会话
3. MCP Inspector 调试脚本（MCPInspector.ps1）：一键检测 Node.js/npm 环境，自动安装 @modelcontextprotocol/inspector，启动 SSE 调试器连接本地 27500 端口
4. MySQL MCP 操作技能文档：为 AI 工作台提供标准化的数据库查询操作指南，支持 SELECT 查询与结果格式化
5. CodeGraph 全局规则强化：扩展为 51 行完整规范（原 15 行），新增禁止创建子代理条款，要求所有代码探索任务（定位符号、追踪调用链、影响分析）必须优先走 CodeGraph 亚毫秒级查询
6. 数据库设计文档（BioCode.pdma.json）大幅更新：补齐 AgentHub、Cap、MicroFunc、Datasource 及三张关联表的完整字段定义与关系图
