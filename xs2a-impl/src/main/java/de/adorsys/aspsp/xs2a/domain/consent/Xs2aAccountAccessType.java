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

package de.adorsys.aspsp.xs2a.domain.consent;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApiModel(description = "AccountAccess type", value = "AccountAccessType")
public enum Xs2aAccountAccessType {
    ALL_ACCOUNTS("allAccounts"),
    ALL_ACCOUNTS_WITH_BALANCES("allAccountsWithBalances");

    private static Map<String, Xs2aAccountAccessType> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(aat -> container.put(aat.getDescription(), aat));
    }

    @ApiModelProperty(value = "description", example = "allAccounts")
    private String description;

    @JsonCreator
    Xs2aAccountAccessType(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public static Optional<Xs2aAccountAccessType> getByDescription(String description) {
        return Optional.ofNullable(container.get(description));
    }
}
