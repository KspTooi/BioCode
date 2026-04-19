package com.ksptool.bio.biz.qf.controller;

import com.ksptool.bio.commons.annotation.PrintLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/qfProc")
@Tag(name = "QF-流程与任务", description = "QF-流程与任务")
@Slf4j
public class QfProcController {

}
