package de.adorsys.aspsp.xs2a.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;

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

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfo("XS2A REST Api", "", "1.0", "urn:tos",
                        new Contact("aro, adorsys GmbH & Co. KG", null, "aro@adorsys.de"), null, null))
                .select()
                .apis(RequestHandlerSelectors.basePackage("de.adorsys.aspsp.xs2a.web"))
                .paths(Predicates.not(PathSelectors.regex("/error.*?")))
                .paths(Predicates.not(PathSelectors.regex("/connect.*")))
                .paths(Predicates.not(PathSelectors.regex("/management.*")))
                .build();
			

    }
    

}
