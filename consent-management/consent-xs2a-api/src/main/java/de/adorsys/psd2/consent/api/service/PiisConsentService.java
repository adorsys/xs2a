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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import org.jetbrains.annotations.Nullable;

import java.util.Currency;
import java.util.List;

public interface PiisConsentService {

    /**
     * Retrieves list of PIIS consents by account reference and optional currency.
     *
     * @param currency              Chosen currency (may be null if not provided during PIIS consent creation)
     * @param accountIdentifierName The name of account reference identifier
     * @return PIIS consents
     */
    CmsResponse<List<CmsConsent>> getPiisConsentListByAccountIdentifier(@Nullable Currency currency, AccountReferenceSelector accountIdentifierName);
}
