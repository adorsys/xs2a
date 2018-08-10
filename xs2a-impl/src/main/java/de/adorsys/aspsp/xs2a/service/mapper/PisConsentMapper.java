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
import de.adorsys.aspsp.xs2a.consent.api.TppInfo;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentService;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.Remittance;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
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

    public PisConsentRequest mapToPisConsentRequestForPeriodicPayment(PeriodicPayment periodicPayment, String paymentId, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment(periodicPayment, paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentService(PisPaymentService.PERIODIC);
        request.setTppInfo(mapToTppInfo());

        return request;
    }

    public PisConsentRequest mapToPisConsentRequestForBulkPayment(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(mapToPisPaymentForBulkPayment(paymentIdentifierMap));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentService(PisPaymentService.BULK);
        request.setTppInfo(mapToTppInfo());

        return request;
    }

    private List<PisPayment> mapToPisPaymentForBulkPayment(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap) {
        return paymentIdentifierMap.entrySet().stream()
                   .map(etr -> mapToPisPaymentForSinglePayment(etr.getKey(), etr.getValue().getPaymentId()))
                   .collect(Collectors.toList());

    }

    public PisConsentRequest mapToPisConsentRequestForSinglePayment(SinglePayment singlePayment, String paymentId, String paymentProduct) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(singlePayment, paymentId)));
        request.setPaymentProduct(PisPaymentProduct.getByCode(paymentProduct).orElse(null));
        request.setPaymentService(PisPaymentService.SINGLE);
        request.setTppInfo(mapToTppInfo());

        return request;
    }

    private TppInfo mapToTppInfo() {
        return null;

    }

    private PisPayment mapToPisPaymentForSinglePayment(SinglePayment payment, String paymentId) {
        PisPayment pisPayment = new PisPayment();

        pisPayment.setPaymentId(paymentId);
        pisPayment.setEndToEndIdentification(payment.getEndToEndIdentification());
        pisPayment.setDebtorAccount(mapToPisAccountReference(payment.getDebtorAccount()));
        pisPayment.setUltimateDebtor(payment.getUltimateDebtor());

        pisPayment.setCurrency(payment.getInstructedAmount().getCurrency());
        pisPayment.setAmount(new BigDecimal(payment.getInstructedAmount().getContent())); // todo remake amount type from String to BigDecimal
        pisPayment.setCreditorAccount(mapToPisAccountReference(payment.getCreditorAccount()));
        pisPayment.setCreditorAgent(Optional.ofNullable(payment.getCreditorAgent())
                                        .map(BICFI::getCode).orElse(""));
        pisPayment.setCreditorName(payment.getCreditorName());
        pisPayment.setCreditorAddress(mapToCmsAddress(payment.getCreditorAddress()));
        pisPayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(payment.getRemittanceInformationStructured()));
        pisPayment.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        pisPayment.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        pisPayment.setUltimateCreditor(payment.getUltimateCreditor());
        pisPayment.setPurposeCode(Optional.ofNullable(payment.getPurposeCode())
                                      .map(PurposeCode::getCode).orElse(""));

        return pisPayment;
    }

    private PisPayment mapToPisPaymentForPeriodicPayment(PeriodicPayment payment, String paymentId) {
        PisPayment pisPayment = new PisPayment();

        pisPayment.setPaymentId(paymentId);
        pisPayment.setEndToEndIdentification(payment.getEndToEndIdentification());
        pisPayment.setDebtorAccount(mapToPisAccountReference(payment.getDebtorAccount()));
        pisPayment.setUltimateDebtor(payment.getUltimateDebtor());
        pisPayment.setCurrency(payment.getInstructedAmount().getCurrency());
        pisPayment.setAmount(new BigDecimal(payment.getInstructedAmount().getContent())); // todo remake amount type from String to BigDecimal
        pisPayment.setCreditorAccount(mapToPisAccountReference(payment.getCreditorAccount()));
        pisPayment.setCreditorAgent(Optional.ofNullable(payment.getCreditorAgent())
                                        .map(BICFI::getCode).orElse(""));
        pisPayment.setCreditorName(payment.getCreditorName());
        pisPayment.setCreditorAddress(mapToCmsAddress(payment.getCreditorAddress()));
        pisPayment.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(payment.getRemittanceInformationStructured()));
        pisPayment.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        pisPayment.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        pisPayment.setUltimateCreditor(payment.getUltimateCreditor());
        pisPayment.setPurposeCode(Optional.ofNullable(payment.getPurposeCode())
                                      .map(PurposeCode::getCode).orElse(""));
        pisPayment.setStartDate(payment.getStartDate());
        pisPayment.setEndDate(payment.getEndDate());
        pisPayment.setExecutionRule(payment.getExecutionRule());
        pisPayment.setFrequency(payment.getFrequency().name());
        pisPayment.setDayOfExecution(payment.getDayOfExecution());

        return pisPayment;
    }

    private CmsAccountReference mapToPisAccountReference(AccountReference accountReference) {
        return new CmsAccountReference(
            accountReference.getIban(),
            accountReference.getBban(),
            accountReference.getPan(),
            accountReference.getMaskedPan(),
            accountReference.getMsisdn(),
            accountReference.getCurrency());
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
