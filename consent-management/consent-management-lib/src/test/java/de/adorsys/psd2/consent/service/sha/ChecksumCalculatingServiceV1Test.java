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

package de.adorsys.psd2.consent.service.sha;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.sha.ChecksumConstant;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChecksumCalculatingServiceV1Test {
    private static final String VERSION_01 = "001";
    private static final byte[] WRONG_CHECKSUM = "checksum in consent".getBytes();
    private static final byte[] WRONG_CHECKSUM_WITH_DELIMITER = ("checksum in consent" + ChecksumConstant.DELIMITER).getBytes();
    private static final byte[] CHECKSUM_TPP_ACCESS = getCorrectChecksumForTppAccess().getBytes();
    private static final byte[] CHECKSUM_ASPSP_ACCESS = getCorrectChecksumForAspspAccess().getBytes();
    private JsonReader jsonReader = new JsonReader();

    private final ChecksumCalculatingServiceV1 checksumCalculatingServiceV1 = new ChecksumCalculatingServiceV1();

    @Test
    public void verifyConsentWithChecksum_success_tppAccesses() {
        // given
        AisConsent aisConsent = buildConsentTpp();

        // when
        boolean actualResult = checksumCalculatingServiceV1.verifyConsentWithChecksum(aisConsent, CHECKSUM_TPP_ACCESS);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void verifyConsentWithChecksum_success_aspspAccesses() {
        // given
        AisConsent aisConsent = buildConsentAspsp();

        // when
        boolean actualResult = checksumCalculatingServiceV1.verifyConsentWithChecksum(aisConsent, CHECKSUM_ASPSP_ACCESS);

        // then
        assertThat(actualResult).isTrue();
    }


    @Test
    public void verifyConsentWithChecksum_wrongChecksum() {
        // given
        AisConsent aisConsent = buildConsentTpp();

        // when
        boolean actualResult = checksumCalculatingServiceV1.verifyConsentWithChecksum(aisConsent, WRONG_CHECKSUM);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verifyConsentWithChecksum_wrongChecksumWithDelimiter() {
        // given
        AisConsent aisConsent = buildConsentTpp();

        // when
        boolean actualResult = checksumCalculatingServiceV1.verifyConsentWithChecksum(aisConsent, WRONG_CHECKSUM_WITH_DELIMITER);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void calculateChecksumForConsent_success() {
        // given
        AisConsent aisConsent = buildConsentTpp();

        // when
        byte[] actualResult = checksumCalculatingServiceV1.calculateChecksumForConsent(aisConsent);

        // then
        assertThat(actualResult).isEqualTo(CHECKSUM_TPP_ACCESS);
    }

    @Test
    public void calculateChecksumForConsent_success_aspspAccesses() {
        // given
        AisConsent aisConsent = buildConsentAspsp();

        // when
        byte[] actualResult = checksumCalculatingServiceV1.calculateChecksumForConsent(aisConsent);

        // then
        assertThat(actualResult).isEqualTo(CHECKSUM_ASPSP_ACCESS);
    }

    @Test
    public void getVersion_success() {
        // when
        String actualResult = checksumCalculatingServiceV1.getVersion();

        //then
        assertThat(actualResult).isEqualTo(VERSION_01);
    }

    private AisConsent buildConsentTpp() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_tpp_access.json", AisConsent.class);
    }

    private AisConsent buildConsentAspsp() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_aspsp_access.json", AisConsent.class);
    }

    private static String getCorrectChecksumForTppAccess() {
        return "001_%_RTlVhnh+IXdr5F4RC44JhXajXCyFnKCCSgbAbUiNdrzsGjnc9NoAoxUHzn/9h+wU2J7FoDW4FUND0SY45wv2pg==";
    }

    private static String getCorrectChecksumForAspspAccess() {
        return "001_%_lrMBNAfd99dNeqdulwz6GlN7oJ/83X5/wGQmZT3PgRp+yquN7BmYNWFJE13vVEfGZzefAQBf8t1Bpzgtmy2+Hw==_%_k5zQQpviUpOqkPDTJJowLHPsJlggttsANNcjRD7gjZsZxFjqGnvMC4O7RH/CGeeL1TNlVSogxiVg3I5RnuICbg==";
    }
}
