/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisRemittance;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisCommonPaymentMapper {
    private final TppInfoMapper tppInfoMapper;
    private final PsuDataMapper psuDataMapper;
    private final AccountReferenceMapper accountReferenceMapper;
    private final AuthorisationMapper authorisationMapper;
    private final CmsAddressMapper cmsAddressMapper;

    public PisCommonPaymentData mapToPisCommonPaymentData(PisPaymentInfo paymentInfo) {
        PisCommonPaymentData commonPaymentData = new PisCommonPaymentData();
        commonPaymentData.setPaymentId(paymentInfo.getPaymentId());
        commonPaymentData.setPaymentType(paymentInfo.getPaymentType());
        commonPaymentData.setPaymentProduct(paymentInfo.getPaymentProduct());
        commonPaymentData.setTransactionStatus(paymentInfo.getTransactionStatus());
        commonPaymentData.setPayment(paymentInfo.getPaymentData());
        commonPaymentData.setTppInfo(tppInfoMapper.mapToTppInfoEntity(paymentInfo.getTppInfo()));
        commonPaymentData.setPsuDataList(psuDataMapper.mapToPsuDataList(paymentInfo.getPsuDataList(), paymentInfo.getInstanceId()));
        commonPaymentData.getPsuDataList().forEach(p -> p.setInstanceId(paymentInfo.getInstanceId()));
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

        commonPaymentData.setTppNotificationUri(paymentInfo.getTppNotificationUri());
        commonPaymentData.setTppNotificationContentPreferred(paymentInfo.getNotificationSupportedModes());
        commonPaymentData.setContentType(paymentInfo.getContentType());
        commonPaymentData.setTppBrandLoggingInformation(paymentInfo.getTppBrandLoggingInformation());
        commonPaymentData.setInstanceId(paymentInfo.getInstanceId());

        return commonPaymentData;
    }

    public List<PisCommonPaymentResponse> mapToPisCommonPaymentResponses(List<PisCommonPaymentData> commonPaymentDatas, Map<String, List<AuthorisationEntity>> authorisations) {
        return commonPaymentDatas.stream()
                   .map(payment -> mapToPisCommonPaymentResponse(payment, authorisations.get(payment.getExternalId())).get())
                   .collect(Collectors.toList());
    }

    public Optional<PisCommonPaymentResponse> mapToPisCommonPaymentResponse(PisCommonPaymentData commonPaymentData, List<AuthorisationEntity> authorisations) {
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
                       response.setAuthorisations(authorisationMapper.mapToAuthorisations(authorisations));
                       response.setCreationTimestamp(cmd.getCreationTimestamp());
                       response.setContentType(cmd.getContentType());
                       response.setInstanceId(cmd.getInstanceId());
                       response.setSigningBasketBlocked(cmd.isSigningBasketBlocked());
                       response.setSigningBasketAuthorised(cmd.isSigningBasketAuthorised());
                       return response;
                   });
    }

    private List<PisPayment> mapToPisPaymentList(List<PisPaymentData> payments, OffsetDateTime offsetDateTime) {
        List<PisPayment> pisPayments = payments.stream()
                                           .map(this::mapToPisPayment)
                                           .collect(Collectors.toList());

        pisPayments.forEach(pisPayment -> pisPayment.setCreationTimestamp(offsetDateTime));
        return pisPayments;
    }

    public PisPayment mapToPisPayment(PisPaymentData payment) {
        return Optional.ofNullable(payment)
                   .map(pm -> {
                       PisPayment pisPayment = new PisPayment();
                       pisPayment.setPaymentId(pm.getPaymentId());
                       pisPayment.setEndToEndIdentification(pm.getEndToEndIdentification());
                       pisPayment.setInstructionIdentification(pm.getInstructionIdentification());
                       pisPayment.setDebtorAccount(accountReferenceMapper.mapToAccountReference(pm.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pm.getUltimateDebtor());
                       pisPayment.setCurrency(pm.getCurrency());
                       pisPayment.setAmount(pm.getAmount());
                       pisPayment.setCreditorAccount(accountReferenceMapper.mapToAccountReference(pm.getCreditorAccount()));
                       pisPayment.setCreditorAgent(pm.getCreditorAgent());
                       pisPayment.setCreditorName(pm.getCreditorName());
                       pisPayment.setCreditorAddress(cmsAddressMapper.mapToCmsAddress(pm.getCreditorAddress()));
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
}
