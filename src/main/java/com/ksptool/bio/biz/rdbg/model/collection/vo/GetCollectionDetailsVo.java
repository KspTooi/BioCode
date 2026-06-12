package com.ksptool.bio.biz.rdbg.model.collection.vo;

import com.ksptool.bio.biz.rdbg.commons.model.RelayBody;
import com.ksptool.bio.biz.rdbg.commons.model.RelayHeader;
import com.ksptool.bio.biz.rdbg.commons.model.RelayParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetCollectionDetailsVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父级ID NULL顶级")
    private Long parentId;

    @Schema(description = "集合名称")
    private String name;

    @Schema(description = "集合类型 0:请求 1:组")
    private Integer kind;

    @Schema(description = "请求URL")
    private String reqUrl;

    @Schema(description = "请求URL参数JSON")
    private List<RelayParam> requestParams;

    @Schema(description = "请求方法 0:GET 1:POST 2:PUT 3:PATCH 4:DELETE 5:HEAD 6:OPTIONS")
    private Integer reqMethod;

    @Schema(description = "请求头")
    private List<RelayHeader> requestHeaders;

    @Schema(description = "请求体类型 0:空 1:form-data 2:raw-text 3:raw-javascription 4:raw-json 5:raw-html 6:raw-xml 7:binary 8:x-www-form-urlencoded")
    private Integer reqBodyKind;

    @Schema(description = "请求体")
    private RelayBody reqBody;

}

