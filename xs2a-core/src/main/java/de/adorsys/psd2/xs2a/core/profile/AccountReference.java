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

package de.adorsys.psd2.xs2a.core.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Currency;

import static de.adorsys.psd2.xs2a.core.profile.AccountReferenceType.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Account Reference", value = "AccountReference")
public class AccountReference {

    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "89370400440532013000")
    private String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "2356 5746 3217 1234")
    private String pan;

    @ApiModelProperty(value = "MASKEDPAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxx1234")
    private String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "+49(0)911 360698-0")
    private String msisdn;

    @ApiModelProperty(value = "Codes following ISO 4217", example = "EUR")
    private Currency currency;

    public AccountReference(AccountReferenceType accountReferenceType, String accountReferenceValue, Currency currency, String resourceId) {
        if (accountReferenceType == IBAN) {
            this.iban = accountReferenceValue;
        } else if (accountReferenceType == BBAN) {
            this.bban = accountReferenceValue;
        } else if (accountReferenceType == PAN) {
            this.pan = accountReferenceValue;
        } else if (accountReferenceType == MSISDN) {
            this.msisdn = accountReferenceValue;
        } else if (accountReferenceType == MASKED_PAN) {
            this.maskedPan = accountReferenceValue;
        }
        this.currency = currency;
        this.resourceId = resourceId;
    }

    @JsonIgnore
    public AccountReferenceSelector getUsedAccountReferenceSelector() {
        if (StringUtils.isNotBlank(iban)) {
            return new AccountReferenceSelector(IBAN, this.iban);
        }
        if (StringUtils.isNotBlank(bban)) {
            return new AccountReferenceSelector(BBAN, this.bban);
        }
        if (StringUtils.isNotBlank(pan)) {
            return new AccountReferenceSelector(PAN, this.pan);
        }
        if (StringUtils.isNotBlank(msisdn)) {
            return new AccountReferenceSelector(MSISDN, this.msisdn);
        }
        if (StringUtils.isNotBlank(maskedPan)) {
            return new AccountReferenceSelector(MASKED_PAN, this.maskedPan);
        }
        return null;
    }
}
