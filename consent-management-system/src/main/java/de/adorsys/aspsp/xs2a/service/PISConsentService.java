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

import de.adorsys.aspsp.xs2a.consent.api.AccountReference;
import de.adorsys.aspsp.xs2a.consent.api.Address;
import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.TppInfo;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentService;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPeriodicPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisSinglePayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.*;
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

    public Optional<PisAbstractConsentResponse> getConsentById(String consentId) {
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
                       consent.setConsentStatus(ConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private Optional<PisConsent> mapToPeriodicPaymentConsent(PisPeriodicPaymentConsentRequest request) {
        return Optional.ofNullable(request.getPeriodicPayment())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(mapToPisPaymentDataFromPeriodic(pmt)));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getPisTppInfo()));
                       consent.setPisPaymentService(PisPaymentService.PERIODIC);
                       consent.setPisPaymentProduct(request.getPisPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(ConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private List<PisPaymentData> mapToPisPaymentDataList(List<PisSinglePayment> payments) {
        return payments.stream()
                   .map(this::mapToPisPaymentDataFromSingle)
                   .collect(Collectors.toList());
    }

    private Optional<PisConsent> mapToPisConsent(PisSinglePaymentConsentRequest request) {
        return Optional.ofNullable(request.getSinglePayment())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(Collections.singletonList(mapToPisPaymentDataFromSingle(pmt)));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getPisTppInfo()));
                       consent.setPisPaymentService(PisPaymentService.SINGLE);
                       consent.setPisPaymentProduct(request.getPisPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(ConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private PisTppInfo mapToPisTppInfo(TppInfo pisTppInfo) {
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

    private Optional<PisAbstractConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> mapToConsentResponse(pc, pc.getPisPaymentService()));
    }

    private PisAbstractConsentResponse mapToConsentResponse(PisConsent pisConsent, PisPaymentService pisPaymentService) {
        if (pisPaymentService == PisPaymentService.SINGLE) {
            return mapToPisSinglePaymentConsentResponse(pisConsent);
        } else if (pisPaymentService == PisPaymentService.BULK) {
            return mapToPisBulkPaymentConsentResponse(pisConsent);
        } else if (pisPaymentService == PisPaymentService.PERIODIC) {
            return mapToPisPeriodicPaymentConsentResponse(pisConsent);
        } else {
            return null;
        }
    }

    private PisSinglePaymentConsentResponse mapToPisSinglePaymentConsentResponse(PisConsent pisConsent) {
        PisSinglePaymentConsentResponse response = new PisSinglePaymentConsentResponse();
        response.setPayment(mapToPisSinglePayment(pisConsent.getPayments().get(0)));// todo make correct getting data
        response.setExternalId(pisConsent.getExternalId());
        response.setConsentStatus(pisConsent.getConsentStatus());
        response.setPisPaymentService(pisConsent.getPisPaymentService());
        response.setPisPaymentProduct(pisConsent.getPisPaymentProduct());
        response.setPisTppInfo(mapToPisAbstractConsentDataPisTppInfo(pisConsent.getPisTppInfo()));

        return response;
    }

    private PisBulkPaymentConsentResponse mapToPisBulkPaymentConsentResponse(PisConsent pisConsent) {
        PisBulkPaymentConsentResponse response = new PisBulkPaymentConsentResponse();
        response.setPayments(mapToPisSinglePaymentList(pisConsent.getPayments()));
        response.setExternalId(pisConsent.getExternalId());
        response.setConsentStatus(pisConsent.getConsentStatus());
        response.setPisPaymentService(pisConsent.getPisPaymentService());
        response.setPisPaymentProduct(pisConsent.getPisPaymentProduct());
        response.setPisTppInfo(mapToPisAbstractConsentDataPisTppInfo(pisConsent.getPisTppInfo()));

        return response;
    }

    private PisPeriodicPaymentConsentResponse mapToPisPeriodicPaymentConsentResponse(PisConsent pisConsent) {
        PisPeriodicPaymentConsentResponse response = new PisPeriodicPaymentConsentResponse();// todo make correct getting data
        response.setPeriodicPayment(mapToPisPeriodicPayment(pisConsent.getPayments().get(0)));
        response.setExternalId(pisConsent.getExternalId());
        response.setConsentStatus(pisConsent.getConsentStatus());
        response.setPisPaymentService(pisConsent.getPisPaymentService());
        response.setPisPaymentProduct(pisConsent.getPisPaymentProduct());
        response.setPisTppInfo(mapToPisAbstractConsentDataPisTppInfo(pisConsent.getPisTppInfo()));

        return response;
    }

    private List<PisSinglePayment> mapToPisSinglePaymentList(List<PisPaymentData> payments) {
        return payments.stream()
                   .map(this::mapToPisSinglePayment)
                   .collect(Collectors.toList());
    }

    private PisPeriodicPayment mapToPisPeriodicPayment(PisPaymentData payment) {
        PisPeriodicPayment pisPeriodicPayment = new PisPeriodicPayment();
        pisPeriodicPayment.setPaymentId(payment.getPaymentId());
        pisPeriodicPayment.setEndToEndIdentification(payment.getEndToEndIdentification());
        pisPeriodicPayment.setDebtorAccount(mapToPisSinglePaymentPisAccountReference(payment.getDebtorAccount()));
        pisPeriodicPayment.setUltimateDebtor(payment.getUltimateDebtor());
        pisPeriodicPayment.setCurrency(payment.getCurrency());
        pisPeriodicPayment.setAmount(payment.getAmount());
        pisPeriodicPayment.setCreditorAccount(mapToPisSinglePaymentPisAccountReference(payment.getCreditorAccount()));
        pisPeriodicPayment.setCreditorAgent(payment.getCreditorAgent());
        pisPeriodicPayment.setCreditorName(payment.getCreditorName());
        pisPeriodicPayment.setCreditorAddress(mapToPisSinglePaymentPisAddress(payment.getCreditorAddress()));
        pisPeriodicPayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPeriodicPayment.setRemittanceInformationStructured(payment.getRemittanceInformationStructured());
        pisPeriodicPayment.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        pisPeriodicPayment.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        pisPeriodicPayment.setUltimateCreditor(payment.getUltimateCreditor());
        pisPeriodicPayment.setPurposeCode(payment.getPurposeCode());
        pisPeriodicPayment.setStartDate(payment.getStartDate());
        pisPeriodicPayment.setEndDate(payment.getEndDate());
        pisPeriodicPayment.setExecutionRule(payment.getExecutionRule());
        pisPeriodicPayment.setFrequency(payment.getFrequency());
        pisPeriodicPayment.setDayOfExecution(payment.getDayOfExecution());

        return pisPeriodicPayment;
    }

    private PisSinglePayment mapToPisSinglePayment(PisPaymentData payment) {
        PisSinglePayment singlePayment = new PisSinglePayment();

        singlePayment.setPaymentId(payment.getPaymentId());
        singlePayment.setEndToEndIdentification(payment.getEndToEndIdentification());
        singlePayment.setDebtorAccount(mapToPisSinglePaymentPisAccountReference(payment.getDebtorAccount()));
        singlePayment.setUltimateDebtor(payment.getUltimateDebtor());
        singlePayment.setCurrency(payment.getCurrency());
        singlePayment.setAmount(payment.getAmount());
        singlePayment.setCreditorAccount(mapToPisSinglePaymentPisAccountReference(payment.getCreditorAccount()));
        singlePayment.setCreditorAgent(payment.getCreditorAgent());
        singlePayment.setCreditorName(payment.getCreditorName());
        singlePayment.setCreditorAddress(mapToPisSinglePaymentPisAddress(payment.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        singlePayment.setRemittanceInformationStructured(payment.getRemittanceInformationStructured());
        singlePayment.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        singlePayment.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        singlePayment.setUltimateCreditor(payment.getUltimateCreditor());
        singlePayment.setPurposeCode(payment.getPurposeCode());

        return singlePayment;
    }

    private AccountReference mapToPisSinglePaymentPisAccountReference(PisAccountReference pisAccountReference) {
        return new AccountReference(pisAccountReference.getIban(),
            pisAccountReference.getBban(),
            pisAccountReference.getPan(),
            pisAccountReference.getMaskedPan(),
            pisAccountReference.getMsisdn(),
            pisAccountReference.getCurrency());
    }

    private Address mapToPisSinglePaymentPisAddress(PisAddress pisAddress) {
        return new Address(
            pisAddress.getStreet(),
            pisAddress.getBuildingNumber(),
            pisAddress.getCity(),
            pisAddress.getPostalCode(),
            pisAddress.getCountry());
    }

    private TppInfo mapToPisAbstractConsentDataPisTppInfo(PisTppInfo pisTppInfo) {
        TppInfo tppInfo = new TppInfo();

        tppInfo.setRegistrationNumber(pisTppInfo.getRegistrationNumber());
        tppInfo.setTppName(pisTppInfo.getTppName());
        tppInfo.setTppRole(pisTppInfo.getTppRole());
        tppInfo.setNationalCompetentAuthority(pisTppInfo.getNationalCompetentAuthority());
        tppInfo.setRedirectUri(pisTppInfo.getRedirectUri());
        tppInfo.setNokRedirectUri(pisTppInfo.getNokRedirectUri());

        return tppInfo;
    }

    private PisPaymentData mapToPisPaymentDataFromPeriodic(PisPeriodicPayment periodicPayment) {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(periodicPayment.getPaymentId());
        pisPaymentData.setEndToEndIdentification(periodicPayment.getEndToEndIdentification());
        pisPaymentData.setDebtorAccount(mapToPisAccountReference(periodicPayment.getDebtorAccount()));
        pisPaymentData.setUltimateDebtor(periodicPayment.getUltimateDebtor());
        pisPaymentData.setAmount(periodicPayment.getAmount());
        pisPaymentData.setCurrency(periodicPayment.getCurrency());
        pisPaymentData.setCreditorAccount(mapToPisAccountReference(periodicPayment.getCreditorAccount()));
        pisPaymentData.setCreditorAgent(periodicPayment.getCreditorAgent());
        pisPaymentData.setCreditorName(periodicPayment.getCreditorName());
        pisPaymentData.setCreditorAddress(mapToPisAddress(periodicPayment.getCreditorAddress()));
        pisPaymentData.setRemittanceInformationUnstructured(periodicPayment.getRemittanceInformationUnstructured());
        pisPaymentData.setRemittanceInformationStructured(periodicPayment.getRemittanceInformationStructured());
        pisPaymentData.setRequestedExecutionDate(periodicPayment.getRequestedExecutionDate());
        pisPaymentData.setRequestedExecutionTime(periodicPayment.getRequestedExecutionTime());
        pisPaymentData.setUltimateCreditor(periodicPayment.getUltimateCreditor());
        pisPaymentData.setPurposeCode(periodicPayment.getPurposeCode());
        pisPaymentData.setStartDate(periodicPayment.getStartDate());
        pisPaymentData.setEndDate(periodicPayment.getEndDate());
        pisPaymentData.setExecutionRule(periodicPayment.getExecutionRule());
        pisPaymentData.setFrequency(periodicPayment.getFrequency());
        pisPaymentData.setDayOfExecution(periodicPayment.getDayOfExecution());

        return pisPaymentData;
    }

    private PisPaymentData mapToPisPaymentDataFromSingle(PisSinglePayment singlePayment) {
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

    private PisAccountReference mapToPisAccountReference(AccountReference accountReference) {
        PisAccountReference pisAccountReference = new PisAccountReference();
        pisAccountReference.setIban(accountReference.getIban());
        pisAccountReference.setBban(accountReference.getBban());
        pisAccountReference.setPan(accountReference.getPan());
        pisAccountReference.setMaskedPan(accountReference.getMaskedPan());
        pisAccountReference.setMsisdn(accountReference.getMsisdn());
        pisAccountReference.setCurrency(accountReference.getCurrency());

        return pisAccountReference;
    }

    private PisAddress mapToPisAddress(Address address) {
        PisAddress pisAddress = new PisAddress();
        pisAddress.setStreet(address.getStreet());
        pisAddress.setBuildingNumber(address.getBuildingNumber());
        pisAddress.setCity(address.getCity());
        pisAddress.setPostalCode(address.getPostalCode());
        pisAddress.setCountry(address.getCountry());

        return pisAddress;
    }
}
