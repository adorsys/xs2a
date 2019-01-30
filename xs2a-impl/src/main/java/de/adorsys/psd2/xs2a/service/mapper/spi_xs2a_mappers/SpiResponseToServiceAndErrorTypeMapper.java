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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType.*;
import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus.*;

@Component
public class SpiResponseToServiceAndErrorTypeMapper {
    private static final Map<SpiResponseStatus, Map<ServiceType, ErrorType>> spiResponseToServiceAndErrorType;

    static {
        Map<ServiceType, ErrorType> technicalFailureServiceTypeToErrorType = new HashMap<>();
        technicalFailureServiceTypeToErrorType.put(PIS, PIS_500);
        technicalFailureServiceTypeToErrorType.put(PIIS, PIIS_500);
        technicalFailureServiceTypeToErrorType.put(SB, SB_500);
        technicalFailureServiceTypeToErrorType.put(AIS, AIS_500);

        Map<ServiceType, ErrorType> unauthorizedFailureServiceTypeToErrorType = new HashMap<>();
        unauthorizedFailureServiceTypeToErrorType.put(PIS, PIS_401);
        unauthorizedFailureServiceTypeToErrorType.put(PIIS, PIIS_401);
        unauthorizedFailureServiceTypeToErrorType.put(SB, SB_401);
        unauthorizedFailureServiceTypeToErrorType.put(AIS, AIS_401);

        Map<ServiceType, ErrorType> logicalFailureServiceTypeToErrorType = new HashMap<>();
        logicalFailureServiceTypeToErrorType.put(PIS, PIS_400);
        logicalFailureServiceTypeToErrorType.put(PIIS, PIIS_400);
        logicalFailureServiceTypeToErrorType.put(SB, SB_400);
        logicalFailureServiceTypeToErrorType.put(AIS, AIS_400);

        Map<ServiceType, ErrorType> notSupportedFailureServiceTypeToErrorType = new HashMap<>();
        notSupportedFailureServiceTypeToErrorType.put(PIS, PIS_400);
        notSupportedFailureServiceTypeToErrorType.put(PIIS, PIIS_400);
        notSupportedFailureServiceTypeToErrorType.put(SB, SB_400);
        notSupportedFailureServiceTypeToErrorType.put(AIS, AIS_400);

        spiResponseToServiceAndErrorType = new HashMap<>();
        spiResponseToServiceAndErrorType.put(TECHNICAL_FAILURE, technicalFailureServiceTypeToErrorType);
        spiResponseToServiceAndErrorType.put(UNAUTHORIZED_FAILURE, unauthorizedFailureServiceTypeToErrorType);
        spiResponseToServiceAndErrorType.put(LOGICAL_FAILURE, logicalFailureServiceTypeToErrorType);
        spiResponseToServiceAndErrorType.put(NOT_SUPPORTED, notSupportedFailureServiceTypeToErrorType);
    }

    public ErrorType mapToErrorType(SpiResponseStatus spiResponseStatus, ServiceType serviceType) {
        return Optional.ofNullable(spiResponseToServiceAndErrorType.get(spiResponseStatus))
                   .map(m -> m.get(serviceType))
                   .orElse(null);
    }

}
