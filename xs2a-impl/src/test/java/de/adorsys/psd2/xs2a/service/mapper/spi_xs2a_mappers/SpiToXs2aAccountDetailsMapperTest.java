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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiToXs2aAccountDetailsMapperTest {
    @InjectMocks
    private SpiToXs2aAccountDetailsMapper spiToXs2aAccountDetailsMapper;

    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
    @Mock
    private SpiToXs2aAddressMapper spiToXs2aAddressMapper;
    @Mock
    private SpiToXs2aAmountMapper spiToXs2aAmountMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aAccountDetails() {
        //Given
        SpiAccountDetails spiAccountDetails = jsonReader.getObjectFromFile("json/SpiAccountDetails.json", SpiAccountDetails.class);
        Xs2aAccountDetails expectedXs2aAccountDetails = jsonReader.getObjectFromFile("json/Xs2aAccountDetails.json", Xs2aAccountDetails.class);

        List<SpiAccountBalance> spiAccountBalances = jsonReader.getObjectFromFile("json/SpiAccountBalanceForAccount.json", new TypeReference<List<SpiAccountBalance>>() {
        });
        List<Xs2aBalance> xs2aBalances = jsonReader.getObjectFromFile("json/Xs2aBalanceForAccount.json", new TypeReference<List<Xs2aBalance>>() {
        });

        when(balanceMapper.mapToXs2aBalanceList(spiAccountBalances)).thenReturn(xs2aBalances);

        //When
        Xs2aAccountDetails actualXs2aAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails);
        //Then
        assertEquals(expectedXs2aAccountDetails, actualXs2aAccountDetails);
    }

    @Test
    void mapToXs2aCardAccountDetails() {
        //Given
        SpiCardAccountDetails spiCardAccountDetails = jsonReader.getObjectFromFile("json/SpiCardAccountDetails.json", SpiCardAccountDetails.class);
        Xs2aCardAccountDetails expectedXs2aCardAccountDetails = jsonReader.getObjectFromFile("json/Xs2aCardAccountDetails.json", Xs2aCardAccountDetails.class);

        List<SpiAccountBalance> spiAccountBalances = jsonReader.getObjectFromFile("json/SpiAccountBalanceForCardAccount.json", new TypeReference<List<SpiAccountBalance>>() {
        });
        List<Xs2aBalance> xs2aBalances = jsonReader.getObjectFromFile("json/Xs2aBalanceForCardAccount.json", new TypeReference<List<Xs2aBalance>>() {
        });

        when(balanceMapper.mapToXs2aBalanceList(spiAccountBalances)).thenReturn(xs2aBalances);
        when(spiToXs2aAmountMapper.mapToXs2aAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal("10000"))))
            .thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "10000"));

        //When
        Xs2aCardAccountDetails actualXs2aCardAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aCardAccountDetails(spiCardAccountDetails);
        //Then
        assertEquals(expectedXs2aCardAccountDetails, actualXs2aCardAccountDetails);
    }

    @Test
    void mapToXs2aAccountDetailsList() {
        //Given
        SpiAccountDetails spiAccountDetails = jsonReader.getObjectFromFile("json/SpiAccountDetails.json", SpiAccountDetails.class);
        Xs2aAccountDetails expectedXs2aAccountDetails = jsonReader.getObjectFromFile("json/Xs2aAccountDetails.json", Xs2aAccountDetails.class);

        List<SpiAccountBalance> spiAccountBalances = jsonReader.getObjectFromFile("json/SpiAccountBalanceForAccount.json", new TypeReference<List<SpiAccountBalance>>() {
        });
        List<Xs2aBalance> xs2aBalances = jsonReader.getObjectFromFile("json/Xs2aBalanceForAccount.json", new TypeReference<List<Xs2aBalance>>() {
        });

        when(balanceMapper.mapToXs2aBalanceList(spiAccountBalances)).thenReturn(xs2aBalances);

        //When
        List<Xs2aAccountDetails> actualXs2aAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aAccountDetailsList(Collections.singletonList(spiAccountDetails));
        //Then
        assertEquals(Collections.singletonList(expectedXs2aAccountDetails), actualXs2aAccountDetails);
    }

    @Test
    void mapToXs2aCardAccountDetailsList() {
        //Given
        SpiCardAccountDetails spiCardAccountDetails = jsonReader.getObjectFromFile("json/SpiCardAccountDetails.json", SpiCardAccountDetails.class);
        Xs2aCardAccountDetails expectedXs2aCardAccountDetails = jsonReader.getObjectFromFile("json/Xs2aCardAccountDetails.json", Xs2aCardAccountDetails.class);

        List<SpiAccountBalance> spiAccountBalances = jsonReader.getObjectFromFile("json/SpiAccountBalanceForCardAccount.json", new TypeReference<List<SpiAccountBalance>>() {
        });
        List<Xs2aBalance> xs2aBalances = jsonReader.getObjectFromFile("json/Xs2aBalanceForCardAccount.json", new TypeReference<List<Xs2aBalance>>() {
        });

        when(balanceMapper.mapToXs2aBalanceList(spiAccountBalances)).thenReturn(xs2aBalances);
        when(spiToXs2aAmountMapper.mapToXs2aAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal("10000"))))
            .thenReturn(new Xs2aAmount(Currency.getInstance("EUR"), "10000"));

        //When
        List<Xs2aCardAccountDetails> actualXs2aCardAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aCardAccountDetailsList(Collections.singletonList(spiCardAccountDetails));
        //Then
        assertEquals(Collections.singletonList(expectedXs2aCardAccountDetails), actualXs2aCardAccountDetails);
    }

    @Test
    void mapToXs2aCardAccountDetailsList_emptyList() {
        //When
        List<Xs2aCardAccountDetails> actualXs2aCardAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aCardAccountDetailsList(Collections.emptyList());
        //Then
        assertEquals(Collections.emptyList(), actualXs2aCardAccountDetails);
    }

    @Test
    void mapToXs2aCardAccountDetailsList_listIsNull() {
        //When
        List<Xs2aCardAccountDetails> actualXs2aCardAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aCardAccountDetailsList(null);
        //Then
        assertEquals(Collections.emptyList(), actualXs2aCardAccountDetails);
    }

    @Test
    void mapToXs2aAccountDetailsList_emptyList() {
        //When
        List<Xs2aAccountDetails> actualXs2aAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aAccountDetailsList(Collections.emptyList());
        //Then
        assertEquals(Collections.emptyList(), actualXs2aAccountDetails);
    }

    @Test
    void mapToXs2aAccountDetailsList_listIsNull() {
        //When
        List<Xs2aAccountDetails> actualXs2aAccountDetails = spiToXs2aAccountDetailsMapper.mapToXs2aAccountDetailsList(null);
        //Then
        assertEquals(Collections.emptyList(), actualXs2aAccountDetails);
    }
}
