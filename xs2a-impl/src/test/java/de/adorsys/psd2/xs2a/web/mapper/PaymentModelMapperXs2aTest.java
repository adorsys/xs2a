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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aObjectMapper.class, MultiPartBoundaryBuilder.class})
class PaymentModelMapperXs2aTest {

    private static final String CONTENT = "payment content";

    private PaymentModelMapperXs2a paymentModelMapper;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;
    @Autowired
    private MultiPartBoundaryBuilder multiPartBoundaryBuilder;

    private MockHttpServletRequest mockHttpServletRequest;
    private final JsonReader jsonReader = new JsonReader();


    @BeforeEach
    void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        paymentModelMapper = new PaymentModelMapperXs2a(mockHttpServletRequest, xs2aObjectMapper, multiPartBoundaryBuilder);
    }

    @Test
    void mapToXs2aPayment() {
        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aPayment();

        assertEquals(CONTENT, new String(actual));
    }

    @Test
    void mapToXs2aRawPayment_notPeriodic() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.SINGLE);

        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aRawPayment(requestParameters, "xml", null);

        assertEquals(CONTENT, new String(actual));
    }

    @Test
    void mapToXs2aRawPayment_periodic() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        MonthsOfExecution months = new MonthsOfExecution();
        months.add("4");

        requestParameters.setPaymentType(PaymentType.PERIODIC);

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        jsonStandingOrderType.setDayOfExecution(DayOfExecution._5);
        jsonStandingOrderType.setExecutionRule(ExecutionRule.FOLLOWING);
        jsonStandingOrderType.setFrequency(FrequencyCode.MONTHLY);
        jsonStandingOrderType.setMonthsOfExecution(months);

        byte[] actual = paymentModelMapper.mapToXs2aRawPayment(requestParameters, "xml", jsonStandingOrderType);

        String expected = jsonReader.getStringFromFile("json/web/mapper/raw-periodic-payment.txt").trim();
        assertEquals(expected, new String(actual));
    }

    @Test
    void mapToXs2aRawPayment_exceptionWhenXmlPartIsNull() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.PERIODIC);

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        jsonStandingOrderType.setDayOfExecution(DayOfExecution._5);
        jsonStandingOrderType.setExecutionRule(ExecutionRule.FOLLOWING);
        jsonStandingOrderType.setFrequency(FrequencyCode.MONTHLY);

        assertThrows(IllegalArgumentException.class, () -> paymentModelMapper.mapToXs2aRawPayment(requestParameters, null, jsonStandingOrderType));
    }
}
