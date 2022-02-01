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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.mapper.ServiceType.*;

@Component
public class ServiceTypeToErrorTypeMapper {
    private static final Map<ServiceType, Map<Integer, ErrorType>> serviceTypeToHttpCodeAndErrorType;

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
        pisHttpCodeToErrorType.put(406, PIS_406);
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

        serviceTypeToHttpCodeAndErrorType = new EnumMap<>(ServiceType.class);
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
