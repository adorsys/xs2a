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

import de.adorsys.aspsp.xs2a.consent.api.CmsAccountReference;
import de.adorsys.aspsp.xs2a.consent.api.CmsAddress;
import de.adorsys.aspsp.xs2a.consent.api.CmsRemittance;
import de.adorsys.aspsp.xs2a.consent.api.CmsTppInfo;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PisConsentMapper {

    public PisConsentRequest mapToPisConsentRequestForPeriodicPayment(PeriodicPayment periodicPayment, String paymentId, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment(periodicPayment, paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentType(PisPaymentType.PERIODIC);
        request.setTppInfo(mapToTppInfo(tppInfo));

        return request;
    }

    public PisConsentRequest mapToPisConsentRequestForBulkPayment(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(mapToPisPaymentForBulkPayment(paymentIdentifierMap));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentType(PisPaymentType.BULK);
        request.setTppInfo(mapToTppInfo(tppInfo));

        return request;
    }

    private List<PisPayment> mapToPisPaymentForBulkPayment(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap) {
        return paymentIdentifierMap.entrySet().stream()
                   .map(etr -> mapToPisPaymentForSinglePayment(etr.getKey(), etr.getValue().getPaymentId()))
                   .collect(Collectors.toList());

    }

    public PisConsentRequest mapToPisConsentRequestForSinglePayment(SinglePayment singlePayment, String paymentId, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(singlePayment, paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(mapToTppInfo(tppInfo));

        return request;
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

    private PisPayment mapToPisPaymentForSinglePayment(SinglePayment payment, String paymentId) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(paymentId);
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());

                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getContent())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                       pisPayment.setCreditorAgent(Optional.ofNullable(pmt.getCreditorAgent())
                                                       .map(BICFI::getCode).orElse(""));
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(PurposeCode::getCode).orElse(""));

                       return pisPayment;

                   }).orElse(null);
    }

    private PisPayment mapToPisPaymentForPeriodicPayment(PeriodicPayment payment, String paymentId) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(paymentId);
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getContent())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                       pisPayment.setCreditorAgent(Optional.ofNullable(pmt.getCreditorAgent())
                                                       .map(BICFI::getCode).orElse(""));
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(PurposeCode::getCode).orElse(""));
                       pisPayment.setStartDate(pmt.getStartDate());
                       pisPayment.setEndDate(pmt.getEndDate());
                       pisPayment.setExecutionRule(pmt.getExecutionRule());
                       pisPayment.setFrequency(pmt.getFrequency().name());
                       pisPayment.setDayOfExecution(pmt.getDayOfExecution());

                       return pisPayment;
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

    private CmsAddress mapToCmsAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreet());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getCity());
                       cmsAddress.setPostalCode(adr.getPostalCode());
                       cmsAddress.setCountry(Optional.ofNullable(adr.getCountry().getCode()).orElse(""));
                       return cmsAddress;
                   }).orElse(new CmsAddress());
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
                   .orElse(new CmsRemittance());
    }
}
