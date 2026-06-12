package com.ksptool.bio.commons.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RelayBody {

    @Schema(description = """
                请求体类型
                0:空
                1:form-data
                2:raw-text
                3:raw-javascription
                4:raw-json
                5:raw-html
                6:raw-xml
                7:binary
                8:x-www-form-urlencoded
            """)
    private Integer kind;

    @Schema(description = "请求体数据(FORM-DATA)")
    private List<RelayParam> formData;

    @Schema(description = "请求体数据(X-WWW-FORM-URL-ENCODED)")
    private List<RelayParam> formDataUrlEncoded;

    @Schema(description = "请求体数据(RAW)")
    private String rawData;

    @Schema(description = "请求体数据(BINARY)")
    private String binaryData;

    /**
     * 默认请求体
     *
     * @return 默认请求体
     */
    public static RelayBody ofDefault() {
        RelayBody relayBody = new RelayBody();
        relayBody.setKind(0);
        relayBody.setFormData(new ArrayList<>());
        relayBody.setFormDataUrlEncoded(new ArrayList<>());
        relayBody.setRawData("");
        relayBody.setBinaryData("");
        return relayBody;
    }

    public String getContentType() {
        if (this.kind == 0) {
            return "text/plain; charset=UTF-8";
        }
        if (this.kind == 1) {
            return "multipart/form-data";
        }
        if (this.kind == 2) {
            return "text/plain; charset=UTF-8";
        }
        if (this.kind == 3) {
            return "application/javascript; charset=UTF-8";
        }
        if (this.kind == 4) {
            return "application/json; charset=UTF-8";
        }
        if (this.kind == 5) {
            return "text/html; charset=UTF-8";
        }
        if (this.kind == 6) {
            return "application/xml; charset=UTF-8";
        }
        if (this.kind == 7) {
            return "application/octet-stream";
        }
        if (this.kind == 8) {
            return "application/x-www-form-urlencoded";
        }
        throw new IllegalArgumentException("不支持的请求体类型: " + this.kind);
    }

}
