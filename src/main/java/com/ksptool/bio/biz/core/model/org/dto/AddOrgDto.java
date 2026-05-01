package com.ksptool.bio.biz.core.model.org.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class AddOrgDto implements DtoCustomValidator {

    @Schema(description = "上级组织ID NULL顶级组织")
    private Long parentId;

    @NotNull(message = "组织机构类型不能为空")
    @Range(min = 0, max = 2, message = "组织机构类型必须在0-2之间")
    @Schema(description = "0:企业 1:子企业 2:部门")
    private Integer kind;

    @NotNull(message = "组织机构名称不能为空")
    @Size(max = 80, message = "组织机构名称长度不能超过80个字符")
    @Schema(description = "组织机构名称")
    private String name;

    @Length(max = 40, message = "组织机构简称长度不能超过40个字符")
    @Schema(description = "组织机构简称")
    private String shortName;

    @NotNull(message = "排序不能为空")
    @Range(min = 0, max = 655350, message = "排序只能在0-655350之间")
    @Schema(description = "排序")
    private Integer seq;

    @Schema(description = "备注")
    @Length(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    /**
     * 验证
     *
     * @return
     */
    @Override
    public String validate() {

        //新建企业不能有父级
        if (kind == 0) {
            if (parentId != null) {
                return "新建企业不能有父级";
            }
        }




        //企业(租户)不允许填写主管ID
        if (kind == 0) {
/*             if (principalId != null) {
                return "企业(租户)不允许填主管ID";
            } */
        }

        //1:子企业 2:部门 必须有父级
        if (kind == 1 || kind == 2 || kind == 3) {
            if (parentId == null) {
                return "新增子企业、部门时必须填写上级组织ID";
            }
        }

        return null;
    }
}

