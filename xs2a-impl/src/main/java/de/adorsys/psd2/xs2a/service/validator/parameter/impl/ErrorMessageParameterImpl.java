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

package de.adorsys.psd2.xs2a.service.validator.parameter.impl;

import de.adorsys.psd2.xs2a.service.validator.parameter.RequestParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Data
@ApiModel(description = "Is used when parameter are not correct", value = "ErrorMessageParameterImpl")
public class ErrorMessageParameterImpl implements RequestParameter {
    @ApiModelProperty(value = "Error description", required = false, example = "Error: 'dataFrom' parameter has wrong format")
    private final String errorMessage;
}
