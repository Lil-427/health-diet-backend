package com.health.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传配置
 * 使上传的文件可通过 HTTP 访问
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 映射到本地文件系统的 ./uploads/ 目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
