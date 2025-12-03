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

package de.adorsys.psd2.xs2a.service.vop.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkVerificationOfPartyRequest;
import de.adorsys.psd2.model.VerificationOfPartyRequest;
import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.domain.vop.SingleVop;
import de.adorsys.psd2.xs2a.service.mapper.VopModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawToXs2aVopMapper {
    private final VopModelMapper vopModelMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public SingleVop mapToSingleVop(byte[] vopBody) {
        if (ArrayUtils.isEmpty(vopBody)) {
            return null;
        }

        VerificationOfPartyRequest vopRequest = readBytes(vopBody, VerificationOfPartyRequest.class);
        SingleVop singleVop = vopModelMapper.mapToXs2aVop(vopRequest);

        if (singleVop != null) {
            singleVop.setVopData(vopBody);
        }

        return singleVop;
    }

    /**
     * Maps raw byte array to BulkVop domain object.
     * Deserializes the byte array into BulkVerificationOfPartyRequest and maps it to internal BulkVop model.
     *
     * @param vopBody the raw VOP request body as byte array
     * @return the mapped BulkVop object or null if the input is empty or deserialization fails
     */
    public BulkVop mapToBulkVop(byte[] vopBody) {
        if (ArrayUtils.isEmpty(vopBody)) {
            return null;
        }

        BulkVerificationOfPartyRequest bulkRequest = readBytes(vopBody, BulkVerificationOfPartyRequest.class);
        if (bulkRequest == null) {
            return null;
        }

        BulkVop bulkVop = vopModelMapper.mapToXs2aVop(bulkRequest);

        if (bulkVop != null) {
            bulkVop.setVopData(vopBody);
        }

        return bulkVop;
    }

    private <T> T readBytes(byte[] vopBody, Class<T> clazz) {
        try {
            return xs2aObjectMapper.readValue(vopBody, clazz);
        } catch (IOException e) {
            log.warn("Couldn't deserialize vop from bytes");
            return null;
        }
    }

}
