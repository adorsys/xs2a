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

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createConsent(PisConsentRequest request) {
        return mapToPisConsent(request.getSinglePayment())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createConsentBulkPayment(PisConsentBulkPaymentRequest request) {
        return mapToBulkPaymentConsent(request.getPayments())
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


    private Optional<PisConsent> mapToBulkPaymentConsent(List<SpiSinglePayments> payments) {
        List<PisPaymentData> paymentDataList = payments.stream()
                                                   .map(this::mapToPisPaymentData)
                                                   .filter(Optional::isPresent)
                                                   .map(Optional::get)
                                                   .collect(Collectors.toList());
        PisConsent consent = new PisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setPayments(paymentDataList);
        consent.setConsentType(ConsentType.PIS);
        consent.setPisConsentType(PisConsentType.SINGLE);
        consent.setConsentStatus(SpiConsentStatus.RECEIVED);

        return Optional.of(consent);
    }

    private Optional<PisConsent> mapToPisConsent(SpiSinglePayments singlePayment) {
        return Optional.ofNullable(singlePayment)
                   .flatMap(this::mapToPisPaymentData)
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(pmt));
                       consent.setConsentType(ConsentType.PIS);
                       consent.setPisConsentType(PisConsentType.SINGLE);
                       consent.setConsentStatus(SpiConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisPaymentData> mapToPisPaymentData(SpiSinglePayments singlePayment) {
        return Optional.ofNullable(singlePayment)
                   .map(sp -> {
                       PisPaymentData payment = new PisPaymentData();
                       payment.setEndToEndIdentification(sp.getEndToEndIdentification());
                       payment.setDebtorIban(sp.getDebtorAccount().getIban());
                       payment.setUltimateDebtor(sp.getUltimateDebtor());
                       payment.setAmount(sp.getInstructedAmount().getContent());
                       payment.setCurrency(sp.getInstructedAmount().getCurrency());
                       payment.setCreditorIban(sp.getCreditorAccount().getIban());
                       payment.setCreditorAgent(sp.getCreditorAgent());
                       payment.setCreditorName(sp.getCreditorName());
                       payment.setRequestedExecutionDate(sp.getRequestedExecutionDate());
                       payment.setRequestedExecutionTime(sp.getRequestedExecutionTime());
                       payment.setUltimateCreditor(sp.getUltimateCreditor());
                       payment.setPurposeCode(sp.getPurposeCode());
                       return payment;
                   });
    }


    private Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> new PisConsentResponse(
                       pc.getExternalId(),
                       pc.getPisConsentType(),
                       pc.getConsentStatus()));
    }
}
