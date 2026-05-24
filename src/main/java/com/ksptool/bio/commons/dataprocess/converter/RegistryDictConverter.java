package com.ksptool.bio.commons.dataprocess.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.ksptool.bio.biz.core.model.registry.RegistryPo;
import com.ksptool.bio.biz.core.service.RegistrySdk;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RegistryDictConverter implements Converter<Integer> {

    @Setter
    private static RegistrySdk registrySdk;

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 读数据：Excel (String) -> Java (Integer)
     */
    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String excelValue = cellData.getStringValue();

        RegistryDict annotation = contentProperty.getField().getAnnotation(RegistryDict.class);
        if (annotation == null) {
            return StringUtils.isNotBlank(excelValue) ? Integer.parseInt(excelValue) : null;
        }

        if (StringUtils.isEmpty(excelValue)) {
            if (annotation.required()) {
                String errorMsg = StringUtils.isEmpty(annotation.message()) ? "该字段不能为空" : annotation.message();
                throw new ExcelAnalysisException(errorMsg);
            }
            return null;
        }

        String key = excelValue.trim();

        // 尝试从注册表获取映射
        Map<String, String> readMap = getReadMap(annotation);
        if (readMap != null && !readMap.isEmpty()) {
            Integer result = parseInteger(readMap.get(key));
            if (result != null) {
                return result;
            }
        }

        // 尝试使用后备format
        if (StringUtils.isNotEmpty(annotation.fallback())) {
            Map<String, Integer> fallbackMap = parseFormatForRead(annotation.fallback());
            Integer result = fallbackMap.get(key);
            if (result != null) {
                return result;
            }
        }

        // 直接尝试解析为数字
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException e) {
            throw new ExcelAnalysisException("解析失败，值【" + key + "】不在允许的范围内");
        }
    }

    /**
     * 写数据：Java (Integer) -> Excel (String)
     */
    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }

        RegistryDict annotation = contentProperty.getField().getAnnotation(RegistryDict.class);
        if (annotation == null) {
            return new WriteCellData<>(String.valueOf(value));
        }

        // 尝试从注册表获取映射
        Map<Integer, String> writeMap = getWriteMap(annotation);
        if (writeMap != null && !writeMap.isEmpty()) {
            String label = writeMap.get(value);
            if (label != null) {
                return new WriteCellData<>(label);
            }
        }

        // 尝试使用后备format
        if (StringUtils.isNotEmpty(annotation.fallback())) {
            Map<Integer, String> fallbackMap = parseFormatForWrite(annotation.fallback());
            String label = fallbackMap.get(value);
            if (label != null) {
                return new WriteCellData<>(label);
            }
        }

        return new WriteCellData<>(String.valueOf(value));
    }

    /**
     * 获取注册表读取映射: label -> nkey
     * 直接委托RegistrySdk，由其Spring Cache保证缓存
     */
    private Map<String, String> getReadMap(RegistryDict annotation) {
        List<RegistryPo> entries = loadEntries(annotation.keyPath());
        if (entries == null || entries.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        for (RegistryPo po : entries) {
            if (po.getLabel() != null && po.getNkey() != null) {
                map.put(po.getLabel(), po.getNkey());
            }
        }
        return map;
    }

    /**
     * 获取注册表写入映射: nvalue(整数) -> label
     * 直接委托RegistrySdk，由其Spring Cache保证缓存
     */
    private Map<Integer, String> getWriteMap(RegistryDict annotation) {
        List<RegistryPo> entries = loadEntries(annotation.keyPath());
        if (entries == null || entries.isEmpty()) {
            return Map.of();
        }
        Map<Integer, String> map = new HashMap<>();
        for (RegistryPo po : entries) {
            if (po.getNvalue() != null && po.getLabel() != null) {
                Integer nvalue = parseInteger(po.getNvalue());
                if (nvalue != null) {
                    map.put(nvalue, po.getLabel());
                }
            }
        }
        return map;
    }

    /**
     * 从RegistrySdk加载注册表条目（已有Spring Cache缓存）
     */
    private List<RegistryPo> loadEntries(String keyPath) {
        if (registrySdk == null) {
            log.warn("RegistryDictConverter: RegistrySdk未注入，Spring容器未就绪");
            return null;
        }
        return registrySdk.getEntries(keyPath);
    }

    /**
     * 安全解析Integer
     */
    private Integer parseInteger(String str) {
        if (str == null) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析后备format用于读取: "0=正常;1=停用" -> Map<"正常", 0>
     */
    private Map<String, Integer> parseFormatForRead(String format) {
        Map<String, Integer> map = new HashMap<>();
        if (StringUtils.isEmpty(format)) {
            return map;
        }
        String[] entries = format.split(";");
        for (String entry : entries) {
            String[] kv = entry.split("=");
            if (kv.length == 2) {
                try {
                    Integer code = Integer.parseInt(kv[0].trim());
                    map.put(kv[1].trim(), code);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    /**
     * 解析后备format用于写入: "0=正常;1=停用" -> Map<0, "正常">
     */
    private Map<Integer, String> parseFormatForWrite(String format) {
        Map<Integer, String> map = new HashMap<>();
        if (StringUtils.isEmpty(format)) {
            return map;
        }
        String[] entries = format.split(";");
        for (String entry : entries) {
            String[] kv = entry.split("=");
            if (kv.length == 2) {
                try {
                    Integer code = Integer.parseInt(kv[0].trim());
                    map.put(code, kv[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }
}
