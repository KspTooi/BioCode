package com.ksptool.bio.commons.model;

import com.ksptool.bio.commons.utils.IdWorker;

import java.io.*;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpRelayMultipartFile {

    private final String boundary;
    private final List<InputStream> inputStreams = new ArrayList<>();

    //提前Footer 字节
    private final byte[] footerBytes;

    //记录不包含 Footer 的当前累积长度
    private long currentContentLength = 0;

    public HttpRelayMultipartFile() {
        this.boundary = "EasSystem14PCP2FormBoundary" + IdWorker.nextId();
        //预先生成 Footer (--boundary--\r\n)
        this.footerBytes = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
    }

    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    /**
     * 获取内容长度 (实时计算：当前累积 + 固定Footer)
     */
    public long getContentLength() {
        return currentContentLength + footerBytes.length;
    }

    public void addText(String name, String value) {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] rnBytes = "\r\n".getBytes(StandardCharsets.UTF_8);

        inputStreams.add(new ByteArrayInputStream(headerBytes));
        inputStreams.add(new ByteArrayInputStream(valueBytes));
        inputStreams.add(new ByteArrayInputStream(rnBytes));

        currentContentLength += headerBytes.length + valueBytes.length + rnBytes.length;
    }

    public void addFile(String name, File file) throws IOException {
        String fileName = file.getName();
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] rnBytes = "\r\n".getBytes(StandardCharsets.UTF_8);

        inputStreams.add(new ByteArrayInputStream(headerBytes));
        inputStreams.add(new FileInputStream(file));
        inputStreams.add(new ByteArrayInputStream(rnBytes));

        currentContentLength += headerBytes.length + file.length() + rnBytes.length;
    }

    public HttpRequest.BodyPublisher toBodyPublisher() {
        //创建临时列表，避免污染 this.inputStreams
        List<InputStream> streamsToSend = new ArrayList<>(this.inputStreams);

        //动态添加 Footer 流
        streamsToSend.add(new ByteArrayInputStream(footerBytes));

        //注意：SequenceInputStream 中的 FileInputStream 读取一次后即消耗殆尽
        //如果需要重试机制，需要重新创建 FileInputStream
        return HttpRequest.BodyPublishers.ofInputStream(() -> new SequenceInputStream(Collections.enumeration(streamsToSend)));
    }
}