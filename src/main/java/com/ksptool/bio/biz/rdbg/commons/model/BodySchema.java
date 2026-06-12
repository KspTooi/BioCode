package com.ksptool.bio.commons.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BodySchema {

    //原名
    private String schemaName;

    //参数列表
    private List<BodySchemaParam> params;

}
