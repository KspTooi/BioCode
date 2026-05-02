import { onUnmounted, ref, type Ref } from "vue";

/**
 * 渲染器缩放服务模块
 * 封装 Ctrl + 滚轮缩放的核心逻辑
 */
export default {
  /**
   * 使用渲染器缩放功能
   * @param options 配置项
   * @param coords 鼠标当前坐标 X,Y 用于以鼠标为中心的缩放
   * @returns 缩放状态和事件处理器
   */
  useRendererScale(
    options: {
      initialScale?: number;
      minScale?: number;
      maxScale?: number;
      step?: number;
    } = {},
    coords?: { x: Ref<number>; y: Ref<number> }
  ) {
    const { initialScale = 1.0, minScale = 0.1, maxScale = 10.0, step = 0.1 } = options;

    const scale = ref(initialScale);
    const showIndicator = ref(false);
    let timer: number | null = null;

    /**
     * 处理鼠标滚轮事件
     * @param e 滚轮事件
     */
    const onWheel = (e: WheelEvent): void => {
      // 只有按住 Ctrl 时才触发缩放
      if (e.ctrlKey) {
        e.preventDefault();

        // 1. 计算新的缩放比例
        const oldScale = scale.value;
        let newScale = oldScale;

        if (e.deltaY < 0) {
          newScale += step; // 放大
        }
        if (e.deltaY > 0) {
          newScale -= step; // 缩小
        }

        // 限制缩放范围
        newScale = Math.min(Math.max(newScale, minScale), maxScale);
        newScale = Math.round(newScale * 100) / 100; // 解决浮点精度

        // 2. 如果提供了坐标，则执行“以鼠标为中心”的位移计算
        if (coords) {
          const container = e.currentTarget as HTMLElement;
          const rect = container.getBoundingClientRect();
          const mouseX = e.clientX - rect.left;
          const mouseY = e.clientY - rect.top;

          // 公式：NewPos = MousePos - (MousePos - OldPos) * (NewScale / OldScale)
          // 只有当缩放比例发生实际变化且不为0时才计算
          if (newScale !== oldScale && oldScale !== 0) {
            const ratio = newScale / oldScale;
            coords.x.value = mouseX - (mouseX - coords.x.value) * ratio;
            coords.y.value = mouseY - (mouseY - coords.y.value) * ratio;
          }
        }

        // 3. 更新缩放值
        scale.value = newScale;

        // 显示缩放提示
        if (timer) {
          clearTimeout(timer);
        }
        showIndicator.value = true;
        timer = window.setTimeout(() => {
          showIndicator.value = false;
        }, 1000);
      }
    };

    /**
     * 重置缩放
     */
    const resetScale = (): void => {
      scale.value = initialScale;
      showIndicator.value = false;
    };

    return {
      scale,
      showIndicator,
      onWheel,
      resetScale,
    };
  },

  /**
   * 渲染器拖拽功能打包
   * @returns 拖拽状态和事件处理器
   */
  useRendererDrag() {
    const x = ref(0);
    const y = ref(0);
    const isDragging = ref(false);

    // 内部变量，记录拖拽起始状态
    let startX = 0;
    let startY = 0;
    let initialX = 0;
    let initialY = 0;

    /**
     * 鼠标移动处理 (绑定在 document 上以保证拖出容器也能响应)
     */
    const onMouseMove = (e: MouseEvent): void => {
      if (!isDragging.value) {
        return;
      }
      e.preventDefault(); // 防止选中文字或触发其他默认行为

      const dx = e.clientX - startX;
      const dy = e.clientY - startY;

      x.value = initialX + dx;
      y.value = initialY + dy;
    };

    /**
     * 鼠标抬起处理
     */
    const onMouseUp = (): void => {
      isDragging.value = false;
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
    };

    /**
     * 鼠标按下事件 (需要绑定在目标元素上)
     * @param e 鼠标事件
     */
    const onMouseDown = (e: MouseEvent): void => {
      // 只有左键点击才触发拖拽
      if (e.button !== 0) {
        return;
      }

      e.preventDefault(); // 防止图片原本的拖拽行为(生成半透明副本)

      isDragging.value = true;
      startX = e.clientX;
      startY = e.clientY;
      initialX = x.value;
      initialY = y.value;

      // 绑定全局事件，防止鼠标移出容器后松开无法响应
      document.addEventListener("mousemove", onMouseMove);
      document.addEventListener("mouseup", onMouseUp);
    };

    /**
     * 重置位置
     */
    const resetPosition = (): void => {
      x.value = 0;
      y.value = 0;
    };

    // 组件卸载时确保移除监听器，防止内存泄漏
    onUnmounted(() => {
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
    });

    return {
      x,
      y,
      isDragging,
      onMouseDown,
      resetPosition,
    };
  },
};
