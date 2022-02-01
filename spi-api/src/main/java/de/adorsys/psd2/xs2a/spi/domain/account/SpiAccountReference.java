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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;

@Data
@NotNull
@Builder
@AllArgsConstructor
public class SpiAccountReference {
    private String aspspAccountId;
    private String resourceId;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;
    private String otherAccountIdentification;

    public SpiAccountReference(@NotNull SpiAccountDetails accountDetails) {
        this.aspspAccountId = accountDetails.getAspspAccountId();
        this.resourceId = accountDetails.getResourceId();
        this.iban = accountDetails.getIban();
        this.bban = accountDetails.getBban();
        this.pan = accountDetails.getPan();
        this.maskedPan = accountDetails.getMaskedPan();
        this.msisdn = accountDetails.getMsisdn();
        this.currency = accountDetails.getCurrency();
    }
}
