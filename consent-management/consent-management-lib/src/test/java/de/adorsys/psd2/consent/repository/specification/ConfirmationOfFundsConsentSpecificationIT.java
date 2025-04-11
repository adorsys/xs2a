/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration-test")
@EnableJpaRepositories(basePackages = "de.adorsys.psd2.consent.repository")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {ConfirmationOfFundsConsentSpecificationIT.Initializer.class})
class ConfirmationOfFundsConsentSpecificationIT extends BaseTest {

    private static final String INSTANCE_ID = "UNDEFINED";

    private final ConfirmationOfFundsConsentSpecification consentSpecification;
    private final ConsentJpaRepository consentJpaRepository;
    private final TppInfoRepository tppInfoRepository;

    @Autowired
    public ConfirmationOfFundsConsentSpecificationIT(ConfirmationOfFundsConsentSpecification consentSpecification,
                                                     ConsentJpaRepository consentJpaRepository,
                                                     TppInfoRepository tppInfoRepository) {
        this.consentSpecification = consentSpecification;
        this.consentJpaRepository = consentJpaRepository;
        this.tppInfoRepository = tppInfoRepository;
    }

    private TppInfoEntity tppInfo;
    private ConsentEntity piisAspspConsent;
    private ConsentEntity piisTppConsent;

    @BeforeEach
    void setUp() {
        clearData();
        // Save the TppInfoEntity using test JSON data.
        tppInfo = tppInfoRepository.save(
            jsonReader.getObjectFromFile("json/specification/tpp-info-entity.json", TppInfoEntity.class)
        );

        // Initialize and persist the ASPSP consent entity.
        piisAspspConsent = createAndPrepareConsent(
            "json/specification/consent-entity.json",
            1L,
            ConsentType.PIIS_ASPSP.getName(),
            tppInfo
        );
        piisAspspConsent = consentJpaRepository.save(piisAspspConsent);

        // Initialize and persist the TPP consent entity.
        piisTppConsent = createAndPrepareConsent(
            "json/specification/consent-entity.json",
            2L,
            ConsentType.PIIS_TPP.getName(),
            tppInfo
        );
        piisTppConsent = consentJpaRepository.save(piisTppConsent);
    }

    /**
     * Helper method to load, update, and prepare a ConsentEntity for persistence.
     *
     * @param filePath    the JSON file path for the consent data.
     * @param id          unique identifier to assign.
     * @param consentType type of the consent.
     * @param tppInfo     the associated TppInfoEntity.
     * @return a fully initialized ConsentEntity instance.
     */
    private ConsentEntity createAndPrepareConsent(String filePath, Long id, String consentType, TppInfoEntity tppInfo) {
        ConsentEntity consent = jsonReader.getObjectFromFile(filePath, ConsentEntity.class);
        consent.setId(id);
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentType(consentType);

        // Safely set TPP information if available.
        if (consent.getTppInformation() != null) {
            consent.getTppInformation().setTppInfo(tppInfo);
        }

        // Set the relationship for each account access if the list is not null.
        if (consent.getAspspAccountAccesses() != null) {
            consent.getAspspAccountAccesses().forEach(access -> access.setConsent(consent));
        }
        return consent;
    }

    @Test
    void testByConsentIdAndInstanceId() {
        // Verify ASPSP Consent Entity.
        Optional<ConsentEntity> actualAspspConsent = consentJpaRepository.findOne(
            consentSpecification.byConsentIdAndInstanceId(piisAspspConsent.getExternalId(), INSTANCE_ID)
        );
        assertTrue(actualAspspConsent.isPresent(), "ASPSP consent should be found");
        actualAspspConsent.ifPresent(consent -> {
            assertEquals(piisAspspConsent.getExternalId(), consent.getExternalId(),
                "External IDs must match for ASPSP consent");
            assertEquals(INSTANCE_ID, consent.getInstanceId(),
                "Instance IDs must match for ASPSP consent");
            assertEquals(ConsentType.PIIS_ASPSP.name(), consent.getConsentType(),
                "Consent type must be PIIS_ASPSP for ASPSP consent");
        });

        // Verify TPP Consent Entity.
        Optional<ConsentEntity> actualTppConsent = consentJpaRepository.findOne(
            consentSpecification.byConsentIdAndInstanceId(piisTppConsent.getExternalId(), INSTANCE_ID)
        );
        assertTrue(actualTppConsent.isPresent(), "TPP consent should be found");
        actualTppConsent.ifPresent(consent -> {
            assertEquals(piisTppConsent.getExternalId(), consent.getExternalId(),
                "External IDs must match for TPP consent");
            assertEquals(INSTANCE_ID, consent.getInstanceId(),
                "Instance IDs must match for TPP consent");
            assertEquals(ConsentType.PIIS_TPP.name(), consent.getConsentType(),
                "Consent type must be PIIS_TPP for TPP consent");
        });
    }
}
