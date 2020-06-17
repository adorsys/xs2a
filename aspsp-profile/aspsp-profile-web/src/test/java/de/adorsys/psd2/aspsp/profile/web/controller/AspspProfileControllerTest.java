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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileControllerTest {

    @InjectMocks
    private AspspProfileController aspspProfileController;

    @Mock
    private AspspProfileService aspspProfileService;

    @Test
    void getAspspSettings() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        when(aspspProfileService.getAspspSettings(""))
            .thenReturn(AspspSettingsBuilder.buildAspspSettings());

        //When:
        ResponseEntity<AspspSettings> actualResponse = aspspProfileController.getAspspSettings("");

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);

        AspspSettings expectedSettings = AspspSettingsBuilder.buildAspspSettings();
        assertThat(actualResponse.getBody()).isEqualTo(expectedSettings);
    }

    @Test
    void getScaApproach() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        when(aspspProfileService.getScaApproaches(""))
            .thenReturn(Collections.singletonList(ScaApproach.REDIRECT));

        //When:
        ResponseEntity<List<ScaApproach>> actualResponse = aspspProfileController.getScaApproaches("");

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(Collections.singletonList(ScaApproach.REDIRECT));
    }
}
