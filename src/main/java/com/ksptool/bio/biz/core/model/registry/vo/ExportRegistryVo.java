package com.ksptool.bio.biz.core.model.registry.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.ksptool.bio.commons.dataprocess.AbstractImportDto;
import com.ksptool.bio.commons.dataprocess.converter.RegistryDict;
import com.ksptool.bio.commons.dataprocess.converter.RegistryDictConverter;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ExportRegistryVo extends AbstractImportDto {

    @ExcelProperty(value = "*条目Key")
    private String nkey;

    @ExcelProperty(value = "*数据类型", converter = RegistryDictConverter.class)
    @RegistryDict(keyPath = "field.core.registry.nvalueKind", fallback = "0=字串;1=整数;2=浮点;3=日期")
    private Integer nvalueKind;

    @ExcelProperty(value = "*条目Value")
    private String nvalue;

    @ExcelProperty(value = "条目标签")
    private String label;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "元数据JSON")
    private String metadata;

    @ExcelProperty(value = "*状态", converter = RegistryDictConverter.class)
    @RegistryDict(keyPath = "field.core.registry.status", fallback = "0=正常;1=停用")
    private Integer status;

    @ExcelProperty(value = "*排序")
    private String seq;

    /**
     * 验证接口参数
     *
     * @return 错误信息 无错误返回null
     */
    public String validate() {

        return null;
    }


}
