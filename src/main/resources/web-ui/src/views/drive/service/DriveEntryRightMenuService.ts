import { DriveStore } from "@/views/drive/service/DriveStore.ts";
import { computed, onMounted, onUnmounted, ref, type Ref } from "vue";
import type { EntryPo } from "@/views/drive/api/DriveTypes";

export interface RightMenuEmitter {
  (e: "on-refresh"): void; //刷新
  (e: "on-paste"): void; //粘贴
  (e: "on-create-folder"): void; //创建文件夹
  (e: "on-upload-file"): void; //上传文件
  (e: "on-preview", entry: EntryPo): void; //预览
  (e: "on-download", entries: EntryPo[]): void; //下载
  (e: "on-download-url", entries: EntryPo[]): void; //创建下载链接
  (e: "on-cut", entries: EntryPo[]): void; //剪切
  (e: "on-copy", entries: EntryPo[]): void; //复制
  (e: "on-delete", entries: EntryPo[]): void; //删除
  (e: "on-rename", entry: EntryPo): void; //重命名
  (e: "on-properties", entry: EntryPo): void; //属性
}

export default {
  /**
   * 右键菜单控制打包
   * @param rightMenuRef 右键菜单引用
   */
  useRightMenuControl(rightMenuRef: Ref<HTMLElement>) {
    const x = ref(0);
    const y = ref(0);
    const visible = ref(false);

    //当前选中的条目列表
    const currentEntries = ref<EntryPo[]>([]);

    //是否有粘贴板内容
    const hasClipboard = computed(() => {
      return DriveStore().getClipBoardEntry.length > 0;
    });

    //是否多选
    const isMultiSelect = computed(() => {
      return currentEntries.value.length > 1;
    });

    //当前选中的条目
    const currentEntry = computed(() => {
      return currentEntries.value[0] || null;
    });

    /**
     * 打开右键菜单
     * @param event 鼠标事件
     * @param entries 当前条目 为null表示没有选中条目
     */
    const openRightMenu = (event: MouseEvent, entries: EntryPo[] | null = null): void => {
      x.value = event.clientX;
      y.value = event.clientY;
      currentEntries.value = entries || [];
      visible.value = true;
    };

    /**
     * 关闭右键菜单
     */
    const closeRightMenu = (): void => {
      visible.value = false;
    };

    /**
     * 处理点击到外部区域关闭菜单
     * @param event 鼠标事件
     */
    const onClickOutside = (event: MouseEvent): void => {
      if (rightMenuRef.value && !rightMenuRef.value.contains(event.target as Node)) {
        closeRightMenu();
      }
    };

    onMounted(() => {
      document.addEventListener("click", onClickOutside);
    });

    onUnmounted(() => {
      document.removeEventListener("click", onClickOutside);
    });

    return {
      openRightMenu,
      closeRightMenu,
      isMultiSelect,
      currentEntry,
      currentEntries,
      hasClipboard,
      visible,
      x,
      y,
    };
  },

  /**
   * 右键菜单事件提交打包
   * @param emit 事件提交
   * @param closeRightMenu 关闭右键菜单
   * @param currentEntry 当前选中的条目
   * @param currentEntries 当前选中的条目列表
   */
  useRightMenuEmitter(
    emit: RightMenuEmitter,
    closeRightMenu: () => void,
    currentEntry: Ref<EntryPo>,
    currentEntries: Ref<EntryPo[]>
  ) {
    return {
      //刷新
      onRefresh: () => {
        emit("on-refresh");
        closeRightMenu();
      },

      //粘贴
      onPaste: () => {
        emit("on-paste");
        closeRightMenu();
      },

      //创建文件夹
      onCreateFolder: () => {
        emit("on-create-folder");
        closeRightMenu();
      },

      //上传文件
      onUploadFile: () => {
        emit("on-upload-file");
        closeRightMenu();
      },

      //预览
      onPreview: () => {
        emit("on-preview", currentEntry.value);
      },

      //下载
      onDownload: () => {
        emit("on-download", currentEntries.value);
        closeRightMenu();
      },

      //下载URL
      onDownloadUrl: () => {
        emit("on-download-url", currentEntries.value);
        closeRightMenu();
      },

      //剪切
      onCut: () => {
        emit("on-cut", currentEntries.value);
        closeRightMenu();
      },

      //复制
      onCopy: () => {
        emit("on-copy", currentEntries.value);
        closeRightMenu();
      },

      //删除
      onDelete: () => {
        emit("on-delete", currentEntries.value);
        closeRightMenu();
      },

      //重命名
      onRename: () => {
        emit("on-rename", currentEntry.value);
        closeRightMenu();
      },

      //属性
      onProperties: () => {
        emit("on-properties", currentEntry.value);
        closeRightMenu();
      },
    };
  },
};
