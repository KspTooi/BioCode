<template>
  <el-dialog
    v-model="visible"
    title="裁剪头像"
    width="520px"
    :close-on-click-modal="false"
    destroy-on-close
    append-to-body
    class="avatar-cropper-dialog"
  >
    <div class="cropper-wrapper">
      <VueCropper
        ref="cropperRef"
        :img="imgSrc"
        :autoCrop="true"
        :fixed="true"
        :fixedNumber="[1, 1]"
        :canScale="true"
        :canMove="true"
        :canMoveBox="false"
        :centerBox="true"
        :high="true"
        :info="false"
        :outputSize="props.quality"
        :outputType="props.outputType"
        mode="contain"
      />
    </div>

    <div class="cropper-toolbar">
      <div class="toolbar-row">
        <span class="toolbar-label">缩放</span>
        <el-slider
          v-model="scaleValue"
          :min="0.5"
          :max="4"
          :step="0.05"
          :show-tooltip="false"
          class="scale-slider"
          @input="onScaleChange"
        />
      </div>
      <div class="toolbar-row toolbar-btns">
        <el-button size="small" plain @click="onRotateLeft">
          <el-icon><RefreshLeft /></el-icon>
          向左旋转
        </el-button>
        <el-button size="small" plain @click="onRotateRight">
          <el-icon><RefreshRight /></el-icon>
          向右旋转
        </el-button>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="confirming" @click="onConfirm">确认裁剪</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import "vue-cropper/dist/index.css";
import { ref } from "vue";
import { VueCropper } from "vue-cropper";
import { ElDialog, ElButton, ElSlider, ElIcon } from "element-plus";
import { RefreshLeft, RefreshRight } from "@element-plus/icons-vue";

interface Props {
  outputSize?: number;
  outputType?: "jpeg" | "png" | "webp";
  quality?: number;
}

const props = withDefaults(defineProps<Props>(), {
  outputSize: 512,
  outputType: "jpeg",
  quality: 0.92,
});

const emit = defineEmits<{ (e: "confirm", file: File): void }>();

const visible = ref(false);
const confirming = ref(false);
const imgSrc = ref("");
const scaleValue = ref(1);
const cropperRef = ref<InstanceType<typeof VueCropper>>();

let prevScale = 1;

const openModal = (src: string): void => {
  imgSrc.value = src;
  scaleValue.value = 1;
  prevScale = 1;
  confirming.value = false;
  visible.value = true;
};

const onScaleChange = (val: number): void => {
  const delta = val - prevScale;
  prevScale = val;
  cropperRef.value?.changeScale(delta > 0 ? 1 : -1);
};

const onRotateLeft = (): void => {
  cropperRef.value?.rotateLeft();
};

const onRotateRight = (): void => {
  cropperRef.value?.rotateRight();
};

const onConfirm = (): void => {
  confirming.value = true;
  cropperRef.value?.getCropBlob((blob: Blob) => {
    const file = new File([blob], `avatar.${props.outputType}`, { type: blob.type });
    emit("confirm", file);
    visible.value = false;
    confirming.value = false;
  });
};

defineExpose({ openModal });
</script>

<style scoped>
.cropper-wrapper {
  width: 100%;
  height: 380px;
  background-color: #f5f7fa;
}

.cropper-toolbar {
  padding: 12px 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-label {
  font-size: 13px;
  color: #888;
  flex-shrink: 0;
  width: 28px;
}

.scale-slider {
  flex: 1;
}

.toolbar-btns {
  justify-content: flex-start;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog) {
  border-radius: 0;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}

:deep(.el-button) {
  border-radius: 0;
}

:deep(.el-slider__runway) {
  border-radius: 0;
}

:deep(.el-slider__bar) {
  border-radius: 0;
}

:deep(.el-slider__button) {
  border-radius: 50%;
}
</style>
