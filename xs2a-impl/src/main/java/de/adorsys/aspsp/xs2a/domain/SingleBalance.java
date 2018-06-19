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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "Balance Information", value = "SingleBalance")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleBalance {

    @ApiModelProperty(value = "amount", required = true)
    private Amount amount;

    @ApiModelProperty(value = "last action date time", example = "2017-10-25T15:30:35.035")
    private LocalDateTime lastActionDateTime;

    @ApiModelProperty(value = "Date", example = "2017-03-26")
    private LocalDate date;
}
