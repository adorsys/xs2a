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

package de.adorsys.psd2.xs2a.web.config;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@EnableSwagger2
public class SwaggerConfig {
    private static final String DEFAULT_PSD2_API_LOCATION = "/psd2-api-1.2-Update-2018-08-17.yaml";

    @Value("${xs2a.swagger.psd2.api.location}")
    private String customPsd2ApiLocation;

    @SuppressWarnings("Guava") // Intellij IDEA claims that Guava predicates could be replaced with Java API,
    // but actually it is not possible
    @Bean(name = "api")
    public Docket apiDocklet() {
        return new Docket(DocumentationType.SWAGGER_2)
                   .apiInfo(new ApiInfoBuilder().build())
                   .select()
                   .apis(RequestHandlerSelectors.basePackage("de.adorsys.psd2.xs2a.web"))
                   .paths(Predicates.not(PathSelectors.regex("/error.*?")))
                   .paths(Predicates.not(PathSelectors.regex("/connect.*")))
                   .paths(Predicates.not(PathSelectors.regex("/management.*")))
                   .build();
    }

    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            SwaggerResource swaggerResource = new SwaggerResource();
            swaggerResource.setLocation(resolveYamlLocation());
            return Collections.singletonList(swaggerResource);
        };
    }

    private String resolveYamlLocation() {
        if (StringUtils.isBlank(customPsd2ApiLocation)) {
            return DEFAULT_PSD2_API_LOCATION;
        }
        return customPsd2ApiLocation;
    }
}
