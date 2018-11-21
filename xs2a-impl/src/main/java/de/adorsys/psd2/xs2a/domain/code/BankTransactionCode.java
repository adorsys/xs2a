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

package de.adorsys.psd2.xs2a.domain.code;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@ApiModel(description = "BankTransactionCode", value = "The Bank transaction code")
@AllArgsConstructor
public class BankTransactionCode {

    // todo documentation doesn't have any definition. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/44
    @ApiModelProperty(value = "BankTransactionCode code", example = "123344")
    private String code;
}
