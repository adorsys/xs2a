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

import de.adorsys.aspsp.xs2a.consent.api.pis.PisConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentService;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ConsentType;
import de.adorsys.aspsp.xs2a.domain.pis.PisConsent;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PISConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createSinglePaymentConsent(PisConsentRequest request) {
        return mapToPisConsent(request.getPaymentId())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createBulkPaymentConsent(PisConsentBulkPaymentRequest request) {
        return mapToBulkPaymentConsent(request.getPaymentIds())
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createPeriodicPaymentConsent(PisConsentPeriodicPaymentRequest request) {
        return mapToPeriodicPaymentConsent(request.getPeriodicPaymentId())
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

    private Optional<PisConsent> mapToBulkPaymentConsent(List<String> paymentIds) { //NOPMD
    // todo make correct mapping

        PisConsent consent = new PisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentType(ConsentType.AIS);
        consent.setConsentType(ConsentType.PIS);
        consent.setPisPaymentService(PisPaymentService.BULK);
        consent.setConsentStatus(PisConsentStatus.RECEIVED);

        return Optional.of(consent);
    }

    private Optional<PisConsent> mapToPeriodicPaymentConsent(String periodicPaymentId) {
        // todo make correct mapping



        return Optional.ofNullable(periodicPaymentId)
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setConsentType(ConsentType.AIS);
                       consent.setConsentType(ConsentType.PIS);
                       consent.setPisPaymentService(PisPaymentService.PERIODIC);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsent> mapToPisConsent(String paymentId) {
        // todo make correct mapping



        return Optional.ofNullable(paymentId)
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());

                       consent.setConsentType(ConsentType.PIS);
                       consent.setPisPaymentService(PisPaymentService.SINGLE);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {




        return Optional.ofNullable(pisConsent)
                   .map(pc -> new PisConsentResponse(
                       pc.getExternalId(),
                       pc.getPisPaymentService(),
                       pc.getConsentStatus(),
                       null) // todo make correct mapping
                   );
    }
}
