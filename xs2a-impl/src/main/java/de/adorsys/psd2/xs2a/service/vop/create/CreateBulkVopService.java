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

import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationParameters;
import de.adorsys.psd2.xs2a.service.vop.create.spi.BulkVopInitiationService;
import de.adorsys.psd2.xs2a.service.vop.mapper.RawToXs2aVopMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for creating bulk VOP verifications.
 * Extends AbstractCreateVopService to handle bulk party verification requests.
 *
 * <p>This service follows the same pattern as CreateSingleVopService but handles
 * bulk verification requests containing multiple party verification entries.</p>
 *
 * <p>Dependencies are injected via constructor following Spring's recommended
 * dependency injection pattern for better testability and immutability.</p>
 *
 * @see CreateSingleVopService
 * @see AbstractCreateVopService
 */
@Slf4j
@Service
public class CreateBulkVopService extends AbstractCreateVopService<BulkVop, BulkVopInitiationService> {
    private final RawToXs2aVopMapper rawToXs2aVopMapper;

    /**
     * Constructs a new CreateBulkVopService with required dependencies.
     *
     * @param vopInitiationService the bulk VOP initiation service for processing verification requests
     * @param rawToXs2aVopMapper the mapper for converting raw byte data to BulkVop domain objects
     */
    public CreateBulkVopService(BulkVopInitiationService vopInitiationService, 
                                RawToXs2aVopMapper rawToXs2aVopMapper) {
        super(vopInitiationService);
        this.rawToXs2aVopMapper = rawToXs2aVopMapper;
    }

    /**
     * Converts raw VOP request bytes to BulkVop domain object.
     * This method is called by the parent class during the verification creation process.
     *
     * @param vop the raw VOP request as byte array
     * @param params the VOP initiation parameters (not used in mapping but required by contract)
     * @return the mapped BulkVop object containing the bulk verification request data
     */
    @Override
    protected BulkVop getVopRequest(byte[] vop, VopInitiationParameters params) {
        return rawToXs2aVopMapper.mapToBulkVop(vop);
    }
}
