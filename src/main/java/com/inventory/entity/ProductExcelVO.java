package com.inventory.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ColumnWidth(20)
public class ProductExcelVO {

    @ExcelProperty(value = "商品名称", index = 0)
    private String name;

    @ExcelProperty(value = "商品编码", index = 1)
    private String code;

    @ExcelProperty(value = "分类", index = 2)
    private String category;

    @ExcelProperty(value = "单价", index = 3)
    private BigDecimal price;

    @ExcelProperty(value = "库存数量", index = 4)
    private Integer quantity;

    @ExcelProperty(value = "最低库存预警", index = 5)
    private Integer minStock;

    @ExcelProperty(value = "备注", index = 6)
    private String remark;

    @ExcelProperty(value = "创建时间", index = 7)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}