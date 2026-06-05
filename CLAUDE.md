# 项目编码规范（Claude Code 入口）

> Claude Code 不读 `.cursor/rules/*.mdc`，本文件作为统一入口，把 Cursor Rules 复用过来。
> 修改规范请只改 `.cursor/rules/*.mdc`，本文件仅做引用，避免双份维护。

## 元规范

@.cursor/rules/_meta/RuleSkeleton.mdc
@.cursor/rules/_meta/Glossary.mdc
@.cursor/skills/_meta/SkillSkeleton.mdc

## 全局通用规则

@.cursor/rules/Global.mdc

1. 始终使用中文回复
2. 代码中短路优先：`if-return` / `if-continue` / `if-throw`
3. 禁止使用 `switch` 与 `else`
4. Java 字符串判空统一使用 ApacheCommonsLang3 的 `StringUtils`
5. 不使用全类名，统一 `import` + 简单类名
6. 不要执行 `mvn` 测试或编译
7. 直接按要求编写代码或操作文件，不要有多余的解释

## 后端 Service 规范（**/service/*Service.java）

@.cursor/rules/web-service/module/WebServiceServiceMR.mdc

@.cursor/rules/web-service/module/WebServiceDtoMR.mdc

## 后端 Vo 规范（**/vo/*Vo.java）

@.cursor/rules/web-service/module/WebServiceVoMR.mdc

## 后端 Controller 规范（**/controller/*Controller.java）

@.cursor/rules/web-service/module/WebServiceControllerMR.mdc

## 后端 Service 规范（**/service/*Service.java）

@.cursor/rules/web-service/module/WebServiceServiceMR.mdc

## 后端 Repository 规范（**/repository/*Repository.java）

@.cursor/rules/web-service/module/WebServiceRepositoryMR.mdc

## 后端方法尺度与控制流规范（**/*.java）

@.cursor/rules/web-service/module/WebServiceMethodMR.mdc

## 前端通用规范（*.ts / *.js / *.vue）

@.cursor/rules/web-ui/WebUI.mdc

## 前端 Api 层规范（src/main/resources/web-ui/src/views/**/api/*Api.ts）

@.cursor/rules/web-ui/WebUiApi.mdc

## 前端 Service 层规范（src/main/resources/web-ui/src/views/**/service/*Service.ts）

@.cursor/rules/web-ui/WebUIService.mdc

## 前端 SFC↔Service 边界规范（*.vue 与 service/*Service.ts）

@.cursor/rules/web-ui/WebUiSfcServiceCR.mdc

## 选择器组件 Skill

@.cursor/skills/web-ui/org-selector/SKILL.md
@.cursor/skills/web-ui/user-selector/SKILL.md

## 组件 Props 定义 Skill

@.cursor/skills/web-ui/component-props/SKILL.md

## SOA 通用组件 Skill

@.cursor/skills/web-ui/com-seq-fixer/SKILL.md

## 布局管理器 Skill

@.cursor/skills/web-ui/layout-provider/SKILL.md

## 列表页布局 Skill

@.cursor/skills/web-ui/std-list-container/SKILL.md
@.cursor/skills/web-ui/std-list-area-query/SKILL.md
@.cursor/skills/web-ui/std-list-area-action/SKILL.md
@.cursor/skills/web-ui/std-list-area-table/SKILL.md
@.cursor/skills/web-ui/std-list-page-refactor/SKILL.md

## Playground 演示 Skill

@.cursor/skills/web-ui/playground-demo/SKILL.md

## 更新日志生成 Skill

@.cursor/skills/changelog/SKILL.md
