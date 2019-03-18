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

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.RequestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestProviderServiceTest {
    private static final String URI = "http://www.adorsys.de";
    private static final String IP = "192.168.0.26";
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";

    private static final String PSU_ID_HEADER = "psu-id";
    private static final String PSU_ID_TYPE_HEADER = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER = "psu-corporate-id-type";

    private static final String PSU_ID = "ID";
    private static final String PSU_ID_TYPE = "TYPE";
    private static final String PSU_CORPORATE_ID = "CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "CORPORATE_ID_TYPE";

    private static PsuIdData PSU_ID_DATA;

    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final Map<String, String> HEADERS = new HashMap<>();

    @InjectMocks
    private RequestProviderService requestProviderService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        buildHeaders();
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
        assertEquals(X_REQUEST_ID, requestData.getRequestId().toString());
        assertEquals(PSU_ID_DATA, requestData.getPsuIdData());
        assertEquals(HEADERS, requestData.getHeaders());
    }

    private void buildHeaders() {
        HEADERS.put(X_REQUEST_ID_HEADER, X_REQUEST_ID);
        HEADERS.put(PSU_ID_HEADER, PSU_ID);
        HEADERS.put(PSU_ID_TYPE_HEADER, PSU_ID_TYPE);
        HEADERS.put(PSU_CORPORATE_ID_HEADER, PSU_CORPORATE_ID);
        HEADERS.put(PSU_CORPORATE_ID_TYPE_HEADER, PSU_CORPORATE_ID_TYPE);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID,
                             PSU_ID_TYPE,
                             PSU_CORPORATE_ID,
                             PSU_CORPORATE_ID_TYPE);
    }
}
