package de.adorsys.aspsp.xs2a.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${license.url}")
    private String licenseUrl;

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
               .apiInfo(getApiInfo())
               .select()
               .apis(RequestHandlerSelectors.basePackage("de.adorsys.aspsp.xs2a.web"))
               .paths(Predicates.not(PathSelectors.regex("/error.*?")))
               .paths(Predicates.not(PathSelectors.regex("/connect.*")))
               .paths(Predicates.not(PathSelectors.regex("/management.*")))
               .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
               .title("XS2A REST Api")
               .contact(new Contact("dgo, adorsys GmbH & Co. KG", "http://www.adorsys.de", "dgo@adorsys.de"))
               .version("1.0")
               .license("Apache License 2.0")
               .licenseUrl(licenseUrl)
               .build();
    }
}
