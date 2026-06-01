package com.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.health.common.BusinessException;
import com.health.dto.UserProfileUpdateDTO;
import com.health.entity.User;
import com.health.mapper.UserMapper;
import com.health.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Value("${file.avatar-path:./uploads/avatars}")
    private String avatarPath;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        if (dto.getHeight() != null) {
            user.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            user.setWeight(dto.getWeight());
        }
        if (dto.getGoal() != null) {
            user.setGoal(dto.getGoal());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        updateById(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        // BCrypt 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!suffix.equals(".jpg") && !suffix.equals(".jpeg") && !suffix.equals(".png")) {
            throw new BusinessException("仅支持 jpg / png 格式的图片");
        }

        // 校验文件大小（不超过 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过 2MB");
        }

        try {
            // 确保目录存在
            File dir = new File(avatarPath);
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString() + suffix;
            Path targetPath = Paths.get(avatarPath, newFilename);
            file.transferTo(targetPath.toFile());

            // 删除旧头像文件
            User user = getById(userId);
            if (user != null) {
                deleteOldAvatarFile(user.getAvatar());
                String avatarUrl = "/uploads/avatars/" + newFilename;
                user.setAvatar(avatarUrl);
                updateById(user);
            }
            return "/uploads/avatars/" + newFilename;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadAvatarBase64(Long userId, String base64Data, String filename) {
        // 限制 Base64 数据大小（解码前约 5MB，对应原始图片约 3.5MB）
        if (base64Data.length() > 7 * 1024 * 1024) {
            throw new BusinessException("头像文件过大，请选择小于 5MB 的图片");
        }
        try {
            // 去掉 data:image/xxx;base64, 前缀
            String pureBase64 = base64Data;
            if (base64Data.contains(",")) {
                pureBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(pureBase64);
            if (bytes.length > 5 * 1024 * 1024) {
                throw new BusinessException("头像文件过大，请选择小于 5MB 的图片");
            }

            // 确定后缀
            String suffix = ".png";
            if (filename != null && filename.contains(".")) {
                suffix = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            }
            if (!suffix.equals(".jpg") && !suffix.equals(".jpeg") && !suffix.equals(".png")) {
                suffix = ".png";
            }

            // 确保目录存在
            File dir = new File(avatarPath);
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }

            String newFilename = UUID.randomUUID().toString() + suffix;
            Path targetPath = Paths.get(avatarPath, newFilename);
            Files.write(targetPath, bytes);

            User user = getById(userId);
            if (user != null) {
                deleteOldAvatarFile(user.getAvatar());
                String avatarUrl = "/uploads/avatars/" + newFilename;
                user.setAvatar(avatarUrl);
                updateById(user);
            }
            return "/uploads/avatars/" + newFilename;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).count() > 0;
    }

    @Override
    public void deleteAccount(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        // 删除旧头像文件
        deleteOldAvatarFile(user.getAvatar());
        // MyBatis-Plus 逻辑删除标记
        removeById(userId);
    }

    private void deleteOldAvatarFile(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) return;
        try {
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            Path oldFile = Paths.get(avatarPath, filename);
            Files.deleteIfExists(oldFile);
        } catch (IOException ignored) {
            // 删除失败不影响新文件上传
        }
    }
}
