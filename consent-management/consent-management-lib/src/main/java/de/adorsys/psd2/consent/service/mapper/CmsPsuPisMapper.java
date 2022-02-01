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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsPsuPisMapper {
    private final TppInfoMapper tppInfoMapper;
    private final PsuDataMapper psuDataMapper;
    private final CmsRemittanceMapper cmsRemittanceMapper;
    private final CorePaymentsConvertService corePaymentsConvertService;
    private final CmsAddressMapper cmsAddressMapper;

    public CmsBasePaymentResponse mapToCmsPayment(@NotNull PisCommonPaymentData paymentData) {
        CmsCommonPayment cmsCommonPayment = new CmsCommonPayment(paymentData.getPaymentProduct());
        cmsCommonPayment.setPaymentId(paymentData.getPaymentId());
        cmsCommonPayment.setPaymentProduct(paymentData.getPaymentProduct());
        cmsCommonPayment.setPaymentType(paymentData.getPaymentType());
        cmsCommonPayment.setTransactionStatus(paymentData.getTransactionStatus());
        cmsCommonPayment.setPaymentData(paymentData.getPayment());

        cmsCommonPayment.setTppInfo(tppInfoMapper.mapToTppInfo(paymentData.getTppInfo()));
        cmsCommonPayment.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(paymentData.getPsuDataList()));
        cmsCommonPayment.setCreationTimestamp(paymentData.getCreationTimestamp());
        cmsCommonPayment.setStatusChangeTimestamp(paymentData.getStatusChangeTimestamp());
        cmsCommonPayment.setTppBrandLoggingInformation(paymentData.getTppBrandLoggingInformation());
        return cmsCommonPayment;
    }

    public CmsBasePaymentResponse mapToCmsPayment(List<PisPaymentData> pisPaymentDataList) {
        PaymentType paymentType = pisPaymentDataList.get(0).getPaymentData().getPaymentType();
        String paymentProduct = pisPaymentDataList.get(0).getPaymentData().getPaymentProduct();

        switch (paymentType) {
            case BULK:
                return mapToCmsBulkPayment(pisPaymentDataList, paymentProduct);
            case PERIODIC:
                return mapToCmsPeriodicPayment(pisPaymentDataList.get(0), paymentProduct);
            case SINGLE:
                return mapToCmsSinglePayment(pisPaymentDataList.get(0), paymentProduct);
            default:
                return null;
        }
    }

    // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1143
    public List<CmsBasePaymentResponse> mapPaymentDataToCmsPayments(@NotNull List<PisCommonPaymentData> pisCommonPaymentData) {
        return pisCommonPaymentData.stream()
                   .map(this::mapPaymentDataToCmsPayment)
                   .collect(Collectors.toList());
    }

    public CmsBasePaymentResponse mapPaymentDataToCmsPayment(@NotNull PisCommonPaymentData pisCommonPaymentData) {
        List<PisPaymentData> pisPaymentData = pisCommonPaymentData.getPayments();
        if (pisPaymentData.isEmpty()) {
            CmsBasePaymentResponse cmsPayment = mapToCmsPayment(pisCommonPaymentData);
            return corePaymentsConvertService.expandCommonPaymentWithCorePayment((CmsCommonPayment) cmsPayment);
        } else {
            return mapToCmsPayment(pisPaymentData);
        }
    }

    private CmsBasePaymentResponse mapToCmsPeriodicPayment(PisPaymentData pisPaymentData, String paymentProduct) {
        CmsPeriodicPayment periodicPayment = new CmsPeriodicPayment(paymentProduct);
        periodicPayment.setPaymentId(pisPaymentData.getPaymentId());
        periodicPayment.setEndToEndIdentification(pisPaymentData.getEndToEndIdentification());
        periodicPayment.setInstructionIdentification(pisPaymentData.getInstructionIdentification());
        periodicPayment.setDebtorAccount(mapToAccountReference(pisPaymentData.getDebtorAccount()));
        periodicPayment.setInstructedAmount(new CmsAmount(pisPaymentData.getCurrency(), pisPaymentData.getAmount()));
        periodicPayment.setCreditorAccount(mapToAccountReference(pisPaymentData.getCreditorAccount()));
        periodicPayment.setCreditorAgent(pisPaymentData.getCreditorAgent());
        periodicPayment.setCreditorName(pisPaymentData.getCreditorName());
        periodicPayment.setCreditorAddress(cmsAddressMapper.mapToCmsAddress(pisPaymentData.getCreditorAddress()));
        periodicPayment.setRemittanceInformationUnstructured(pisPaymentData.getRemittanceInformationUnstructured());
        periodicPayment.setRequestedExecutionDate(pisPaymentData.getRequestedExecutionDate());
        periodicPayment.setRequestedExecutionTime(pisPaymentData.getRequestedExecutionTime());
        periodicPayment.setDayOfExecution(pisPaymentData.getDayOfExecution());
        periodicPayment.setStartDate(pisPaymentData.getStartDate());
        periodicPayment.setEndDate(pisPaymentData.getEndDate());
        periodicPayment.setExecutionRule(pisPaymentData.getExecutionRule());
        periodicPayment.setFrequency(FrequencyCode.valueOf(pisPaymentData.getFrequency()));
        PisCommonPaymentData paymentData = pisPaymentData.getPaymentData();
        if (Objects.nonNull(paymentData)) {
            periodicPayment.setTppInfo(tppInfoMapper.mapToTppInfo(paymentData.getTppInfo()));
            periodicPayment.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(paymentData.getPsuDataList()));
            periodicPayment.setPaymentStatus(paymentData.getTransactionStatus());
            periodicPayment.setCreationTimestamp(paymentData.getCreationTimestamp());
            periodicPayment.setStatusChangeTimestamp(paymentData.getStatusChangeTimestamp());
        }
        periodicPayment.setUltimateDebtor(pisPaymentData.getUltimateDebtor());
        periodicPayment.setUltimateCreditor(pisPaymentData.getUltimateCreditor());
        periodicPayment.setPurposeCode(pisPaymentData.getPurposeCode());
        periodicPayment.setRemittanceInformationStructured(cmsRemittanceMapper.mapToCmsRemittance(pisPaymentData.getRemittanceInformationStructured()));
        return periodicPayment;
    }

    private CmsBasePaymentResponse mapToCmsBulkPayment(List<PisPaymentData> pisPaymentDataList, String paymentProduct) {
        PisPaymentData bulkPisPaymentData = pisPaymentDataList.get(0);
        CmsBulkPayment bulkPayment = new CmsBulkPayment();
        bulkPayment.setPaymentId(bulkPisPaymentData.getPaymentData().getPaymentId());
        bulkPayment.setBatchBookingPreferred(false);
        bulkPayment.setDebtorAccount(mapToAccountReference(bulkPisPaymentData.getDebtorAccount()));
        bulkPayment.setPaymentProduct(paymentProduct);
        List<CmsSinglePayment> payments = pisPaymentDataList.stream()
                                              .map(p -> (CmsSinglePayment) mapToCmsSinglePayment(p, paymentProduct))
                                              .collect(Collectors.toList());
        bulkPayment.setPayments(payments);
        bulkPayment.setPaymentProduct(paymentProduct);

        return bulkPayment;
    }

    private CmsBasePaymentResponse mapToCmsSinglePayment(PisPaymentData pisPaymentData, String paymentProduct) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(paymentProduct);
        singlePayment.setPaymentId(pisPaymentData.getPaymentId());
        singlePayment.setEndToEndIdentification(pisPaymentData.getEndToEndIdentification());
        singlePayment.setInstructionIdentification(pisPaymentData.getInstructionIdentification());
        singlePayment.setDebtorAccount(mapToAccountReference(pisPaymentData.getDebtorAccount()));
        singlePayment.setInstructedAmount(new CmsAmount(pisPaymentData.getCurrency(), pisPaymentData.getAmount()));
        singlePayment.setCreditorAccount(mapToAccountReference(pisPaymentData.getCreditorAccount()));
        singlePayment.setCreditorAgent(pisPaymentData.getCreditorAgent());
        singlePayment.setCreditorName(pisPaymentData.getCreditorName());
        singlePayment.setCreditorAddress(cmsAddressMapper.mapToCmsAddress(pisPaymentData.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(pisPaymentData.getRemittanceInformationUnstructured());
        singlePayment.setRequestedExecutionDate(pisPaymentData.getRequestedExecutionDate());
        singlePayment.setRequestedExecutionTime(pisPaymentData.getRequestedExecutionTime());
        PisCommonPaymentData paymentData = pisPaymentData.getPaymentData();
        if (Objects.nonNull(paymentData)) {
            singlePayment.setTppInfo(tppInfoMapper.mapToTppInfo(paymentData.getTppInfo()));
            singlePayment.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(paymentData.getPsuDataList()));
            singlePayment.setPaymentStatus(paymentData.getTransactionStatus());
            singlePayment.setCreationTimestamp(paymentData.getCreationTimestamp());
            singlePayment.setStatusChangeTimestamp(paymentData.getStatusChangeTimestamp());
        }
        singlePayment.setUltimateDebtor(pisPaymentData.getUltimateDebtor());
        singlePayment.setUltimateCreditor(pisPaymentData.getUltimateCreditor());
        singlePayment.setPurposeCode(pisPaymentData.getPurposeCode());
        singlePayment.setRemittanceInformationStructured(cmsRemittanceMapper.mapToCmsRemittance(pisPaymentData.getRemittanceInformationStructured()));
        return singlePayment;
    }

    private AccountReference mapToAccountReference(AccountReferenceEntity pisAccountReference) {
        return Optional.ofNullable(pisAccountReference)
                   .map(ref -> new AccountReference(null,
                                                    null,
                                                    ref.getIban(),
                                                    ref.getBban(),
                                                    ref.getPan(),
                                                    ref.getMaskedPan(),
                                                    ref.getMsisdn(),
                                                    ref.getCurrency(),
                                                    null)
                   ).orElse(null);
    }
}
