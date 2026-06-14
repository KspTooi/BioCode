package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDef;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import com.ksptool.bio.biz.aacp.model.func.AacpMicroFuncPo;
import com.ksptool.bio.biz.aacp.model.func.dto.AddMicroFuncDto;
import com.ksptool.bio.biz.aacp.model.func.dto.EditMicroFuncDto;
import com.ksptool.bio.biz.aacp.model.func.dto.GetMicroFuncListDto;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncDetailsVo;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncListVo;
import com.ksptool.bio.biz.aacp.model.func.vo.GetMicroFuncRegistryVo;
import com.ksptool.bio.biz.aacp.repository.CapMicroFuncRepository;
import com.ksptool.bio.biz.aacp.repository.MicroFuncRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Slf4j
@Service
public class MicroFuncService {

    @Autowired
    private MicroFuncRepository repository;

    @Autowired
    private CapMicroFuncRepository capMicroFuncRepository;

    @Autowired
    private MicroFuncRuntimeService runtimeService;

    /**
     * 查询微函数列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetMicroFuncListVo> getMicroFuncList(GetMicroFuncListDto dto) {
        AacpMicroFuncPo query = new AacpMicroFuncPo();
        assign(dto, query);

        Page<AacpMicroFuncPo> page = repository.getMicroFuncList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetMicroFuncListVo> vos = as(page.getContent(), GetMicroFuncListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增微函数
     *
     * @param dto 新增条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addMicroFunc(AddMicroFuncDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("微函数标识已存在,请更换后重试.");
        }
        AacpMicroFuncPo insertPo = as(dto, AacpMicroFuncPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑微函数
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editMicroFunc(EditMicroFuncDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("微函数标识已存在,请更换后重试.");
        }
        AacpMicroFuncPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询微函数详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetMicroFuncDetailsVo getMicroFuncDetails(CommonIdDto dto) throws BizException {
        AacpMicroFuncPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetMicroFuncDetailsVo.class);
    }

    /**
     * 删除微函数
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeMicroFunc(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("微函数不支持批量删除");
        }
        long refCount = capMicroFuncRepository.countByMicroFuncId(dto.getId());
        if (refCount > 0) {
            throw new BizException("该微函数已被" + refCount + "个能力包使用，无法删除");
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 获取已注册微函数列表（从 MicroFuncRegistry 查询 @MicroFunc 注入的函数）
     *
     * @return 已注册微函数列表
     */
    public List<GetMicroFuncRegistryVo> getMicroFuncRegistryList() {
        List<GetMicroFuncRegistryVo> vos = new ArrayList<>();
        for (MicroFuncDef def : runtimeService.getAll()) {
            GetMicroFuncRegistryVo vo = new GetMicroFuncRegistryVo();
            vo.setTarget(def.getTarget());
            vo.setName(def.getName());
            vo.setDescription(def.getDescription());
            vo.setParameterCount(def.getParameters().length);

            List<String> typeNames = new ArrayList<>();
            for (var p : def.getParameters()) {
                typeNames.add(p.getType().getName());
            }
            vo.setParameterTypes(typeNames);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 同步微函数：从 MicroFuncRuntimeService 获取所有已注册定义，缺失的自动写入数据库。
     *
     * @return 同步结果描述
     */
    @Transactional(rollbackFor = Exception.class)
    public String syncMicroFuncs() {
        Collection<MicroFuncDef> defs = runtimeService.getAll();
        int added = 0;
        int updated = 0;
        int skipped = 0;
        for (MicroFuncDef def : defs) {
            String schema = new Gson().toJson(def.getInputSchema());
            AacpMicroFuncPo existing = repository.getByCode(def.getTarget());

            if (existing == null) {
                AacpMicroFuncPo po = new AacpMicroFuncPo();
                po.setCode(def.getTarget());
                po.setName(def.getName());
                po.setDescription(def.getDescription());
                po.setTarget(def.getTarget());
                po.setSchema(schema);
                repository.save(po);
                added++;
                continue;
            }

            //已有记录：比较注解数据与库中是否一致，不一致则更新
            boolean needUpdate = false;
            if (!Objects.equals(existing.getName(), def.getName())) {
                existing.setName(def.getName());
                needUpdate = true;
            }
            if (!Objects.equals(existing.getDescription(), def.getDescription())) {
                existing.setDescription(def.getDescription());
                needUpdate = true;
            }
            if (!Objects.equals(existing.getTarget(), def.getTarget())) {
                existing.setTarget(def.getTarget());
                needUpdate = true;
            }
            if (!Objects.equals(existing.getSchema(), schema)) {
                existing.setSchema(schema);
                needUpdate = true;
            }
            if (needUpdate) {
                repository.save(existing);
                updated++;
                continue;
            }
            skipped++;
        }
        log.info("微函数同步完成: 新增{}条, 更新{}条, 跳过{}条", added, updated, skipped);
        return "同步完成：新增" + added + "条，更新" + updated + "条，跳过" + skipped + "条";
    }

    @MicroFunc(target = "test.hello", name = "问候", description = "返回一句问候语")
    public String sayHello() {
        return "你好，微函数系统已就绪！";
    }

    @MicroFunc(target = "test.add", name = "加法计算", description = "对两个整数执行加法运算")
    public int addNumbers(@Param("a") int a, @Param("b") int b) {
        return a + b;
    }

    @MicroFunc(target = "test.time", name = "获取时间", description = "返回当前服务器时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @MicroFunc(target = "test.echo", name = "回声", description = "将输入的消息原样返回")
    public String echo(@Param("message") String message) {
        return message;
    }

    @MicroFunc(target = "test.status", name = "状态列表", description = "返回系统状态项列表")
    public List<String> listStatus() {
        return Arrays.asList("运行中", "正常", "微函数数量：" + runtimeService.size());
    }

    /**
     * 发起HTTP GET请求并返回响应体，供AI代理访问外部API或网页
     * <p>
     * 使用 Java 11 HttpClient，连接超时10秒，读取超时30秒。
     * HTTP 4xx/5xx响应不会抛异常，直接返回状态码与响应体。
     *
     * @param url 目标URL（需https或http协议头）
     * @return 格式化的响应信息（状态码 + 响应体前2000字符）
     */
    @MicroFunc(target = "test.curl", name = "HTTP请求", description = "向指定URL发起GET请求并返回响应内容，支持代理访问网页或API")
    public String httpGet(@Param("url") String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (body == null) {
            body = "";
        }
        if (body.length() > 2000) {
            body = body.substring(0, 2000) + "\n...[响应已截断，原文长度:" + body.length() + "]";
        }
        return "状态码: " + response.statusCode() + "\n" + body;
    }
}
