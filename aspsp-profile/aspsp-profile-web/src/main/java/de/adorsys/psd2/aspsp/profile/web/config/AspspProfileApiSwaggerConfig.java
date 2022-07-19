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

package de.adorsys.psd2.aspsp.profile.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AspspProfileApiSwaggerConfig {
    @Value("${xs2a.license.url}")
    private String licenseUrl;
    private final BuildProperties buildProperties;

    @Bean
    public GroupedOpenApi aspspProfileGroupedOpenAPI() {
        return GroupedOpenApi.builder()
                   .group("ASPSP Profile REST API")
                   .packagesToScan("de.adorsys.psd2.aspsp.profile.web.controller")
                   .pathsToExclude("/error.*?")
                   .pathsToExclude("/connect.*")
                   .pathsToExclude("/management.*")
                   .build();
    }

    @Bean
    public OpenAPI aspspProfileOpenAPI() {
        return new OpenAPI()
                   .info(new Info()
                             .title("ASPSP Profile REST API")
                             .description("OpenApi for ASPSP Profile application")
                             .contact(new Contact()
                                          .name("pru")
                                          .email("pru@adorsys.com.ua")
                                          .url("https://www.adorsys.de"))
                             .version(buildProperties.getVersion() + " " + buildProperties.get("build.number"))
                             .license(new License().name("AGPL version 3.0").url(licenseUrl)));
    }
}
