/**
 * 表格勾选服务
 * 这个服务提供通用的表格勾选功能，包含单选+多选，以及普通勾选与跨页勾选两种模式
 *
 * 这个服务做的事情:
 * 给定一个表格 + 当前页数据 + 外部已选IDS，它帮你管理表格的"勾选状态"。
 *
 * 注意：单选与复选在表格中都是以多选模式实现的，只是单选模式下会强制取消其余行选中状态
 * @Author: KspTooi
 * @Since 1.6.Z(26).8
 */
import { watchDebounced } from "@vueuse/core";
import { computed, nextTick, ref, type Ref } from "vue";

export type Mode = "single" | "multiple" | "multiple-in-page";

/**
 * 表格勾选参数
 * T = 行数据类型(通常为 GetXXXXListVo)
 * K = 唯一标识类型(通常为 string | number)
 */
interface StcsParam<T, K extends string | number> {
  //模式: 单选、多选、多选不跨页
  mode: Mode;

  //当前页数据
  listData: Ref<T[]>;

  //外部双向绑定的已选 ids
  checkedIds: Ref<K[]>;

  //外部双向绑定的已选 VO
  checkedRows: Ref<T[]>;

  //行键 用于从行数据中获取唯一标识
  rowKey: (row: T) => K;
}

export default {
  /**
   * 使用表格勾选服务
   * @param params 表格勾选参数
   */
  useStdTableCheck<T, K extends string | number>(params: StcsParam<T, K>) {
    //解构参数
    const { mode, listData, checkedIds, checkedRows, rowKey } = params;

    /**
     * 判断行是否已勾选
     * @param row 行数据
     * @returns 是否已勾选
     */
    const isRowChecked = (row: T): boolean => {
      return checkedIds.value.includes(rowKey(row));
    };

    /**
     * 判断当前页是否都已勾选(用于表头全选框)
     * @returns 是否都已勾选
     */
    const isPageChecked = (): boolean => {
      //如果当前页数据为空，则认为未勾选
      if (!listData.value || listData.value.length === 0) {
        return false;
      }

      //如果是单选模式，则认为未勾选
      if (mode === "single") {
        return false;
      }

      return listData.value.every((row) => isRowChecked(row));
    };

    /**
     * 判断当前页是否半选(用于表头半选框)
     * @returns 是否半选
     */
    const isPageHalfChecked = (): boolean => {
      //如果当前页数据为空，则认为未勾选
      if (!listData.value || listData.value.length === 0) {
        return false;
      }
      //计算已勾选行数
      const checkedCount = listData.value.filter((row) => isRowChecked(row)).length;
      return checkedCount > 0 && checkedCount < listData.value.length;
    };

    /**
     * 切换当前页全选状态
     */
    const togglePageCheck = (): void => {
      //单选模式下，禁用全选
      if (mode === "single") {
        return;
      }

      //当前是半选
      if (isPageHalfChecked()) {
        //把所有行都勾选上
        listData.value.forEach((row) => {
          if (!isRowChecked(row)) {
            toggleCheck(row);
          }
        });
      }

      //当前是全选
      if (isPageChecked()) {
        //清空所有勾选
        clearCheck();
        return;
      }

      //当前是未选 把所有行都勾选上
      listData.value.forEach((row) => toggleCheck(row));
    };

    /**
     * 清空所有勾选
     */
    const clearCheck = (): void => {
      checkedRows.value = [];
      checkedIds.value = [];
    };

    /**
     * 切换行勾选状态
     * @param row 行数据
     */
    const toggleCheck = (row: T): void => {
      const key = rowKey(row);

      //已经勾选
      if (isRowChecked(row)) {
        //单选模式下，直接清空已选
        if (mode === "single") {
          checkedRows.value = [];
          checkedIds.value = [];
          return;
        }

        //多选模式下，直接移除已选
        if (mode === "multiple" || mode === "multiple-in-page") {
          checkedRows.value = checkedRows.value.filter((row) => rowKey(row) !== key);
          checkedIds.value = checkedIds.value.filter((id) => id !== key);
          return;
        }
      }

      //未勾选
      if (!isRowChecked(row)) {
        //单选模式下，直接设置已选
        if (mode === "single") {
          checkedRows.value = [row];
          checkedIds.value = [key];
          return;
        }

        //多选模式下，直接添加已选
        if (mode === "multiple" || mode === "multiple-in-page") {
          checkedRows.value.push(row);
          checkedIds.value.push(key);
          return;
        }
      }
    };

    /**
     * 表格行勾选事件
     * @param row 行数据
     */
    const onElRowCheck = (row: T): void => {
      toggleCheck(row);
    };

    return {
      checkedRows,
      isRowChecked,
      isPageChecked,
      isPageHalfChecked,
      togglePageCheck,
      clearCheck,
      toggleCheck,
      onElRowCheck,
    };
  },
};
