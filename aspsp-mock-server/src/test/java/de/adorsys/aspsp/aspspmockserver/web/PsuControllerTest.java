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

import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class PsuControllerTest {
    private static final String ASPSP_PSU_ID = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String WRONG_ASPSP_PSU_ID = "WRONG_ASPSP_PSU_ID";
    private static final String PSU_ID = "aspsp";
    private static final String WRONG_PSU_ID = "zzz";
    private static final String E_MAIL = "info@adorsys.ua";
    private static final String PSU_PASSWORD = "zzz";
    private static final String WRONG_E_MAIL = "wrong e-mail";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String ACCOUNT_ID = "ACCOUNT1-0000-0000-0000-a000q000000t";
    private static final String IBAN = "DE123456789";
    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private PsuController psuController;

    @Mock
    private PsuService psuService;

    @Before
    public void setUp() {
        when(psuService.createPsuAndReturnId(getPsu(null, E_MAIL, getDetails(false), getProducts())))
            .thenReturn(PSU_ID);
        when(psuService.createPsuAndReturnId(getPsu(null, WRONG_E_MAIL, getDetails(false), getProducts())))
            .thenReturn(null);
        when(psuService.getAllPsuList()).thenReturn(Collections.singletonList(getPsu(PSU_ID, E_MAIL, getDetails(false), getProducts())));
        when(psuService.getPsuByPsuId(PSU_ID)).thenReturn(Optional.of(getPsu(PSU_ID, E_MAIL, getDetails(false), getProducts())));
        when(psuService.getPsuByPsuId(WRONG_PSU_ID)).thenReturn(Optional.empty());
        when(psuService.getAllowedPaymentProducts(PSU_ID)).thenReturn(getProducts());
        when(psuService.getAllowedPaymentProducts(WRONG_PSU_ID)).thenReturn(null);
        when(psuService.deletePsuByAspspPsuId(ASPSP_PSU_ID)).thenReturn(true);
        when(psuService.deletePsuByAspspPsuId(WRONG_ASPSP_PSU_ID)).thenReturn(false);

    }

    @Test
    public void createPsu_Success() {
        //When
        ResponseEntity actualResult = psuController.createPsu(getPsu(null, E_MAIL, getDetails(false), getProducts()));

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(OK);
        assertThat(actualResult.getBody()).isEqualTo(PSU_ID);
    }

    @Test
    public void createPsu_Failure_wrong_email() {
        //When:
        ResponseEntity actualResponse = psuController.createPsu(getPsu(null, WRONG_E_MAIL, getDetails(false), getProducts()));

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void readAllPsuList_Success() {
        //When
        ResponseEntity actualResult = psuController.readAllPsuList();

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(OK);
        assertThat(actualResult.getBody()).isEqualTo(Collections.singletonList(getPsu(PSU_ID, E_MAIL, getDetails(false), getProducts())));
    }

    @Test
    public void readPsuById_Success() {
        //When
        ResponseEntity actualResult = psuController.readPsuById(PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(OK);
        assertThat(actualResult.getBody()).isEqualTo(getPsu(PSU_ID, E_MAIL, getDetails(false), getProducts()));
    }

    @Test
    public void readPsuById_Failure_wrong_id() {
        //When
        ResponseEntity actualResult = psuController.readPsuById(WRONG_PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(NO_CONTENT);
    }

    @Test
    public void readPaymentProductsById_Success() {
        //When
        ResponseEntity actualResult = psuController.readPaymentProductsById(PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(OK);
        assertThat(actualResult.getBody()).isEqualTo(getProducts());
    }

    @Test
    public void readPaymentProductsById_Failure_wrong_id() {
        //When
        ResponseEntity actualResult = psuController.readPaymentProductsById(WRONG_PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(NO_CONTENT);
    }

    @Test
    public void deletePsu_Success() {
        //When
        ResponseEntity actualResult = psuController.deletePsu(ASPSP_PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(NO_CONTENT);
    }

    @Test
    public void deletePsu_Failure_wrong_id() {
        //When
        ResponseEntity actualResult = psuController.deletePsu(WRONG_ASPSP_PSU_ID);

        //Then
        assertThat(actualResult.getStatusCode()).isEqualTo(NOT_FOUND);
    }


    private List<String> getProducts() {
        return Collections.singletonList(CORRECT_PAYMENT_PRODUCT);
    }

    private Psu getPsu(String psuId, String email, List<AspspAccountDetails> details, List<String> products) {
        return new Psu(ASPSP_PSU_ID, email, PSU_ID, PSU_PASSWORD, details, products, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")));
    }

    private List<AspspAccountDetails> getDetails(boolean isEmpty) {
        return isEmpty
                   ? Collections.emptyList()
                   : Collections.singletonList(new AspspAccountDetails(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "Alfred", null, null, null, null, null, null, null, Collections.emptyList()));

    }
}
