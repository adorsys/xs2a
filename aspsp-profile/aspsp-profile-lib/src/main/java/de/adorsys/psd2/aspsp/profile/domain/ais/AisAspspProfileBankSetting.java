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

package de.adorsys.psd2.aspsp.profile.domain.ais;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AisAspspProfileBankSetting {

    /**
     * 	URL to online-banking to authorise consent with redirect approach
     */
    private ConsentTypeBankSetting consentTypes;


    /**
     * 	A group of settings to define URL link for redirect approach
     */
    private AisRedirectLinkBankSetting redirectLinkToOnlineBanking;


    /**
     * 	A group of settings for transactions support
     */
    private AisTransactionBankSetting transactionParameters;


    /**
     * 	A group of settings for delta reports support
     */
    private DeltaReportBankSetting deltaReportSettings;


    /**
     * 	A group of settings to set up SCA for one-time consent
     */
    private OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsents;
}
