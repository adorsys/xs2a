/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.AllArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
public class SwaggerConfig {

    private final BuildProperties buildProperties;

    @Bean
    public GroupedOpenApi psd2ApiV1() {
        return GroupedOpenApi.builder()
                   .group("Berlin Group PSD2 API v1")
                   .packagesToScan("de.adorsys.psd2.xs2a.web.controller.psd2")
                   .pathsToExclude("/error.*?")
                   .pathsToExclude("/connect.*")
                   .pathsToExclude("/management.*")
                   .build();
    }

    @Bean
    public GroupedOpenApi confirmationOfFundsApi() {
        return GroupedOpenApi.builder()
                   .group("Confirmation Of Funds API")
                   .packagesToScan("de.adorsys.psd2.xs2a.web.controller.cof")
                   .pathsToExclude("/error.*?")
                   .pathsToExclude("/connect.*")
                   .pathsToExclude("/management.*")
                   .build();
    }

    @Bean
    public GroupedOpenApi trustedBeneficiariesApi() {
        return GroupedOpenApi.builder()
                   .group("Trusted Beneficiaries API")
                   .packagesToScan("de.adorsys.psd2.xs2a.web.controller.tb")
                   .pathsToExclude("/error.*?")
                   .pathsToExclude("/connect.*")
                   .pathsToExclude("/management.*")
                   .build();
    }

    @Primary
    @Bean(name = "api")
    public OpenAPI psd2OpenAPI() {
        return new OpenAPI()
                   .info(new Info()
                             .title("XS2A PSD2 API")
                             .description("OpenApi for XS2A application")
                             .contact(new Contact()
                                          .name("pru")
                                          .email("pru@adorsys.com.ua")
                                          .url("https://www.adorsys.de"))
                             .version(buildProperties.getVersion() + " " + buildProperties.get("build.number"))
                             .license(new License().name("AGPL version 3.0")));
    }
}
