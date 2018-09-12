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
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
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
public class PisConsentMapper {

    public Optional<PisConsent> mapToPisConsent(PisConsentRequest request) {
        return Optional.ofNullable(request.getPayments())
                   .map(pmt -> {
                       PisConsent consent = new PisConsent();
                       consent.setExternalId(UUID.randomUUID().toString());
                       consent.addPaymentsData(mapToPisPaymentDataList(pmt));
                       consent.setPisTppInfo(mapToPisTppInfo(request.getTppInfo()));
                       consent.setPisPaymentType(request.getPaymentType());
                       consent.setPisPaymentProduct(request.getPaymentProduct());
                       consent.setConsentType(ConsentType.PIS);
                       consent.setConsentStatus(CmsConsentStatus.RECEIVED);
                       consent.setAspspConsentData(request.getAspspConsentData());
                       return consent;
                   });
    }

    private List<PisPaymentData> mapToPisPaymentDataList(List<PisPayment> payments) {
        return payments.stream()
                   .map(this::mapToPisPaymentData)
                   .collect(Collectors.toList());
    }

    private PisPaymentData mapToPisPaymentData(PisPayment payment) {
        return Optional.ofNullable(payment)
                   .map(pm -> {
                       PisPaymentData pisPaymentData = new PisPaymentData();
                       pisPaymentData.setPaymentId(pm.getPaymentId());
                       pisPaymentData.setEndToEndIdentification(pm.getEndToEndIdentification());
                       pisPaymentData.setDebtorAccount(mapToPisAccountReference(pm.getDebtorAccount()));
                       pisPaymentData.setUltimateDebtor(pm.getUltimateDebtor());
                       pisPaymentData.setAmount(pm.getAmount());
                       pisPaymentData.setCurrency(pm.getCurrency());
                       pisPaymentData.setCreditorAccount(mapToPisAccountReference(pm.getCreditorAccount()));
                       pisPaymentData.setCreditorAgent(pm.getCreditorAgent());
                       pisPaymentData.setCreditorName(pm.getCreditorName());
                       pisPaymentData.setCreditorAddress(mapToPisAddress(pm.getCreditorAddress()));
                       pisPaymentData.setRemittanceInformationUnstructured(pm.getRemittanceInformationUnstructured());
                       pisPaymentData.setRemittanceInformationStructured(mapToPisRemittance(pm.getRemittanceInformationStructured()));
                       pisPaymentData.setRequestedExecutionDate(pm.getRequestedExecutionDate());
                       pisPaymentData.setRequestedExecutionTime(pm.getRequestedExecutionTime());
                       pisPaymentData.setUltimateCreditor(pm.getUltimateCreditor());
                       pisPaymentData.setPurposeCode(pm.getPurposeCode());
                       pisPaymentData.setStartDate(pm.getStartDate());
                       pisPaymentData.setEndDate(pm.getEndDate());
                       pisPaymentData.setExecutionRule(pm.getExecutionRule());
                       pisPaymentData.setFrequency(pm.getFrequency());
                       pisPaymentData.setDayOfExecution(pm.getDayOfExecution());

                       return pisPaymentData;
                   }).orElse(null);
    }

    private PisTppInfo mapToPisTppInfo(CmsTppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tin -> {
                       PisTppInfo pisTppInfo = new PisTppInfo();
                       pisTppInfo.setRegistrationNumber(tin.getRegistrationNumber());
                       pisTppInfo.setTppName(tin.getTppName());
                       pisTppInfo.setTppRole(tin.getTppRole());
                       pisTppInfo.setNationalCompetentAuthority(tin.getNationalCompetentAuthority());
                       pisTppInfo.setRedirectUri(tin.getRedirectUri());
                       pisTppInfo.setNokRedirectUri(tin.getNokRedirectUri());

                       return pisTppInfo;
                   }).orElse(null);
    }

    public GetPisConsentAuthorizationResponse mapToGetPisConsentAuthorizationResponse(PisConsentAuthorization pis) {
        GetPisConsentAuthorizationResponse response = new GetPisConsentAuthorizationResponse();
        response.setPayments(mapToPisPaymentList(pis.getConsent().getPayments()));
        response.setPaymentType(pis.getConsent().getPisPaymentType());
        response.setPassword(pis.getPassword());
        response.setPsuId(pis.getPsuId());
        response.setScaStatus(pis.getScaStatus());
        return response;
    }

    public UpdatePisConsentPsuDataResponse mapToUpdatePisConsentPsuDataResponse(PisConsentAuthorization pisConsentAuthorization) {
        return new UpdatePisConsentPsuDataResponse(pisConsentAuthorization.getScaStatus());
    }

    public Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> {
                       PisConsentResponse response = new PisConsentResponse();
                       response.setPayments(mapToPisPaymentList(pc.getPayments()));
                       response.setExternalId(pc.getExternalId());
                       response.setConsentStatus(pc.getConsentStatus());
                       response.setPaymentType(pc.getPisPaymentType());
                       response.setPaymentProduct(pc.getPisPaymentProduct());
                       response.setTppInfo(mapToTppInfo(pc.getPisTppInfo()));
                       response.setAspspConsentData(pc.getAspspConsentData());
                       return response;
                   });
    }

    private List<PisPayment> mapToPisPaymentList(List<PisPaymentData> payments) {
        return payments.stream()
                   .map(this::mapToPisPayment)
                   .collect(Collectors.toList());
    }

    private PisPayment mapToPisPayment(PisPaymentData payment) {
        return Optional.ofNullable(payment)
                   .map(pm -> {
                       PisPayment pisPayment = new PisPayment();
                       pisPayment.setPaymentId(pm.getPaymentId());
                       pisPayment.setEndToEndIdentification(pm.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(mapToCmsAccountReference(pm.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pm.getUltimateDebtor());
                       pisPayment.setCurrency(pm.getCurrency());
                       pisPayment.setAmount(pm.getAmount());
                       pisPayment.setCreditorAccount(mapToCmsAccountReference(pm.getCreditorAccount()));
                       pisPayment.setCreditorAgent(pm.getCreditorAgent());
                       pisPayment.setCreditorName(pm.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pm.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pm.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pm.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pm.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pm.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pm.getUltimateCreditor());
                       pisPayment.setPurposeCode(pm.getPurposeCode());
                       pisPayment.setStartDate(pm.getStartDate());
                       pisPayment.setEndDate(pm.getEndDate());
                       pisPayment.setExecutionRule(pm.getExecutionRule());
                       pisPayment.setFrequency(pm.getFrequency());
                       pisPayment.setDayOfExecution(pm.getDayOfExecution());

                       return pisPayment;
                   }).orElse(null);
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
                   .orElse(null);
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
                   .orElse(null);
    }

    private CmsAccountReference mapToCmsAccountReference(PisAccountReference pisAccountReference) {
        return Optional.ofNullable(pisAccountReference)
                   .map(ref -> new CmsAccountReference(ref.getIban(),
                       ref.getBban(),
                       ref.getPan(),
                       ref.getMaskedPan(),
                       ref.getMsisdn(),
                       ref.getCurrency())
                   ).orElse(null);
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
                   }).orElse(null);
    }

    private CmsTppInfo mapToTppInfo(PisTppInfo pisTppInfo) {
        return Optional.ofNullable(pisTppInfo)
                   .map(tpp -> {
                       CmsTppInfo tppInfo = new CmsTppInfo();

                       tppInfo.setRegistrationNumber(tpp.getRegistrationNumber());
                       tppInfo.setTppName(tpp.getTppName());
                       tppInfo.setTppRole(tpp.getTppRole());
                       tppInfo.setNationalCompetentAuthority(tpp.getNationalCompetentAuthority());
                       tppInfo.setRedirectUri(tpp.getRedirectUri());
                       tppInfo.setNokRedirectUri(tpp.getNokRedirectUri());
                       return tppInfo;

                   }).orElse(null);
    }

    private PisAccountReference mapToPisAccountReference(CmsAccountReference cmsAccountReference) {
        return Optional.ofNullable(cmsAccountReference)
                   .map(ref -> {
                       PisAccountReference pisAccountReference = new PisAccountReference();
                       pisAccountReference.setIban(cmsAccountReference.getIban());
                       pisAccountReference.setBban(cmsAccountReference.getBban());
                       pisAccountReference.setPan(cmsAccountReference.getPan());
                       pisAccountReference.setMaskedPan(cmsAccountReference.getMaskedPan());
                       pisAccountReference.setMsisdn(cmsAccountReference.getMsisdn());
                       pisAccountReference.setCurrency(cmsAccountReference.getCurrency());

                       return pisAccountReference;
                   }).orElse(null);
    }

    private PisAddress mapToPisAddress(CmsAddress cmsAddress) {
        return Optional.ofNullable(cmsAddress)
                   .map(adr -> {
                       PisAddress pisAddress = new PisAddress();
                       pisAddress.setStreet(adr.getStreet());
                       pisAddress.setBuildingNumber(adr.getBuildingNumber());
                       pisAddress.setCity(adr.getCity());
                       pisAddress.setPostalCode(adr.getPostalCode());
                       pisAddress.setCountry(adr.getCountry());

                       return pisAddress;
                   }).orElse(null);
    }
}
