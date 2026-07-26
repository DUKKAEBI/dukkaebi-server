package com.ducami.dukkaebi.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class SpringConfigTest {
    @Test
    void doesNotExposeLocalResourcesWhenUsingS3Storage() {
        SpringConfig config = new SpringConfig();
        ReflectionTestUtils.setField(config, "storageProvider", "s3");
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);

        config.addResourceHandlers(registry);

        verifyNoInteractions(registry);
    }
}
