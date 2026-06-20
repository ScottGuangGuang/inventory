package com.inventory.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.common.PageResult;
import com.inventory.common.RedisUtil;
import com.inventory.common.Result;
import com.inventory.entity.Product;
import com.inventory.mapper.ProductMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping
    public Result<List<Product>> list() {
        return Result.success(productMapper.selectList(null));
    }

    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        String cacheKey = "products:page:" + pageNum + ":" + pageSize + ":" + (keyword == null ? "all" : keyword);

        // 从缓存读取（使用 TypeReference 保留泛型信息）
        PageResult<Product> cached = redisUtil.get(cacheKey, new TypeReference<PageResult<Product>>() {});
        if (cached != null) {
            return Result.success(cached);
        }

        // 查询数据库
        Page<Product> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        wrapper.orderByDesc("created_at");

        Page<Product> result = productMapper.selectPage(page, wrapper);
        PageResult<Product> pageResult = PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        // 存入缓存
        redisUtil.set(cacheKey, pageResult);

        return Result.success(pageResult);
    }

    // 新增商品（开启校验）
    @PostMapping
    public Result<String> create(@Valid @RequestBody Product product) {
        int rows = productMapper.insert(product);
        return rows > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 更新商品（开启校验）
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @Valid @RequestBody Product product) {
        product.setId(id);
        int rows = productMapper.updateById(product);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        int rows = productMapper.deleteById(id);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}