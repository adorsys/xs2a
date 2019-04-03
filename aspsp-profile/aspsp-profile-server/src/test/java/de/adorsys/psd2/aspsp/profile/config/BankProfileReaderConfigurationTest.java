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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.AspspProfileApplication;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = AspspProfileApplication.class)
@TestPropertySource(properties = {"bank_profile.path = classpath:bank_profile_no_sca_redirect_flow.yml"})
public class BankProfileReaderConfigurationTest {
    @Autowired
    private BankProfileReaderConfiguration bankProfileReaderConfiguration;

    @Test
    public void profileConfigurationDefaultScaRedirectFlow() {
        //Given
        //When
        ProfileConfiguration profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, profileConfiguration.getSetting().getScaRedirectFlow());
    }
}
