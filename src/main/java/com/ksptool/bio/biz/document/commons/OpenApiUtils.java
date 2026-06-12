package com.ksptool.bio.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class OpenApiUtils {

    /**
     * 判断OpenAPI是否是完整的文档
     *
     * @param openAPI 文档
     * @return 是否是完整的文档 true:完整 false:不完整
     */
    public static boolean isComplete(OpenAPI openAPI) {
        if (openAPI == null) {
            return false;
        }
        if (openAPI.getInfo() == null) {
            return false;
        }
        if (StringUtils.isBlank(openAPI.getInfo().getTitle())) {
            return false;
        }
        return true;
    }

    /**
     * 获取OpenAPI的MD5值
     *
     * @param openAPI 文档
     * @return MD5值 如果文档不完整返回null
     */
    public static String getHash(OpenAPI openAPI) {
        if (openAPI == null) {
            return null;
        }
        if (!isComplete(openAPI)) {
            return null;
        }
        try {
            String json = Json.mapper().writeValueAsString(openAPI);
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            log.error("获取OpenAPI的MD5值失败:{}", e.getMessage(), e);
            return null;
        }
    }

    public static String toJson(OpenAPI openAPI) {
        if (openAPI == null) {
            return null;
        }
        try {
            return Json.mapper().writeValueAsString(openAPI);
        } catch (JsonProcessingException e) {
            log.error("转换OpenAPI为JSON失败:{}", e.getMessage(), e);
            return null;
        }
    }


}
