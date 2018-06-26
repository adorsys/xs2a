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

import de.adorsys.aspsp.xs2a.consent.api.pis.*;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ConsentType;
import de.adorsys.aspsp.xs2a.domain.PisConsent;
import de.adorsys.aspsp.xs2a.domain.PisPaymentData;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
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

    public Optional<String> createSinglePaymentConsent(PisConsentRequest request) {
        return mapToPisConsent(request.getPisSinglePayment())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createBulkPaymentConsent(PisConsentBulkPaymentRequest request) {
        return mapToBulkPaymentConsent(request.getPayments())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createPeriodicPaymentConsent(PisConsentPeriodicPaymentRequest request) {
        return mapToPeriodicPaymentConsent(request.getPisPeriodicPayment())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<PisConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return getPisConsentById(consentId)
                   .flatMap(this::mapToPisConsentResponse);
    }

    public Optional<Boolean> updateConsentStatusById(String consentId, PisConsentStatus status) {
        return getPisConsentById(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    private Optional<PisConsent> getPisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, PisConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private Optional<PisConsent> mapToBulkPaymentConsent(List<PisSinglePayment> payments) {
        List<PisPaymentData> paymentDataList = payments.stream()
                                                   .map(this::mapToPisPaymentData)
                                                   .filter(Optional::isPresent)
                                                   .map(Optional::get)
                                                   .collect(Collectors.toList());
        PisConsent consent = new PisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setPayments(paymentDataList);
        consent.setConsentType(ConsentType.PIS);
        consent.setPisConsentType(PisConsentType.BULK);
        consent.setConsentStatus(PisConsentStatus.RECEIVED);

        return Optional.of(consent);
    }

    private Optional<PisConsent> mapToPeriodicPaymentConsent(PisPeriodicPayment periodicPayment) {
        return Optional.ofNullable(periodicPayment)
                   .flatMap(this::mapToPisPaymentData)
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(pmt));
                       consent.setConsentType(ConsentType.PIS);
                       consent.setPisConsentType(PisConsentType.PERIODIC);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsent> mapToPisConsent(PisSinglePayment singlePayment) {
        return Optional.ofNullable(singlePayment)
                   .flatMap(this::mapToPisPaymentData)
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(pmt));
                       consent.setConsentType(ConsentType.PIS);
                       consent.setPisConsentType(PisConsentType.SINGLE);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisPaymentData> mapToPisPaymentData(PisSinglePayment singlePayment) {
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
                       pc.getConsentStatus(),
                       mapToPisPayments(pc.getPayments()))
                   );
    }

    private List<PisPayment> mapToPisPayments(List<PisPaymentData> payments) {
        return payments.stream()
                   .map(pmt -> new PisPayment(
                       pmt.getEndToEndIdentification(),
                       pmt.getDebtorIban(),
                       pmt.getUltimateDebtor(),
                       pmt.getCurrency(),
                       pmt.getAmount(),
                       pmt.getCreditorIban(),
                       pmt.getCreditorAgent(),
                       pmt.getCreditorName(),
                       pmt.getRequestedExecutionDate(),
                       pmt.getRequestedExecutionTime(),
                       pmt.getUltimateCreditor(),
                       pmt.getPurposeCode()))
                   .collect(Collectors.toList());
    }
}
