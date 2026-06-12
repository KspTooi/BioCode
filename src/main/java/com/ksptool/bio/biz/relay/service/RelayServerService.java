package com.ksptool.bio.biz.relay.service;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.relay.commons.HttpRelayServlet;
import com.ksptool.bio.biz.relay.commons.HttpRouteRule;
import com.ksptool.bio.biz.relay.commons.RouteSelector;
import com.ksptool.bio.biz.relay.model.relayserver.*;
import com.ksptool.bio.biz.relay.model.relayserverroute.po.RelayServerRoutePo;
import com.ksptool.bio.biz.relay.model.routerule.po.RouteRulePo;
import com.ksptool.bio.biz.relay.repository.RelayServerRepository;
import com.ksptool.bio.biz.relay.repository.RouteRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static com.ksptool.entities.Entities.as;

@Slf4j
@Service
public class RelayServerService {

    //运行中的中继服务器 端口 -> Tomcat实例
    private final Map<Integer, Tomcat> runningServers = new ConcurrentHashMap<>();

    //中继服务器实例 中继服务器ID -> 中继服务器实例(用于熔断处理)
    private final Map<Long, HttpRelayServlet> realaySevletInstances = new ConcurrentHashMap<>();

    @Autowired
    private RelayServerRepository relayServerRepository;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private Gson gson;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RouteRuleRepository routeRuleRepository;

    /**
     * 获取中继服务器列表
     *
     * @param dto 查询条件
     * @return 中继服务器列表
     */
    public PageResult<GetRelayServerListVo> getRelayServerList(GetRelayServerListDto dto) {

        Page<RelayServerPo> pPos = relayServerRepository.getRelayServerList(dto, dto.pageRequest());
        List<GetRelayServerListVo> vos = new ArrayList<>();

        for (RelayServerPo item : pPos) {
            GetRelayServerListVo vo = new GetRelayServerListVo();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setHost(item.getHost() + ":" + item.getPort());
            vo.setForwardType(item.getForwardType());
            vo.setForwardUrl(item.getForwardUrl());
            vo.setAutoStart(item.getAutoStart());
            vo.setStatus(item.getStatus());
            vo.setCreateTime(item.getCreateTime());
            vos.add(vo);
        }

        return PageResult.success(vos, pPos.getTotalElements());
    }

    /**
     * 添加中继服务器
     *
     * @param dto 中继服务器信息
     * @return 中继服务器ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRelayServer(AddRelayServerDto dto) throws BizException {

        //中继服务器名称不能重复
        Long count = relayServerRepository.countByName(dto.getName());

        if (count > 0) {
            throw new BizException("中继服务器名称不能重复");
        }

        //主机和端口不能重复
        count = relayServerRepository.countByHostAndPort(dto.getHost(), dto.getPort());

        if (count > 0) {
            throw new BizException("已有相同主机和端口的中继服务器");
        }

        RelayServerPo insertPo = new RelayServerPo();
        insertPo.setName(dto.getName());
        insertPo.setHost(dto.getHost());
        insertPo.setPort(dto.getPort());
        insertPo.setForwardType(dto.getForwardType());
        insertPo.setForwardUrl(dto.getForwardUrl());
        insertPo.setAutoStart(dto.getAutoStart());
        insertPo.setStatus(1); //0:已禁用 1:未启动 2:运行中 3:启动失败
        insertPo.setErrorMessage(null);
        insertPo.setOverrideRedirect(dto.getOverrideRedirect()); //重定向覆写 0:否 1:是
        insertPo.setOverrideRedirectUrl(dto.getOverrideRedirectUrl()); //重定向覆写URL
        insertPo.setRequestIdStrategy(dto.getRequestIdStrategy()); //请求ID策略 0:随机生成 1:从响应头获取
        insertPo.setRequestIdHeaderName(dto.getRequestIdHeaderName()); //请求ID在响应头中的字段名称
        insertPo.setBizErrorStrategy(dto.getBizErrorStrategy()); //业务错误策略 0:由HTTP状态码决定 1:由业务错误码决定
        insertPo.setBizErrorCodeField(dto.getBizErrorCodeField()); //业务错误码字段(JSONPath)
        insertPo.setBizSuccessCodeValue(dto.getBizSuccessCodeValue()); //业务成功码值(正确时返回的值)

        //当桥接目标类型为路由时，处理路由规则 0:直接 1:路由
        if (dto.getForwardType() == 1) {

            //搜集路由规则IDS
            List<Long> routeRuleIds = new ArrayList<>();
            for (RelayServerRouteRuleDto item : dto.getRouteRules()) {
                routeRuleIds.add(item.getRouteRuleId());
            }

            //获取路由规则列表
            List<RouteRulePo> routeRuleList = routeRuleRepository.getRouteRuleListByIds(routeRuleIds);

            if (routeRuleList.isEmpty()) {
                throw new BizException("路由规则查询失败，请检查路由规则是否存在或无权限访问");
            }

            for (var item : routeRuleList) {

                //从原始DTO中查询出SEQ
                var seq = -1;
                for (var dtoItem : dto.getRouteRules()) {
                    if (dtoItem.getRouteRuleId().equals(item.getId())) {
                        seq = dtoItem.getSeq();
                        break;
                    }
                }

                if (seq == -1) {
                    throw new BizException("路由规则SEQ查询失败，请检查路由规则是否存在或无权限访问");
                }

                if (item.getRouteServer() == null) {
                    throw new BizException("路由规则:" + item.getName() + " 未配置目标服务器，请检查路由规则是否存在或无权限访问");
                }

                RelayServerRoutePo newRule = new RelayServerRoutePo();
                newRule.setRelayServer(insertPo);
                newRule.setRouteRule(item);
                newRule.setSeq(seq);
                insertPo.addRouteRule(newRule);
            }

        }


        relayServerRepository.save(insertPo);
    }

    /**
     * 编辑中继服务器
     *
     * @param dto 中继服务器信息
     * @return 中继服务器ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void editRelayServer(EditRelayServerDto dto) throws BizException {

        RelayServerPo updatePo = relayServerRepository.findById(dto.getId())
                .orElseThrow(() -> new BizException("中继服务器不存在"));

        if (updatePo.getStatus() == 2) {
            throw new BizException("无法修改一个正在运行的中继服务器");
        }

        //中继服务器名称不能重复
        Long count = relayServerRepository.countByNameExcludeId(dto.getName(), updatePo.getId());

        if (count > 0) {
            throw new BizException("中继服务器名称不能重复");
        }

        //主机和端口不能重复
        count = relayServerRepository.countByHostAndPortExcludeId(dto.getHost(), dto.getPort(), updatePo.getId());

        if (count > 0) {
            throw new BizException("已有相同主机和端口的中继服务器");
        }

        updatePo.setName(dto.getName());
        updatePo.setHost(dto.getHost());
        updatePo.setPort(dto.getPort());
        updatePo.setForwardType(dto.getForwardType());
        updatePo.setForwardUrl(dto.getForwardUrl());
        updatePo.setAutoStart(dto.getAutoStart());
        updatePo.setOverrideRedirect(dto.getOverrideRedirect()); //重定向覆写 0:否 1:是
        updatePo.setOverrideRedirectUrl(dto.getOverrideRedirectUrl()); //重定向覆写URL
        updatePo.setRequestIdStrategy(dto.getRequestIdStrategy()); //请求ID策略 0:随机生成 1:从响应头获取
        updatePo.setRequestIdHeaderName(dto.getRequestIdHeaderName()); //请求ID在响应头中的字段名称
        updatePo.setBizErrorStrategy(dto.getBizErrorStrategy()); //业务错误策略 0:由HTTP状态码决定 1:由业务错误码决定
        updatePo.setBizErrorCodeField(dto.getBizErrorCodeField()); //业务错误码字段(JSONPath)
        updatePo.setBizSuccessCodeValue(dto.getBizSuccessCodeValue()); //业务成功码值(正确时返回的值)

        //清空原有路由规则
        updatePo.clearRouteRules();

        //当桥接目标类型为路由时，处理路由规则 0:直接 1:路由
        if (dto.getForwardType() == 1) {


            //搜集路由规则IDS
            List<Long> routeRuleIds = new ArrayList<>();
            for (RelayServerRouteRuleDto item : dto.getRouteRules()) {
                routeRuleIds.add(item.getRouteRuleId());
            }

            //获取路由规则列表
            List<RouteRulePo> routeRuleList = routeRuleRepository.getRouteRuleListByIds(routeRuleIds);

            if (routeRuleList.isEmpty()) {
                throw new BizException("路由规则查询失败，请检查路由规则是否存在或无权限访问");
            }

            for (var item : routeRuleList) {

                //从原始DTO中查询出SEQ
                var seq = -1;
                for (var dtoItem : dto.getRouteRules()) {
                    if (dtoItem.getRouteRuleId().equals(item.getId())) {
                        seq = dtoItem.getSeq();
                        break;
                    }
                }

                if (seq == -1) {
                    throw new BizException("路由规则SEQ查询失败，请检查路由规则是否存在或无权限访问");
                }

                if (item.getRouteServer() == null) {
                    throw new BizException("路由规则:" + item.getName() + " 未配置目标服务器，请检查路由规则是否存在或无权限访问");
                }

                RelayServerRoutePo newRule = new RelayServerRoutePo();
                newRule.setRelayServer(updatePo);
                newRule.setRouteRule(item);
                newRule.setSeq(seq);
                updatePo.addRouteRule(newRule);
            }

        }

        relayServerRepository.save(updatePo);
    }


    /**
     * 获取中继服务器详情
     *
     * @param id 中继服务器ID
     * @return 中继服务器详情
     */
    public GetRelayServerDetailsVo getRelayServerDetails(Long id) throws BizException {
        RelayServerPo po = relayServerRepository.findById(id)
                .orElseThrow(() -> new BizException("中继服务器不存在"));
        GetRelayServerDetailsVo vo = new GetRelayServerDetailsVo();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setHost(po.getHost());
        vo.setPort(po.getPort());
        vo.setForwardType(po.getForwardType());
        vo.setForwardUrl(po.getForwardUrl());
        vo.setAutoStart(po.getAutoStart());
        vo.setStatus(po.getStatus());
        vo.setCreateTime(po.getCreateTime());
        vo.setOverrideRedirect(po.getOverrideRedirect());
        vo.setOverrideRedirectUrl(po.getOverrideRedirectUrl());
        vo.setRequestIdStrategy(po.getRequestIdStrategy());
        vo.setRequestIdHeaderName(po.getRequestIdHeaderName());
        vo.setBizErrorStrategy(po.getBizErrorStrategy());
        vo.setBizErrorCodeField(po.getBizErrorCodeField());
        vo.setBizSuccessCodeValue(po.getBizSuccessCodeValue());

        //处理绑定的路由规则回显
        List<RelayServerRouteRuleVo> routeRules = new ArrayList<>();

        for (RelayServerRoutePo item : po.getRouteRules()) {
            RelayServerRouteRuleVo ruleVo = new RelayServerRouteRuleVo();
            ruleVo.setRouteRuleId(item.getRouteRule().getId());
            ruleVo.setRouteRuleName(item.getRouteRule().getName());
            ruleVo.setSeq(item.getSeq());
            routeRules.add(ruleVo);
        }
        vo.setRouteRules(routeRules);
        return vo;
    }


    /**
     * 删除中继服务器
     *
     * @param id 中继服务器ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRelayServer(Long id) throws BizException {

        RelayServerPo po = relayServerRepository.findById(id)
                .orElseThrow(() -> new BizException("中继服务器不存在"));

        if (po.getStatus() == 2) {
            throw new BizException("无法删除一个正在运行的中继服务器");
        }

        relayServerRepository.deleteById(id);
    }

    /**
     * 启动中继服务器
     *
     * @param id 中继服务器ID
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> startRelayServer(Long id) throws BizException {

        RelayServerPo po = relayServerRepository.findById(id)
                .orElseThrow(() -> new BizException("中继服务器不存在"));

        //0:已禁用 1:未启动 2:运行中 3:启动失败
        if (po.getStatus() != 1 && po.getStatus() != 3) {
            throw new BizException("中继服务器状态不正确");
        }

        if (runningServers.containsKey(po.getPort())) {
            log.warn("端口 {} 的代理服务已在运行中。", po.getPort());
            return Result.success("中继服务器已在运行中");
        }

        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(po.getPort());

            // 配置Connector以使用虚拟线程
            tomcat.getConnector().getProtocolHandler().setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // 创建一个临时的Context
            File base = new File(System.getProperty("java.io.tmpdir"));
            Context context = tomcat.addContext("", base.getAbsolutePath());

            // 创建并注册我们的转发Servlet
            HttpRelayServlet servlet = new HttpRelayServlet(as(po, GetRelayServerDetailsVo.class), httpClient, gson, requestService);

            if (po.getForwardType() == 1) { //0:直接 1:路由
                List<RelayServerRoutePo> routeRules = po.getRouteRules();
                if (routeRules == null || routeRules.isEmpty()) {
                    throw new BizException("运行中继服务器失败，未找到有效的路由规则!");
                }

                //组装路由规则
                List<HttpRouteRule> httpRouteRules = new ArrayList<>();

                for (RelayServerRoutePo item : routeRules) {
                    HttpRouteRule httpRouteRule = new HttpRouteRule();
                    httpRouteRule.setTargetHost(item.getRouteRule().getRouteServer().getHost());
                    httpRouteRule.setTargetPort(item.getRouteRule().getRouteServer().getPort());
                    httpRouteRule.setMatchType(item.getRouteRule().getMatchType());
                    httpRouteRule.setMatchKey(item.getRouteRule().getMatchKey());
                    httpRouteRule.setMatchOperator(item.getRouteRule().getMatchOperator());
                    httpRouteRule.setMatchValue(item.getRouteRule().getMatchValue());
                    httpRouteRule.setSeq(item.getSeq());
                    httpRouteRules.add(httpRouteRule);
                }

                //创建路由选择器
                RouteSelector routeSelector = new RouteSelector(httpRouteRules);
                servlet.setRouteSelector(routeSelector);
            }

            Tomcat.addServlet(context, "proxyServlet", servlet);
            context.addServletMappingDecoded("/*", "proxyServlet"); // 匹配所有路径
            tomcat.start();

            if (tomcat.getConnector().getState() == LifecycleState.FAILED) {
                tomcat.stop();
                tomcat.destroy();
                throw new LifecycleException("Tomcat connector failed to start.");
            }

            runningServers.put(po.getPort(), tomcat);
            log.info("中继服务器在端口 {} 启动，转发至 {}", po.getPort(), po.getForwardUrl());

            //注册中继服务器实例
            realaySevletInstances.put(po.getId(), servlet);

            po.setStatus(2);
            relayServerRepository.save(po);
        } catch (Exception e) {
            log.error("启动中继服务器失败", e);
            po.setStatus(3);
            po.setErrorMessage(e.getMessage());
            relayServerRepository.save(po);
            return Result.error("启动中继服务器失败 原因:" + e.getMessage());
        }

        return Result.success("启动中继服务器成功");
    }

    /**
     * 停止中继服务器
     *
     * @param id 中继服务器ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> stopRelayServer(Long id) throws BizException {

        RelayServerPo po = relayServerRepository.findById(id)
                .orElseThrow(() -> new BizException("中继通道不存在"));

        if (po.getStatus() != 2) {
            throw new BizException("中继通道状态不正确");
        }

        Tomcat tomcat = runningServers.remove(po.getPort());
        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (LifecycleException e) {
                log.error("停止中继通道失败", e);
            }
        }

        po.setStatus(1);
        relayServerRepository.save(po);

        //注销中继服务器实例
        realaySevletInstances.remove(po.getId());

        return Result.success("停止中继通道成功");
    }

    /**
     * 启动所有自动运行的中继服务器
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void initRelayServer() {

        //将所有中继服务器修改为停止状态
        relayServerRepository.stopAllRelayServer();

        //获取所有自动运行的中继服务器
        List<RelayServerPo> autoStartRelayServerList = relayServerRepository.getAutoStartRelayServerList();

        for (RelayServerPo item : autoStartRelayServerList) {
            try {
                startRelayServer(item.getId());
            } catch (BizException e) {
                log.error("初始化中继服务器失败", e);
            }
        }

    }

    /**
     * 获取中继服务器路由状态
     *
     * @param id 中继服务器ID
     * @return 中继服务器路由状态
     */
    public List<GetRelayServerRouteStateVo> getRelayServerRouteState(Long id) throws BizException {

        HttpRelayServlet servlet = realaySevletInstances.get(id);
        if (servlet == null) {
            throw new BizException("中继服务器实例不存在,这可能是因为中继服务器未启动或配置错误!");
        }

        List<HttpRouteRule> httpRouteRules = servlet.getRouteSelector().getRouteRules();
        return as(httpRouteRules, GetRelayServerRouteStateVo.class);
    }

    /**
     * 重置熔断状态
     *
     * @param dto 中继服务器ID
     * @throws BizException
     */
    public void resetRelayServerBreaker(ResetRelayServerBreakerDto dto) throws BizException {
        HttpRelayServlet servlet = realaySevletInstances.get(dto.getId());

        if (servlet == null) {
            throw new BizException("中继服务器实例不存在,这可能是因为中继服务器未启动或配置错误!");
        }

        //0:复位熔断 1:置为熔断
        if (dto.getKind() == 0) {
            //如果主机和端口都填写，则重置指定主机的熔断状态
            if (dto.getHost() != null && dto.getPort() != null) {
                servlet.getRouteSelector().resetBreakStatus(dto.getHost(), dto.getPort());
            }

            //如果主机不填写，则重置所有主机的熔断状态
            if (dto.getHost() == null) {
                servlet.getRouteSelector().resetBreakStatus();
            }
        }

        //1:置为熔断
        if (dto.getKind() == 1) {

            //必须填写主机和端口
            if (dto.getHost() == null || dto.getPort() == null) {
                throw new BizException("主机和端口不能为空");
            }

            servlet.getRouteSelector().breakHostPort(dto.getHost(), dto.getPort());
        }

    }

}
