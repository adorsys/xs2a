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

package de.adorsys.psd2.consent.aspsp.api.piis;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Piis consent request", name = "PiisConsentRequest")
public class CreatePiisConsentRequest {
    @Schema(description = "TPP attribute that fully described TPP for which the consent will be created. If the property is omitted, the consent will be created for all TPPs")
    private String tppAuthorisationNumber;

    @Schema(description = "Account, where the confirmation of funds service is aimed to be submitted to.")
    private AccountReference account;

    @Schema(description = "Consent`s expiration date. The content is the local ASPSP date in ISODate Format", example = "2020-10-10")
    private LocalDate validUntil;

    @Schema(description = "Card Number of the card issued by the PIISP. Should be delivered if available.", example = "1234567891234")
    private String cardNumber;

    @Schema(description = "Expiry date of the card issued by the PIISP", example = "2020-12-31")
    private LocalDate cardExpiryDate;

    @Schema(description = "Additional explanation for the card product.", example = "MyMerchant Loyalty Card")
    private String cardInformation;

    @Schema(description = "Additional information about the registration process for the PSU, e.g. a reference to the TPP / PSU contract.", example = "Your contract Number 1234 with MyMerchant is completed with the registration with your bank.")
    private String registrationInformation;
}

