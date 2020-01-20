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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.domain.sha.AisConsentSha;
import de.adorsys.psd2.consent.domain.sha.AspspAccountAccessSha;
import de.adorsys.psd2.consent.domain.sha.ChecksumConstant;
import de.adorsys.psd2.consent.domain.sha.TppAccountAccessSha;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChecksumCalculatingServiceV1 implements ChecksumCalculatingService {
    private static final String VERSION = "001";
    private static final Charset CHARSET = Charset.defaultCharset();
    private final Sha512HashingService hashingService = new Sha512HashingService();
    private final ObjectMapper objectMapper = buildObjectMapper();

    @Override
    public boolean verifyConsentWithChecksum(AisConsent consent, byte[] checksum) {
        String checksumStr = new String(checksum);
        String[] elements = checksumStr.split(ChecksumConstant.DELIMITER);

        if (elements.length == 1) {
            return false;
        }

        boolean aisConsentValid = true;
        boolean aspspAccessValid = true;

        if (elements.length > 1) {
            String aisConsentChecksumFromDb = elements[ChecksumConstant.AIS_CONSENT_CHECKSUM_START_POSITION];
            String aisConsentChecksum = calculateChecksumForConsentStr(consent);

            aisConsentValid = aisConsentChecksumFromDb.equals(aisConsentChecksum);
        }

        if (elements.length > 2) {
            String aspspAccessFromDb = elements[ChecksumConstant.ASPSP_ACCESS_CHECKSUM_START_POSITION];
            String aspspAccessChecksum = calculateChecksumForAspspAccessStr(consent.getAspspAccountAccesses());

            aspspAccessValid = aspspAccessFromDb.equals(aspspAccessChecksum);
        }

        return aisConsentValid && aspspAccessValid;
    }

    @Override
    public byte[] calculateChecksumForConsent(AisConsent consent) {
        StringBuilder sb = new StringBuilder(VERSION).append(ChecksumConstant.DELIMITER);

        byte[] aisConsentAsBytes = getBytesFromObject(mapToShaModel(consent));
        byte[] aisConsentChecksum = calculateChecksum(aisConsentAsBytes);
        String aisConsentChecksumString = Base64.getEncoder().encodeToString(aisConsentChecksum);
        sb.append(aisConsentChecksumString);

        List<AspspAccountAccessSha> aspspAccountAccess = mapToAspspAccessSha(consent.getAspspAccountAccesses());
        if (!aspspAccountAccess.isEmpty()) {
            sb.append(ChecksumConstant.DELIMITER);

            byte[] aisAspspAccessBytes = getBytesFromObject(mapToAspspAccessSha(consent.getAspspAccountAccesses()));
            byte[] aisAspspAccessChecksum = calculateChecksum(aisAspspAccessBytes);
            String aisAspspAccessChecksumString = Base64.getEncoder().encodeToString(aisAspspAccessChecksum);
            sb.append(aisAspspAccessChecksumString);
        }

        return sb.toString().getBytes();
    }

    private String calculateChecksumForConsentStr(AisConsent consent) {
        byte[] aisConsentAsBytes = getBytesFromObject(mapToShaModel(consent));
        byte[] aisConsentChecksum = calculateChecksum(aisConsentAsBytes);
        return Base64.getEncoder().encodeToString(aisConsentChecksum);
    }

    private String calculateChecksumForAspspAccessStr(List<AspspAccountAccess> aspspAccountAccesses) {
        byte[] aspspAccessAsBytes = getBytesFromObject(mapToAspspAccessSha(aspspAccountAccesses));
        byte[] aspspAccessChecksum = calculateChecksum(aspspAccessAsBytes);
        return Base64.getEncoder().encodeToString(aspspAccessChecksum);
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    private AisConsentSha mapToShaModel(AisConsent consent) {
        AisConsentSha aisConsentSha = new AisConsentSha();
        aisConsentSha.setRecurringIndicator(consent.isRecurringIndicator());
        aisConsentSha.setCombinedServiceIndicator(consent.isCombinedServiceIndicator());
        aisConsentSha.setExpireDate(consent.getValidUntil());
        aisConsentSha.setTppFrequencyPerDay(consent.getTppFrequencyPerDay());
        aisConsentSha.setAccesses(mapToTppAccessSha(consent.getAccesses()));
        return aisConsentSha;
    }

    private List<TppAccountAccessSha> mapToTppAccessSha(List<TppAccountAccess> accountAccesses) {
        return accountAccesses.stream()
                   .map(acc -> new TppAccountAccessSha(acc.getAccountIdentifier(),
                                                       Optional.ofNullable(acc.getCurrency())
                                                           .map(Currency::toString).orElse(null),
                                                       Optional.ofNullable(acc.getTypeAccess())
                                                           .map(Enum::name).orElse(null),
                                                       Optional.ofNullable(acc.getAccountReferenceType())
                                                           .map(Enum::name).orElse(null)))
                   .collect(Collectors.toList());
    }

    private List<AspspAccountAccessSha> mapToAspspAccessSha(List<AspspAccountAccess> accountAccesses) {
        return accountAccesses.stream()
                   .map(acc -> new AspspAccountAccessSha(
                       acc.getAccountIdentifier(),
                       Optional.ofNullable(acc.getCurrency())
                           .map(Currency::toString).orElse(null),
                       Optional.ofNullable(acc.getTypeAccess())
                           .map(Enum::name).orElse(null),
                       Optional.ofNullable(acc.getAccountReferenceType())
                           .map(Enum::name).orElse(null),
                       acc.getResourceId(),
                       acc.getAspspAccountId()))
                   .filter(AspspAccountAccessSha::isNotEmpty)
                   .collect(Collectors.toList());
    }

    private byte[] calculateChecksum(byte[] checksumSource) {
        return hashingService.hash(checksumSource, CHARSET);
    }

    private byte[] getBytesFromObject(Object inputValue) {
        try {
            return objectMapper.writeValueAsBytes(inputValue);
        } catch (JsonProcessingException e) {
            return new byte[0];
        }
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper localObjectMapper = new ObjectMapper();
        localObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        localObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        localObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        localObjectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        return localObjectMapper;
    }
}
