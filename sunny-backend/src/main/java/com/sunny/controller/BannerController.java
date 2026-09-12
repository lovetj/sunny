package com.sunny.controller;

import com.sunny.common.PageResult;
import com.sunny.common.Result;
import com.sunny.dto.BannerDTO;
import com.sunny.dto.BatchStatusDTO;
import com.sunny.dto.PageDTO;
import com.sunny.entity.Banner;
import com.sunny.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.listAll());
    }

    @GetMapping("/page")
    public Result<PageResult<Banner>> page(PageDTO pageDTO) {
        return Result.success(bannerService.pageList(pageDTO));
    }

    @GetMapping("/{id}")
    public Result<Banner> detail(@PathVariable Long id) {
        return Result.success(bannerService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody BannerDTO dto) {
        bannerService.addBanner(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody BannerDTO dto) {
        bannerService.updateBanner(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        bannerService.deleteBatch(ids);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDeletePost(@RequestBody List<Long> ids) {
        bannerService.deleteBatch(ids);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        bannerService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@Valid @RequestBody BatchStatusDTO dto) {
        bannerService.updateStatusBatch(dto.getIds(), dto.getStatus());
        return Result.success();
    }
}
