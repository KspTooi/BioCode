package com.ksptool.bio.biz.qf.model.qfmodel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetQfModelListVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模型分组")
    private String groupName;


    @Schema(description = "模型名称")
    private String name;

    @Schema(description = "模型编码")
    private String code;

    @Schema(description = "关联业务表单名称")
    private String bizFormName;
    @Schema(description = "关联业务表单编码")
    private String bizFormCode;

    @Schema(description = "模型版本号")
    private Integer version;

    @Schema(description = "模型状态 0:草稿 1:已部署 2:历史")
    private Integer status;

    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
