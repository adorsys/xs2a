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


import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV3;
import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV4;
import de.adorsys.psd2.consent.service.sha.impl.AisChecksumCalculatingServiceV5;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecksumCalculatingFactory {
    private final MultiKeyMap<String, ChecksumCalculatingService> services = new MultiKeyMap<>();

    @Autowired
    private AisChecksumCalculatingServiceV3 aisV3;
    @Autowired
    private AisChecksumCalculatingServiceV4 aisV4;
    @Autowired
    private AisChecksumCalculatingServiceV5 aisV5;
    @Autowired
    private NoProcessingChecksumService noProcessingService;

    @PostConstruct
    public void init() {
        // for deprecated services will use noProcessingService
        services.put(new MultiKey<>("001", ConsentType.AIS.getName()), noProcessingService);
        services.put(new MultiKey<>("002", ConsentType.AIS.getName()), noProcessingService);

        services.put(new MultiKey<>(aisV3.getVersion(), ConsentType.AIS.getName()), aisV3);
        services.put(new MultiKey<>(aisV4.getVersion(), ConsentType.AIS.getName()), aisV4);
        services.put(new MultiKey<>(aisV5.getVersion(), ConsentType.AIS.getName()), aisV5);
    }

    /**
     * Provides an appropriate checksum calculator by checksum and consent type
     *
     * @param checksum    Data with calculator version info
     * @param consentType Type of consent
     * @return Optional value  of  checksum calculating service
     */
    public Optional<ChecksumCalculatingService> getServiceByChecksum(byte[] checksum, ConsentType consentType) {
        if (checksum == null) {
            log.debug("Checksum is NULL");
            return getDefaultService(consentType);
        }

        String checksumStr = new String(checksum);
        String[] elements = checksumStr.split(ChecksumConstant.DELIMITER);

        String versionSting = elements[ChecksumConstant.VERSION_START_POSITION];
        Optional<ChecksumCalculatingService> checksumCalculatingServiceOptional = Optional.ofNullable(services.get(new MultiKey<>(versionSting, consentType.getName())));

        if (checksumCalculatingServiceOptional.isEmpty()) {
            log.info("Unknown version: [{}] ", versionSting);
        }
        return checksumCalculatingServiceOptional;
    }

    private Optional<ChecksumCalculatingService> getDefaultService(ConsentType consentType) {
        if (ConsentType.AIS == consentType) {
            return Optional.of(aisV5);
        }
        log.info("Given consent type `[{}]` is not supported.", consentType);
        return Optional.empty();
    }
}
