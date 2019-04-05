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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.psd2.xs2a.service.validator.parameter.RequestParameter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Data
@ApiModel(description = "Account request parameter", value = "AccountRequestParameter")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestParameter implements RequestParameter {

    @ApiModelProperty(value = "Starting date of the account statement", example = "2017-10-30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateFrom;

    @ApiModelProperty(value = "End date of the account statement", example = "2017-11-30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateTo;
}
