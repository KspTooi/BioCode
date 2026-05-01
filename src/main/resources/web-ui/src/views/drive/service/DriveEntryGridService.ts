import DriveApi from "@/views/drive/api/DriveApi.ts";

import { ElMessage } from "element-plus";
import { ref, onUnmounted, type Ref, reactive, computed, type Reactive, watch } from "vue";
import type { CurrentDirPo, EntryPo, GetEntryListDto, GetEntryListVo } from "@/views/drive/api/DriveTypes.ts";
import { DriveStore } from "@/views/drive/service/DriveStore";
import type Result from "@/commons/model/Result";

export interface EntryGridEmitter {
  //目录切换
  (e: "on-directory-change", currentDir: CurrentDirPo): void;

  //条目列表加载完成
  (e: "on-entries-loaded", items: EntryPo[], total: number): void;

  //条目单击
  (e: "on-entry-click", entry: EntryPo): void;

  //条目双击
  (e: "on-entry-dblclick", entry: EntryPo): void;

  //拖拽条目
  (e: "on-entry-drag", target: EntryPo, entries: EntryPo[], currentDir: CurrentDirPo): void;

  //右键菜单打开
  (e: "on-entry-contextmenu", entries: EntryPo[], event: MouseEvent): void;
}

/**
 * DriveEntryGrid 服务模块
 * 包含框选和拖拽逻辑的钩子函数
 */
export default {
  /**
   * 条目列表打包
   * @param emit
   */
  useEntryList(emit: EntryGridEmitter) {
    const listQuery = reactive<GetEntryListDto>({
      driveSpaceId: null,
      directoryId: DriveStore().getCurrentDir.id,
      keyword: null,
      pageNum: 1,
      pageSize: 50000,
    });
    const listData = ref<EntryPo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    //当前目录信息
    const currentDir = ref<CurrentDirPo>({
      id: null,
      name: null,
      parentId: null,
    });

    const hasParentDir = computed(() => {
      if (currentDir.value.id == null && currentDir.value.parentId == null && currentDir.value.name == null) {
        return false;
      }
      return true;
    });

    const listLoad = async (): Promise<Result<GetEntryListVo>> => {
      listLoading.value = true;
      try {
        //如果当前没有空间则不进行任何操作
        if (DriveStore().getCurrentDriveSpace == null) {
          return;
        }

        listQuery.driveSpaceId = DriveStore().getCurrentDriveSpace.id;
        const res = await DriveApi.getEntryList(listQuery);

        if (res.code != 0) {
          return;
        }

        //检查是否有更改目录
        const isChangeDir = res.data.dirId !== currentDir.value.id;
        if (isChangeDir) {
          emit("on-directory-change", {
            id: res.data.dirId,
            name: res.data.dirName,
            parentId: res.data.dirParentId,
          });
          console.log(`目录变更: ${currentDir.value.name} -> ${res.data.dirName}`);
          DriveStore().setCurrentDir({
            id: res.data.dirId,
            name: res.data.dirName,
            parentId: res.data.dirParentId,
          });
          DriveStore().setCurrentDirPaths(res.data.paths);
        }

        //更新当前目录信息
        currentDir.value = {
          id: res.data.dirId,
          name: res.data.dirName,
          parentId: res.data.dirParentId,
        };

        //后端VO转换为前端PO
        const items: EntryPo[] = res.data.items.map((item) => {
          return {
            id: item.id,
            parentId: res.data.dirParentId,
            name: item.name,
            kind: item.kind,
            attachId: item.attachId,
            attachSize: item.attachSize,
            attachSuffix: item.attachSuffix,
            createTime: item.createTime,
            updateTime: null,
          };
        });

        listData.value = items;
        listTotal.value = parseInt(res.data.total);
        emit("on-entries-loaded", items, parseInt(res.data.total));

        return res;
      } catch (error: any) {
        listLoading.value = false;
        ElMessage.error(error.message || "加载列表失败");
        throw error;
      } finally {
        listLoading.value = false;
      }
    };

    return {
      hasParentDir,
      currentDir,
      listQuery,
      listData,
      listTotal,
      listLoading,
      listLoad,
    };
  },

  /**
   * 鼠标框选打包
   * @param gridRef 容器的 DOM 引用 (gridRef)
   * @param entryRefs 条目 DOM 引用集合 (entryRefs) ID->DOM元素
   */
  useEntrySelection(gridRef: Ref<HTMLElement>, entryRefs: Ref<Map<string, HTMLElement>>) {
    //是否正在框选
    const hasSelecting = ref(false);

    //框选矩形
    const selectBox = ref({
      left: 0,
      top: 0,
      width: 0,
      height: 0,
    });

    //选中项ID集合
    const selectedIds = ref<Set<string>>(new Set());

    //框选开始坐标
    const startX = ref(0);
    const startY = ref(0);

    // 更新选中项 (核心碰撞检测算法)
    const updateSelectedIds = (): void => {
      //如果容器不存在则返回
      if (!gridRef.value) {
        return;
      }

      const newSelectedIds = new Set<string>();
      const boxRect = {
        left: selectBox.value.left,
        top: selectBox.value.top,
        right: selectBox.value.left + selectBox.value.width,
        bottom: selectBox.value.top + selectBox.value.height,
      };

      const gridRect = gridRef.value.getBoundingClientRect();
      const scrollLeft = gridRef.value.scrollLeft;
      const scrollTop = gridRef.value.scrollTop;

      //遍历所有条目
      entryRefs.value.forEach((entryEl, id) => {
        const entryRect = entryEl.getBoundingClientRect();

        // 计算条目相对于容器内容的坐标
        const entryBox = {
          left: entryRect.left - gridRect.left + scrollLeft,
          top: entryRect.top - gridRect.top + scrollTop,
          right: entryRect.right - gridRect.left + scrollLeft,
          bottom: entryRect.bottom - gridRect.top + scrollTop,
        };

        // 碰撞检测
        if (
          boxRect.left < entryBox.right &&
          boxRect.right > entryBox.left &&
          boxRect.top < entryBox.bottom &&
          boxRect.bottom > entryBox.top
        ) {
          newSelectedIds.add(id);
        }
      });

      selectedIds.value = newSelectedIds;
    };

    let cachedRect: DOMRect | null = null; // 缓存的容器矩形
    let rafId: number | null = null; // 请求动画帧ID

    /**
     * 鼠标移动事件
     * @param event 鼠标事件
     */
    const onMouseMove = (event: MouseEvent): void => {
      if (!hasSelecting.value || !gridRef.value) {
        return;
      }

      //取消上次请求动画帧
      if (rafId) {
        cancelAnimationFrame(rafId);
      }

      rafId = requestAnimationFrame(() => {
        //如果没有检测到鼠标按下 立即结束框选
        if (event.buttons === 0) {
          onMouseUp();
          return;
        }

        const container = gridRef.value;

        if (!container) {
          return;
        }

        //只有在没有缓存时才获取 Rect（建议在 onMouseDown 中获取并重置为 null）
        if (!cachedRect) {
          cachedRect = container.getBoundingClientRect();
        }

        //计算相对坐标（考虑滚动条）
        let currentX = event.clientX - cachedRect.left + container.scrollLeft;
        let currentY = event.clientY - cachedRect.top + container.scrollTop;

        //边界约束：限制坐标不超出容器实际内容区域
        const scrollWidth = container.scrollWidth;
        const scrollHeight = container.scrollHeight;
        currentX = Math.max(0, Math.min(currentX, scrollWidth));
        currentY = Math.max(0, Math.min(currentY, scrollHeight));

        //计算矩形几何属性
        const left = Math.min(startX.value, currentX);
        const top = Math.min(startY.value, currentY);
        const width = Math.abs(currentX - startX.value);
        const height = Math.abs(currentY - startY.value);

        //批量更新响应式数据
        selectBox.value = { left, top, width, height };

        //执行碰撞检测（建议也进行节流处理）
        updateSelectedIds();
      });
    };

    /**
     * 鼠标抬起事件
     */
    const onMouseUp = (): void => {
      //如果不在框选状态则返回
      if (!hasSelecting.value) {
        return;
      }

      hasSelecting.value = false;
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
      window.removeEventListener("blur", onMouseUp);
    };

    /**
     * 鼠标按下事件
     * @param event 鼠标事件
     */
    const onMouseDown = (event: MouseEvent): void => {
      //如果容器不存在则返回
      if (!gridRef.value) {
        return;
      }

      //如果不是左键按下则返回
      if (event.button !== 0) {
        return;
      }

      //如果点击到文件本身则返回
      const target = event.target as HTMLElement;
      if (target.closest(".drive-entry-item")) {
        return;
      }

      //记录框选开始坐标
      const rect = gridRef.value.getBoundingClientRect();
      startX.value = event.clientX - rect.left + gridRef.value.scrollLeft;
      startY.value = event.clientY - rect.top + gridRef.value.scrollTop;

      //开始新框选时清空旧选择
      hasSelecting.value = true;
      selectedIds.value.clear();

      selectBox.value = {
        left: startX.value,
        top: startY.value,
        width: 0,
        height: 0,
      };

      document.addEventListener("mousemove", onMouseMove);
      document.addEventListener("mouseup", onMouseUp);
      //失去焦点时立即结束框选
      window.addEventListener("blur", onMouseUp);
    };

    /**
     * 用于鼠标单击一个元素时选择、取消
     * @param entry 条目对象
     */
    const selectEntry = (entry: EntryPo): void => {
      //如果点选的时候当前选择了不止一个条目 则直接点选当前条目
      if (selectedIds.value.size > 1) {
        selectedIds.value.clear();
        selectedIds.value.add(entry.id);
        return;
      }

      //点选时如果当前条目已经选中 则取消选中
      if (selectedIds.value.has(entry.id)) {
        selectedIds.value.delete(entry.id);
        return;
      }

      //取消所有选中
      selectedIds.value.clear();

      //添加选中
      selectedIds.value.add(entry.id);
    };

    // 清理副作用
    onUnmounted(() => {
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
    });

    return {
      //用于鼠标单击一个元素时选择、取消
      selectEntry,

      //是否正在框选
      hasSelecting,

      //框选矩形
      selectBox,

      //选中项ID集合
      selectedIds,

      //鼠标按下事件
      onMouseDown,

      // 如果需要手动清空选择,也可以导出这个方法
      clearSelection: () => selectedIds.value.clear(),
    };
  },

  /**
   * 拖拽逻辑钩子
   * @param entryData 当前文件列表
   * @param selectedIds 当前已被选中的条目IDS
   * @param emit Vue的emit函数,用于向父组件发送最终的拖拽事件
   */
  useEntryDrag(
    entryData: Ref<EntryPo[]>,
    selectedIds: Ref<Set<string>>,
    emit: (event: "on-entry-drag", target: EntryPo, entries: EntryPo[], currentDir: CurrentDirPo) => void
  ) {
    //当前正在拖拽的条目
    const draggedEntry: Ref<EntryPo> = ref();

    /**
     * 开始拖拽
     * @param entry 拖拽的条目
     * @param event 拖拽事件
     */
    const onDragStart = (entry: EntryPo, event: DragEvent): void => {
      if (!entry.id) {
        return;
      }

      draggedEntry.value = entry;
      if (event.dataTransfer) {
        event.dataTransfer.effectAllowed = "move";
        //可设置拖拽时的预览图
        //event.dataTransfer.setDragImage(...)
      }
    };

    /**
     * 拖拽经过某条目
     * @param entry 拖拽的条目
     * @param event 拖拽事件
     */
    const onDragOver = (entry: EntryPo, event: DragEvent): void => {
      // 简单的防抖或检查可以在这里做
      if (event.dataTransfer) {
        event.dataTransfer.dropEffect = "move";
      }
    };

    /**
     * 放置在某条目上 (目标是普通文件/文件夹)
     * @param targetEntry 目标条目
     * @param event 拖拽事件
     */
    const onDrop = (targetEntry: EntryPo, currentDir: CurrentDirPo, event: DragEvent): void => {
      //计算实际被拖拽的文件列表 (处理多选)
      let dragEntries: EntryPo[] = [];

      const hasMultiDrag = selectedIds.value.size > 1 && selectedIds.value.has(draggedEntry.value.id);

      //如果拖拽的是选中集合里的某一项,则移动所有选中的项
      if (hasMultiDrag) {
        // 如果拖拽的是选中集合里的一项,则移动所有选中的项
        dragEntries = entryData.value.filter((item) => selectedIds.value.has(item.id));
      }
      if (!hasMultiDrag) {
        dragEntries = [draggedEntry.value];
      }

      //如果拖拽到上级目录 target为null 需要组装一个虚拟的EntryPo
      if (targetEntry == null) {
        const parentDirPo: EntryPo = {
          id: currentDir.parentId,
          parentId: currentDir.parentId,
          name: currentDir.name + "_上级目录",
          kind: 1,
          attachId: null,
          attachSize: null,
          attachSuffix: null,
          createTime: null,
          updateTime: null,
        };

        emit("on-entry-drag", parentDirPo, dragEntries, currentDir);
        draggedEntry.value = null;
        return;
      }

      //如果拖拽集合中的某一项与拖拽的目标相同 则判定为自身拖拽
      if (dragEntries.some((item) => item.id === targetEntry.id)) {
        return;
      }

      //发送事件
      emit("on-entry-drag", targetEntry, dragEntries, currentDir);
      draggedEntry.value = null;
    };

    return {
      draggedEntry,
      onDragStart,
      onDragOver,
      onDrop,
    };
  },

  /**
   * 文件夹导航打包
   * @param currentDir 当前目录
   */
  useDirectoryNavigation(listQuery: Reactive<GetEntryListDto>, selectedIds: Ref<Set<string>>, listLoad: () => void) {
    /**
     * 重定向到指定目录
     * @param dirId 目录ID
     */
    const redirectDirectory = (dirId: string): void => {
      listQuery.directoryId = dirId;
      listLoad();
      selectedIds.value.clear();
    };

    /**
     * 进入文件夹
     * @param entry 条目对象
     * @param currentDir 当前目录
     */
    const enterDirectory = (entry: EntryPo, currentDir: CurrentDirPo): void => {
      //如果条目ID为空 则进入上级目录
      if (entry.id == null) {
        listQuery.directoryId = currentDir.parentId;
        listLoad();
        selectedIds.value.clear();
        return;
      }

      //判断是否是文件夹
      if (entry.kind !== 1) {
        //ElMessage.error("指定的目标不是文件夹.");
        return;
      }

      //进入文件夹
      listQuery.directoryId = entry.id;
      listLoad();
      selectedIds.value.clear();
    };

    /**
     * 返回上级目录
     */
    const backspace = (): void => {
      const currentDir = DriveStore().getCurrentDir;

      //已经位于root目录
      if (currentDir == null && currentDir.id == null && currentDir.name == null && currentDir.parentId == null) {
        console.log("已经位于root目录");
        return;
      }

      listQuery.directoryId = currentDir.parentId;
      listLoad();
      selectedIds.value.clear();
    };

    //监听云盘空间变更
    watch(
      () => DriveStore().getCurrentDriveSpace,
      (newVal) => {
        console.log("云盘空间变更", newVal);
        //同步更新 driveSpaceId，否则 listLoad 仍会使用旧值
        listQuery.driveSpaceId = newVal?.id ?? null;
        listQuery.directoryId = null;
        listLoad();
        selectedIds.value.clear();
      },
      { immediate: true, deep: true }
    );

    return {
      redirectDirectory,
      enterDirectory,
      backspace,
    };
  },
};
