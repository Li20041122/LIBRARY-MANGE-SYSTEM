package edu.fosu.book.controller;

import edu.fosu.book.common.BusinessException;
import edu.fosu.book.common.Result;
import edu.fosu.book.dao.UserMapper;
import edu.fosu.book.dto.PageResult;
import edu.fosu.book.entity.User;
import edu.fosu.book.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable String id) {
        User user = userService.selectByPrimaryKey(id);
        if (user != null) {
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } else {
            throw new BusinessException(404, "用户不存在");
        }
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 不返回密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/page")
    public Result<PageResult<User>> getUserPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<User> list = userMapper.selectPage(keyword, offset, size);
        // 不返回密码
        list.forEach(u -> u.setPassword(null));
        long total = userMapper.countAll(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PostMapping
    public Result<User> insertUser(@RequestBody User user) {
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 默认角色为普通用户
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }
        // 空字符串转为null，避免外键约束错误
        if (user.getDepartid() != null && user.getDepartid().isEmpty()) {
            user.setDepartid(null);
        }
        User savedUser = userService.insertUser(user);
        if (savedUser != null) {
            savedUser.setPassword(null);
            return Result.success(savedUser);
        } else {
            throw new BusinessException("添加用户失败");
        }
    }

    @PutMapping("/{id}")
    public Result<User> updateUserById(@PathVariable String id, @RequestBody User user) {
        if (!id.equals(user.getUserid())) {
            throw new BusinessException(400, "ID不匹配");
        }

        User existingUser = userService.selectByPrimaryKey(id);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 如果密码为空，保留原密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else {
            // 加密新密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 空字符串转为null，避免外键约束错误
        if (user.getDepartid() != null && user.getDepartid().isEmpty()) {
            user.setDepartid(null);
        }

        User updatedUser = userService.updateByPrimaryKey(user);
        if (updatedUser != null) {
            updatedUser.setPassword(null);
            return Result.success(updatedUser);
        } else {
            throw new BusinessException("更新用户失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUserById(@PathVariable String id) {
        User existingUser = userService.selectByPrimaryKey(id);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userService.deleteByPrimaryKey(id);
        return Result.success();
    }
}
