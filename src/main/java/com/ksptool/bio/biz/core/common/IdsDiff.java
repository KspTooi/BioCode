package com.ksptool.bio.biz.core.common;

import java.util.*;

/**
 * ID列表差异计算器
 * 用于计算已存在的ID列表与输入的ID列表之间的差异
 */
public class IdsDiff {

    private final List<Long> addIds;
    private final List<Long> removeIds;

    /**
     * 构造函数，计算两个ID集合的差异
     *
     * @param existsIds 已存在的ID列表
     * @param inputIds  输入的ID列表
     */
    public IdsDiff(Collection<Long> existsIds, Collection<Long> inputIds) {
        Set<Long> existsSet = existsIds != null ? new HashSet<>(existsIds) : new HashSet<>();
        Set<Long> inputSet = inputIds != null ? new HashSet<>(inputIds) : new HashSet<>();

        this.addIds = new ArrayList<>();
        for (Long id : inputSet) {
            if (!existsSet.contains(id)) {
                addIds.add(id);
            }
        }

        this.removeIds = new ArrayList<>();
        for (Long id : existsSet) {
            if (!inputSet.contains(id)) {
                removeIds.add(id);
            }
        }
    }

    /**
     * 获取需要新增的ID列表（在inputIds中存在，但在existsIds中不存在）
     *
     * @return 新增的ID列表
     */
    public List<Long> getAddIds() {
        return addIds;
    }

    /**
     * 获取需要删除的ID列表（在existsIds中存在，但在inputIds中不存在）
     *
     * @return 删除的ID列表
     */
    public List<Long> getRemoveIds() {
        return removeIds;
    }

    /**
     * 是否存在需要新增的ID
     *
     * @return 是否存在需要新增的ID
     */
    public boolean hasAdd() {
        return !addIds.isEmpty();
    }

    /**
     * 是否存在需要删除的ID
     *
     * @return 是否存在需要删除的ID
     */
    public boolean hasRemove() {
        return !removeIds.isEmpty();
    }

}
