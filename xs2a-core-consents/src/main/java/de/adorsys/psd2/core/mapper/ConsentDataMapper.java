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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.core.data.ais.AisConsentData;
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

    public byte[] getBytesFromAisConsentData(AisConsentData aisConsentData) {
        try {
            return objectMapper.writeValueAsBytes(aisConsentData);
        } catch (JsonProcessingException e) {
            log.info("Can't convert aisConsentData to byte[]: {}", e.getMessage());
            return new byte[0];
        }
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper localObjectMapper = new ObjectMapper();
        localObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        localObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        localObjectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        return localObjectMapper;
    }
}
