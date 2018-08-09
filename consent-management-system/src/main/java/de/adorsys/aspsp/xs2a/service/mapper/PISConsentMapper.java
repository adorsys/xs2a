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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.consent.api.*;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ConsentType;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PISConsentMapper {

    public Optional<PisConsent> mapToPisConsent(PisConsentRequest request) {
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
        pisPaymentData.setRemittanceInformationStructured(mapToPisRemittance(payment.getRemittanceInformationStructured()));
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

    public Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> {
                       PisConsentResponse response = new PisConsentResponse();
                       response.setPayments(mapToPisPaymentList(pc.getPayments()));
                       response.setExternalId(pc.getExternalId());
                       response.setConsentStatus(pc.getConsentStatus());
                       response.setPaymentService(pc.getPisPaymentService());
                       response.setPaymentProduct(pc.getPisPaymentProduct());
                       response.setTppInfo(mapToTppInfo(pc.getPisTppInfo()));

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
        pisPayment.setDebtorAccount(mapToCmsAccountReference(payment.getDebtorAccount()));
        pisPayment.setUltimateDebtor(payment.getUltimateDebtor());
        pisPayment.setCurrency(payment.getCurrency());
        pisPayment.setAmount(payment.getAmount());
        pisPayment.setCreditorAccount(mapToCmsAccountReference(payment.getCreditorAccount()));
        pisPayment.setCreditorAgent(payment.getCreditorAgent());
        pisPayment.setCreditorName(payment.getCreditorName());
        pisPayment.setCreditorAddress(mapToCmsAddress(payment.getCreditorAddress()));
        pisPayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(payment.getRemittanceInformationStructured()));
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

    private PisRemittance mapToPisRemittance(CmsRemittance remittance) {
        return Optional.ofNullable(remittance)
                   .map(r -> {
                       PisRemittance pisRemittance = new PisRemittance();
                       pisRemittance.setReference(r.getReference());
                       pisRemittance.setReferenceIssuer(r.getReferenceIssuer());
                       pisRemittance.setReferenceType(r.getReferenceType());
                       return pisRemittance;
                   })
                   .orElse(new PisRemittance());
    }

    private CmsRemittance mapToCmsRemittance(PisRemittance pisRemittance) {
        return Optional.ofNullable(pisRemittance)
                   .map(r -> {
                       CmsRemittance remittance = new CmsRemittance();
                       remittance.setReference(r.getReference());
                       remittance.setReferenceIssuer(r.getReferenceIssuer());
                       remittance.setReferenceType(r.getReferenceType());
                       return remittance;
                   })
                   .orElse(new CmsRemittance());
    }

    private CmsAccountReference mapToCmsAccountReference(PisAccountReference pisAccountReference) {
        return new CmsAccountReference(pisAccountReference.getIban(),
            pisAccountReference.getBban(),
            pisAccountReference.getPan(),
            pisAccountReference.getMaskedPan(),
            pisAccountReference.getMsisdn(),
            pisAccountReference.getCurrency());
    }

    private CmsAddress mapToCmsAddress(PisAddress pisAddress) {
        return Optional.ofNullable(pisAddress)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreet());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getCity());
                       cmsAddress.setPostalCode(adr.getPostalCode());
                       cmsAddress.setCountry(adr.getCountry());
                       return cmsAddress;
                   }).orElse(new CmsAddress());
    }

    private TppInfo mapToTppInfo(PisTppInfo pisTppInfo) {
        TppInfo tppInfo = new TppInfo();

        tppInfo.setRegistrationNumber(pisTppInfo.getRegistrationNumber());
        tppInfo.setTppName(pisTppInfo.getTppName());
        tppInfo.setTppRole(pisTppInfo.getTppRole());
        tppInfo.setNationalCompetentAuthority(pisTppInfo.getNationalCompetentAuthority());
        tppInfo.setRedirectUri(pisTppInfo.getRedirectUri());
        tppInfo.setNokRedirectUri(pisTppInfo.getNokRedirectUri());

        return tppInfo;
    }

    private PisAccountReference mapToPisAccountReference(CmsAccountReference cmsAccountReference) {
        PisAccountReference pisAccountReference = new PisAccountReference();
        pisAccountReference.setIban(cmsAccountReference.getIban());
        pisAccountReference.setBban(cmsAccountReference.getBban());
        pisAccountReference.setPan(cmsAccountReference.getPan());
        pisAccountReference.setMaskedPan(cmsAccountReference.getMaskedPan());
        pisAccountReference.setMsisdn(cmsAccountReference.getMsisdn());
        pisAccountReference.setCurrency(cmsAccountReference.getCurrency());

        return pisAccountReference;
    }

    private PisAddress mapToPisAddress(CmsAddress cmsAddress) {
        PisAddress pisAddress = new PisAddress();
        pisAddress.setStreet(cmsAddress.getStreet());
        pisAddress.setBuildingNumber(cmsAddress.getBuildingNumber());
        pisAddress.setCity(cmsAddress.getCity());
        pisAddress.setPostalCode(cmsAddress.getPostalCode());
        pisAddress.setCountry(cmsAddress.getCountry());

        return pisAddress;
    }
}
