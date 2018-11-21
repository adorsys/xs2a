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

package de.adorsys.psd2.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApiModel(description = "CashAccountType", value = "Cash Account Type")
public enum CashAccountType {
    CACC("Current"),  // Account used to post debits and credits when no specific account has been nominated
    CASH("CashPayment"),  // Account used for the payment of cash
    CHAR("Charges"),  // Account used for charges if different from the account for payment
    CISH("CashIncome"),  // Account used for payment of income if different from the current cash account
    COMM("Commission"),  // Account used for commission if different from the account for payment
    CPAC("ClearingParticipantSettlementAccount"),  // Account used to post settlement debit and credit entries on behalf of a designated Clearing Participant
    LLSV("LimitedLiquiditySavingsAccount"),  // Account used for savings with special interest and withdrawal terms
    LOAN("Loan"),  // Account used for loans
    MGLD("Marginal Lending"),  // Account used for a marginal lending facility
    MOMA("Money Market"),  // Account used for money markets if different from the cash account
    NREX("NonResidentExternal"),  // Account used for non-resident external
    ODFT("Overdraft"),  // Account is used for overdrafts
    ONDP("OverNightDeposit"),  // Account used for overnight deposits
    OTHR("OtherAccount"),  // Account not otherwise specified
    SACC("Settlement"),  // Account used to post debit and credit entries, as a result of transactions cleared and settled through a specific clearing and settlement system
    SLRY("Salary"),  // Accounts used for salary payments
    SVGS("Savings"),  // Account used for savings
    TAXE("Tax"),  // Account used for taxes if different from the account for payment
    TRAN("TransactingAccount"),  // A transacting account is the most basic type of bank account that you can get. The main difference between transaction and cheque accounts is that you usually do not get a cheque book with your transacting account and neither are you offered an overdraft facility
    TRAS("Cash Trading");  // Account used for trading if different from the current cash account

    private final static Map<String, CashAccountType> container = new HashMap<>();

    static {
        for (CashAccountType cashAccountType : values()) {
            container.put(cashAccountType.getValue(), cashAccountType);
        }
    }

    private String value;

    @JsonCreator
    CashAccountType(String value) {
        this.value = value;
    }

    @JsonIgnore
    public static Optional<CashAccountType> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }

    public String getValue() {
        return value;
    }
}
