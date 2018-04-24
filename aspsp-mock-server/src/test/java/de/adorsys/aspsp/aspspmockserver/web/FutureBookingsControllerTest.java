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

import de.adorsys.aspsp.aspspmockserver.service.FutureBookingsService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FutureBookingsControllerTest {
    private static final String ACCOUNT_ID = "123456789";
    private static final String WRONG_ACCOUNT_ID = "0";

    @Autowired
    private FutureBookingsController futureBookingsController;

    @MockBean(name = "futureBookingsService")
    private FutureBookingsService futureBookingsService;

    @Before
    public void setUp() {
        when(futureBookingsService.changeBalances(ACCOUNT_ID))
            .thenReturn(getSpiAccountDetails());
    }

    @Test
    public void changeBalances_Success() throws Exception {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<SpiAccountDetails> actualResult = futureBookingsController.changeBalances(ACCOUNT_ID);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getBody()).isEqualTo(getSpiAccountDetails());
    }

    private Optional<SpiAccountDetails> getSpiAccountDetails() {
        return Optional.of(new SpiAccountDetails("qwertyuiop12345678", "DE99999999999999", null, "4444333322221111",
            "444433xxxxxx1111", null, Currency.getInstance("EUR"), "Emily", "GIRO",
            null, "ACVB222", null));
    }
}
