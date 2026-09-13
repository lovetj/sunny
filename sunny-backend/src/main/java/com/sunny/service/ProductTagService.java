package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.common.PageResult;
import com.sunny.dto.PageDTO;
import com.sunny.dto.ProductTagDTO;
import com.sunny.entity.ProductTag;

import java.util.List;

public interface ProductTagService extends IService<ProductTag> {
    List<ProductTag> listAll();

    PageResult<ProductTag> pageList(PageDTO pageDTO);

    void addTag(ProductTagDTO dto);

    void updateTag(ProductTagDTO dto);

    void deleteTag(Long id);

    void deleteBatch(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}
