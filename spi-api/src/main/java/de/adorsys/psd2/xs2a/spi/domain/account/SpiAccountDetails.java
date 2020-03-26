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

import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Currency;
import java.util.List;

@Data
@AllArgsConstructor
public class SpiAccountDetails {
    /**
     * Bank specific account identifier. Should not be provided for TPP (for these purposes resourceId should be used)
     */
    private String aspspAccountId;

    private String resourceId;
    /**
     * International Bank Account Number
     * 2 letters CountryCode + 2 digits checksum + BBAN
     * DE89 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String iban;
    /**
     * Basic Bank Account Number
     * 8 symbols bank id + account number
     * 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String bban;
    /**
     * Primary Account Number
     * 0000 0000 0000 0000 (Example)
     */
    private String pan;

    /**
     * Same as previous, several signs are masked with "*"
     */
    private String maskedPan;

    /**
     * Mobile Subscriber Integrated Services Digital Number
     * 00499113606980 (Adorsys tel nr)
     */
    private String msisdn;
    private Currency currency;
    private String name;
    private String displayName;
    private String product;
    private SpiAccountType cashSpiAccountType;
    private SpiAccountStatus spiAccountStatus;

    /**
     * SWIFT
     * 4 letters bankCode + 2 letters CountryCode + 2 symbols CityCode + 3 symbols BranchCode
     * DEUTDE8EXXX (Deuche Bank AG example)
     */
    private String bic;
    private String linkedAccounts;
    private SpiUsageType usageType;
    private String details;

    private List<SpiAccountBalance> balances;

    private String ownerName;
    private SpiAddress ownerAddress;

    /**
     * @param aspspAccountId Bank specific account identifier
     * @param resourceId id of resource
     * @param iban International Bank Account Number
     * @param bban Basic Bank Account Number
     * @param pan Primary Account Number
     * @param maskedPan Primary Account Number in masked form
     * @param msisdn Mobile Subscriber Integrated Services Digital Number
     * @param currency Account currency
     * @param name Name of the account given by the bank or the PSU in Online-Banking
     * @param product Product Name of the Bank for this account, proprietary definition
     * @param cashSpiAccountType ExternalCashAccountType1Code from ISO 20022
     * @param spiAccountStatus status of spi account
     * @param bic The BIC associated to the account
     * @param linkedAccounts cash accounts associated to pending card transactions
     * @param usageType the usage type of the account: PRIV or ORGA
     * @param details Specifications: characteristics of the account, characteristics of the relevant card
     * @param balances spi account balances
     * @param ownerName Name of the legal account owner
     * @param ownerAddress Address of the account owner
     *
     * @deprecated since 6.0/7.0, use all args constructor instead
     */
    @Deprecated // ToDo remove deprecated constructor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1240
    public SpiAccountDetails(String aspspAccountId, String resourceId, String iban, String bban, String pan, String maskedPan,
                             String msisdn, Currency currency, String name, String product, SpiAccountType cashSpiAccountType,
                             SpiAccountStatus spiAccountStatus, String bic, String linkedAccounts, SpiUsageType usageType,
                             String details, List<SpiAccountBalance> balances, String ownerName, SpiAddress ownerAddress) {
        this(aspspAccountId, resourceId, iban, bban, pan, maskedPan, msisdn, currency, name, null, product,
             cashSpiAccountType, spiAccountStatus, bic, linkedAccounts, usageType, details, balances, ownerName, ownerAddress);
    }

    public void emptyBalances() {
        balances = null;
    }
}
