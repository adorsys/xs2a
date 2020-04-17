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
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class SpiTrustedBeneficiaries {
    /**
     * Resource identification of the list entry
     */
    @NotNull
    private String trustedBeneficiaryId;
    /**
     * This is provided by the ASPSP if the trusted
     * beneficiary entry is applicable to a dedicated
     * account only
     */
    private SpiAccountReference debtorAccount;
    /**
     * The creditor account as used in the trusted
     * beneficiary list of the PSU
     */
    @NotNull
    private SpiAccountReference creditorAccount;
    /**
     * It is mandated where the information is
     * mandated for related credit transfers
     */
    private String creditorAgent;
    /**
     * Name of the creditor as provided by the PSU
     */
    @NotNull
    private String creditorName;
    /**
     * An alias for the creditor as defined by the PSU
     * as an alias when displaying the list of trusted
     * beneficiaries in online channels of the ASPSP
     */
    private String creditorAlias;
    /**
     * Identification of Creditors
     */
    private String creditorId;
    /**
     * Address of creditor
     */
    private SpiAddress creditorAddress;
}
