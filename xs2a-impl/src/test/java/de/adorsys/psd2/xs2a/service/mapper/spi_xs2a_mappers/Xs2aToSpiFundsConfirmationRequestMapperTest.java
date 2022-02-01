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
