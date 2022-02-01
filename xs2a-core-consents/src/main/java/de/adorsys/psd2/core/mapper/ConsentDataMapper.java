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

package de.adorsys.psd2.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ConsentDataMapper {
    private final ObjectMapper objectMapper = buildObjectMapper();

    public AisConsentData mapToAisConsentData(byte[] consentData) {
        if (consentData == null) {
            return AisConsentData.buildDefaultAisConsentData();
        }
        try {
            return objectMapper.readValue(consentData, AisConsentData.class);
        } catch (IOException e) {
            log.info("Can't convert byte[] to AisConsentData: {}", e.getMessage());
            return null;
        }
    }

    public PiisConsentData mapToPiisConsentData(byte[] consentData) {
        if (consentData == null) {
            return PiisConsentData.buildDefaultConsentData();
        }
        try {
            return objectMapper.readValue(consentData, PiisConsentData.class);
        } catch (IOException e) {
            log.info("Can't convert byte[] to PiisConsentData: {}", e.getMessage());
            return null;
        }
    }

    public byte[] getBytesFromConsentData(Object consentData) {
        try {
            return objectMapper.writeValueAsBytes(consentData);
        } catch (JsonProcessingException e) {
            log.info("Can't convert consentData to byte[]: {}", e.getMessage());
            return new byte[0];
        }
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapperConfig objectMapperConfig = new ObjectMapperConfig();
        return objectMapperConfig.xs2aObjectMapper();
    }
}
