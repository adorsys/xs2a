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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "Account Report", value = "AccountReport")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@JsonRootName(value = "transactions")
public class AccountReport {

    @ApiModelProperty(value = "Booked TransactionsCreditorResponse", required = true)
    @NotNull
    private final Transactions[] booked;

    @ApiModelProperty(value = "Pending TransactionsCreditorResponse")
    private final Transactions[] pending;

    @ApiModelProperty(value = "Links: the following links might be used within this context:" +
                              "account link (mandatory)" +
                              "first_page_link (optional)" +
                              "second_page_link (optional)" +
                              "current_page_ link (optional)" +
                              "last_page_link (optional)", required = true)
    @NotNull
    @JsonProperty("_links")
    private Links links;
}
