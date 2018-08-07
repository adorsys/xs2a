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
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPeriodicPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisSinglePayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.*;
import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisConsentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ConsentType;
import de.adorsys.aspsp.xs2a.domain.pis.*;
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
public class PISConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createSinglePaymentConsent(PisSinglePaymentConsentRequest request) {
        return mapToPisConsent(request)
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createBulkPaymentConsent(PisBulkPaymentConsentRequest request) {
        return mapToBulkPaymentConsent(request)
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<String> createPeriodicPaymentConsent(PisPeriodicPaymentConsentRequest request) {
        return mapToPeriodicPaymentConsent(request)
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return getPisConsentById(consentId)
                   .flatMap(this::mapToPisConsentResponse);
    }

    public Optional<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        return getPisConsentById(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    private Optional<PisConsent> getPisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, ConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private Optional<PisConsent> mapToBulkPaymentConsent(PisBulkPaymentConsentRequest request) {
        return Optional.ofNullable(request.getPayments())
                   .map(pmts -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(mapToPisPaymentDataList(pmts));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getPisTppInfo()));
                       consent.setPisPaymentService(PisPaymentService.BULK);
                       consent.setPisPaymentProduct(request.getPisPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsent> mapToPeriodicPaymentConsent(PisPeriodicPaymentConsentRequest request) {
        return Optional.ofNullable(request.getPeriodicPayment())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(mapToPisPaymentData(pmt)));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getPisTppInfo()));
                       consent.setPisPaymentService(PisPaymentService.PERIODIC);
                       consent.setPisPaymentProduct(request.getPisPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private List<PisPaymentData> mapToPisPaymentDataList(List<PisSinglePayment> payments) {
        return payments.stream()
                   .map(this::mapToPisPaymentData)
                   .collect(Collectors.toList());
    }

    private Optional<PisConsent> mapToPisConsent(PisSinglePaymentConsentRequest request) {
        return Optional.ofNullable(request.getPisSinglePayment())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(mapToPisPaymentData(pmt)));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getPisTppInfo()));
                       consent.setPisPaymentService(PisPaymentService.SINGLE);
                       consent.setPisPaymentProduct(request.getPisPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(PisConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private PisTppInfo mapToPisTppInfo(PisAbstractConsentRequest.PisTppInfo pisTppInfo) {
        return Optional.ofNullable(pisTppInfo)
                   .map(tin -> {
                       PisTppInfo tppInfo = new PisTppInfo();
                       tppInfo.setRegistrationNumber(tin.getRegistrationNumber());
                       tppInfo.setTppName(tin.getTppName());
                       tppInfo.setTppRole(tin.getTppRole());
                       tppInfo.setNationalCompetentAuthority(tin.getNationalCompetentAuthority());
                       tppInfo.setRedirectUri(tin.getRedirectUri());
                       tppInfo.setNokRedirectUri(tin.getNokRedirectUri());

                       return tppInfo;
                   }).orElse(null);
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

    private PisPaymentData mapToPisPeriodicPaymentData(PisPeriodicPayment periodicPayment) {
        PisPaymentData pisPaymentData = mapToPisPaymentData(periodicPayment);
        pisPaymentData.setStartDate(periodicPayment.getStartDate());
        pisPaymentData.setExecutionRule(periodicPayment.getExecutionRule());
        pisPaymentData.setEndDate(periodicPayment.getEndDate());
        pisPaymentData.setFrequency(periodicPayment.getFrequency());
        pisPaymentData.setDayOfExecution(periodicPayment.getDayOfExecution());

        return pisPaymentData;
    }

    private PisPaymentData mapToPisPaymentData(PisSinglePayment singlePayment) {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(singlePayment.getPaymentId());
        pisPaymentData.setEndToEndIdentification(singlePayment.getEndToEndIdentification());
        pisPaymentData.setDebtorAccount(mapToPisAccountReference(singlePayment.getDebtorAccount()));
        pisPaymentData.setUltimateDebtor(singlePayment.getUltimateDebtor());
        pisPaymentData.setAmount(singlePayment.getAmount());
        pisPaymentData.setCurrency(singlePayment.getCurrency());
        pisPaymentData.setCreditorAccount(mapToPisAccountReference(singlePayment.getCreditorAccount()));
        pisPaymentData.setCreditorAgent(singlePayment.getCreditorAgent());
        pisPaymentData.setCreditorName(singlePayment.getCreditorName());
        pisPaymentData.setCreditorAddress(mapToPisAddress(singlePayment.getCreditorAddress()));
        pisPaymentData.setRemittanceInformationUnstructured(singlePayment.getRemittanceInformationUnstructured());
        pisPaymentData.setRemittanceInformationStructured(singlePayment.getRemittanceInformationStructured());
        pisPaymentData.setRequestedExecutionDate(singlePayment.getRequestedExecutionDate());
        pisPaymentData.setRequestedExecutionTime(singlePayment.getRequestedExecutionTime());
        pisPaymentData.setUltimateCreditor(singlePayment.getUltimateCreditor());
        pisPaymentData.setPurposeCode(singlePayment.getPurposeCode());

        return pisPaymentData;
    }

    private PisAccountReference mapToPisAccountReference(PisSinglePayment.PisAccountReference accountReference) {
        PisAccountReference pisAccountReference = new PisAccountReference();
        pisAccountReference.setIban(accountReference.getIban());
        pisAccountReference.setBban(accountReference.getBban());
        pisAccountReference.setPan(accountReference.getPan());
        pisAccountReference.setMaskedPan(accountReference.getMaskedPan());
        pisAccountReference.setMsisdn(accountReference.getMsisdn());
        pisAccountReference.setCurrency(accountReference.getCurrency());

        return pisAccountReference;
    }

    private PisAddress mapToPisAddress(PisSinglePayment.PisAddress address) {
        PisAddress pisAddress = new PisAddress();
        pisAddress.setStreet(address.getStreet());
        pisAddress.setBuildingNumber(address.getBuildingNumber());
        pisAddress.setCity(address.getCity());
        pisAddress.setPostalCode(address.getPostalCode());
        pisAddress.setCountry(address.getCountry());

        return pisAddress;
    }
}
