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

package de.adorsys.psd2.xs2a.service.payment.status;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class handles traditional payments (single, bulk, periodic).
 *
 */
@Slf4j
public abstract class AbstractReadPaymentStatusService implements ReadPaymentStatusService {

    protected SpiPaymentFactory spiPaymentFactory;

    private SpiErrorMapper spiErrorMapper;
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    public AbstractReadPaymentStatusService(SpiPaymentFactory spiPaymentFactory, SpiErrorMapper spiErrorMapper,
                                            SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory) {
        this.spiPaymentFactory = spiPaymentFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
    }

    @Override
    public ReadPaymentStatusResponse readPaymentStatus(CommonPaymentData commonPaymentData, SpiContextData spiContextData, @NotNull String encryptedPaymentId) {
        List<PisPayment> pisPayments = getPisPayments(commonPaymentData);

        if (CollectionUtils.isEmpty(pisPayments)) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(ErrorType.PIS_400)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                    .build());
        }

        Optional spiPaymentOptional = createSpiPayment(commonPaymentData.getPayments(), commonPaymentData.getPaymentProduct());

        if (!spiPaymentOptional.isPresent()) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                    .build()
            );
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiGetPaymentStatusResponse> spiResponse = getSpiPaymentStatusById(spiContextData, spiPaymentOptional.get(), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            return new ReadPaymentStatusResponse(errorHolder);
        }

        SpiGetPaymentStatusResponse payload = spiResponse.getPayload();
        return new ReadPaymentStatusResponse(payload.getTransactionStatus(), payload.getFundsAvailable());
    }

    protected abstract Optional createSpiPayment(List<PisPayment> pisPayments, String paymentProduct);

    protected abstract SpiResponse<SpiGetPaymentStatusResponse> getSpiPaymentStatusById(SpiContextData spiContextData, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider);

    private List<PisPayment> getPisPayments(CommonPaymentData commonPaymentData) {
        List<PisPayment> pisPayments = Optional.of(commonPaymentData)
                                           .map(CommonPaymentData::getPayments)
                                           .orElseGet(Collections::emptyList);

        pisPayments.forEach(pmt -> {
            pmt.setPaymentId(commonPaymentData.getExternalId());
            pmt.setTransactionStatus(commonPaymentData.getTransactionStatus());
            pmt.setPsuDataList(commonPaymentData.getPsuData());
            pmt.setStatusChangeTimestamp(commonPaymentData.getStatusChangeTimestamp());
        });

        return pisPayments;
    }
}
