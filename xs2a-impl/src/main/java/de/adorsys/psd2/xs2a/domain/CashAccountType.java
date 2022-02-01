/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
    CARD("CardAccount"), // Account used for credit card payments
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

    private static final Map<String, CashAccountType> container = new HashMap<>();

    static {
        for (CashAccountType cashAccountType : values()) {
            container.put(cashAccountType.getValue(), cashAccountType);
        }
    }

    private final String value;

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
