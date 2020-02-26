/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsentData;
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
