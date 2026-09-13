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
import com.sunny.entity.ProductTag;
import com.sunny.mapper.ProductMapper;
import com.sunny.mapper.ProductTagMapper;
import com.sunny.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductTagMapper productTagMapper;

    @Override
    public List<Product> listAll() {
        return baseMapper.selectProductsWithCategory();
    }

    @Override
    public List<Product> listHotselling(Integer status) {
        List<ProductTag> hotTags = productTagMapper.selectList(
                new LambdaQueryWrapper<ProductTag>()
                        .eq(ProductTag::getIsHotselling, 1)
                        .eq(ProductTag::getStatus, 1)
        );
        if (hotTags == null || hotTags.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> hotTagIds = hotTags.stream().map(ProductTag::getId).collect(Collectors.toSet());
        List<Product> allProducts = baseMapper.selectProductsWithCategory();
        if (allProducts == null || allProducts.isEmpty()) {
            return Collections.emptyList();
        }
        return allProducts.stream()
                .filter(p -> {
                    if (status != null && !status.equals(p.getStatus())) {
                        return false;
                    }
                    List<Long> tagIds = parseTagIds(p.getTags());
                    return tagIds.stream().anyMatch(hotTagIds::contains);
                })
                .sorted((p1, p2) -> {
                    int s1 = p1.getStatus() != null ? p1.getStatus() : 0;
                    int s2 = p2.getStatus() != null ? p2.getStatus() : 0;
                    if (s1 != s2) {
                        return Integer.compare(s2, s1);
                    }
                    int sort1 = p1.getSort() != null ? p1.getSort() : 0;
                    int sort2 = p2.getSort() != null ? p2.getSort() : 0;
                    if (sort1 != sort2) {
                        return Integer.compare(sort1, sort2);
                    }
                    if (p1.getCreateTime() != null && p2.getCreateTime() != null) {
                        return p2.getCreateTime().compareTo(p1.getCreateTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    private List<Long> parseTagIds(String tagsStr) {
        if (!StringUtils.hasText(tagsStr)) {
            return Collections.emptyList();
        }
        try {
            String clean = tagsStr.trim();
            if (clean.startsWith("[") && clean.endsWith("]")) {
                clean = clean.substring(1, clean.length() - 1);
            }
            List<Long> tagIds = new ArrayList<>();
            for (String part : clean.split(",")) {
                String idStr = part.trim().replace("\"", "").replace("'", "");
                if (StringUtils.hasText(idStr)) {
                    tagIds.add(Long.parseLong(idStr));
                }
            }
            return tagIds;
        } catch (Exception e) {
            return Collections.emptyList();
        }
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
