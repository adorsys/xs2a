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

import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTppRole;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.consent.api.CmsAccountReference;
import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.CmsRemittance;
import de.adorsys.psd2.consent.api.CmsTppInfo;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.PisPaymentProduct;
import de.adorsys.psd2.consent.api.pis.PisPaymentType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Xs2aPisConsentMapper {

    public PisConsentRequest mapToCmsPisConsentRequestForSinglePayment(CreatePisConsentData createPisConsentData) {
        PisConsentRequest request = new PisConsentRequest();
        request.setPayments(Collections.singletonList(mapToPisPaymentForSinglePayment(createPisConsentData.getSinglePayment())));
        request.setPaymentProduct(PisPaymentProduct.getByCode(createPisConsentData.getPaymentProduct()).orElse(null));
        request.setPaymentType(PisPaymentType.SINGLE);
        request.setTppInfo(mapToTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(
            Optional.ofNullable(createPisConsentData.getAspspConsentData())
                .map(AspspConsentData::getAspspConsentData)
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
                .map(AspspConsentData::getAspspConsentData)
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
                .map(AspspConsentData::getAspspConsentData)
                .orElse(null));

        return request;

    }

    public Optional<Xsa2CreatePisConsentAuthorisationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xsa2CreatePisConsentAuthorisationResponse(response.getAuthorizationId(), Xs2aScaStatus.RECEIVED.name(), paymentType.getValue()));
    }

    public Optional<Xs2aCreatePisConsentCancellationAuthorisationResponse> mapToXs2aCreatePisConsentCancellationAuthorisationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xs2aCreatePisConsentCancellationAuthorisationResponse(response.getAuthorizationId(), Xs2aScaStatus.RECEIVED.name(), paymentType.getValue()));
    }

    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> mapToXs2aPaymentCancellationAuthorisationSubResource (String response) {
        return Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.singletonList(response)));
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

                       cmsTppInfo.setRegistrationNumber(tpp.getAuthorisationNumber());
                       cmsTppInfo.setTppName(tpp.getTppName());

                       List<Xs2aTppRole> tppRoles = tpp.getTppRoles();
                       if (CollectionUtils.isNotEmpty(tppRoles)) {
                           cmsTppInfo.setTppRole(tppRoles.get(0).name()); // TODO properly map tppRoles to CmsTppInfo https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/266
                       }

                       cmsTppInfo.setNationalCompetentAuthority(tpp.getAuthorityName());
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


}
