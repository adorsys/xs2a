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

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.PisConsent;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createConsent(PisConsentRequest request) {
        PisConsent consent = new PisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setDebtorAccount(mapToAccountReference(request.getDebtorAccount()));
        consent.setInstructedAmount(mapToAmount(request.getInstructedAmount()));
        consent.setCreditorAccount(mapToAccountReference(request.getCreditorAccount()));
        consent.setCreditorAgent(request.getCreditorAgent());
        consent.setCreditorName(request.getCreditorName());
        consent.setRequestedExecutionDate(request.getRequestedExecutionDate());
        consent.setRequestedExecutionTime(request.getRequestedExecutionTime());
        consent.setConsentStatus(SpiConsentStatus.RECEIVED);
        return Optional.of(consent.getExternalId());
    }

    public Optional<SpiConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    public Optional<Boolean> updateConsentStatusById(String consentId, SpiConsentStatus status) {
        return getPisConsentById(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    private Optional<PisConsent> getPisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, SpiConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private AccountReference mapToAccountReference(SpiAccountReference reference) {
        return new AccountReference(reference.getIban(), reference.getCurrency());
    }

    private Amount mapToAmount(SpiAmount amount) {
        return new Amount(amount.getCurrency(), new BigDecimal(amount.getContent()));
    }
}
