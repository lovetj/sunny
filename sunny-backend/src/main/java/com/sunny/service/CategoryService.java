package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.common.PageResult;
import com.sunny.dto.CategoryDTO;
import com.sunny.dto.PageDTO;
import com.sunny.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> listAll();

    PageResult<Category> pageList(PageDTO pageDTO);

    void addCategory(CategoryDTO dto);

    void updateCategory(CategoryDTO dto);

    void deleteCategory(Long id);

    void deleteBatch(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}

