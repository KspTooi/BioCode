import { ref, type Ref } from "vue";
import type { CurrentDirPo, EntryPo } from "@/views/drive/api/DriveTypes";
import DriveFileSelector from "@/views/drive/components/DriveFileSelector.vue";
import DriveModalRemove from "@/views/drive/components/DriveModalRemove.vue";
import DriveModalRename from "@/views/drive/components/DriveModalRename.vue";
import DriveModalMoveConfirm from "@/views/drive/components/DriveModalMoveConfirm.vue";
import DriveModalCreateDir from "@/views/drive/components/DriveModalCreateDir.vue";
import DriveEntryGrid from "@/views/drive/components/DriveEntryGrid.vue";
import { DriveStore } from "@/views/drive/service/DriveStore.ts";
import DriveApi from "@/views/drive/api/DriveApi";
import { ElMessage } from "element-plus";
import { Result } from "@/commons/model/Result.ts";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import FileCategoryService, { EntryCategory } from "@/commons/service/FileCategoryService.ts";
import type DriveModalDownloadUrl from "@/views/drive/components/DriveModalDownloadUrl.vue";
import Http from "@/commons/Http";

//当前目录信息
const currentDir = ref<CurrentDirPo>({
  id: null,
  name: null,
  parentId: null,
});

const entryTotal = ref(0);
const entryData = ref<EntryPo[]>([]);
const entryKeyword = ref<string>(null);

export default {
  /**
   * 条目列表打包
   * @param entryGridRef 条目列表引用
   */
  useEntryList(entryGridRef: Ref<InstanceType<typeof DriveEntryGrid>>) {
    return {
      currentDir,
      entryTotal,
      entryData,
      entryKeyword,

      /**
       * 条目列表加载完成
       * @param items 条目列表
       * @param total 总条数
       */
      onGridLoad(items: EntryPo[], total: number) {
        entryData.value = items;
        entryTotal.value = total;
      },

      /**
       * 目录切换
       * @param dir 当前目录
       */
      onGridDirectoryChange(dir: CurrentDirPo) {
        currentDir.value = dir;
      },

      /**
       * 更新查询关键词
       * @param keyword 查询关键词
       */
      updateQueryKeyword(keyword: string) {
        entryKeyword.value = keyword;
      },

      /**
       * 加载条目列表
       */
      loadEntries: async () => {
        await entryGridRef.value.listLoad();
      },
    };
  },

  /**
   * 条目右键菜单功能打包
   * @param createModalRef 创建文件夹模态框引用
   * @param fileSelectorRef 文件选择器引用
   * @param removeModalRef 删除确认对话框引用
   * @param renameModalRef 重命名模态框引用
   * @param entryGridRef 条目列表引用
   */
  useEntryRightMenuFunction(
    createModalRef: Ref<InstanceType<typeof DriveModalCreateDir>>,
    fileSelectorRef: Ref<InstanceType<typeof DriveFileSelector>>,
    removeModalRef: Ref<InstanceType<typeof DriveModalRemove>>,
    renameModalRef: Ref<InstanceType<typeof DriveModalRename>>,
    entryGridRef: Ref<InstanceType<typeof DriveEntryGrid>>,
    downloadUrlModalRef: Ref<InstanceType<typeof DriveModalDownloadUrl>>
  ) {
    //获取标签页服务
    const { getActiveTabIndex, openTabAt } = ComTabService.useTabService();

    return {
      //刷新
      refresh: async () => {
        await entryGridRef.value.listLoad();
      },

      //粘贴
      paste: async () => {
        const entries = DriveStore().getClipBoardEntry;

        //粘贴板没有内容
        if (entries.length === 0) {
          return;
        }

        try {
          //调用后端粘贴接口
          const result = await DriveApi.copyEntry({
            driveSpaceId: DriveStore().getCurrentDriveSpace.id,
            entryIds: entries.map((item) => item.id as string),
            parentId: entryGridRef.value.getCurrentDirId(),
          });
          if (Result.isSuccess(result)) {
            ElMessage.success("粘贴成功");
            entryGridRef.value.listLoad();
          }
        } catch (error: any) {
          ElMessage.error(error.message || "粘贴失败");
        }
      },

      //创建文件夹
      createDir: () => {
        createModalRef.value.openModal();
      },

      //上传文件
      uploadFile: () => {
        fileSelectorRef.value.openSelector();
      },

      //预览
      preview: async (entry: EntryPo) => {
        if (entry.kind === 1) {
          return; // 文件夹不预览
        }

        let routePath = "";
        let icon = "";
        const fileCategory = FileCategoryService.getFileCategory(entry.attachSuffix);

        //图片
        if (fileCategory === EntryCategory.PHOTO) {
          routePath = "/drive/preview-photo";
          icon = "el-icon-picture";
        }

        //视频
        if (fileCategory === EntryCategory.VIDEO) {
          routePath = "/drive/preview-video";
          icon = "el-icon-video-play";
        }

        //音频
        if (fileCategory === EntryCategory.AUDIO) {
          routePath = "/drive/preview-video";
          icon = "el-icon-headset";
        }

        //PDF
        if (fileCategory === EntryCategory.PDF) {
          routePath = "/drive/preview-pdf";
          icon = "el-icon-document";
        }

        //Word文档
        if (fileCategory === EntryCategory.WORD) {
          routePath = "/drive/preview-word";
          icon = "el-icon-document";
        }

        //Excel表格
        if (fileCategory === EntryCategory.EXCEL) {
          routePath = "/drive/preview-excel";
          icon = "el-icon-table";
        }

        //代码
        if (fileCategory === EntryCategory.CODE) {
          routePath = "/drive/preview-code";
          icon = "el-icon-code";
        }

        //TXT
        if (fileCategory === EntryCategory.TEXT) {
          routePath = "/drive/preview-code";
          icon = "el-icon-text";
        }

        //未能获取到受支持的预览类型
        if (!routePath) {
          ElMessage.info(`暂不支持预览 .${entry.attachSuffix} 文件，请下载查看`);
          return;
        }

        try {
          // 获取临时访问签名
          const result = await DriveApi.getEntrySign({ ids: [entry.id] });

          if (Result.isSuccess(result)) {
            const sign = result.data.params;

            // 构建带参数的路径
            // 将 sign 作为 query 参数传递
            const fullPath = `${routePath}?sign=${encodeURIComponent(sign)}&name=${encodeURIComponent(entry.name)}`;

            //在当前激活的TAB后面插入一个新标签
            openTabAt(
              {
                id: `preview-${entry.id}`, // 使用文件ID作为Tab ID，防止重复打开
                icon: null,
                title: entry.name,
                path: fullPath,
                closable: true,
                kind: "normal",
              },
              getActiveTabIndex() + 1
            );
          }
        } catch (error: any) {
          ElMessage.error(error.message || "获取预览链接失败");
        }
      },

      /**
       * 下载
       * @param entries 条目列表
       */
      download: async (entries: EntryPo[]) => {
        //不支持文件夹与文件混选下载
        if (entries.some((item) => item.kind === 1)) {
          ElMessage.error("不支持文件与文件夹打包下载！请选择同类型条目打包下载！");
          return;
        }

        //获取签名
        try {
          const result = await DriveApi.getEntrySign({ ids: entries.map((item) => item.id as string) });
          if (Result.isSuccess(result)) {
            const params = result.data.params;
            window.open(Http.resolve(`/drive/object/access/downloadEntry?sign=${params}`), "_blank");
          }
        } catch (error: any) {
          ElMessage.error(error.message || "下载失败");
        }
      },

      //下载URL
      downloadUrl: async (entries: EntryPo[]) => {
        //不支持文件夹与文件混选下载
        if (entries.some((item) => item.kind === 1)) {
          ElMessage.error("不支持文件与文件夹打包下载！请选择同类型条目打包下载！");
          return;
        }

        //获取签名
        try {
          const result = await DriveApi.getEntrySign({ ids: entries.map((item) => item.id as string) });
          if (Result.isSuccess(result)) {
            const params = result.data.params;

            // 拼接完整的下载链接
            const fullUrl = Http.getApiUrl() + `/drive/object/access/downloadEntry?sign=${params}`;

            //弹出URL模态框
            downloadUrlModalRef.value?.openModal(fullUrl);
          }
        } catch (error: any) {
          ElMessage.error(error.message || "下载失败");
        }
      },

      //剪切
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cut: (entries: EntryPo[]) => {
        ElMessage.info("不支持剪切功能");
      },

      //复制
      copy: (entries: EntryPo[]) => {
        DriveStore().setClipBoardEntry(entries);
      },

      //删除
      remove: async (entries: EntryPo[]) => {
        await removeModalRef.value.openConfirm(entries);
      },

      //重命名
      rename: (entry: EntryPo) => {
        renameModalRef.value.openModal(entry);
      },

      //属性
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      properties: (entry: EntryPo) => {
        ElMessage.info("暂不支持属性功能");
      },
    };
  },

  /**
   * 条目拖拽功能打包
   * @param entryGridRef 条目列表引用
   * @param moveConfirmModalRef 移动冲突确认模态框引用
   * @param dirParentId 当前目录父级ID
   */
  useEntryDragFunction(
    entryGridRef: Ref<InstanceType<typeof DriveEntryGrid>>,
    moveConfirmModalRef: Ref<InstanceType<typeof DriveModalMoveConfirm>>
  ) {
    return {
      /**
       * 拖拽移动
       * @param target 目标条目 当ID为null时表示拖拽到上级目录
       * @param entries 条目列表
       * @param currentDir 当前目录
       */
      dragMove: async (target: EntryPo, entries: EntryPo[], currentDir: CurrentDirPo) => {
        const entryIds: string[] = [];

        entries.forEach((item) => {
          entryIds.push(item.id as string);
        });

        //检测移动
        const result = await DriveApi.checkEntryMove({
          driveSpaceId: DriveStore().getCurrentDriveSpace.id,
          targetId: target.id,
          entryIds: entryIds,
          mode: 0,
        });
        const canMove = result.data.canMove;

        //0:可以移动 1:名称冲突 2:不可移动
        if (canMove == 2) {
          ElMessage.error(result.data.message);
          return;
        }

        if (canMove == 1) {
          const conflictNames = result.data.conflictNames;
          const action = await moveConfirmModalRef.value.openModal(conflictNames);

          //取消
          if (action === -1) {
            return;
          }

          //跳过
          if (action === 1) {
            await DriveApi.moveEntry({
              driveSpaceId: DriveStore().getCurrentDriveSpace.id,
              targetId: target.id,
              entryIds: entryIds,
              mode: 1,
            });
            entryGridRef.value.listLoad();
            return;
          }
        }

        //覆盖移动
        await DriveApi.moveEntry({
          driveSpaceId: DriveStore().getCurrentDriveSpace.id,
          targetId: target.id,
          entryIds: entryIds,
          mode: 0,
        });
        entryGridRef.value.listLoad();
      },
    };
  },
};
