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

package de.adorsys.aspsp.xs2a.spi.impl.mapper;

import de.adorsys.aspsp.xs2a.consent.api.CmsAccountReference;
import de.adorsys.aspsp.xs2a.consent.api.CmsAddress;
import de.adorsys.aspsp.xs2a.consent.api.CmsRemittance;
import de.adorsys.aspsp.xs2a.consent.api.CmsTppInfo;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTppInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiPisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiRemittance;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SpiPisConsentMapper {

    public PisConsentRequest mapToCmsPisConsentRequestForSinglePayment(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(spiPisConsentRequest.getPayments().get(0))));
        request.setPaymentProduct(PisPaymentProduct.getByCode(spiPisConsentRequest.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(mapToTppInfo(spiPisConsentRequest.getTppInfo()));
        request.setAspspConsentData(Optional.ofNullable(spiPisConsentRequest.getAspspConsentData())
                                        .map(AspspConsentData::getAspspConsentData)
                                        .orElse(null));

        return request;
    }

    public PisConsentRequest mapToCmsPisConsentRequestForPeriodicPayment(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment((SpiPeriodicPayment)spiPisConsentRequest.getPayments().get(0))));
        request.setPaymentProduct(PisPaymentProduct.getByCode(spiPisConsentRequest.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.PERIODIC);
        request.setTppInfo(mapToTppInfo(spiPisConsentRequest.getTppInfo()));
        request.setAspspConsentData(Optional.ofNullable(spiPisConsentRequest.getAspspConsentData())
                                        .map(AspspConsentData::getAspspConsentData)
                                        .orElse(null));

        return request;
    }

    public PisConsentRequest mapToCmsPisConsentRequestForBulkPayment(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(mapToPisPaymentForBulkPayment(spiPisConsentRequest.getPayments()));
        request.setPaymentProduct(PisPaymentProduct.getByCode(spiPisConsentRequest.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.BULK);
        request.setTppInfo(mapToTppInfo(spiPisConsentRequest.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(spiPisConsentRequest.getAspspConsentData())
                .map(AspspConsentData::getAspspConsentData)
                .orElse(null));

        return request;
    }

    private PisPayment mapToPisPaymentForSinglePayment(SpiSinglePayment payment) {
        return Optional.ofNullable(payment)
                .map(pmt -> {
                    PisPayment pisPayment = new PisPayment();
                    pisPayment.setPaymentId(pmt.getPaymentId());
                    pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                    pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                    pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());

                    pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                    pisPayment.setAmount(pmt.getInstructedAmount().getContent());
                    pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                    pisPayment.setCreditorAgent(Optional.ofNullable(pmt.getCreditorAgent())
                                                    .orElse(""));
                    pisPayment.setCreditorName(pmt.getCreditorName());
                    pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                    pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                    pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                    pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                    pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                    pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                    pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                  .orElse(""));

                    return pisPayment;
                }).orElse(null);
    }

    private PisPayment mapToPisPaymentForPeriodicPayment(SpiPeriodicPayment payment) {
        return Optional.ofNullable(payment)
                .map(pmt -> {
                    PisPayment pisPayment = new PisPayment();

                    pisPayment.setPaymentId(pmt.getPaymentId());
                    pisPayment.setEndToEndIdentification(pmt.getEndToEndIdentification());
                    pisPayment.setDebtorAccount(mapToPisAccountReference(pmt.getDebtorAccount()));
                    pisPayment.setUltimateDebtor(pmt.getUltimateDebtor());
                    pisPayment.setCurrency(pmt.getInstructedAmount().getCurrency());
                    pisPayment.setAmount(pmt.getInstructedAmount().getContent());
                    pisPayment.setCreditorAccount(mapToPisAccountReference(pmt.getCreditorAccount()));
                    pisPayment.setCreditorAgent(Optional.ofNullable(pmt.getCreditorAgent())
                                                    .orElse(""));
                    pisPayment.setCreditorName(pmt.getCreditorName());
                    pisPayment.setCreditorAddress(mapToCmsAddress(pmt.getCreditorAddress()));
                    pisPayment.setRemittanceInformationUnstructured(pmt.getRemittanceInformationUnstructured());
                    pisPayment.setRemittanceInformationStructured(mapToCmsRemittance(pmt.getRemittanceInformationStructured()));
                    pisPayment.setRequestedExecutionDate(pmt.getRequestedExecutionDate());
                    pisPayment.setRequestedExecutionTime(pmt.getRequestedExecutionTime());
                    pisPayment.setUltimateCreditor(pmt.getUltimateCreditor());
                    pisPayment.setPurposeCode(Optional.ofNullable(pmt.getPurposeCode())
                                                  .orElse(""));
                    pisPayment.setStartDate(pmt.getStartDate());
                    pisPayment.setEndDate(pmt.getEndDate());
                    pisPayment.setExecutionRule(pmt.getExecutionRule());
                    pisPayment.setFrequency(pmt.getFrequency());
                    pisPayment.setDayOfExecution(pmt.getDayOfExecution());

                    return pisPayment;
                }).orElse(null);

    }

    private List<PisPayment> mapToPisPaymentForBulkPayment(List<SpiSinglePayment> payment) {
        return payment.stream()
                   .map(this::mapToPisPaymentForSinglePayment)
                   .collect(Collectors.toList());
    }

    private CmsTppInfo mapToTppInfo(SpiTppInfo tppInfo) {
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

    private CmsAccountReference mapToPisAccountReference(SpiAccountReference accountReference) {
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

    private CmsAddress mapToCmsAddress(SpiAddress address) {
        return Optional.ofNullable(address)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreet());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getCity());
                       cmsAddress.setPostalCode(adr.getPostalCode());
                       cmsAddress.setCountry(Optional.ofNullable(adr.getCountry()).orElse(""));
                       return cmsAddress;
                   }).orElseGet(CmsAddress::new);
    }

    private CmsRemittance mapToCmsRemittance(SpiRemittance remittance) {
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
