/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.vop.create;

import de.adorsys.psd2.xs2a.domain.vop.SingleVop;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationParameters;
import de.adorsys.psd2.xs2a.service.vop.create.spi.SingleVopInitiationService;
import de.adorsys.psd2.xs2a.service.vop.mapper.RawToXs2aVopMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for creating single VOP verifications
 */
@Slf4j
@Service
public class CreateSingleVopService extends AbstractCreateVopService<SingleVop, SingleVopInitiationService> {
    private final RawToXs2aVopMapper rawToXs2aVopMapper;

    public CreateSingleVopService(SingleVopInitiationService vopInitiationService, RawToXs2aVopMapper rawToXs2aVopMapper) {
        super(vopInitiationService);
        this.rawToXs2aVopMapper = rawToXs2aVopMapper;
    }

    @Override
    protected SingleVop getVopRequest(byte[] vop, VopInitiationParameters params) {
        return rawToXs2aVopMapper.mapToSingleVop(vop);
    }
}
