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
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ais.*;
import de.adorsys.psd2.xs2a.service.mapper.psd2.piis.*;
import de.adorsys.psd2.xs2a.service.mapper.psd2.pis.*;
import de.adorsys.psd2.xs2a.service.mapper.psd2.sb.*;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;

@Component
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class ErrorMapperContainer {
    private final Map<ErrorType, Psd2ErrorMapper> mapperContainer = new EnumMap<>(ErrorType.class);

    private final PIS400ErrorMapper pis400ErrorMapper;
    private final PIS401ErrorMapper pis401ErrorMapper;
    private final PIS403ErrorMapper pis403ErrorMapper;
    private final PIS404ErrorMapper pis404ErrorMapper;
    private final PIS405ErrorMapper pis405ErrorMapper;
    private final PIS406ErrorMapper pis406ErrorMapper;
    private final PIS409ErrorMapper pis409ErrorMapper;
    private final PIS415ErrorMapper pis415ErrorMapper;
    private final PIS500ErrorMapper pis500ErrorMapper;
    private final PISCANC405ErrorMapper pisCanc405ErrorMapper;

    private final PIIS400ErrorMapper piis400ErrorMapper;
    private final PIIS401ErrorMapper piis401ErrorMapper;
    private final PIIS403ErrorMapper piis403ErrorMapper;
    private final PIIS404ErrorMapper piis404ErrorMapper;
    private final PIIS405ErrorMapper piis405ErrorMapper;
    private final PIIS406ErrorMapper piis406ErrorMapper;
    private final PIIS409ErrorMapper piis409ErrorMapper;
    private final PIIS415ErrorMapper piis415ErrorMapper;
    private final PIIS429ErrorMapper piis429ErrorMapper;
    private final PIIS500ErrorMapper piis500ErrorMapper;

    private final AIS415ErrorMapper ais415ErrorMapper;
    private final AIS500ErrorMapper ais500ErrorMapper;

    private final SB400ErrorMapper sb400ErrorMapper;
    private final SB401ErrorMapper sb401ErrorMapper;
    private final SB403ErrorMapper sb403ErrorMapper;
    private final SB404ErrorMapper sb404ErrorMapper;
    private final SB405ErrorMapper sb405ErrorMapper;
    private final SB409ErrorMapper sb409ErrorMapper;
    private final SB415ErrorMapper sb415ErrorMapper;
    private final SB500ErrorMapper sb500ErrorMapper;

    private final AIS400ErrorMapper ais400ErrorMapper;
    private final AIS401ErrorMapper ais401ErrorMapper;
    private final AIS403ErrorMapper ais403ErrorMapper;
    private final AIS404ErrorMapper ais404ErrorMapper;
    private final AIS405ErrorMapper ais405ErrorMapper;
    private final AIS406ErrorMapper ais406ErrorMapper;
    private final AIS409ErrorMapper ais409ErrorMapper;
    private final AIS429ErrorMapper ais429ErrorMapper;

    @PostConstruct
    public void fillErrorMapperContainer() {
        mapperContainer.put(PIS_400, pis400ErrorMapper);
        mapperContainer.put(PIS_401, pis401ErrorMapper);
        mapperContainer.put(PIS_403, pis403ErrorMapper);
        mapperContainer.put(PIS_404, pis404ErrorMapper);
        mapperContainer.put(PIS_405, pis405ErrorMapper);
        mapperContainer.put(PIS_406, pis406ErrorMapper);
        mapperContainer.put(PIS_409, pis409ErrorMapper);
        mapperContainer.put(PIS_415, pis415ErrorMapper);
        mapperContainer.put(PIS_500, pis500ErrorMapper);
        mapperContainer.put(PIS_CANC_405, pisCanc405ErrorMapper);

        mapperContainer.put(PIIS_400, piis400ErrorMapper);
        mapperContainer.put(PIIS_401, piis401ErrorMapper);
        mapperContainer.put(PIIS_403, piis403ErrorMapper);
        mapperContainer.put(PIIS_404, piis404ErrorMapper);
        mapperContainer.put(PIIS_405, piis405ErrorMapper);
        mapperContainer.put(PIIS_406, piis406ErrorMapper);
        mapperContainer.put(PIIS_409, piis409ErrorMapper);
        mapperContainer.put(PIIS_415, piis415ErrorMapper);
        mapperContainer.put(PIIS_429, piis429ErrorMapper);
        mapperContainer.put(PIIS_500, piis500ErrorMapper);

        mapperContainer.put(AIS_400, ais400ErrorMapper);
        mapperContainer.put(AIS_401, ais401ErrorMapper);
        mapperContainer.put(AIS_403, ais403ErrorMapper);
        mapperContainer.put(AIS_404, ais404ErrorMapper);
        mapperContainer.put(AIS_405, ais405ErrorMapper);
        mapperContainer.put(AIS_406, ais406ErrorMapper);
        mapperContainer.put(AIS_409, ais409ErrorMapper);
        mapperContainer.put(AIS_415, ais415ErrorMapper);
        mapperContainer.put(AIS_429, ais429ErrorMapper);
        mapperContainer.put(AIS_500, ais500ErrorMapper);

        mapperContainer.put(SB_400, sb400ErrorMapper);
        mapperContainer.put(SB_401, sb401ErrorMapper);
        mapperContainer.put(SB_403, sb403ErrorMapper);
        mapperContainer.put(SB_404, sb404ErrorMapper);
        mapperContainer.put(SB_405, sb405ErrorMapper);
        mapperContainer.put(SB_409, sb409ErrorMapper);
        mapperContainer.put(SB_415, sb415ErrorMapper);
        mapperContainer.put(SB_500, sb500ErrorMapper);
    }

    @SuppressWarnings("unchecked")
    public ErrorBody getErrorBody(MessageError error) {
        Psd2ErrorMapper psd2ErrorMapper = mapperContainer.get(error.getErrorType());

        return new ErrorBody(psd2ErrorMapper.getMapper()
                                 .apply(error), psd2ErrorMapper.getErrorStatus());
    }

    @Value
    public static class ErrorBody {
        private Object body;
        private HttpStatus status;
    }
}
