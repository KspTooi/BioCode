package com.ksptool.bio.biz.rdbg.model.collection.dto;

import com.ksptool.bio.biz.rdbg.commons.model.RelayBody;
import com.ksptool.bio.biz.rdbg.commons.model.RelayHeader;
import com.ksptool.bio.biz.rdbg.commons.model.RelayParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
public class EditCollectionDto {

    @NotNull(message = "主键ID不能为空")
    @Schema(description = "主键ID")
    private Long id;

    @NotBlank(message = "集合名称不能为空")
    @Size(min = 1, max = 128, message = "集合名称长度必须在1-128个字符之间")
    @Schema(description = "集合名称")
    private String name;

    @Size(max = 320, message = "请求URL长度不能超过320个字符")
    @Schema(description = "请求URL")
    private String reqUrl;

    @NotNull(message = "请求URL参数不能为空")
    @Schema(description = "请求URL参数JSON")
    private List<RelayParam> requestParams;

    @Range(min = 0, max = 6, message = "请求方法只能在0-6之间")
    @Schema(description = "请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS")
    private Integer reqMethod;

    @NotNull(message = "请求头不能为空")
    @Schema(description = "请求头JSON")
    private List<RelayHeader> requestHeaders;

    @Range(min = 0, max = 8, message = "请求体类型只能在0-8之间")
    @Schema(description = "请求体类型 0:空 1:form-data 2:raw-text 3:raw-javascription 4:raw-json 5:raw-html 6:raw-xml 7:binary 8:x-www-form-urlencoded")
    private Integer reqBodyKind;

    @Schema(description = "请求体JSON")
    private RelayBody reqBody;

}

