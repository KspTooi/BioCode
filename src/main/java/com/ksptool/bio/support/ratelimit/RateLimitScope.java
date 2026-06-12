package com.ksptool.bio.support.ratelimit;

public enum RateLimitScope {

    //全局限流
    GLOBAL,

    //IP地址限流
    IP_ADDRESS,

    //用户ID限流
    USER_ID,

}
