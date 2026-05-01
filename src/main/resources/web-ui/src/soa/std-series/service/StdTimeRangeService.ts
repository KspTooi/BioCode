import { computed, nextTick, ref, watch, type Ref } from "vue";

type DisableDateHandler = (date: Date) => boolean;

interface StdTimeRangeServiceOptions {
  rangeStart: Ref<string>;
  rangeEnd: Ref<string>;
  getType: () => "daterange" | "datetimerange";
  getIsFocus: () => boolean;
  getDisableStartDate?: () => DisableDateHandler | undefined;
  getDisableEndDate?: () => DisableDateHandler | undefined;
}

interface FocusablePicker {
  focus?: () => void;
}

export default {
  useStdTimeRange(options: StdTimeRangeServiceOptions) {
    //内部开始日期
    const innerRangeStart = ref(options.rangeStart.value);
    //内部结束日期
    const innerRangeEnd = ref(options.rangeEnd.value);
    //开始时间选择器引用
    const startPickerRef = ref<FocusablePicker>();
    //结束时间选择器引用
    const endPickerRef = ref<FocusablePicker>();
    //是否需要聚焦结束时间
    const pendingFocusEnd = ref(false);
    //是否需要聚焦开始时间
    const pendingFocusStart = ref(false);
    //是否正在同步内部日期
    let isSyncingInner = false;
    /**
     * 监听内部日期变化
     * @param start 开始日期
     * @param end 结束日期
     */
    watch([() => options.rangeStart.value, () => options.rangeEnd.value], ([start, end]) => {
      if (isSyncingInner) {
        return;
      }
      innerRangeStart.value = start;
      innerRangeEnd.value = end;
    });

    /**
     * 同步内部日期到父组件
     */
    const syncRangeToParent = (): void => {
      isSyncingInner = true;
      if (!innerRangeStart.value || !innerRangeEnd.value) {
        options.rangeStart.value = "";
        options.rangeEnd.value = "";
        queueMicrotask(() => {
          isSyncingInner = false;
        });
        return;
      }
      options.rangeStart.value = innerRangeStart.value;
      options.rangeEnd.value = innerRangeEnd.value;
      queueMicrotask(() => {
        isSyncingInner = false;
      });
    };

    /**
     * 开始日期值
     */
    const rangeStartValue = computed({
      get: () => innerRangeStart.value,
      set: (val: string) => {
        if (!val) {
          innerRangeStart.value = "";
          syncRangeToParent();
          return;
        }
        const inputDate = toDateTime(val);
        const rangeEndDate = toDateTime(innerRangeEnd.value);
        if (rangeEndDate && inputDate && inputDate.getTime() > rangeEndDate.getTime()) {
          if (options.getType() !== "datetimerange") {
            return;
          }
          if (!isSameDay(inputDate, rangeEndDate)) {
            return;
          }
          innerRangeStart.value = innerRangeEnd.value;
          pendingFocusEnd.value = false;
          syncRangeToParent();
          return;
        }
        innerRangeStart.value = val;
        if (!innerRangeEnd.value) {
          syncRangeToParent();
          pendingFocusEnd.value = true;
          return;
        }
        pendingFocusEnd.value = false;
        syncRangeToParent();
      },
    });

    /**
     * 结束日期值
     */
    const rangeEndValue = computed({
      get: () => innerRangeEnd.value,
      set: (val: string) => {
        if (!val) {
          innerRangeEnd.value = "";
          syncRangeToParent();
          return;
        }
        const inputDate = toDateTime(val);
        const rangeStartDate = toDateTime(innerRangeStart.value);
        if (rangeStartDate && inputDate && inputDate.getTime() < rangeStartDate.getTime()) {
          if (options.getType() !== "datetimerange") {
            return;
          }
          if (!isSameDay(inputDate, rangeStartDate)) {
            return;
          }
          innerRangeEnd.value = innerRangeStart.value;
          pendingFocusStart.value = false;
          syncRangeToParent();
          return;
        }
        innerRangeEnd.value = val;
        if (!innerRangeStart.value) {
          syncRangeToParent();
          pendingFocusStart.value = true;
          return;
        }
        pendingFocusStart.value = false;
        syncRangeToParent();
      },
    });

    /**
     * 开始时间选择器可见变化
     * @param visible 是否可见
     */
    const onStartPickerVisibleChange = (visible: boolean): void => {
      if (visible) {
        return;
      }
      if (!pendingFocusEnd.value) {
        return;
      }
      pendingFocusEnd.value = false;
      nextTick(() => {
        if (!options.getIsFocus()) {
          return;
        }
        endPickerRef.value?.focus?.();
      });
    };

    /**
     * 结束时间选择器可见变化
     * @param visible 是否可见
     */
    const onEndPickerVisibleChange = (visible: boolean): void => {
      if (visible) {
        return;
      }
      if (!pendingFocusStart.value) {
        return;
      }
      pendingFocusStart.value = false;
      nextTick(() => {
        if (!options.getIsFocus()) {
          return;
        }
        startPickerRef.value?.focus?.();
      });
    };

    /**
     * 开始时间验证消息
     */
    const startValidateMessage = computed(() => {
      if (!innerRangeEnd.value) {
        return "";
      }
      if (innerRangeStart.value) {
        return "";
      }
      return "请选择开始时间";
    });

    /**
     * 结束时间验证消息
     */
    const endValidateMessage = computed(() => {
      if (!innerRangeStart.value) {
        return "";
      }
      if (innerRangeEnd.value) {
        return "";
      }
      return "请选择结束时间";
    });

    /**
     * 创建数字范围
     * @param start 开始
     * @param end 结束
     * @returns 数字范围
     */
    const createNumberRange = (start: number, end: number): number[] => {
      if (start > end) {
        return [];
      }
      return Array.from({ length: end - start + 1 }, (_, idx) => start + idx);
    };

    /**
     * 转换为日期时间
     * @param value 值
     * @returns 日期时间
     */
    const toDateTime = (value: unknown): Date | undefined => {
      if (value instanceof Date) {
        if (Number.isNaN(value.getTime())) {
          return undefined;
        }
        return value;
      }
      if (value && typeof value === "object") {
        const maybeToDate = (value as { toDate?: () => Date }).toDate;
        if (typeof maybeToDate === "function") {
          const date = maybeToDate();
          if (!(date instanceof Date)) {
            return undefined;
          }
          if (Number.isNaN(date.getTime())) {
            return undefined;
          }
          return date;
        }
        const maybeInnerDate = (value as { $d?: Date }).$d;
        if (maybeInnerDate instanceof Date) {
          if (Number.isNaN(maybeInnerDate.getTime())) {
            return undefined;
          }
          return maybeInnerDate;
        }
      }
      if (typeof value !== "string" && typeof value !== "number") {
        return undefined;
      }
      if (value === "") {
        return undefined;
      }
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return undefined;
      }
      return date;
    };

    /**
     * 判断是否同一天
     * @param dateA 日期A
     * @param dateB 日期B
     * @returns 是否同一天
     */
    const isSameDay = (dateA: Date, dateB: Date): boolean => {
      if (dateA.getFullYear() !== dateB.getFullYear()) {
        return false;
      }
      if (dateA.getMonth() !== dateB.getMonth()) {
        return false;
      }
      return dateA.getDate() === dateB.getDate();
    };

    /**
     * 默认开始日期禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const defaultDisableStartDate = (date: Date): boolean => {
      if (!innerRangeEnd.value) {
        return false;
      }
      const rangeEndDate = toDateTime(innerRangeEnd.value);
      if (!rangeEndDate) {
        return false;
      }
      const maxDate = new Date(rangeEndDate.getFullYear(), rangeEndDate.getMonth(), rangeEndDate.getDate(), 23, 59, 59, 999);
      return date.getTime() > maxDate.getTime();
    };

    /**
     * 默认结束日期禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const defaultDisableEndDate = (date: Date): boolean => {
      if (!innerRangeStart.value) {
        return false;
      }
      const rangeStartDate = toDateTime(innerRangeStart.value);
      if (!rangeStartDate) {
        return false;
      }
      const minDate = new Date(rangeStartDate.getFullYear(), rangeStartDate.getMonth(), rangeStartDate.getDate(), 0, 0, 0, 0);
      return date.getTime() < minDate.getTime();
    };

    /**
     * 解析开始日期禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableStartDate = (date: Date): boolean => {
      const disableStartDate = options.getDisableStartDate?.();
      if (disableStartDate) {
        return disableStartDate(date);
      }
      return defaultDisableStartDate(date);
    };

    /**
     * 解析结束日期禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableEndDate = (date: Date): boolean => {
      const disableEndDate = options.getDisableEndDate?.();
      if (disableEndDate) {
        return disableEndDate(date);
      }
      return defaultDisableEndDate(date);
    };

    /**
     * 解析开始面板日期
     * @param date 日期
     * @returns 日期
     */
    const resolveStartPanelDate = (date?: unknown): Date | undefined => {
      const panelDate = toDateTime(date);
      if (panelDate) {
        return panelDate;
      }
      return toDateTime(innerRangeStart.value);
    };

    /**
     * 解析结束面板日期
     * @param date 日期
     * @returns 日期
     */
    const resolveEndPanelDate = (date?: unknown): Date | undefined => {
      const panelDate = toDateTime(date);
      if (panelDate) {
        return panelDate;
      }
      return toDateTime(innerRangeEnd.value);
    };

    /**
     * 解析开始时间禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableStartHours = (date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeEndDate = toDateTime(innerRangeEnd.value);
      if (!rangeEndDate) {
        return [];
      }
      const panelDate = resolveStartPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeEndDate)) {
        return [];
      }
      return createNumberRange(rangeEndDate.getHours() + 1, 23);
    };

    /**
     * 解析开始时间禁用逻辑
     * @param hour 小时
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableStartMinutes = (hour: number, date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeEndDate = toDateTime(innerRangeEnd.value);
      if (!rangeEndDate) {
        return [];
      }
      const panelDate = resolveStartPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeEndDate)) {
        return [];
      }
      if (hour !== rangeEndDate.getHours()) {
        return [];
      }
      return createNumberRange(rangeEndDate.getMinutes() + 1, 59);
    };

    /**
     * 解析开始时间禁用逻辑
     * @param hour 小时
     * @param minute 分钟
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableStartSeconds = (hour: number, minute: number, date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeEndDate = toDateTime(innerRangeEnd.value);
      if (!rangeEndDate) {
        return [];
      }
      const panelDate = resolveStartPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeEndDate)) {
        return [];
      }
      if (hour !== rangeEndDate.getHours()) {
        return [];
      }
      if (minute !== rangeEndDate.getMinutes()) {
        return [];
      }
      return createNumberRange(rangeEndDate.getSeconds() + 1, 59);
    };

    /**
     * 解析结束时间禁用逻辑
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableEndHours = (date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeStartDate = toDateTime(innerRangeStart.value);
      if (!rangeStartDate) {
        return [];
      }
      const panelDate = resolveEndPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeStartDate)) {
        return [];
      }
      return createNumberRange(0, rangeStartDate.getHours() - 1);
    };

    /**
     * 解析结束时间禁用逻辑
     * @param hour 小时
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableEndMinutes = (hour: number, date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeStartDate = toDateTime(innerRangeStart.value);
      if (!rangeStartDate) {
        return [];
      }
      const panelDate = resolveEndPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeStartDate)) {
        return [];
      }
      if (hour !== rangeStartDate.getHours()) {
        return [];
      }
      return createNumberRange(0, rangeStartDate.getMinutes() - 1);
    };

    /**
     * 解析结束时间禁用逻辑
     * @param hour 小时
     * @param minute 分钟
     * @param date 日期
     * @returns 是否禁用
     */
    const resolveDisableEndSeconds = (hour: number, minute: number, date?: unknown): number[] => {
      if (options.getType() !== "datetimerange") {
        return [];
      }
      const rangeStartDate = toDateTime(innerRangeStart.value);
      if (!rangeStartDate) {
        return [];
      }
      const panelDate = resolveEndPanelDate(date);
      if (!panelDate) {
        return [];
      }
      if (!isSameDay(panelDate, rangeStartDate)) {
        return [];
      }
      if (hour !== rangeStartDate.getHours()) {
        return [];
      }
      if (minute !== rangeStartDate.getMinutes()) {
        return [];
      }
      return createNumberRange(0, rangeStartDate.getSeconds() - 1);
    };

    return {
      startPickerRef,
      endPickerRef,
      rangeStartValue,
      rangeEndValue,
      onStartPickerVisibleChange,
      onEndPickerVisibleChange,
      startValidateMessage,
      endValidateMessage,
      resolveDisableStartDate,
      resolveDisableEndDate,
      resolveDisableStartHours,
      resolveDisableStartMinutes,
      resolveDisableStartSeconds,
      resolveDisableEndHours,
      resolveDisableEndMinutes,
      resolveDisableEndSeconds,
    };
  },
};
