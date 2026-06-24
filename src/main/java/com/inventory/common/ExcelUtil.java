package com.inventory.common;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class ExcelUtil {

    /**
     * 导出 Excel（单sheet，最多100万行）
     */
    public static <T> void export(HttpServletResponse response, 
                                  List<T> data, 
                                  Class<T> clazz, 
                                  String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet("数据")
                .doWrite(data);
    }

    /**
     * 导入 Excel（分批读取，适合大文件）
     */
    public static <T> void importExcel(Consumer<List<T>> consumer,
                                       Class<T> clazz,
                                       String filePath) {
        EasyExcel.read(filePath, clazz, new PageReadListener<T>(consumer))
                .sheet()
                .doRead();
    }
}