package de.adorsys.aspsp.aspspmockserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {
    private String resource;
    private Credentials credentials;
    private String realm;

    @Data
    public static class Credentials {
        private String secret;
    }
}
