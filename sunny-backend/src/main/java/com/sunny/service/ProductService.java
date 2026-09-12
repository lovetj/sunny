package com.sunny.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.common.PageResult;
import com.sunny.dto.PageDTO;
import com.sunny.dto.ProductDTO;
import com.sunny.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {
    List<Product> listAll();

    List<Product> listByCategoryId(Long categoryId);

    PageResult<Product> pageList(PageDTO pageDTO);

    void addProduct(ProductDTO dto);

    void updateProduct(ProductDTO dto);

    void deleteProduct(Long id);

    void deleteBatch(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}
