/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class Xs2aPisCommonPaymentMapperTest {
    private final static JsonReader jsonReader = new JsonReader();
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static PsuIdData psuData;
    private static CreateAuthorisationResponse createPisAuthorisationResponse;

    @InjectMocks
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;

    @BeforeEach
    void setUp() {
        psuData =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/psu-data.json", PsuIdData.class);
        createPisAuthorisationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-pis-authorisation-response.json",
                                         CreateAuthorisationResponse.class);
    }

    @Test
    void buildSpiScaConfirmation() {
        SpiScaConfirmation expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-sca-confirmation.json",
                                         SpiScaConfirmation.class);

        PaymentAuthorisationParameters request =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-update-pis-common-payment-psu-data-request.json",
                                         PaymentAuthorisationParameters.class);
        String consentId = "consentId";
        String paymentId = "paymentId";

        SpiScaConfirmation actual = xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, consentId, paymentId, psuData);

        assertEquals(expected, actual);
    }

    @Test
    void mapToXs2aPisCommonPayment() {
        Xs2aPisCommonPayment expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-pis-common-payment.json",
                                         Xs2aPisCommonPayment.class);

        CreatePisCommonPaymentResponse input =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-pis-common-payment-response.json",
                                         CreatePisCommonPaymentResponse.class);

        Xs2aPisCommonPayment actual = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(input, psuData);

        assertEquals(expected, actual);
    }

    @Test
    void mapToXs2aCreatePisCancellationAuthorisationResponse() {
        createPisAuthorisationResponse.setInternalRequestId("cancellationInternalRequestId");
        Xs2aCreatePisCancellationAuthorisationResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-create-pis-cancellation-authorisation-response.json",
                                         Xs2aCreatePisCancellationAuthorisationResponse.class);

        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualOptional =
            xs2aPisCommonPaymentMapper
                .mapToXs2aCreatePisCancellationAuthorisationResponse(createPisAuthorisationResponse, PAYMENT_TYPE);

        assertEquals(expected, actualOptional.get());
    }

    @Test
    void mapToXs2aCreatePisCancellationAuthorisationResponse_ShouldReturnEmpty() {
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> expected = Optional.empty();
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actual = xs2aPisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(null, null);
        assertEquals(expected, actual);
    }

    @Test
    void mapToXsa2CreatePisAuthorisationResponse() {
        Xs2aCreatePisAuthorisationResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-create-pis-authorisation-response.json",
                                         Xs2aCreatePisAuthorisationResponse.class);

        Optional<Xs2aCreatePisAuthorisationResponse> actualOptional =
            xs2aPisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(createPisAuthorisationResponse, PAYMENT_TYPE);

        assertEquals(expected, actualOptional.get());
    }

    @Test
    void mapToXsa2CreatePisAuthorisationResponse_ShouldReturnEmpty() {
        Optional<Xs2aCreatePisAuthorisationResponse> expected = Optional.empty();
        Optional<Xs2aCreatePisAuthorisationResponse> actual = xs2aPisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(null, null);
        assertEquals(expected, actual);
    }

    @Test
    void mapToUpdateAuthorisationRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataResponse authorisationProcessorResponse = jsonReader.getObjectFromFile("json/service/mapper/consent/authorisation-processor-response.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);

        UpdateAuthorisationRequest actual = xs2aPisCommonPaymentMapper.mapToUpdateAuthorisationRequest(authorisationProcessorResponse, AuthorisationType.PIS_CREATION);

        UpdateAuthorisationRequest expected = jsonReader.getObjectFromFile("json/service/mapper/consent/update-authorisation-request.json", UpdateAuthorisationRequest.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToUpdateAuthorisationRequest_nullValue() {
        assertNull(xs2aPisCommonPaymentMapper.mapToUpdateAuthorisationRequest(null, AuthorisationType.PIS_CREATION));
    }
}
