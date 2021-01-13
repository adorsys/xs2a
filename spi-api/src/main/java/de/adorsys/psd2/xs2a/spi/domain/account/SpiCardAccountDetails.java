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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Currency;
import java.util.List;

@Data
@AllArgsConstructor
public class SpiCardAccountDetails {
    /**
     * Bank specific account identifier. Should not be provided for TPP (for these purposes resourceId should be used)
     */
    private String aspspAccountId;

    private String resourceId;
    private String maskedPan;
    private Currency currency;
    private String name;
    private String displayName;
    private String product;
    private SpiAccountStatus spiAccountStatus;
    private SpiAccountType cashSpiAccountType;
    private SpiUsageType usageType;
    private String details;
    private SpiAmount creditLimit;
    private List<SpiAccountBalance> balances;
    private String ownerName;
    private Boolean debitAccounting;
}
