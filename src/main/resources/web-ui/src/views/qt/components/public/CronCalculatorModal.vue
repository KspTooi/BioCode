<template>
  <el-dialog v-model="visible" title="Cron 表达式生成器" width="650px" destroy-on-close append-to-body>
    <div class="cron-calculator">
      <el-form label-width="100px">
        <!-- 第一级：大模式选择 -->
        <el-form-item label="配置模式">
          <el-radio-group v-model="mainMode" class="main-mode-group">
            <el-radio-button label="interval">频率模式 (间隔)</el-radio-button>
            <el-radio-button label="scheduled">定时模式 (固定)</el-radio-button>
            <el-radio-button label="custom">高级自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-divider border-style="dashed" />

        <!-- 频率模式配置 -->
        <template v-if="StringUtils.equals(mainMode, 'interval')">
          <el-form-item label="执行频率">
            <div class="flex items-center">
              <span class="mr-2">每隔</span>
              <el-input-number v-model="config.intervalValue" :min="1" controls-position="right" style="width: 120px" />
              <el-select v-model="config.intervalUnit" class="mx-2" style="width: 100px">
                <el-option label="秒" value="second" />
                <el-option label="分钟" value="minute" />
                <el-option label="小时" value="hour" />
              </el-select>
              <span>执行一次</span>
            </div>
            <div class="text-xs text-gray-400 mt-2">注：频率模式从整点/整分开始计算间隔</div>
          </el-form-item>
        </template>

        <!-- 定时模式配置 -->
        <template v-if="StringUtils.equals(mainMode, 'scheduled')">
          <el-form-item label="周期类型">
            <el-radio-group v-model="subMode" size="small">
              <el-radio label="daily">每天</el-radio>
              <el-radio label="weekly">每周</el-radio>
              <el-radio label="monthly">每月</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 每周特定日 -->
          <el-form-item v-if="StringUtils.equals(subMode, 'weekly')" label="执行日">
            <el-checkbox-group v-model="config.weekDays">
              <el-checkbox :label="1">周一</el-checkbox>
              <el-checkbox :label="2">周二</el-checkbox>
              <el-checkbox :label="3">周三</el-checkbox>
              <el-checkbox :label="4">周四</el-checkbox>
              <el-checkbox :label="5">周五</el-checkbox>
              <el-checkbox :label="6">周六</el-checkbox>
              <el-checkbox :label="0">周日</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <!-- 每月特定日 -->
          <el-form-item v-if="StringUtils.equals(subMode, 'monthly')" label="执行日">
            <div class="flex items-center">
              <span>每月第</span>
              <el-input-number
                v-model="config.day"
                :min="1"
                :max="31"
                controls-position="right"
                class="mx-2"
                style="width: 100px"
              />
              <span>天</span>
            </div>
          </el-form-item>

          <el-form-item label="执行时间">
            <el-time-picker v-model="config.time" format="HH:mm:ss" value-format="HH:mm:ss" placeholder="选择具体时间点" />
          </el-form-item>
        </template>

        <!-- 自定义模式 -->
        <template v-if="StringUtils.equals(mainMode, 'custom')">
          <el-form-item label="表达式">
            <el-input v-model="customCron" placeholder="秒 分 时 日 月 周 (Quartz 格式)" />
          </el-form-item>
        </template>

        <el-divider />

        <!-- 预览区域 -->
        <div class="preview-section bg-gray-50 p-4 rounded border border-gray-100">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center">
              <el-tag type="success" effect="dark" class="mr-2">{{ finalCron }}</el-tag>
              <span class="text-gray-500 text-sm">生成的表达式</span>
            </div>
          </div>

          <div class="description-box flex items-start mb-4">
            <el-icon class="mt-1 mr-2 text-blue-500"><InfoFilled /></el-icon>
            <div>
              <div class="text-blue-600 font-medium">语义化描述：</div>
              <div class="text-gray-600 text-sm mt-1">{{ cronDescription }}</div>
            </div>
          </div>

          <div class="next-runs" style="min-height: 140px">
            <div class="text-xs text-gray-400 mb-2 font-bold uppercase tracking-wider">未来 6 次执行预览：</div>
            <div class="grid grid-cols-1 gap-1">
              <div v-for="(run, index) in nextRunTimes" :key="index" class="text-sm flex items-center">
                <span class="w-6 text-gray-300">{{ index + 1 }}.</span>
                <span>{{ run }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="onConfirm">应用表达式</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue";
import cronstrue from "cronstrue/i18n";
import { CronExpression, CronExpressionParser } from "cron-parser";
import { InfoFilled } from "@element-plus/icons-vue";

/**
 * 模拟 Apache Commons Lang 的 StringUtils
 */
const StringUtils = {
  equals: (s1: string, s2: string) => s1 === s2,
  isNotBlank: (s: string) => s !== null && s !== undefined && s.trim().length > 0,
  contains: (s: string, sub: string) => s && s.indexOf(sub) !== -1,
};

const emit = defineEmits<{
  (e: "onConfirm", cron: string | null): void;
}>();

const visible = ref(false);
const mainMode = ref("interval"); // interval, scheduled, custom
const subMode = ref("daily"); // daily, weekly, monthly
const customCron = ref("0 0 0 * * ?");

const config = reactive({
  intervalValue: 5,
  intervalUnit: "second", // second, minute, hour
  time: "00:00:00",
  weekDays: [1],
  day: 1,
});

// 计算最终的 Cron 表达式
const finalCron = computed(() => {
  if (StringUtils.equals(mainMode.value, "custom")) {
    return customCron.value;
  }

  // 频率模式逻辑
  if (StringUtils.equals(mainMode.value, "interval")) {
    const val = config.intervalValue;
    if (StringUtils.equals(config.intervalUnit, "second")) {
      return `0/${val} * * * * ?`;
    }
    if (StringUtils.equals(config.intervalUnit, "minute")) {
      return `0 0/${val} * * * ?`;
    }
    if (StringUtils.equals(config.intervalUnit, "hour")) {
      return `0 0 0/${val} * * ?`;
    }
  }

  // 定时模式逻辑
  const [hour, minute, second] = config.time.split(":").map(Number);

  if (StringUtils.equals(subMode.value, "daily")) {
    return `${second} ${minute} ${hour} * * ?`;
  }

  if (StringUtils.equals(subMode.value, "weekly")) {
    const days = config.weekDays.length > 0 ? config.weekDays.join(",") : "*";
    return `${second} ${minute} ${hour} ? * ${days}`;
  }

  if (StringUtils.equals(subMode.value, "monthly")) {
    return `${second} ${minute} ${hour} ${config.day} * ?`;
  }

  return "0 0 0 * * ?";
});

// Cron 描述
const cronDescription = computed(() => {
  if (!finalCron.value) {
    return "无效的表达式";
  }
  try {
    return cronstrue.toString(finalCron.value, { locale: "zh_CN" });
  } catch {
    return "无法解析该表达式的含义";
  }
});

const parseCron = (cron: string): CronExpression => {
  if (CronExpressionParser?.parse) {
    return CronExpressionParser.parse(cron, { tz: "Asia/Shanghai" });
  }
  throw new Error("cron-parser api not found");
};

const pad2 = (n: number): string => String(n).padStart(2, "0");

const formatDateTime = (date: Date): string => {
  const yyyy = date.getFullYear();
  const MM = pad2(date.getMonth() + 1);
  const dd = pad2(date.getDate());
  const HH = pad2(date.getHours());
  const mm = pad2(date.getMinutes());
  const ss = pad2(date.getSeconds());
  return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`;
};

const normalizeToDate = (value: unknown): Date | null => {
  if (value instanceof Date) {
    return value;
  }
  const maybe: any = value;
  if (maybe?.toDate && typeof maybe.toDate === "function") {
    return maybe.toDate();
  }
  if (maybe?.getTime && typeof maybe.getTime === "function") {
    return new Date(maybe.getTime());
  }
  return null;
};

// 最近执行时间
const nextRunTimes = computed(() => {
  if (!finalCron.value) {
    return [];
  }
  try {
    const interval = parseCron(finalCron.value);
    const times = [];
    for (let i = 0; i < 6; i++) {
      const nextDate = normalizeToDate(interval.next());
      if (!nextDate) {
        break;
      }
      times.push(formatDateTime(nextDate));
    }
    return times;
  } catch {
    return ["无法计算执行时间预览"];
  }
});

const openModal = (cron?: string): void => {
  visible.value = true;
  if (!cron) {
    return;
  }

  try {
    customCron.value = cron;
    const parts = cron.split(" ");
    if (parts.length < 6) {
      mainMode.value = "custom";
      return;
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [s, m, h, dom, mon, dow] = parts;

    // 识别频率模式 (0/X 或 */X)
    if (StringUtils.contains(s, "/") || StringUtils.contains(m, "/") || StringUtils.contains(h, "/")) {
      mainMode.value = "interval";
      if (StringUtils.contains(s, "/")) {
        config.intervalUnit = "second";
        config.intervalValue = parseInt(s.split("/")[1]);
        return;
      }
      if (StringUtils.contains(m, "/")) {
        config.intervalUnit = "minute";
        config.intervalValue = parseInt(m.split("/")[1]);
        return;
      }
      if (StringUtils.contains(h, "/")) {
        config.intervalUnit = "hour";
        config.intervalValue = parseInt(h.split("/")[1]);
      }
      return;
    }

    // 识别定时模式
    mainMode.value = "scheduled";
    config.time = `${h.padStart(2, "0")}:${m.padStart(2, "0")}:${s.padStart(2, "0")}`;

    if (StringUtils.equals(dom, "*") && StringUtils.equals(dow, "?")) {
      subMode.value = "daily";
      return;
    }
    if (StringUtils.equals(dom, "?") && !StringUtils.equals(dow, "*")) {
      subMode.value = "weekly";
      config.weekDays = dow.split(",").map(Number);
      return;
    }
    if (!StringUtils.equals(dom, "*") && !StringUtils.equals(dom, "?")) {
      subMode.value = "monthly";
      config.day = parseInt(dom);
      return;
    }

    mainMode.value = "custom";
  } catch {
    mainMode.value = "custom";
  }
};

const onConfirm = (): void => {
  emit("onConfirm", finalCron.value);
  visible.value = false;
};

defineExpose({ openModal });
</script>

<style scoped>
.cron-calculator {
  padding: 10px 5px;
  min-height: 550px;
}
.main-mode-group {
  margin-bottom: 5px;
}
.mx-2 {
  margin-left: 8px;
  margin-right: 8px;
}
.preview-section {
  transition: all 0.3s ease;
}
.description-box {
  background-color: #f0f7ff;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}
</style>
