# 项目编码规范（Claude Code 入口）

> Claude Code 不读 `.cursor/rules/*.mdc`，本文件作为统一入口，把 Cursor Rules 复用过来。
> 修改规范请只改 `.cursor/rules/*.mdc`，本文件仅做引用，避免双份维护。

## 全局通用规则

1. 始终使用中文回复
2. 代码中短路优先：`if-return` / `if-continue` / `if-throw`
3. 禁止使用 `switch` 与 `else`
4. Java 字符串判空统一使用 ApacheCommonsLang3 的 `StringUtils`
5. 不使用全类名，统一 `import` + 简单类名
6. 不要执行 `mvn` 测试或编译
7. 直接按要求编写代码或操作文件，不要有多余的解释

## 后端 Java 规范

@.cursor/rules/backend.mdc

## 前端通用规范（*.ts / *.js / *.vue）

@.cursor/rules/frontend.mdc

## 前端 views 模块规范（src/main/resources/web-ui/src/views/**）

> 这是前端最重要的硬性规范，涉及 Api / Service / SFC / Route / 类型 / 命名 / 组件位置等。
> 修改 `views/**` 下任意文件前必读。

@.cursor/rules/frontend-views.mdc
