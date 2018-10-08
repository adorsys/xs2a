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

package de.adorsys.aspsp.aspspmockserver.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Remittance", value = "Remittance")
public class Remittance {
    @ApiModelProperty(value = "the actual reference", required = true, example = "Ref Number Merchant")
    private String reference;

    @ApiModelProperty(value = "reference type", example = "reference type")
    private String referenceType;

    @ApiModelProperty(value = "reference issuer", example = "reference issuer")
    private String referenceIssuer;
}
