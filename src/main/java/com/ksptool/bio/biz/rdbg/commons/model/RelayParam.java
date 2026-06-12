package com.ksptool.bio.commons.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@Setter
public class RelayParam {

    @Schema(description = "是否启用")
    private boolean e;

    @Schema(description = "k")
    private String k;

    @Schema(description = "v 格式为Drive://#{id1};Drive://#{id2}时为当前团队云盘中的文件")
    private String v;

    @Schema(description = "排序")
    private Integer s;

    /**
     * 将MultiValueMap转换为RelayParam列表
     *
     * @param map MultiValueMap
     * @return RelayParam列表
     */
    public static List<RelayParam> ofMultiValueMap(MultiValueMap<String, String> map) {

        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }

        List<RelayParam> params = new ArrayList<>();
        final AtomicInteger index = new AtomicInteger(0);
        map.forEach((k, v) -> {
            RelayParam param = new RelayParam();
            param.setE(true);
            param.setK(k);
            param.setV(v.get(0));
            param.setS(index.getAndIncrement());
            params.add(param);
        });

        //按排序处理查询参数
        params.sort(Comparator.comparingInt(RelayParam::getS));
        return params;
    }

    /**
     * 将RelayParam列表转换为MultiValueMap
     *
     * @param params RelayParam列表
     * @return MultiValueMap
     */
    public static MultiValueMap<String, String> toMultiValueMap(List<RelayParam> params) {
        if (params == null || params.isEmpty()) {
            return new LinkedMultiValueMap<>();
        }

        var mvMap = new LinkedMultiValueMap<String, String>();

        for (var param : params) {

            //跳过空参数
            if (StringUtils.isBlank(param.getK()) || StringUtils.isBlank(param.getV())) {
                continue;
            }
            //跳过那些未启用的参数
            if (!param.isE()) {
                continue;
            }
            mvMap.add(param.getK(), param.getV());
        }

        return mvMap;
    }

    @Override
    public String toString() {
        return "RelayParam[e=" + e + ", k=" + k + ", v=" + v + ", s=" + s + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RelayParam that = (RelayParam) obj;
        //只要k、v相等就认为相等
        return k.equals(that.k) && v.equals(that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, v);
    }

    /**
     * 是否为文本类型
     *
     * @return 是否为文本类型
     * 文本类型不包含任何含有drive://字符串的值
     */
    public boolean isText() {
        return StringUtils.isNotBlank(v) && v.toLowerCase().matches("^[^drive://]*$");
    }

    /**
     * 是否为文件类型
     *
     * @return 是否为文件类型
     * 只要值中含有drive://字符串就认为时文件类型
     */
    public boolean isFile() {
        return StringUtils.isNotBlank(v) && v.toLowerCase().contains("drive://");
    }

    /**
     * 获取文件ID列表
     *
     * @return 文件ID列表
     */
    public List<Long> toFileIds() {

        if (!isFile()) {
            throw new IllegalArgumentException("当前参数不是文件类型");
        }

        var isMultiFile = v.contains(";");

        //移除drive://协议前缀字符串
        var _v = v.toLowerCase().trim().replace("drive://", "");

        //处理单个文件 
        if (!isMultiFile) {
            return List.of(Long.parseLong(_v));
        }

        //处理多个文件
        var fileIdStrs = _v.split(";");
        return Arrays.stream(fileIdStrs).map(Long::parseLong).collect(Collectors.toList());
    }

}
