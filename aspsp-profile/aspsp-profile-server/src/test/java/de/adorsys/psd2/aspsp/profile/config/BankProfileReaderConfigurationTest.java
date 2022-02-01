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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.AspspProfileApplication;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = AspspProfileApplication.class)
@TestPropertySource(properties = {"xs2a.bank_profile.path = classpath:bank_profile_no_sca_redirect_flow.yml"})
class BankProfileReaderConfigurationTest {

    private static final String INSTANCE_ID = "bank1";

    @Autowired
    private BankProfileReaderConfiguration bankProfileReaderConfiguration;

    @Test
    void profileConfigurationDefaultScaRedirectFlow() {
        //Given
        //When
        ProfileConfigurations profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, profileConfiguration.getSetting(INSTANCE_ID).getCommon().getScaRedirectFlow());
    }
}
