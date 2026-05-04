package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QBE模型
 * 这个模型用于存储提供给QBE引擎的全部参数
 *
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class QbeModel {

    //表名: 例如local_user_account
    private String tableName;

    //业务域: 例如user
    private String bizDomain;

    //表注释
    private String comment;

    //用于简写模型名称
    private String std; //标准化名称 ex: LocalUserAccount
    private String camelCase; //小驼峰名称 ex: localUserAccount
    private String pascalCase; //大驼峰名称 ex: LocalUserAccount
    private String snakeCase; //下划线名称 ex: local_user_account
    private String lowerCase; //全小写名称 ex: localuseraccount
    private String upperCase; //全大写名称 ex: LOCALUSERACCOUNT

    //字段列表
    private List<QbeField> fields;

    //附加字段
    private Map<String, Object> ext;


    /**
     * 构造函数
     *
     * @param tableName 表名: 例如local_user_account
     * @param modelName 模型名称: 例如LocalUserAccount
     */
    public QbeModel(String tableName, String modelName) {
        this.tableName = tableName;
        this.std = StdName.of(modelName).getValue();
        this.camelCase = StdName.of(modelName).toSmallCamelCase();
        this.pascalCase = StdName.of(modelName).getValue();
        this.snakeCase = StdName.of(modelName).toUnderLineName();
        this.lowerCase = StdName.of(modelName).toLowerCase();
        this.upperCase = StdName.of(modelName).toUpperCase();
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
