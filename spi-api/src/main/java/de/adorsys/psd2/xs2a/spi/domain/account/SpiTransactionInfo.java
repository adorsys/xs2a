/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import lombok.Data;

import java.util.List;

@Data
public class SpiTransactionInfo {
    private final String creditorName;
    private final SpiAccountReference creditorAccount;
    private final String creditorAgent;
    private final String ultimateCreditor;
    private final String debtorName;
    private final SpiAccountReference debtorAccount;
    private final String debtorAgent;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final List<String> remittanceInformationUnstructuredArray;
    private final SpiRemittance remittanceInformationStructured;
    private final List<SpiRemittance> remittanceInformationStructuredArray;
    private final String purposeCode;
}
