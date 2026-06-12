package com.ksptool.bio.biz.auth.model.basicpat.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddBasicPatDto implements DtoCustomValidator {

    @NotNull(message = "PAT名称不能为空")
    @Size(max = 40, message = "PAT名称长度不能超过40")
    @Schema(description = "PAT名称")
    private String name;

    @Schema(description = "过期时间")
    private LocalDateTime expire;

    /**
     * 校验过期时间不能是过去
     *
     * @return 验证结果 验证通过返回null 否则返回错误信息
     */
    @Override
    public String validate() {
        if (expire != null && expire.isBefore(LocalDateTime.now())) {
            return "过期时间不能是过去";
        }
        return null;
    }
}
