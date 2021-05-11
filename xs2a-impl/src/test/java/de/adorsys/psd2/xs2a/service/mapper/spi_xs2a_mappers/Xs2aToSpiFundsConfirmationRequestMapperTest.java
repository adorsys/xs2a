/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiFundsConfirmationRequestMapper.class,
    Xs2aToSpiAccountReferenceMapper.class,
    Xs2aToSpiAmountMapper.class})
class Xs2aToSpiFundsConfirmationRequestMapperTest {

    @Autowired
    private Xs2aToSpiFundsConfirmationRequestMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiFundsConfirmationRequest() {
        //Given
        SpiFundsConfirmationRequest expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-funds-confirmation-request-expected.json", SpiFundsConfirmationRequest.class);

        //When
        SpiFundsConfirmationRequest actual = mapper.mapToSpiFundsConfirmationRequest(getTestFundsConfirmationrequest());

        //Then
        assertThat(actual)
            .isNotNull()
            .isEqualTo(expected);
    }

    private FundsConfirmationRequest getTestFundsConfirmationrequest() {
        FundsConfirmationRequest confirmationRequest =  new FundsConfirmationRequest();
        confirmationRequest.setPsuAccount(getTestPsuAccount());
        confirmationRequest.setInstructedAmount(new Xs2aAmount(Currency.getInstance("EUR"), "11"));
        confirmationRequest.setCardNumber("cardNumber");
        confirmationRequest.setPayee("test payee");
        return confirmationRequest;
    }

    private AccountReference getTestPsuAccount() {
        return new AccountReference(AccountReferenceType.IBAN,
            "55",
            Currency.getInstance("EUR"),
            "resourceId",
            "aspspAccountId");
    }
}
