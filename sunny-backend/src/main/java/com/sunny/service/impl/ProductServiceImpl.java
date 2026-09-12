package com.sunny.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunny.common.PageResult;
import com.sunny.dto.PageDTO;
import com.sunny.dto.ProductDTO;
import com.sunny.entity.Product;
import com.sunny.mapper.ProductMapper;
import com.sunny.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public List<Product> listAll() {
        return baseMapper.selectProductsWithCategory();
    }

    @Override
    public List<Product> listByCategoryId(Long categoryId) {
        return baseMapper.selectByCategoryId(categoryId);
    }

    @Override
    public PageResult<Product> pageList(PageDTO pageDTO) {
        Page<Product> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        IPage<Product> result = baseMapper.selectPageWithCategory(page, pageDTO.getKeyword(), pageDTO.getCategoryId(), pageDTO.getStatus());
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    @Override
    public void addProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        product.setSales(0);
        save(product);
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        removeById(id);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        updateById(product);
    }

    @Override
    public void updateStatusBatch(List<Long> ids, Integer status) {
        if (ids != null && !ids.isEmpty()) {
            List<Product> products = ids.stream().map(id -> {
                Product p = new Product();
                p.setId(id);
                p.setStatus(status);
                return p;
            }).collect(java.util.stream.Collectors.toList());
            updateBatchById(products);
        }
    }
}
