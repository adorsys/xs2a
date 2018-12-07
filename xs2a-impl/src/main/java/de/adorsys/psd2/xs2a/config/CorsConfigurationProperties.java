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
