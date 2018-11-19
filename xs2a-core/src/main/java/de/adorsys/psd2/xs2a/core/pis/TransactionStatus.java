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

package de.adorsys.psd2.xs2a.core.pis;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TransactionStatus {

    ACCP("AcceptedCustomerProfile", false),  //Preceding check of technical validation was successful. Customer profile check was also successful
    ACSC("AcceptedSettlementCompleted", true),  //Settlement on the debtor's account has been completed. Usage : this can be used by the first agent to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement"),
    ACSP("AcceptedSettlementInProcess", false),  //All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution
    ACTC("AcceptedTechnicalValidation", false),  //AuthenticationObject and syntactical and semantical validation are successful"),
    ACWC("AcceptedWithChange", false),  //Instruction is accepted but a change will be made, such as date or remittance not sent
    ACWP("AcceptedWithoutPosting", false),  //Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account
    RCVD("Received", false),  //Payment initiation has been received by the receiving agent
    PDNG("Pending", false),  //Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed
    RJCT("Rejected", true),  //Payment initiation or individual transaction included in the payment initiation has been rejected
    CANC("Canceled", true); //Canceled

    private static Map<String, TransactionStatus> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(status -> container.put(status.getTransactionStatus(), status));
    }

    private String transactionStatus;
    private final boolean finalisedStatus;

    public boolean isFinalisedStatus() {
        return finalisedStatus;
    }

    TransactionStatus(String transactionStatus, boolean finalisedStatus) {
        this.transactionStatus = transactionStatus;
        this.finalisedStatus = finalisedStatus;
    }

    @JsonCreator
    public static TransactionStatus getByValue(String transactionStatus) {
        return container.get(transactionStatus);
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
