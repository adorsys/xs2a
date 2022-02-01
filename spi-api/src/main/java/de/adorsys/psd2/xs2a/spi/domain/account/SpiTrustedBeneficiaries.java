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
