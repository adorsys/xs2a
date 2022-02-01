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
