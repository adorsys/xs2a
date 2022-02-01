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

package de.adorsys.psd2.consent.service.sha;

import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV4;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AisChecksumCalculatingServiceV4Test {
    private static final byte[] CORRECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS = getCorrectChecksumForMultipleAccounts().getBytes();
    private final AisChecksumCalculatingServiceV4 aisChecksumCalculatingServiceV4 = new AisChecksumCalculatingServiceV4();

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void verifyConsentWithChecksum_multipleAccounts() {
        // given
        AisConsent aisConsent = buildConsentTppIbanMultiple();

        // when
        boolean actualResult = aisChecksumCalculatingServiceV4.verifyConsentWithChecksum(aisConsent, CORRECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS);

        // then
        assertTrue(actualResult);
    }

    @Test
    void verifyConsentWithChecksum_multipleAccounts1() {
        // given
        AisConsent aisConsent = buildConsentTppIbanMultipleMixed();

        // when
        boolean actualResult = aisChecksumCalculatingServiceV4.verifyConsentWithChecksum(aisConsent, CORRECT_CHECKSUM_FOR_MULTIPLE_ACCOUNTS);

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
