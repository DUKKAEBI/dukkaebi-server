package com.ducami.dukkaebi.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class SpringConfig implements WebMvcConfigurer {
    @Value("${storage.local.root-path}")
    private String localStorageRootPath;

    @Value("${storage.local.public-base-path}")
    private String localStoragePublicBasePath;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new  BCryptPasswordEncoder();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = Paths.get(localStorageRootPath)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler(normalizePath(localStoragePublicBasePath) + "/**")
                .addResourceLocations(resourceLocation);
    }

    private String normalizePath(String path) {
        return "/" + path.replaceAll("^/+|/+$", "");
    }
}
