package com.ksptool.bio.commons.dataprocess;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.ksptool.assembly.entity.exception.BizException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExportWizard<T> {

    private Class<T> targetClazz;

    private List<T> data;

    private HttpServletResponse response;

    private String suffix = ".xlsx";

    public ExportWizard(List<T> data, HttpServletResponse response) throws BizException {
        this.data = data;
        this.response = response;
        if (data == null || data.isEmpty()) {
            throw new BizException("导出数据为空");
        }
        if (response == null) {
            throw new BizException("响应对象不能为空");
        }
        targetClazz = (Class<T>) data.get(0).getClass();
    }

    /**
     * 导出数据
     *
     * @param prefix 前缀
     */
    public void transfer(String prefix) throws IOException {

        var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        var fileName = prefix + "#" + sdf.format(new Date()) + suffix;

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        EasyExcel.write(response.getOutputStream(), targetClazz)
                .sheet(prefix)
                .head(targetClazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new AttachImageWriteHandler())
                .doWrite(data);
    }


    public void setSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            suffix = ".xlsx";
        }
        if (!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }
        this.suffix = suffix;
    }

}
