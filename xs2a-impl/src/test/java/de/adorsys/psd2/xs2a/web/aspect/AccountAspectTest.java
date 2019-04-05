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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountAspectTest {

    private static final String CONSENT_ID = "some consent id";
    private static final String FORCED_BASE_URL = "http://base.url";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String TPP_ID = "Test TppId";
    private static final String AUTHORITY_ID = "Authority id";
    private static final String IBAN = "DE123456789";
    private static final String BIC = "GENODEF1N02";
    private static final String NAME = "Schmidt";
    private static final String ERROR_TEXT = "Error occurred while processing";
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String RESOURCE_ID = "33333-999999999";
    private final Currency EUR_CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private AccountAspect accountAspect;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private MessageService messageService;

    @Before
    public void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(buildMockHttpServletRequest()));
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings());
    }

    @Test
    public void invokeGetAccountDetailsListAspect_withBalance_shouldAddBalanceLink() {
        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountAspect.getAccountDetailsListAspect(buildXs2aAccountListHolderResponseObject(true), CONSENT_ID, true);

        // Then
        assertNoErrorsAndAccountListPresent(actualResponse);
        assertNotNull(actualResponse.getBody().getAccountDetails().get(0).getLinks().getBalances());
    }

    @Test
    public void invokeGetAccountDetailsListAspect_withoutBalance_shouldAddEmptyLinks() {
        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountAspect.getAccountDetailsListAspect(buildXs2aAccountListHolderResponseObject(false), CONSENT_ID, true);

        // Then
        assertNoErrorsAndAccountListPresent(actualResponse);
        assertNull(actualResponse.getBody().getAccountDetails().get(0).getLinks().getBalances());
    }

    @Test
    public void invokeGetAccountDetailsListAspect_withBalancesAndTransactions_shouldAddAllLinks() {
        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountAspect.getAccountDetailsListAspect(buildXs2aAccountListHolderResponseObjectWithBalancesAndTransactions(), CONSENT_ID, true);

        // Then
        assertNoErrorsAndAccountListPresent(actualResponse);
        Links links = actualResponse.getBody().getAccountDetails().get(0).getLinks();
        assertNotNull(links.getBalances());
        assertNotNull(links.getTransactions());
    }

    @Test
    public void invokeGetAccountDetailsListAspect_withError_shouldAddTextErrorMessage() {
        // Given
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountAspect.getAccountDetailsListAspect(buildXs2aAccountListHolderWithError(), CONSENT_ID, true);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

    private void assertNoErrorsAndAccountListPresent(ResponseObject<Xs2aAccountListHolder> actualResponse) {
        assertFalse(actualResponse.hasError());
        assertFalse(actualResponse.getBody().getAccountDetails().isEmpty());
    }

    private ResponseObject<Xs2aAccountListHolder> buildXs2aAccountListHolderResponseObject(boolean isConsentWithBalance) {
        AccountConsent accountConsent = buildAccountConsent(isConsentWithBalance);
        List<Xs2aAccountDetails> xs2aAccountDetailsList = buildXs2aAccountDetailsList();

        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(xs2aAccountDetailsList, accountConsent);

        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .body(xs2aAccountListHolder)
                   .build();
    }

    private ResponseObject<Xs2aAccountListHolder> buildXs2aAccountListHolderResponseObjectWithBalancesAndTransactions() {
        AccountConsent accountConsent = buildAccountConsentWithBalancesAndTransactions();
        List<Xs2aAccountDetails> xs2aAccountDetailsList = buildXs2aAccountDetailsList();

        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(xs2aAccountDetailsList, accountConsent);

        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .body(xs2aAccountListHolder)
                   .build();
    }

    private List<Xs2aAccountDetails> buildXs2aAccountDetailsList() {
        return Collections.singletonList(
            new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, RESOURCE_ID, IBAN, null, null, null,
                                   null, EUR_CURRENCY, NAME, null,
                                   CashAccountType.CACC, AccountStatus.ENABLED, BIC, "", Xs2aUsageType.PRIV, "", null));
    }

    private ResponseObject<Xs2aAccountListHolder> buildXs2aAccountListHolderWithError() {
        return ResponseObject.<Xs2aAccountListHolder>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                   .build();
    }

    private AccountConsent buildAccountConsent(boolean isConsentWithBalance) {
        Xs2aAccountAccess xs2aAccountAccess;
        if (isConsentWithBalance) {
            xs2aAccountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.singletonList(buildReference()), Collections.emptyList(), null, null);
        } else {
            xs2aAccountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);
        }
        return new AccountConsent(null, xs2aAccountAccess, false, LocalDate.now().plusDays(1), 10,
                                  null, ConsentStatus.VALID, false, false,
                                  null, TPP_INFO, null, false, Collections.emptyList(), OffsetDateTime.now(), 10);
    }

    private AccountConsent buildAccountConsentWithBalancesAndTransactions() {
        Xs2aAccountAccess xs2aAccountAccess = new Xs2aAccountAccess(Collections.singletonList(buildReference()), Collections.singletonList(buildReference()), Collections.singletonList(buildReference()), null, null);
        return new AccountConsent(null, xs2aAccountAccess, false, LocalDate.now().plusDays(1), 10,
                                  null, ConsentStatus.VALID, false, false,
                                  null, TPP_INFO, null, false, Collections.emptyList(), OffsetDateTime.now(), 10);
    }

    private AspspSettings buildAspspSettings() {
        return new AspspSettings(1, false, false, null, null,
                                 null, false, null, null,
                                 1, 1, false, false, false,
                                 false, false, false, 1,
                                 null, 1, 1,
                                 null, 1, false,
                                 false, false, false, FORCED_BASE_URL);
    }

    private MockHttpServletRequest buildMockHttpServletRequest() {
        return new MockHttpServletRequest();
    }

    private AccountReference buildReference() {
        AccountReference reference = new AccountReference();
        reference.setResourceId(RESOURCE_ID);
        reference.setIban(IBAN);
        reference.setCurrency(EUR_CURRENCY);
        return reference;
    }

    public static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setAuthorityId(AUTHORITY_ID);
        tppInfo.setTppRedirectUri(new TppRedirectUri("", ""));
        return tppInfo;
    }

}
