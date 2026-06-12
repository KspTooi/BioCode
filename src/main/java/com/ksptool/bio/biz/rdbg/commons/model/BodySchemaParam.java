package com.ksptool.bio.commons.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BodySchemaParam {

    //参数名
    private String name;

    //参数类型 Schema为嵌套对象
    private String type;

    //参数描述
    private String description;

    //参数默认值
    private String defaultValue;

    //参数是否必填
    private Boolean required;

    //参数是否为数组
    private Boolean isArray;

    //参数嵌套Schema
    private BodySchema schema;

}
