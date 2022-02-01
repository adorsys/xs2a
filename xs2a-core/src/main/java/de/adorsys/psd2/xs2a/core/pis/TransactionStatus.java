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

package de.adorsys.psd2.xs2a.core.pis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TransactionStatus {

    ACCC("AcceptedSettlementCompletedCreditor", true), // Settlement on the creditor's account has been completed.
    ACCP("AcceptedCustomerProfile", false),  //Preceding check of technical validation was successful. Customer profile check was also successful
    ACSC("AcceptedSettlementCompleted", true),  //Settlement on the debtor's account has been completed. Usage : this can be used by the first agent to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement"),
    ACSP("AcceptedSettlementInProcess", false),  //All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution
    ACTC("AcceptedTechnicalValidation", false),  //AuthenticationObject and syntactical and semantical validation are successful"),
    ACWC("AcceptedWithChange", false),  //Instruction is accepted but a change will be made, such as date or remittance not sent
    ACWP("AcceptedWithoutPosting", false),  //Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account
    RCVD("Received", false),  //Payment initiation has been received by the receiving agent
    PDNG("Pending", false),  //Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed
    RJCT("Rejected", true),  //Payment initiation or individual transaction included in the payment initiation has been rejected
    CANC("Canceled", true), //Canceled
    ACFC("AcceptedFundsChecked", false), //Preceding check of technical validation and customer profile was successful and an automatic funds check was positive
    PATC("PartiallyAcceptedTechnicalCorrect", false), // The payment initiation needs multiple authentications, where some but not yet all have been performed. Syntactical and semantical validations are successful.
    PART("PartiallyAccepted", false); // A number of transactions have been accepted, whereas another number of transactions have not yet achieved 'accepted' status.

    private static Map<String, TransactionStatus> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(status -> container.put(status.getTransactionStatus(), status));
    }

    private String transactionStatusString;
    private final boolean finalisedStatus;

    public boolean isFinalisedStatus() {
        return finalisedStatus;
    }

    public boolean isNotFinalisedStatus() {
        return !isFinalisedStatus();
    }

    TransactionStatus(String transactionStatus, boolean finalisedStatus) {
        this.transactionStatusString = transactionStatus;
        this.finalisedStatus = finalisedStatus;
    }

    public static TransactionStatus getByValue(String transactionStatus) {
        return container.get(transactionStatus);
    }

    public String getTransactionStatus() {
        return transactionStatusString;
    }
}
