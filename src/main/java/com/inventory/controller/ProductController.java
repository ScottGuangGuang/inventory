package com.inventory.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.common.PageResult;
import com.inventory.common.RedisUtil;
import com.inventory.common.Result;
import com.inventory.entity.Product;
import com.inventory.entity.ProductExcelVO;
import com.inventory.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;   // 改为注入 Service
    @Autowired
    private RedisUtil redisUtil;

    // ========== 原有接口（改用 Service 实现） ==========

    @GetMapping
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        String cacheKey = "products:page:" + pageNum + ":" + pageSize + ":" + (keyword == null ? "all" : keyword);

        PageResult<Product> cached = redisUtil.get(cacheKey, new TypeReference<PageResult<Product>>() {});
        if (cached != null) {
            return Result.success(cached);
        }

        Page<Product> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        wrapper.orderByDesc("created_at");

        Page<Product> result = productService.page(page, wrapper);  // 改用 Service
        PageResult<Product> pageResult = PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        redisUtil.set(cacheKey, pageResult);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody Product product) {
        boolean success = productService.save(product);  // 改用 Service
        if (success) {
            redisUtil.deleteKeys("products:page:*");
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @Valid @RequestBody Product product) {
        product.setId(id);
        boolean success = productService.updateById(product);  // 改用 Service
        if (success) {
            redisUtil.deleteKeys("products:page:*");
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = productService.removeById(id);  // 改用 Service
        if (success) {
            redisUtil.deleteKeys("products:page:*");
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    // ========== 新增：Excel 导入导出 ==========

    /**
     * 导出所有商品
     * GET /api/products/export
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<Product> products = productService.list();
        List<ProductExcelVO> excelData = new ArrayList<>();
        for (Product p : products) {
            ProductExcelVO vo = new ProductExcelVO();
            BeanUtils.copyProperties(p, vo);
            excelData.add(vo);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("商品库存_" + System.currentTimeMillis(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ProductExcelVO.class)
                .sheet("商品数据")
                .doWrite(excelData);
    }

    /**
     * 导入商品（批量新增）
     * POST /api/products/import
     */
    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.matches(".+\\.(xlsx|xls)$")) {
            return Result.error("请上传Excel文件（.xlsx或.xls）");
        }

        // 读取 Excel
        List<ProductExcelVO> excelData = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), ProductExcelVO.class, new PageReadListener<ProductExcelVO>(excelData::addAll))
                .sheet()
                .doRead();

        if (excelData.isEmpty()) {
            return Result.error("Excel文件为空，请填写数据后重试");
        }

        // 转换为 Product 实体
        List<Product> products = new ArrayList<>();
        for (ProductExcelVO vo : excelData) {
            Product p = new Product();
            BeanUtils.copyProperties(vo, p);
            products.add(p);
        }

        // MyBatis-Plus 批量插入
        boolean success = productService.saveBatch(products);

        // 清除分页缓存
        redisUtil.deleteKeys("products:page:*");

        return success ? Result.success("导入成功，共 " + products.size() + " 条记录")
                : Result.error("导入失败");
    }

    /**
     * 下载导入模板
     * GET /api/products/export/template
     */
    @GetMapping("/export/template")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        List<ProductExcelVO> template = new ArrayList<>();
        ProductExcelVO example = new ProductExcelVO();
        example.setName("示例：iPhone 15");
        example.setCode("P20240001");
        example.setCategory("电子产品");
        example.setPrice(new java.math.BigDecimal("5999.00"));
        example.setQuantity(100);
        example.setMinStock(10);
        example.setRemark("请删除示例数据后填写");
        template.add(example);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("商品导入模板", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ProductExcelVO.class)
                .sheet("导入模板")
                .doWrite(template);
    }
}