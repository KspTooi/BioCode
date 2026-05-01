/// <reference lib="webworker" />
import { createSHA256 } from "hash-wasm";
import type { IHasher } from "hash-wasm/dist/lib/WASMInterface";

// 50MB 分片：平衡内存占用与调用开销的最佳甜点
const CHUNK_SIZE = 50 * 1024 * 1024;

interface HashMessage {
  type: "start" | "chunk" | "end";
  file?: File;
}

let hasher: IHasher | null = null;

self.onmessage = async (e: MessageEvent<HashMessage>) => {
  const { type, file } = e.data;

  if (type === "start" && file) {
    try {
      // 懒加载单例模式：只编译一次 WASM
      if (!hasher) {
        hasher = await createSHA256();
      }
      hasher.init(); // 每次计算新文件必须重置

      const fileSize = file.size;
      const totalChunks = Math.ceil(fileSize / CHUNK_SIZE);
      let lastReportedProgress = 0;

      // 使用同步读取器 (Worker 专属神器)
      // 这行代码现在不会报错了，因为顶部的 <reference /> 引入了 Worker 类型定义
      const reader = new FileReaderSync();

      for (let i = 0; i < totalChunks; i++) {
        const start = i * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, fileSize);

        // 切片（仅是指针操作，不耗时）
        const blob = file.slice(start, end);

        // 同步读取：阻塞线程直到数据读入内存。
        // 在 Worker 中阻塞是好对，因为这意味着 CPU 在全力跑 I/O，没有异步调度的空隙。
        const buffer = reader.readAsArrayBuffer(blob);

        // 传入 Uint8Array 视图，避免拷贝
        hasher.update(new Uint8Array(buffer));

        // 进度汇报：减少通信频率
        const progress = Math.round(((i + 1) / totalChunks) * 100);
        if (progress - lastReportedProgress >= 2 || progress === 100) {
          lastReportedProgress = progress;
          self.postMessage({ type: "progress", progress });
        }
      }

      const hash = hasher.digest("hex");
      self.postMessage({ type: "completed", hash });
    } catch (error: any) {
      self.postMessage({ type: "error", error: error.message || "计算哈希失败" });
    }
  }
};
