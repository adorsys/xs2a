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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class Xs2aObjectMapper extends ObjectMapper {

    public Xs2aObjectMapper(Xs2aObjectMapper src) {
        super(src);
    }

    /**
     * Converts value from input stream into JSON and extracts its field by given name
     *
     * @param stream        input stream used to read JSON content
     * @param fieldName     name of the JSON field to be extracted
     * @param typeReference type reference of the field
     * @param <T>           type of the field to be extracted
     * @return value of the extracted field, if it was found in the valid JSON
     */
    public <T> Optional<T> toJsonField(InputStream stream, String fieldName, TypeReference<T> typeReference) throws IOException {
            JsonNode jsonNode = readTree(stream);
            JsonNode fieldNode = jsonNode.get(fieldName);

            if (fieldNode == null) {
                return Optional.empty();
            }
            T value = readValue(treeAsTokens(fieldNode), typeReference);
            return Optional.ofNullable(value);
    }

    public List<String> toJsonGetValuesForField(InputStream stream, String fieldName) throws IOException {
        JsonNode jsonNode = readTree(stream);
        return new ArrayList<>(jsonNode.findValuesAsText(fieldName));
    }

    public List<String> toJsonGetListValuesForField(InputStream stream, String fieldName) throws IOException {
        List<String> values = new ArrayList<>();
        ArrayNode arrayNode = (ArrayNode) readTree(stream).get(fieldName);
            if(arrayNode != null) {
                Iterator<JsonNode> elements = arrayNode.elements();
                while(elements.hasNext()) {
                    values.add(elements.next().asText());
                }
            }
        return values;
    }

    @Override
    public ObjectMapper copy() {
        _checkInvalidCopy(Xs2aObjectMapper.class);
        return new Xs2aObjectMapper(this);
    }
}
