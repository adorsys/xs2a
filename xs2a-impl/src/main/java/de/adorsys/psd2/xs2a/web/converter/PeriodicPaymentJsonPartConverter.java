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

package de.adorsys.psd2.xs2a.web.converter;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Converter, responsible for mapping incoming json_standingorderType parameter into PeriodicPaymentInitiationXmlPart2StandingorderTypeJson
 * in {@link de.adorsys.psd2.api.PaymentApi#initiatePayment} in case of multipart payment initiation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicPaymentJsonPartConverter implements Converter<String, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson> {
    private final Xs2aObjectMapper xs2aObjectMapper;

    @Override
    public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        try {
            return xs2aObjectMapper.readValue(source, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class);
        } catch (IOException e) {
            log.info("Can't convert json to object: {}", e.getMessage());
            return null;
        }
    }
}
