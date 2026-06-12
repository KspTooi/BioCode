package com.ksptool.bio.biz.auth.model.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatLoginDto {

    @NotBlank(message = "PAT令牌不能为空")
    @Schema(description = "PAT令牌密文(ChaCha20-Poly1305格式:密文Base64:IV-Base64)")
    private String patToken;

}
