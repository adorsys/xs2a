/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import org.springframework.web.bind.annotation.*;

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
