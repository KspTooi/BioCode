package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import com.ksptool.bio.biz.core.model.exceltemplate.ExcelTemplatePo;
import com.ksptool.bio.biz.core.model.exceltemplate.dto.AddExcelTemplateDto;
import com.ksptool.bio.biz.core.model.exceltemplate.dto.EditExcelTemplateDto;
import com.ksptool.bio.biz.core.model.exceltemplate.dto.GetExcelTemplateListDto;
import com.ksptool.bio.biz.core.model.exceltemplate.vo.GetExcelTemplateDetailsVo;
import com.ksptool.bio.biz.core.model.exceltemplate.vo.GetExcelTemplateListVo;
import com.ksptool.bio.biz.core.repository.ExcelTemplateRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.0.1(A).1
 */
@Service
public class ExcelTemplateService {

    @Autowired
    private ExcelTemplateRepository repository;

    @Autowired
    private AttachService attachService;

    /**
     * 查询Excel模板列表
     *
     * @param dto 查询条件
     * @return 模板列表
     */
    public PageResult<GetExcelTemplateListVo> getExcelTemplateList(GetExcelTemplateListDto dto) {
        ExcelTemplatePo query = new ExcelTemplatePo();
        assign(dto, query);

        Page<ExcelTemplatePo> page = repository.getExcelTemplateList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetExcelTemplateListVo> vos = as(page.getContent(), GetExcelTemplateListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增Excel模板
     *
     * @param dto 新增模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void addExcelTemplate(AddExcelTemplateDto dto) {
        ExcelTemplatePo insertPo = as(dto, ExcelTemplatePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑Excel模板
     *
     * @param dto 编辑模板
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editExcelTemplate(EditExcelTemplateDto dto) throws BizException {
        ExcelTemplatePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));

        //查询code是否被占用
        var existPo = repository.getExcelTemplateByCode(dto.getCode());

        if (existPo != null && existPo.getId() != updatePo.getId()) {
            throw new BizException("模板标识已存在:[" + existPo.getCode() + "]");
        }

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询Excel模板详情
     *
     * @param dto 查询条件
     * @return 模板详情
     * @throws BizException 业务异常
     */
    public GetExcelTemplateDetailsVo getExcelTemplateDetails(CommonIdDto dto) throws BizException {
        ExcelTemplatePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));
        return as(po, GetExcelTemplateDetailsVo.class);
    }

    /**
     * 删除Excel模板
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeExcelTemplate(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
        }
        if (!dto.isBatch()) {
            repository.deleteById(dto.getId());
        }
    }

    /**
     * 上传Excel模板
     *
     * @param file 上传文件
     * @throws BizException 业务异常
     */
    public void uploadExcelTemplate(MultipartFile[] file) throws BizException {

        var addPos = new ArrayList<ExcelTemplatePo>();

        for (var f : file) {
            var po = new ExcelTemplatePo();
            parseTemplateName(f, po);
            po.setStatus(0); //默认启用 0:启用 1:禁用

            //查询code是否被占用
            var existPo = repository.getExcelTemplateByCode(po.getCode());

            if (existPo != null) {
                repository.delete(existPo);

                //复制状态、描述到新模板
                po.setStatus(existPo.getStatus());
                po.setRemark(existPo.getRemark());
            }

            //处理附件上传
            var attachId = attachService.uploadAttach(f, "core_excel_template");
            po.setAttachId(attachId);
            addPos.add(po);
        }

        repository.saveAll(addPos);
    }


    /**
     * 解析模板名称
     * <p>
     * 模板命名规则
     * 模板名称-唯一标识符.xlsx
     *
     * @param file 上传文件
     * @param po   模板实体
     * @throws BizException 业务异常
     */
    public void parseTemplateName(MultipartFile file, ExcelTemplatePo po) throws BizException {

        var name = file.getOriginalFilename();

        if (name == null) {
            throw new BizException("文件名不能为空");
        }

        var suffix = name.substring(name.lastIndexOf(".") + 1);

        if (suffix == null) {
            throw new BizException("文件后缀不能为空");
        }

        //截取模板名称
        var tName = name.substring(0, name.lastIndexOf("-"));
        var tCode = name.substring(name.lastIndexOf("-") + 1).replace(".xlsx", "").toLowerCase();

        if (StringUtils.isBlank(tName) || StringUtils.isBlank(tCode)) {
            throw new BizException("模板名称或唯一标识符不能为空");
        }

        if (tName.length() > 32) {
            throw new BizException("模板名称不能超过32个字符");
        }

        if (tCode.length() > 32) {
            throw new BizException("唯一标识符不能超过32个字符");
        }

        po.setName(tName);
        po.setCode(tCode);
    }

    /**
     * 下载Excel模板
     *
     * @param code     模板标识
     * @param response HTTP响应
     * @throws BizException 业务异常
     */
    public void downloadExcelTemplate(String code, HttpServletResponse response) throws BizException {

        if (StringUtils.isBlank(code)) {
            throw new BizException("模板标识不能为空");
        }

        ExcelTemplatePo template = repository.getExcelTemplateByCode(code);

        if (template == null) {
            throw new BizException("模板:[" + code + "]不存在，请联系管理员");
        }

        if (template.getStatus() == 1) {
            throw new BizException("模板:[" + code + "]已禁用，请联系管理员");
        }

        if (template.getAttachId() == null) {
            throw new BizException("模板:[" + code + "]附件不存在，请联系管理员");
        }

        AttachPo attach = attachService.requireAttach(template.getAttachId());

        if (attach == null) {
            throw new BizException("模板:[" + code + "]附件不存在，请联系管理员");
        }

        Path filePath = attachService.getAttachLocalPath(Paths.get(attach.getPath()));

        if (!Files.exists(filePath)) {
            throw new BizException("模板:[" + code + "]文件不存在，请联系管理员");
        }

        try {
            String filename = template.getName() + "-" + template.getCode() + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
            response.setContentLengthLong(Files.size(filePath));

            try (InputStream is = Files.newInputStream(filePath);
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            throw new BizException("下载模板:[" + code + "]失败: " + e.getMessage() + "，请联系管理员");
        }
    }


}