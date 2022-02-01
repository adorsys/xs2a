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

package de.adorsys.psd2.xs2a.spi.domain;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This object represents known Context of call, provided by this or previous requests in scope of one process (e.g. one payment or one AIS consent)
 */
@Value
@NotNull
@RequiredArgsConstructor
public class SpiContextData {
    private SpiPsuData psuData;
    private TppInfo tppInfo;
    private UUID xRequestId;
    private UUID internalRequestId;
    private String oAuth2Token;
    @Nullable
    private String tppBrandLoggingInformation;
    @Nullable
    private Boolean tppRejectionNoFundsPreferred;
    @Nullable
    private Boolean tppRedirectPreferred;
    @Nullable
    private Boolean tppDecoupledPreferred;
}
