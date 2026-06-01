package com.health.controller;

import com.health.common.Result;
import com.health.dto.UpdatePasswordRequest;
import com.health.dto.UserProfileUpdateDTO;
import com.health.entity.User;
import com.health.service.UserService;
import com.health.vo.UserProfileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户信息控制器
 * 提供当前用户信息的查询、更新、修改密码和上传头像功能
 */
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户信息管理")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户个人信息（隐藏密码）
     */
    @GetMapping("/profile")
    @ApiOperation("获取当前用户个人信息")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setAge(user.getAge());
        vo.setUsername(user.getUsername());
        vo.setHeight(user.getHeight());
        vo.setWeight(user.getWeight());
        vo.setGoal(user.getGoal());
        vo.setGender(user.getGender());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());

        return Result.success(vo);
    }

    /**
     * 更新当前用户个人信息（身高、体重、目标）
     */
    @PutMapping("/profile")
    @ApiOperation("更新个人信息")
    public Result<Void> updateProfile(HttpServletRequest request,
                                      @Validated @RequestBody UserProfileUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        userService.updateProfile(userId, dto);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> updatePassword(HttpServletRequest request,
                                       @Validated @RequestBody UpdatePasswordRequest dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }

        // 校验两次密码一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @ApiOperation("上传头像")
    public Result<String> uploadAvatar(HttpServletRequest request,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }
        if (file == null || file.isEmpty()) {
            return Result.error("未接收到文件");
        }
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success("上传成功", avatarUrl);
    }

    /**
     * Base64 方式上传头像
     */
    @PostMapping("/avatar/base64")
    @ApiOperation("Base64 上传头像")
    public Result<String> uploadAvatarBase64(HttpServletRequest request,
                                              @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }
        String base64 = body.get("file");
        String filename = body.getOrDefault("filename", "avatar.png");
        if (base64 == null || base64.isEmpty()) {
            return Result.error("未接收到文件");
        }
        String avatarUrl = userService.uploadAvatarBase64(userId, base64, filename);
        return Result.success("上传成功", avatarUrl);
    }

    /**
     * 注销账号
     */
    @DeleteMapping("/account")
    @ApiOperation("注销账号")
    public Result<Void> deleteAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未认证，请先登录");
        }
        userService.deleteAccount(userId);
        return Result.success("账号已注销", null);
    }
}
