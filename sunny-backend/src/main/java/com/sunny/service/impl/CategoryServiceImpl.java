package com.sunny.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunny.common.PageResult;
import com.sunny.dto.CategoryDTO;
import com.sunny.dto.PageDTO;
import com.sunny.entity.Category;
import com.sunny.entity.Product;
import com.sunny.mapper.CategoryMapper;
import com.sunny.mapper.ProductMapper;
import com.sunny.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort);
        return list(wrapper);
    }

    @Override
    public PageResult<Category> pageList(PageDTO pageDTO) {
        Page<Category> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(pageDTO.getKeyword())) {
            String kw = pageDTO.getKeyword().trim();
            wrapper.and(w -> w.like(Category::getName, kw).or().like(Category::getCode, kw));
        }
        if (pageDTO.getStatus() != null) {
            wrapper.eq(Category::getStatus, pageDTO.getStatus());
        }
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        Page<Category> result = page(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    private void checkUniqueNameAndCode(String name, String code, Long excludeId) {
        if (StringUtils.hasText(name)) {
            LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, name.trim());
            if (excludeId != null) {
                nameWrapper.ne(Category::getId, excludeId);
            }
            if (count(nameWrapper) > 0) {
                throw new RuntimeException("分类名称 [" + name.trim() + "] 已存在");
            }
        }
        if (StringUtils.hasText(code)) {
            LambdaQueryWrapper<Category> codeWrapper = new LambdaQueryWrapper<Category>()
                    .eq(Category::getCode, code.trim());
            if (excludeId != null) {
                codeWrapper.ne(Category::getId, excludeId);
            }
            if (count(codeWrapper) > 0) {
                throw new RuntimeException("分类编码 [" + code.trim() + "] 已存在");
            }
        }
    }

    @Override
    public void addCategory(CategoryDTO dto) {
        checkUniqueNameAndCode(dto.getName(), dto.getCode(), null);
        Category category = new Category();
        BeanUtil.copyProperties(dto, category);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        save(category);
    }

    @Override
    public void updateCategory(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        checkUniqueNameAndCode(dto.getName(), dto.getCode(), dto.getId());
        Category category = new Category();
        BeanUtil.copyProperties(dto, category);
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getCategoryId, id);
        Long productCount = productMapper.selectCount(productWrapper);
        if (productCount != null && productCount > 0) {
            throw new RuntimeException("该分类下存在 " + productCount + " 个关联商品，无法直接删除！请先转移或删除商品。");
        }
        removeById(id);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.in(Product::getCategoryId, ids);
            Long productCount = productMapper.selectCount(productWrapper);
            if (productCount != null && productCount > 0) {
                throw new RuntimeException("所选分类中存在关联商品，无法直接删除！");
            }
            removeByIds(ids);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        updateById(category);
    }

    @Override
    public void updateStatusBatch(List<Long> ids, Integer status) {
        if (ids != null && !ids.isEmpty()) {
            List<Category> list = ids.stream().map(id -> {
                Category c = new Category();
                c.setId(id);
                c.setStatus(status);
                return c;
            }).collect(Collectors.toList());
            updateBatchById(list);
        }
    }
}

