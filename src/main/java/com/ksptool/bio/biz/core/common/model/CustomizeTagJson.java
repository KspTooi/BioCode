package com.ksptool.bio.biz.core.common.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Getter
@Setter
public class CustomizeTagJson {
    @NotBlank(message = "name不可为空")
    private String n;
}
