package com.ksptool.bio.commons.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

@Getter
public class HttpRelayResponse {

    private final Set<RelayHeader> headers;

    private final InputStream body;

    private HttpResponse<InputStream> response;

    public HttpRelayResponse(HttpResponse<InputStream> response) {
        this.response = response;

        //处理响应头
        var retHeaders = new HashSet<RelayHeader>();

        for (var entry : response.headers().map().entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue()) {
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                retHeaders.add(RelayHeader.of(name, value));
            }
        }
        this.headers = retHeaders;
        this.body = response.body();
    }

    public String firstHeaderValue(String name) {
        return firstHeaderValue(name, null);
    }

    public String firstHeaderValue(String name, String orElse) {
        for (var header : headers) {
            if (StringUtils.isBlank(header.getK())) {
                continue;
            }
            if (header.getK().equalsIgnoreCase(name)) {
                return header.getV();
            }
        }
        return orElse;
    }

}
