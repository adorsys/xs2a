/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChecksumCalculatingServiceV2Test {
    private static final String VERSION_02 = "002";
    private static final byte[] WRONG_CHECKSUM = "checksum in consent".getBytes();
    private static final byte[] WRONG_CHECKSUM_WITH_DELIMITER = ("checksum in consent" + ChecksumConstant.DELIMITER).getBytes();
    private static final byte[] CHECKSUM_TPP_ACCESS_IBAN = getCorrectChecksumForTppAccessIban().getBytes();
    private static final byte[] CHECKSUM_ASPSP_ACCESS_IBAN = getCorrectChecksumForAspspAccessIban().getBytes();

    private static final byte[] CHECKSUM_TPP_ACCESS_IBAN_MASKEDPAN = getCorrectChecksumForTppAccessIbanAndMaskedPan().getBytes();
    private static final byte[] CHECKSUM_ASPSP_ACCESS_IBAN_MASKEDPAN = getCorrectChecksumForAspspAccessIbanAndMaskedPan().getBytes();
    private JsonReader jsonReader = new JsonReader();

    private final ChecksumCalculatingServiceV2 checksumCalculatingServiceV2 = new ChecksumCalculatingServiceV2();

    @Test
    void verifyConsentWithChecksum_success_tppAccesses() {
        // given
        AisConsent aisConsent = buildConsentTppIban();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, CHECKSUM_TPP_ACCESS_IBAN);

        // then
        assertTrue(actualResult);
    }

    @Test
    void verifyConsentWithChecksum_success_aspspAccesses() {
        // given
        AisConsent aisConsent = buildConsentAspspIban();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, CHECKSUM_ASPSP_ACCESS_IBAN);

        // then
        assertTrue(actualResult);
    }


    @Test
    void verifyConsentWithChecksum_wrongChecksum() {
        // given
        AisConsent aisConsent = buildConsentTppIban();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, WRONG_CHECKSUM);

        // then
        assertFalse(actualResult);
    }

    @Test
    void verifyConsentWithChecksum_wrongChecksumWithDelimiter() {
        // given
        AisConsent aisConsent = buildConsentTppIban();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, WRONG_CHECKSUM_WITH_DELIMITER);

        // then
        assertFalse(actualResult);
    }

    @Test
    void calculateChecksumForConsent_success() {
        // given
        AisConsent aisConsent = buildConsentTppIban();

        // when
        byte[] actualResult = checksumCalculatingServiceV2.calculateChecksumForConsent(aisConsent);

        // then
        assertArrayEquals(CHECKSUM_TPP_ACCESS_IBAN, actualResult);
    }

    @Test
    void calculateChecksumForConsent_success_aspspAccesses() {
        // given
        AisConsent aisConsent = buildConsentAspspIban();

        // when
        byte[] actualResult = checksumCalculatingServiceV2.calculateChecksumForConsent(aisConsent);

        // then
        assertArrayEquals(CHECKSUM_ASPSP_ACCESS_IBAN, actualResult);
    }

    @Test
    void verifyConsentWithChecksum_success_tppAccesses_IbanAndMaskedPan() {
        // given
        AisConsent aisConsent = buildConsentTppIbanAndMaskedPan();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, CHECKSUM_TPP_ACCESS_IBAN_MASKEDPAN);

        // then
        assertTrue(actualResult);
    }

    @Test
    void verifyConsentWithChecksum_success_aspspAccesses_IbanAndMaskedPan() {
        // given
        AisConsent aisConsent = buildConsentAspspIbanAndMaskedPan();

        // when
        boolean actualResult = checksumCalculatingServiceV2.verifyConsentWithChecksum(aisConsent, CHECKSUM_ASPSP_ACCESS_IBAN_MASKEDPAN);

        // then
        assertTrue(actualResult);
    }

    @Test
    void calculateChecksumForConsent_successIbanAndMaskedPan() {
        // given
        AisConsent aisConsent = buildConsentTppIbanAndMaskedPan();

        // when
        byte[] actualResult = checksumCalculatingServiceV2.calculateChecksumForConsent(aisConsent);

        // then
        assertArrayEquals(CHECKSUM_TPP_ACCESS_IBAN_MASKEDPAN, actualResult);
    }

    @Test
    void calculateChecksumForConsent_success_aspspAccessesIbanAndMaskedPan() {
        // given
        AisConsent aisConsent = buildConsentAspspIbanAndMaskedPan();

        // when
        byte[] actualResult = checksumCalculatingServiceV2.calculateChecksumForConsent(aisConsent);

        // then
        assertArrayEquals(CHECKSUM_ASPSP_ACCESS_IBAN_MASKEDPAN, actualResult);
    }

    @Test
    void getVersion() {
        // when
        String actualResult = checksumCalculatingServiceV2.getVersion();

        //then
        assertEquals(VERSION_02, actualResult);
    }

    private AisConsent buildConsentTppIban() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_tpp_access.json", AisConsent.class);
    }

    private AisConsent buildConsentAspspIban() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_aspsp_access.json", AisConsent.class);
    }

    private static String getCorrectChecksumForTppAccessIban() {
        return "002_%_bjcOzwEA/NUHWnrfp2FkrgrFQis95it6p1K7JPtWUghxO3TsNLFkfRUOcKTTlZxUBP+rauvPXpc7hoZX2lCnMA==";
    }

    private static String getCorrectChecksumForAspspAccessIban() {
        return "002_%_ywb10IC/RWqq6rinq1lcTDb0hobDIMWUCRZOVToY7XtjD558y49ngaM0kT4iZSt9y2hCpHy8Gd3RQTcuJHnuHA==_%_eyJpYmFuIjoiM0hsNjJKRloxQTF1c2JSN2l0VTlnWHRWcGN1QTFFcDRnMWRUWllUTzRzcDY2czNia3pFU1RqYUE3dXkxV2ZOOXhYNEI4bU5lYlg0MEl3UllaTGp2TUE9PSJ9";
    }

    private AisConsent buildConsentTppIbanAndMaskedPan() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_tpp_access_iban&maskedpan.json", AisConsent.class);
    }

    private AisConsent buildConsentAspspIbanAndMaskedPan() {
        return jsonReader.getObjectFromFile("json/dedicated-ais-consent_aspsp_access_iban&maskedpan.json", AisConsent.class);
    }

    private static String getCorrectChecksumForTppAccessIbanAndMaskedPan() {
        return "002_%_ZxeY31kWWr9IHcxNXXNWACxxrQmNAncbydvWXwjsy0X6hibviiPglCTTKss0/CbBm8UKQbiDM2/RE4BkC5NICw==";
    }

    private static String getCorrectChecksumForAspspAccessIbanAndMaskedPan() {
        return "002_%_WSryPk+OMeMCuNhSG5SNOIGI+4DgqjZ+ld8UpXaurGI3aNquLaqL1zddtnqndp7VwymRESHpjkfhVMkO1Cwueg==_%_eyJpYmFuIjoiV0g4MGZ4ZkE4em01bnZRR1d6bEZXc2pLMlhZQU1keWh0azZaZjlEMXVuNDZNb1JFMXpRemlQOS9NSy9aQkM0R3F4MHpCY3h5Mm0vdnkzNmcxSDJSNmc9PSIsIm1hc2tlZFBhbiI6Ik54YmxHL3JyNDRMN3VMNHd6bEVtOHhwa3pLMGVIVnQ4bGpWbEpzVmNkR3lYdmR5dVR4dVJhMTltS1poOUM0T2hjSkRCZ3lhWXRUcVBZQXFHdnNJKzB3PT0ifQ==";
    }

}
