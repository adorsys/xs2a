/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.aspspmockserver.config;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
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

import static java.util.Collections.singletonList;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfig extends WebMvcConfigurerAdapter {
    @Value("${license.url}")
    private String licenseUrl;

    private final KeycloakConfigProperties keycloakConfig;

    @Bean(name = "api")
    public Docket apiDocklet() {
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
                   .licenseUrl(licenseUrl)
                   .build();
    }

    private OAuth securitySchema() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
                                  .tokenEndpoint(new TokenEndpoint(keycloakConfig.getRootPath() + "/protocol/openid-connect/token", "oauthtoken"))
                                  .tokenRequestEndpoint(new TokenRequestEndpoint(keycloakConfig.getRootPath() + "/protocol/openid-connect/auth", keycloakConfig.getResource(), keycloakConfig.getCredentials().getSecret()))
                                  .build();
        return new OAuthBuilder()
                   .name("oauth2")
                   .grantTypes(singletonList(grantType))
                   .scopes(scopes())
                   .build();
    }

    private List<AuthorizationScope> scopes() {
        return singletonList(new AuthorizationScope("read", "Access read API"));
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                   .clientId(keycloakConfig.getResource())
                   .clientSecret(keycloakConfig.getCredentials().getSecret())
                   .realm(keycloakConfig.getRealm())
                   .appName(keycloakConfig.getResource())
                   .scopeSeparator(",")
                   .useBasicAuthenticationWithAccessCodeGrant(false)
                   .build();
    }
}
