/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;

@Component
public class SwaggerResourceBuilder {
    private static final String DEFAULT_PSD2_API_LOCATION = "/psd2-api_1.3.8_2020-11-06v1.yaml";
    private static final String DEFAULT_PSD2_API_FUNDS_CONFIRMATION_LOCATION = "/psd2-confirmation-of-funds-consent-2.0-20190607.yaml";

    @Value("${xs2a.swagger.psd2.api.location:}")
    private String customPsd2ApiLocation;

    public SwaggerResource buildPSD2ApiV1() {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setLocation(resolveYamlLocation());
        swaggerResource.setName("Berlin Group PSD2 API v1");
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

    public SwaggerResource buildFundsConfirmationApiV2() {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setLocation(DEFAULT_PSD2_API_FUNDS_CONFIRMATION_LOCATION);
        swaggerResource.setName("Berlin Group PSD2 Funds confirmation API v2");
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

    private String resolveYamlLocation() {
        if (StringUtils.isBlank(customPsd2ApiLocation)) {
            return DEFAULT_PSD2_API_LOCATION;
        }
        return customPsd2ApiLocation;
    }
}
