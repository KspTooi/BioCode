package com.ksptool.bio.biz.qf.model.qftodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditQfTodoDto {

    @Schema(description = "主键ID")
    private Long id;


}
