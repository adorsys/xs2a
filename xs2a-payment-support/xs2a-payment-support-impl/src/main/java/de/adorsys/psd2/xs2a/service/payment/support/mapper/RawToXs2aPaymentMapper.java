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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.PaymentModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawToXs2aPaymentMapper {
    private final PaymentModelMapper paymentModelMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public SinglePayment mapToSinglePayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        PaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, PaymentInitiationJson.class);
        SinglePayment singlePayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (singlePayment != null) {
            singlePayment.setPaymentData(paymentBody);
        }

        return singlePayment;
    }

    public PeriodicPayment mapToPeriodicPayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        PeriodicPaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, PeriodicPaymentInitiationJson.class);
        PeriodicPayment periodicPayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (periodicPayment != null) {
            periodicPayment.setPaymentData(paymentBody);
        }

        return periodicPayment;
    }

    public BulkPayment mapToBulkPayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        BulkPaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, BulkPaymentInitiationJson.class);
        BulkPayment bulkPayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (bulkPayment != null) {
            bulkPayment.setPaymentData(paymentBody);
        }

        return bulkPayment;
    }

    private <T> T readBytes(byte[] paymentBody, Class<T> clazz) {
        try {
            return xs2aObjectMapper.readValue(paymentBody, clazz);
        } catch (IOException e) {
            log.warn("Couldn't deserialize payment from bytes");
            return null;
        }
    }
}
