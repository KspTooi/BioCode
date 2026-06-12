package com.ksptool.bio.commons.model;

import java.util.HashSet;
import java.util.Set;

public class HopByHopHeaders {

    private static final Set<String> HOP_BY_HOP_HEADERS = new HashSet<>();

    static {
        HOP_BY_HOP_HEADERS.add("connection");
        HOP_BY_HOP_HEADERS.add("keep-alive");
        HOP_BY_HOP_HEADERS.add("proxy-authenticate");
        HOP_BY_HOP_HEADERS.add("proxy-authorization");
        HOP_BY_HOP_HEADERS.add("te");
        HOP_BY_HOP_HEADERS.add("trailer");
        HOP_BY_HOP_HEADERS.add("transfer-encoding");
        HOP_BY_HOP_HEADERS.add("upgrade");
        HOP_BY_HOP_HEADERS.add("host");
        HOP_BY_HOP_HEADERS.add("content-length");
    }

    /**
     * 获取默认的hop-by-hop请求头
     *
     * @return 默认的hop-by-hop请求头
     */
    public static Set<String> ofDefault() {
        return new HashSet<>(HOP_BY_HOP_HEADERS);
    }


}
