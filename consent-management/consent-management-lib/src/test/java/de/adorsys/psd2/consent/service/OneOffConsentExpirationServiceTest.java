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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.service.mapper.CmsAisConsentMapper;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OneOffConsentExpirationServiceTest {
    private static final Long CONSENT_ID = 123358L;
    private static final String RESOURCE_ID = "LGCGDC4KTx0tgnpZGYTTr8";
    public static final String BENEFICIARIES_URI = "/v1/trusted-beneficiaries";
    public static final String INSTANCE_ID = "bank1";

    @InjectMocks
    private OneOffConsentExpirationService oneOffConsentExpirationService;

    @Mock
    private AisConsentUsageRepository aisConsentUsageRepository;
    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;
    @Mock
    private CmsAisConsentMapper cmsAisConsentMapper;
    @Mock
    private AspspProfileService aspspProfileService;

    @Spy
    private final ConsentDataMapper consentDataMapper = new ConsentDataMapper();

    private final JsonReader jsonReader = new JsonReader();
    private AisConsentTransaction aisConsentTransaction;
    private CmsConsent cmsConsent;
    private AccountReference accountReference;
    private AspspSettings aspspSettings;

    @BeforeEach
    void setUp() {
        accountReference = jsonReader.getObjectFromFile("json/service/account-reference.json", AccountReference.class);
        aisConsentTransaction = new AisConsentTransaction();
        cmsConsent = new CmsConsent();
        cmsConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setInstanceId(INSTANCE_ID);
        aspspSettings = jsonReader.getObjectFromFile("json/AspspSetting.json", AspspSettings.class);
    }

    @Test
    void isConsentExpired_multipleAccounts_partiallyUsed_shouldReturnFalse() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(0);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_multipleAccounts_fullyUsed_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_bookingStatusAllIsPresent_shouldReturnTrue() {
        // Given
        aspspSettings.getAis().getTransactionParameters().getAvailableBookingStatuses().add(BookingStatus.ALL);
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);
        aisConsentTransaction.setBookingStatus(BookingStatus.ALL);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_bookingStatusBothIsPresent_shouldReturnTrue() {
        // Given
        aspspSettings.getAis().getTransactionParameters().getAvailableBookingStatuses().add(BookingStatus.BOTH);
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);
        aisConsentTransaction.setBookingStatus(BookingStatus.BOTH);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_allAvailableAccounts_shouldReturnTrue() {
        // Given
        AisConsent aisConsent = buildAisConsent(accountReference, null, null, ALL_ACCOUNTS, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_bankOffered_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = buildAisConsent(null, null, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_notUsed_shouldReturnFalse() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(0);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_fullyUsed_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithBalances_partiallyUsed_shouldReturnFalse() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(null, accountReference, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithBalances_fullyUsed_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(2);

        AisConsent aisConsent = buildAisConsent(null, accountReference, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithTransactions_partiallyUsed_shouldReturnFalse() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(null, null, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithTransactions_fullyUsed_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(5);

        AisConsent aisConsent = buildAisConsent(null, null, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_withBeneficiaries_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, Pageable.unpaged()))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(4);
        when(aisConsentUsageRepository.countByConsentIdAndRequestUri(CONSENT_ID, BENEFICIARIES_URI)).thenReturn(2);

        AisConsent aisConsent = jsonReader.getObjectFromFile("json/ais-consent-dedicated-with-beneficiaries.json", AisConsent.class);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(4);

        AisConsent aisConsent = buildAisConsent(accountReference, accountReference, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_GlobalConsent_shouldReturnTrue() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(aspspSettings);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(4);

        AisConsent aisConsent = buildAisConsent(accountReference, accountReference, accountReference, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    private AisConsent buildAisConsent(AccountReference account,
                                       AccountReference balance,
                                       AccountReference transaction,
                                       AccountAccessType availableAccounts,
                                       AccountAccessType allPsd2) {
        AisConsent aisConsent = new AisConsent();

        AccountAccess accountAccess = new AccountAccess(
            Optional.ofNullable(account).map(Collections::singletonList).orElseGet(Collections::emptyList),
            Optional.ofNullable(balance).map(Collections::singletonList).orElseGet(Collections::emptyList),
            Optional.ofNullable(transaction).map(Collections::singletonList).orElseGet(Collections::emptyList),
            null);

        aisConsent.setAspspAccountAccesses(accountAccess);
        aisConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);

        AisConsentData consentData = new AisConsentData(
            Optional.ofNullable(availableAccounts).orElse(null),
            Optional.ofNullable(allPsd2).orElse(null),
            null,
            false);

        aisConsent.setConsentData(consentData);
        return aisConsent;
    }
}
