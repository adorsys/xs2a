/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AcceptContentTypeDeserializer extends StdDeserializer<MediaType[]> {

    private static List<MediaType> ALLOWED_ACCEPT_HEADER_VALUES = new ArrayList<>();

    static {
        ALLOWED_ACCEPT_HEADER_VALUES.add(MediaType.APPLICATION_JSON);
        ALLOWED_ACCEPT_HEADER_VALUES.add(MediaType.APPLICATION_XML);
        ALLOWED_ACCEPT_HEADER_VALUES.add(MediaType.TEXT_PLAIN);
        ALLOWED_ACCEPT_HEADER_VALUES.add(MediaType.ALL);
    }

    public AcceptContentTypeDeserializer() {
        super(MediaType[].class);
    }

    @Override
    public MediaType[] deserialize(JsonParser jsonParser, DeserializationContext ctxt) {
        try {

            String parsedText = jsonParser.getText();
            List<MediaType> mediaTypes = MediaType.parseMediaTypes(parsedText);

            long validMediaTypes = mediaTypes.stream()
                                       .filter(ALLOWED_ACCEPT_HEADER_VALUES::contains)
                                       .count();

            if (validMediaTypes == mediaTypes.size()) {
                return mediaTypes.toArray(new MediaType[0]);
            } else {
                throw new IllegalArgumentException("Unsupported 'Accept' header values");
            }

        } catch (IOException e) {
            log.error("Unsupported Accept header value format!");
        }
        return null;
    }
}
