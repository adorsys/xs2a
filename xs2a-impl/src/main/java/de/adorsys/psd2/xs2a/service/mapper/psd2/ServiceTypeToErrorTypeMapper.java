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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType.*;

@Component
public class ServiceTypeToErrorTypeMapper {
    private final static Map<ServiceType, Map<Integer, ErrorType>> serviceTypeToHttpCodeAndErrorType;

    static {
        Map<Integer, ErrorType> aisHttpCodeToErrorType = new HashMap<>();
        aisHttpCodeToErrorType.put(400, AIS_400);
        aisHttpCodeToErrorType.put(401, AIS_401);
        aisHttpCodeToErrorType.put(403, AIS_403);
        aisHttpCodeToErrorType.put(404, AIS_404);
        aisHttpCodeToErrorType.put(405, AIS_405);
        aisHttpCodeToErrorType.put(406, AIS_406);
        aisHttpCodeToErrorType.put(415, AIS_415);
        aisHttpCodeToErrorType.put(429, AIS_429);
        aisHttpCodeToErrorType.put(500, AIS_500);

        Map<Integer, ErrorType> pisHttpCodeToErrorType = new HashMap<>();
        pisHttpCodeToErrorType.put(400, PIS_400);
        pisHttpCodeToErrorType.put(401, PIS_401);
        pisHttpCodeToErrorType.put(403, PIS_403);
        pisHttpCodeToErrorType.put(404, PIS_404);
        pisHttpCodeToErrorType.put(405, PIS_405);
        pisHttpCodeToErrorType.put(409, PIS_409);
        pisHttpCodeToErrorType.put(415, PIS_415);
        pisHttpCodeToErrorType.put(500, PIS_500);

        Map<Integer, ErrorType> piisHttpCodeToErrorType = new HashMap<>();
        piisHttpCodeToErrorType.put(400, PIIS_400);
        piisHttpCodeToErrorType.put(401, PIIS_401);
        piisHttpCodeToErrorType.put(403, PIIS_403);
        piisHttpCodeToErrorType.put(404, PIIS_404);
        piisHttpCodeToErrorType.put(405, PIIS_405);
        piisHttpCodeToErrorType.put(409, PIIS_409);
        piisHttpCodeToErrorType.put(415, PIIS_415);
        piisHttpCodeToErrorType.put(500, PIIS_500);

        Map<Integer, ErrorType> sbHttpCodeToErrorType = new HashMap<>();
        sbHttpCodeToErrorType.put(400, SB_400);
        sbHttpCodeToErrorType.put(401, SB_401);
        sbHttpCodeToErrorType.put(403, SB_403);
        sbHttpCodeToErrorType.put(404, SB_404);
        sbHttpCodeToErrorType.put(405, SB_405);
        sbHttpCodeToErrorType.put(409, SB_409);
        sbHttpCodeToErrorType.put(415, SB_415);
        sbHttpCodeToErrorType.put(500, SB_500);

        serviceTypeToHttpCodeAndErrorType = new HashMap<>();
        serviceTypeToHttpCodeAndErrorType.put(AIS, aisHttpCodeToErrorType);
        serviceTypeToHttpCodeAndErrorType.put(PIS, pisHttpCodeToErrorType);
        serviceTypeToHttpCodeAndErrorType.put(PIIS, piisHttpCodeToErrorType);
        serviceTypeToHttpCodeAndErrorType.put(SB, sbHttpCodeToErrorType);
    }

    public ErrorType mapToErrorType(ServiceType serviceType, int httpCode) {
        return Optional.ofNullable(serviceTypeToHttpCodeAndErrorType.get(serviceType))
                   .map(m -> m.get(httpCode))
                   .orElse(null);
    }
}
