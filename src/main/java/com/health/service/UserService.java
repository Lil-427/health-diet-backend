package com.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.health.dto.UserProfileUpdateDTO;
import com.health.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 更新用户个人信息（身高、体重、目标）
     *
     * @param userId 用户 ID
     * @param dto    更新信息
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 修改密码
     *
     * @param userId      用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 上传头像
     *
     * @param userId 用户 ID
     * @param file   头像文件
     * @return 头像访问地址
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * Base64 方式上传头像
     */
    String uploadAvatarBase64(Long userId, String base64, String filename);
}
