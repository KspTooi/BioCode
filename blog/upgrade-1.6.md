# BioCode v1.6 更新日志

> 版本范围：1.6A#1 → 1.6Z57

---

## 1.6A#1-岗位与任务

Version 1.6A CheckPoint 1

后端改进
1.用户会话表core_user_session迁移至auth域auth_user_session 并记录flyway

前端改进
1.QT轻任务调度整个域的前端页面现在都支持"查询持久化"特性

增量业务功能
1.Core核心域新增业务 岗位管理CRUD 新增、编辑、删除、批量删
2.QT轻任务调度 现在支持有效期截止(到设定时间自动停止)
3.QT轻任务调度 现在支持配置任务失败策略(自动暂停和等待下次调度)
4.QT轻任务调度 现在支持配置日志策略(全写、只写失败、不写)
5.QT轻任务调度 现在支持任务快照

---

## 1.6A#17-QT交付

Version 1.6A CheckPoint 17

后端改进

前端改进
1.QT轻任务页面UI优化
2.编辑、新增任务调度时增加对参数JSON格式的严格校验

增量业务功能
1.QT轻任务域模块测试用例通过 可交付使用

---

## 1.6C#15-滑动验证码

Version 1.6C CheckPoint 15

后端改进
1.整合TIANAI滑动验证码(暂无二次认证)
2.注册表SDK
3.移除大量旧式组件

前端改进
1.统一后端Api计算方式,通过Http模块
2.移除大量旧式组件
3.在登录界面整合滑动验证码

增量业务功能
1.QT轻任务域 增加"立即执行"功能,并支持临时参数
2.QT轻任务域 现在支持过期任务显示
3.QT轻任务域 现在支持任务的导入与导出(暂未制作模板)

---

## 1.6G#5-品牌重构

Version 1.6G CheckPoint 5

后端改进
1.彻底改进集成部署模式和标准部署模式间的混乱配置
2.新增OSHI依赖
3.重构后端 统一包名为com.ksptool.bio
4.重构资源文件夹，统一Support资源和Static资源
5.现在有resource/support-static用于存储那些系统支持资源 例如:默认的Excel导入模板、代码生成器蓝图模板、数据库原始设计稿(这些都无需暴露给用户)
6.现在有resource/web-static用于存储那些需要通过静态资源访问的内容 例如: css、js、图像等
7.改进前端项目目录 现在前端项目位于resource/web-ui

前端改进

增量业务功能
1.CORE域 新增功能"系统探针"
2.项目品牌升级 新品牌名称BioCode(生化代码)

---

## 1.6G#16-系统探针

Version 1.6G CheckPoint 16

后端改进

前端改进

增量业务功能
1."系统探针"模块视觉效果改进
2."系统探针"模块增加"系统信息" 这会显示系统环境、JVM信息、磁盘列表、网卡列表

---

## 1.6G#20-系统探针

Version 1.6G CheckPoint 20

后端改进

前端改进
1.修复"Excel"导入模板下载错误问题
2.更新AI编码规则 禁用前端相对路径导入

增量业务功能
1."系统探针"模块视觉效果改进
2."系统探针"模块功能测试通过 可交付

---

## 1.6H#10-行级数据权限

Version 1.6H CheckPoint 10

后端改进
1.Auth模块二期扩展使得系统支持行级数据权限。
2.现在可以在Po上继承RowScopePo并在控制器或服务加入RowScope注解以启用RS数据权限

前端改进

增量业务功能

---

## 1.6I#25-数据权限交付

Version 1.6I CheckPoint 25

后端改进
1.Auth域二期扩展进行集成测试 修复多项问题，完善二期功能。
2.修复: RS数据权限模块 修复Hibernate过滤器不自动关闭问题(现在在后切中关)
3.修复: 用户会话模块现在拥有完整数据权限
4.修复: User表与Session表增加版本号机制,这是为了热更新用户Session和缓存一致性准备的。
5.现在操作用户、组(角色)、权限码、组织架构时会为当前受影响的所有在线用户增加版本号并且清除缓存，当下一次请求到来时Session将会自动刷新以获得最新的权限码和预计算RS数据权限
6.修复: User表雪花ID失效问题
7.修复: User表字段按标准术语重命名，编写flyway迁移脚本
8.用户默认的DV现在是0
9.用户会话(Session)现在包含更多扩展字段 如DV、用户名、部门、租户
10.同步会话和用户表的Hibernate实体定义(主要是字段长度)
11.现在操作用户、组(角色)、权限码、组织架构时将会清空在线用户的菜单缓存，下一次请求到来时用户将会获得根据权限过滤的最新菜单
12.修复: 现在方法级权限校验异常后不会再出现服务器内部错误，而是返回标准的401
14.现在移动组织架构时，其自身和子节点的路径IDS将会正确查找计算，以解决查询不精确的问题
15.修复: 数据权限Hibernate过滤器导致原有查询条件失效的问题
16.修复: 在使用AUD构建RS数据权限时 如果RS级别为"本部门及以下" 现在将会把用户自身的部门也添加进Allow列表
17.优化RowScopePo数据权限过滤器，父类只制定标准而不是强迫子类使用那三个字段("root_id","dept_id","creator_id") 这些字段现在由子类控制，必须显示在子类中声明

前端改进
1.修复路由问题

增量业务功能
1."Auth域二期扩展(数据权限)"测试通过，已可以交付使用。
2.现在有新的"首页"可以使用，用户无任何权限时至少能看首页。
3.404、401页面重制与优化
4."在线用户"模块现在支持实时查看在线用户的"RS数据权限"和"权限码"

---

## 1.6J#15-按钮权限

Version 1.6J CheckPoint 15
后端改进
1.增加了一个简单的限流模块(RL)，以供集成环境下使用，提供了有限的集群支持(需要Redis)。
2.修复: GroupController.getGroupPermissionNodeView接口筛选条件问题
3.RS数据权限现在也支持超级权限检查，对于那些拥有超级权限的用户将不受任何数据权限限制。
4.CORE域注册表模块二期：这一期完善了注册表SDK，使得后端使用代码访问注册表更轻松，并为SDK的许多操作函数增加了缓存。
5.注册表现在拥有系统枚举SystemRegistry

前端改进
1.增加按钮级权限检查功能，补全权限最后一块拼图。现在可以在元素上使用v-has-code自定义指令判断用户按钮权限(此前虽然后端有权限验证，但前端按钮依然可点)
2.前端按钮级别权限现在支持超级权限检查 即拥有超级权限的用户(*:*:*) 不限制任何权限
3.优化: 用户管理界面中企业列的最小宽度并启用溢出提示

增量业务功能
1.维护中心现在支持修复系统注册表(恢复默认)

---

## 1.6K#10-异常管理

Version 1.6K CheckPoint 10

后端改进
1.清除多个域的冗余代码
2.添加通用响应结果代码枚举类ResultCode，定义业务状态码及对应HTTP状态码和描述信息
3.添加处理用户未绑定租户异常的异常处理方法，增强异常管理

前端改进

增量业务功能
1.私有域业务功能增量

---

## 1.6M#11-私有域迭代

Version 1.6M CheckPoint 11

后端改进
1.省略私有域业务功能

前端改进
1.省略私有域业务功能

增量业务功能
1.私有域业务功能增量

---

## 1.6M#32-私有域迭代

Version 1.6M CheckPoint 32

后端改进
1.省略私有域业务功能

前端改进
1.省略私有域业务功能

增量业务功能
1.私有域业务功能增量

---

## 1.6N1-安全加固

Version 1.6N CheckPoint 1

后端改进
1.更新UserPo类中的status字段，添加columnDefinition属性以确保数据库兼容性
2.引入DGWM动态管理全局白名单，更新SecurityConfig以使用新的白名单管理器，同时添加UserSessionAuthFilter以实现基于会话的认证机制
3.废弃自动携带的Cookie鉴权方式，改为更安全的Authorization Bearer头
4.引入AppRegistry和AppVersion类，重构版本管理逻辑，更新相关代码以支持新的版本控制机制
5.支持URL参数中携带token以进行GET请求鉴权 (也可以发送Authorization Bearer头 二选一)
6.引入安装向导拦截器以支持安装向导模式，优化用户会话认证逻辑并增强安全性
7.数据库基线版本更新 现在使用1.6.1359(1.6M59)脚本进行数据初始化

前端改进
1.修复集成部署时ExcelTemplate上传失败问题 使用Http.axios()替代axios，确保一致的HTTP请求处理
2.现在前端支持动态版本号，用户登录时后端将返回应用版本号

增量业务功能
1.通过废弃Cookie鉴权方式彻底解决了多个CSRF安全问题
2.前端动态版本号显示
3.应用初始化安装向导
4.QF(QuickWorkFlow)域一期规划

---

## 1.6N45-GEN规划

Version 1.6N CheckPoint 45

后端改进

前端改进

增量业务功能
1.GEN域一期规划

---

## 1.6N68-DTO验证

Version 1.6N CheckPoint 45

后端改进
1.新增自定义DTO验证功能，包含DtoCustomValidator接口和DtoCustomValidatorAspect切面，支持在控制器中进行自定义验证

前端改进

增量业务功能
1.GEN域研发

---

## 1.6O65-数据源

Version 1.6O CheckPoint 65

feat: 添加新的控制器、服务、DTO和VO以支持#{PTSTN}功能，更新OutSchemaService以整合数据源和SCM操作

---

## 1.6S32-基础架构大修

Version 1.6S CheckPoint 32

后端改进
1.Gen域更名为Assembly域
2.使用第二代生成引擎(QBE)代替第一代生成引擎(AssemblyBP)作为代码生成器，QBE引擎更简单、拥有更好的兼容性
3.新增用于JPA自动雪花ID的注解与生成器
4.修复多个域的OpenAi文档、修复错误并补充遗漏
5.添加Druid连接池支持，添加静态资源和Druid监控页面到动态全局白名单
6.安全升级 接口白名单以及鉴权由新的DGWM组件管理
8.支持JPA审计功能，实现自动填充审计字段(创建时间、创建人等……)
9.更新全部域的PO类，将手写的逻辑替换为Jpa审计模式
10.Core域新增6个JPA转换器，用于PO中的字段转换映射com.ksptool.bio.biz.core.common.jpa.ListCTJConv
11.Core域附件功能新增零散文件上传和预览功能，用于小文件非分片上传(主要是为简化小文件上传)
12.附件现在支持完整的租户隔离
13.修复Core域消息通知的多个已知问题
14.Core域新增DtoCustomValidator接口以及切面，通过实现该接口可以在Dto中编写自定义的复杂表单校验逻辑com.ksptool.bio.biz.core.common.aop.DtoCustomValidator
15.Core域组织架构功能的多个已知问题修复
16.添加JsonEntityMapperConfig类以支持自定义JSON序列化和反序列化，增强日期时间处理能力
17.新增ByteUtils类，提供字节数组处理工具方法，包括转换、合并、分片和随机生成等功能
18.现在后端支持Mybatis数据权限，包含RsContext、RsContextHolder和RsBuilder类以实现动态SQL构建和权限上下文管理
19.添加DecimalCompare类，提供大数值比较功能，包括小于、大于和等于的判断方法
20.SpringBoot版本升级为更稳定的4.0.5
21.新的QBE生成模板，支持后端JPA+Mybatis一键代码生成
23.新的RS数据权限注解和切面，支持基于用户的静态数据权限过滤
24.新的RS数据权限注解和切面，支持基于租户的静态数据权限过滤
25.Core域菜单系统后期大修，将Resource表的多态菜单重构为core_menu表，简化菜单模块逻辑
26.从Core域移除整个端点管理功能 删除 EndpointController
27.从Core域移除整个资源管理功能 删除Resource以及相关联组件
28.从Core域移除整个旧式公司及旧式公司人员模块
29.维护中心衍生修改，去除对端点管理的支持

前端改进
1.新增前端CDRC服务，用于不同路由间带复杂参数跳转
2.新增前端COTRC服务，用于已知路由间的跳转，完美兼容左侧菜单与标签页
3.所有域的表格增加"序号"列 增强用户交互
4.所有域的模态框表单现在在完成操作后都会自动关闭
5.优化权限服务和用户组服务，调整代码格式和逻辑结构，提升可读性
6.SOA组件拆分为COM系列与STD系列，COM组件为带有特殊业务逻辑的组件，STD为不包含业务逻辑的组件
7.新增StdTimeRange.vue组件
8.重构前端菜单组件，废弃旧的Store，所有菜单逻辑统一到MenuService中管理
9.新增Pinia持久化插件、现在Pinia可以持久化了
10.多项前端架构改进，挪动优化组件位置
11.移除多个旧组件并引入StdIconPicker组件以优化图标选择功能
12.移除多个RDBG相关组件以简化代码结构
13.前端现在集成了Eslint，并拥有特殊的项目代码规范，添加ESLint linting和修复命令以增强代码质量
14.更新tsconfig.json以支持ES2022和DOM库
15.GenricHotkey服务优化，改善并简化快捷键处理逻辑
16.前端全部代码根据新版ESLINT规则进行大重构，现在所有前端代码遵守新的编码规范
17.彻底废弃旧版多标签TabHolder，引入统一的ComTabService Api更加简洁，现在CTS将统一管理整个项目中的多标签功能
18.新的用户落地页
19.修复ComSeqFixerService中的多个问题，现在通过Fixer修改的排序最大值为655350
20.修复"用户管理"中组织架构树无法正常滚动的问题
21.新增CustomizeTagJson和ExtendedFileAttachJson接口以支持自定义标签和扩展文件附件数据结构
22.GRS新增getRouteByNameOrPath方法以支持根据名称或路径获取路由
23.修复持久化服务(QueryPersistService)在持久化对象时会错误序列化的问题
24.修复版本号获取错误问题
25.多个域的表格增加空值处理逻辑，空值在表格中将显示为-
26.新的QBE生成模板，支持更灵活的前端页面生成
27.新的StdCustomizeTagSelect 组件，完美支持CTJ格式

增量业务功能
1.用户组现在支持排序功能
2.新的用户落地页
3.多标签现在完美支持图标功能，将会展示菜单图标
4.组织机构现在支持4级 公司、子公司、部门、班组 而不是原来的两级(公司、部门)
5.EP域增加功能"提示词管理"
6.现在系统支持超过2级的菜单了

---

## 1.6T1-QF轻工作流

Version 1.6T CheckPoint 1

后端改进
1.QF流程模型分组功能优化改进 增加更强的校验
2.添加排序字段验证规则，确保排序值在0到655350之间
3.添加流程模型部署历史管理功能
5.优化菜单树添加空目录修剪功能
6.更新 jsch 依赖版本为更稳定的  2.28.0
7.代码生成器改进-SCM管理添加SCM URL标准化和格式校验功能 现在兼容多种GIT仓库URL格式
8.代码生成器改进-修复不同系统环境下的路径问题
9.代码生成器改进-重构SCM模块以通过浅拉取实现更快的检出速度
10.代码生成器改进-SCM模块现在会对比远端仓库是否有更新来决定是否拉取，如果没有更新则不会拉取 大幅提高检出速度
11.质量更新、用户登录多端以后登出不应销毁所有会话 而是销毁当前会话
12.新增SchemaValidationFailureAnalyzer类以分析Hibernate的SchemaManagementException，提供详细的错误描述和解决方案
13.完成QF轻工作流域的全部研发 包括模型组、模型、部署记录、待办事项、已办事项及业务表单，支持流程管理功能
14.针对数据库采用全新设计的43个数据域
15.新增QF成员类型枚举、成员服务及相关工具，支持办理成员的解析与管理功能
16.QF域抄送(CC)功能一期规划
17.添加多个流程和任务监听器以处理待办事项状态更新，确保在流程结束和任务取消时正确标记待办状态

前端改进
1.开发了全新的流程模型设计器(QFD)
2.完全废弃旧版混乱的流程模型设计器
3.更新ImportWizardModal.vue中的颜色定义，使用CSS变量替代硬编码颜色值以增强可维护性
4.更新StdTimeRange.vue，调整日期选择器的类名，增加showMsg属性以控制验证消息的显示
5.完成QF轻工作流域的全部研发 包括模型组、模型、部署记录、待办事项、已办事项及业务表单，支持流程管理功能
6.更新eslint配置，调整函数参数限制至16个并添加新常量以增强可读性

增量业务功能
1.QBE代码生成引擎现在已可以投入使用
2.QBE生成器添加导航锚点功能，现在可以通过在蓝图仓库中创建.qbeinput、.qbeoutput文件来定义导航锚点 前端可以直接选择锚点不用输入路径
3.QF轻工作流的部分模块已可以交付使用(流程分组、流程模型、流程部署、流程发起、表单管理、我的待办)

---

## 1.6U90-多租户架构

Version 1.6U CheckPoint 90

后端改进

1. 多租户架构（MultiRoot-MultiOrg）：新增 core_root 租户表与 admin_user_id 字段，支持租户管理账号 UID；新增租户管理模块（增删改查、DTO/VO/异常处理）；重命名旧表并迁移数据以适配新数据模型。
2. RS 7 级数据权限-2ID 方案：新的数据权限计算器与数据权限模拟器；移除旧式 6 级数据权限相关代码。
3. 维护中心改动，移除原有的"初始化用户"，"初始化角色"等功能，统一合并为"用户体系冷启动"，它将负责完成平台冷启动的所有工作。
4. 新增权限桶类(PermissionBucket)，它负责统一处理权限码的添加、合并、转换等操作，也负责系统与 SpringSecurity 的权限码桥接转换。
5. 新增权限码类(PermissionCode) 它负责定义权限码的格式与通配符规则。
6. 用户与组织：重构用户会话管理（AuthUserSession）,移除旧式(AuthUserDetails)以支持新的 MRMO 架构模型。
7. Flowable 工作流：新增流程节点状态枚举与工具类，支持生成带颜色标记的 BPMN XML；新增 QfProcStartedEvent/QfTaskStartedEvent 事件；
8. 增加了两个 JPA 转换器(ListLongConv/SetLongConv)
9. 审计模块包结构统一重命名。

前端改进

1. 多租户管理界面：新增租户管理页面（列表、创建、编辑模态框）。
2. 现在有新的 QFD 设计器可用，它是一个强大的自研流程设计器，支持 Flowable BPMN 2.0 标准，并提供了丰富的流程设计功能。
3. 维护中心新增用户体系冷启动功能，它将负责完成平台冷启动的所有工作。
4. 这次带来了全新的 StdBreadcrumb 面包屑组件，使得面包屑使用后端的菜单数据并且支持点击跳转，当没有数据时回退到路由 meta 中的静态 breadcrumb 数据。
5. 整个项目的所有页面统一术语为: 创建、编辑、删除、关闭、保存、批量删除。所有页面不再出现术语不统一的情况。
6. 前端工程化升级改造(第三批次)-规范，制定了更严格的代码规范（SOA、子系统说明、技术禁用项），并且对所有代码进行了重构以符合新的代码规范。
7. 前端工程化升级改造(第三批次)-AI 相容性：新增的 AI 相容性规范文档使得 AI 可以更好的理解代码并进行修改。
8. 前端工程化升级改造(第三批次)-既有问题处理：引入了更强的编译检查插件，现在不论是 ESLINT 还是 TS 类型检查不通过都会在 WEB 界面实时给出提示，而不是编译发版时才进行大爆破。
9. 升级依赖：Vite 从 6.2.1 到 8.0.10
10. 升级依赖：Vue 从 3.5.13 到 3.5.33
11. 升级依赖：VueRouter 从 4.5.1 到 5.0.6
12. 升级依赖：TypeScript 从 5.8.0 到 6.0.3
13. 升级依赖：Element Plus 从 2.11.3 到 2.13.7
14. 升级依赖：Pinia 从 3.0.1 到 3.0.4
15. 升级依赖：TailwindCSS 从 4.1.16 到 4.2.4
16. 升级依赖：vue-tsc 从 2.2.8 到 3.2.7
17. 升级依赖：ESLint 从 10.1.0 到 10.3.0
18. 升级依赖：eslint-plugin-vue 从 10.8.0 到 10.9.0
19. 升级依赖：typescript-eslint 从 8.57.1 到 8.59.1
20. 升级依赖：unplugin-icons 从 22.2.0 到 23.0.1
21. 升级依赖：unplugin-vue-components 从 29.0.0 到 32.0.0
22. 升级依赖：sass-embedded 从 1.93.2 到 1.99.0
23. 升级依赖：postcss 从 8.5.6 到 8.5.13
24. 升级依赖：prettier 从 3.6.2 到 3.8.3
25. 升级依赖：npm-run-all2 从 7.0.2 到 8.0.4
26. 升级依赖：autoprefixer 从 10.4.21 到 10.5.0
27. 升级依赖：@vue/tsconfig 从 0.7.0 到 0.9.1
28. 升级依赖：@tailwindcss/postcss 从 4.1.16 到 4.2.4
29. 构建优化：生产环境禁用 vite-plugin-checker，构建速度由 ~1 分钟优化至 ~10 秒。
30. 调整依赖版本限制，全部依赖均使用~严格锁定版本，不再使用^宽松锁定版本。

增量业务功能

1. 多租户-多集团（MultiRoot-MultiOrg）：系统从单公司模式升级为多租户多集团架构，支持最高 16 级的组织架构，每个租户独立管理其组织、用户和数据权限，支持租户级数据隔离。
2. RS 7 级行级数据权限：基于 Root → Org → DirectOrg 三层 ID 的 7 级权限模型，支持精细到用户级别的数据访问控制与权限模拟调试。
3. 全新的 QFD 系列流程设计器。
4. 新的用户体系冷启动功能，它负责初始化租户+用户+组+权限等一系列用户体系(维护中心)。
5. 面包屑现在可以点击跳转了
6. 页面术语不再混乱，且现在行为更加统一。
7. 用户组管理-编辑=>现在有新的数据权限模拟器可用，它可以代入现有的组织架构快速模拟你所选的数据权限范围。

---

## 1.6V54-菜单系统大修

Version 1.6V CheckPoint 54

后端改进
1.菜单权限码重构：调整菜单实体的 permissionCode 字段为 Set 类型，同步更新 AddMenuDto/EditMenuDto/GetMenuDetailsVo/GetMenuTreeVo 等 DTO/VO 的字段长度与注释，前后端类型对齐。
2.超级操作权限加固：增强超级操作权限检查逻辑，防止用户解除超级组的 SA 权限；统一权限仓库引用命名（pRepository），新增超级操作权限的专项处理。
3.用户组编码统一：将"用户组标识"重命名为"用户组编码"，同步更新 DTO、API 及实体注释；内置组禁止修改组编码。
4.用户状态体系重构：新增 Switch 类统一管理开关状态（封禁/启用）；UserPo、AuthUserSession、UserService 全部接入 Switch 进行用户状态判断；新增 isDisabled/isEnabled 方法；同步调整用户状态值与描述。
5.内置用户密码保护：新增内置用户密码修改限制，防止通过编辑接口强制修改内置用户密码；新增 CLAUDE_CODE_EFFORT_LEVEL 思考强度自定义功能。
6.控制器标签规范化：所有控制器标签统一为简短前缀（QT/AUDIT/CORE/AUTH），提升代码可读性与一致性。
7.数据库与附件实体优化：AttachPo 添加审计监听器，调整字段注释及长度，支持更深路径存储。
8.代码库清理：移除过时的 core_user_group、core_user_session、core_resource 等旧表设计方案；删除过时 SQL 文件；清理临时文件与旧版 BPMN 编辑器组件。

前端改进
1.菜单管理全面重构：基于全新的 StdAdvTree 高级树组件重写菜单管理页面，采用左右分栏（Splitpanes）布局，左侧为可搜索树形目录，右侧为动态详情面板。
2.全新的 StdAdvTree 高级树组件：新增支持模糊搜索（带防抖）、节点 CRUD 操作按钮、自定义插槽（label/append/actions/root-actions）、根节点虚拟行、初始化选中节点（initValue）、文本选择禁用等特性。
3.菜单面板状态管理统一：将 panelVisible、panelMode、panelForm、treeData、panelPermissionCodes 等核心状态全部迁移至 Pinia MenuManagerTreeStore，移除组件内零散的本地状态，实现 tree 与 panel 之间的数据共享。
4.权限码多选下拉：所需权限字段从文本输入改为多选下拉框（el-select + multiple + filterable + allow-create），对接后端 /permission/getPermissionDefinition 接口，展示已注册权限码列表并支持自由输入新权限码。
5.菜单面板交互优化：操作按钮（关闭/保存）从底部移至顶部标题栏右侧；编辑模式下显示骨架屏加载态；空状态引导页；隐藏菜单节点以删除线样式标记；面包屑导航展示父级路径。
6.父级选择树智能过滤：父级菜单选择器根据当前节点类型（目录/菜单/按钮）自动禁用不合法的父级选项，按钮节点不在树中显示。
7.用户管理增强：系统用户密码修改限制（置灰输入框）；用户状态以单选框切换封禁/启用。
8.自定义标签选择组件：新增支持 CTJ 格式双向绑定的标签选择器。
9.代码清理：移除旧版 BPMN 编辑器组件及临时文件。

增量业务功能
1.前端菜单管理大修与重构：全新的菜单管理界面，基于高级树组件的左树右表布局。
2.权限码多选：菜单/按钮配置支持多权限码选择，可搜索已注册权限码或自由创建新码，权限码缺失检测（完全缺失/部分缺失）在树节点中可视化标记。
3.用户安全加固：内置用户密码不可通过编辑接口修改，内置用户组编码不可变更，超级操作权限防解除保护。
4.用户状态管理：用户支持封禁/启用开关，AuthUserSession 基于 Switch 统一判断用户可用性。
5.CC一键启动脚本：AI 思考强度自定义：用户可在设置中配置 CLAUDE_CODE_EFFORT_LEVEL 以控制 AI 推理深度。

---

## 1.6W46-菜单权限

Version 1.6W CheckPoint 46

后端改进

1. 基于菜单的权限管理（1.6W 核心主题）：新增 auth_group_menu 表与 GroupMenuPo 实体，建立用户组与菜单的直接绑定关系，替代旧式用户授权管理。
2. 用户组管理重构：重构 GroupController，新增组详情查询、组删除、更新组菜单、更新组权限(GP)功能
3. 行级数据权限增强：新增 RowScopes 枚举及其 JPA 转换器，支持新的数据权限范围定义；新增 RowScopeUserOnlyPo 过滤器，用于仅过滤用户本人数据的表。
4. 新增 IdsDiff 类用于计算 ID 列表的新增/删除差异。
5. AuthUserDetailsService（AUDS）会话构建改进：登录/刷新时，系统分别加载 GP 直接权限码（pRepository）和 GM 菜单派生权限码（gmRepository），通过 PermissionBucket 统一合并为 SpringSecurity GrantedAuthority，实现 GM + GP 双路权限在会话层的最终汇聚。同时集成新的 RsCalculator 行级数据权限计算器，基于用户组完成 RS 范围预计算。
6. 查询接口扩展：新增根据用户组 ID 获取菜单、根据用户 ID 获取权限代码的查询接口，优化用户权限管理逻辑。
7. 系统组权限保护：增强系统组权限控制逻辑，确保超级操作权限(SA)在系统组中不可移除。
8. 组菜单缓存管理：组菜单更新时自动清理相关缓存并更新用户版本号。
9. 代码清理：完全移除旧式用户授权管理模块及冗余的组权限相关 DTO 与接口；JPA 转换器统一迁移至 AUTH 域下的 JPA 包。
10. 升级依赖：SpringBoot 从 4.0.4 升级至 4.0.6 GA。
11. 升级依赖：oshi-core 从 6.9.3 升级至 7.0.1。
12. 升级依赖：spring-boot-quartz 从 4.1.0-M1 升级至 4.1.0-RC1。
13. 升级依赖：tika-core 从 3.2.3 升级至 3.3.0。
14. 升级依赖：H2 Database 从 2.2.220 升级至 2.4.240。
15. 升级依赖：commons-fileupload2-jakarta-servlet6 从 2.0.0-M4 升级至 2.0.0-M5。
16. 升级依赖：Gson 从 2.11.0 升级至 2.14.0。

前端改进

1. 新增用户组管理界面：包含用户组查询、创建、编辑和批量删除功能，支持完整的用户组生命周期管理。
2. 新增组权限(GP)管理模态框：支持权限 ID 列表的选择与更新，包含高级权限操作入口，受超级管理员权限控制。
3. 新增组菜单管理模态框：支持菜单树的全选/取消全选、级联变更提示与警告提示，实现用户组与菜单的灵活绑定。
4. StdAdvTree(高级树) 组件增强：新增复选框功能，支持树节点的多选操作，并添加 CheckTreeDemo 演示页面。
5. 用户组详情视图优化：调整成员数量与菜单数量字段命名；优化权限总数显示和按钮权限控制；添加溢出提示并调整操作列宽度。
6. 修复：统一权限控制注解中数据源、输出方案、聚合模型、类型映射方案等的命名格式。
7. 修复：按钮组件允许在前端添加子项的问题。

增量业务功能

1. 双路权限管理架构（GM/GP）：用户组现在支持两条权限管理路径——
   GM（组菜单，Group Menu）：常规路径，将菜单树绑定到用户组，用户通过菜单间接获得权限。所有管理员均可操作。
   GP（组权限，Group Permission）：高级路径，直接为组分配或撤销特定权限码，实现精细控制。仅拥有超级操作权限(SA)的管理员可操作。
   两条路径互补：GM 负责粗粒度的菜单级授权，GP 负责细粒度的权限码级干预。实际使用中，常规管理员通过 GM 绑定菜单即可覆盖绝大多数场景；SA 管理员在需要例外授权或收回特定权限时，通过 GP 进行精确调整。
2. 用户组管理大修与重构：本次彻底移除了两套旧式实现——
   旧式 GP 管理：直接操作用户组权限码列表，所有权限变更都通过 GP 入口完成，缺少菜单维度的抽象，难以维护。
   旧式 GM 模拟管理：表面上提供菜单绑定的交互，但底层并不存储菜单与组的绑定关系。实际操作时，先将菜单内包含的权限码全部解包，再塞入 GP 的权限码列表中，本质上仍是 GP，菜单绑定关系在保存后即丢失。
   在新设计中，GM 拥有独立的绑定表，菜单与组的绑定关系被原生存储，不再通过 GP 间接模拟；GP 则回归其本职——仅用于 SA 管理员对特定权限码做例外增删。两套路径各司其职、数据独立，不再相互冒充。
3. 行级数据权限 RowScopes 增强：新增 RowScopeUserOnlyPo 过滤器，专门解决"表里只有用户 ID 列、没有租户/组织列"的场景——当数据表设计上只记录所属用户而不记录租户或组织时，常规过滤器会因找不到列而报错，该过滤器仅按用户 ID 过滤，避免缺列问题。
4. 系统组保护机制：系统组中的超级操作权限(SA)受保护，不可被移除或修改，防止误操作导致权限失控。

---

## 1.6X68-租户菜单包

Version 1.6X CheckPoint 68

后端改进
1.全项目权限码标准化（1.6X 核心主题）：将模糊的 view 拆分为 list/details，覆盖 Core/Auth/QT/QF/Audit 六大域共 55 处，消除权限二义性。
2.通用树构建器（TreeBuilder）：Core 域新增泛型建树工具，VO 实现 TreeNode 接口即可参与建树，内置 NPE 防御。Core 域菜单服务已完成整合。
3.菜单包（Pack）体系：Core 域新增菜单包管理模块，支持 CRUD、菜单绑定、租户绑定、编码唯一性校验、被使用时删除保护。新增根据菜单ID反查所属包、根据包ID获取绑定租户列表、MenuPack 删除关联等查询接口。
4.超级租户与内置租户：Core 域租户表新增 is_system 字段，内置租户禁止菜单包操作。新增超级租户检查——仅超级租户可新增和编辑菜单。Core 域新增 RowScopeRootOnlyPo 租户级数据权限过滤器。用户体系冷启动保存管理员用户ID。
5.租户管理增强：到期时间范围查询、用户总数统计。CoreRootService 接入 Switch 类替代硬编码。
6.CheatPermission 枚举：Auth 域新增超级权限枚举，统一定义 SA（::）、SR（:::）、PERSP（:::*:PS）三类超级权限码，替代维护服务中的硬编码。
7.透视权限：Auth 域新增 @Systemscope 注解、AOP 切面、Hibernate 过滤器。实体继承 SystemScopePo 自动受控——持有 PERSP/SR 时透视全部数据，否则屏蔽 is_system=true。UserPo 已接入。
8.Switch 状态类增强：JPA 无缝转换入库，支持在 DTO/VO 中直接使用。CoreRootService 已接入。
9.组织架构查询优化：Core 域组织仓库支持 parentId=NULL 查询，组织服务名称唯一性校验优化。
10.菜单服务优化：Core 域 GetMenuTreeDto 新增 grantable 字段支持菜单授予功能。菜单 DTO 增强按钮隐藏状态检查验证。菜单服务扁平化逻辑使用批量转换简化。控制器接口描述规范化。
11.用户组数据模型简化：Auth 域移除用户组 DTO 中冗余的权限ID列表字段，移除组服务中的菜单权限处理逻辑，优化状态检查条件。
12.注册表缓存：Core 域新增清除注册表缓存接口。
13.代码清理：移除 Auth 域权限/菜单/组控制器中不必要的权限注解，移除 Core 域废弃的 missingPermission 字段。PromptPo/PromptController 接入 RowScopeRootOnlyPo。

前端改进

1.菜单包管理：新增 PackManager 组件，支持 CRUD 全流程、备注多行输入、排序增强、状态选择优化。接口统一重命名为 Pack 前缀。
2.租户管理增强：到期时间范围选择、内置租户状态显示、管理员菜单包绑定管理（RootRpModal）。调整按钮样式与布局，优化表单结构，简化状态管理逻辑。
3.菜单面板优化：编辑模式下自动加载最新菜单数据，优化面板显示逻辑。
4.StdAdvTree 扩展：注册表管理界面从旧式树组件替换为 StdAdvTree 高级树组件。
5.注册表缓存：前端新增清除注册表缓存操作入口。
6.编码规范建设：新增 CLAUDE.md 入口文件统一引用 .cursor/rules 规则集。新增前端方法级 JSDoc 注释规范。
7.工具链：新增 claude-code 更新确认功能。

增量业务功能

1.权限码标准化：55 处注解从模糊 view 拆分为 list/details，支持列表与详情差异化授权。
2.菜单包体系(MP)：提供租户级菜单的组合与复用能力，核心链路：创建包 → 绑定菜单 → 绑定租户 → 用户获得菜单。
3.通用树构建器：消除各 Service 中重复的平面列表→树结构建树代码。
4.CheatPermission 与透视权限：引入 SA/SR/PERSP 三类超级权限码。PERSP（::::PS）为透视权限，持有者不受 is_system 过滤器限制可查看全部内置数据，普通管理员仅见业务数据。

---

## 1.6Y70-累积质量改进

Version 1.6Y CheckPoint 70

1. 租户管理角色（核心）：租户表新增管理角色字段，建租户时自动建管理组并发 PERSP 权限。
2. 三路权限汇聚：登录时 GM（组菜单）+ GP（组权限）+ MP（菜单包）在会话层合并。
3. 菜单树分支：SA 看全部、租管看菜单包、普通用户看组绑定菜单。
4. 权限缓存自动刷新：菜单包绑定或内容变更时刷新受影响用户。
5. 菜单校验：单菜单最多 10 个权限码，禁配 SA/SR/PERSP。
6. 菜单验证下沉：DTO 自校验，控制器不再处理。
7. 权限接口简化：移除前端可提交的"系统内置"字段。
8. 认证异常脱敏：登录内部错误统一返回"认证系统暂时不可用"。
9. 用户档案：支持强制刷新（清缓存+递增版本+清菜单），头像走缓存、上传后自动逐出。
10. 待办取消：联动删除 Flowable 任务和实例，发布 QfTaskCancelledEvent 供下游补偿。
11. 删除待办时自动清理关联 Flowable 任务，防止孤立数据。
12. 审批流转记录：已完成节点保留历史快照姓名，未完成节点用当前昵称。
13. 组织校验：部门类型节点下只允许放部门。
14. 内置用户改密保护移到加密之前，堵绕过漏洞。
15. SA 防解除收紧为同时匹配系统标识和组 ID；系统组禁止接口改菜单绑定。
16. 修复多实例会签/或签参与者表达式错误。

1. 个人中心：新增用户档案页，可查看/修改基本信息，头像支持裁剪上传，更新后即时生效。
2. 个人中心：多标签下信息变动自动同步，刷新加节流，避免重复请求。
3. 注销：返回结果对象并完善错误处理，注销流程更可靠。
4. 登录审计：登录记录支持点开看明细。
5. 流程表单设计：放置公共组件改为弹窗搜索选择，按名称分类并附使用注意，避免一次加载全部。
6. 流程审批：审批节点时间戳和状态标签更准；运行时遇到下线/未发布组件时给出明确提示。
7. 流程引擎：修复多实例会签/或签参与者表达式异常。
8. 通知公告：新增"查看"模式，可回看已发通知详情。
9. 通知公告：接收对象改用统一的组织/用户选择器，支持跨页选择和昵称搜索；接收人数标签调整。
10. 用户管理：部门选择改为组织树，支持搜索级联;批量变更逻辑优化；不再展示系统用户。
11. 用户组管理：组织选择改用新选择器；权限总数列动态显示；菜单管理按钮可用性增强。
12. 用户组管理：系统内置组 SA 权限保护，禁止误解除。
13. 组织管理：部门类型节点下禁止放置非部门；文案从"部门"统一为"组织"；树选择处理修复。
14. 菜单管理：修复面板可见时加载态显示问题；界面结构整体优化。
15. 权限管理：移除前端可提交的"系统内置"字段，由后端统一管理。
16. 会话管理：在线人数标签和按钮文案统一。
17. 顶栏公告下拉：样式和结构优化，视觉一致性提升。
18. 维护中心菜单：根据用户权限动态显示，未授权用户不再可见。
19. 路由管理：禁止直接调用 Vue Router，统一走 GenricRouteService 收口。
20. 基础能力：上线公共组织树和公共用户选择器，替换各页面分散的旧选择器，清理约 1150 行冗余代码。
21. 基础能力：高级树组件服务化重构，新增自定义过滤、自定义禁用判断、半选状态、勾选响应式同步。
22. 基础能力：引入多标签 Tab 服务，配合用户信息节流刷新等场景。
23. 依赖更新：新增 vue-cropper（头像裁剪）、@vueuse/core（响应式工具）。

1. 租户管理角色（1.6Y 核心主题）：租户表新增管理角色字段，创建租户时自动建立独立管理员组并分配 PERSP，替代原先一刀切的超级权限。租管身份动态判定，同一用户在不同租户下可拥有不同身份。
2. 三路权限汇聚（GM + GP + MP）：在 1.6W 双路基础上引入 MP（菜单包）。SA 看全部、租管走菜单包、普通用户走组绑定，三路在会话构建阶段合并。菜单包变更自动递增受影响用户版本并清缓存，无需重新登录。
3. 菜单超级权限分发防护：单菜单最多 10 个权限码，且禁止携带 SA/SR/PERSP，关闭"菜单包→菜单"间接分发超级权限的灰色路径。
4. DTO 自校验模式：菜单模块首次引入 DtoCustomValidator，DTO 持久化前自校验，控制器不再承担参数验证职责，为后续向其他域推广奠定基础。
5. 流程表单公共组件按需选择：设计器放置公共组件由"一次加载全部"改为"模态框搜索选择"，附使用注意提示；运行期遇下线/未发布组件给出明确提示。
6. 流程待办取消：待办新增取消动作，联动删除 Flowable 任务和实例，发布 QfTaskCancelledEvent 供下游补偿；删除待办前清理关联任务，杜绝孤立数据。
7. 用户档案与头像：上线统一个人中心，支持档案编辑、头像裁剪上传与版本号机制；多标签刷新加节流；后端提供强制刷新（逐出缓存+版本递增+菜单清理）。
8. 前端公共选择器：ModalOrgTree、ModalUserSelector 落地并被各管理页统一接入，替换分散选择器，删除冗余约 1150 行，交互一致化。
9. 高级树服务化：StdAdvTree 组件状态/节点/勾选逻辑抽离至 StdAdvTreeService，新增自定义过滤、自定义启用判断、半选支持，可维护性显著提升。
10. 路由统一收口：禁止业务代码直接操作 Vue Router，统一走 GenricRouteService，配合维护中心菜单按权限动态显隐。

---

## 1.6Z57-标准化

Version 1.6Z CheckPoint 57

后端改进
1. 事件驱动架构：新增 BizEvent 抽象基类，统一业务事件模型。新增租户创建/删除事件（RootCreateEvent/RootRemoveEvent）及用户创建/删除事件（UserCreateEvent/UserRemoveEvent），集成至 CoreRootService 与 UserService 发布。
2. 任务分配事件：新增 QfTaskAssignedEvent 及 QfTaskAssignedListener，更新 QfFlowableConfig 以支持任务重分配逻辑，重构 QfTodoService 待办处理流程。
3. 菜单外链支持：MenuPo 菜单项类型新增外链嵌套（EXTLINK_NESTED）和外链跳转（EXTLINK_JUMP），MenuService 增加外链类型验证逻辑，同步更新 AddMenuDto/EditMenuDto/GetMenuTreeDto/Vo 全链路 DTO。
4. 组织接口增强：新增获取组织机构树列表接口（OrgController.getOrgTreeList），支持按参数返回组织信息。优化 OrgRepository 查询逻辑。
5. 注册表字典转换：新增获取注册表信息接口。ImportRegistryDto/ExportRegistryVo 新增 RegistryDict/RegistryDictConverter，支持数据类型和状态的字典转换，Excel 导出可使用 REG 注册表。
6. 附件图片导出：新增 AttachImageWriteHandler 与 AttachImagesConverter，支持附件图片在 Excel 导出中的转换处理。
7. 用户/用户组增强：GetUserListDto 增加用户 ID 集合字段；UserGroupRepository 新增批量查询用户组下用户 ID 方法；修复用户组查询排序问题。
8. 数据权限 FULL 模式增强：新增 @RequireOrgForFull 注解及 RequireOrgForFullAspect 切面，FULL 数据权限模式下强制用户必须有部门或公司信息，防止越权。
9. 日期格式增强：JacksonLdtInputConv 支持仅日期格式，允许前端传入不带时分秒的日期字符串。
10. 异常处理优化：新增数据完整性约束违反异常处理，提供用户友好的错误信息；错误代码生成逻辑新增时间戳格式。
11. 工具类：新增 IconResolver 类，用于解析 Iconify JSON 文件并输出图标名为纯文本格式。

前端改进
1. PG 域（核心）：新增 Playground 内部组件演示平台，含 18 个演示页（PgStdAdvTree/PgStdListLayout/PgStdListContainer/PgStdListAreaQuery/PgStdListAreaAction/PgStdListAreaTable/PgStdTableCheckColumn/PgInputOrgTree/PgInputUserSelector/PgStdIframe/PgStdPgLayout/PgStdQueryCollapse/PgStdTimeRange/PgStdDateRange/PgStdIconPicker/PgStdExpandButton/PgStdCustomizeTagSelect 等），统一路由注册至 PlayGroundRouteRegister。
2. 组织/用户选择器标准化：新增 InputOrgTree（多选）、InputOrgTreeSingle（单选）、InputUserSelector 三个带输入框的选择器组件，配套 InputOrgTreeService/InputUserSelectorService。重构 ModalOrgTree/ModalUserSelector/OrgTree，增加排除节点、自定义禁用、裁剪根组织等能力。
3. Std 组件系列完善：StdAdvTree 新增搜索防抖、只读模式、排除节点（excludeNks/excludeNodeMethod）；StdListAreaTable 新增 v-model:list-form + :list-total + :load-list 内置分页模式；新增 StdTableCheckColumn 表格勾选列组件（全选/半选/行勾选）；新增 StdIframe 外部 iframe 组件；新增 StdPgLayout 演示页布局组件（演示/props/emits 三标签）；新增 StdQueryCollapse 查询折叠组件。
4. 布局系统重构（CLPS）：新增 ComLayoutProviderService 公共布局服务，支持多布局模式注册与动态切换。新增 ComLayoutProvider 组件，根据路由 meta.layout 动态渲染布局。AdminRoot 简化，布局逻辑全部迁移至 CLPS。
5. 面包屑与导航：新增 ComBreadcrumb 组件，支持基于后端菜单数据的动态面包屑导航，在 ComFramework 中替换旧式 StdBreadcrumb。
6. 路由与标签服务增强：GenricRouteService（GRS）新增路由冲突检测机制（GrConflictOverlay），固定路由和标签页功能。ComTabService（CTS）标签页服务重构优化。ComMenuService（CMS）支持外链嵌套与外链打开。
7. 列表页全域标准化翻新（核心主题）：以下页面全部从 StdListLayout 翻新为 StdListContainer + StdListAreaQuery + StdListAreaAction + StdListAreaTable 四件套标准布局，删除 #pagination 自定义分页，改用内置分页——
   Core 域：OrgManager（组织管理）、NoticeManager（通知公告）、CoreRoot（租户管理）、ExcelTemplateManager（Excel模板）、RegistryManager（注册表）、Post（岗位）、PackManager（菜单包）、UserManager（用户管理）、NoticeTemplate（通知模板）、MenuManager（菜单管理）
   Auth 域：PermissionManager（权限管理）、GroupManager（用户组管理）、SessionManager（会话管理）
   Audit 域：AuditErrorRcd（错误审计）、AuditLoginRcd（登录审计）
   QF 域：QfBizForm/QfCc/QfModel/QfModelDeployRcd/QfModelGroup/QfTodo
   QT 域：QtTask/QtTaskGroup/QtTaskRcd
8. QF 域组件翻新：QfApprove/QfApproveModal 优化状态显示、输入框自适应、新增返回按钮。QFD 设计器 QfdPanelMultiInstance 多实例面板及 Service 重构。
9. 按钮与交互统一：全域按钮样式统一为主色调（primary），输入框统一设置自适应高度（autosize），表格空值统一显示为"-"，序号列、溢出提示、查询持久化指示器等细节统一。
10. 项目配置：Node 版本要求锁定，依赖版本规范调整。

增量业务功能
1. 事件驱动架构：租户/用户/任务的生命周期事件统一发布，支持下游模块解耦订阅与补偿处理。
2. 外链菜单：菜单系统支持外链嵌套（嵌入 iframe）和外链跳转（新窗口打开）两种类型，配合 StdIframe 组件实现站内嵌套外部页面。
3. 公共组件库演示平台（PG 域）：20+ Std/Com 组件均具备独立演示页，含 Props/Emits/Slots 文档，支持实时交互验证。
4. 布局多模式：CLPS 支持多套布局注册与运行时切换，路由级 layout 配置，固定路由/标签页独立布局。
5. 列表页标准化收口：全域 20+ 列表页完成标准化翻新，统一四件套布局、内置分页、统一交互模式，删除约 1500 行冗余分页代码。
6. Excel 导出注册表增强：导出字段支持 REG 注册表字典转换，附件图片可嵌入导出。

规范建设
1. 全新 AI 相容性规则与 SKILL 体系：CLAUDE.md 重构为统一入口，引用 .cursor/rules 下全部规范。新增 21 个 SKILL 接入文档（组织选择器/用户选择器/布局管理器/StdListContainer/StdListAreaQuery/StdListAreaAction/StdListAreaTable/列表页翻新/Playground 演示等），覆盖前端核心组件全场景。
2. 后端规范：Controller/Dto/Vo/Service/Repository 五层规范更新，统一代码风格与校验边界。
3. 前端规范：Api/Service/VueSfc 三层规范更新，禁用相对路径导入，统一函数命名，全局 ESLint/TS 类型检查。
4. 通用规范：Global.mdc 更新，术语表（Glossary.mdc）和骨架模板（RuleSkeleton.mdc/SkillSkeleton.mdc）完善。
5. Claude Code 脚本移至项目顶层，放开仅前端工作区限制。
