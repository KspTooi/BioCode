package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.commons.MicroFuncDefinition;
import com.ksptool.bio.biz.aacp.commons.MicroFuncRegistry;
import com.ksptool.bio.biz.aacp.model.AacpFuncPo;
import com.ksptool.bio.biz.aacp.model.dto.AddAacpFuncDto;
import com.ksptool.bio.biz.aacp.model.dto.EditAacpFuncDto;
import com.ksptool.bio.biz.aacp.model.dto.GetAacpFuncListDto;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpFuncDetailsVo;
import com.ksptool.bio.biz.aacp.model.vo.GetAacpFuncListVo;
import com.ksptool.bio.biz.aacp.model.vo.GetMicroFuncListVo;
import com.ksptool.bio.biz.aacp.repository.AacpCapabilityFuncRepository;
import com.ksptool.bio.biz.aacp.repository.AacpFuncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpFuncService {

    @Autowired
    private AacpFuncRepository repository;

    @Autowired
    private AacpCapabilityFuncRepository capabilityFuncRepository;

    @Autowired
    private MicroFuncRegistry microFuncRegistry;

    /**
     * 查询微函数列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpFuncListVo> getAacpFuncList(GetAacpFuncListDto dto) {
        AacpFuncPo query = new AacpFuncPo();
        assign(dto, query);

        Page<AacpFuncPo> page = repository.getAacpFuncList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpFuncListVo> vos = as(page.getContent(), GetAacpFuncListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增微函数
     *
     * @param dto 新增条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpFunc(AddAacpFuncDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("微函数标识已存在,请更换后重试.");
        }
        AacpFuncPo insertPo = as(dto, AacpFuncPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑微函数
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpFunc(EditAacpFuncDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("微函数标识已存在,请更换后重试.");
        }
        AacpFuncPo updatePo = repository.findById(dto.getId())
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
    public GetAacpFuncDetailsVo getAacpFuncDetails(CommonIdDto dto) throws BizException {
        AacpFuncPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpFuncDetailsVo.class);
    }

    /**
     * 删除微函数
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpFunc(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("微函数不支持批量删除");
        }
        long refCount = capabilityFuncRepository.countByFuncId(dto.getId());
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
    public List<GetMicroFuncListVo> getMicroFuncList() {
        List<GetMicroFuncListVo> vos = new ArrayList<>();
        for (MicroFuncDefinition def : microFuncRegistry.getAll()) {
            GetMicroFuncListVo vo = new GetMicroFuncListVo();
            vo.setTarget(def.getTarget());
            vo.setName(def.getName());
            vo.setDescription(def.getDescription());
            vo.setParameterCount(def.getParameterTypes().length);

            List<String> typeNames = new ArrayList<>();
            for (Class<?> type : def.getParameterTypes()) {
                typeNames.add(type.getName());
            }
            vo.setParameterTypes(typeNames);
            vos.add(vo);
        }
        return vos;
    }

    @MicroFunc(target = "test.hello", name = "问候", description = "返回一句问候语")
    public String sayHello() {
        return "你好，微函数系统已就绪！";
    }

    @MicroFunc(target = "test.add", name = "加法计算", description = "对两个整数执行加法运算")
    public int addNumbers(int a, int b) {
        return a + b;
    }

    @MicroFunc(target = "test.time", name = "获取时间", description = "返回当前服务器时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @MicroFunc(target = "test.echo", name = "回声", description = "将输入的消息原样返回")
    public String echo(String message) {
        return message;
    }

    @MicroFunc(target = "test.status", name = "状态列表", description = "返回系统状态项列表")
    public List<String> listStatus() {
        return Arrays.asList("运行中", "正常", "微函数数量：" + microFuncRegistry.size());
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
    public String httpGet(String url) throws Exception {
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
