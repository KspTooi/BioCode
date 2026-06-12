package com.ksptool.bio.commons.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RelayHeader {

    @Schema(description = "是否默认")
    private Boolean d;

    @Schema(description = "是否启用")
    private Boolean e;

    @Schema(description = "是否自动计算")
    private Boolean a;

    @Schema(description = "请求头键")
    private String k;

    @Schema(description = "请求头值")
    private String v;

    @Schema(description = "排序")
    private Integer s;

    public static RelayHeader of(String k, String v) {
        RelayHeader relayHeader = new RelayHeader();
        relayHeader.setD(false);
        relayHeader.setE(false);
        relayHeader.setA(false);
        relayHeader.setK(k);
        relayHeader.setV(v);
        relayHeader.setS(0);
        return relayHeader;
    }

    public static RelayHeader ofDefault(String k, String v, Integer s) {
        RelayHeader relayHeader = new RelayHeader();
        relayHeader.setD(true);
        relayHeader.setE(true);
        relayHeader.setA(false);
        relayHeader.setK(k);
        relayHeader.setV(v);
        relayHeader.setS(s);
        return relayHeader;
    }

    public static RelayHeader ofDefaultAuto(String k, String v, Integer s) {
        RelayHeader relayHeader = new RelayHeader();
        relayHeader.setD(true);
        relayHeader.setE(true);
        relayHeader.setA(true);
        relayHeader.setK(k);
        relayHeader.setV(v);
        relayHeader.setS(s);
        return relayHeader;
    }

    public static List<RelayHeader> ofDefaultList() {
        List<RelayHeader> relayHeaderList = new ArrayList<>();
        relayHeaderList.add(ofDefaultAuto("eas-token", "auto-calculated", -100));
        relayHeaderList.add(ofDefaultAuto("Content-Type", "auto-calculated", -95));
        relayHeaderList.add(ofDefaultAuto("Content-Length", "auto-calculated", -90));
        relayHeaderList.add(ofDefaultAuto("Host", "auto-calculated", -85));
        relayHeaderList.add(ofDefault("User-Agent", "EasServer/1.4M/CP23", -80));
        relayHeaderList.add(ofDefault("Accept", "*/*", -75));
        relayHeaderList.add(ofDefault("Accept-Encoding", "gzip, deflate, br", -70));
        relayHeaderList.add(ofDefault("Connection", "keep-alive", -65));
        return relayHeaderList;
    }


    public static int count(List<RelayHeader> header, String hk) {
        int count = 0;
        for (var h : header) {
            if (StringUtils.isBlank(h.getK())) {
                continue;
            }
            if (h.getK().toLowerCase().equals(hk.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    public static String firstValue(List<RelayHeader> header, String hk) {
        return values(header, hk).stream().findFirst().orElse(null);
    }

    public static List<String> values(List<RelayHeader> header, String hk) {
        List<String> values = new ArrayList<>();
        for (var h : header) {
            if (StringUtils.isBlank(h.getK())) {
                continue;
            }
            if (h.getK().toLowerCase().equals(hk.toLowerCase())) {
                values.add(h.getV());
            }
        }
        return values;
    }


    @Override
    public String toString() {
        return "RelayHeader[d=" + d + ", e=" + e + ", a=" + a + ", k=" + k + ", v=" + v + ", s=" + s + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RelayHeader that = (RelayHeader) obj;
        //只要k、v相等就认为相等
        return k.equals(that.k) && v.equals(that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, v);
    }

}
