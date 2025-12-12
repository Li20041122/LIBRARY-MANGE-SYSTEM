package edu.fosu.book.controller;

import edu.fosu.book.common.BusinessException;
import edu.fosu.book.common.Result;
import edu.fosu.book.dao.UserMapper;
import edu.fosu.book.dto.ChangePasswordRequest;
import edu.fosu.book.dto.LoginRequest;
import edu.fosu.book.dto.RegisterRequest;
import edu.fosu.book.dto.UserInfo;
import edu.fosu.book.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 检查用户ID是否已存在
        User existingUser = userMapper.selectByPrimaryKey(request.getUserid());
        if (existingUser != null) {
            throw new BusinessException(400, "该用户ID已被注册");
        }

        // 检查用户名是否已存在
        User existingUsername = userMapper.selectByUsername(request.getUsername());
        if (existingUsername != null) {
            throw new BusinessException(400, "该用户名已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUserid(request.getUserid());
        user.setUsername(request.getUsername());
        // 密码使用BCrypt加密
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhonenum(request.getPhonenum());
        user.setSex(request.getSex());
        // 空字符串转为null，避免外键约束错误
        if (request.getDepartid() != null && !request.getDepartid().isEmpty()) {
            user.setDepartid(request.getDepartid());
        } else {
            user.setDepartid(null);
        }
        // 默认角色为普通用户
        user.setRole("user");

        userMapper.insert(user);

        return Result.success();
    }

    @PostMapping("/login")
    public Result<UserInfo> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证密码（支持明文和BCrypt加密两种方式）
        boolean passwordMatch = false;
        if (user.getPassword().startsWith("$2a$")) {
            // BCrypt加密的密码
            passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            // 明文密码（兼容旧数据）
            passwordMatch = user.getPassword().equals(request.getPassword());
        }

        if (!passwordMatch) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 创建用户信息并存入session
        UserInfo userInfo = new UserInfo(user.getUserid(), user.getUsername(),
            user.getRole() != null ? user.getRole() : "user", user.getDepartid());
        session.setAttribute("user", userInfo);

        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/current")
    public Result<UserInfo> getCurrentUser(HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute("user");
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(user);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpSession session) {
        UserInfo userInfo = (UserInfo) session.getAttribute("user");
        if (userInfo == null) {
            throw new BusinessException(401, "未登录");
        }

        // 验证两次密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的新密码不一致");
        }

        // 获取用户信息
        User user = userMapper.selectByPrimaryKey(userInfo.getUserid());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 验证原密码
        boolean passwordMatch = false;
        if (user.getPassword().startsWith("$2a$")) {
            passwordMatch = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        } else {
            passwordMatch = user.getPassword().equals(request.getOldPassword());
        }

        if (!passwordMatch) {
            throw new BusinessException(400, "原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateByPrimaryKey(user);

        return Result.success();
    }
}
