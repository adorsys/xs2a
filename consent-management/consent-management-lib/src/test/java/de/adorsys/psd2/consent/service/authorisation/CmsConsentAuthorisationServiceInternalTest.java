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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsConsentAuthorisationServiceInternalTest {

    private static final String EXTERNAL_ID = "2384a6a0-8f02-4b79-aeab-00aa7e03a0r2";
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String INSTANCE_ID = "test-instance-id";

    @InjectMocks
    private CmsConsentAuthorisationServiceInternal cmsConsentAuthorisationServiceInternal;

    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AuthorisationSpecification authorisationSpecification;

    private JsonReader jsonReader = new JsonReader();
    private AuthorisationEntity authorisationEntity;
    private AuthenticationDataHolder authenticationDataHolder;

    @BeforeEach
    void setUp() {
        authorisationEntity = jsonReader.getObjectFromFile("json/authorisation-entity.json", AuthorisationEntity.class);
        authenticationDataHolder = jsonReader.getObjectFromFile("json/authentication-data-holder.json", AuthenticationDataHolder.class);
    }

    @Test
    void getAuthorisationByExternalId_success() throws AuthorisationIsExpiredException {
        authorisationEntity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));

        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(authorisationEntity));

        Optional<AuthorisationEntity> actual = cmsConsentAuthorisationServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);

        assertTrue(actual.isPresent());
        assertEquals(authorisationEntity, actual.get());
    }

    @Test
    void getAuthorisationByExternalId_notFound_empty() throws AuthorisationIsExpiredException {
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(authorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        Optional<AuthorisationEntity> actual = cmsConsentAuthorisationServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);

        assertTrue(actual.isEmpty());
    }

    @Test
    void getAuthorisationByExternalId_expired_error() {
        authorisationEntity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().minusDays(1));

        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(authorisationEntity));

        assertThrows(AuthorisationIsExpiredException.class,
                     () -> cmsConsentAuthorisationServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID));
    }

    @Test
    void updateScaStatusAndAuthenticationData_success() {
        authorisationEntity.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        authorisationEntity.setScaAuthenticationData("none");
        authorisationEntity.setAuthenticationMethodId("none");

        boolean actual = cmsConsentAuthorisationServiceInternal.updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, authorisationEntity, authenticationDataHolder);

        assertTrue(actual);
        assertEquals(authenticationDataHolder.getAuthenticationData(), authorisationEntity.getScaAuthenticationData());
        assertEquals(authenticationDataHolder.getAuthenticationMethodId(), authorisationEntity.getAuthenticationMethodId());
        assertEquals(ScaStatus.RECEIVED, authorisationEntity.getScaStatus());
    }

    @Test
    void updateScaStatusAndAuthenticationData_statusFinalised() {
        authorisationEntity.setScaStatus(ScaStatus.FAILED);
        boolean actual = cmsConsentAuthorisationServiceInternal.updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, authorisationEntity, authenticationDataHolder);
        assertFalse(actual);
    }

    @Test
    void getAuthorisationsByParentExternalId() {
        cmsConsentAuthorisationServiceInternal.getAuthorisationsByParentExternalId(EXTERNAL_ID);
        verify(authorisationRepository, times(1)).findAllByParentExternalIdAndType(EXTERNAL_ID, AuthorisationType.CONSENT);
    }

    @Test
    void getAuthorisationByRedirectId_success() throws RedirectUrlIsExpiredException {
        authorisationEntity.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plusDays(1));

        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(authorisationEntity));

        Optional<AuthorisationEntity> actual = cmsConsentAuthorisationServiceInternal.getAuthorisationByRedirectId(AUTHORISATION_ID, INSTANCE_ID);

        assertTrue(actual.isPresent());
        assertEquals(authorisationEntity, actual.get());
    }

    @Test
    void getAuthorisationByRedirectId_redirectUrlIsExpired() {
        authorisationEntity.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().minusDays(1));

        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(authorisationEntity));

        assertThrows(RedirectUrlIsExpiredException.class,
                     () -> cmsConsentAuthorisationServiceInternal.getAuthorisationByRedirectId(AUTHORISATION_ID, INSTANCE_ID));

        assertEquals(ScaStatus.FAILED, authorisationEntity.getScaStatus());
    }
}
