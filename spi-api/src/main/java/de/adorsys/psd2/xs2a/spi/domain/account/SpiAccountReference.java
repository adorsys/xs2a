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

package de.adorsys.psd2.xs2a.spi.domain.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;

@Data
@NotNull
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

    public SpiAccountReference(String resourceId, String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency) {
        this.resourceId = resourceId;
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
    }
}
