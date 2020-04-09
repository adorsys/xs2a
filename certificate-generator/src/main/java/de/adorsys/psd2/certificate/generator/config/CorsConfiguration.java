package de.adorsys.psd2.certificate.generator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfiguration implements WebMvcConfigurer {
    private final CorsConfigProperties corsConfigProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(getTargetParameters(corsConfigProperties.getAllowedOrigins()))
            .allowedMethods(getTargetParameters(corsConfigProperties.getAllowedMethods()))
            .allowedHeaders(getTargetParameters(corsConfigProperties.getAllowedHeaders()))
            .allowCredentials(corsConfigProperties.getAllowCredentials())
            .maxAge(corsConfigProperties.getMaxAge());
    }

    private String[] getTargetParameters(List<String> targetParameters) {
        return targetParameters.toArray(new String[0]);
    }
}
