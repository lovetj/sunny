package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.common.PageResult;
import com.sunny.dto.PageDTO;
import com.sunny.dto.UserDTO;
import com.sunny.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    List<User> listAll();

    PageResult<User> pageList(PageDTO pageDTO);

    void addUser(UserDTO dto);

    void updateUser(UserDTO dto);

    void deleteUser(Long id);

    void deleteBatch(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}
