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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@ApiModel(description = "Account information", value = "AccountInfo")
public class AccountInfo {

    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;

    @ApiModelProperty(value = "Account-Identifier: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE2310010010123456789")
    private String accountIdentifier;

    @ApiModelProperty(value = "ISO 4217 currency code", example = "EUR")
    private String currency;

    @ApiModelProperty(value = "Type of the account reference: IBAN, BBAN, IBAN, BBAN, PAN, MASKED_PAN, MSISDN", required = true, example = "IBAN")
    private AccountReferenceType accountType;

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
            accountInfo.setAccountType(accountReferenceType);
            return accountInfo;
        }
    }
}
