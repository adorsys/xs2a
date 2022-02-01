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

package de.adorsys.xs2a.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JsonReader {

    private static final String PARSING_EXCEPTION_MSG = "Exception during class '%s' parsing: %s";
    private static final String PARSING_LIST_EXCEPTION_MSG = "Exception during list of class '%s' parsing: %s";
    private static final String READING_FILE_EXCEPTION_MSG = "Exception during reading '%s' file.";

    private Xs2aObjectMapper xs2aObjectMapper;

    public JsonReader() {
        xs2aObjectMapper = getObjectMapper();
    }

    public JsonReader(Map<String, Boolean> properties) {
        xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.findAndRegisterModules();
        for (Map.Entry<String, Boolean> property : properties.entrySet()) {
            xs2aObjectMapper.configure(SerializationFeature.valueOf(property.getKey()), property.getValue());
        }
    }

    /**
     * @param <T> type of object to get
     * @param fileName name of the file
     * @param name name of the class of object to get
     *
     * @return serialized instance read from file
     */
    public <T> T getObjectFromFile(String fileName, Class<T> name) {
        URL resourcePath = getFileFromClasspath(fileName);
        try {
            return xs2aObjectMapper.readValue(resourcePath, name);
        } catch (IOException e) {
            throw new ParseContentJsonReaderException(String.format(PARSING_EXCEPTION_MSG, name, e.getMessage()));
        }
    }

    /**
     * @param <T> type of object to get
     * @param fileName name of the file
     * @param name name of the class of object to get
     *
     * @return serialized instance read from file
     */
    public <T> T getObjectFromFile(String fileName, TypeReference<T> name) {
        URL resourcePath = getFileFromClasspath(fileName);
        try {
            return xs2aObjectMapper.readValue(resourcePath, name);
        } catch (IOException e) {
            throw new ParseContentJsonReaderException(String.format(PARSING_EXCEPTION_MSG, name, e.getMessage()));
        }
    }

    /**
     * @param fileName name of the file
     * @return String representation of json file
     */
    public String getStringFromFile(String fileName) {
        try {
            return IOUtils.toString(getResourceAsStream(fileName), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ParseContentJsonReaderException(String.format(READING_FILE_EXCEPTION_MSG, fileName, e.getMessage()));
        }
    }

    /**
     * @param fileName name of the file
     *
     * @return byte array read from file
     */
    public byte[] getBytesFromFile(String fileName) {
        try {
            return IOUtils.toByteArray(getResourceAsStream(fileName));
        } catch (Exception e) {
            throw new ParseContentJsonReaderException(String.format(READING_FILE_EXCEPTION_MSG, fileName, e.getMessage()));
        }
    }

    /**
     * @param <T> type of object to get
     * @param json json representation of object
     * @param name name of the class of object to get
     *
     * @return serialized object read from String
     */
    public <T> T getObjectFromString(String json, Class<T> name) {
        try {
            return xs2aObjectMapper.readValue(json, name);
        } catch (IOException e) {
            throw new ParseContentJsonReaderException(String.format(PARSING_EXCEPTION_MSG, name, e.getMessage()));
        }
    }

    /**
     * @param <T> type of objects in list to get
     * @param json json representation of objects in list
     * @param name name of the class of objects in list to get
     *
     * @return list of serialized object read from String
     */
    public <T> List<T> getListFromString(String json, Class<T> name) {
        try {
            return xs2aObjectMapper.readValue(json,
                                              xs2aObjectMapper.getTypeFactory().constructCollectionType(List.class, name));
        } catch (IOException e) {
            throw new ParseContentJsonReaderException(String.format(PARSING_LIST_EXCEPTION_MSG, name, e.getMessage()));
        }
    }

    /**
     * @param <T> type of objects in list to get
     * @param fileName name of the file
     * @param name name of the class of objects in list to get
     *
     * @return list of serialized object read from file
     */
    public <T> List<T> getListFromFile(String fileName, Class<T> name) {
        URL resourcePath = getFileFromClasspath(fileName);
        try {
            return xs2aObjectMapper.readValue(resourcePath,
                                              xs2aObjectMapper.getTypeFactory().constructCollectionType(List.class, name));
        } catch (IOException e) {
            throw new ParseContentJsonReaderException(String.format(PARSING_LIST_EXCEPTION_MSG, name, e.getMessage()));
        }
    }

    /**
     * @param value object to be written as string
     *
     * @return string representation of object
     */
    public String writeValueAsString(Object value) {
        try {
            return xs2aObjectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new ParseContentJsonReaderException("Exception during object parsing to String." + e.getMessage());
        }
    }

    private URL getFileFromClasspath(String filename) {
        return JsonReader.class.getClassLoader().getResource(filename);
    }

    private InputStream getResourceAsStream(String resourcePath) {
        return JsonReader.class.getClassLoader().getResourceAsStream(resourcePath);
    }

    private Xs2aObjectMapper getObjectMapper() {
        Xs2aObjectMapper objectMapper = new Xs2aObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
