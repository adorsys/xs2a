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
