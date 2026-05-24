package com.ksptool.bio.biz.core.controller;

import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.auth.common.aop.RowScope;
import com.ksptool.bio.biz.core.model.org.dto.AddOrgDto;
import com.ksptool.bio.biz.core.model.org.dto.EditOrgDto;
import com.ksptool.bio.biz.core.model.org.dto.GetOrgListDto;
import com.ksptool.bio.biz.core.model.org.dto.GetOrgTreeDto;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgDetailsVo;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgListVo;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgTreeVo;
import com.ksptool.bio.biz.core.service.OrgService;
import com.ksptool.bio.biz.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/org")
@Tag(name = "CORE-组织机构管理", description = "组织机构管理")
@Slf4j
@RowScope(mode = RowScope.Mode.ROOT_ONLY)
public class OrgController {

    @Autowired
    private OrgService orgService;

    @Autowired
    private UserService userService;


    @PostMapping("/getOrgTree")
    @Operation(summary = "查询组织机构树(不分页)")
    public Result<List<GetOrgTreeVo>> getOrgTree(@RequestBody @Valid GetOrgTreeDto dto) throws Exception {
        List<GetOrgTreeVo> list = orgService.getOrgTree(dto);
        return Result.success(list);
    }

    @PostMapping("/getOrgList")
    @Operation(summary = "查询组织机构树列表")
    public PageResult<GetOrgListVo> getOrgList(@RequestBody @Valid GetOrgListDto dto) throws Exception {
       return orgService.getOrgList(dto);
    }

    @PreAuthorize("@auth.hasCode('core:org:add')")
    @Operation(summary = "新增组织机构")
    @PostMapping("/addOrg")
    public Result<String> addOrg(@RequestBody @Valid AddOrgDto dto) throws Exception {

        orgService.addOrg(dto);

        //给该企业/租户下的全部在线用户加版本
        userService.increaseDvByRootId(dto.getParentId());
        return Result.success("新增成功");
    }


    @PreAuthorize("@auth.hasCode('core:org:edit')")
    @Operation(summary = "编辑组织机构")
    @PostMapping("/editOrg")
    public Result<String> editOrg(@RequestBody @Valid EditOrgDto dto) throws Exception {
        orgService.editOrg(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@auth.hasCode('core:org:details')")
    @Operation(summary = "查询组织机构详情")
    @PostMapping("/getOrgDetails")
    public Result<GetOrgDetailsVo> getOrgDetails(@RequestBody @Valid CommonIdDto dto) throws Exception {
        GetOrgDetailsVo details = orgService.getOrgDetails(dto);
        if (details == null) {
            return Result.error("无数据");
        }
        return Result.success(details);
    }

    @PreAuthorize("@auth.hasCode('core:org:remove')")
    @Operation(summary = "删除组织机构")
    @PostMapping("/removeOrg")
    public Result<String> removeOrg(@RequestBody @Valid CommonIdDto dto) throws Exception {
        orgService.removeOrg(dto);
        return Result.success("操作成功");
    }

}
