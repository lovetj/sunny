package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.common.PageResult;
import com.sunny.dto.BannerDTO;
import com.sunny.dto.PageDTO;
import com.sunny.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> listAll();

    PageResult<Banner> pageList(PageDTO pageDTO);

    void addBanner(BannerDTO dto);

    void updateBanner(BannerDTO dto);

    void deleteBanner(Long id);

    void deleteBatch(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}
