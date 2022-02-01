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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.web.mapper.ScaMethodsMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aPaymentMapperImpl.class, ScaMethodsMapperImpl.class})
class SpiToXs2aPaymentMapperTest {

    @Autowired
    private SpiToXs2aPaymentMapper mapper;

    private final JsonReader jsonReader = new JsonReader();
    private final InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider =
        mock(InitialSpiAspspConsentDataProvider.class);

    @Test
    void mapSingleToCommonPaymentInitiateResponse_success() {
        SpiSinglePaymentInitiationResponse singlePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);

        CommonPaymentInitiationResponse actualResponse =
            mapper.mapToCommonPaymentInitiateResponse(singlePaymentInitiationResponse, PaymentType.SINGLE, initialSpiAspspConsentDataProvider);

        CommonPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/common-payment-initial-response.json", CommonPaymentInitiationResponse.class);
        expectedResponse.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void mapSingleToCommonPaymentInitiateResponse_transactionFeeIndicatorIsNotSupported() {
        SpiSinglePaymentInitiationResponse singlePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);
        singlePaymentInitiationResponse.setSpiTransactionFeeIndicator(null);

        CommonPaymentInitiationResponse actualResponse =
            mapper.mapToCommonPaymentInitiateResponse(singlePaymentInitiationResponse, PaymentType.SINGLE, initialSpiAspspConsentDataProvider);

        CommonPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/common-payment-initial-response.json", CommonPaymentInitiationResponse.class);
        expectedResponse.setTransactionFeeIndicator(null);
        expectedResponse.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void mapToPaymentInitiateResponse_Single() {
        SpiSinglePaymentInitiationResponse spiSinglePaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/single-payment-initial-response.json", SpiSinglePaymentInitiationResponse.class);

        SinglePaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiSinglePaymentInitiationResponse, initialSpiAspspConsentDataProvider);

        SinglePaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", SinglePaymentInitiationResponse.class);
        expectedResponse.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void mapToPaymentInitiateResponse_Periodic() {
        SpiPeriodicPaymentInitiationResponse spiPeriodicPaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initial-response.json", SpiPeriodicPaymentInitiationResponse.class);

        PeriodicPaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiPeriodicPaymentInitiationResponse, initialSpiAspspConsentDataProvider);

        PeriodicPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", PeriodicPaymentInitiationResponse.class);
        expectedResponse.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void mapToPaymentInitiateResponse_Bulk() {
        SpiBulkPaymentInitiationResponse spiBulkPaymentInitiationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initial-response.json", SpiBulkPaymentInitiationResponse.class);

        BulkPaymentInitiationResponse actualResponse = mapper.mapToPaymentInitiateResponse(
            spiBulkPaymentInitiationResponse, initialSpiAspspConsentDataProvider);

        BulkPaymentInitiationResponse expectedResponse =
            jsonReader.getObjectFromFile("json/service/mapper/payment-initial-response.json", BulkPaymentInitiationResponse.class);
        expectedResponse.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void spiAmountToXs2aAmount_notNull() {
        //When
        Xs2aAmount actual = mapper.spiAmountToXs2aAmount(getTestSpiAmount());
        //Then
        assertNotNull(actual.getAmount());
        assertNotNull(actual.getCurrency());
        assertEquals("22", actual.getAmount());
        assertEquals("USD", actual.getCurrency().getCurrencyCode());
    }

    private SpiAmount getTestSpiAmount() {
        return new SpiAmount(Currency.getInstance("USD"), BigDecimal.valueOf(22));
    }
}
