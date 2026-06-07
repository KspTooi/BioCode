package com.ksptool.bio.biz.qf.model.qftodo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetQfTodoDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "当前节点名称 (如: 财务总监审批)")
    private String nodeName;

    @Schema(description = "摘要(如：张三提交的 5000 元报销)")
    private String summary;

    @Schema(description = "办理成员类型 0:办理人, 1:候选组")
    private Integer memberType;

    @Schema(description = "办理成员ID (用户ID或用户组标识)")
    private Long memberId;

    @Schema(description = "发起人ID")
    private Long initiatorId;

    @Schema(description = "PC端路由名")
    private String routePc;

    @Schema(description = "移动端路由名")
    private String routeMobile;

    @Schema(description = "物理表数据主键ID")
     private Long dataId;

    @Schema(description = "流程ID")
    private String engProcId;

    @Schema(description = "待办状态 0:待办 1:已办 10:已作废")
    private Integer status;
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
        private Integer kind;

        /**
         * 显示名称
         */
        private String name;

    }

}
