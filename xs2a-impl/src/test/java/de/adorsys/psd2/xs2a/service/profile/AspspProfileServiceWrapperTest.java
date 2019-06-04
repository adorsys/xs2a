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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileServiceWrapperTest {
    private static final String ASPSP_SETTINGS_JSON_PATH = "json/service/profile/AspspSettings.json";

    @Mock
    private AspspProfileService aspspProfileService;

    @InjectMocks
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Before
    public void setUp() {
        when(aspspProfileService.getAspspSettings())
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
    }

    @Test
    public void getAvailableBookingStatuses() {
        // Given
        BookingStatus bookingStatus = BookingStatus.BOOKED;

        // When
        List<BookingStatus> actualAvailableStatuses = aspspProfileServiceWrapper.getAvailableBookingStatuses();

        // Then
        assertEquals(Collections.singletonList(bookingStatus), actualAvailableStatuses);
    }
}
