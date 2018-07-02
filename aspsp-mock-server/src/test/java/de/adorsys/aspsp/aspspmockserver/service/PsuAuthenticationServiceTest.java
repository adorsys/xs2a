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

import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PsuAuthenticationServiceTest {
    private static final String PSU_ID_1 = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String PSU_ID_2 = "ad918c89-4346-4f16-b5c8-d781b040200c";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String TAN_ID = "2d4b403b-f5f5-41c0-847f-b6abf1edb102";
    private static final String TAN_NUMBER = "123456";
    private static final String WRONG_TAN_NUMBER = "wrong tan number";

    @Autowired
    PsuAuthenticationService psuAuthenticationService;

    @MockBean
    private TanRepository tanRepository;
    @MockBean
    private PsuRepository psuRepository;

    @Before
    public void setUp() {
        when(psuRepository.findOne(PSU_ID_1))
            .thenReturn(getPsu1());
        when(psuRepository.findOne(PSU_ID_2))
            .thenReturn(getPsu2());
        when(psuRepository.findOne(WRONG_PSU_ID))
            .thenReturn(null);
        when(tanRepository.save(any(Tan.class)))
            .thenReturn(getUnusedTan());
        when(tanRepository.findByPsuIdAndTanStatus(PSU_ID_1, TanStatus.UNUSED))
            .thenReturn(Optional.of(getUnusedTan()));
        when(tanRepository.findByPsuIdAndTanStatus(PSU_ID_2, TanStatus.UNUSED))
            .thenReturn(Optional.empty());
    }

    @Test
    public void generateTanForPsu_Failure() {
        //When
        boolean actualResult = psuAuthenticationService.generateAndSendTanForPsu(WRONG_PSU_ID);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuTanNumberValid_Success() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID_1, TAN_NUMBER);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuTanNumberValid_Failure() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID_1, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuTanNumberValid_TanStatusValid() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID_2, TAN_NUMBER);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuTanNumberValid_TanStatusInvalid() {
        //When
        boolean actualResult = psuAuthenticationService.isPsuTanNumberValid(PSU_ID_1, WRONG_TAN_NUMBER);

        //Then
        assertThat(actualResult).isFalse();
    }

    private Psu getPsu1() {
        return new Psu(PSU_ID_1, "test1@gmail.com", null);
    }

    private Psu getPsu2() {
        return new Psu(PSU_ID_2, "test2@gmail.com", null);
    }

    private Tan getUnusedTan() {
        return new Tan(TAN_ID, PSU_ID_1, TAN_NUMBER, TanStatus.UNUSED);
    }
}
