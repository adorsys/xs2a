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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "Account information", value = "AccountInfo")
public class AccountInfo {

    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "DE2310010010156789")
    private String aspspAccountId;

    @ApiModelProperty(value = "Account-Identifier: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE2310010010123456789")
    private String accountIdentifier;

    @ApiModelProperty(value = "ISO 4217 currency code", example = "EUR")
    private String currency;

    @ApiModelProperty(value = "Type of the account reference: IBAN, BBAN, IBAN, BBAN, PAN, MASKED_PAN, MSISDN", required = true, example = "IBAN")
    private AccountReferenceType accountReferenceType;

    private AccountInfo(){}

    public static AccountInfoBuilder builder() {
        return new AccountInfoBuilder();
    }

    public static final class AccountInfoBuilder {
        private String resourceId;
        private String aspspAccountId;
        private String accountIdentifier;
        private String currency;
        private AccountReferenceType accountReferenceType;

        private AccountInfoBuilder() {
        }

        public AccountInfoBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public AccountInfoBuilder aspspAccountId(String aspspAccountId) {
            this.aspspAccountId = aspspAccountId;
            return this;
        }

        public AccountInfoBuilder accountIdentifier(String accountIdentifier) {
            this.accountIdentifier = accountIdentifier;
            return this;
        }

        public AccountInfoBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public AccountInfoBuilder accountReferenceType(AccountReferenceType accountReferenceType) {
            this.accountReferenceType = accountReferenceType;
            return this;
        }

        public AccountInfo build() {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setResourceId(resourceId);
            accountInfo.setAspspAccountId(aspspAccountId);
            accountInfo.setAccountIdentifier(accountIdentifier);
            accountInfo.setCurrency(currency);
            accountInfo.setAccountReferenceType(accountReferenceType);
            return accountInfo;
        }
    }
}
