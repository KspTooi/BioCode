# BioCode v1.7 更新日志

> 版本范围：1.7A~1.7Z

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
