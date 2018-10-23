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

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.consent.api.CmsAccountReference;
import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated since 1.10. Will be removed in 1.11.
 * This class should not exist, since no dependencies between spi-api and consent-api are allowed.
 */
@Component
@AllArgsConstructor
@Deprecated
public class SpiCmsPisMapper {
    public SpiSinglePayment mapToSpiSinglePayment(PisPayment pisPayment) {
        SpiSinglePayment payment = new SpiSinglePayment();
        payment.setPaymentId(pisPayment.getPaymentId());
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        payment.setDebtorAccount(mapToSpiAccountReferenceFromCmsReference(pisPayment.getDebtorAccount()));
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());
        payment.setInstructedAmount(new SpiAmount(pisPayment.getCurrency(), pisPayment.getAmount()));
        payment.setCreditorAccount(mapToSpiAccountReferenceFromCmsReference(pisPayment.getCreditorAccount()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAddress(mapToSpiAddressFromCmsAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(mapToSpiRemittanceStructuredFromCmsRemittance(pisPayment.getRemittanceInformationStructured()));
        payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
        payment.setRequestedExecutionTime(pisPayment.getRequestedExecutionTime());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setPurposeCode(pisPayment.getPurposeCode());
        payment.setPaymentStatus(SpiTransactionStatus.ACCP);
        return payment;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(PisPayment pisPayment) {
        SpiPeriodicPayment payment = new SpiPeriodicPayment();
        payment.setPaymentId(pisPayment.getPaymentId());
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        payment.setDebtorAccount(mapToSpiAccountReferenceFromCmsReference(pisPayment.getDebtorAccount()));
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());
        payment.setInstructedAmount(new SpiAmount(pisPayment.getCurrency(), pisPayment.getAmount()));
        payment.setCreditorAccount(mapToSpiAccountReferenceFromCmsReference(pisPayment.getCreditorAccount()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAddress(mapToSpiAddressFromCmsAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(mapToSpiRemittanceStructuredFromCmsRemittance(pisPayment.getRemittanceInformationStructured()));
        payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
        payment.setRequestedExecutionTime(pisPayment.getRequestedExecutionTime());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setPurposeCode(pisPayment.getPurposeCode());
        payment.setPaymentStatus(SpiTransactionStatus.ACCP);
        payment.setStartDate(pisPayment.getStartDate());
        payment.setEndDate(pisPayment.getEndDate());
        payment.setExecutionRule(pisPayment.getExecutionRule());
        payment.setFrequency(pisPayment.getFrequency());
        payment.setDayOfExecution(pisPayment.getDayOfExecution());
        return payment;
    }

    public SpiBulkPayment mapToSpiBulkPayment(List<PisPayment> pisPayments) {
        SpiBulkPayment payment = new SpiBulkPayment();
        payment.setBatchBookingPreferred(false);
        payment.setDebtorAccount(mapToSpiAccountReferenceFromCmsReference(pisPayments.get(0).getDebtorAccount()));
        payment.setRequestedExecutionDate(pisPayments.get(0).getRequestedExecutionDate());
        List<SpiSinglePayment> paymentList = pisPayments.stream()
                                                 .map(this::mapToSpiSinglePayment)
                                                 .collect(Collectors.toList());
        payment.setPayments(paymentList);
        return payment;
    }

    public SpiScaConfirmation buildSpiScaConfirmation(UpdatePisConsentPsuDataRequest request, String consentId) {
        SpiScaConfirmation paymentConfirmation = new SpiScaConfirmation();
        paymentConfirmation.setPaymentId(request.getPaymentId());
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(request.getPsuId());
        return paymentConfirmation;
    }

    private SpiRemittance mapToSpiRemittanceStructuredFromCmsRemittance(CmsRemittance remittanceInformationStructured) {
        SpiRemittance remittance = new SpiRemittance();
        remittance.setReference(remittanceInformationStructured.getReference());
        remittance.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());
        remittance.setReferenceType(remittanceInformationStructured.getReferenceType());
        return remittance;
    }

    private SpiAddress mapToSpiAddressFromCmsAddress(CmsAddress address) {
        return new SpiAddress(address.getStreet(), address.getBuildingNumber(), address.getCity(), address.getPostalCode(), address.getCountry());
    }

    private SpiAccountReference mapToSpiAccountReferenceFromCmsReference(CmsAccountReference reference) {
        return new SpiAccountReference(reference.getIban(), reference.getBban(), reference.getPan(), reference.getMaskedPan(), reference.getMsisdn(), reference.getCurrency());
    }

    public List<CmsScaMethod> mapToCmsScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.stream()
                   .map(this::mapToCmsScaMethod)
                   .collect(Collectors.toList());
    }

    private CmsScaMethod mapToCmsScaMethod(@NotNull SpiScaMethod spiScaMethod) {
        return CmsScaMethod.valueOf(spiScaMethod.name());
    }
}
