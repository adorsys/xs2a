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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TransactionStatus {

    ACCP("AcceptedCustomerProfile"),  //Preceding check of technical validation was successful. Customer profile check was also successful
    ACSC("AcceptedSettlementCompleted"),  //Settlement on th’ debtor's account has been completed. Usage : this can be used by the first agent to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement"),
    ACSP("AcceptedSettlementInProcess"),  //All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution
    ACTC("AcceptedTechnicalValidation"),  //AuthenticationObject and syntactical and semantical validation are successful"),
    ACWC("AcceptedWithChange"),  //Instruction is accepted but a change will be made, such as date or remittance not sent
    ACWP("AcceptedWithoutPosting"),  //Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account
    RCVD("Received"),  //Payment initiation has been received by the receiving agent
    PDNG("Pending"),  //Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed
    RJCT("Rejected");  //Payment initiation or individual transaction included in the payment initiation has been rejected

    private static final Map<String, TransactionStatus> container = new HashMap<>();

    static {
        for (TransactionStatus status : values()) {
            container.put(status.getName(), status);
        }
    }

    private String name;

    @JsonCreator
    TransactionStatus(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static Optional<TransactionStatus> getByName(String name) {
        return Optional.ofNullable(container.get(name));
    }
}
