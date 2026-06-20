package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // BaseMapper 已经提供了基本的 CRUD 方法：
    // insert, deleteById, updateById, selectById, selectList, 等
    // 这里暂时不需要额外写方法
}