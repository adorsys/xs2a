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
            throw new ParseContentJsonReaderException(String.format(READING_FILE_EXCEPTION_MSG, fileName));
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
            throw new ParseContentJsonReaderException(String.format(READING_FILE_EXCEPTION_MSG, fileName));
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
