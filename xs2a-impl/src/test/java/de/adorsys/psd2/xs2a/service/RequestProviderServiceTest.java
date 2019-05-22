/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestProviderServiceTest {
    private static final String URI = "http://www.adorsys.de";
    private static final String IP = "192.168.0.26";
    private static PsuIdData PSU_ID_DATA;
    private static final JsonReader jsonReader = new JsonReader();
    private static final Map<String, String> HEADERS = jsonReader.getObjectFromFile("json/RequestHeaders.json", new TypeReference<Map<String, String>>() {});

    @InjectMocks
    private RequestProviderService requestProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        PSU_ID_DATA = buildPsuIdData();
        HEADERS.forEach((key, value) -> when(httpServletRequest.getHeader(key)).thenReturn(value));
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(HEADERS.keySet()));
        when(httpServletRequest.getRequestURI()).thenReturn(URI);
        when(httpServletRequest.getRemoteAddr()).thenReturn(IP);
    }

    @Test
    public void getRequestDataSuccess() {
        //Given
        //When
        RequestData requestData = requestProviderService.getRequestData();
        //Then
        assertEquals(IP, requestData.getIp());
        assertEquals(URI, requestData.getUri());
        assertEquals(HEADERS.get(Xs2aHeaderConstant.X_REQUEST_ID), requestData.getRequestId().toString());
        assertEquals(PSU_ID_DATA, requestData.getPsuIdData());
        assertEquals(HEADERS, requestData.getHeaders());
    }

    @Test
    public void getPsuIpAddress() {
        //Given
        //When
        String psuIpAddress = requestProviderService.getPsuIpAddress();
        //Then
        assertEquals(HEADERS.get(Xs2aHeaderConstant.PSU_IP_ADDRESS), psuIpAddress);
        assertTrue(requestProviderService.isRequestFromPsu());
        assertFalse(requestProviderService.isRequestFromTPP());
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(HEADERS.get(Xs2aHeaderConstant.PSU_ID),
                             HEADERS.get(Xs2aHeaderConstant.PSU_ID_TYPE),
                             HEADERS.get(Xs2aHeaderConstant.PSU_CORPORATE_ID),
                             HEADERS.get(Xs2aHeaderConstant.PSU_CORPORATE_ID_TYPE));
    }
}
