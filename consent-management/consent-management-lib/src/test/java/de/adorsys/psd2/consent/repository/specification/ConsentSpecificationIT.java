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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {ConsentSpecificationIT.Initializer.class})
class ConsentSpecificationIT extends BaseTest {

    private static final String INSTANCE_ID = "UNDEFINED";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("987654321", "", "", "", "");
    private static final String ASPSP_ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private ConsentSpecification consentSpecification;

    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private TppInfoRepository tppInfoRepository;

    @BeforeEach
    void setUp() {
        clearData();

        TppInfoEntity tppInfo = tppInfoRepository.save(
            jsonReader.getObjectFromFile("json/specification/tpp-info-entity.json", TppInfoEntity.class));

        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        consentEntity.setConsentType(ConsentType.AIS.getName());
        consentEntity.getTppInformation().setTppInfo(tppInfo);
        consentEntity.getAspspAccountAccesses().get(0).setConsent(consentEntity);

        consentJpaRepository.save(consentEntity);
    }

    @Test
    @Transactional
    void byPsuIdDataAndAspspAccountIdAndInstanceId() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(
                PSU_ID_DATA,
                ASPSP_ACCOUNT_ID,
                INSTANCE_ID
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(ASPSP_ACCOUNT_ID, actualConsent.getAspspAccountAccesses().get(0).getAspspAccountId());
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actualConsent.getInstanceId());
    }
}
