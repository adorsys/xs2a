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


package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiXs2aAccountMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationConsent;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationServiceTest {
    private final Currency EUR = Currency.getInstance("EUR");
    private final String AMOUNT_1600 = "1600.00";
    private final String AMOUNT_160 = "160.00";

    @InjectMocks
    private FundsConfirmationService fundsConfirmationService;

    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Mock
    private FundsConfirmationSpi fundsConfirmationSpi;

    @Mock
    private SpiXs2aAccountMapper accountMapper;

    @Mock
    private FundsConfirmationConsentDataService fundsConfirmationConsentDataService;

    @Before
    public void setUp() {
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(ResponseObject.builder().build());
        when(fundsConfirmationConsentDataService.getAspspConsentDataByConsentId(anyString())).thenReturn(getAspspConsentData());

        when(accountMapper.mapToSpiAccountReference(getSufficientFundsConfirmationRequest().getPsuAccount())).thenReturn(getValidSpiAccountReference());
        when(accountMapper.mapToSpiAccountReference(getInSufficientFundsConfirmationRequest().getPsuAccount())).thenReturn(getValidSpiAccountReference());
        when(accountMapper.mapToSpiAmount(getSufficientFundsConfirmationRequest().getInstructedAmount())).thenReturn(getSufficientSpiAmount());
        when(accountMapper.mapToSpiAmount(getInSufficientFundsConfirmationRequest().getInstructedAmount())).thenReturn(getInsufficientSpiAmount());

        when(fundsConfirmationSpi.peformFundsSufficientCheck(null, getValidSpiAccountReference(), getSufficientSpiAmount(), getAspspConsentData()))
            .thenReturn(new SpiResponse<>(Boolean.TRUE, getAspspConsentData()));
        when(fundsConfirmationSpi.peformFundsSufficientCheck(null, getValidSpiAccountReference(), getInsufficientSpiAmount(), getAspspConsentData()))
            .thenReturn(new SpiResponse<>(Boolean.FALSE, getAspspConsentData()));
    }

    @Test
    public void fundsConfirmation_success() {
        //Given:
        FundsConfirmationResponse successResponse = new FundsConfirmationResponse(true);
        ResponseObject<FundsConfirmationResponse> expected = ResponseObject.<FundsConfirmationResponse>builder()
                                                                 .body(successResponse)
                                                                 .build();

        //When:
        ResponseObject<FundsConfirmationResponse> actual = fundsConfirmationService.fundsConfirmation(getSufficientFundsConfirmationRequest());

        //Then
        assertThat(actual.getBody().isFundsAvailable()).isEqualTo(expected.getBody().isFundsAvailable());
    }

    @Test
    public void fundsConfirmation_notEnoughMoney() {
        //Given:
        FundsConfirmationResponse failureResponse = new FundsConfirmationResponse(false);
        ResponseObject<FundsConfirmationResponse> expected = ResponseObject.<FundsConfirmationResponse>builder()
                                                                 .body(failureResponse)
                                                                 .build();

        //When:
        ResponseObject<FundsConfirmationResponse> actual = fundsConfirmationService.fundsConfirmation(getInSufficientFundsConfirmationRequest());

        //Then
        assertThat(actual.getBody().isFundsAvailable()).isEqualTo(expected.getBody().isFundsAvailable());
    }

    private SpiAmount getSufficientSpiAmount() {
        return new SpiAmount(EUR, new BigDecimal(AMOUNT_160));
    }

    private SpiAmount getInsufficientSpiAmount() {
        return new SpiAmount(EUR, new BigDecimal(AMOUNT_1600));
    }

    private FundsConfirmationRequest getSufficientFundsConfirmationRequest() {
        FundsConfirmationRequest request = new FundsConfirmationRequest();
        request.setPayee("Check24");
        request.setCardNumber("12345");
        request.setInstructedAmount(new Xs2aAmount());
        request.setPsuAccount(new Xs2aAccountReference());
        return request;
    }

    private FundsConfirmationRequest getInSufficientFundsConfirmationRequest() {
        return new FundsConfirmationRequest();
    }

    private AspspConsentData getAspspConsentData() {
        return new AspspConsentData();
    }

    private SpiAccountReference getValidSpiAccountReference() {
        return new SpiAccountReference("DE371234599999",
            "1111111111",
            "1111",
            "23456xxxxxx1234",
            "0172/1111111",
            EUR
        );
    }

    private SpiFundsConfirmationConsent getSpiFundsConfirmationConsent() {
        return new SpiFundsConfirmationConsent();
    }
}
