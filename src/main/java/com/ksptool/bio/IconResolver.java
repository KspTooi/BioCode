package com.ksptool.bio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * 解析 Iconify JSON 文件，将图标名输出为纯文本（每行一个，格式："prefix:name",）
 * 用法：将 input 指向 @iconify/json/json/xxx.json，运行 main 即可
 */
public class IconResolver {

    public static void main(String[] args) throws Exception {

        // 输入：@iconify/json 里的某个集合 JSON，例如 mdi.json
        var input = new File("mdi.json");

        // 输出：纯文本，每行一条，直接粘贴到 TS 数组中
        var output = new File("mdi_icons.txt");

        // 从文件名取前缀（去掉 .json）
        var fileName = input.getName();
        var prefix = fileName.substring(0, fileName.lastIndexOf('.'));

        JsonObject root;
        try (var reader = new FileReader(input)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        var icons = root.getAsJsonObject("icons");
        if (icons == null) {
            System.err.println("JSON 中未找到 icons 字段");
            return;
        }

        try (var writer = new PrintWriter(new FileWriter(output))) {
            for (var entry : icons.entrySet()) {
                writer.println("  \"" + prefix + ":" + entry.getKey() + "\",");
            }
        }

        System.out.println("完成，共 " + icons.size() + " 个图标，输出至：" + output.getAbsolutePath());
    }
}

