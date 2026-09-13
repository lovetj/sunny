package com.sunny.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunny.common.PageResult;
import com.sunny.dto.PageDTO;
import com.sunny.dto.ProductTagDTO;
import com.sunny.entity.ProductTag;
import com.sunny.mapper.ProductTagMapper;
import com.sunny.service.ProductTagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductTagServiceImpl extends ServiceImpl<ProductTagMapper, ProductTag> implements ProductTagService {

    @Override
    public List<ProductTag> listAll() {
        LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTag::getStatus, 1)
                .orderByAsc(ProductTag::getSort)
                .orderByDesc(ProductTag::getCreateTime);
        return list(wrapper);
    }

    @Override
    public PageResult<ProductTag> pageList(PageDTO pageDTO) {
        Page<ProductTag> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(pageDTO.getKeyword())) {
            String kw = pageDTO.getKeyword().trim();
            wrapper.like(ProductTag::getName, kw);
        }
        if (pageDTO.getStatus() != null) {
            wrapper.eq(ProductTag::getStatus, pageDTO.getStatus());
        }
        wrapper.orderByAsc(ProductTag::getSort).orderByDesc(ProductTag::getCreateTime);
        Page<ProductTag> result = page(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    private void checkUniqueName(String name, Long excludeId) {
        if (StringUtils.hasText(name)) {
            LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<ProductTag>()
                    .eq(ProductTag::getName, name.trim());
            if (excludeId != null) {
                wrapper.ne(ProductTag::getId, excludeId);
            }
            if (count(wrapper) > 0) {
                throw new RuntimeException("标签名称 [" + name.trim() + "] 已存在");
            }
        }
    }

    @Override
    public void addTag(ProductTagDTO dto) {
        checkUniqueName(dto.getName(), null);
        ProductTag tag = new ProductTag();
        BeanUtil.copyProperties(dto, tag);
        if (tag.getStatus() == null) {
            tag.setStatus(1);
        }
        if (tag.getSort() == null) {
            tag.setSort(0);
        }
        if (tag.getIsHotselling() == null) {
            tag.setIsHotselling(0);
        }
        save(tag);
    }

    @Override
    public void updateTag(ProductTagDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("标签ID不能为空");
        }
        checkUniqueName(dto.getName(), dto.getId());
        ProductTag tag = new ProductTag();
        BeanUtil.copyProperties(dto, tag);
        updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
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
        ProductTag tag = new ProductTag();
        tag.setId(id);
        tag.setStatus(status);
        updateById(tag);
    }

    @Override
    public void updateStatusBatch(List<Long> ids, Integer status) {
        if (ids != null && !ids.isEmpty()) {
            List<ProductTag> tags = ids.stream().map(id -> {
                ProductTag t = new ProductTag();
                t.setId(id);
                t.setStatus(status);
                return t;
            }).collect(Collectors.toList());
            updateBatchById(tags);
        }
    }
}
