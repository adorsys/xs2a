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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.consent.domain.account.AccountAccess;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.domain.sha.AisConsentSha;
import de.adorsys.psd2.consent.domain.sha.AspspAccountAccessSha;
import de.adorsys.psd2.consent.domain.sha.ChecksumConstant;
import de.adorsys.psd2.consent.domain.sha.TppAccountAccessSha;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChecksumCalculatingServiceV2 implements ChecksumCalculatingService {
    private static final String VERSION = "002";
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
            Map<AccountReferenceType, String> accessChecksumMapFromDB = getChecksumMapFromEncodedString(aspspAccessFromDb);
            Map<AccountReferenceType, String> currentAccessChecksumMap = calculateChecksumMapByReferenceType(consent.getAspspAccountAccesses());

            aspspAccessValid = areCurrentAccessesValid(accessChecksumMapFromDB, currentAccessChecksumMap);
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

            String aspspAccessChecksumEncodedString = mapAspspAccessesToChecksumEncodedString(consent.getAspspAccountAccesses());
            sb.append(aspspAccessChecksumEncodedString);
        }

        return sb.toString().getBytes();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    private String mapAspspAccessesToChecksumEncodedString(List<AspspAccountAccess> aspspAccesses) {
        Map<AccountReferenceType, String> checksumMap = calculateChecksumMapByReferenceType(aspspAccesses);
        byte[] checksumMapInBytes = getBytesFromObject(checksumMap);

        return Base64.getEncoder().encodeToString(checksumMapInBytes);
    }

    private Map<AccountReferenceType, String> getChecksumMapFromEncodedString(String aspspAccessFromDb) {
        byte[] decodedString = Base64.getDecoder().decode(aspspAccessFromDb);

        return getChecksumMapFromBytes(decodedString);
    }

    private boolean areCurrentAccessesValid(Map<AccountReferenceType, String> accessMapFromDB, Map<AccountReferenceType, String> currentAccessMap) {
        return accessMapFromDB.entrySet().stream()
                   .allMatch(ent ->
                                 Optional.ofNullable(currentAccessMap.get(ent.getKey()))
                                     .map(v -> v.equals(ent.getValue()))
                                     .orElse(false));
    }

    private String calculateChecksumForConsentStr(AisConsent consent) {
        byte[] aisConsentAsBytes = getBytesFromObject(mapToShaModel(consent));
        byte[] aisConsentChecksum = calculateChecksum(aisConsentAsBytes);
        return Base64.getEncoder().encodeToString(aisConsentChecksum);
    }

    private Map<AccountReferenceType, String> calculateChecksumMapByReferenceType(List<AspspAccountAccess> aspspAccountAccesses) {
        Map<AccountReferenceType, String> checkSumMap = new LinkedHashMap<>();
        List<AspspAccountAccess> actualAccess = getClosedAccess(aspspAccountAccesses);

        for (AccountReferenceType type : AccountReferenceType.values()) {
            List<AspspAccountAccess> filtered = filterByReferenceTypeSorted(actualAccess, type);

            if (CollectionUtils.isEmpty(filtered)) {
                continue;
            }

            String accessChecksumByType = calculateChecksumForAccesses(filtered);
            checkSumMap.put(type, accessChecksumByType);
        }

        return checkSumMap;
    }

    private List<AspspAccountAccess> filterByReferenceTypeSorted(List<AspspAccountAccess> aspspAccountAccesses, AccountReferenceType type) {
        return aspspAccountAccesses.stream()
                   .filter(acc -> acc.getAccountReferenceType() == type)
                   .sorted(Comparator.comparing(AccountAccess::getTypeAccess)
                               .thenComparing(AccountAccess::getAccountIdentifier)
                               .thenComparing(acc-> Optional.ofNullable(acc.getCurrency())
                                                      .map(Currency::getCurrencyCode)
                                                      .orElse(null)))
                   .collect(Collectors.toList());
    }

    private List<AspspAccountAccess> getClosedAccess(List<AspspAccountAccess> aspspAccountAccesses) {
        return aspspAccountAccesses.stream()
                   .filter(acc -> StringUtils.isNotBlank(acc.getResourceId()) || StringUtils.isNotBlank(acc.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

    private String calculateChecksumForAccesses(List<AspspAccountAccess> aspspAccountAccesses) {
        byte[] aspspAccessAsBytes = getBytesFromObject(mapToAspspAccessSha(aspspAccountAccesses));
        byte[] aspspAccessChecksum = calculateChecksum(aspspAccessAsBytes);
        return Base64.getEncoder().encodeToString(aspspAccessChecksum);
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

    private Map<AccountReferenceType, String> getChecksumMapFromBytes(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, new TypeReference<LinkedHashMap<AccountReferenceType, String>>() {
            });
        } catch (IOException e) {
            return Collections.emptyMap();
        }
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
        localObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        localObjectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        return localObjectMapper;
    }
}
