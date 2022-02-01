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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Currency;

@Data
@ApiModel(description = "Amount information", value = "Amount")
@NoArgsConstructor
@AllArgsConstructor
public class Xs2aAmount {

	@ApiModelProperty(value = "ISO 4217 currency code", required = true, example = "EUR")
    @NotNull
    private Currency currency;

	@ApiModelProperty(value = "The amount given with fractional digits, where fractions must be compliant to the currency definition. The decimal separator is a dot", required = true, example = "1000.00")
    @NotNull
    private String amount;
}
