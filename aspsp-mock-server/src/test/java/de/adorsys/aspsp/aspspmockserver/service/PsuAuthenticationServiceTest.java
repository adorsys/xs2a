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

import de.adorsys.aspsp.aspspmockserver.repository.EmailTanRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Tan;
import de.adorsys.aspsp.xs2a.spi.domain.psu.TanStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PsuAuthenticationServiceTest {
    private static final String PSU_ID = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String TAN_ID = "2d4b403b-f5f5-41c0-847f-b6abf1edb102";
    private static final int TAN_NUMBER = 123456;
    private static final int WRONG_TAN_NUMBER = 0;

    @Autowired
    PsuAuthenticationService psuAuthenticationService;

    @MockBean
    private EmailTanRepository emailTanRepository;
    @MockBean
    private PsuRepository psuRepository;

    @Before
    public void setUp() {
        when(psuRepository.findOne(PSU_ID))
            .thenReturn(getPsu());
        when(psuRepository.findOne(WRONG_PSU_ID))
            .thenReturn(null);
        when(emailTanRepository.save(any(Tan.class)))
            .thenReturn(getUnusedTan());
        when(emailTanRepository.findTansByPsuIdIn(PSU_ID))
            .thenReturn(Collections.singletonList(getUnusedTan()));

    }

    @Test
    public void generateTanForPsu_Success() {
        //When
        String actualResult = psuAuthenticationService.generateAndSendTanForPsu(PSU_ID);

        //Then
        assertThat(actualResult).isNotNull();
    }

    @Test
    public void generateTanForPsu_Failure() {
        //When
        String actualResult = psuAuthenticationService.generateAndSendTanForPsu(WRONG_PSU_ID);

        //Then
        assertThat(actualResult).isNull();
    }

    @Test
    public void isPsuTanNumberValid_Success() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID, TAN_NUMBER);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuTanNumberValid_Failure() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult).isFalse();
    }

    private Psu getPsu() {
        return new Psu(PSU_ID, "test@gmail.com", null);
    }

    private Tan getUnusedTan() {
        return new Tan(TAN_ID, PSU_ID, TAN_NUMBER, TanStatus.UNUSED);
    }
}
