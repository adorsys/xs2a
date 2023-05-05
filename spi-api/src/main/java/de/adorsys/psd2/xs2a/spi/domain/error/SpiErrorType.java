/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.error;

import de.adorsys.psd2.xs2a.spi.domain.mapper.SpiServiceType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum SpiErrorType {
    PIS_400(SpiServiceType.PIS, 400),
    PIS_401(SpiServiceType.PIS, 401),
    PIS_403(SpiServiceType.PIS, 403),
    PIS_404(SpiServiceType.PIS, 404),
    PIS_405(SpiServiceType.PIS, 405),
    PIS_406(SpiServiceType.PIS, 406),
    PIS_409(SpiServiceType.PIS, 409),
    PIS_415(SpiServiceType.PIS, 415),
    PIS_500(SpiServiceType.PIS, 500),
    PIS_503(SpiServiceType.PIS, 503),
    PIS_CANC_405(SpiServiceType.PIS, 405),
    PIS_CANC_503(SpiServiceType.PIS, 503),

    PIIS_400(SpiServiceType.PIIS, 400),
    PIIS_401(SpiServiceType.PIIS, 401),
    PIIS_403(SpiServiceType.PIIS, 403),
    PIIS_404(SpiServiceType.PIIS, 404),
    PIIS_405(SpiServiceType.PIIS, 405),
    PIIS_406(SpiServiceType.PIIS, 406),
    PIIS_409(SpiServiceType.PIIS, 409),
    PIIS_415(SpiServiceType.PIIS, 415),
    PIIS_429(SpiServiceType.PIIS, 429),
    PIIS_500(SpiServiceType.PIIS, 500),
    PIIS_503(SpiServiceType.PIIS, 503),

    AIS_400(SpiServiceType.AIS, 400),
    AIS_401(SpiServiceType.AIS, 401),
    AIS_403(SpiServiceType.AIS, 403),
    AIS_404(SpiServiceType.AIS, 404),
    AIS_405(SpiServiceType.AIS, 405),
    AIS_406(SpiServiceType.AIS, 406),
    AIS_409(SpiServiceType.AIS, 409),
    AIS_415(SpiServiceType.AIS, 415),
    AIS_429(SpiServiceType.AIS, 429),
    AIS_500(SpiServiceType.AIS, 500),
    AIS_503(SpiServiceType.AIS, 503),

    SB_400(SpiServiceType.SB, 400),
    SB_401(SpiServiceType.SB, 401),
    SB_403(SpiServiceType.SB, 403),
    SB_404(SpiServiceType.SB, 404),
    SB_405(SpiServiceType.SB, 405),
    SB_409(SpiServiceType.SB, 409),
    SB_415(SpiServiceType.SB, 415),
    SB_500(SpiServiceType.SB, 500),
    SB_503(SpiServiceType.SB, 503);

    @Getter
    private SpiServiceType serviceType;
    @Getter
    private int errorCode;

    SpiErrorType(SpiServiceType serviceType, int errorCode) {
        this.serviceType = serviceType;
        this.errorCode = errorCode;
    }

    public static Optional<SpiErrorType> getByServiceTypeAndErrorCode(SpiServiceType serviceType, int errorCode) {
        return Arrays.stream(SpiErrorType.values())
            .filter(et -> et.getServiceType().equals(serviceType))
            .filter(et -> et.getErrorCode() == errorCode)
            .findFirst();
    }
}
