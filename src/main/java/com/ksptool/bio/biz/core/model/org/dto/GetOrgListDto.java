package com.ksptool.bio.biz.core.model.org.dto;

import com.ksptool.assembly.entity.web.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class GetOrgListDto extends PageQuery {

    @NotNull(message = "组织集合不能为空")
    @Schema(description = "组织ID集合")
    private Set<Long> orgIds;
}

