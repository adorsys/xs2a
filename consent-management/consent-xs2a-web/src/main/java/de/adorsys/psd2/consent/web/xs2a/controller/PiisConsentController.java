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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.PiisConsentApi;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PiisConsentController implements PiisConsentApi {
    private final PiisConsentService piisConsentService;

    @Override
    public ResponseEntity<List<CmsConsent>> getPiisConsentListByAccountReference(String currency, AccountReferenceType accountReferenceType, String accountIdentifier) {
        Currency nullableCurrency = StringUtils.isBlank(currency) ? null : Currency.getInstance(currency);
        CmsResponse<List<CmsConsent>> response = piisConsentService.getPiisConsentListByAccountIdentifier(nullableCurrency, new AccountReferenceSelector(accountReferenceType, accountIdentifier));

        return response.isSuccessful() && CollectionUtils.isNotEmpty(response.getPayload())
                   ? ResponseEntity.ok(response.getPayload())
                   : ResponseEntity.notFound().build();
    }
}
