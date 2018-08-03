package de.adorsys.aspsp.xs2a.integtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfigProperty {
    private String clientId;
    private String clientSecret;
    private String url;
    private String grantType;
    private String username;
    private String password;
}
