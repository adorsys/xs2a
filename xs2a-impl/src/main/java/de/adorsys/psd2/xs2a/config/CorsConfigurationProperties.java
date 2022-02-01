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

package de.adorsys.psd2.xs2a.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Service
@Qualifier("xs2aCorsConfigProperties")
public class CorsConfigurationProperties {

    @Value("${xs2a.endpoints.cors.max-age}")
    private long maxAge;

    @Value("${xs2a.endpoints.cors.allow-credentials}")
    private Boolean allowCredentials;

    @Value("#{'${xs2a.endpoints.cors.allowed-methods}'.split(',')}")
    private List<String> allowedMethods;

    @Value("#{'${xs2a.endpoints.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${xs2a.endpoints.cors.allowed-headers}'.split(',')}")
    private List<String> allowedHeaders;
}
