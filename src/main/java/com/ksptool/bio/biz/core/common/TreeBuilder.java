package com.ksptool.bio.biz.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平面列表 → 树结构 通用构建器
 * <p>
 * 将包含 id / parentId / children 的扁平节点列表转换为树形结构，
 * parentId 为 null 的节点作为根节点返回。
 *
 * @author KspTool
 * @since 1.6.24(X).40
 */
public class TreeBuilder<T extends TreeBuilder.TreeNode<T>> {

    /**
     * 将扁平节点列表构建为树，返回根节点列表
     *
     * @param flatList 扁平节点列表
     * @return 根节点列表
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> flatList) {
        List<T> roots = new ArrayList<>();

        if (flatList == null || flatList.isEmpty()) {
            return roots;
        }

        Map<Long, T> index = new HashMap<>();
        for (T node : flatList) {
            if (node.getId() != null) {
                index.put(node.getId(), node);
            }
        }

        for (T node : flatList) {
            Long parentId = node.getParentId();

            if (parentId == null) {
                roots.add(node);
                continue;
            }

            T parent = index.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
                continue;
            }

            roots.add(node);
        }

        return roots;
    }

    /**
     * 树节点接口 — VO 实现此接口即可参与建树
     */
    public interface TreeNode<T> {
        Long getId();

        Long getParentId();

        List<T> getChildren();
    }
}
