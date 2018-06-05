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

import de.adorsys.aspsp.xs2a.domain.PisConsent;
import de.adorsys.aspsp.xs2a.domain.PisConsentResponse;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createConsent(PisConsentRequest request) {
        return mapToPisConsent(request.getSinglePayment())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<SpiConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return getPisConsentById(consentId)
                   .flatMap(this::mapToPisConsentResponse);
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

    private Optional<PisConsent> mapToPisConsent(SpiSinglePayments singlePayment) {
        return Optional.ofNullable(singlePayment)
                   .map(sp -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setEndToEndIdentification(sp.getEndToEndIdentification());
                       consent.setDebtorIban(sp.getDebtorAccount().getIban());
                       consent.setUltimateDebtor(sp.getUltimateDebtor());
                       consent.setAmount(sp.getInstructedAmount().getContent());
                       consent.setCurrency(sp.getInstructedAmount().getCurrency());
                       consent.setCreditorIban(sp.getCreditorAccount().getIban());
                       consent.setCreditorAgent(sp.getCreditorAgent());
                       consent.setCreditorName(sp.getCreditorName());
                       consent.setRequestedExecutionDate(sp.getRequestedExecutionDate());
                       consent.setRequestedExecutionTime(sp.getRequestedExecutionTime());
                       consent.setUltimateCreditor(sp.getUltimateCreditor());
                       consent.setPurposeCode(sp.getPurposeCode());
                       consent.setConsentStatus(SpiConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> new PisConsentResponse(
                       pc.getExternalId(),
                       pc.getDebtorIban(),
                       pc.getUltimateDebtor(),
                       pc.getCurrency(),
                       pc.getAmount(),
                       pc.getCreditorIban(),
                       pc.getCreditorAgent(),
                       pc.getCreditorName(),
                       pc.getRequestedExecutionDate(),
                       pc.getRequestedExecutionTime(),
                       pc.getConsentStatus()));
    }
}
