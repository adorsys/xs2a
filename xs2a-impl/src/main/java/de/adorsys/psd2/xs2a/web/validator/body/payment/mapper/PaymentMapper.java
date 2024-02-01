/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.body.payment.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.ChargeBearerMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final PurposeCodeMapper purposeCodeMapper;
    private final RemittanceMapper remittanceMapper;
    private final ChargeBearerMapper chargeBearerMapper;

    @Autowired
    public PaymentMapper(Xs2aObjectMapper xs2aObjectMapper, PurposeCodeMapper purposeCodeMapper, RemittanceMapper remittanceMapper, ChargeBearerMapper chargeBearerMapper) {
        this.xs2aObjectMapper = xs2aObjectMapper;
        this.purposeCodeMapper = purposeCodeMapper;
        this.remittanceMapper = remittanceMapper;
        this.chargeBearerMapper = chargeBearerMapper;
    }

    public SinglePayment mapToSinglePayment(Object body) {
        return mapToXs2aSinglePayment(convertPayment(body, PaymentInitiationJson.class));
    }

    public PeriodicPayment mapToPeriodicPayment(Object body) {
        return mapToXs2aPeriodicPayment(convertPayment(body, PeriodicPaymentInitiationJson.class));
    }

    public BulkPayment mapToBulkPayment(Object body) {
        return mapToXs2aBulkPayment(convertPayment(body, BulkPaymentInitiationJson.class));
    }

    private <R> R convertPayment(Object payment, Class<R> clazz) {
        return xs2aObjectMapper.convertValue(payment, clazz);
    }

    private SinglePayment mapToXs2aSinglePayment(PaymentInitiationJson paymentRequest) {
        SinglePayment payment = new SinglePayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorId(paymentRequest.getCreditorId());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationUnstructuredArray(paymentRequest.getRemittanceInformationUnstructuredArray());
        payment.setRemittanceInformationStructured(remittanceMapper.mapToRemittance(paymentRequest.getRemittanceInformationStructured()));
        payment.setRemittanceInformationStructuredArray(mapToRemittanceArray(paymentRequest.getRemittanceInformationStructuredArray()));
        payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        payment.setUltimateDebtor(paymentRequest.getUltimateDebtor());
        payment.setUltimateCreditor(paymentRequest.getUltimateCreditor());
        payment.setPurposeCode(purposeCodeMapper.mapToPurposeCode(paymentRequest.getPurposeCode()));
        payment.setInstructionIdentification(paymentRequest.getInstructionIdentification());
        payment.setDebtorName(paymentRequest.getDebtorName());
        payment.setChargeBearer(chargeBearerMapper.mapToChargeBearer(paymentRequest.getChargeBearer()));

        return payment;
    }

    private PeriodicPayment mapToXs2aPeriodicPayment(PeriodicPaymentInitiationJson paymentRequest) {
        PeriodicPayment payment = new PeriodicPayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorId(paymentRequest.getCreditorId());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));

        payment.setStartDate(paymentRequest.getStartDate());
        payment.setExecutionRule(mapToPisExecutionRule(paymentRequest.getExecutionRule()).orElse(null));
        payment.setEndDate(paymentRequest.getEndDate());
        payment.setFrequency(mapToFrequencyCode(paymentRequest.getFrequency()));
        payment.setDayOfExecution(mapToPisDayOfExecution(paymentRequest.getDayOfExecution()).orElse(null));
        payment.setMonthsOfExecution(mapToMonthsOfExecution(paymentRequest.getMonthsOfExecution()));
        payment.setUltimateDebtor(paymentRequest.getUltimateDebtor());
        payment.setUltimateCreditor(paymentRequest.getUltimateCreditor());
        payment.setPurposeCode(purposeCodeMapper.mapToPurposeCode(paymentRequest.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationUnstructuredArray(paymentRequest.getRemittanceInformationUnstructuredArray());
        payment.setRemittanceInformationStructured(remittanceMapper.mapToRemittance(paymentRequest.getRemittanceInformationStructured()));
        payment.setRemittanceInformationStructuredArray(mapToRemittanceArray(paymentRequest.getRemittanceInformationStructuredArray()));
        payment.setInstructionIdentification(paymentRequest.getInstructionIdentification());
        payment.setDebtorName(paymentRequest.getDebtorName());

        return payment;
    }

    private AccountReference mapToXs2aAccountReference(Object accountReference) {
        return xs2aObjectMapper.convertValue(accountReference, AccountReference.class);
    }

    private Optional<PisExecutionRule> mapToPisExecutionRule(ExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(ExecutionRule::toString)
                   .flatMap(PisExecutionRule::getByValue);
    }

    private List<String> mapToMonthsOfExecution(MonthsOfExecution monthsOfExecution) {
        return monthsOfExecution == null
                   ? null
                   : new ArrayList<>(monthsOfExecution);
    }

    private de.adorsys.psd2.xs2a.core.pis.FrequencyCode mapToFrequencyCode(FrequencyCode frequency) {
        return de.adorsys.psd2.xs2a.core.pis.FrequencyCode.valueOf(frequency.name());
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
                       targetAddress.setStreetName(a.getStreetName());
                       targetAddress.setBuildingNumber(a.getBuildingNumber());
                       targetAddress.setTownName(a.getTownName());
                       targetAddress.setPostCode(a.getPostCode());
                       targetAddress.setCountry(new Xs2aCountryCode(a.getCountry()));
                       return targetAddress;
                   })
                   .orElse(null);
    }

    private BulkPayment mapToXs2aBulkPayment(BulkPaymentInitiationJson paymentRequest) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setBatchBookingPreferred(paymentRequest.isBatchBookingPreferred());
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
                       payment.setRequestedExecutionTime(paymentRequest.getRequestedExecutionTime());
                       payment.setUltimateDebtor(p.getUltimateDebtor());
                       payment.setUltimateCreditor(p.getUltimateCreditor());
                       payment.setPurposeCode(purposeCodeMapper.mapToPurposeCode(p.getPurposeCode()));
                       payment.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       payment.setRemittanceInformationUnstructuredArray(p.getRemittanceInformationUnstructuredArray());
                       payment.setRemittanceInformationStructured(remittanceMapper.mapToRemittance(p.getRemittanceInformationStructured()));
                       payment.setRemittanceInformationStructuredArray(mapToRemittanceArray(p.getRemittanceInformationStructuredArray()));
                       payment.setInstructionIdentification(p.getInstructionIdentification());
                       payment.setDebtorName(p.getDebtorName());
                       payment.setChargeBearer(chargeBearerMapper.mapToChargeBearer(p.getChargeBearer()));

                       return payment;
                   })
                   .collect(Collectors.toList());
    }

    private List<Remittance> mapToRemittanceArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
        if (CollectionUtils.isEmpty(remittanceInformationStructuredArray)) {
            return null;
        }

        return remittanceInformationStructuredArray.stream()
                   .map(remittanceMapper::mapToRemittance)
                   .collect(Collectors.toList());
    }
}
