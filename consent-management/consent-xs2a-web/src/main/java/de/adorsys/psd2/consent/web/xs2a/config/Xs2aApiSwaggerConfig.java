/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.web.xs2a.config;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@RequiredArgsConstructor
public class Xs2aApiSwaggerConfig {
    @Value("${xs2a.license.url}")
    private String licenseUrl;
    private final BuildProperties buildProperties;

    @SuppressWarnings("Guava")  // Intellij IDEA claims that Guava predicates could be replaced with Java API,
    // but actually it is not possible
    @Bean(name = "xs2a-api")
    public Docket apiDocklet() {
        return new Docket(DocumentationType.SWAGGER_2)
                   .groupName("Internal CMS-XS2A-API")
                   .apiInfo(getApiInfo())
                   .tags(
                       InternalCmsXs2aApiTagHolder.AIS_CONSENTS,
                       InternalCmsXs2aApiTagHolder.AIS_PSU_DATA,
                       InternalCmsXs2aApiTagHolder.ASPSP_CONSENT_DATA,
                       InternalCmsXs2aApiTagHolder.AUTHORISATIONS,
                       InternalCmsXs2aApiTagHolder.CONSENTS,
                       InternalCmsXs2aApiTagHolder.EVENTS,
                       InternalCmsXs2aApiTagHolder.PIIS_CONSENTS,
                       InternalCmsXs2aApiTagHolder.PIS_COMMON_PAYMENT,
                       InternalCmsXs2aApiTagHolder.PIS_PAYMENTS,
                       InternalCmsXs2aApiTagHolder.PIS_PSU_DATA,
                       InternalCmsXs2aApiTagHolder.TPP
                   )
                   .select()
                   .apis(RequestHandlerSelectors.basePackage("de.adorsys.psd2.consent.web.xs2a"))
                   .paths(Predicates.not(PathSelectors.regex("/error.*?")))
                   .paths(Predicates.not(PathSelectors.regex("/connect.*")))
                   .paths(Predicates.not(PathSelectors.regex("/management.*")))
                   .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                   .title("XS2A CMS Internal API")
                   .contact(new Contact("adorsys GmbH & Co. KG", "https://adorsys-platform.de/solutions/", "psd2@adorsys.de"))
                   .version(buildProperties.getVersion() + " " + buildProperties.get("build.number"))
                   .license("GNU Affero General Public License (AGPL) version 3.0")
                   .licenseUrl(licenseUrl)
                   .build();
    }
}
