/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.payment.*;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisCommonPaymentMapper {
    private final TppInfoMapper tppInfoMapper;
    private final PsuDataMapper psuDataMapper;
    private final AccountReferenceMapper accountReferenceMapper;
    private final CmsAuthorisationMapper cmsAuthorisationMapper;

    public List<PisPaymentData> mapToPisPaymentDataList(List<PisPayment> payments, PisCommonPaymentData pisCommonPayment) {
        if (CollectionUtils.isEmpty(payments)) {
            return Collections.emptyList();
        }

        return payments.stream()
                   .map(p -> mapToPisPaymentData(p, pisCommonPayment))
                   .collect(Collectors.toList());
    }

    public PisCommonPaymentData mapToPisCommonPaymentData(PisPaymentInfo paymentInfo) {
        PisCommonPaymentData commonPaymentData = new PisCommonPaymentData();
        commonPaymentData.setPaymentId(paymentInfo.getPaymentId());
        commonPaymentData.setPaymentType(paymentInfo.getPaymentType());
        commonPaymentData.setPaymentProduct(paymentInfo.getPaymentProduct());
        commonPaymentData.setTransactionStatus(paymentInfo.getTransactionStatus());
        commonPaymentData.setPayment(paymentInfo.getPaymentData());
        commonPaymentData.setTppInfo(tppInfoMapper.mapToTppInfoEntity(paymentInfo.getTppInfo()));
        commonPaymentData.setPsuDataList(psuDataMapper.mapToPsuDataList(paymentInfo.getPsuDataList()));
        commonPaymentData.setMultilevelScaRequired(paymentInfo.isMultilevelScaRequired());
        commonPaymentData.setAspspAccountId(paymentInfo.getAspspAccountId());
        AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();
        TppRedirectUri tppRedirectUri = paymentInfo.getTppRedirectUri();
        if (tppRedirectUri != null) {
            authorisationTemplate.setRedirectUri(tppRedirectUri.getUri());
            authorisationTemplate.setNokRedirectUri(tppRedirectUri.getNokUri());
        }
        commonPaymentData.setAuthorisationTemplate(authorisationTemplate);
        commonPaymentData.setInternalRequestId(paymentInfo.getInternalRequestId());
        Optional.ofNullable(paymentInfo.getCreationTimestamp()).ifPresent(commonPaymentData::setCreationTimestamp);
        return commonPaymentData;
    }

    private PisPaymentData mapToPisPaymentData(PisPayment payment, PisCommonPaymentData pisCommonPayment) {
        return Optional.ofNullable(payment)
                   .map(pm -> {
                       PisPaymentData pisPaymentData = new PisPaymentData();
                       pisPaymentData.setPaymentId(pm.getPaymentId());
                       pisPaymentData.setEndToEndIdentification(pm.getEndToEndIdentification());
                       pisPaymentData.setDebtorAccount(accountReferenceMapper.mapToAccountReferenceEntity(pm.getDebtorAccount()));
                       pisPaymentData.setUltimateDebtor(pm.getUltimateDebtor());
                       pisPaymentData.setAmount(pm.getAmount());
                       pisPaymentData.setCurrency(pm.getCurrency());
                       pisPaymentData.setCreditorAccount(accountReferenceMapper.mapToAccountReferenceEntity(pm.getCreditorAccount()));
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
                       pisPaymentData.setPaymentData(pisCommonPayment);
                       pisPaymentData.setBatchBookingPreferred(pm.getBatchBookingPreferred());

                       return pisPaymentData;
                   }).orElse(null);
    }

    public GetPisAuthorisationResponse mapToGetPisAuthorizationResponse(PisAuthorization pis) {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        Optional.ofNullable(pis.getPaymentData())
            .ifPresent(paymentData -> {
                response.setPayments(mapToPisPaymentList(paymentData.getPayments(), paymentData.getCreationTimestamp()));
                response.setPaymentId(paymentData.getPaymentId());
                response.setPaymentType(paymentData.getPaymentType());
                response.setPaymentInfo(mapToPisPaymentInfo(paymentData));
            });
        response.setScaStatus(pis.getScaStatus());
        response.setPsuIdData(psuDataMapper.mapToPsuIdData(pis.getPsuData()));
        response.setChosenScaApproach(pis.getScaApproach());
        return response;
    }

    public Optional<PisCommonPaymentResponse> mapToPisCommonPaymentResponse(PisCommonPaymentData commonPaymentData) {
        return Optional.ofNullable(commonPaymentData)
                   .map(cmd -> {
                       PisCommonPaymentResponse response = new PisCommonPaymentResponse();
                       response.setPayments(mapToPisPaymentList(cmd.getPayments(), cmd.getCreationTimestamp()));
                       response.setExternalId(cmd.getPaymentId());
                       response.setPaymentType(cmd.getPaymentType());
                       response.setPaymentProduct(cmd.getPaymentProduct());
                       response.setTppInfo(tppInfoMapper.mapToTppInfo(cmd.getTppInfo()));
                       response.setPsuData(psuDataMapper.mapToPsuIdDataList(cmd.getPsuDataList()));
                       response.setPaymentData(cmd.getPayment());
                       response.setTransactionStatus(cmd.getTransactionStatus());
                       response.setStatusChangeTimestamp(cmd.getStatusChangeTimestamp());
                       response.setMultilevelScaRequired(cmd.isMultilevelScaRequired());
                       response.setAuthorisations(cmsAuthorisationMapper.mapToAuthorisations(cmd.getAuthorizations()));
                       response.setCreationTimestamp(cmd.getCreationTimestamp());
                       return response;
                   });
    }

    private PisPaymentInfo mapToPisPaymentInfo(PisCommonPaymentData paymentData) {
        return Optional.ofNullable(paymentData)
                   .map(dta -> {
                            PisPaymentInfo paymentInfo = new PisPaymentInfo();
                            paymentInfo.setPaymentId(dta.getPaymentId());
                            paymentInfo.setPaymentProduct(dta.getPaymentProduct());
                            paymentInfo.setPaymentType(dta.getPaymentType());
                            paymentInfo.setTransactionStatus(dta.getTransactionStatus());
                            paymentInfo.setPaymentData(dta.getPayment());
                            paymentInfo.setPsuDataList(psuDataMapper.mapToPsuIdDataList(dta.getPsuDataList()));
                            paymentInfo.setTppInfo(tppInfoMapper.mapToTppInfo(dta.getTppInfo()));
                            paymentInfo.setCreationTimestamp(paymentData.getCreationTimestamp());

                            return paymentInfo;
                        }
                   )
                   .orElse(null);
    }

    private List<PisPayment> mapToPisPaymentList(List<PisPaymentData> payments, OffsetDateTime offsetDateTime) {
        List<PisPayment> pisPayments = payments.stream()
                                       .map(this::mapToPisPayment)
                                       .collect(Collectors.toList());

        pisPayments.forEach(pisPayment -> pisPayment.setCreationTimestamp(offsetDateTime));
        return pisPayments;
    }

    private PisPayment mapToPisPayment(PisPaymentData payment) {
        return Optional.ofNullable(payment)
                   .map(pm -> {
                       PisPayment pisPayment = new PisPayment();
                       pisPayment.setPaymentId(pm.getPaymentId());
                       pisPayment.setEndToEndIdentification(pm.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(accountReferenceMapper.mapToCmsAccountReference(pm.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pm.getUltimateDebtor());
                       pisPayment.setCurrency(pm.getCurrency());
                       pisPayment.setAmount(pm.getAmount());
                       pisPayment.setCreditorAccount(accountReferenceMapper.mapToCmsAccountReference(pm.getCreditorAccount()));
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
                       pisPayment.setPsuDataList(psuDataMapper.mapToPsuIdDataList(pm.getPaymentData().getPsuDataList()));
                       pisPayment.setBatchBookingPreferred(pm.getBatchBookingPreferred());

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

    CmsAddress mapToCmsAddress(PisAddress pisAddress) {
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
