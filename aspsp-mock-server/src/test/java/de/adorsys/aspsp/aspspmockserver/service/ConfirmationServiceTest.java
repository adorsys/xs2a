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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
import de.adorsys.psd2.aspsp.mock.api.psu.Tan;
import de.adorsys.psd2.aspsp.mock.api.psu.TanStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmationServiceTest {
    private static final String ASPSP_PSU_ID_1 = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String ASPSP_PSU_ID_2 = "ad918c89-4346-4f16-b5c8-d781b040200c";
    private final String IBAN_1 = "DE123456789";
    private final String IBAN_2 = "DE987654321";
    private static final String WRONG_IBAN = "Wrong iban";
    private static final String TAN_ID = "2d4b403b-f5f5-41c0-847f-b6abf1edb102";
    private static final String TAN_NUMBER = "123456";
    private static final String WRONG_TAN_NUMBER = "wrong tan number";
    private static final String CONSENT_ID = "6d4b403b-f5f5-41c0-847f-b6abf1edb102";
    private final String PSU_ID_1 = "aspsp";
    private final String WRONG_PSU_ID = "aspsp";
    private final String PSU_ID_2 = "aspsp1";

    @InjectMocks
    private TanConfirmationService tanConfirmationService;

    @Mock
    private TanRepository tanRepository;
    @Mock
    private PsuRepository psuRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ConsentService consentService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(tanConfirmationService, "maximumNumberOfTanAttempts", 3);
        when(psuRepository.findOne(ASPSP_PSU_ID_1))
            .thenReturn(getPsu1());
        when(psuRepository.findOne(ASPSP_PSU_ID_2))
            .thenReturn(getPsu2());
        when(psuRepository.findOne(WRONG_PSU_ID))
            .thenReturn(null);
        when(accountService.getPsuIdByIban(WRONG_IBAN))
            .thenReturn(Optional.empty());
        when(accountService.getPsuIdByIban(IBAN_1))
            .thenReturn(Optional.of(ASPSP_PSU_ID_1));
        when(accountService.getPsuIdByIban(IBAN_2))
            .thenReturn(Optional.of(ASPSP_PSU_ID_2));
        when(tanRepository.save(any(Tan.class)))
            .thenReturn(getUnusedTan());
        when(tanRepository.findByPsuIdAndTanStatus(PSU_ID_1, TanStatus.UNUSED))
            .thenReturn(Collections.singletonList(getUnusedTan()));
        when(tanRepository.findByPsuIdAndTanStatus(PSU_ID_2, TanStatus.UNUSED))
            .thenReturn(Collections.emptyList());
        when(accountService.getPsuByPsuId(PSU_ID_1))
            .thenReturn(Optional.empty());
        when(accountService.getPsuByPsuId(WRONG_PSU_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void generateAndSendTanForPsuByName_Failure() {
        //When
        boolean actualResult = tanConfirmationService.generateAndSendTanForPsuById(WRONG_PSU_ID);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void isTanNumberValidByPsuId_Failure() {
        //When
        ResponseEntity actualResult = tanConfirmationService.confirmTan(PSU_ID_1, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void isTanNumberValidByIban_TanStatusValid() {
        //When
        ResponseEntity actualResult = tanConfirmationService.confirmTan(PSU_ID_1, TAN_NUMBER);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void isPsuTanNumberValid_TanStatusInvalid() {
        //When
        ResponseEntity actualResult = tanConfirmationService.confirmTan(PSU_ID_1, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Psu getPsu1() {
        return new Psu(PSU_ID_1, "test1@gmail.com", "aspsp1", "zzz", null, null, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")));
    }

    private Psu getPsu2() {
        return new Psu(PSU_ID_2, "test2@gmail.com", "aspsp2", "zzz", null, null, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")));
    }

    private Tan getUnusedTan() {
        return new Tan(TAN_ID, PSU_ID_1, TAN_NUMBER, TanStatus.UNUSED, 0);
    }
}
