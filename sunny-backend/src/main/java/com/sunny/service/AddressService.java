package com.sunny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunny.dto.AddressDTO;
import com.sunny.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {

    List<Address> listByUserId(Long userId);

    Address getDetail(Long userId, Long id);

    Address getDefaultAddress(Long userId);

    void addAddress(Long userId, AddressDTO dto);

    void updateAddress(Long userId, AddressDTO dto);

    void deleteAddress(Long userId, Long id);

    void setDefaultAddress(Long userId, Long id);
}
