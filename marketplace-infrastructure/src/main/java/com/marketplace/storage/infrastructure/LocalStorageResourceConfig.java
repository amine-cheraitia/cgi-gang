package com.marketplace.storage.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageResourceConfig implements WebMvcConfigurer {
    private final StorageProperties storageProperties;

    public LocalStorageResourceConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPathMatcher(new AntPathMatcher());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rootPath = Path.of(storageProperties.getLocal().getRootPath()).toAbsolutePath().normalize();
        registry.addResourceHandler("/files/**")
            .addResourceLocations("file:" + rootPath + "/");
    }
}
