package com.ksptool.bio.biz.auth.model.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatLoginDto {

    @NotBlank(message = "PAT令牌不能为空")
    @Size(max = 500, message = "PAT令牌长度不能超过500")
    @Schema(description = "PAT令牌密文(ChaCha20-Poly1305格式:密文Base64:IV-Base64)")
    private String patToken;

}
