/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Currency;
import java.util.List;

@Data
public class Xs2aCardAccountDetails {
    @JsonIgnore
    private final String aspspAccountId;

    @Size(max = 35)
    @NotNull
    private final String resourceId;

    @Size(max = 35)
    private final String maskedPan;

    @NotNull
    private final Currency currency;

    private final String name;

    private final String displayName;

    @Size(max = 35)
    private final String product;

    private final CashAccountType cashAccountType;

    private final AccountStatus accountStatus;

    private final Xs2aUsageType usageType;

    private final String details;

    private final List<Xs2aBalance> balances;

    private final Xs2aAmount creditLimit;

    private final String ownerName;

    private final Boolean debitAccounting;

    @JsonProperty("_links")
    private Links links = new Links();
}
