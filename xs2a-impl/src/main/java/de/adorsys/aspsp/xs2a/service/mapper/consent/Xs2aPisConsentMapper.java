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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.consent.api.CmsAccountReference;
import de.adorsys.aspsp.xs2a.consent.api.CmsAddress;
import de.adorsys.aspsp.xs2a.consent.api.CmsRemittance;
import de.adorsys.aspsp.xs2a.consent.api.CmsTppInfo;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class Xs2aPisConsentMapper {

    public PisConsentRequest mapToCmsPisConsentRequestForSinglePayment(CreatePisConsentData createPisConsentData, String paymentId) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(createPisConsentData.getSinglePayment(), paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getAspspConsentData)
                .orElse(null));

        return request;
    }

    public PisConsentRequest mapToCmsPisConsentRequestForPeriodicPayment(CreatePisConsentData createPisConsentData, String paymentId) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment(createPisConsentData.getPeriodicPayment(), paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.PERIODIC);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getAspspConsentData)
                .orElse(null));

        return request;
    }

    public PisConsentRequest mapToCmsPisConsentRequestForBulkPayment(CreatePisConsentData createPisConsentData) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(mapToPisPaymentForBulkPayment(createPisConsentData.getPaymentIdentifierMap()));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.BULK);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getAspspConsentData)
                .orElse(null));

        return request;

    }

    public Optional<Xsa2CreatePisConsentAuthorizationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(SpiCreatePisConsentAuthorizationResponse spi, PaymentType paymentType) {
        return Optional.ofNullable(spi)
                   .map(s -> new Xsa2CreatePisConsentAuthorizationResponse(s.getAuthorizationId(), SpiScaStatus.RECEIVED.name(), paymentType.getValue()));
    }

    private PisPayment mapToPisPaymentForSinglePayment(SinglePayment payment, String paymentId) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(Optional.ofNullable(paymentId).orElseGet(() -> UUID.randomUUID().toString()));
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getAmount())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                       pisPayment.setCreditorAgent(pmt.getCreditorAgent());
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(Xs2aPurposeCode::getCode)
                                                     .orElse(""));

                       return pisPayment;

                   }).orElse(null);
    }

    private PisPayment mapToPisPaymentForPeriodicPayment(PeriodicPayment payment, String paymentId) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(Optional.ofNullable(paymentId).orElseGet(() -> UUID.randomUUID().toString()));
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getAmount())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                       pisPayment.setCreditorAgent(pmt.getCreditorAgent());
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(Xs2aPurposeCode::getCode)
                                                     .orElse(""));
                       pisPayment.setStartDate(pmt.getStartDate());
                       pisPayment.setEndDate(pmt.getEndDate());
                       pisPayment.setExecutionRule(pmt.getExecutionRule());
                       pisPayment.setFrequency(pmt.getFrequency().name());
                       pisPayment.setDayOfExecution(pmt.getDayOfExecution());

                       return pisPayment;
                   }).orElse(null);
    }

    private List<PisPayment> mapToPisPaymentForBulkPayment(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap) {
        return paymentIdentifierMap.entrySet().stream()
                   .map(etr -> mapToPisPaymentForSinglePayment(etr.getKey(), etr.getValue().getPaymentId()))
                   .collect(Collectors.toList());
    }

    private CmsTppInfo mapToTppInfo(TppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tpp -> {
                       CmsTppInfo cmsTppInfo = new CmsTppInfo();

                       cmsTppInfo.setRegistrationNumber(tpp.getRegistrationNumber());
                       cmsTppInfo.setTppName(tpp.getTppName());
                       cmsTppInfo.setTppRole(tpp.getTppRole());
                       cmsTppInfo.setNationalCompetentAuthority(tpp.getNationalCompetentAuthority());
                       cmsTppInfo.setRedirectUri(tpp.getRedirectUri());
                       cmsTppInfo.setNokRedirectUri(tpp.getNokRedirectUri());
                       return cmsTppInfo;
                   }).orElse(null);
    }

    private CmsAccountReference mapToPisAccountReference(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
                   .map(ref -> new CmsAccountReference(
                       ref.getIban(),
                       ref.getBban(),
                       ref.getPan(),
                       ref.getMaskedPan(),
                       ref.getMsisdn(),
                       ref.getCurrency())
                   ).orElse(null);
    }

    private CmsAddress mapToCmsAddress(Xs2aAddress address) {
        return Optional.ofNullable(address)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreet());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getCity());
                       cmsAddress.setPostalCode(adr.getPostalCode());
                       cmsAddress.setCountry(Optional.ofNullable(adr.getCountry().getCode()).orElse(""));
                       return cmsAddress;
                   }).orElseGet(CmsAddress::new);
    }

    private CmsRemittance mapToCmsRemittance(Remittance remittance) {
        return Optional.ofNullable(remittance)
                   .map(rm -> {
                       CmsRemittance cmsRemittance = new CmsRemittance();
                       cmsRemittance.setReference(rm.getReference());
                       cmsRemittance.setReferenceIssuer(rm.getReferenceIssuer());
                       cmsRemittance.setReferenceType(rm.getReferenceType());
                       return cmsRemittance;
                   })
                   .orElseGet(CmsRemittance::new);
    }
}
