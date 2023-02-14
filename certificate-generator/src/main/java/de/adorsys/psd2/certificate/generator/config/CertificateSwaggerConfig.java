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

package de.adorsys.psd2.certificate.generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateSwaggerConfig {

    @Bean
    public GroupedOpenApi certificateGeneratorRestApi() {
        return GroupedOpenApi.builder()
                   .group("Certificate Generator REST API")
                   .packagesToScan("de.adorsys.psd2.certificate.generator.controller")
                   .build();
    }

    @Bean
    public OpenAPI certificateGeneratorOpenAPI() {
        return new OpenAPI()
                   .info(new Info()
                             .title("Certificate Generator")
                             .description("Certificate Generator for Testing Purposes of XS2A Environment")
                             .contact(new Contact()
                                          .name("pru")
                                          .email("pru@adorsys.com.ua")
                                          .url("https://www.adorsys.de"))
                             .license(new License().name("AGPL version 3.0")));
    }
}
