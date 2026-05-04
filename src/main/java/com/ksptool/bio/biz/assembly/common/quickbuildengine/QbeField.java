package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class QbeField {

    //字段名称(直接从数据库获取的名称) 例: user_id
    private String fieldName;

    //用于简写的字段名称
    private String std; //标准化名称 ex: LocalUserAccount
    private String camelCase; //小驼峰名称 ex: localUserAccount
    private String pascalCase; //大驼峰名称 ex: LocalUserAccount
    private String snakeCase; //下划线名称 ex: local_user_account
    private String lowerCase; //全小写名称 ex: localuseraccount
    private String upperCase; //全大写名称 ex: LOCALUSERACCOUNT

    //字段类型
    private String type;

    //字段长度
    private Integer length;

    //字段注释
    private String comment;

    //是否必填
    private boolean required;

    //是否主键
    private boolean primaryKey;

    //排序号
    private int seq;

    //字段支持的操作
    private boolean listQuery = true;
    private boolean listView = true;
    private boolean add = true;
    private boolean edit = true;
    private boolean details = true;

    //附加字段
    private Map<String, Object> ext;

    /**
     * 构造函数
     *
     * @param fieldName 字段名称: 例如user_id
     * @param fieldType 字段类型: 例如VARCHAR
     */
    public QbeField(String fieldName, String fieldType) {

        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("字段名称不能为空!");
        }

        if (StringUtils.isBlank(fieldType)) {
            throw new IllegalArgumentException("字段类型不能为空!");
        }

        this.fieldName = fieldName;
        this.type = fieldType;

        StdName stdNameObj = StdName.of(fieldName);
        this.std = stdNameObj.getValue();
        this.camelCase = stdNameObj.toSmallCamelCase();
        this.pascalCase = stdNameObj.getValue();
        this.snakeCase = stdNameObj.toUnderLineName();
        this.lowerCase = stdNameObj.toLowerCase();
        this.upperCase = stdNameObj.toUpperCase();
    }

    /**
     * 向附加字段中添加字段
     *
     * @param key   字段名称
     * @param value 字段值
     */
    public void put(String key, Object value) {
        if (ext == null) {
            ext = new HashMap<>();
        }
        ext.put(key, value);
    }

}
