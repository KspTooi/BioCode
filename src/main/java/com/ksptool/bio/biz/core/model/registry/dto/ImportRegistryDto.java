package com.ksptool.bio.biz.core.model.registry.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.ksptool.bio.commons.dataprocess.AbstractImportDto;
import com.ksptool.bio.commons.dataprocess.Str;
import com.ksptool.bio.commons.dataprocess.converter.RegistryDict;
import com.ksptool.bio.commons.dataprocess.converter.RegistryDictConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 导入注册表条目DTO
 * <p>
 * 导入字段说明：
 * 1.*nkey 节点Key
 * 2.nvalueKind 数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
 * 3.nvalue 条目Value
 * 4.label 条目标签
 * 5.remark 说明
 * 6.metadata 元数据JSON
 * 7.status 状态 0:正常 1:停用
 * 8.*seq 排序
 */
@Getter
@Setter
public class ImportRegistryDto extends AbstractImportDto {

    @ExcelProperty(value = "*条目Key")
    private String nkey;

    @ExcelProperty(value = "*数据类型", converter = RegistryDictConverter.class)
    @RegistryDict(keyPath = "field.core.registry.nvalueKind", fallback = "0=字串;1=整数;2=浮点;3=日期", required = true, message = "数据类型不能为空")
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
    @RegistryDict(keyPath = "field.core.registry.status", fallback = "0=正常;1=停用", required = true, message = "状态不能为空")
    private Integer status;

    @ExcelProperty(value = "*排序")
    private String seq;

    /**
     * 验证接口参数
     *
     * @return 错误信息 无错误返回null
     */
    public String validate() {
        // 校验节点Key
        if (Str.isBlank(nkey)) {
            return "节点Key不能为空";
        }

        if (nkey.length() > 128) {
            return "节点Key长度不能超过128个字符";
        }

        if (!nkey.matches("^[a-zA-Z0-9_\\-]+$")) {
            return "节点Key只能包含字母、数字、下划线或中划线";
        }

        // 校验排序
        if (Str.isBlank(seq)) {
            return "排序不能为空";
        }

        if (Str.isNotInteger(seq)) {
            return "排序必须为整数 当前值:" + seq;
        }

        // 校验数据类型（RegistryDictConverter已转换，校验范围）
        if (nvalueKind == null) {
            return "数据类型不能为空";
        }

        if (nvalueKind < 0 || nvalueKind > 3) {
            return "数据类型只能为0(字串)、1(整数)、2(浮点)或3(日期) 当前值:" + nvalueKind;
        }

        // 如果填写了数据类型，则必须填写Value
        if (nvalueKind != null && Str.isBlank(nvalue)) {
            return "填写了数据类型时必须填写Value";
        }

        // 如果填写了Value，则必须填写数据类型
        if (Str.isNotBlank(nvalue) && nvalueKind == null) {
            return "填写了Value时必须填写数据类型";
        }

        // 校验Value长度
        if (Str.isNotBlank(nvalue) && nvalue.length() > 1024) {
            return "节点Value长度不能超过1024个字符";
        }

        // 根据数据类型校验Value格式
        if (nvalueKind != null && Str.isNotBlank(nvalue)) {

            if (nvalueKind == 1 && Str.isNotInteger(nvalue)) {
                return "值必须为整数 当前值:" + nvalue;
            }

            if (nvalueKind == 2 && Str.isNotDouble(nvalue)) {
                return "值必须为浮点数 当前值:" + nvalue;
            }

            if (nvalueKind == 3 && Str.isNotDateTime(nvalue)) {
                return "值必须为日期时间(yyyy-MM-dd HH:mm:ss) 当前值:" + nvalue;
            }
        }

        // 校验节点标签
        if (Str.isNotBlank(label) && label.length() > 32) {
            return "节点标签长度不能超过32个字符";
        }

        // 校验说明
        if (Str.isNotBlank(remark) && remark.length() > 1000) {
            return "说明长度不能超过1000个字符";
        }

        // 处理元数据默认值
        if (Str.isBlank(metadata)) {
            metadata = "{}";
        }

        // 校验状态（RegistryDictConverter已转换，校验范围）
        if (status == null) {
            return "状态不能为空";
        }

        if (status != 0 && status != 1) {
            return "状态只能为0(正常)或1(停用) 当前值:" + status;
        }

        return null;
    }


}
