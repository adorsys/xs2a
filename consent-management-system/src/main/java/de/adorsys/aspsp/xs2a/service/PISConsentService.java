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
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentService;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ConsentType;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.repository.PisConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PISConsentService {
    private final PisConsentRepository pisConsentRepository;

    public Optional<String> createPaymentConsent(PisConsentRequest request) {
        return mapToPisConsent(request)
                   .map(pisConsentRepository::save)
                   .map(PisConsent::getExternalId);
    }

    private Optional<PisConsent> mapToPisConsent(PisConsentRequest request) {
        return Optional.ofNullable(request.getPayments())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.setPayments(mapToPisPaymentDataList(pmt));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getTppInfo()));
                       consent.setPisPaymentService(request.getPaymentService());
                       consent.setPisPaymentProduct(request.getPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(ConsentStatus.RECEIVED);
                       return consent;
                   });
    }

    private List<PisPaymentData> mapToPisPaymentDataList(List<PisPayment> payments) {
        return payments.stream()
                   .map(this::mapToPisPaymentData)
                   .collect(Collectors.toList());
    }

    private PisPaymentData mapToPisPaymentData(PisPayment payment) {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(payment.getPaymentId());
        pisPaymentData.setEndToEndIdentification(payment.getEndToEndIdentification());
        pisPaymentData.setDebtorAccount(mapToPisAccountReference(payment.getDebtorAccount()));
        pisPaymentData.setUltimateDebtor(payment.getUltimateDebtor());
        pisPaymentData.setAmount(payment.getAmount());
        pisPaymentData.setCurrency(payment.getCurrency());
        pisPaymentData.setCreditorAccount(mapToPisAccountReference(payment.getCreditorAccount()));
        pisPaymentData.setCreditorAgent(payment.getCreditorAgent());
        pisPaymentData.setCreditorName(payment.getCreditorName());
        pisPaymentData.setCreditorAddress(mapToPisAddress(payment.getCreditorAddress()));
        pisPaymentData.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPaymentData.setRemittanceInformationStructured(payment.getRemittanceInformationStructured());
        pisPaymentData.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        pisPaymentData.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        pisPaymentData.setUltimateCreditor(payment.getUltimateCreditor());
        pisPaymentData.setPurposeCode(payment.getPurposeCode());
        pisPaymentData.setStartDate(payment.getStartDate());
        pisPaymentData.setEndDate(payment.getEndDate());
        pisPaymentData.setExecutionRule(payment.getExecutionRule());
        pisPaymentData.setFrequency(payment.getFrequency());
        pisPaymentData.setDayOfExecution(payment.getDayOfExecution());

        return pisPaymentData;
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

    private Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> {
                       PisConsentResponse response = new PisConsentResponse();
                       response.setPayments(mapToPisPaymentList(pc.getPayments()));
                       response.setExternalId(pc.getExternalId());
                       response.setConsentStatus(pc.getConsentStatus());
                       response.setPaymentService(pc.getPisPaymentService());
                       response.setPaymentProduct(pc.getPisPaymentProduct());
                       response.setTppInfo(mapToPisAbstractConsentDataPisTppInfo(pc.getPisTppInfo()));

                       return response;
                   });
    }

    private List<PisPayment> mapToPisPaymentList(List<PisPaymentData> payments) {
        return payments.stream()
                   .map(this::mapToPisPayment)
                   .collect(Collectors.toList());
    }

    private PisPayment mapToPisPayment(PisPaymentData payment) {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setPaymentId(payment.getPaymentId());
        pisPayment.setEndToEndIdentification(payment.getEndToEndIdentification());
        pisPayment.setDebtorAccount(mapToPisSinglePaymentPisAccountReference(payment.getDebtorAccount()));
        pisPayment.setUltimateDebtor(payment.getUltimateDebtor());
        pisPayment.setCurrency(payment.getCurrency());
        pisPayment.setAmount(payment.getAmount());
        pisPayment.setCreditorAccount(mapToPisSinglePaymentPisAccountReference(payment.getCreditorAccount()));
        pisPayment.setCreditorAgent(payment.getCreditorAgent());
        pisPayment.setCreditorName(payment.getCreditorName());
        pisPayment.setCreditorAddress(mapToPisSinglePaymentPisAddress(payment.getCreditorAddress()));
        pisPayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPayment.setRemittanceInformationStructured(payment.getRemittanceInformationStructured());
        pisPayment.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        pisPayment.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        pisPayment.setUltimateCreditor(payment.getUltimateCreditor());
        pisPayment.setPurposeCode(payment.getPurposeCode());
        pisPayment.setStartDate(payment.getStartDate());
        pisPayment.setEndDate(payment.getEndDate());
        pisPayment.setExecutionRule(payment.getExecutionRule());
        pisPayment.setFrequency(payment.getFrequency());
        pisPayment.setDayOfExecution(payment.getDayOfExecution());

        return pisPayment;
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
