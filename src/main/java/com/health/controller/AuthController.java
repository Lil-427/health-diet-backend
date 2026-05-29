package com.health.controller;

import com.health.common.Result;
import com.health.dto.LoginRequest;
import com.health.dto.RegisterRequest;
import com.health.entity.User;
import com.health.service.UserService;
import com.health.utils.JwtUtils;
import com.health.vo.LoginResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 认证控制器
 * 提供注册和登录接口
 */
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 用户注册
     *
     * @param request 注册请求（用户名、密码）
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Void> register(@Validated @RequestBody RegisterRequest request) {
        // 用户名去首尾空格
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (username.isEmpty()) {
            return Result.error("用户名不能为空");
        }

        // 检查用户名是否已存在
        User existingUser = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        if (userService.save(user)) {
            return Result.success("注册成功", null);
        }
        return Result.error("注册失败");
    }

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名、密码）
     * @return Token 和用户信息
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        // 根据用户名查询用户
        User user = userService.lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .one();

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        // 生成 Token（携带 userId）
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        LoginResponse loginResponse = new LoginResponse(token, user.getId(), user.getUsername());
        return Result.success(loginResponse);
    }
}
