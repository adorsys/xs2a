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

    /**
     * @param aspspAccountId Bank specific account identifier
     * @param resourceId id of resource
     * @param maskedPan Primary Account Number in masked form
     * @param currency Account currency
     * @param name Name of the account given by the bank or the PSU in Online-Banking
     * @param product Product Name of the Bank for this account, proprietary definition
     * @param spiAccountStatus status of spi account
     * @param cashSpiAccountType ExternalCashAccountType1Code from ISO 20022
     * @param usageType the usage type of the account: PRIV or ORGA
     * @param details Specifications: characteristics of the account, characteristics of the relevant card
     * @param creditLimit credit limit of the PSU aggregated for all cards related to this card account in total
     * @param balances spi account balances
     * @param ownerName Name of the legal account owner
     *
     * @deprecated since 6.0/7.0, use all args constructor instead
     */
    @Deprecated // ToDo remove deprecated constructor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1240
    public SpiCardAccountDetails(String aspspAccountId, String resourceId, String maskedPan, Currency currency, String name,
                                 String product, SpiAccountStatus spiAccountStatus, SpiAccountType cashSpiAccountType,
                                 SpiUsageType usageType, String details, SpiAmount creditLimit,
                                 List<SpiAccountBalance> balances, String ownerName) {
        this(aspspAccountId, resourceId, maskedPan, currency, name, null, product, spiAccountStatus, cashSpiAccountType,
             usageType, details, creditLimit, balances, ownerName);
    }
}
