/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AisChecksumCalculatingServiceV4Test {
    private static final byte[] C0RECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS = getCorrectChecksumForMultipleAccounts().getBytes();
    private final AisChecksumCalculatingServiceV4 aisChecksumCalculatingServiceV4 = new AisChecksumCalculatingServiceV4();

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void verifyConsentWithChecksum_multipleAccounts() {
        // given
        AisConsent aisConsent = buildConsentTppIbanMultiple();

        // when
        boolean actualResult = aisChecksumCalculatingServiceV4.verifyConsentWithChecksum(aisConsent, C0RECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS);

        // then
        assertTrue(actualResult);
    }

    @Test
    void verifyConsentWithChecksum_multipleAccounts1() {
        // given
        AisConsent aisConsent = buildConsentTppIbanMultipleMixed();

        // when
        boolean actualResult = aisChecksumCalculatingServiceV4.verifyConsentWithChecksum(aisConsent, C0RECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS);

        // then
        assertTrue(actualResult);
    }

    private AisConsent buildConsentTppIbanMultiple() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent-multiple-accounts.json", AisConsent.class);
    }

    private AisConsent buildConsentTppIbanMultipleMixed() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent-multiple-accounts-mixed.json", AisConsent.class);
    }

    private static String getCorrectChecksumForMultipleAccounts() {
        return "003_%_Mj7nHAyka6LdA3zDA0e4TQU0283X1iFDGyJSRePNNpaRlHeuAG5a24vid4K/5jNIWyTsaEo4JZSPmYkiSJ5YoA==_%_eyJpYmFuIjoiUjBqQWVubFFJcm0zR2JNdG9qN2FvWDZWRXdXRmNhM3NMVnROUFVkOFV2T1RLSUpqUmhkb0tIdGpvamxBQTFFSzdVUVhMQ1cwZU53NkZ6TG1jQk9vUUE9PSJ9";
    }
}
