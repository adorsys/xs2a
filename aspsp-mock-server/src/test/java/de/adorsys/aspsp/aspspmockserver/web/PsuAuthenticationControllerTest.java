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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PsuAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PsuAuthenticationControllerTest {
    private static final String PSU_ID = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final int TAN_NUMBER = 123456;
    private static final int WRONG_TAN_NUMBER = 0;
    private static MockHttpServletRequest MOCK_SERVLET;
    @Autowired
    private PsuAuthenticationController psuAuthenticationController;

    @MockBean
    private PsuAuthenticationService psuAuthenticationService;

    @Before
    public void setUp() {
        when(psuAuthenticationService.generateAndSendTanForPsu(PSU_ID))
            .thenReturn(PSU_ID);
        when(psuAuthenticationService.generateAndSendTanForPsu(WRONG_PSU_ID))
            .thenReturn(null);
        when(psuAuthenticationService.isPsuTanNumberValid(PSU_ID, TAN_NUMBER))
            .thenReturn(true);
        when(psuAuthenticationService.isPsuTanNumberValid(PSU_ID, WRONG_TAN_NUMBER))
            .thenReturn(false);
        MOCK_SERVLET = new MockHttpServletRequest();
        MOCK_SERVLET.setServerName("www.example.com");
        MOCK_SERVLET.setRequestURI("/psu-authentication/");
    }

    @Test
    public void generateTan_Success() throws Exception {
        //Given
        HttpStatus expecteStatus = CREATED;

        //When
        ResponseEntity actualResult = psuAuthenticationController.generateAndSendTan(MOCK_SERVLET, PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expecteStatus);
    }

    @Test
    public void generateTan_Failure() throws Exception {
        //Given
        HttpStatus expecteStatus = BAD_REQUEST;

        //When
        ResponseEntity actualResult = psuAuthenticationController.generateAndSendTan(MOCK_SERVLET, WRONG_PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expecteStatus);
    }

    @Test
    public void validatePsuTan_Success() throws Exception {
        //Given
        HttpStatus expecteStatus = OK;

        //When
        ResponseEntity actualResult = psuAuthenticationController.validatePsuTan(MOCK_SERVLET, PSU_ID, TAN_NUMBER);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expecteStatus);
    }

    @Test
    public void validatePsuTan_Failure() throws Exception {
        //Given
        HttpStatus expecteStatus = BAD_REQUEST;

        //When
        ResponseEntity actualResult = psuAuthenticationController.validatePsuTan(MOCK_SERVLET, PSU_ID, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expecteStatus);
    }
}
