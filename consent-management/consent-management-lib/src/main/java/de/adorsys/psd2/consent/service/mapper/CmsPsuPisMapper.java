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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
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
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final TppInfoMapper tppInfoMapper;
    private final PsuDataMapper psuDataMapper;

        public CmsPayment mapToCmsPayment(@NotNull PisCommonPaymentData paymentData) {
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
        return cmsCommonPayment;
    }

    public CmsPayment mapToCmsPayment(List<PisPaymentData> pisPaymentDataList) {
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

    // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
    public List<CmsPayment> mapPaymentDataToCmsPayments(@NotNull List<PisCommonPaymentData> pisCommonPaymentData) {
        return pisCommonPaymentData.stream()
                   .map(this::mapPaymentDataToCmsPayment)
                   .collect(Collectors.toList());
    }

    private CmsPayment mapPaymentDataToCmsPayment(@NotNull PisCommonPaymentData pisCommonPaymentData) {
        List<PisPaymentData> pisPaymentData = pisCommonPaymentData.getPayments();
        if (pisPaymentData.isEmpty()) {
            return mapToCmsPayment(pisCommonPaymentData);
        } else {
            return mapToCmsPayment(pisPaymentData);
        }
    }

    private CmsPayment mapToCmsPeriodicPayment(PisPaymentData pisPaymentData, String paymentProduct) {
        CmsPeriodicPayment periodicPayment = new CmsPeriodicPayment(paymentProduct);
        periodicPayment.setPaymentId(pisPaymentData.getPaymentId());
        periodicPayment.setEndToEndIdentification(pisPaymentData.getEndToEndIdentification());
        periodicPayment.setDebtorAccount(mapToCmsAccountReference(pisPaymentData.getDebtorAccount()));
        periodicPayment.setInstructedAmount(new CmsAmount(pisPaymentData.getCurrency(), pisPaymentData.getAmount()));
        periodicPayment.setCreditorAccount(mapToCmsAccountReference(pisPaymentData.getCreditorAccount()));
        periodicPayment.setCreditorAgent(pisPaymentData.getCreditorAgent());
        periodicPayment.setCreditorName(pisPaymentData.getCreditorName());
        periodicPayment.setCreditorAddress(pisCommonPaymentMapper.mapToCmsAddress(pisPaymentData.getCreditorAddress()));
        periodicPayment.setRemittanceInformationUnstructured(pisPaymentData.getRemittanceInformationUnstructured());
        periodicPayment.setRequestedExecutionDate(pisPaymentData.getRequestedExecutionDate());
        periodicPayment.setRequestedExecutionTime(pisPaymentData.getRequestedExecutionTime());
        periodicPayment.setDayOfExecution(pisPaymentData.getDayOfExecution());
        periodicPayment.setStartDate(pisPaymentData.getStartDate());
        periodicPayment.setEndDate(pisPaymentData.getEndDate());
        periodicPayment.setExecutionRule(pisPaymentData.getExecutionRule());
        periodicPayment.setFrequency(CmsFrequencyCode.valueOf(pisPaymentData.getFrequency()));
        PisCommonPaymentData paymentData = pisPaymentData.getPaymentData();
        if (Objects.nonNull(paymentData)) {
            periodicPayment.setTppInfo(tppInfoMapper.mapToTppInfo(paymentData.getTppInfo()));
            periodicPayment.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(paymentData.getPsuDataList()));
            periodicPayment.setPaymentStatus(paymentData.getTransactionStatus());
            periodicPayment.setCreationTimestamp(paymentData.getCreationTimestamp());
            periodicPayment.setStatusChangeTimestamp(paymentData.getStatusChangeTimestamp());
        }
        return periodicPayment;
    }

    private CmsPayment mapToCmsBulkPayment(List<PisPaymentData> pisPaymentDataList, String paymentProduct) {
        PisPaymentData bulkPisPaymentData = pisPaymentDataList.get(0);
        CmsBulkPayment bulkPayment = new CmsBulkPayment();
        bulkPayment.setPaymentId(bulkPisPaymentData.getPaymentId());
        bulkPayment.setBatchBookingPreferred(false);
        bulkPayment.setDebtorAccount(mapToCmsAccountReference(bulkPisPaymentData.getDebtorAccount()));
        bulkPayment.setPaymentProduct(paymentProduct);
        List<CmsSinglePayment> payments = pisPaymentDataList.stream()
                                              .map(p -> (CmsSinglePayment) mapToCmsSinglePayment(p, paymentProduct))
                                              .collect(Collectors.toList());
        bulkPayment.setPayments(payments);
        bulkPayment.setPaymentProduct(paymentProduct);

        return bulkPayment;
    }

    private CmsPayment mapToCmsSinglePayment(PisPaymentData pisPaymentData, String paymentProduct) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(paymentProduct);
        singlePayment.setPaymentId(pisPaymentData.getPaymentId());
        singlePayment.setEndToEndIdentification(pisPaymentData.getEndToEndIdentification());
        singlePayment.setDebtorAccount(mapToCmsAccountReference(pisPaymentData.getDebtorAccount()));
        singlePayment.setInstructedAmount(new CmsAmount(pisPaymentData.getCurrency(), pisPaymentData.getAmount()));
        singlePayment.setCreditorAccount(mapToCmsAccountReference(pisPaymentData.getCreditorAccount()));
        singlePayment.setCreditorAgent(pisPaymentData.getCreditorAgent());
        singlePayment.setCreditorName(pisPaymentData.getCreditorName());
        singlePayment.setCreditorAddress(pisCommonPaymentMapper.mapToCmsAddress(pisPaymentData.getCreditorAddress()));
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

        return singlePayment;
    }

    private CmsAccountReference mapToCmsAccountReference(AccountReferenceEntity pisAccountReference) {
        return Optional.ofNullable(pisAccountReference)
                   .map(ref -> new CmsAccountReference(null,
                       ref.getIban(),
                       ref.getBban(),
                       ref.getPan(),
                       ref.getMaskedPan(),
                       ref.getMsisdn(),
                       ref.getCurrency())
                   ).orElse(null);
    }
}
