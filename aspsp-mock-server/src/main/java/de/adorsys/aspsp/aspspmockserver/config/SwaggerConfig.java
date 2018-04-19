package de.adorsys.aspsp.aspspmockserver.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurerAdapter {
    @Value("${auth_server_url}")
    String authUrl;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(getApiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage("de.adorsys.aspsp.aspspmockserver.web"))
        .paths(Predicates.not(PathSelectors.regex("/error.*?")))
        .paths(Predicates.not(PathSelectors.regex("/connect.*")))
        .paths(Predicates.not(PathSelectors.regex("/management.*")))
        .build()
               .securitySchemes(singletonList(securitySchema()));
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
        .title("XS2A SPI MOCK API")
        .description("Mock server to simulate ASPSP")
        .contact(new Contact("dgo, adorsys GmbH & Co. KG", "http://www.adorsys.de", "dgo@adorsys.de"))
        .version("1.0")
        .license("Apache License 2.0")
        .licenseUrl("API license URL")
        .build();
    }

    private OAuth securitySchema() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
                              .tokenEndpoint(new TokenEndpoint(authUrl + "/protocol/openid-connect/token", "oauthtoken"))
                              .tokenRequestEndpoint(new TokenRequestEndpoint(authUrl + "/protocol/openid-connect/auth", "aspsp-mock", "1166b089-1868-442f-aa66-ad38100715b4"))
                              .build();
        return new OAuthBuilder()
               .name("oauth2")
               .grantTypes(asList(grantType))
               .scopes(scopes())
               .build();
    }

    private List<AuthorizationScope> scopes() {
        return asList(new AuthorizationScope("read", "Access read API"));
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
               .clientId("aspsp-mock")
               .clientSecret("1166b089-1868-442f-aa66-ad38100715b4")
               .realm("xs2a")
               .appName("aspsp-mock")
               .scopeSeparator(",")
               .useBasicAuthenticationWithAccessCodeGrant(false)
        .build();
    }
}
