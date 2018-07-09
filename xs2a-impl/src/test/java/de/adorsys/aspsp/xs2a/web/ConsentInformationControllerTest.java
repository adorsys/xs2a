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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.config.WebConfigTest;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.util.StringUtils.isEmpty;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebConfigTest.class)
public class ConsentInformationControllerTest {
    private final String CORRECT_PSU_ID = "ID 777";
    private final String WRONG_PSU_ID = "ID 666";
    private final String CONSENT_ID = "XXXX-YYYY-XXXX-YYYY";
    private final String WRONG_CONSENT_ID = "YYYY-YYYY-YYYY-YYYY";

    @Autowired
    private ConsentInformationController consentInformationController;

    @MockBean(name = "consentService")
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(consentService.createAccountConsentsWithResponse(any(), anyBoolean(), anyBoolean(), eq(CORRECT_PSU_ID))).thenReturn(createConsentResponse(CONSENT_ID));
        when(consentService.createAccountConsentsWithResponse(any(), anyBoolean(), anyBoolean(), eq(WRONG_PSU_ID))).thenReturn(createConsentResponse(null));
        when(consentService.getAccountConsentsStatusById(CONSENT_ID)).thenReturn(ResponseObject.<ConsentStatus>builder().body(ConsentStatus.RECEIVED).build());
        when(consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID)).thenReturn(ResponseObject.<ConsentStatus>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build());
        when(consentService.getAccountConsentById(CONSENT_ID)).thenReturn(getConsent(CONSENT_ID));
        when(consentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(getConsent(WRONG_CONSENT_ID));
        when(consentService.deleteAccountConsentsById(CONSENT_ID)).thenReturn(ResponseObject.<Void>builder().build());
        when(consentService.deleteAccountConsentsById(WRONG_CONSENT_ID)).thenReturn(ResponseObject.<Void>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build());
    }

    @Test
    public void createAccountConsent_Success() {
        //Given:
        boolean tppRedirect = false;
        boolean withBalance = false;
        CreateConsentReq consentRequest = new CreateConsentReq();

        //When:
        ResponseEntity responseEntity = consentInformationController.createAccountConsent(CORRECT_PSU_ID, tppRedirect, withBalance, consentRequest);
        CreateConsentResp resp = (CreateConsentResp) responseEntity.getBody();

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getConsentStatus()).isEqualTo(ConsentStatus.RECEIVED);
        assertThat(resp.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsent_Failure() {
        //Given:
        boolean tppRedirect = false;
        boolean withBalance = false;
        CreateConsentReq consentRequest = new CreateConsentReq();
        //When:
        ResponseEntity responseEntity = consentInformationController.createAccountConsent(WRONG_PSU_ID, tppRedirect, withBalance, consentRequest);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseEntity responseEntity = consentInformationController.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseEntity responseEntity = consentInformationController.getAccountConsentsStatusById(WRONG_CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getAccountConsentsInformationById_Success() {
        //When:
        ResponseEntity responseEntity = consentInformationController.getAccountConsentsInformationById(CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(AccountConsent.class);
    }

    @Test
    public void getAccountConsentsInformationById_Failure() {
        //When:
        ResponseEntity responseEntity = consentInformationController.getAccountConsentsInformationById(WRONG_CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteAccountConsent_Success() {
        //When:
        ResponseEntity responseEntity = consentInformationController.deleteAccountConsent(CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void deleteAccountConsent_Failure() {
        //When:
        ResponseEntity responseEntity = consentInformationController.deleteAccountConsent(WRONG_CONSENT_ID);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    private ResponseObject createConsentResponse(String consentId) {
        return isEmpty(consentId)
               ? ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
               : ResponseObject.builder().body(new CreateConsentResp(ConsentStatus.RECEIVED, consentId, null, new Links(), null)).build();
    }

    private ResponseObject getConsent(String consentId) {
        AccountConsent accountConsent = consentId.equals(WRONG_CONSENT_ID)
                                        ? null
                                        : new AccountConsent(consentId, new AccountAccess(null,null,null,null,null), false, LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false);
        return isEmpty(accountConsent)
               ? ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
               : ResponseObject.builder().body(accountConsent).build();
    }

}
