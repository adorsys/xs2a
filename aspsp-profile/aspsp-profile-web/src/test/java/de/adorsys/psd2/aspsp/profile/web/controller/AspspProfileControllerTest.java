/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileControllerTest {


    @InjectMocks
    private AspspProfileController aspspProfileController;

    @Mock
    private AspspProfileService aspspProfileService;

    @Before
    public void setUpAccountServiceMock() {
        when(aspspProfileService.getAspspSettings())
            .thenReturn(AspspSettingsBuilder.buildAspspSettings());
        when(aspspProfileService.getScaApproaches())
            .thenReturn(Collections.singletonList(ScaApproach.REDIRECT));
    }

    @Test
    public void getAspspSettings() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        AspspSettings expectedSettings = AspspSettingsBuilder.buildAspspSettings();

        //When:
        ResponseEntity<AspspSettings> actualResponse = aspspProfileController.getAspspSettings();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(expectedSettings);
    }

    @Test
    public void getScaApproach() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<ScaApproach>> actualResponse = aspspProfileController.getScaApproaches();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(Collections.singletonList(ScaApproach.REDIRECT));
    }
}
