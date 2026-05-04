package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import com.alibaba.excel.util.StringUtils;
import com.ksptool.text.PreparedPrompt;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class QbeBlueprint {

    //蓝图文件名
    private String fileName;

    //蓝图SHA256
    private String sha256Hex;

    //绝对路径
    private String absoluteFilePath;

    //相对路径(相对于基准路径)
    private String relativeFilePath;

    //基准路径(蓝图仓库路径)
    private String basePath;

    //模板内容(从.vm文件读取)
    private String templateContent;

    //包含占位符的相对路径(相对于基准路径)
    private String relativePathWithPlaceholder;

    //包含占位符的文件名
    private String fileNameWithPlaceholder;

    //最终输出文件名(占位符已解析)
    private String outputFileName;

    //最终输出路径(包含文件名,占位符已解析)
    private String outputFilePath;

    /**
     * 解析路径
     * 以给定的模型解析相对路径和文件名，以替换掉文件名和路径中的占位符
     * 例如: /#{tableName}/#{std}/#{camelCase}.java.vm 将被解析为 /local_user_account/LocalUserAccount/localUserAccount.java
     *
     * @param model 模型
     */
    public void resolvePath(QbeModel model) {

        if (model == null) {
            throw new IllegalArgumentException("模型不能为空!");
        }

        var params = new HashMap<String, String>();
        params.put("tableName", model.getTableName());
        params.put("std", model.getStd());
        params.put("camelCase", model.getCamelCase());
        params.put("pascalCase", model.getPascalCase());
        params.put("snakeCase", model.getSnakeCase());
        params.put("lowerCase", model.getLowerCase());
        params.put("upperCase", model.getUpperCase());
        params.put("bizDomain", model.getBizDomain());
        resolvePath(params);
    }


    /**
     * 解析路径
     * 以给定的参数解析相对路径和文件名，以替换掉文件名和路径中的占位符
     * 例如: /#{tableName}/#{std}/#{camelCase}.java.vm 将被解析为 /local_user_account/LocalUserAccount/localUserAccount.java
     *
     * @param params 解析参数
     */
    public void resolvePath(Map<String, String> params) {

        if (StringUtils.isBlank(relativePathWithPlaceholder)) {
            return;
        }
        if (StringUtils.isBlank(fileNameWithPlaceholder)) {
            return;
        }

        //解析相对路径
        PreparedPrompt pathPrompt = PreparedPrompt.prepare(this.getRelativePathWithPlaceholder());
        pathPrompt.setParameters(params);
        String resolvedRelativePath = pathPrompt.execute(false);

        //解析文件名
        PreparedPrompt fileNamePrompt = PreparedPrompt.prepare(this.getFileNameWithPlaceholder());
        fileNamePrompt.setParameters(params);
        String resolvedFileName = fileNamePrompt.execute(false);

        //如果文件名以.vm结尾，则去掉.vm
        if (resolvedFileName.endsWith(".vm")) {
            resolvedFileName = resolvedFileName.substring(0, resolvedFileName.length() - 3);
        }

        //设置最终输出文件名和路径
        this.setOutputFileName(resolvedFileName);
        this.setOutputFilePath(resolvedRelativePath);
    }

    /**
     * 获取相对目录路径
     * 只保留目录部分，去除文件名，兼容Windows和Linux路径分隔符
     *
     * @return 相对目录路径，如果为空则返回null
     */
    public String getRelativeDirectoryPath() {
        if (StringUtils.isBlank(relativeFilePath)) {
            return null;
        }

        String normalizedPath = relativeFilePath.replace('\\', '/');
        int lastIndex = normalizedPath.lastIndexOf('/');

        if (lastIndex == -1) {
            return "";
        }

        if (lastIndex == 0) {
            return "/";
        }

        return normalizedPath.substring(0, lastIndex);
    }


}
