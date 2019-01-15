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

package de.adorsys.aspsp.aspspmockserver.service.mapper;

import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentInfo;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPeriodicPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public AspspPayment mapToAspspPayment(AspspPaymentInfo paymentInfo) {
        AspspPayment aspspPayment = new AspspPayment();
        aspspPayment.setPaymentId(paymentInfo.getPaymentId());
        aspspPayment.setPaymentStatus(paymentInfo.getPaymentStatus());
        aspspPayment.setPaymentProduct(paymentInfo.getPaymentProduct());
        aspspPayment.setPisPaymentType(PisPaymentType.valueOf(paymentInfo.getPisPaymentType()));
        aspspPayment.setPaymentData(paymentInfo.getPaymentData());
        aspspPayment.setDebtorAccount(new AspspAccountReference(UUID.randomUUID().toString(), Currency.getInstance("EUR")));
        return aspspPayment;
    }

    public AspspPaymentInfo mapToAspspPaymentInfo(AspspPayment aspspPayment) {
        return new AspspPaymentInfo(aspspPayment.getPaymentId(),
                                    aspspPayment.getPaymentStatus(),
                                    aspspPayment.getPaymentProduct(),
                                    aspspPayment.getPisPaymentType().name(),
                                    aspspPayment.getPaymentData(),
                                    Optional.ofNullable(aspspPayment.getDebtorAccount())
                                        .map(AspspAccountReference::getAccountId)
                                        .orElse(null)
        );
    }

    public List<AspspPayment> mapToAspspPaymentList(List<AspspSinglePayment> payments, String bulkId) {
        return payments.stream()
                   .map(p -> mapToBulkAspspPayment(p, bulkId))
                   .collect(Collectors.toList());
    }

    private AspspPayment mapToBulkAspspPayment(AspspSinglePayment aspspSinglePayment, String bulkId) {
        AspspPayment aspspPayment = mapToAspspPayment(aspspSinglePayment, PisPaymentType.BULK);
        aspspPayment.setBulkId(bulkId);
        return aspspPayment;
    }

    public List<AspspSinglePayment> mapToAspspSinglePaymentList(List<AspspPayment> payments) {
        return payments.stream()
                   .map(this::mapToAspspSinglePayment)
                   .collect(Collectors.toList());
    }

    public AspspPayment mapToAspspPayment(AspspSinglePayment singlePayment, PisPaymentType paymentType) {
        return Optional.ofNullable(singlePayment)
                   .map(s -> buildAspspPayment(s, paymentType))
                   .orElse(null);
    }

    public AspspPayment mapToAspspPayment(AspspPeriodicPayment periodicPayment, PisPaymentType paymentType) {
        return Optional.ofNullable(periodicPayment)
                   .map(s -> {
                       AspspPayment aspsp = buildAspspPayment(periodicPayment, paymentType);
                       aspsp.setStartDate(periodicPayment.getStartDate());
                       aspsp.setEndDate(periodicPayment.getEndDate());
                       aspsp.setExecutionRule(periodicPayment.getExecutionRule());
                       aspsp.setFrequency(periodicPayment.getFrequency());
                       aspsp.setDayOfExecution(periodicPayment.getDayOfExecution());
                       return aspsp;
                   })
                   .orElse(null);
    }

    private AspspPayment buildAspspPayment(AspspSinglePayment single, PisPaymentType paymentType) {
        AspspPayment aspsp = new AspspPayment(paymentType);
        aspsp.setPaymentId(single.getPaymentId());
        aspsp.setEndToEndIdentification(single.getEndToEndIdentification());
        aspsp.setDebtorAccount(single.getDebtorAccount());
        aspsp.setUltimateDebtor(single.getUltimateDebtor());
        aspsp.setInstructedAmount(single.getInstructedAmount());
        aspsp.setCreditorAccount(single.getCreditorAccount());
        aspsp.setCreditorAgent(single.getCreditorAgent());
        aspsp.setCreditorName(single.getCreditorName());
        aspsp.setCreditorAddress(single.getCreditorAddress());
        aspsp.setUltimateCreditor(single.getUltimateCreditor());
        aspsp.setPurposeCode(single.getPurposeCode());
        aspsp.setRequestedExecutionDate(single.getRequestedExecutionDate());
        aspsp.setRequestedExecutionTime(single.getRequestedExecutionTime());
        aspsp.setPaymentStatus(single.getPaymentStatus());
        aspsp.setRemittanceInformationStructured(single.getRemittanceInformationStructured());
        aspsp.setRemittanceInformationUnstructured(single.getRemittanceInformationUnstructured());
        return aspsp;
    }

    public AspspSinglePayment mapToAspspSinglePayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(aspsp -> {
                       AspspSinglePayment single = new AspspSinglePayment();
                       single.setPaymentId(aspsp.getPaymentId());
                       single.setEndToEndIdentification(aspsp.getEndToEndIdentification());
                       single.setDebtorAccount(aspsp.getDebtorAccount());
                       single.setUltimateDebtor(aspsp.getUltimateDebtor());
                       single.setInstructedAmount(aspsp.getInstructedAmount());
                       single.setCreditorAccount(aspsp.getCreditorAccount());
                       single.setCreditorAgent(aspsp.getCreditorAgent());
                       single.setCreditorName(aspsp.getCreditorName());
                       aspsp.setCreditorAddress(single.getCreditorAddress());
                       single.setUltimateCreditor(aspsp.getUltimateCreditor());
                       single.setPurposeCode(aspsp.getPurposeCode());
                       single.setRequestedExecutionDate(aspsp.getRequestedExecutionDate());
                       single.setRequestedExecutionTime(aspsp.getRequestedExecutionTime());
                       single.setPaymentStatus(aspspPayment.getPaymentStatus());
                       single.setRemittanceInformationStructured(aspsp.getRemittanceInformationStructured());
                       single.setRemittanceInformationUnstructured(aspsp.getRemittanceInformationUnstructured());
                       return single;
                   })
                   .orElse(null);
    }

    public AspspPeriodicPayment mapToAspspPeriodicPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(aspsp -> {
                       AspspPeriodicPayment periodic = new AspspPeriodicPayment();
                       periodic.setPaymentId(aspsp.getPaymentId());
                       periodic.setEndToEndIdentification(aspsp.getEndToEndIdentification());
                       periodic.setDebtorAccount(aspsp.getDebtorAccount());
                       periodic.setUltimateDebtor(aspsp.getUltimateDebtor());
                       periodic.setInstructedAmount(aspsp.getInstructedAmount());
                       periodic.setCreditorAccount(aspsp.getCreditorAccount());
                       periodic.setCreditorAgent(aspsp.getCreditorAgent());
                       periodic.setCreditorName(aspsp.getCreditorName());
                       periodic.setUltimateCreditor(aspsp.getUltimateCreditor());
                       periodic.setPurposeCode(aspsp.getPurposeCode());
                       periodic.setRequestedExecutionDate(aspsp.getRequestedExecutionDate());
                       periodic.setRequestedExecutionTime(aspsp.getRequestedExecutionTime());
                       periodic.setStartDate(aspsp.getStartDate());
                       periodic.setEndDate(aspsp.getEndDate());
                       periodic.setExecutionRule(aspsp.getExecutionRule());
                       periodic.setFrequency(aspsp.getFrequency());
                       periodic.setDayOfExecution(aspsp.getDayOfExecution());
                       periodic.setPaymentStatus(aspspPayment.getPaymentStatus());
                       periodic.setRemittanceInformationStructured(aspsp.getRemittanceInformationStructured());
                       periodic.setRemittanceInformationUnstructured(aspsp.getRemittanceInformationUnstructured());
                       return periodic;
                   })
                   .orElse(null);
    }
}
