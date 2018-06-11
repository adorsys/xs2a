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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.account.AccountHolder;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.EXPIRED;
import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.RECEIVED;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    private final AspspProfileService profileService;
    private final AisConsentRepository aisConsentRepository;
    private final ConsentMapper consentMapper;

    @Transactional
    public Optional<String> createConsent(AisConsentRequest request) {
        int minFrequencyPerDay = profileService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        AisConsent consent = new AisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(RECEIVED);
        consent.setExpectedFrequencyPerDay(minFrequencyPerDay);
        consent.setTppFrequencyPerDay(request.getFrequencyPerDay());
        consent.setUsageCounter(minFrequencyPerDay);
        consent.setRequestDate(Instant.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuId(request.getPsuId());
        consent.setTppId(request.getTppId());
        consent.addAccounts(readAccounts(request.getAccess()));
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        AisConsent saved = aisConsentRepository.save(consent);
        return saved.getId() != null
                   ? Optional.ofNullable(saved.getExternalId())
                   : Optional.empty();
    }

    private List<AisAccount> readAccounts(AisAccountAccessInfo access) {
        AccountHolder holder = new AccountHolder();
        holder.fillAccess(access.getAccounts(), TypeAccess.ACCOUNT);
        holder.fillAccess(access.getBalances(), TypeAccess.BALANCE);
        holder.fillAccess(access.getTransactions(), TypeAccess.TRANSACTION);
        return buildAccounts(holder.getAccountAccesses());
    }

    private List<AisAccount> buildAccounts(Map<String, Set<AccountAccess>> accountAccesses) {
        return accountAccesses
                   .entrySet().stream()
                   .map(e -> new AisAccount(e.getKey(), e.getValue()))
                   .collect(Collectors.toList());
    }

    public Optional<SpiConsentStatus> getConsentStatusById(String consentId) {
        return getAisConsentById(consentId)
                   .map(AisConsent::getConsentStatus);
    }

    public Optional<Boolean> updateConsentStatusById(String consentId, SpiConsentStatus status) {
        return getAisConsentById(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    public Optional<SpiAccountConsent> getSpiAccountConsentById(String consentId) {
        return getAisConsentById(consentId)
                   .map(consentMapper::mapToSpiAccountConsent);
    }

    @Transactional
    public Optional<Long> consentActionLog(ConsentActionRequest request) {
        // TODO
        return null;
    }

    private Optional<AisConsent> getAisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(aisConsentRepository::findByExternalId);
    }

    private AisConsent setStatusAndSaveConsent(AisConsent consent, SpiConsentStatus status) {
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent);
    }
}
