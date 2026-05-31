package com.ksptool.bio.biz.qf.model.qftodo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QfTodoFormConfigVo {
    /**
     * 是否允许填写审批意见: 0=不允许, 1=允许
     */
    private Integer allowComment;

    /**
     * 操作按钮配置列表
     */
    private List<OperationConfig> allowActions = new ArrayList<>();

    /**
     * 允许编辑的表单字段列表
     */
    private List<String> allowEditFields = new ArrayList<>();

    @Data
    public static class OperationConfig {
        /**
         * 操作类型: 0=同意, 1=驳回, 2=转交, 3=驳回节点
         */
        private Integer actionType;

        /**
         * 显示名称
         */
        private String displayName;

    }
}