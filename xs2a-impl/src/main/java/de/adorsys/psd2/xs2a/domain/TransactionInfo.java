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

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class TransactionInfo {
    @Size(max = 70)
    private final String creditorName;
    private final AccountReference creditorAccount;
    private final String creditorAgent;
    @Size(max = 70)
    private final String ultimateCreditor;
    private final String debtorName;
    private final AccountReference debtorAccount;
    private final String debtorAgent;
    @Size(max = 70)
    private final String ultimateDebtor;
    @Size(max = 140)
    private final String remittanceInformationUnstructured;
    private final List<String> remittanceInformationUnstructuredArray;
    @Size(max = 140)
    private final String remittanceInformationStructured;
    private final List<String> remittanceInformationStructuredArray;
    private final PurposeCode purposeCode;
}
