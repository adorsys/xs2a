/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.consent.api.CmsAccountReference;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccess;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountConsent;
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AisConsentMapper {

    public AisAccountConsent mapToAisAccountConsent(AisConsent consent) {
        return new AisAccountConsent(
            consent.getId().toString(),
            mapToAisAccountAccess(consent.getAccesses()),
            consent.isRecurringIndicator(),
            consent.getExpireDate(),
            consent.getUsageCounter(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            false,
            consent.isTppRedirectPreferred(),
            consent.getAspspConsentData());
    }

    private AisAccountAccess mapToAisAccountAccess(List<AccountAccess> accountAccesses) {
        return new AisAccountAccess(mapToCmsAccountReference(accountAccesses, TypeAccess.ACCOUNT),
            mapToCmsAccountReference(accountAccesses, TypeAccess.BALANCE),
            mapToCmsAccountReference(accountAccesses, TypeAccess.TRANSACTION));
    }

    private List<CmsAccountReference> mapToCmsAccountReference(List<AccountAccess> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .filter(ass -> ass.getTypeAccess() == typeAccess)
                   .map(access -> new CmsAccountReference(access.getIban(), access.getCurrency()))
                   .collect(Collectors.toList());
    }
}
