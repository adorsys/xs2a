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
import de.adorsys.aspsp.xs2a.spi.domain.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class AcceptContentTypeDeserializer extends StdDeserializer<ContentType[]> {

    public AcceptContentTypeDeserializer() {
        super(ContentType[].class);
    }

    @Override
    public ContentType[] deserialize(JsonParser jsonParser, DeserializationContext ctxt) {
        try {
            String input = jsonParser.getText();
            String[] acceptTypes = StringUtils.split(input, ", ");
            return Arrays.stream(acceptTypes)
                       .map(ContentType::getByName)
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .toArray(ContentType[]::new);
        } catch (IOException e) {
            log.error("Unsupported Accept header value format!");
        }
        return null;
    }
}
