/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Account Reference")
@EqualsAndHashCode(exclude = "id")
public class AccountReference {

    @JsonIgnore
    private Long id;

    @Schema(example = "123-DEDE89370400440532013000-EUR")
    private String aspspAccountId;

    @Schema(description = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @Schema(description = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @Schema(description = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "89370400440532013000")
    private String bban;

    @Schema(description = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "2356 5746 3217 1234")
    private String pan;

    @Schema(description = "MASKED_PAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxx1234")
    private String maskedPan;

    @Schema(description = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "+49(0)911 360698-0")
    private String msisdn;

    @Schema(description = "Codes following ISO 4217", example = "EUR")
    private Currency currency;

    @Schema(description = "Other account identifier", example = "30-163033-7")
    private String other;

    private String cashAccountType;

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

    public AccountReference(String aspspAccountId, String resourceId, String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency, String other) {
        this.aspspAccountId = aspspAccountId;
        this.resourceId = resourceId;
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
        this.other = other;
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
