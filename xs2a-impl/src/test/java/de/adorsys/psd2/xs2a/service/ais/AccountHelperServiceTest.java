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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.ErrorToActionStatusMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountReferenceMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountHelperServiceTest {
    private static final String ACCOUNT_ID = "accountId";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String CONSENT_ID = "Test consentId";
    private static final boolean WITH_BALANCE = true;

    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE = buildSpiAccountReference();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final List<AccountReference> REFERENCES = Collections.singletonList(XS2A_ACCOUNT_REFERENCE);
    private static final SpiContextData SPI_CONTEXT_DATA = buildSpiContextData();
    private static final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final Xs2aAccountAccess ACCOUNT_ACCESS = createAccountAccess();
    private static final AccountConsent ACCOUNT_CONSENT = createConsent();
    private static final AccountConsent ACCOUNT_CONSENT_FALSE_ONE_ACCESS_TYPE = createConsentWithFalceOneAccessType();

    private static final ActionStatus ACTION_STATUS_SUCCESS = ActionStatus.SUCCESS;
    private static final ActionStatus ACTION_STATUS_CONSENT_INVALID_STATUS = ActionStatus.CONSENT_INVALID_STATUS;
    private static final TypeAccess TYPE_ACCESS_ACCOUNT = TypeAccess.ACCOUNT;

    private static final MessageError CONSENT_INVALID_MESSAGE_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    private static final ResponseObject RESPONSE_OBJECT = ResponseObject.builder().build();
    private static final ResponseObject RESPONSE_OBJECT_WITH_ERROR = ResponseObject.builder().fail(CONSENT_INVALID_MESSAGE_ERROR).build();

    @InjectMocks
    private AccountHelperService accountHelperService;

    @Mock
    private Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    @Mock
    private ErrorToActionStatusMapper errorToActionStatusMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private RequestProviderService requestProviderService;

    private static SpiAccountReference buildSpiAccountReference() {
        return new SpiAccountReference(ACCOUNT_ID, null, null, null, null, null, null);
    }

    @Test
    void findAccountReference_WithNullAccountAccessType() {
        // Given
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE)).thenReturn(SPI_ACCOUNT_REFERENCE);
        // When
        SpiAccountReference actual = accountHelperService.findAccountReference(REFERENCES, ACCOUNT_ID);
        // Then
        assertEquals(SPI_ACCOUNT_REFERENCE, actual);
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    @Test
    void getSpiContextData() {
        // Given
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        // When
        SpiContextData actual = accountHelperService.getSpiContextData();
        // Then
        assertEquals(SPI_CONTEXT_DATA, actual);
    }

    private static SpiContextData buildSpiContextData() {
        return new SpiContextData(new SpiPsuData(null, null, null, null, null), new TppInfo(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData(null, null, null, null);
    }

    @Test
    void createActionStatus_WithThreeArguments() {
        // When
        ActionStatus actual = accountHelperService.createActionStatus(WITH_BALANCE, TYPE_ACCESS_ACCOUNT, RESPONSE_OBJECT);
        // Then
        assertEquals(ACTION_STATUS_SUCCESS, actual);
    }

    @Test
    void createActionStatus_WithThreeArgumentsAndError() {
        // Given
        when(errorToActionStatusMapper.mapActionStatusError(CONSENT_INVALID, WITH_BALANCE, TYPE_ACCESS_ACCOUNT)).thenReturn(ACTION_STATUS_CONSENT_INVALID_STATUS);
        // When
        ActionStatus actual = accountHelperService.createActionStatus(WITH_BALANCE, TYPE_ACCESS_ACCOUNT, RESPONSE_OBJECT_WITH_ERROR);
        // Then
        assertEquals(ACTION_STATUS_CONSENT_INVALID_STATUS, actual);
    }

    @Test
    void needsToUpdateUsage_returnTrue() {
        // When
        boolean actual = accountHelperService.needsToUpdateUsage(ACCOUNT_CONSENT);
        // Then
        assertTrue(actual);
    }

    @Test
    void needsToUpdateUsage_returnFalse() {
        // Given
        when(requestProviderService.isRequestFromTPP()).thenReturn(false);
        // When
        boolean actual = accountHelperService.needsToUpdateUsage(ACCOUNT_CONSENT_FALSE_ONE_ACCESS_TYPE);
        // Then
        assertFalse(actual);
    }

    private static AccountConsent createConsent() {
        return new AccountConsent(CONSENT_ID, ACCOUNT_ACCESS, ACCOUNT_ACCESS, false, LocalDate.now(), null, 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap());
    }

    private static AccountConsent createConsentWithFalceOneAccessType() {
        return new AccountConsent(CONSENT_ID, ACCOUNT_ACCESS, ACCOUNT_ACCESS, true, LocalDate.now(), null, 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap());
    }

    private static Xs2aAccountAccess createAccountAccess() {
        return new Xs2aAccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null, null, null);
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }
}
