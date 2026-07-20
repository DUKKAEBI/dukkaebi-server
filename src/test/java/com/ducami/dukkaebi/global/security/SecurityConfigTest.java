package com.ducami.dukkaebi.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    private final CorsConfigurationSource source = new SecurityConfig(null).corsConfigurationSource();

    @Test
    void allowsTailscaleWebOrigins() {
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/auth/sign-in"));

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("http://100.105.241.21:18080")).isEqualTo("http://100.105.241.21:18080");
        assertThat(config.checkOrigin("http://100.105.241.21:18081")).isEqualTo("http://100.105.241.21:18081");
    }

    @Test
    void allowsDukkaebiVercelOriginsWithoutTrailingSlash() {
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/auth/sign-in"));

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://dukkaebi.vercel.app")).isEqualTo("https://dukkaebi.vercel.app");
        assertThat(config.checkOrigin("https://dukkaebi-admin.vercel.app")).isEqualTo("https://dukkaebi-admin.vercel.app");
    }

    @Test
    void allowsWarsDomainFrontendOrigin() {
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/auth/sign-in"));

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://dukkaebi.wars.p-e.kr")).isEqualTo("https://dukkaebi.wars.p-e.kr");
    }

    @Test
    void allowsWarsDomainAdminOrigin() {
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/auth/sign-in"));

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://admin.dukkaebi.wars.p-e.kr")).isEqualTo("https://admin.dukkaebi.wars.p-e.kr");
    }
}
