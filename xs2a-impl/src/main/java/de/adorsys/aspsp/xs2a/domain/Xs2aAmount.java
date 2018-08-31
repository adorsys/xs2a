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

package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Currency;

@Data
@ApiModel(description = "Amount information", value = "Amount")
public class Xs2aAmount {

	@ApiModelProperty(value = "ISO 4217 currency code", required = true, example = "EUR")
    @NotNull
    private Currency currency;

	@ApiModelProperty(value = "The amount given with fractional digits, where fractions must be compliant to the currency definition. The decimal separator is a dot", required = true, example = "1000.00")
    @NotNull
    private String content;
}
