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

package de.adorsys.aspsp.xs2a.integtest.config.rest.consent;

import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessType;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsentMapper {

    public CreateAisConsentRequest mapToAisConsentRequest(CreateConsentReq req, String psuId, String tppId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest request = new CreateAisConsentRequest();
                       request.setPsuId(psuId);
                       request.setTppId(tppId);
                       request.setFrequencyPerDay(r.getFrequencyPerDay());
                       request.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       request.setValidUntil(r.getValidUntil());
                       request.setRecurringIndicator(r.isRecurringIndicator());
                       request.setCombinedServiceIndicator(r.isCombinedServiceIndicator());

                       return request;
                   })
                   .orElse(null);
    }


    //Spi
    private AisAccountAccessInfo mapToAisAccountAccessInfo(Xs2aAccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(Optional.ofNullable(access.getAccounts())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setBalances(Optional.ofNullable(access.getBalances())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setTransactions(Optional.ofNullable(access.getTransactions())
                                       .map(this::mapToListAccountInfo)
                                       .orElseGet(Collections::emptyList));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(availAcc -> AisAccountAccessType.valueOf(availAcc.name()))
                                            .orElse(null));
        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(allPsd2 -> AisAccountAccessType.valueOf(allPsd2.name()))
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(List<Xs2aAccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToAccountInfo(Xs2aAccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setResourceId(ref.getResourceId());
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }
}
