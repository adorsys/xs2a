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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingServiceV1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChecksumCalculatingFactoryTest {
    private final ChecksumCalculatingFactory factory = new ChecksumCalculatingFactory();
    private final ChecksumCalculatingServiceV1 v001 = new ChecksumCalculatingServiceV1();
    private static final byte[] CHECKSUM = Base64.getDecoder().decode(getCorrectChecksum());
    private static final byte[] WRONG_CHECKSUM = "wrong checksum in consent".getBytes();

    @Test
    public void getServiceByChecksum_success() {
        // when
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(CHECKSUM);

        //then
        assertThat(actualResult.isPresent()).isEqualTo(true);
        assertThat(actualResult.get().getVersion()).isEqualTo(v001.getVersion());
    }

    @Test
    public void getServiceByChecksum_emptyChecksum() {
        // when
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(new byte[0]);

        //then
        assertThat(actualResult.isPresent()).isEqualTo(false);
    }

    @Test
    public void getServiceByChecksum_wrongChecksum() {
        // when
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(WRONG_CHECKSUM);

        //then
        assertThat(actualResult.isPresent()).isEqualTo(false);
    }

    @Test
    public void getServiceByChecksum_nullChecksum() {
        // when
        Optional<ChecksumCalculatingService> actualResult = factory.getServiceByChecksum(null);

        //then
        assertThat(actualResult.isPresent()).isEqualTo(true);
        assertThat(actualResult.get().getVersion()).isEqualTo(v001.getVersion());
    }

    private static String getCorrectChecksum() {
        return "MDAxXyVfY1ZQa05GWnV0NjdLTTQ0aEdheHUrWXpKZWNLODN2QTRKY0huMDV3M0hGbUI0alhycmR3S1VEcDRhZEo2aTJMK3dDUU9UOGJiWmNSQ1UrRUpWcDJ5d0E9PV8lX003akNvS1JNc1FWd2svcDhRR0JNWjNvL0VBTDRNQUZ0SStrek9qQUVMNlAxeUJIajh4bHlGa0hUb21FUEYyOEFpMEhyTUlEalQ1NGVydWVwT2ZTdmxBPT0=";
    }
}
