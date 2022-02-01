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

package de.adorsys.psd2.xs2a.core.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Remittance", value = "Remittance")
public class Remittance {

    @ApiModelProperty(value = "the actual reference", required = true, example = "Ref Number Merchant")
    @Size(max = 35)
    private String reference;

    @ApiModelProperty(value = "reference type", example = "reference type")
    @Size(max = 35)
    private String referenceType;

    @ApiModelProperty(value = "reference issuer", example = "reference issuer")
    @Size(max = 35)
    private String referenceIssuer;
}
