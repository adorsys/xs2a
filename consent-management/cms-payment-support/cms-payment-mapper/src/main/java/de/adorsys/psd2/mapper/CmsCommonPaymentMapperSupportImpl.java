/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.core.payment.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmsCommonPaymentMapperSupportImpl implements CmsCommonPaymentMapper {
    private final Xs2aObjectMapper xs2aObjectMapper;

    @Override
    public CmsBasePaymentResponse mapToCmsSinglePayment(CmsCommonPayment cmsCommonPayment) {
        PaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), PaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }
        return mapToCmsSinglePayment(payment, cmsCommonPayment);
    }

    @Override
    public CmsBasePaymentResponse mapToCmsBulkPayment(CmsCommonPayment cmsCommonPayment) {
        BulkPaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), BulkPaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }
        return mapToCmsBulkPayment(payment, cmsCommonPayment);
    }

    @Override
    public CmsBasePaymentResponse mapToCmsPeriodicPayment(CmsCommonPayment cmsCommonPayment) {
        PeriodicPaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), PeriodicPaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }

        return mapToCmsPeriodicPayment(payment, cmsCommonPayment);
    }

    private CmsPeriodicPayment mapToCmsPeriodicPayment(PeriodicPaymentInitiationJson periodicPaymentInitiationJson, CmsCommonPayment cmsCommonPayment) {

        CmsPeriodicPayment periodicPayment = new CmsPeriodicPayment(cmsCommonPayment.getPaymentProduct());
        fillBasePaymentFields(periodicPayment, cmsCommonPayment);
        periodicPayment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        periodicPayment.setEndToEndIdentification(periodicPaymentInitiationJson.getEndToEndIdentification());
        periodicPayment.setInstructionIdentification(periodicPaymentInitiationJson.getInstructionIdentification());
        periodicPayment.setDebtorAccount(mapToAccountReference(periodicPaymentInitiationJson.getDebtorAccount()));
        periodicPayment.setDebtorName(periodicPaymentInitiationJson.getDebtorName());
        Xs2aAmount instructedAmount = periodicPaymentInitiationJson.getInstructedAmount();
        periodicPayment.setInstructedAmount(new CmsAmount(mapToCurrency(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        periodicPayment.setCreditorAccount(mapToAccountReference(periodicPaymentInitiationJson.getCreditorAccount()));
        periodicPayment.setCreditorAgent(periodicPaymentInitiationJson.getCreditorAgent());
        periodicPayment.setCreditorName(periodicPaymentInitiationJson.getCreditorName());
        periodicPayment.setCreditorAddress(mapToCmsAddress(periodicPaymentInitiationJson.getCreditorAddress()));
        periodicPayment.setRemittanceInformationUnstructured(periodicPaymentInitiationJson.getRemittanceInformationUnstructured());
        periodicPayment.setDayOfExecution(mapToPisDayOfExecution(periodicPaymentInitiationJson.getDayOfExecution()));
        periodicPayment.setStartDate(periodicPaymentInitiationJson.getStartDate());
        periodicPayment.setEndDate(periodicPaymentInitiationJson.getEndDate());
        periodicPayment.setExecutionRule(mapToPisExecutionRule(periodicPaymentInitiationJson.getExecutionRule()).orElse(null));
        periodicPayment.setFrequency(mapToFrequencyCode(periodicPaymentInitiationJson.getFrequency()));
        periodicPayment.setUltimateDebtor(periodicPaymentInitiationJson.getUltimateDebtor());
        periodicPayment.setUltimateCreditor(periodicPaymentInitiationJson.getUltimateCreditor());
        periodicPayment.setPurposeCode(mapToPurposeCode(periodicPaymentInitiationJson.getPurposeCode()));
        periodicPayment.setRemittanceInformationStructured(mapToCmsRemittance(periodicPaymentInitiationJson.getRemittanceInformationStructured()));
        periodicPayment.setTppBrandLoggingInformation(cmsCommonPayment.getTppBrandLoggingInformation());
        periodicPayment.setRemittanceInformationStructuredArray(mapToCmsRemittanceList(periodicPaymentInitiationJson.getRemittanceInformationStructuredArray()));

        return periodicPayment;
    }

    private CmsBulkPayment mapToCmsBulkPayment(BulkPaymentInitiationJson bulkPaymentInitiationJson, CmsCommonPayment cmsCommonPayment) {

        CmsBulkPayment bulkPayment = new CmsBulkPayment();
        fillBasePaymentFields(bulkPayment, cmsCommonPayment);
        bulkPayment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        bulkPayment.setBatchBookingPreferred(bulkPaymentInitiationJson.getBatchBookingPreferred());
        bulkPayment.setDebtorAccount(mapToAccountReference(bulkPaymentInitiationJson.getDebtorAccount()));
        bulkPayment.setDebtorName(bulkPaymentInitiationJson.getDebtorName());
        bulkPayment.setBatchBookingPreferred(bulkPaymentInitiationJson.getBatchBookingPreferred());
        bulkPayment.setRequestedExecutionDate(bulkPaymentInitiationJson.getRequestedExecutionDate());
        List<CmsSinglePayment> payments = bulkPaymentInitiationJson.getPayments().stream()
                                              .map(p -> mapToCmsSinglePayment(p, cmsCommonPayment))
                                              .collect(Collectors.toList());
        bulkPayment.setPayments(payments);
        bulkPayment.setTppBrandLoggingInformation(cmsCommonPayment.getTppBrandLoggingInformation());

        return bulkPayment;
    }

    private CmsSinglePayment mapToCmsSinglePayment(PaymentInitiationJson paymentInitiationJson, CmsCommonPayment cmsCommonPayment) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(cmsCommonPayment.getPaymentProduct());
        fillBasePaymentFields(singlePayment, cmsCommonPayment);
        singlePayment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        singlePayment.setInstructionIdentification(paymentInitiationJson.getInstructionIdentification());
        singlePayment.setDebtorAccount(mapToAccountReference(paymentInitiationJson.getDebtorAccount()));
        singlePayment.setDebtorName(paymentInitiationJson.getDebtorName());
        Xs2aAmount instructedAmount = paymentInitiationJson.getInstructedAmount();
        singlePayment.setInstructedAmount(new CmsAmount(mapToCurrency(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        singlePayment.setCreditorAccount(mapToAccountReference(paymentInitiationJson.getCreditorAccount()));
        singlePayment.setCreditorAgent(paymentInitiationJson.getCreditorAgent());
        singlePayment.setCreditorName(paymentInitiationJson.getCreditorName());
        singlePayment.setCreditorAddress(mapToCmsAddress(paymentInitiationJson.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(paymentInitiationJson.getRemittanceInformationUnstructured());
        singlePayment.setRequestedExecutionDate(paymentInitiationJson.getRequestedExecutionDate());
        singlePayment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        singlePayment.setUltimateDebtor(paymentInitiationJson.getUltimateDebtor());
        singlePayment.setUltimateCreditor(paymentInitiationJson.getUltimateCreditor());
        singlePayment.setPurposeCode(mapToPurposeCode(paymentInitiationJson.getPurposeCode()));
        singlePayment.setRemittanceInformationStructured(mapToCmsRemittance(paymentInitiationJson.getRemittanceInformationStructured()));
        singlePayment.setTppBrandLoggingInformation(cmsCommonPayment.getTppBrandLoggingInformation());
        singlePayment.setRemittanceInformationStructuredArray(mapToCmsRemittanceList(paymentInitiationJson.getRemittanceInformationStructuredArray()));
        singlePayment.setChargeBearer(Optional.ofNullable(paymentInitiationJson.getChargeBearer())
                                          .map(ChargeBearer::toString)
                                          .orElse(null));
        return singlePayment;
    }

    private <T> T convert(byte[] paymentData, Class<T> tClass) {
        try {
            return paymentData != null ? xs2aObjectMapper.readValue(paymentData, tClass) : null;
        } catch (IOException e) {
            log.warn("Can't convert byte[] to Object {}", e.getMessage());
            return null;
        }
    }

    private void fillBasePaymentFields(BaseCmsPayment payment, CmsCommonPayment cmsCommonPayment) {
        payment.setPaymentProduct(cmsCommonPayment.getPaymentProduct());
        payment.setPaymentId(cmsCommonPayment.getPaymentId());
        payment.setTppInfo(cmsCommonPayment.getTppInfo());
        payment.setPsuIdDatas(cmsCommonPayment.getPsuIdDatas());
        payment.setCreationTimestamp(cmsCommonPayment.getCreationTimestamp());
        payment.setStatusChangeTimestamp(cmsCommonPayment.getStatusChangeTimestamp());
    }

    private CmsRemittance mapToCmsRemittance(RemittanceInformationStructured remittanceInformationStructured) {
        if (remittanceInformationStructured == null) {
            return null;
        }

        CmsRemittance cmsRemittance = new CmsRemittance();
        cmsRemittance.setReference(remittanceInformationStructured.getReference());
        cmsRemittance.setReferenceType(remittanceInformationStructured.getReferenceType());
        cmsRemittance.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());

        return cmsRemittance;
    }

    private List<CmsRemittance> mapToCmsRemittanceList(List<RemittanceInformationStructured> remittanceInformationStructuredArray) {
        if (CollectionUtils.isEmpty(remittanceInformationStructuredArray)) {
            return Collections.emptyList();
        }

        return remittanceInformationStructuredArray.stream()
                   .map(this::mapToCmsRemittance)
                   .collect(Collectors.toList());
    }

    private CmsAddress mapToCmsAddress(Address pisAddress) {
        return Optional.ofNullable(pisAddress)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreetName(adr.getStreetName());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setTownName(adr.getTownName());
                       cmsAddress.setPostCode(adr.getPostCode());
                       cmsAddress.setCountry(adr.getCountry());
                       return cmsAddress;
                   }).orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.profile.AccountReference mapToAccountReference(AccountReference pisAccountReference) {
        return Optional.ofNullable(pisAccountReference)
                   .map(ref -> new de.adorsys.psd2.xs2a.core.profile.AccountReference(null,
                                                                                      null,
                                                                                      ref.getIban(),
                                                                                      ref.getBban(),
                                                                                      ref.getPan(),
                                                                                      ref.getMaskedPan(),
                                                                                      ref.getMsisdn(),
                                                                                      mapToCurrency(ref.getCurrency()),
                                                                                      null)
                   ).orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.pis.FrequencyCode mapToFrequencyCode(FrequencyCode frequency) {
        return de.adorsys.psd2.xs2a.core.pis.FrequencyCode.valueOf(frequency.name());
    }

    private PisDayOfExecution mapToPisDayOfExecution(DayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution).map(DayOfExecution::toString).map(PisDayOfExecution::fromValue).orElse(null);
    }

    private Optional<PisExecutionRule> mapToPisExecutionRule(ExecutionRule executionRule) {
        return Optional.ofNullable(executionRule)
                   .map(ExecutionRule::toString)
                   .flatMap(PisExecutionRule::getByValue);
    }

    private String mapToPurposeCode(PurposeCode purposeCode) {
        return Optional.ofNullable(purposeCode).map(PurposeCode::toString).orElse(null);
    }

    private Currency mapToCurrency(String currency) {
        return Optional.ofNullable(currency).map(Currency::getInstance).orElse(null);
    }
}
