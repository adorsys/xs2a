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

package de.adorsys.psd2.aspsp.mock.api.payment;

import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspPsuData;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AspspSinglePayment {

    @Id
    private String paymentId;
    private String endToEndIdentification;
    private AspspAccountReference debtorAccount;
    @Deprecated // Since 1.2
    private String ultimateDebtor;
    private AspspAmount instructedAmount;
    private AspspAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private AspspAddress creditorAddress;
    @Deprecated // Since 1.2
    private String ultimateCreditor;
    @Deprecated // Since 1.2
    private String purposeCode;
    private String remittanceInformationUnstructured;
    @Deprecated // Since 1.2
    private AspspRemittance remittanceInformationStructured;
    @Deprecated // Since 1.2
    private LocalDate requestedExecutionDate;
    @Deprecated // Since 1.2
    private LocalDateTime requestedExecutionTime;
    private AspspTransactionStatus paymentStatus;
    private List<AspspPsuData> psuDataList;
}
