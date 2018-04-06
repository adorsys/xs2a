package de.adorsys.aspsp.aspspmockserver.config;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.google.common.base.Predicates;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(getApiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage("de.adorsys.aspsp.aspspmockserver.web"))
        .paths(Predicates.not(PathSelectors.regex("/error.*?")))
        .paths(Predicates.not(PathSelectors.regex("/connect.*")))
        .paths(Predicates.not(PathSelectors.regex("/management.*")))
        .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(
        "XS2A SPI MOCK API",
        "Mock server to simulate ASPSP",
        "1.0",
        "urn:tos",
        new Contact("dgo, adorsys GmbH & Co. KG", "http://www.adorsys.de", "dgo@adorsys.de"),
        "Apache License 2.0",
        "API license URL"
        );
    }
}
