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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.payment.create.PisPaymentInfoCreationObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class Xs2aToCmsPisCommonPaymentRequestMapper {
    private final Xs2aRemittanceMapper xs2aRemittanceMapper;

    public PisPaymentInfo mapToPisPaymentInfo(PisPaymentInfoCreationObject creationObject) {
        PaymentInitiationParameters paymentInitiationParameters = creationObject.getPaymentInitiationParameters();
        PaymentInitiationResponse response = creationObject.getResponse();

        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentProduct(paymentInitiationParameters.getPaymentProduct());
        paymentInfo.setPaymentType(paymentInitiationParameters.getPaymentType());
        paymentInfo.setTransactionStatus(response.getTransactionStatus());
        paymentInfo.setTppInfo(creationObject.getTppInfo());
        paymentInfo.setPaymentId(response.getPaymentId());
        paymentInfo.setPsuDataList(Collections.singletonList(paymentInitiationParameters.getPsuData()));
        paymentInfo.setMultilevelScaRequired(response.isMultilevelScaRequired());
        paymentInfo.setAspspAccountId(response.getAspspAccountId());
        paymentInfo.setTppRedirectUri(paymentInitiationParameters.getTppRedirectUri());
        paymentInfo.setInternalRequestId(creationObject.getInternalRequestId());
        paymentInfo.setPaymentData(creationObject.getPaymentData());
        paymentInfo.setCreationTimestamp(creationObject.getCreationTimestamp());
        paymentInfo.setTppNotificationUri(Optional.ofNullable(paymentInitiationParameters.getTppNotificationData()).map(TppNotificationData::getTppNotificationUri).orElse(null));
        paymentInfo.setNotificationSupportedModes(Optional.ofNullable(paymentInitiationParameters.getTppNotificationData()).map(TppNotificationData::getNotificationModes).orElse(null));
        paymentInfo.setContentType(creationObject.getContentType());

        return paymentInfo;
    }

    public PisCommonPaymentRequest mapToCmsSinglePisCommonPaymentRequest(SinglePayment singlePayment, String paymentProduct) {
        PisCommonPaymentRequest request = new PisCommonPaymentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(singlePayment)));
        request.setPaymentProduct(paymentProduct);
        // TODO put real tppInfo data https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/406
        request.setTppInfo(new TppInfo());
        return request;
    }

    public PisCommonPaymentRequest mapToCmsPeriodicPisCommonPaymentRequest(PeriodicPayment periodicPayment, String paymentProduct) {
        PisCommonPaymentRequest request = new PisCommonPaymentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment(periodicPayment)));
        request.setPaymentProduct(paymentProduct);
        // TODO put real tppInfo data https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/406
        request.setTppInfo(new TppInfo());
        return request;
    }

    public PisCommonPaymentRequest mapToCmsBulkPisCommonPaymentRequest(BulkPayment bulkPayment, String paymentProduct) {
        PisCommonPaymentRequest request = new PisCommonPaymentRequest();
        request.setPaymentId(bulkPayment.getPaymentId());
        request.setPayments(mapToListPisPayment(bulkPayment.getPayments(), bulkPayment.getBatchBookingPreferred()));
        request.setPaymentProduct(paymentProduct);
        request.setPaymentType(PaymentType.BULK);
        // TODO put real tppInfo data https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/406
        request.setTppInfo(new TppInfo());
        return request;

    }

    private List<PisPayment> mapToListPisPayment(List<SinglePayment> payments, Boolean batchBookingPreferred) {
        return payments.stream()
                   .map(this::mapToPisPaymentForSinglePayment)
                   .map(pisPayment -> updateBatchBookingPreferred(pisPayment, batchBookingPreferred))
                   .collect(Collectors.toList());
    }

    private PisPayment updateBatchBookingPreferred(PisPayment pisPayment, Boolean batchBookingPreferred) {
        pisPayment.setBatchBookingPreferred(batchBookingPreferred);
        return pisPayment;
    }

    private PisPayment mapToPisPaymentForSinglePayment(SinglePayment payment) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(pmt.getPaymentId());
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setInstructionIdentification(pmt.getInstructionIdentification());
                       pisPayment.setDebtorAccount(pmt.getDebtorAccount());
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getAmount())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(pmt.getCreditorAccount());
                       pisPayment.setCreditorAgent(pmt.getCreditorAgent());
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(xs2aRemittanceMapper.mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(PurposeCode::toString)
                                                     .orElse(""));

                       return pisPayment;

                   }).orElse(null);
    }

    private PisPayment mapToPisPaymentForPeriodicPayment(PeriodicPayment payment) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(pmt.getPaymentId());
                       pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                       pisPayment.setInstructionIdentification(pmt.getInstructionIdentification());
                       pisPayment.setDebtorAccount(pmt.getDebtorAccount());
                       pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                       pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                       pisPayment.setAmount(new BigDecimal(pmt.getInstructedAmount().getAmount())); // todo remake amount type from String to BigDecimal
                       pisPayment.setCreditorAccount(pmt.getCreditorAccount());
                       pisPayment.setCreditorAgent(pmt.getCreditorAgent());
                       pisPayment.setCreditorName(pmt.getCreditorName());
                       pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                       pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                       pisPayment.setRemittanceInformationStructured(xs2aRemittanceMapper.mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                       pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                       pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                       pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                       pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                     .map(PurposeCode::toString)
                                                     .orElse(""));
                       pisPayment.setStartDate(pmt.getStartDate());
                       pisPayment.setEndDate(pmt.getEndDate());
                       pisPayment.setExecutionRule(pmt.getExecutionRule());
                       pisPayment.setFrequency(pmt.getFrequency().name());
                       pisPayment.setDayOfExecution(pmt.getDayOfExecution());

                       return pisPayment;
                   }).orElse(null);
    }

    private CmsAddress mapToCmsAddress(Xs2aAddress address) {
        return Optional.ofNullable(address)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreetName());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getTownName());
                       cmsAddress.setPostalCode(adr.getPostCode());
                       cmsAddress.setCountry(Optional.ofNullable(adr.getCountry()).map(Xs2aCountryCode::getCode).orElse(null));
                       return cmsAddress;
                   }).orElse(null);
    }
}
