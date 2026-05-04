package com.ksptool.bio.biz.assembly.common.quickbuildengine;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Slf4j
@Getter
public class QbeBlueprintReader {

    /**
     * 蓝图仓库路径
     */
    private Path repositoryPath;

    /**
     * 蓝图扩展名
     */
    @Setter
    private String blueprintExtension = "vm";


    /**
     * 构造函数
     *
     * @param repositoryPath 蓝图仓库路径
     * @throws IllegalArgumentException 蓝图仓库路径不存在或不是目录
     */
    public QbeBlueprintReader(String repositoryPath) {
        this.repositoryPath = Paths.get(repositoryPath);

        if (!Files.exists(this.repositoryPath)) {
            throw new IllegalArgumentException("蓝图仓库路径不存在：" + repositoryPath);
        }

        if (!Files.isDirectory(this.repositoryPath)) {
            throw new IllegalArgumentException("蓝图仓库路径不是目录：" + repositoryPath);
        }

    }

    /**
     * 读取蓝图
     *
     * @return 蓝图列表
     * @throws IOException 读取文件失败
     */
    public List<QbeBlueprint> readBlueprint() throws IOException {

        log.info("开始读取蓝图，蓝图仓库路径：{}，扩展名：{}", repositoryPath, blueprintExtension);

        var blueprints = new ArrayList<QbeBlueprint>();

        try (Stream<Path> paths = Files.walk(repositoryPath)) {
            List<Path> matchedPaths = paths.filter(Files::isRegularFile)
                    .filter(this::matchesExtension)
                    .toList();
            for (Path path : matchedPaths) {
                QbeBlueprint blueprint = createQbeBlueprint(path);
                if (blueprint != null) {
                    blueprints.add(blueprint);
                }
            }
        }

        return blueprints;
    }

    /**
     * 判断是否为蓝图文件
     *
     * @param path 文件路径
     * @return 是否为蓝图文件
     */
    private boolean matchesExtension(Path path) {
        String fileName = path.getFileName().toString();
        if (StringUtils.isBlank(fileName)) {
            return false;
        }

        //解析文件扩展名
        int lastDotIndex = fileName.lastIndexOf('.');

        //如果扩展名不存在或者扩展名是文件名的一部分，则不是蓝图文件
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return false;
        }

        var extension = fileName.substring(lastDotIndex + 1);
        return blueprintExtension.equalsIgnoreCase(extension);
    }

    /**
     * 创建蓝图对象
     *
     * @param path 文件路径
     * @return 蓝图对象
     * @throws IOException 读取文件失败
     */
    private QbeBlueprint createQbeBlueprint(Path path) throws IOException {
        QbeBlueprint blueprint = new QbeBlueprint();

        String templateContent = Files.readString(path);

        //解析蓝图内容
        blueprint.setTemplateContent(templateContent);
        blueprint.setSha256Hex(DigestUtils.sha256Hex(templateContent));
        blueprint.setFileName(path.getFileName().toString());

        //设置绝对路径和基准路径
        blueprint.setAbsoluteFilePath(path.toAbsolutePath().toString());
        blueprint.setBasePath(repositoryPath.toAbsolutePath().toString());

        //设置相对路径
        Path relativePath = repositoryPath.relativize(path);
        blueprint.setRelativeFilePath(relativePath.toString());

        // 将相对路径和文件名中的反斜杠统一为正斜杠，便于模板解析
        String relativePathStr = relativePath.toString().replace('\\', '/');
        blueprint.setRelativePathWithPlaceholder(relativePathStr);
        blueprint.setFileNameWithPlaceholder(path.getFileName().toString());

        //设置最终输出文件名和路径
        blueprint.setOutputFileName(null);
        blueprint.setOutputFilePath(null);
        return blueprint;
    }

}
