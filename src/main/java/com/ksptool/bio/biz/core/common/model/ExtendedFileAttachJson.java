package com.ksptool.bio.biz.core.common.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class ExtendedFileAttachJson {

    @NotNull(message = "附件ID不可为空")
    private Long i;

    @NotBlank(message = "path不可为空")
    private String p;

    @NotBlank(message = "name不可为空")
    private String n;

    @NotNull(message = "上传时间不可为空")
    private LocalDateTime t;

    @NotNull(message = "文件大小不可为空")
    private Long l;

}
