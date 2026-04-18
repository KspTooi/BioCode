import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";

/**
 * ESLint 配置 — Vue + TypeScript + JavaScript 语法与风格检查
 *
 * 源码质量与风格优化思路
 *
 * 许多开发者在从初级向中级进阶的过程中，会经历一个"炫技期"——刚刚掌握了高阶函数、解构、
 * 展开运算符等特性，自然想把它们用到极致。一行代码里嵌套三层三元、串两次 map、再来一个深层
 * 展开，写完的瞬间确实会有一种"我进步了"的成就感。这种探索欲本身没有错，它恰恰说明你在
 * 快速成长。但真正的进阶，不是"能写出多复杂的代码"，而是"能让多少人轻松读懂你的代码"。
 *
 * 几乎所有资深工程师都走过同一条路：先追求精巧，再回归朴素。当你开始负责线上排障、做 Code
 * Review、或者接手半年前别人写的模块时，就会切身体会到————代码的读者远比作者多，代码被阅读
 * 的次数远比被编写的次数多。写代码时多花的那几秒钟"展开写清楚"，能为未来每一次阅读节省
 * 数分钟的理解成本。这就是这套规则存在的意义：帮助你更快地完成从"能写"到"写得好"的跨越。
 *
 * 关于对象扩展运算符（...）：
 * { ...userInput, ...defaultConfig } 这样的写法虽然简洁，但会将来源对象的所有属性隐式地
 * 合并到目标中。如果来源对象意外携带了多余字段（如 isAdmin: true），排查由此产生的覆盖问题
 * 成本较高。显式逐一赋值虽然代码更长，但每个字段的来源和去向都一目了然。
 * 此规则为警告级别，在必要场景下允许使用，但请优先考虑显式赋值。
 *
 * 关于 AI 协作编程：
 * 在 AI 辅助编程日益普及的今天，显式代码风格还带来了一个额外的重要优势——它对 AI 极其友好。
 * AI 模型（无论是代码补全、Code Review 还是自动重构）的工作方式本质上也是"阅读理解"：
 * 它需要从上下文中推断意图，然后生成或修改代码。当代码是显式的、扁平的、每个变量和字段都
 * 有明确来源时，AI 能更准确地理解你的意图，给出更可靠的建议。反之，如果代码中充满了隐式
 * 展开、深层嵌套和压缩写法，AI 同样会"读不懂"，输出质量会明显下降。
 * 换句话说，对人友好的代码，对 AI 同样友好；为团队写的清晰代码，也是在为你的 AI 助手写
 * 清晰的提示词。这套规范在无形中让你的整个代码库都成为高质量的 AI 上下文。
 *
 * 总体原则：
 * 稍长一些的显式代码，阅读时只是多滚动几行；而高度压缩的隐式写法，理解时消耗的是认知资源和凌晨三点的睡眠质量。
 * 这套规范假设每一位代码读者（包括未来的自己和 AI 工具）都希望在最短时间内看懂逻辑，
 * 因此优先选择清晰、可预测的写法，而非追求极致的精简。
 */
export default defineConfigWithVueTs(
  // JS 基础规则集，覆盖常见的语法与逻辑错误
  js.configs.recommended,

  // Vue + TypeScript 推荐规则集
  vueTsConfigs.recommended,

  // Vue 单文件组件推荐规则集
  ...pluginVue.configs["flat/recommended"],

  {
    rules: {
      //关闭Vue自带的一些规则(因为这与prettier的格式化冲突)
      "vue/html-closing-bracket-newline": "off",
      "vue/singleline-html-element-content-newline": "off",
      "vue/max-attributes-per-line": "off",
      "vue/html-self-closing": "off",
      "vue/html-indent": "off",

      //允许使用 console 因为 console 在开发时非常有用，可通过工具在编译时统一删除控制台打印日志。
      "no-console": "off",

      //允许使用 any 类型
      "@typescript-eslint/no-explicit-any": "off",

      //允许使用未使用的变量
      "@typescript-eslint/no-unused-vars": "warn",

      //允许使用多单词组件名
      "vue/multi-word-component-names": "off",

      //Vue SFC：<script> 超过 200 行时应将业务逻辑拆分到 *Service.ts 等模块，保持组件以视图编排为主
      "vue/max-lines-per-block": [
        "error",
        {
          script: 300,
          skipBlankLines: true,
        },
      ],

      // 允许使用常量条件 因为常量条件在编译时已经确定，不会影响性能
      "no-constant-condition": "off",

      // 函数参数不超过 16 个，超出时建议用 VO/DTO/PO 封装，以提高可读性和可维护性
      "max-params": ["error", 16],

      // 禁止嵌套三元表达式。嵌套三元需要读者在脑中维护多层条件栈，可读性极差，请改用 if-return 卫语句
      "no-nested-ternary": "error",

      // 禁止非空断言(!)。非空断言绕过了 TypeScript 的空值检查，等于人为制造了类型安全盲区，请使用显式的空值判断
      "@typescript-eslint/no-non-null-assertion": "error",

      // 强制使用大括号。参考 2014 年 Apple "goto fail" 漏洞（CVE-2014-1266）：省略大括号导致关键语句脱离条件控制，引发严重安全事故。始终加上大括号可从根本上杜绝此类风险
      curly: ["error", "all"],

      // 要求显式声明函数返回类型，使函数契约一目了然，便于调用方快速理解接口
      "@typescript-eslint/explicit-function-return-type": [
        "error",
        {
          // 允许箭头函数表达式省略返回类型（如 map/filter 回调），因为此类场景下类型推导已足够清晰
          allowExpressions: true,

          // 允许 useXxx 命名的 Composable/Store 服务函数省略返回类型。此类函数的返回值通常包含大量 Ref<T> 与方法，手动维护返回类型的成本高于收益，且 TypeScript 推导已足够准确
          allowedNames: ["^use[A-Z].*", "^use[a-z].*"],
        },
      ],

      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["./*", "../*"],
              message:
                "请使用路径别名（如 @/）代替相对路径导入。相对路径在文件移动或目录重构时极易断裂，而绝对别名使模块定位更稳定、更直观。请在 tsconfig 和打包工具中统一配置。",
            },
          ],
        },
      ],

      "no-restricted-syntax": [
        "error",
        {
          selector: "IfStatement > IfStatement.alternate",
          message:
            "请避免 else-if 嵌套。嵌套的条件分支会使控制流变得复杂且难以追踪。建议使用卫语句（Guard Clauses）：不满足前置条件时提前 return，使主逻辑保持在最浅层级。",
        },
        {
          selector: "IfStatement[alternate!=null]",
          message:
            "请使用卫语句（Guard Clauses）替代 else 分支。对不满足条件的情况提前 return/continue/throw，使函数的正常路径保持扁平，减少缩进层级，提升可读性。",
        },
        {
          selector: "TSTypeReference[typeName.name=/^(Pick|Omit|Partial|Exclude)$/]",
          message:
            "请避免使用 Pick/Omit/Partial/Exclude 等工具类型进行类型拼接。直接定义一个语义清晰的独立类型，可读性和可维护性更好，在该项目中代码生成器会为每一个接口都生成对应的Dto、Vo等类型，请直接使用这些类型，不需要使用类型拼接与合并。前后端数据结构的脱节是绝大多数联调 Bug 的温床，请务必避免。",
        },
        {
          selector: "SwitchStatement",
          message:
            "请勿使用 switch 语句。switch 存在忘写 break 导致穿透执行的风险，且各 case 共享作用域容易引发变量冲突。建议使用对象字面量映射（Object Map）或策略模式替代。",
        },
        {
          selector: "CallExpression[callee.property.name=/^(reduce|reduceRight)$/]",
          message:
            "请避免使用 reduce/reduceRight。累加器的中间状态隐式传递，在复杂场景下可读性和可调试性较差。建议使用 for...of 循环显式处理，逻辑更清晰，断点调试也更方便。",
        },
        /* {
          selector:
            "CallExpression[callee.property.name=/^(map|filter)$/] > MemberExpression.callee > CallExpression[callee.property.name=/^(map|filter)$/]",
          message:
            "请避免 map/filter 链式调用。每次链式调用都会创建中间数组，多次链式会产生不必要的内存开销。如有两个以上的操作，建议合并到一个 for 循环中，将复杂度从 O(2n) 降至 O(n)。",
        }, */
      ],
    },
  },

  // 忽略目录
  {
    ignores: ["dist/**", "node_modules/**", "*.d.ts", "**/public/**/*.ts", "**/public/**/*.js"],
  }
);
