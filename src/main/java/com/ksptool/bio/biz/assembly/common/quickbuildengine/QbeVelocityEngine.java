package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import com.ksptool.bio.biz.assembly.model.opschema.OpSchemaPo;
import com.ksptool.text.PreparedPrompt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Slf4j
public class QbeVelocityEngine {

    //velocity引擎实例
    private VelocityEngine velocityEngine;

    //输出基准路径
    private String outputBasePath;

    //文件覆盖开关
    private boolean overwriteEnabled = false;

    //全局变量 这些变量将在所有模板中可用 它们将会挂载在global下 例如putGlobalVar("name", "value") 将可在所有模板中使用${global.name}获取
    private Map<String, String> globalVars = new HashMap<>();


    public QbeVelocityEngine() {
        initVelocityEngine();
    }

    private void initVelocityEngine() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        velocityEngine.init();

        Properties props = new Properties();
        props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        Velocity.init(props);
    }

    /**
     * 设置全局变量
     * 全局变量将在所有模板中可用 它们将会挂载在global下 例如putGlobalVar("name", "value") 将可在所有模板中使用${global.name}获取
     *
     * @param key   变量名，不能为空
     * @param value 变量值，不能为空
     */
    public void putGlobalVar(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("全局变量名不能为空!");
        }
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("全局变量值不能为空!");
        }
        globalVars.put(key, value);
        log.info("设置全局变量：{} = {}", key, value);
    }

    /**
     * 设置文件覆盖开关
     *
     * @param overwriteEnabled true:覆盖 false:不覆盖
     */
    public void setOverwriteEnabled(boolean overwriteEnabled) {
        this.overwriteEnabled = overwriteEnabled;
    }

    /**
     * 设置输出基准路径(绝对路径)
     * 如果路径已被手动设置，则后续调用将被忽略
     *
     * @param absolutePath 绝对路径
     */
    public void setOutputBasePath(String absolutePath) {

        if (StringUtils.isBlank(absolutePath)) {
            throw new IllegalArgumentException("输出基准路径不能为空!");
        }

        Path path = Paths.get(absolutePath).normalize();
        this.outputBasePath = path.toAbsolutePath().toString();
        log.info("设置输出基准路径(绝对路径)：{}", this.outputBasePath);
    }


    /**
     * 渲染蓝图
     *
     * @param model      QBE模型
     * @param schema     输出方案
     * @param blueprints 蓝图列表
     */
    public void render(QbeModel model, OpSchemaPo schema, List<QbeBlueprint> blueprints) {

        if (model == null) {
            throw new IllegalArgumentException("模型不能为空!");
        }
        if (blueprints == null || blueprints.isEmpty()) {
            throw new IllegalArgumentException("蓝图列表不能为空!");
        }

        if (StringUtils.isBlank(outputBasePath)) {
            throw new IllegalArgumentException("输出基准路径不能为空!");
        }

        //用于路径解析的参数
        Map<String, String> params = new HashMap<>();
        params.put("tableName", model.getTableName());
        params.put("std", model.getStd());
        params.put("camelCase", model.getCamelCase());
        params.put("pascalCase", model.getPascalCase());
        params.put("snakeCase", model.getSnakeCase());
        params.put("lowerCase", model.getLowerCase());
        params.put("upperCase", model.getUpperCase());
        params.put("bizDomain", schema.getBizDomain());

        //渲染蓝图
        for (QbeBlueprint blueprint : blueprints) {

            //先解析蓝图输出路径和输出名
            PreparedPrompt pathPrompt = PreparedPrompt.prepare(blueprint.getRelativePathWithPlaceholder());
            pathPrompt.setParameters(params);
            String resolvedRelativePath = pathPrompt.execute(false);

            PreparedPrompt fileNamePrompt = PreparedPrompt.prepare(blueprint.getFileNameWithPlaceholder());
            fileNamePrompt.setParameters(params);
            String resolvedFileName = fileNamePrompt.execute(false);

            if (resolvedFileName.endsWith(".vm")) {
                resolvedFileName = resolvedFileName.substring(0, resolvedFileName.length() - 3);
            }

            Path resolvedRelativePathObj = Paths.get(resolvedRelativePath);
            Path resolvedDir = resolvedRelativePathObj.getParent();
            if (resolvedDir == null) {
                resolvedDir = Paths.get("");
            }

            Path outputFilePath = Paths.get(outputBasePath).resolve(resolvedDir).resolve(resolvedFileName).normalize();

            if (!outputFilePath.startsWith(Paths.get(outputBasePath))) {
                throw new RuntimeException("输出路径超出基准目录范围：" + outputFilePath);
            }

            String templateContent = blueprint.getTemplateContent();
            if (StringUtils.isBlank(templateContent)) {
                log.warn("蓝图模板内容为空，跳过处理");
                return;
            }

            StringWriter writer = new StringWriter();

            //构建VelocityContext
            VelocityContext vc = buildVc(model, schema, globalVars);

            //渲染模板
            boolean success = Velocity.evaluate(vc, writer, "template", templateContent);
            if (!success) {
                throw new RuntimeException("无法渲染模板，蓝图：" + blueprint.getFileName());
            }
            String renderedContent = writer.toString();
            //写入文件
            try {
                Files.createDirectories(outputFilePath.getParent());
                if (Files.exists(outputFilePath)) {
                    if (overwriteEnabled) {
                        log.info("文件已存在，覆盖文件：{}", outputFilePath);

                        Files.writeString(outputFilePath, renderedContent);
                        log.info("已覆盖文件：{}", outputFilePath);
                    }
                    if (!overwriteEnabled) {
                        log.warn("文件已存在，跳过处理：{}", outputFilePath);
                        return;
                    }
                }
                Files.writeString(outputFilePath, renderedContent);
                log.info("已写入文件：{}", outputFilePath);
            } catch (IOException e) {
                throw new RuntimeException("无法写入文件：" + outputFilePath, e);
            }

        }
    }


    /**
     * 渲染模板为字符串
     *
     * @param templateContent 模板内容
     * @param model           QBE模型
     * @param globalVars      全局变量
     * @return 渲染后的字符串
     */
    public String renderAsString(String templateContent, QbeModel model, OpSchemaPo schema, Map<String, String> globalVars) {

        if (StringUtils.isBlank(templateContent)) {
            throw new IllegalArgumentException("模板内容不能为空!");
        }
        if (model == null) {
            throw new IllegalArgumentException("模型不能为空!");
        }

        //构建VelocityContext
        VelocityContext vc = buildVc(model, schema, globalVars);
        StringWriter writer = new StringWriter();

        //渲染模板
        boolean success = Velocity.evaluate(vc, writer, "template", templateContent);
        if (!success) {
            throw new RuntimeException("无法渲染模板内容");
        }

        return writer.toString();
    }


    /**
     * 构建VelocityContext
     *
     * @param model      模型
     * @param GlobalVars 全局变量
     * @return VelocityContext 包含模型和全局变量的VelocityContext
     */
    public VelocityContext buildVc(QbeModel model, OpSchemaPo schema, Map<String, String> GlobalVars) {
        //渲染模板 先把modelMap放进VC
        VelocityContext vc = new VelocityContext();

        if (model != null) {
            vc.put("model", model);
        }

        if (schema != null) {
            vc.put("schema", schema);
        }

        if (GlobalVars != null) {
            vc.put("global", GlobalVars);
        }

        return vc;
    }

}
