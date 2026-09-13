package com.sunny.controller;

import com.sunny.common.PageResult;
import com.sunny.common.Result;
import com.sunny.dto.BatchStatusDTO;
import com.sunny.dto.LoginDTO;
import com.sunny.dto.PageDTO;
import com.sunny.dto.UserDTO;
import com.sunny.entity.User;
import com.sunny.service.UserService;
import com.sunny.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        User user = userService.getByUsername(dto.getUsername());
        if (user != null) {
            user.setPassword(null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody LoginDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<User> info(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @RequestParam(required = false) String username) {
        User user = null;
        if (authorization != null && jwtUtil.validateToken(authorization)) {
            Long userId = jwtUtil.getUserId(authorization);
            user = userService.getById(userId);
        } else if (username != null && !username.trim().isEmpty()) {
            user = userService.getByUsername(username);
        }
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error(401, "用户未登录");
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.listAll());
    }

    @GetMapping("/page")
    public Result<PageResult<User>> page(PageDTO pageDTO) {
        return Result.success(userService.pageList(pageDTO));
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO dto) {
        userService.addUser(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDeletePost(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@Valid @RequestBody BatchStatusDTO dto) {
        userService.updateStatusBatch(dto.getIds(), dto.getStatus());
        return Result.success();
    }
}
