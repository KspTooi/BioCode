package com.ksptool.bio.biz.rdbg.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.bio.biz.rdbg.commons.GsonUtils;
import com.ksptool.bio.biz.rdbg.commons.model.HttpHeaderVo;
import com.ksptool.bio.biz.rdbg.commons.model.RequestSchema;
import com.ksptool.bio.biz.rdbg.model.filter.SimpleFilterOperationPo;
import com.ksptool.bio.biz.rdbg.model.filter.SimpleFilterTriggerPo;
import com.ksptool.bio.biz.rdbg.model.userrequest.UserRequestPo;
import com.ksptool.bio.biz.rdbg.model.userrequestlog.UserRequestLogPo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
public class UserRequestFilterService {

    @Autowired
    private Gson gson;


    /**
     * 执行响应过滤器
     *
     * @param res   响应
     * @param po    用户请求
     * @param logPo 用户请求日志
     */
    public void doResponseFilter(HttpResponse<String> res, UserRequestPo po, UserRequestLogPo logPo) {

        //获取请求组上的过滤器
        var groupPo = po.getGroup();

        //未加入请求组，不执行过滤器
        if (groupPo == null) {
            return;
        }

        var filterPoList = groupPo.getFilters();

        //遍历每个过滤器
        for (var filterPo : filterPoList) {

            //过滤器被禁用则跳过 0:启用 1:禁用
            if (filterPo.getStatus() == 1) {
                continue;
            }

            //过滤器不是响应过滤器则跳过
            if (filterPo.getDirection() != 1) {
                continue;
            }

            //获取过滤器下的触发器与操作
            var triggerPoList = filterPo.getTriggers();
            var operationPoList = filterPo.getOperations();

            //无触发器或操作则跳过
            if (triggerPoList.isEmpty() || operationPoList.isEmpty()) {
                continue;
            }

            //判断响应是否命中所有触发器 未全部命中则跳过
            var hitAll = true;
            for (var triggerPo : triggerPoList) {
                if (!isHitTrigger(res, triggerPo)) {
                    hitAll = false;
                    break;
                }
            }
            if (!hitAll) {
                continue;
            }


            //遍历执行全部操作
            for (var item : operationPoList) {
                doResponseOperation(res, item, logPo);
            }

        }


    }

    /**
     * 执行响应操作
     *
     * @param res         响应
     * @param operationPo 操作列表
     */
    public void doResponseOperation(HttpResponse<String> res, SimpleFilterOperationPo operationPo, UserRequestLogPo logPo) {

        //类型 0:持久化 1:缓存 2:注入缓存 3.注入持久化 4:覆写URL
        var kind = operationPo.getKind();
        //目标 0:标头 1:JSON载荷 2:URL(仅限kind=4)
        var target = operationPo.getTarget();
        var f = operationPo.getF();
        var t = operationPo.getT();

        // 处理持久化
        if (kind == 0) {

            // 持久化标头
            if (target == 0) {
                res.headers().firstValue(f).ifPresent(value -> {
                    //globalConfigService.setValue(t, value);
                    log.info("响应过滤器：持久化标头 '{}' 的值为 '{}' 到配置 '{}'", f, value, t);
                });
            }

            // 持久化JSON载荷
            if (target == 1) {
                Optional<String> contentTypeOpt = res.headers().firstValue("Content-Type");
                if (contentTypeOpt.isPresent() && contentTypeOpt.get().contains("application/json")) {
                    String jsonPayload = res.body();
                    if (StringUtils.isNotBlank(jsonPayload)) {
                        JsonElement jsonElement = gson.fromJson(jsonPayload, JsonElement.class);
                        String value = GsonUtils.getFromPath(jsonElement, f);
                        if (StringUtils.isNotBlank(value)) {
                            //globalConfigService.setValue(t, value);
                            log.info("响应过滤器：持久化JSON路径 '{}' 的值为 '{}' 到配置 '{}'", f, value, t);
                        }
                    }
                }
            }
        }

        // 缓存 (暂不开发)
        if (kind == 1) {
            if (target == 0) {
                log.info("响应过滤器：处理缓存标头(跳过)");
            }
            if (target == 1) {
                log.info("响应过滤器：处理缓存JSON载荷(跳过)");
            }
        }

        // 注入和覆写URL不适用于响应

        // 标记请求状态
        if (kind == 50) {
            if (target == 50) {
                logPo.setStatus(0);
                log.info("响应过滤器：处理标记请求状态(正常)");
            }
            if (target == 51) {
                logPo.setStatus(1);
                log.info("响应过滤器：处理标记请求状态(HTTP失败)");
            }
            if (target == 52) {
                logPo.setStatus(2);
                log.info("响应过滤器：处理标记请求状态(业务失败)");
            }
            if (target == 53) {
                logPo.setStatus(3);
                log.info("响应过滤器：处理标记请求状态(连接超时)");
            }
        }

        // 获取请求ID
        if (kind == 60) {

            //0:标头
            if (target == 0) {
                var value = res.headers().firstValue(f).orElse(null);
                if (StringUtils.isNotBlank(value)) {
                    logPo.setRequestId(value);
                }
            }

            //1:JSON载荷
            if (target == 1) {
                var value = GsonUtils.getFromPath(gson.fromJson(res.body(), JsonElement.class), f);
                if (StringUtils.isNotBlank(value)) {
                    logPo.setRequestId(value);
                }
            }
        }
    }


    /**
     * 执行请求过滤器
     *
     * @param rs 请求Schema
     * @param po 用户请求
     */
    public void doRequestFilter(RequestSchema rs, UserRequestPo po) {

        //获取请求组上的过滤器
        var groupPo = po.getGroup();

        //未加入请求组，不执行过滤器
        if (groupPo == null) {
            return;
        }

        var filterPoList = groupPo.getFilters();

        //遍历每个过滤器
        for (var filterPo : filterPoList) {

            //过滤器被禁用则跳过 0:启用 1:禁用
            if (filterPo.getStatus() == 1) {
                continue;
            }

            //过滤器不是请求过滤器则跳过
            if (filterPo.getDirection() != 0) {
                continue;
            }

            //获取过滤器下的触发器与操作
            var triggerPoList = filterPo.getTriggers();
            var operationPoList = filterPo.getOperations();

            //无触发器或操作则跳过
            if (triggerPoList.isEmpty() || operationPoList.isEmpty()) {
                continue;
            }

            //判断请求是否命中所有触发器 未全部命中则跳过
            var hitAll = true;
            for (var triggerPo : triggerPoList) {
                if (!isHitTrigger(rs, triggerPo)) {
                    hitAll = false;
                    break;
                }
            }
            if (!hitAll) {
                continue;
            }


            //遍历执行全部操作
            for (var item : operationPoList) {
                doRequestOperation(rs, item);
            }

        }


    }


    /**
     * 执行请求操作
     *
     * @param rs          请求Schema
     * @param operationPo 操作列表
     */
    public void doRequestOperation(RequestSchema rs, SimpleFilterOperationPo operationPo) {

        //类型 0:持久化 1:缓存 2:注入缓存 3.注入持久化 4:覆写URL
        var kind = operationPo.getKind();
        //目标 0:标头 1:JSON载荷 2:URL(仅限kind=4)
        var target = operationPo.getTarget();

        var f = operationPo.getF();
        var t = operationPo.getT();

        //处理持久化 0:持久化 1:缓存 2:注入缓存 3.注入持久化 4:覆写URL
        if (kind == 0) {

            //持久化标头 直接将标头中f字段的值存储到配置中 如果f为空则不存储
            if (target == 0) {
                var value = rs.getHeader(t);
                if (StringUtils.isNotBlank(value)) {
                    //globalConfigService.setValue(t, value);
                }
            }

            //持久化JSON载荷 解析f字段的路径 将其值存储到配置中 如果f为空则不存储
            if (target == 1) {
                var value = GsonUtils.getFromPath(gson.fromJson(new String(rs.getBody(), StandardCharsets.UTF_8), JsonElement.class), f);
                if (StringUtils.isNotBlank(value)) {
                    //globalConfigService.setValue(t, value);
                }
            }

        }

        //处理缓存(暂不开发)
        if (kind == 1) {
            //缓存标头 直接将标头中f字段的值存储到缓存中 如果f为空则不存储
            if (target == 0) {
                log.info("处理缓存标头");
            }

            //缓存JSON载荷 解析f字段的路径 将其值存储到缓存中 如果f为空则不存储
            if (target == 1) {
                log.info("处理缓存JSON载荷");
            }
        }

        //处理注入缓存(暂不开发)
        if (kind == 2) {
            //注入缓存标头 直接将标头中f字段的值存储到缓存中 如果f为空则不存储
            if (target == 0) {
                log.info("处理注入缓存标头");
            }

            //注入缓存JSON载荷 解析f字段的路径 将其值存储到缓存中 如果f为空则不存储
            if (target == 1) {
                log.info("处理注入缓存JSON载荷");
            }
        }

        //处理注入持久化
        if (kind == 3) {

            //注入持久化标头 从配置中获取f键的值 并注入到t标头
            if (target == 0) {
                //var value = globalConfigService.get(f);
                //if (StringUtils.isNotBlank(value)) {
                //    rs.setHeader(t, value);
                //}
            }

            //注入持久化JSON载荷 解析f字段的路径 将其值存储到配置中 如果f为空则不存储
            if (target == 1) {
                //var value = globalConfigService.get(f);
                //if (StringUtils.isNotBlank(value)) {
                //    var jsonElement = gson.fromJson(value, JsonElement.class);
                //    Map<String, String> map = new HashMap<>();
                //    map.put(t, value);
                //    GsonUtils.injectContent(jsonElement, map);
                //    rs.setBody(gson.toJson(jsonElement).getBytes(StandardCharsets.UTF_8));
                //}
            }

        }

        //处理覆写URL
        if (kind == 4) {
            rs.setHost(t);
        }

    }


    /**
     * 判断请求是否命中触发器
     *
     * @param rs        请求Schema
     * @param triggerPo 触发器
     * @return 是否命中触发器
     */
    private boolean isHitTrigger(RequestSchema rs, SimpleFilterTriggerPo triggerPo) {

        var tgt = triggerPo.getTarget();
        var kind = triggerPo.getKind();
        var tk = triggerPo.getTk();
        var tv = triggerPo.getTv();

        //处理总是触发
        if (tgt == 4) {
            return true;
        }


        //TGT 0:标头 1:JSON载荷 2:URL 3:HTTP方法 4:总是触发

        //处理标头
        if (tgt == 0) {

            Optional<HttpHeaderVo> headerOpt = rs.getHeaders().stream()
                    .filter(h -> h.getK().equalsIgnoreCase(tk))
                    .findFirst();

            //标头包含(如果只填写tk则判断标头中是否有tk字段 如果同时填写了tk+tv则判断标头中tk字段的值是否包含tv)
            if (kind == 0) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isPresent();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(httpHeaderVo -> httpHeaderVo.getV().contains(tv)).orElse(false);
                }
            }

            //标头不包含(如果只填写tk则判断标头中是否没有tk字段 如果同时填写了tk+tv则判断标头中tk字段的值是否不包含tv)
            if (kind == 1) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isEmpty();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(httpHeaderVo -> !httpHeaderVo.getV().contains(tv)).orElse(true);
                }
            }

            //标头等于(如果只填写tk则判断标头中是否有tk字段 如果同时填写了tk+tv则判断标头中tk字段的值是否等于tv)
            if (kind == 2) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isPresent();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(httpHeaderVo -> httpHeaderVo.getV().equals(tv)).orElse(false);
                }
            }

            //标头不等于(如果只填写tk则判断标头中是否没有tk字段 如果同时填写了tk+tv则判断标头中tk字段的值是否不等于tv)
            if (kind == 3) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isEmpty();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(httpHeaderVo -> !httpHeaderVo.getV().equals(tv)).orElse(true);
                }
            }

        }

        //处理JSON载荷
        if (tgt == 1) {

            //如果载荷不是JSON则直接返回false
            if (!rs.getContentType().contains("application/json")) {
                return false;
            }

            String jsonPayload = new String(rs.getBody(), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(jsonPayload)) {
                return false;
            }
            JsonElement jsonElement = gson.fromJson(jsonPayload, JsonElement.class);
            String valueFromPath = GsonUtils.getFromPath(jsonElement, tk);


            //载荷包含(只填写tk则解析json下是否存在tk 如果同时填写了tk+tv则判断json下tk字段的值是否包含tv)
            if (kind == 0) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return valueFromPath != null;
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return valueFromPath != null && valueFromPath.contains(tv);
                }
            }

            //载荷不包含(只填写tk则解析json下是否不存在tk 如果同时填写了tk+tv则判断json下tk字段的值是否不包含tv)
            if (kind == 1) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return valueFromPath == null;
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return valueFromPath == null || !valueFromPath.contains(tv);
                }
            }

            //载荷等于(如果只填写tk则判断json下tk字段的值是否等于tv 如果同时填写了tk+tv则判断json下tk字段的值是否等于tv)
            if (kind == 2) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return tv.equals(valueFromPath);
                }
            }

            //载荷不等于(如果只填写tk则判断json下tk字段的值是否不等于tv 如果同时填写了tk+tv则判断json下tk字段的值是否不等于tv)
            if (kind == 3) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return !tv.equals(valueFromPath);
                }
            }

        }

        //处理URL
        if (tgt == 2) {
            String url = rs.getUrl();
            if (StringUtils.isBlank(url)) {
                return false;
            }
            //URL包含(只能填写tv 直接判断URL是否包含tv)
            if (kind == 0) {
                return url.contains(tv);
            }

            //URL不包含(只能填写tv 直接判断URL是否不包含tv)
            if (kind == 1) {
                return !url.contains(tv);
            }

            //URL等于(只能填写tv 直接判断URL是否等于tv)
            if (kind == 2) {
                return url.equals(tv);
            }

            //载荷不等于(只能填写tv 直接判断URL是否不等于tv)
            if (kind == 3) {
                return !url.equals(tv);
            }

        }

        //处理HTTP方法
        if (tgt == 3) {
            String method = rs.getMethod();
            if (StringUtils.isBlank(method)) {
                return false;
            }
            //HTTP方法包含(只能填写tv 直接判断HTTP方法是否包含tv)
            if (kind == 0) {
                return method.contains(tv);
            }

            //HTTP方法不包含(只能填写tv 直接判断HTTP方法是否不包含tv)
            if (kind == 1) {
                return !method.contains(tv);
            }

            //HTTP方法等于(只能填写tv 直接判断HTTP方法是否等于tv)
            if (kind == 2) {
                return method.equalsIgnoreCase(tv);
            }

            //HTTP方法不等于(只能填写tv 直接判断HTTP方法是否不等于tv)
            if (kind == 3) {
                return !method.equalsIgnoreCase(tv);
            }
        }
        return false;
    }

    /**
     * 判断响应是否命中触发器
     *
     * @param rs        响应
     * @param triggerPo 触发器
     * @return 是否命中触发器
     */
    private boolean isHitTrigger(HttpResponse<String> rs, SimpleFilterTriggerPo triggerPo) {

        var tgt = triggerPo.getTarget();
        var kind = triggerPo.getKind();
        var tk = triggerPo.getTk();
        var tv = triggerPo.getTv();

        // 总是触发
        if (tgt == 4) {
            return true;
        }

        // HTTP方法不适用于响应
        if (tgt == 3) {
            return false;
        }

        //处理URL
        if (tgt == 2) {
            String url = rs.uri().toString();
            if (StringUtils.isBlank(url)) {
                return false;
            }
            //URL包含(只能填写tv 直接判断URL是否包含tv)
            if (kind == 0) {
                return url.contains(tv);
            }
            //URL不包含(只能填写tv 直接判断URL是否不包含tv)
            if (kind == 1) {
                return !url.contains(tv);
            }
            //URL等于(只能填写tv 直接判断URL是否等于tv)
            if (kind == 2) {
                return url.equals(tv);
            }
            //URL不等于(只能填写tv 直接判断URL是否不等于tv)
            if (kind == 3) {
                return !url.equals(tv);
            }
        }

        // 处理标头
        if (tgt == 0) {
            Optional<String> headerOpt = rs.headers().firstValue(tk);

            // 包含
            if (kind == 0) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isPresent();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(v -> v.contains(tv)).orElse(false);
                }
            }

            // 不包含
            if (kind == 1) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isEmpty();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(v -> !v.contains(tv)).orElse(true);
                }
            }

            // 等于
            if (kind == 2) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isPresent();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(v -> v.equals(tv)).orElse(false);
                }
            }

            // 不等于
            if (kind == 3) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return headerOpt.isEmpty();
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return headerOpt.map(v -> !v.equals(tv)).orElse(true);
                }
            }
        }

        // 处理JSON载荷
        if (tgt == 1) {
            Optional<String> contentTypeOpt = rs.headers().firstValue("Content-Type");
            if (contentTypeOpt.isEmpty() || !contentTypeOpt.get().contains("application/json")) {
                return false;
            }

            String jsonPayload = rs.body();
            if (StringUtils.isBlank(jsonPayload)) {
                return false;
            }
            JsonElement jsonElement = gson.fromJson(jsonPayload, JsonElement.class);
            String valueFromPath = GsonUtils.getFromPath(jsonElement, tk);

            // 包含
            if (kind == 0) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    return valueFromPath != null;
                }
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return valueFromPath != null && valueFromPath.contains(tv);
                }
            }

            // 不包含
            if (kind == 1) {

                //只填写了tk 则判断json下是否不存在tk
                if (StringUtils.isNotBlank(tk) && StringUtils.isBlank(tv)) {
                    if (valueFromPath == null) {
                        return true;
                    }
                    return false;
                }

                //同时填写了tk和tv 则判断json下tk字段的值是否不包含tv
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    if (valueFromPath == null) {
                        return true;
                    }
                    if (valueFromPath.contains(tv)) {
                        return false;
                    }
                    return true;
                }
            }

            // 等于
            if (kind == 2) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return tv.equals(valueFromPath);
                }
            }

            // 不等于
            if (kind == 3) {
                if (StringUtils.isNotBlank(tk) && StringUtils.isNotBlank(tv)) {
                    return !tv.equals(valueFromPath);
                }
            }
        }

        return false;
    }


}
