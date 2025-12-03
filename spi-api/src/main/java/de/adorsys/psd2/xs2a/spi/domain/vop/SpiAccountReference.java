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

package de.adorsys.psd2.xs2a.spi.domain.vop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Account Reference.
 * See document "openFinance API Framework Data Dictionary", section "Account Reference" for more details.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpiAccountReference {
    
    /**
     * IBAN of the account.
     */
    private String iban;
    
    /**
     * BBAN of the account. This data elements is used for payment accounts which have no IBAN.
     */
    private String bban;
    
    /**
     * Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements.
     */
    private String pan;
    
    /**
     * Primary Account Number (PAN) of a card in a masked form.
     */
    private String maskedPan;
    
    /**
     * An alias to access a payment account via a registered mobile phone number.
     */
    private String msisdn;
    
    /**
     * An alias with a proprietary coding.
     */
    private SpiGenericAccountIdentification other;
    
    /**
     * Cash account type code.
     * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
     */
    private SpiCashAccountType typeCode;
    
    /**
     * Cash account type proprietary.
     * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
     */
    private String typeProprietary;
    
    /**
     * ISO 4217 Alpha 3 currency code.
     */
    private String currency;
    
    /**
     * Proxy account identification.
     */
    private SpiProxyAccountIdentification proxy;
    
    /**
     * Name of the account.
     */
    private String name;
    
    /**
     * Owner of the account.
     */
    private SpiPartyDescription2 owner;
    
    /**
     * Financial institution servicing the account.
     */
    private SpiFinancialInstitutionIdentification servicer;
}
