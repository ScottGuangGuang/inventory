package com.inventory.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.entity.Product;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}