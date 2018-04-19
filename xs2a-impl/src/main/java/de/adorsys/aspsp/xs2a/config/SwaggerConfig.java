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

package de.adorsys.aspsp.xs2a.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
               .apiInfo(getApiInfo())
               .select()
               .apis(RequestHandlerSelectors.basePackage("de.adorsys.aspsp.xs2a.web"))
               .paths(Predicates.not(PathSelectors.regex("/error.*?")))
               .paths(Predicates.not(PathSelectors.regex("/connect.*")))
               .paths(Predicates.not(PathSelectors.regex("/management.*")))
               .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(
        "XS2A REST Api",
        "",
        "1.0",
        "urn:tos",
        new Contact("aro, adorsys GmbH & Co. KG", null, "aro@adorsys.de"),
        "License of API",
        "API license URL"
        );
    }
}
