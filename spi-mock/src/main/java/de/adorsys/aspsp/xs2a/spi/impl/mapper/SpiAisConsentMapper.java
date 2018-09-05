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

package de.adorsys.aspsp.xs2a.spi.impl.mapper;

import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SpiAisConsentMapper {

    public Optional<SpiConsentStatus> mapToSpiConsentStatus(CmsConsentStatus consentStatus) {
        return Optional.ofNullable(consentStatus)
                   .map(status -> SpiConsentStatus.valueOf(status.name()));
    }

    public CreateAisConsentRequest mapToCmsCreateAisConsentRequest(SpiCreateAisConsentRequest req) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuId(r.getPsuId());
                       aisRequest.setTppId(r.getTppId());
                       aisRequest.setFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAccess(mapToCmsAisAccountAccessInfo(r.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());
                       aisRequest.setAspspConsentData(r.getAspspConsentData());

                       return aisRequest;
                   })
                   .orElse(null);
    }

    private AisAccountAccessInfo mapToCmsAisAccountAccessInfo(SpiAccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(Optional.ofNullable(access.getAccounts())
                                   .map(this::mapToListCmsAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setBalances(Optional.ofNullable(access.getBalances())
                                   .map(this::mapToListCmsAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setTransactions(Optional.ofNullable(access.getTransactions())
                                       .map(this::mapToListCmsAccountInfo)
                                       .orElseGet(Collections::emptyList));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(SpiAccountAccessType::name)
                                            .orElse(null));
        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(SpiAccountAccessType::name)
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListCmsAccountInfo(List<SpiAccountReference> refs) {
        return refs.stream()
                   .map(this::mapToCmsAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToCmsAccountInfo(SpiAccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }

    public AisConsentAuthorizationRequest mapToAisConsentAuthorization(SpiScaStatus scaStatus) {
        return Optional.ofNullable(scaStatus)
                   .map(st -> {
                       AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
                       consentAuthorization.setScaStatus(CmsScaStatus.valueOf(st.name()));
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public SpiAccountConsentAuthorization mapToSpiAccountConsentAuthorization(AisConsentAuthorizationResponse response) {
        return Optional.ofNullable(response)
                   .map(resp -> {
                       SpiAccountConsentAuthorization consentAuthorization = new SpiAccountConsentAuthorization();

                       consentAuthorization.setId(resp.getAuthorizationId());
                       consentAuthorization.setConsentId(resp.getConsentId());
                       consentAuthorization.setPsuId(resp.getPsuId());
                       consentAuthorization.setScaStatus(SpiScaStatus.valueOf(resp.getScaStatus().name()));
                       consentAuthorization.setAuthenticationMethodId(resp.getAuthenticationMethodId());
                       consentAuthorization.setScaAuthenticationData(resp.getScaAuthenticationData());
                       consentAuthorization.setPassword(resp.getPassword());
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public AisConsentAuthorizationRequest mapToAisConsentAuthorizationRequest(SpiUpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(updatePsuData)
                   .map(data -> {
                       AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
                       consentAuthorization.setPsuId(data.getPsuId());
                       consentAuthorization.setScaStatus(CmsScaStatus.valueOf(data.getScaStatus().name()));
                       consentAuthorization.setAuthenticationMethodId(data.getAuthenticationMethodId());
                       consentAuthorization.setPassword(data.getPassword());
                       consentAuthorization.setScaAuthenticationData(data.getScaAuthenticationData());

                       return consentAuthorization;
                   })
                   .orElse(null);
    }
}
