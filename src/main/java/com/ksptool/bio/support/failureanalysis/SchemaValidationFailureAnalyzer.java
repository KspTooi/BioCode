package com.ksptool.bio.support.failureanalysis;


import org.hibernate.tool.schema.spi.SchemaManagementException;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;

import org.springframework.boot.diagnostics.FailureAnalysis;


import java.util.regex.Matcher;

import java.util.regex.Pattern;


public class SchemaValidationFailureAnalyzer extends AbstractFailureAnalyzer<SchemaManagementException> {


    // 预编译正则表达式以提取 Hibernate 报错中的关键信息

    private static final Pattern WRONG_TYPE_PATTERN = Pattern.compile("wrong column type encountered in column \\[(.*?)\\] in table \\[(.*?)\\]; found \\[(.*?)\\], but expecting \\[(.*?)\\]");

    private static final Pattern MISSING_TABLE_PATTERN = Pattern.compile("missing table \\[(.*?)\\]");

    private static final Pattern MISSING_COLUMN_PATTERN = Pattern.compile("missing column \\[(.*?)\\] in table \\[(.*?)\\]");

    private static final Pattern MISSING_SEQUENCE_PATTERN = Pattern.compile("missing sequence \\[(.*?)\\]");

    private static final String DEFAULT_ACTION = """
            请检查您的实体类 (Entity) 注解与数据库实际结构的匹配情况。
            """;


    @Override

    protected FailureAnalysis analyze(@NonNull Throwable rootFailure, SchemaManagementException cause) {

        String originalMessage = cause.getMessage();

        if (originalMessage == null) {

            String description = """            
                    数据库结构校验失败 (Schema Management Exception)！
                    原始报错: %s           
                    """.formatted(originalMessage);

            return new FailureAnalysis(description, DEFAULT_ACTION, cause);
        }

        Matcher wrongTypeMatcher = WRONG_TYPE_PATTERN.matcher(originalMessage);

        if (wrongTypeMatcher.find()) {

            String column = wrongTypeMatcher.group(1);
            String table = wrongTypeMatcher.group(2);
            String found = wrongTypeMatcher.group(3);
            String expecting = wrongTypeMatcher.group(4);


            String description = """             
                    【字段类型不匹配】      
                    表 [%s] 中的字段 [%s] 类型与 Java 实体类不一致。 
                    - 🔍 数据库实际类型: %s
                    - 💻 实体类期望类型: %s
                    """.formatted(table, column, found, expecting);

            String action = """            
                    👉 解决方案:   
                    1. 修改实体类属性类型，使其与数据库的 %s 类型兼容。
                    2. 在属性上使用 @Column(columnDefinition = "%s") 强制匹配。
                    3. 修改数据库表结构以匹配 %s。
                    """.formatted(found, found, expecting);

            return new FailureAnalysis(description, action, cause);
        }


        Matcher missingTableMatcher = MISSING_TABLE_PATTERN.matcher(originalMessage);

        if (missingTableMatcher.find()) {

            String table = missingTableMatcher.group(1);


            String description = """                  
                    【缺少表】
                    在数据库中找不到实体类对应的表 [%s]。              
                    """.formatted(table);

            String action = """                 
                    👉 解决方案:
                    1. 请确认数据库中是否已建立名为 `%s` 的表。                
                    2. 如果数据库中的表名与类名不同，请在实体类上加上 @Table(name = "实际表名") 注解。            
                    """.formatted(table);


            return new FailureAnalysis(description, action, cause);

        }


        Matcher missingColumnMatcher = MISSING_COLUMN_PATTERN.matcher(originalMessage);

        if (missingColumnMatcher.find()) {

            String column = missingColumnMatcher.group(1);
            String table = missingColumnMatcher.group(2);

            String description = """          
                    【缺少字段】     
                    在数据库的表 [%s] 中找不到对应的字段 [%s]。           
                    """.formatted(table, column);

            String action = """
                    👉 解决方案:
                    1. 请确认表 `%s` 中是否存在列 `%s`。
                    2. 如果数据库中列名使用了不同的命名规范（如驼峰和下划线不一致），请使用 @Column(name = "实际列名")。
                    3. 如果该属性只是业务中间变量，不需要存入数据库，请加上 @Transient 注解。  
                    """.formatted(table, column);

            return new FailureAnalysis(description, action, cause);
        }


        Matcher missingSequenceMatcher = MISSING_SEQUENCE_PATTERN.matcher(originalMessage);

        if (missingSequenceMatcher.find()) {

            String sequence = missingSequenceMatcher.group(1);


            String description = """    
                    【缺少序列】
                    在数据库中找不到自增序列 (Sequence) [%s]。
                    """.formatted(sequence);

            String action = """                
                    👉 解决方案:
                    请在数据库中创建名为 `%s` 的序列，或者检查实体类的主键生成策略（如 @GeneratedValue）是否配置正确。
                    """.formatted(sequence);

            return new FailureAnalysis(description, action, cause);

        }

        String description = """
                数据库结构校验失败 (Schema Management Exception)！
                原始报错: %s
                """.formatted(originalMessage);

        return new FailureAnalysis(description, DEFAULT_ACTION, cause);

    }


}

