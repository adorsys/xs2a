/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@ApiModel(description = "Account Reference", value = "AccountReference")
@EqualsAndHashCode(exclude = "id")
public class AccountReference {

    @JsonIgnore
    private Long id;

    @ApiModelProperty(example = "123-DEDE89370400440532013000-EUR")
    private String aspspAccountId;

    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "89370400440532013000")
    private String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "2356 5746 3217 1234")
    private String pan;

    @ApiModelProperty(value = "MASKED_PAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxx1234")
    private String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "+49(0)911 360698-0")
    private String msisdn;

    @ApiModelProperty(value = "Codes following ISO 4217", example = "EUR")
    private Currency currency;

    @ApiModelProperty(value = "Other account identifier", example = "30-163033-7")
    private String otherAccountIdentification;
    /**
     * This constructor should be used for storing initial accounts data (as it was requested by TPP)
     *
     * @param accountReferenceType  Account identifier type
     * @param accountReferenceValue Account identifier value
     * @param currency              Currency Type
     */
    public AccountReference(AccountReferenceType accountReferenceType, String accountReferenceValue, Currency currency) {
        this(accountReferenceType, accountReferenceValue, currency, null, null);
    }

    public AccountReference(String aspspAccountId, String resourceId, String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency, String otherAccountIdentification) {
        this.aspspAccountId = aspspAccountId;
        this.resourceId = resourceId;
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
        this.otherAccountIdentification = otherAccountIdentification;
    }

    /**
     * This constructor should be used for storing accounts data received from aspsp
     *
     * @param accountReferenceType  Account identifier type
     * @param accountReferenceValue Account identifier value
     * @param currency              Currency Type
     * @param resourceId            The identification that denotes the addressed account
     * @param aspspAccountId        Bank specific account ID
     */
    public AccountReference(AccountReferenceType accountReferenceType, String accountReferenceValue, Currency currency, String resourceId, String aspspAccountId) {
        if (accountReferenceType != null) {
            accountReferenceType.setFieldValue(this, accountReferenceValue);
        }
        this.currency = currency;
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
    }

    public AccountReference(Long id, AccountReferenceType accountReferenceType, String accountReferenceValue, Currency currency, String resourceId, String aspspAccountId) {
        if (accountReferenceType != null) {
            accountReferenceType.setFieldValue(this, accountReferenceValue);
        }
        this.id = id;
        this.currency = currency;
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
    }

    @JsonIgnore
    public AccountReferenceType getAccountReferenceType() {
        return getUsedAccountReferenceSelector().getAccountReferenceType();
    }

    @JsonIgnore
    public AccountReferenceSelector getUsedAccountReferenceSelector() {
        return Stream.of(AccountReferenceType.values())
                   .sorted(Comparator.comparingInt(AccountReferenceType::getOrder))
                   .filter(type -> StringUtils.isNotBlank(type.getFieldValue(this)))
                   .findFirst().map(type -> new AccountReferenceSelector(type, type.getFieldValue(this)))
                   .orElseThrow(() -> new IllegalArgumentException("At least one account reference property must be set!"));
    }

    @JsonIgnore
    public Set<AccountReferenceType> getUsedAccountReferenceFields() {
        return Stream.of(AccountReferenceType.values())
                   .filter(type -> StringUtils.isNotBlank(type.getFieldValue(this)))
                   .collect(Collectors.toSet());

    }

    @JsonIgnore
    public boolean isNotCardAccount() {
        return StringUtils.isAllBlank(pan, maskedPan);
    }

    @JsonIgnore
    public boolean isNotIbanAccount() {
        return StringUtils.isAllBlank(iban, bban, msisdn);
    }
}
