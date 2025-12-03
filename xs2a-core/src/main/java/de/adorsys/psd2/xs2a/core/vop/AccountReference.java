/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.vop;

import lombok.Data;

/**
 * Account Reference POJO for VOP API.
 * See document "openFinance API Framework Data Dictionary", section "Account Reference" for more details.
 */
@Data
public class AccountReference {
    /**
     * IBAN of the account.
     * Example: FR7612345987650123456789014
     */
    private String iban;

    /**
     * BBAN of the account. This data element is used for payment accounts which have no IBAN.
     * Example: BARC12345612345678
     */
    private String bban;

    /**
     * Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements.
     * Maximum 35 characters.
     */
    private String pan;

    /**
     * Primary Account Number (PAN) of a card in a masked form.
     * Maximum 35 characters.
     */
    private String maskedPan;

    /**
     * An alias to access a payment account via a registered mobile phone number.
     * Maximum 35 characters.
     */
    private String msisdn;

    /**
     * An alias with a proprietary coding.
     */
    private GenericAccountIdentification other;

    /**
     * Account type code.
     * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
     */
    private CashAccountType typeCode;

    /**
     * Account type proprietary.
     * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
     * Maximum 35 characters.
     */
    private String typeProprietary;

    /**
     * ISO 4217 Alpha 3 currency code.
     * Example: EUR
     */
    private String currency;

    /**
     * Proxy account identification.
     */
    private ProxyAccountIdentification proxy;

    /**
     * Name of the account.
     * Maximum 70 characters.
     */
    private String name;

    /**
     * Owner of the account.
     */
    private PartyDescription2 owner;

    /**
     * Financial institution that services the account.
     */
    private FinancialInstitutionIdentification servicer;
}
