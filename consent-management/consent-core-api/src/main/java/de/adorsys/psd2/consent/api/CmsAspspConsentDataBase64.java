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

package de.adorsys.psd2.consent.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@Value
@ApiModel(description = "ASPSP Consent data", value = "CmsAspspConsentDataBase64")
public class CmsAspspConsentDataBase64 {

    @ApiModelProperty(value = "Consent ID", required = true, example = "d2796b05-418e-49bc-84ce-c6728a1b2018")
    private String consentId;
    @ApiModelProperty(value = "ASPSP consent data Base64", required = true, example = "zdxcvvzzzxcvzzzz")
    private String aspspConsentDataBase64;
}
