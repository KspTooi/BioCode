package com.ksptool.bio.commons.routeselector;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class HttpRouteRule {

    //目标主机
    private String targetHost;
    //目标端口
    private Integer targetPort;
    //匹配类型 0:全部 1:来源IP地址
    private Integer matchType;
    //匹配键
    private String matchKey;
    //匹配操作 0:等于
    private Integer matchOperator;
    //匹配值
    private String matchValue;
    //权重
    private Integer seq;
    //流过该规则的请求数量
    private AtomicLong hitCount;
    //是否已熔断 0:否 1:是
    private AtomicInteger isBreaked;

    public HttpRouteRule() {
        this.hitCount = new AtomicLong(0);
        this.isBreaked = new AtomicInteger(0);
    }

    /**
     * 是否已熔断
     *
     * @return
     */
    public boolean isBreaked() {
        return isBreaked.get() == 1;
    }

}
