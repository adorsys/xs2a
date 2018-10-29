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

package de.adorsys.psd2.consent.server.service.mapper;

import de.adorsys.psd2.consent.api.CmsAccountReference;
import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.CmsConsentStatus;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.server.domain.ConsentType;
import de.adorsys.psd2.consent.server.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisConsentMapper {
    private final ConsentMapper consentMapper;

    public PisConsent mapToPisConsent(PisConsentRequest request) {
        PisConsent consent = new PisConsent();
        consent.setPayments(mapToPisPaymentDataList(request.getPayments(), consent));
        consent.setTppInfo(consentMapper.mapToTppInfo(request.getTppInfo()));
        consent.setPaymentType(request.getPaymentType());
        consent.setPisPaymentProduct(request.getPaymentProduct());
        consent.setConsentType(ConsentType.PIS);
        consent.setConsentStatus(CmsConsentStatus.RECEIVED);
        return consent;
    }

    public List<PisPaymentData> mapToPisPaymentDataList(List<PisPayment> payments, PisConsent consent) {
        if (CollectionUtils.isEmpty(payments)) {
            return Collections.emptyList();
        }
        return payments.stream()
                   .map(p -> mapToPisPaymentData(p, consent))
                   .collect(Collectors.toList());
    }

    private PisPaymentData mapToPisPaymentData(PisPayment payment, PisConsent consent) {
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
                       pisPaymentData.setConsent(consent);

                       return pisPaymentData;
                   }).orElse(null);
    }

    public GetPisConsentAuthorisationResponse mapToGetPisConsentAuthorizationResponse(PisConsentAuthorization pis) {
        GetPisConsentAuthorisationResponse response = new GetPisConsentAuthorisationResponse();
        response.setPayments(mapToPisPaymentList(pis.getConsent().getPayments()));
        response.setPaymentType(pis.getConsent().getPaymentType());
        response.setScaStatus(pis.getScaStatus());
        response.setConsentId(pis.getConsent().getExternalId());
        return response;
    }

    public Optional<PisConsentResponse> mapToPisConsentResponse(PisConsent pisConsent) {
        return Optional.ofNullable(pisConsent)
                   .map(pc -> {
                       PisConsentResponse response = new PisConsentResponse();
                       response.setPayments(mapToPisPaymentList(pc.getPayments()));
                       response.setExternalId(pc.getExternalId());
                       response.setConsentStatus(pc.getConsentStatus());
                       response.setPaymentType(pc.getPaymentType());
                       response.setPaymentProduct(pc.getPisPaymentProduct());
                       response.setTppInfo(consentMapper.mapToCmsTppInfo(pc.getTppInfo()));
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
