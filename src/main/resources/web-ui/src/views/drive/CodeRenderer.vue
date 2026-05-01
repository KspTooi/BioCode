<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { DocumentCopy } from "@element-plus/icons-vue";
import axios from "axios";
import hljs from "highlight.js";
// 引入样式，你可以选择其他主题，如 'github', 'vs2015' 等
import "highlight.js/styles/atom-one-dark.css";
import Http from "@/commons/Http";

const route = useRoute();

// 状态变量
const codeContent = ref("");
const loading = ref(false);
const rawHtml = ref("");
const detectedLanguage = ref("");

// 从路由获取参数
const fileName = computed(() => (route.query.name as string) || "unknown");
const sign = computed(() => route.query.sign as string);

// 计算下载/预览链接
const fileUrl = computed(() => {
  if (!sign.value) {
    return "";
  }
  return Http.resolve(`/drive/object/access/downloadEntry?sign=${sign.value}&preview=1`);
});

// 扩展名到语言的简单映射（Highlight.js 通常能自动检测，但手动指定更准确）
const getLanguageByExt = (filename: string): string => {
  const ext = filename.split(".").pop()?.toLowerCase();
  const map: Record<string, string> = {
    js: "javascript",
    ts: "typescript",
    vue: "xml", // vue 文件通常包含 html/js/css，xml 或 html 模式通常效果不错
    html: "xml",
    css: "css",
    scss: "scss",
    json: "json",
    java: "java",
    py: "python",
    go: "go",
    c: "c",
    cpp: "cpp",
    cs: "csharp",
    php: "php",
    sql: "sql",
    md: "markdown",
    sh: "bash",
    yml: "yaml",
    xml: "xml",
  };
  return ext ? map[ext] || "" : "";
};

/**
 * 加载代码内容
 */
const loadCode = async (): Promise<void> => {
  if (!fileUrl.value) {
    return;
  }

  loading.value = true;
  codeContent.value = "";
  rawHtml.value = "";

  try {
    // 请求文件内容，指定响应类型为 text
    const response = await axios.get(fileUrl.value, {
      responseType: "text",
      // 防止 axios 自动转换 JSON
      transformResponse: [(data) => data],
    });

    codeContent.value = response.data;
    renderCode();
  } catch (error: any) {
    console.error("加载代码失败:", error);
    ElMessage.error("代码文件加载失败，可能文件过大或网络异常");
  } finally {
    loading.value = false;
  }
};

/**
 * 渲染并高亮代码
 */
const renderCode = (): void => {
  if (!codeContent.value) {
    return;
  }

  const lang = getLanguageByExt(fileName.value);
  let result;

  if (lang && hljs.getLanguage(lang)) {
    result = hljs.highlight(codeContent.value, { language: lang });
    detectedLanguage.value = lang;
  }
  if (!lang || !hljs.getLanguage(lang)) {
    result = hljs.highlightAuto(codeContent.value);
    detectedLanguage.value = result.language || "text";
  }

  // 生成带有行号的 HTML
  rawHtml.value = generateLineNumbers(result.value);
};

/**
 * 为代码添加行号结构
 */
const generateLineNumbers = (html: string): string => {
  const lines = html.split(/\r\n|\r|\n/);
  // 最后一行如果是空行，通常编辑器不显示行号，但 split 会多出一个空串
  if (lines[lines.length - 1] === "") {
    lines.pop();
  }

  // 使用 table 布局或 flex 布局来展示行号和代码
  // 这里使用简单的 ordered list (ol) 方式，或者手动构建行结构
  // 为了更好的复制体验，通常将行号用 CSS 伪元素生成，或者使用不可选中的元素

  return lines.map((line) => `<div class="code-line">${line || " "}</div>`).join("");
};

/**
 * 复制代码到剪贴板
 */
const onCopy = async (): Promise<void> => {
  if (!codeContent.value) {
    return;
  }
  try {
    await navigator.clipboard.writeText(codeContent.value);
    ElMessage.success("代码已复制到剪贴板");
  } catch {
    ElMessage.error("复制失败");
  }
};

onMounted(() => {
  loadCode();
});

watch(
  () => fileUrl.value,
  () => {
    loadCode();
  }
);
</script>

<template>
  <div class="code-preview-container">
    <div class="toolbar">
      <div class="file-info">
        <span class="file-name">{{ fileName }}</span>
        <span v-if="detectedLanguage" class="file-lang">({{ detectedLanguage }})</span>
      </div>
      <div class="actions">
        <el-button link type="primary" @click="onCopy">
          <el-icon><DocumentCopy /></el-icon> 复制
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="code-wrapper">
      <pre v-if="rawHtml" class="hljs code-block"><code v-html="rawHtml"></code></pre>
      <div v-else-if="!loading" class="empty-tip">暂无内容</div>
    </div>
  </div>
</template>

<style scoped>
.code-preview-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  background-color: #282c34; /* Atom One Dark 背景色 */
  overflow: hidden;
}

.toolbar {
  height: 40px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background-color: #21252b;
  border-bottom: 1px solid #181a1f;
  color: #abb2bf;
}

.file-name {
  font-weight: 600;
  font-size: 14px;
  margin-right: 8px;
}

.file-lang {
  font-size: 12px;
  color: #5c6370;
}

.code-wrapper {
  flex: 1;
  overflow: auto;
  position: relative;
  /* 滚动条样式优化 */
  scrollbar-width: thin;
  scrollbar-color: #4b5263 #282c34;
}

.code-wrapper::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.code-wrapper::-webkit-scrollbar-track {
  background: #282c34;
}

.code-wrapper::-webkit-scrollbar-thumb {
  background: #4b5263;
  border-radius: 5px;
}

.code-block {
  margin: 0;
  padding: 16px;
  font-family: "Fira Code", "Consolas", "Monaco", "Courier New", monospace;
  font-size: 14px;
  line-height: 1.5;
  background: transparent !important; /* 让 highlight.js 背景透明，使用容器背景 */
  counter-reset: line-number;
}

/* 行号样式实现 */
:deep(.code-line) {
  position: relative;
  padding-left: 3.5em; /* 为行号留出空间 */
  min-height: 1.5em; /* 确保空行也有高度 */
}

:deep(.code-line::before) {
  counter-increment: line-number;
  content: counter(line-number);
  position: absolute;
  left: 0;
  top: 0;
  width: 3em;
  text-align: right;
  color: #5c6370;
  user-select: none; /* 防止复制时选中行号 */
  border-right: 1px solid #3e4451;
  padding-right: 0.5em;
}

.empty-tip {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #5c6370;
}
</style>
