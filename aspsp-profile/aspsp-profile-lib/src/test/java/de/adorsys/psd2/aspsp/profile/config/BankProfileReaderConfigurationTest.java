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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class BankProfileReaderConfigurationTest {
    private BankProfileReaderConfiguration bankProfileReaderConfiguration;

    @Before
    public void setUp() {
        bankProfileReaderConfiguration = new BankProfileReaderConfiguration();
        bankProfileReaderConfiguration.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    public void profileConfigurationWithAdditionalFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReaderConfiguration.profileConfiguration();

        Whitebox.setInternalState(bankProfileReaderConfiguration,
                                  "customBankProfile",
                                  "classpath:bank_profile_additional_fields.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReaderConfiguration.profileConfiguration();


        assertEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
    public void profileConfigurationWithoutUsualFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReaderConfiguration.profileConfiguration();

        Whitebox.setInternalState(bankProfileReaderConfiguration,
                                  "customBankProfile",
                                  "classpath:bank_profile_missing_fields.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReaderConfiguration.profileConfiguration();


        assertNotEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }
}
