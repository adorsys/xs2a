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

package de.adorsys.psd2.xs2a.web.validator.body.payment.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public PaymentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SinglePayment getSinglePayment(Object body) {
        return mapToXs2aSinglePayment(convertPayment(body, PaymentInitiationJson.class));
    }

    public PeriodicPayment getPeriodicPayment(Object body) {
        return mapToXs2aPeriodicPayment(convertPayment(body, PeriodicPaymentInitiationJson.class));
    }

    public BulkPayment getBulkPayment(Object body) {
        return mapToXs2aBulkPayment(convertPayment(body, BulkPaymentInitiationJson.class));
    }

    private <R> R convertPayment(Object payment, Class<R> clazz) {
        return objectMapper.convertValue(payment, clazz);
    }

    private SinglePayment mapToXs2aSinglePayment(PaymentInitiationJson paymentRequest) {
        SinglePayment payment = new SinglePayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        return payment;
    }

    private PeriodicPayment mapToXs2aPeriodicPayment(PeriodicPaymentInitiationJson paymentRequest) {
        PeriodicPayment payment = new PeriodicPayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(LocalDate.now());
        payment.setRequestedExecutionTime(OffsetDateTime.now().plusHours(1));

        payment.setStartDate(paymentRequest.getStartDate());
        payment.setExecutionRule(mapToPisExecutionRule(paymentRequest.getExecutionRule()).orElse(null));
        payment.setEndDate(paymentRequest.getEndDate());
        payment.setFrequency(mapToXs2aFrequencyCode(paymentRequest.getFrequency()));
        payment.setDayOfExecution(mapToPisDayOfExecution(paymentRequest.getDayOfExecution()).orElse(null));
        return payment;
    }

    private de.adorsys.psd2.xs2a.core.profile.AccountReference mapToXs2aAccountReference(Object accountReference) { //NOPMD
        return objectMapper.convertValue(accountReference, AccountReference.class);
    }

    private Optional<PisExecutionRule> mapToPisExecutionRule(ExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(ExecutionRule::toString)
                   .flatMap(PisExecutionRule::getByValue);
    }

    private Xs2aFrequencyCode mapToXs2aFrequencyCode(FrequencyCode frequency) {
        return Xs2aFrequencyCode.valueOf(frequency.name());
    }

    private Optional<PisDayOfExecution> mapToPisDayOfExecution(DayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution)
                   .map(DayOfExecution::toString)
                   .flatMap(PisDayOfExecution::getByValue);
    }

    private Xs2aAmount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Xs2aAmount amountTarget = new Xs2aAmount();
                       amountTarget.setAmount(a.getAmount());
                       amountTarget.setCurrency(getCurrencyByCode(a.getCurrency()));
                       return amountTarget;
                   })
                   .orElse(null);
    }

    private Currency getCurrencyByCode(String code) {
        if (StringUtils.isNotEmpty(code)) {
            try {
                return Currency.getInstance(code);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }

    private Xs2aAddress mapToXs2aAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> {
                       Xs2aAddress targetAddress = new Xs2aAddress();
                       targetAddress.setStreet(a.getStreet());
                       targetAddress.setBuildingNumber(a.getBuildingNumber());
                       targetAddress.setCity(a.getCity());
                       targetAddress.setPostalCode(a.getPostalCode());
                       targetAddress.setCountry(new Xs2aCountryCode(a.getCountry()));
                       return targetAddress;
                   })
                   .orElseGet(Xs2aAddress::new);
    }

    private BulkPayment mapToXs2aBulkPayment(BulkPaymentInitiationJson paymentRequest) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setBatchBookingPreferred(paymentRequest.getBatchBookingPreferred());
        bulkPayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        bulkPayment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        bulkPayment.setRequestedExecutionTime(paymentRequest.getRequestedExecutionTime());
        bulkPayment.setPayments(mapBulkPaymentToSinglePayments(paymentRequest));
        return bulkPayment;
    }

    private List<SinglePayment> mapBulkPaymentToSinglePayments(BulkPaymentInitiationJson paymentRequest) {
        return paymentRequest.getPayments().stream()
                   .map(p -> {
                       SinglePayment payment = new SinglePayment();
                       payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
                       payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
                       payment.setEndToEndIdentification(p.getEndToEndIdentification());
                       payment.setInstructedAmount(mapToXs2aAmount(p.getInstructedAmount()));
                       payment.setCreditorAccount(mapToXs2aAccountReference(p.getCreditorAccount()));
                       payment.setCreditorAgent(p.getCreditorAgent());
                       payment.setCreditorName(p.getCreditorName());
                       payment.setCreditorAddress(mapToXs2aAddress(p.getCreditorAddress()));
                       payment.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       payment.setRequestedExecutionTime(paymentRequest.getRequestedExecutionTime());
                       return payment;
                   })
                   .collect(Collectors.toList());
    }
}
