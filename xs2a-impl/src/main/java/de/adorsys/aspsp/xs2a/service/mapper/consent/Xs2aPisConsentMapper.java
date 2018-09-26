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

import de.adorsys.aspsp.xs2a.consent.api.*;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentProduct;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiRemittance;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
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
public class Xs2aPisConsentMapper {

    public PisConsentRequest mapToCmsPisConsentRequestForSinglePayment(CreatePisConsentData createPisConsentData) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(createPisConsentData.getSinglePayment())));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getBody)
                .orElse(null));

        return request;
    }

    public PisConsentRequest mapToCmsPisConsentRequestForPeriodicPayment(CreatePisConsentData createPisConsentData) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForPeriodicPayment(createPisConsentData.getPeriodicPayment())));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.PERIODIC);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getBody)
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
                .map(AspspConsentData::getBody)
                .orElse(null));

        return request;

    }

    public Optional<Xsa2CreatePisConsentAuthorisationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.ofNullable(response)
                   .map(s -> new Xsa2CreatePisConsentAuthorisationResponse(s.getAuthorizationId(), SpiScaStatus.RECEIVED.name(), paymentType.getValue()));
    }

    private PisPayment mapToPisPaymentForSinglePayment(SinglePayment payment) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(pmt.getPaymentId());
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

    private PisPayment mapToPisPaymentForPeriodicPayment(PeriodicPayment payment) {
        return Optional.ofNullable(payment)
                   .map(pmt -> {
                       PisPayment pisPayment = new PisPayment();

                       pisPayment.setPaymentId(pmt.getPaymentId());
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
                   .map(etr -> mapToPisPaymentForSinglePayment(etr.getKey()))
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

    private CmsAccountReference mapToPisAccountReference(Xs2aAccountReference xs2aAccountReference) {
        return Optional.ofNullable(xs2aAccountReference)
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
                       cmsAddress.setCountry(Optional.ofNullable(adr.getCountry()).map(Xs2aCountryCode::getCode).orElse(null));
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

    public Optional<Xs2aUpdatePisConsentPsuDataResponse> mapToXs2aUpdatePisConsentPsuDataResponse(UpdatePisConsentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> new Xs2aUpdatePisConsentPsuDataResponse(getScaStatus(response), response.getAvailableScaMethods()));
    }

    private String getScaStatus(UpdatePisConsentPsuDataResponse response) {
        return Optional.ofNullable(response.getScaStatus())
                   .map(Enum::name)
                   .orElse(null);
    }

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

    public SpiPaymentConfirmation buildSpiPaymentConfirmation(UpdatePisConsentPsuDataRequest request, String consentId) {
        SpiPaymentConfirmation paymentConfirmation = new SpiPaymentConfirmation();
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setPaymentId(request.getPaymentId());
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

    private CmsScaMethod mapToCmsScaMethod(SpiScaMethod spiScaMethod) {
        return CmsScaMethod.valueOf(spiScaMethod.name());
    }
}
